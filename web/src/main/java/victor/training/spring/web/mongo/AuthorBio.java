package victor.training.spring.web.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data // i'm sorry
public class AuthorBio {
  @Id
  private Long id;
  private String name;
  private String bio;
}
