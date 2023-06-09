package victor.training.spring.sql;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CommentRepo extends ReactiveCrudRepository<Comment, Long> {
  Flux<Comment> findByPostId(Long postId);
}
