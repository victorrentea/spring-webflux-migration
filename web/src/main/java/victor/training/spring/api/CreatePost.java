package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
  public void createPost(@RequestBody CreatePostRequest request) {
    Post post = postRepo.save(request.toPost());
    commentRepo.save(new Comment().setPostId(post.getId()).setComment("Posted on " + now()));
    rabbitSender.sendMessage("Post created: " + post.getId());
  }
}
