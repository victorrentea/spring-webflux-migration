package victor.training.spring.flux.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
  List<Comment> findByPostId(Long postId);
}
