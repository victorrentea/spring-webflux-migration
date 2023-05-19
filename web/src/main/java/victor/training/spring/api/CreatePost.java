package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;
import victor.training.spring.rabbit.RabbitSender;

import java.util.UUID;

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
          .setId(UUID.randomUUID().toString())
          .setTitle(title).setBody(body).setAuthorId(authorId);
    }
  }
  @PostMapping("posts")
  @Transactional
  public void createPost(@RequestBody CreatePostRequest request) {
    Post post = postRepo.save(request.toPost());
    commentRepo.save(new Comment()
        .setId(UUID.randomUUID().toString())
        .setPostId(post.getId())
        .setComment("Posted on " + now()));

    // TODO move to reactiveCrudRepository
    // TODO delete comment
    // TODO delete jooq
    // TODO listen to rabbit from tests
    // TODO add rabbit listened in prod
    // TODO
    rabbitSender.sendMessage("Post created: " + post.getId());
  }
}
