package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC6_GetPostLikes {
  private final Map<Long, Integer> postLikes = new ConcurrentHashMap<>();

  public record LikeEvent(long postId, int likes) {
  }
  @Bean
  public Consumer<Flux<LikeEvent>> onLikeEvent() {
    return likeEventFlux -> likeEventFlux.subscribe(likeEvent -> {
      postLikes.put(likeEvent.postId(), likeEvent.likes());
      sinks.tryEmitNext(likeEvent);
      log.info("Received {}", likeEvent);
    });
  }

  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable Long postId) {
    return postLikes.getOrDefault(postId, 0);
  }
  @GetMapping(value = "posts/{postId}/likes-live",produces = "text/event-stream")
  public Flux<String> getPostLikesLive(@PathVariable Long postId) {
    return sinks.asFlux()
        .filter(e->e.postId()==postId)
        .map(e->"Likes: " + e.likes());
  }

  private final Sinks.Many<LikeEvent> sinks = Sinks.many().multicast().directBestEffort();
}
