package victor.training.spring.sql;

import static java.time.LocalDateTime.now;
import static javax.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class Post {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  private String title;
  private String body;
  private Long authorId; 
  private LocalDateTime createdAt = now();
}

