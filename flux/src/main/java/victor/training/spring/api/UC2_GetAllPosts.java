
package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC2_GetAllPosts {
  private final PostRepo postRepo;

  public record GetPostsResponse(long id, String title) {
    GetPostsResponse(Post post) {
      this(post.id(), post.title());
    }
  }
  @GetMapping("posts")
  // Reactive would allow to send millions to client (not BRO')
  public Flux<GetPostsResponse> getAllPosts() {
    return postRepo.findAll().map(GetPostsResponse::new);
  }
}
