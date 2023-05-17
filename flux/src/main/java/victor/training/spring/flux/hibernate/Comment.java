package victor.training.spring.flux.hibernate;

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
  private Long postId;

  private String comment;
  private String name;
}
