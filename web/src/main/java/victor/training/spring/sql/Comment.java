package victor.training.spring.sql;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data // i'm sorry
@Entity
public class Comment {
  @Id
  private String id;
  private String postId;

  private String comment;
  private String name;
  private LocalDateTime createdAt = now();
}
