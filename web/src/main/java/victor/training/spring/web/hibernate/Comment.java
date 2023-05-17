package victor.training.spring.web.hibernate;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data // i'm sorry
@Entity
public class Comment {
  @Id
  @GeneratedValue
  private Long id;
  @ManyToOne
  private Post post;

  private String comment;
  private String name;
}
