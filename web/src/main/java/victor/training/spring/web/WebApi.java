package victor.training.spring.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.web.WebApi.GetPostByIdResponse.CommentResponse;
import victor.training.spring.web.hibernate.Comment;
import victor.training.spring.web.hibernate.CommentRepo;
import victor.training.spring.web.hibernate.Post;
import victor.training.spring.web.hibernate.PostRepo;
import victor.training.spring.web.mongo.Author;
import victor.training.spring.web.mongo.AuthorRepo;
import victor.training.spring.web.rabbit.RabbitSender;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebApi {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RabbitSender rabbitSender;
  private final AuthorRepo authorRepo;
  private final RestTemplate restTemplate;

  @PostConstruct
  public void initialDataInMongo() {
    authorRepo.save(new Author().setId(1000L).setName("John DOE").setBio("Long description"));
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }
  @GetMapping("posts")
  public List<GetPostsResponse> getAllPosts() {
    return postRepo.findAll().stream().map(GetPostsResponse::new).toList();
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
  public GetPostByIdResponse getPostById(@PathVariable Long postId) {
    Post post = postRepo.findById(postId).orElseThrow();
    List<CommentResponse> comments = commentRepo.findByPostId(postId).stream()
        .map(CommentResponse::new)
        .toList();
    return new GetPostByIdResponse(post, comments);
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
  public void createPost(@RequestBody CreatePostRequest request) {
    Post post = postRepo.save(request.toPost());
    commentRepo.save(new Comment().setPostId(post.getId()).setComment("Posted on " + now()));
    rabbitSender.sendMessage("Post created: " + post.getId());
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  public record GetAuthorsResponse(Long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.getId(), author.getName(),email, author.getBio());
    }
  }
  @GetMapping("authors")
  public List<GetAuthorsResponse> getAllAuthors() {
    List<GetAuthorsResponse> list = new ArrayList<>();
    for (Author author : authorRepo.findAll()) {
      String email = fetchEmail(author.getId());
      list.add(new GetAuthorsResponse(author, email));
    }
    return list;
  }

  private String fetchEmail(Long authorId) {
    return restTemplate.getForObject("http://localhost:9999/contact/" + authorId + "/email", String.class);
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  record CreateCommentRequest(String comment) {
  }
  @PreAuthorize("isAuthenticated()")
  @PostMapping("post/{postId}/comments")
  public void createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
    Post post = postRepo.findById(postId).orElseThrow();
    boolean safe = checkOffensive(post.getBody(), request.comment);
    boolean authorAllows = checkAuthorAllowsComments(post.getAuthorId());
    if (safe && authorAllows) {
      commentRepo.save(createComment(request.comment, post.getId()));
    } else {
      throw new IllegalArgumentException("Comment Rejected");
    }
  }

  private static Comment createComment(String comment, Long postId) {
    String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
    return new Comment()
        .setName(loggedInUser)
        .setComment(comment)
        .setPostId(postId);
  }

  private boolean checkAuthorAllowsComments(Long authorId) {
    String result = restTemplate.getForObject("http://localhost:9999/author/" + authorId + "/comments", String.class);
    return Boolean.parseBoolean(result);
  }

  private boolean checkOffensive(String body, String comment) {
    record SafetyCheckRequest(String body, String comment) {
    }
    String result = restTemplate.postForObject("http://localhost:9999/safety-check", new SafetyCheckRequest(body, comment), String.class);
    return "OK".equals(result);
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
