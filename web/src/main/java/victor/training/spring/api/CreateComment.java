package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import static java.util.UUID.randomUUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreateComment { // #5
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RestTemplate restTemplate;

  public record CreateCommentRequest(String comment) {
  }
  @PreAuthorize("isAuthenticated()")
  @PostMapping("posts/{postId}/comments")
  public void createComment(@PathVariable String postId, @RequestBody CreateCommentRequest request) {
    Post post = postRepo.findById(postId).orElseThrow();
    boolean safe = checkOffensive(post.getBody(), request.comment);
    boolean authorAllows = checkAuthorAllowsComments(post.getAuthorId());
    if (safe && authorAllows) {
      commentRepo.save(createComment(request.comment, post.getId()));
    } else {
      throw new IllegalArgumentException("Comment Rejected");
    }
  }

  private static Comment createComment(String comment, String postId) {
    String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
    return new Comment()
        .setId(randomUUID().toString())
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
}
