package victor.training.spring.sql;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data // i'm sorry
@Table
public class Post {
  @Id
  private String id;

  private String title;
  private String body;
  private Long authorId;
  private LocalDateTime createdAt = now();

}
