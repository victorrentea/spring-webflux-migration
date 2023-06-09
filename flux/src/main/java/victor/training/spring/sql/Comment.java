package victor.training.spring.sql;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table
public class Comment {
  @Id
  private String id;
  private String postId;

  private String comment;
  private String name;
  private LocalDateTime createdAt = now();
}
