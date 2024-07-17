package victor.training.spring.sql;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Entity // JPA/ hibernate
public class CommentHibernate {
  @Id
  private Long id;
  private Long postId; // numeric FK instead of `@ManyToOne Post post;`
  private String comment;
  private String name;
  private LocalDateTime createdAt = now();

  public CommentHibernate() {}

  public CommentHibernate(Long postId, String comment) {
    this(postId, comment, null);
  }

  public CommentHibernate(Long postId, String comment, String name) {
    this.postId = postId;
    this.comment = comment;
    this.name = name;
  }

  public String comment() {
    return comment;
  }
}
