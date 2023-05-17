package victor.training.spring.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.web.hibernate.Comment;
import victor.training.spring.web.hibernate.CommentRepo;
import victor.training.spring.web.hibernate.Post;
import victor.training.spring.web.hibernate.PostRepo;
import victor.training.spring.web.mongo.AuthorBio;
import victor.training.spring.web.mongo.AuthorBioRepo;
import victor.training.spring.web.rabbit.RabbitSender;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.security.Principal;
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
  private final AuthorBioRepo authorBioRepo;
  private final RestTemplate restTemplate;

  @PostConstruct
  public void initialDataInMongo() {
    authorBioRepo.save(new AuthorBio().setId(1000L).setName("John DOE").setBio("Long description"));
  }

  public record GetPostsResponse(Long id, String title) {
  }

  @GetMapping("posts")
  public List<GetPostsResponse> posts() {
    return postRepo.findAll().stream().map(post -> new GetPostsResponse(post.getId(), post.getTitle())).toList();
  }

  public record GetPostByIdResponse(Long id, String title, String body, List<GetPostByIdResponseComment> comments) {
  }
  public record GetPostByIdResponseComment(String comment, String name) {
  }

  @GetMapping("posts/{postId}")
  public GetPostByIdResponse getById(@PathVariable Long postId) {
    Post post = postRepo.findById(postId).orElseThrow();
    List<GetPostByIdResponseComment> comments = commentRepo.findByPostId(postId).stream()
        .map(c -> new GetPostByIdResponseComment(c.getComment(), c.getName()))
        .toList();
    return new GetPostByIdResponse(post.getId(), post.getTitle(), post.getBody(), comments);
  }

  public record CreatePostRequest(String title, String body, Long authorId) {
  }

  @PostMapping("posts")
  @Transactional
  public void createPost(@RequestBody CreatePostRequest request) {
    Post post = postRepo.save(new Post()
        .setTitle(request.title)
        .setBody(request.body)
        .setAuthorId(request.authorId));
    commentRepo.save(new Comment().setPost(post).setComment("Posted on " + now()));

    rabbitSender.sendMessage("Post created: " + post.getId());
  }

  public record GetAuthorsResponse(Long id, String name, String email, String bio) {
  }

  @GetMapping("authors")
  public List<GetAuthorsResponse> getAuthors() {
    List<GetAuthorsResponse> list = new ArrayList<>();
    for (AuthorBio a : authorBioRepo.findAll()) {
      String email = fetchEmail(a.getId());
      GetAuthorsResponse getAuthorsResponse = new GetAuthorsResponse(a.getId(), a.getName(), email, a.getBio());
      list.add(getAuthorsResponse);
    }
    return list;
  }

  private String fetchEmail(Long authorId) {
    return restTemplate.getForObject("http://localhost:9999/contact/" + authorId + "/email", String.class);
  }

  record CreateCommentRequest(String comment) {
  }

  @PostMapping("post/{postId}/comments")
  public void createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request, Principal principal) {
    Post post = postRepo.findById(postId).orElseThrow();
    boolean safe = checkOffensive(post.getBody(), request.comment);
    boolean authorAllows = checkAuthorAllowsComments(post.getAuthorId());
    if (safe && authorAllows) {
      String name = principal != null ? principal.getName() : "anonymous";
      commentRepo.save(new Comment().setName(name).setComment(request.comment).setPost(post));
    } else {
      throw new IllegalArgumentException("Comment Rejected");
    }
  }

  private boolean checkAuthorAllowsComments(Long authorId) {
    return Boolean.parseBoolean(restTemplate.getForObject("http://localhost:9999/author/" + authorId + "/comments", String.class));
  }

  private boolean checkOffensive(String body, String comment) {
    record SafetyCheckRequest(String body, String comment){}
    String result = restTemplate.postForObject("http://localhost:9999/safety-check", new SafetyCheckRequest(body, comment), String.class);
    return "OK".equals(result);
  }

  @GetMapping
  public ResponseEntity<Void> redirectRootToSwagger() {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create("/swagger-ui.html"));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}
