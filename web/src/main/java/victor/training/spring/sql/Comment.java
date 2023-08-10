//package victor.training.spring.sql;
//
//import lombok.Data;
//
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import java.time.LocalDateTime;
//
//import static java.time.LocalDateTime.now;
//import static javax.persistence.GenerationType.IDENTITY;
//
//@Data
//@Entity
//public class Comment {
//  @Id
//  @GeneratedValue(strategy = IDENTITY)
//  private Long id;
//  private Long postId; // numeric FK instead of `@ManyToOne Post post;`
//  private String comment;
//  private String name;
//  private LocalDateTime createdAt = now();
//}
