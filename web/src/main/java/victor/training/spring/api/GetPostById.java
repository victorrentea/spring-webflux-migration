package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import victor.training.spring.api.GetPostById.GetPostByIdResponse.CommentResponse;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetPostById { // #3
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;

  public record GetPostByIdResponse(String id, String title, String body, List<CommentResponse> comments) {
    GetPostByIdResponse(Post post, List<CommentResponse> comments) {
      this(post.getId(), post.getTitle(), post.getBody(), comments);
    }
    public record CommentResponse(String comment, String name) {
      CommentResponse(Comment comment) {
        this(comment.getComment(), comment.getName());
      }
    }
  }
  @GetMapping("posts/{postId}")
  public GetPostByIdResponse getPostById(@PathVariable Long postId) {
    Post post = postRepo.findById(postId).orElseThrow();
    List<CommentResponse> comments = commentRepo.findByPostId(postId).stream()
        .map(CommentResponse::new)
        .toList();
    return new GetPostByIdResponse(post, comments);
  }

}
