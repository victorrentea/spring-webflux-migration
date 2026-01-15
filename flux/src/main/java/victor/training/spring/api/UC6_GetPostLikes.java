package victor.training.spring.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import victor.training.spring.sql.PostRepo;

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
  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable long postId) {
    return postLikes.getOrDefault(postId, 0);
  }
  public record LikeEvent(Long postId, int likes) {

  }

  @Bean
  public Function<Flux<LikeEvent>, Flux<LikedPosts>> onLikeEvent() {
    // TODO process incoming LikeEvent stream to update postLikes map and emit LikedPosts every 2 seconds
    return null; // TODO
  }

  public record LikedPosts(Collection<String> titles) {
  }

  private final Sinks.Many<LikeEvent> eventSink = Sinks.many().multicast().directBestEffort();
  // TODO push live likes to browser for UX❤️ http://localhost:8081/posts/2/likes-live

  @GetMapping(value = "posts/{postId}/likes-live", produces = "text/event-stream")
  public Flux<Integer> getPostLikesLive(@PathVariable long postId) {
    return eventSink.asFlux()
        .filter(likeEvent -> likeEvent.postId() == postId)
        .map(LikeEvent::likes);
  }
}
