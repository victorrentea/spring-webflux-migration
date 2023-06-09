package victor.training.spring.sql;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Comment {
  @Id
  private String id; // uuid instead of @GeneratedValue
  private String postId; // numeric FK instead of @ManyToOne Post post;
  private String comment;
  private String name;
  private LocalDateTime createdAt = now();
}
