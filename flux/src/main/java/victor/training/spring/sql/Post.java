package victor.training.spring.sql;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
public class Post {
  @Id
  private Long id;
  private String title;
  private String body;
  private Long authorId; 
  private LocalDateTime createdAt = now();
}

