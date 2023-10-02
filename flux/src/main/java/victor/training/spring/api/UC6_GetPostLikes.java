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

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC6_GetPostLikes {
  private final Map<Long, Integer> postLikes = new ConcurrentHashMap<>();
  private final PostRepo postRepo;

  private final Sinks.Many<LikeEvent> likeEventSink = Sinks.many().multicast().directBestEffort();

  public record LikeEvent(long postId, int likes) {
  }

  @Bean
  public Function<Flux<LikeEvent>, Flux<LikedPosts>> onLikeEvent() {
    return flux -> flux
        .doOnNext(likeEvent -> postLikes.put(likeEvent.postId(), likeEvent.likes()))
        .doOnNext(likeEvent -> likeEventSink.tryEmitNext(likeEvent))
        .map(LikeEvent::postId)
        .buffer(Duration.ofSeconds(1))
        .flatMap(postIds -> postRepo.findAllById(postIds).map(Post::title).collectList())
        .map(LikedPosts::new)
        .doOnNext(message -> log.info("Sending {}", message));
  }

  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable Long postId) {
    return postLikes.getOrDefault(postId, 0);
  }

  // TODO push live likes to browser for UX❤️ http://localhost:8081/posts/2/likes-live
  @GetMapping(value = "posts/{postId}/likes-live", produces = "text/event-stream")
  public Flux<Integer> getPostLikesLive(@PathVariable long postId) {
    return likeEventSink.asFlux()
        .filter(likeEvent -> likeEvent.postId() == postId)
        .map(LikeEvent::likes);
  }

  // TODO every 1 second emit new post titles
  public record LikedPosts(Collection<String> titles) {
  }
}
