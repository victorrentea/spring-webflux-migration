package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
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
    @Transactional // lives on the Reactor Context in a reactive app
    public Mono<Long> createPost(@RequestBody CreatePostRequest request) {
        return postRepo.save(request.toPost())
                .flatMap(this::createComment)
                .delayUntil(pid -> rabbitSender.sendMessage("Post created: " + pid));
    }

  private Mono<Long> createComment(Long pid) {
    CommentRecord comment = new CommentRecord();
    comment.setPostId(pid);
    comment.setComment("Posted on " + now());
    return commentRepo.save(comment).thenReturn(pid);
  }
}
