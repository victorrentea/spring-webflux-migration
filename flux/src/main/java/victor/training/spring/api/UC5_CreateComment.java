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
    // TODO check if comments are allowed for the post author (authorAllowsComments)
    return postRepo.findById(postId)

        .filterWhen(post -> Mono.zip(
            authorAllowsComments(post.authorId()),
            isSafe(post.body(), request.comment()),
            Boolean::logicalAnd)

        )

        // only posts that allow comments will pass through
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment Rejected")))

        .flatMap(post -> commentRepo.save(new Comment(post.id(), request.comment(), request.name())))
        .then();
  }

  private Mono<Boolean> authorAllowsComments(long authorId) {
    String url = "http://localhost:9999/authoasdsadasdasr/" + authorId + "/comments-allowed";
//    Hooks.onOperatorDebug(); // SUPER EXPENSIVE, only for desperate debugging.

    Mono<String> result = webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)

//        .doOnSubscribe()
//        .log
//        .checkpoint("isCommentUnlocked",// printed next to any exception ABOVE
//            true)
        ;  // EXPENSIVE CPU, stacktrace captured at chain ASSEMBLY, not at actual execution
    // stacktraces of exceptions in reactive chains don't include the method that CREATED the chain/any callers
    return result.map(Boolean::parseBoolean);
  }

  private Mono<Boolean> isSafe(String body, String comment) {
    record Request(String body, String comment) {
    }
    String url = "http://localhost:9999/safety-check";
    Mono<String> result = webClient.post()
        .uri(url)
        .bodyValue(new Request(body, comment))
        .retrieve()
        .bodyToMono(String.class)

        .name("isCommentSafe") // you canfind this /actuator/prometheus
//        first metric = Δt between subscribe-->complete
        .tap(Micrometer.metrics(meterRegistry)); // replacement of @Timed
    return result.map("OK"::equals);
  }
}
