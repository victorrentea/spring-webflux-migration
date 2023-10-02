package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.time.Duration.ofSeconds;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC6_GetPostLikes {
  private final Map<Long, Integer> postLikes = new ConcurrentHashMap<>();
  private final PostRepo postRepo;

  public record LikeEvent(long postId, int likes) {
  }

  // TODO every 3 seconds emit new post titles
  public record LikedPosts(Collection<String> titles) {
  }

  @Bean
  public Function<Flux<LikeEvent>, Flux<LikedPosts>> onLikeEvent() {
    return likeEventFlux -> likeEventFlux
        .doOnNext(event -> log.info("Received {}", event))
        .doOnNext(event -> postLikes.put(event.postId(), event.likes()))
        .doOnNext(event -> sinks.tryEmitNext(event))

        .map(LikeEvent::postId)
        .buffer(ofSeconds(3))
        .flatMap(postIds -> postRepo.findAllById(postIds).map(Post::title).collectList())
        .map(LikedPosts::new)
        .doOnNext(msg -> log.info("Sending {}", msg));

  }

  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable Long postId) {
    return postLikes.getOrDefault(postId, 0);
  }

  @GetMapping(value = "posts/{postId}/likes-live", produces = "text/event-stream")
  public Flux<String> getPostLikesLive(@PathVariable Long postId) {
    return sinks.asFlux()
        .filter(e -> e.postId() == postId)
        .map(e -> "Likes: " + e.likes());
  }

  private final Sinks.Many<LikeEvent> sinks = Sinks.many().multicast().directBestEffort();
}
