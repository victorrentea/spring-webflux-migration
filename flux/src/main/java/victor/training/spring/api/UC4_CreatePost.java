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
import victor.training.spring.rabbit.RabbitSender;
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
  private final RabbitSender rabbitSender;

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post().title(title).body(body).authorId(authorId);
    }
  }

  @PostMapping("posts")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    Mono<Post> postMono = postRepo.save(request.toPost());
    return postMono.flatMap(post -> Mono.zip(
                rabbitSender.sendPostCreatedEvent("Post created: " + post.id()).thenReturn(1),
                createInitialComment(post.id()).flatMap(commentRepo::save)
            )
        )
        .then();
  }

  private static Mono<Comment> createInitialComment(Long postId) {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication().getName())
        .map(loggedInUser -> new Comment(postId, "Posted on " + now(), loggedInUser));
  }
}
