package victor.training.spring.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, String> {
  List<Comment> findByPostId(String postId);
}
