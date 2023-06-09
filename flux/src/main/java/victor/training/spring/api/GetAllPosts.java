package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllPosts { // #2
  static {
    // add to VM options at startup: -XX:+AllowRedefinitionToAddDeleteMethods
    BlockHound.install();
  }
  private final PostRepo postRepo;

  public record GetPostsResponse(String id, String title) {
    GetPostsResponse(Post post) {
      this(post.getId(), post.getTitle());
    }
  }

  @GetMapping("posts")
  public Flux<GetPostsResponse> getAllPosts() {
    // findAll agata 1/2xCPU threaduri pe care cheama Netty metodele @RestController
    // blockhound ar arunca exceptie cand chemi endpointul asta, ca blochezi
    // "legacy" non-reactive library call care nu tre sa ruleze AICI BLOCAND THREADUL
    log.info("Cine cheama functia efectiva"); // TODO de ce vad parallel- ? treabuia netti9qur2572857-
    // "legacy" non-reactive library call
    return Mono.fromCallable(postRepo::findAll)
        .subscribeOn(Schedulers.boundedElastic()) // subscribeul in sus unde ruleaza -> e pe alt thread acum
        .flatMapMany(Flux::fromIterable)
        .map(GetPostsResponse::new);
  }
}
