package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.hibernate.Post;
import victor.training.spring.hibernate.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllPosts { // #2
  private final PostRepo postRepo;

  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }
  @GetMapping("posts")
  public Flux<GetPostsResponse> getAllPosts() {
    return Flux.fromStream(() -> postRepo.findAll().stream())
        .subscribeOn(Schedulers.boundedElastic())
        .map(GetPostsResponse::new);
  }
}
