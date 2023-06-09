package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllPosts { // #2
  private final PostRepo postRepo;

  static {  // ⭐️⭐️⭐️ Detection of blocking code in a reactive application:
    // ⚠️ add to startup VM options: -XX:+AllowRedefinitionToAddDeleteMethods
//    BlockHound.builder()
//        .allowBlockingCallsInside("io.r2dbc.postgresql.authentication.SASLAuthenticationHandler", "handleAuthenticationSASL")
//        .install();
  }

  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }
  @GetMapping("posts")
  public List<GetPostsResponse> getAllPosts() {
    return postRepo.findAll().stream().map(GetPostsResponse::new).toList();
  }
}
