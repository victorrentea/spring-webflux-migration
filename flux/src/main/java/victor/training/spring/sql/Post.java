package victor.training.spring.sql;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table
public class Post /*implements Persistable<String>*/ {
  @Id
  private Long id;
  private String title;
  private String body;
  private Long authorId; 
  private LocalDateTime createdAt = now();

//  @Transient
//  private boolean isNew;
}

