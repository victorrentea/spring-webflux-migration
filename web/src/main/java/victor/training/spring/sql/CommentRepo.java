package victor.training.spring.sql;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
  @Timed("findCommentsByPostId")
  List<Comment> findByPostId(Long postId);
}
