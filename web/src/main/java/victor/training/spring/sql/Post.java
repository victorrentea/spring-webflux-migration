package victor.training.spring.sql;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class Post {
  @Id
  private String id;
  private String title;
  private String body;
  private Long authorId; 
  private LocalDateTime createdAt = now();
}

