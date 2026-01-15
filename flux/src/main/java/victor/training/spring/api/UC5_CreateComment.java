package victor.training.spring.api;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.lib.BlockingLib;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.PostRepo;

import java.time.Duration;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC5_CreateComment {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final WebClient webClient;
  private final MeterRegistry meterRegistry;
  private final ReactiveStringRedisTemplate redisTemplate;

  public record CreateCommentRequest(String comment, String name) {
  }

  @PostMapping("posts/{postId}/comments")
  public Mono<Void> createComment(@PathVariable long postId, @RequestBody CreateCommentRequest request) {
    return postRepo.findById(postId)
        .switchIfEmpty(Mono.error(new NoSuchElementException()))
        .filterWhen(post -> Mono.zip(
            isUnlockedCached(post.authorId()),
            isSafe(post.body(), request.comment()),
            Boolean::logicalAnd))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment Rejected")))
        .map(post -> new Comment(post.id(), request.comment(), request.name()))
        .flatMap(commentRepo::save)
        .then();
  }

  private Mono<Boolean> isUnlockedCached(long authorId) {
    String cacheKey = "flux:author-unlocked:" + authorId;
    return redisTemplate.opsForValue().get(cacheKey)
        .map(Boolean::parseBoolean)
        .doOnNext(v -> log.info("Redis cache hit for author-unlocked:{}", authorId))
        .switchIfEmpty(Mono.defer(() -> isUnlocked(authorId)
            .flatMap(result -> redisTemplate.opsForValue().set(cacheKey, String.valueOf(result), Duration.ofMinutes(10))
                .thenReturn(result))
            .doOnSubscribe(s -> log.info("Redis cache miss for author-unlocked:{}", authorId))));
  }

  private Mono<Boolean> isUnlocked(long authorId) {
    return webClient.get()
        .uri("http://localhost:9999/author/{authorId}/comments-allowed", authorId)
        .retrieve()
        .bodyToMono(String.class)
        .name("isUnlocked") // exposed via /actuator/prometheus
        .tap(Micrometer.metrics(meterRegistry))
        .checkpoint("isUnlocked")
        .map(Boolean::parseBoolean);
  }

  private Mono<Boolean> isSafe(String body, String comment) {
    return Mono.fromCallable(() -> BlockingLib.isSafe(body, comment))
        .publishOn(Schedulers.boundedElastic());
    // ± using VirtualThreads: see https://projectreactor.io/docs/core/release/reference/coreFeatures/schedulers.html
  }
}
