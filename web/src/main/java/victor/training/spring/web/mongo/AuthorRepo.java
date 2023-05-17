package victor.training.spring.web.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuthorRepo extends MongoRepository<Author, String> {
}
