package victor.training.spring.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.PostRepo;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC5_CreateComment {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final WebClient webClient;

  public record CreateCommentRequest(String comment, String name) {
  }

  @PostMapping("posts/{postId}/comments")
  public Mono<Void> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
    return postRepo.findById(postId)
        .switchIfEmpty(Mono.error(new NoSuchElementException()))
        .flatMap(post -> Mono.zip(
            checkOffensive(post.body(), request.comment()),
            checkAuthorAllowsComments(post.authorId()),
            Boolean::logicalAnd)
            .filter(b-> b)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment Rejected")))
            .then(commentRepo.save(new Comment(post.id(), request.comment(), request.name())))).then();

    // Post post = postRepo.findById(postId).orElseThrow();
    //    boolean offensive = checkOffensive(post.body(), request.comment());
    //    boolean unlocked = checkAuthorAllowsComments(post.authorId());
    //    if (offensive && unlocked) {
    //      commentRepo.save(new Comment(post.id(), request.comment(), request.name()));
    //    } else {
    //      throw new IllegalArgumentException("Comment Rejected");
    //    }
  }

  private Mono<Boolean> checkAuthorAllowsComments(Long authorId) {
    String url = "http://localhost:9999/author/" + authorId + "/comments";
    Mono<String> resultMono = webClient.get().uri(url).retrieve().bodyToMono(String.class);
    return resultMono.map(Boolean::parseBoolean);
  }

  private Mono<Boolean> checkOffensive(String body, String comment) {
    record Request(String body, String comment) {
    }
    String url = "http://localhost:9999/safety-check";
    Mono<String> result = webClient.post().uri(url).bodyValue(new Request(body, comment)).retrieve().bodyToMono(String.class);
    return result.map("OK"::equals);
  }
}
