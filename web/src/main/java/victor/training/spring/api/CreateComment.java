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
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.PostRepo;
import victor.training.spring.table.tables.records.CommentRecord;
import victor.training.spring.table.tables.records.PostRecord;

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
  public void createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
      PostRecord post = postRepo.findById(postId).orElseThrow();
      boolean safe = checkOffensive(post.getBody(), request.comment);
      boolean authorAllows = checkAuthorAllowsComments(post.getAuthorId());
      if (safe && authorAllows) {
        commentRepo.save(createComment(request.comment, postId));
      } else {
        throw new IllegalArgumentException("Comment Rejected");
      }
  }

  private static CommentRecord createComment(String commentText, Long postId) {
    String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
    System.out.println("Link to postId: " + postId);
    CommentRecord comment = new CommentRecord();
    comment.setName(loggedInUser);
    comment.setComment(commentText);
    comment.setPostId(postId);;
    return comment;
  }

  private boolean checkAuthorAllowsComments(Long authorId) {
    String result = restTemplate.getForObject("http://localhost:9999/author/" + authorId + "/comments", String.class);
    System.out.println("Calling " + "http://localhost:9999/author/" + authorId + "/comments got " + result );
    return Boolean.parseBoolean(result);
  }

  private boolean checkOffensive(String body, String comment) {
    record SafetyCheckRequest(String body, String comment) {
    }
    String result = restTemplate.postForObject("http://localhost:9999/safety-check", new SafetyCheckRequest(body, comment), String.class);
    return "OK".equals(result);
  }
}
