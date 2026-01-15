package victor.training.spring.api;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC5_CreateComment {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final WebClient webClient;
  private final MeterRegistry meterRegistry;

  public record CreateCommentRequest(String comment, String name) {
  }

  @PostMapping("posts/{postId}/comments")
  public Mono<Void> createComment(@PathVariable long postId, @RequestBody CreateCommentRequest request) {
    return postRepo.findById(postId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found")))
        .filterWhen(post -> Mono.zip(
            isUnlocked(post.authorId()),
            isSafe(post.body(), request.comment()),
            Boolean::logicalAnd))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment Rejected")))
        .map(post -> new Comment(post.id(), request.comment(), request.name()))
        .flatMap(commentRepo::save)
        .then();
  }

  private Mono<Boolean> isUnlocked(long authorId) {
    Mono<String> result = webClient.get()
        .uri("http://localhost:9999/author/{authorId}/comments-allowed", authorId)
        .retrieve()
        .bodyToMono(String.class)
        .checkpoint("isCommentUnlocked");
    return result.map(Boolean::parseBoolean);
  }

  private Mono<Boolean> isSafe(String body, String comment) {
    record Request(String body, String comment) {
    }
    Mono<String> result = webClient.post()
        .uri("http://localhost:9999/safety-check")
        .bodyValue(new Request(body, comment))
        .retrieve()
        .bodyToMono(String.class)

        .name("isCommentSafe")
        .tap(Micrometer.metrics(meterRegistry));
    return result.map("OK"::equals);
  }
}
