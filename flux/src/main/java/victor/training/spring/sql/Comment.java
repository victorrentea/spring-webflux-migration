package victor.training.spring.sql;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Table
public class Comment {
  @Id
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
