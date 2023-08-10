package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.spring.rabbit.RabbitSender;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.PostRepo;
import victor.training.spring.table.tables.records.CommentRecord;
import victor.training.spring.table.tables.records.PostRecord;

import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreatePost { // #4
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;
  private final RabbitSender rabbitSender;

  public record CreatePostRequest(String title, String body, Long authorId) {
    PostRecord toPost() {
      PostRecord post = new PostRecord();
      post.setTitle(title);
      post.setBody(body);
      post.setAuthorId(authorId);
      return post;
    }
  }
  @PostMapping("posts")
  @Transactional
  public Long createPost(@RequestBody CreatePostRequest request) {
    Long postId = postRepo.save(request.toPost());


    CommentRecord comment = new CommentRecord();
    comment.setPostId(postId);
    comment.setComment("Posted on " + now());
    commentRepo.save(comment);

    rabbitSender.sendMessage("Post created: " + postId);
    return postId;
  }
}
