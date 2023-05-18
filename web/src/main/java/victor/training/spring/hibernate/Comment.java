package victor.training.spring.hibernate;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data // i'm sorry
@Entity
public class Comment {
  @Id
  @GeneratedValue
  private Long id;
  private Long postId;

  private String comment;
  private String name;
  private LocalDateTime createdAt = now();
}
