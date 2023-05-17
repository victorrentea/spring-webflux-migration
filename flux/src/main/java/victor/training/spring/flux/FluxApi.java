package victor.training.spring.flux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.spring.flux.FluxApi.GetPostByIdResponse.CommentResponse;
import victor.training.spring.flux.hibernate.Comment;
import victor.training.spring.flux.hibernate.CommentRepo;
import victor.training.spring.flux.hibernate.Post;
import victor.training.spring.flux.hibernate.PostRepo;
import victor.training.spring.flux.mongo.Author;
import victor.training.spring.flux.mongo.AuthorRepo;
import victor.training.spring.flux.rabbit.RabbitSender;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import static java.time.LocalDateTime.now;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FluxApi {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RabbitSender rabbitSender;
  private final AuthorRepo authorRepo;
  private final WebClient webClient;

  @PostConstruct
  public void initialDataInMongo() {
    authorRepo.save(new Author().setId(1000L).setName("John DOE").setBio("Long description")).block();
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }

  @GetMapping("posts")
  public Flux<GetPostsResponse> posts() {
    return Mono.fromCallable(postRepo::findAll)
        .subscribeOn(boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .map(GetPostsResponse::new);
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  public record GetPostByIdResponse(Long id, String title, String body, List<CommentResponse> comments) {
    GetPostByIdResponse(Post post, List<CommentResponse> comments) {
      this(post.getId(), post.getTitle(), post.getBody(), comments);
    }

    public record CommentResponse(String comment, String name) {
      CommentResponse(Comment comment) {
        this(comment.getComment(), comment.getName());
      }
    }
  }

  @GetMapping("posts/{postId}")
  public Mono<GetPostByIdResponse> getById(@PathVariable Long postId) {
    return Mono.fromCallable(() -> postRepo.findById(postId).orElseThrow())
        .subscribeOn(boundedElastic())
        .zipWith(findComments(postId), GetPostByIdResponse::new);
  }

  private Mono<List<CommentResponse>> findComments(Long postId) {
    return Flux.fromStream(() -> commentRepo.findByPostId(postId).stream())
        .subscribeOn(boundedElastic())
        .map(CommentResponse::new)
        .collectList();
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post()
          .setTitle(title)
          .setBody(body)
          .setAuthorId(authorId);
    }
  }

  @PostMapping("posts")
  @Transactional
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    return Mono.fromCallable(() -> {
          Post post = postRepo.save(request.toPost());
          commentRepo.save(new Comment().setPostId(post.getId()).setComment("Posted on " + now()));
          return post;
        })
        .subscribeOn(boundedElastic())
        .flatMap(post -> rabbitSender.sendMessage("Post created: " + post.getId()));
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  public record GetAuthorsResponse(Long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.getId(), author.getName(), email, author.getBio());
    }
  }

  @GetMapping("authors")
  public Flux<GetAuthorsResponse> getAuthors() {
    return authorRepo.findAll()
        .flatMap(a -> fetchEmail(a.getId()).map(e -> new GetAuthorsResponse(a, e)));
  }

  private Mono<String> fetchEmail(Long authorId) {
    return webClient.get()
        .uri("http://localhost:9999/contact/" + authorId + "/email")
        .retrieve().bodyToMono(String.class);
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  record CreateCommentRequest(String comment) {
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("post/{postId}/comments")
  public Mono<Void> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request, Principal principal) {
    return Mono.fromCallable(() -> postRepo.findById(postId).orElseThrow())
        .subscribeOn(boundedElastic())
        .flatMap(p -> Mono.zip(
                checkOffensive(p.getBody(), request.comment),
                checkAuthorAllowsComments(p.getAuthorId()),
                Boolean::logicalAnd)
            .flatMap(FluxApi::checkAccepted)
            .doOnNext(e -> log.info("accepted: " + e))
            .then(createComment(request.comment, postId))
            .doOnNext(e -> log.info("comment: " + e))
            .flatMap(c -> Mono.fromRunnable(() -> commentRepo.save(c))
                .subscribeOn(boundedElastic()))
        ).then();
  }

  private static Mono<Comment> createComment(String comment, Long postId) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName)
        .map(loggedInUser -> new Comment().setName(loggedInUser)
            .setComment(comment)
            .setPostId(postId));
  }

  private static Mono<Boolean> checkAccepted(Boolean accepted) {
    if (!accepted) {
      return Mono.error(new IllegalArgumentException("Comment Rejected"));
    } else {
      return Mono.just(accepted);
    }
  }

  private Mono<Boolean> checkAuthorAllowsComments(Long authorId) {
    return webClient.get().uri("http://localhost:9999/author/" + authorId + "/comments")
        .retrieve()
        .bodyToMono(String.class)
        .map(Boolean::parseBoolean);
  }

  private Mono<Boolean> checkOffensive(String body, String comment) {
    record SafetyCheckRequest(String body, String comment) {
    }
    return webClient.post().uri("http://localhost:9999/safety-check")
        .bodyValue(new SafetyCheckRequest(body, comment))
        .retrieve()
        .bodyToMono(String.class)
        .map("OK"::equals);
  }

  //region redirect / to /swagger-ui.html
  @GetMapping
  public ResponseEntity<Void> redirectRootToSwagger() {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create("/swagger-ui.html"));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
  //endregion
}
