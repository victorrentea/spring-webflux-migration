package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

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
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    // TODO create the post, then create an initial comment "Posted on {date}: {post title}" by the current user
    return null;
  }


  private final Sender sender;
  public Mono<Void> sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    OutboundMessage outboundMessage = new OutboundMessage("", "post-created-event", message.getBytes());
    sender.sendWithPublishConfirms(Mono.just(outboundMessage));
    return null;
  }
}
