package victor.training.spring.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuthorRepo extends ReactiveMongoRepository<Author, String> {
}
