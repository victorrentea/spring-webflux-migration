package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.hibernate.Comment;
import victor.training.spring.hibernate.CommentRepo;
import victor.training.spring.hibernate.Post;
import victor.training.spring.hibernate.PostRepo;
import victor.training.spring.rabbit.RabbitSender;

import static java.time.LocalDateTime.now;

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
          .setTitle(title)
          .setBody(body)
          .setAuthorId(authorId);
    }
  }
  @PostMapping("posts")
  @Transactional
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    return Mono.fromCallable(() -> postRepo.save(request.toPost())).subscribeOn(Schedulers.boundedElastic())
        .delayUntil(p-> saveComment(p.getId()))
        .flatMap(p-> rabbitSender.sendMessage("Post created: " + p.getId()));
  }

  private Mono<Void> saveComment(Long postId) {
    return Mono.fromCallable(() ->
            commentRepo.save(new Comment().setPostId(postId).setComment("Posted on " + now())))
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }
}
