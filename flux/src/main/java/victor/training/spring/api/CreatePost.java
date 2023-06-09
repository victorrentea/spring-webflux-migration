package victor.training.spring.api;

import static java.time.LocalDateTime.now;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import victor.training.spring.rabbit.RabbitSender;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreatePost { // #4
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RabbitSender rabbitSender;

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post().setTitle(title).setBody(body).setAuthorId(authorId);
    }
  }

  @PostMapping("posts")
  @Transactional
  public Mono<Void> createPost(@RequestBody CreatePostRequest request) {
    Post post = request.toPost();
    return postRepo.save(post)
        .then(commentRepo.save(createInitialComment(post)))
        .then(rabbitSender.sendMessage("Post created: " + post.getId())).then();
  }

  private static Comment createInitialComment(Post post) {
    return new Comment().setComment("Posted on " + now()).setPostId(post.getId());
  }
}
