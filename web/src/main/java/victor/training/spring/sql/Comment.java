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
public class Comment {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id; 
  private Long postId; // numeric FK instead of `@ManyToOne Post post;`
  private String comment;
  private String name;
  private LocalDateTime createdAt = now();

  public Comment() {}

  public Comment(Long postId, String comment) {
    this(postId, comment, null);
  }

  public Comment(Long postId, String comment, String name) {
    this.postId = postId;
    this.comment = comment;
    this.name = name;
  }

  public String comment() {
    return comment;
  }
}
