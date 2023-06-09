package victor.training.spring.sql;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PostRepo extends ReactiveCrudRepository<Post, Long> {

}
