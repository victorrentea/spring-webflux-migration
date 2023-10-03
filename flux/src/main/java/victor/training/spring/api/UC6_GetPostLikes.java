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

  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable long postId) {
    return postLikes.getOrDefault(postId, 0);
  }

  public record LikeEvent(long postId, int likes) {
  }
private final PostRepo postRepo;
  @Bean
  public Function<Flux<LikeEvent>, Flux<LikedPosts>> onLikeEvent() {
    return flux -> flux
        .doOnNext(event -> postLikes.put(event.postId(), event.likes()))
        .doOnNext(event -> eventSink.tryEmitNext(event))
        .map(LikeEvent::postId)
        .buffer(Duration.ofSeconds(1))
        .flatMap(ids -> postRepo.findAllById(ids).map(Post::title).collectList())
        .map(LikedPosts::new)
        .doOnNext(message -> log.info("Sending " + message));
  }
  // TODO every 1 second emit titles of recently liked posts. Hard: keep listening despite failed messages
  public record LikedPosts(Collection<String> titles) {
  }

  Sinks.Many<LikeEvent> eventSink = Sinks.many().multicast().directBestEffort();

  // TODO push live likes to browser for UX❤️ http://localhost:8081/posts/2/likes-live
  @GetMapping(value = "posts/{postId}/likes-live", produces = "text/event-stream")
  public Flux<Integer> getPostLikesLive(@PathVariable long postId) {
    return eventSink.asFlux()
        .filter(event -> event.postId() == postId)
        .map(LikeEvent::likes);
  }

}
