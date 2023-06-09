package victor.training.spring.sql;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CommentRepo extends ReactiveCrudRepository<Comment, String> {
  Flux<Comment> findByPostId(String postId);
}
