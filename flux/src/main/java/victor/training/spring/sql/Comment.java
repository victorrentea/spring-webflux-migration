package victor.training.spring.sql;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table
public class Comment /*implements Persistable<String>*/ {
  @Id
  private Long id; // uuid instead of @GeneratedValue
  private Long postId; // numeric FK instead of @ManyToOne Post post;
  private String comment;
  private String name;
  private LocalDateTime createdAt = now();

//  @Transient
//  private boolean isNew;
}
