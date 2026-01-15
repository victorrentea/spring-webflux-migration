package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC4_CreatePost {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post().title(title).body(body).authorId(authorId);
    }
  }

  @PostMapping("posts")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public void createPost(@RequestBody CreatePostRequest request) {
    Post post = postRepo.save(request.toPost());
    commentRepo.save(createInitialComment(post.id(), request.title()));
    sendPostCreatedEvent("Post created: " + post.id());
  }
  // TODO triangle

  private static Comment createInitialComment(long postId, String postTitle) {
    String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
    return new Comment(postId, "Posted on " + now() + ": " + postTitle, loggedInUser);
  }

  private final RabbitTemplate rabbitTemplate;
  private void sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    rabbitTemplate.convertAndSend("post-created-event", message);
  }
}
