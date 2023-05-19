package victor.training.spring.api;

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

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreateComment { // #5
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final WebClient webClient;

  public record CreateCommentRequest(String comment) {
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("posts/{postId}/comments")
  public Mono<Void> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
    return findPost(postId)
        .flatMap(p -> Mono.zip(
                checkOffensive(p.getBody(), request.comment),
                checkAuthorAllowsComments(p.getAuthorId()),
                Boolean::logicalAnd)
            .flatMap(CreateComment::checkCommentApproved)
            .flatMap(b -> createComment(request.comment, p.getId()))
            .flatMap(this::saveComment))
        .doOnNext(comment -> log.info("Saved this: " + comment))
        .then();
  }

  private static Mono<Comment> createComment(String comment, String postId) {
    return ReactiveSecurityContextHolder.getContext()
        .map(c -> c.getAuthentication().getName())
        .map(u -> new Comment()
            .setId(UUID.randomUUID().toString())
            .setName(u)
            .setComment(comment)
            .setPostId(postId));
  }

  private static Mono<Boolean> checkCommentApproved(Boolean b) {
    if (!b) {
      return Mono.error(new IllegalArgumentException("Comment Rejected"));
    } else {
      return Mono.just(b);
    }
  }


  private Mono<Comment> saveComment(Comment comment) {
    return commentRepo.save(comment);
  }

  private Mono<Post> findPost(Long postId) {
    return postRepo.findById(postId)
        .doOnSubscribe(s -> log.info("DB Query"));
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
        .retrieve().bodyToMono(String.class)
        .map("OK"::equals);
  }
}
