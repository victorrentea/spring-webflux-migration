package victor.training.spring.sql;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepo extends R2dbcRepository<Comment, Long> {
  Flux<Comment> findByPostId(Long postId);
}
