package victor.training.spring.hibernate;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Entity
@Data // i'm sorry
public class Post {
  @Id
  @GeneratedValue
  private Long id;

  private String title;
  private String body;
  private Long authorId;
  private LocalDateTime createdAt = now();

}
