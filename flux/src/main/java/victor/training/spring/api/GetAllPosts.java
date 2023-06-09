package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllPosts { // #2
  static {
    // ⭐️⭐️⭐️ Detection of blocking code in a reactive application:
    // ⚠️ add to startup VM options: -XX:+AllowRedefinitionToAddDeleteMethods
    BlockHound.builder()
        .allowBlockingCallsInside("io.r2dbc.postgresql.authentication.SASLAuthenticationHandler", "handleAuthenticationSASL")
        .install();
  }
  private final PostRepo postRepo;

  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }

  @GetMapping("posts")
  public Flux<GetPostsResponse> getAllPosts() {
    return postRepo.findAll().map(GetPostsResponse::new);
  }
}
