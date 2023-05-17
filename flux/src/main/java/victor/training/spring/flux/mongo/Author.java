package victor.training.spring.flux.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Document
@Data // i'm sorry
public class Author {
  @Id
  private Long id;
  private String name;
  private String bio;
  private LocalDateTime createdAt = now();

}
