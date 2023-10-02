package victor.training.spring.sql;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PostRepo extends R2dbcRepository<Post, Long> {

}
