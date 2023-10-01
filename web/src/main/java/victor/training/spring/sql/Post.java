package victor.training.spring.sql;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.LocalDateTime.now;

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

