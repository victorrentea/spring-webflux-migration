package victor.training.spring.sql;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Entity
@Data // i'm sorry
public class Post {
  @Id
  private String id;

  private String title;
  private String body;
  private Long authorId;
  private LocalDateTime createdAt = now();

}
