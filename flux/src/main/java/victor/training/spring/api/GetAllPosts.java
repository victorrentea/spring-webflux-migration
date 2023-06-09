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
public class GetAllPosts { // #2
  static {
//    BlockHound.install(); // add to startup VM options: -XX:+AllowRedefinitionToAddDeleteMethods
  }
  private final PostRepo postRepo;

  public record GetPostsResponse(Long id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }

  @GetMapping("posts")
  public Flux<GetPostsResponse> getAllPosts() {
    log.info("Cine cheama functia efectiva"); // TODO de ce vad parallel- ? treabuia netti9qur2572857-

    // "legacy" non-reactive library call (JPA)
//    return Mono.fromCallable(postRepo::findAll)
//        .subscribeOn(Schedulers.boundedElastic()) // subscribeul in sus unde ruleaza -> e pe alt thread acum
//        .flatMapMany(Flux::fromIterable)
    
    return postRepo.findAll().map(GetPostsResponse::new);
  }
}
