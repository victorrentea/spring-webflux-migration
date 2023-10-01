package victor.training.spring.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC6_GetPostLikes {
  private final Map<Long, Integer> postLikes = new ConcurrentHashMap<>();

  @GetMapping("posts/{postId}/likes")
  public Integer getPostLikes(@PathVariable Long postId) {
    return postLikes.getOrDefault(postId, 0);
  }

  public record LikeEvent(long postId, int likes) {
  }

  @Bean
  public Consumer<LikeEvent> onLikeEvent() {
    return likeEvent -> {
      postLikes.put(likeEvent.postId(), likeEvent.likes());
      log.info("Received {}", likeEvent);
    };
  }
}
