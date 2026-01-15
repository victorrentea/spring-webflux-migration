package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
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
  @PreAuthorize("isAuthenticated()") // ✅
  @Transactional // ✅
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    return postRepo.save(request.toPost())
        .delayUntil(post->createInitialComment(post.id(), request.title())
            .flatMap(commentRepo::save))
        .flatMap(post->sendPostCreatedEvent("Post created: " + post.id()))
        .then();
  }
  private static Mono<Comment> createInitialComment(long postId, String postTitle) {
    return ReactiveSecurityContextHolder.getContext() // has to be subscribed in the same chain that Spring subscribed to, ie the Publisher returned from the @RestController method
        .map(securityContext -> securityContext.getAuthentication().getName())
        .map(username -> new Comment(postId, "Posted on " + now() + ": " + postTitle, username));
  }

  private final Sender reactiveRabbitSender;
  public Mono<Void> sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    OutboundMessage outboundMessage = new OutboundMessage("", "post-created-event", message.getBytes());
    return reactiveRabbitSender.sendWithPublishConfirms(Mono.just(outboundMessage)).then();
  }
}
