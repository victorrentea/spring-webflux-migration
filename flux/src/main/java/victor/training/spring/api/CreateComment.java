package victor.training.spring.api;

import static java.util.UUID.randomUUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreateComment { // #5
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final WebClient webClient;

  public record CreateCommentRequest(String comment) {}

  @PreAuthorize("isAuthenticated()")
  @PostMapping("posts/{postId}/comments")
  public Mono<Void> createComment(
      @PathVariable String postId, @RequestBody CreateCommentRequest request) {

    return postRepo
        .findById(postId)
        .switchIfEmpty(Mono.error(IllegalArgumentException::new))
        .delayUntil(post -> approveComment(request, post))
        .flatMap(post -> createComment(request.comment, post.getId()))
        .flatMap(comment -> commentRepo.save(comment))
        .then();
  }

  private Mono<Boolean> approveComment(CreateCommentRequest request, Post post) {
//    List<Mono<Boolean>> maiMulti = List.of(f1(post),f2(post),f2(post));
//    Mono<Boolean> gata = Flux.concat(Flux.fromIterable(maiMulti))
//        .reduce(true, Boolean::logicalAnd);
    return Mono.zip(
            checkOffensive(post.getBody(), request.comment),
            checkAuthorAllowsComments(post.getAuthorId()),
            Boolean::logicalAnd)
        .flatMap(
            b -> {
              if (!b)
                return Mono.error(new IllegalArgumentException("Comment Rejected"));
              else return Mono.just(b);
            });
  }

  private Mono<Boolean> f1(Post post) {
    throw new RuntimeException("Method not implemented");
  }
  private Mono<Boolean> f2(Post post) {
    throw new RuntimeException("Method not implemented");
  }

  private static Mono<Comment> createComment(String comment, String postId) {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> {
          String loggedInUser = context.getAuthentication().getName();
          return new Comment()
              .setNew(true)
              .setId(randomUUID().toString())
              .setName(loggedInUser)
              .setComment(comment)
              .setPostId(postId);
        });
  }

  private Mono<Boolean> checkAuthorAllowsComments(Long authorId) {
    return webClient
        .get()
        .uri("http://localhost:9999/author/" + authorId + "/comments")
        .retrieve()
        .bodyToMono(String.class)
        .map(Boolean::parseBoolean);
  }

  private Mono<Boolean> checkOffensive(String body, String comment) {
    record SafetyCheckRequest(String body, String comment) {}
    return webClient
        .post()
        .uri("http://localhost:9999/safety-check")
        .bodyValue(new SafetyCheckRequest(body, comment))
        .retrieve()
        .bodyToMono(String.class)
        .map("OK"::equals);
  }
}
