package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.rabbit.RabbitSender;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreatePost { // #4
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RabbitSender rabbitSender;

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post()
          .setId(randomUUID().toString())
          .setTitle(title)
          .setBody(body)
          .setAuthorId(authorId);
    }
  }
  @PostMapping("posts")
  @Transactional
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    return postRepo.save(request.toPost())
//        .delayUntil(p-> saveComment(p.getId()))
        .flatMap(p-> rabbitSender.sendMessage("Post created: " + p.getId()));
  }

  private Mono<Void> saveComment(String postId) {
    return Mono.fromCallable(() ->
            commentRepo.save(new Comment().setPostId(randomUUID().toString()).setComment("Posted on " + now())))
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }
}
