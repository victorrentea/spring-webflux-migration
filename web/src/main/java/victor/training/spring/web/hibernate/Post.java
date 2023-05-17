package victor.training.spring.web.hibernate;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data // i'm sorry
public class Post {
  @Id
  @GeneratedValue
  private Long id;

  private String title;
  private String body;
  private Long authorId;
}
