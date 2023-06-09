package victor.training.spring.mongo;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Author {
  @Id
  private Long id;
  private String name;
  private String bio;
  private LocalDateTime createdAt = now();
}
