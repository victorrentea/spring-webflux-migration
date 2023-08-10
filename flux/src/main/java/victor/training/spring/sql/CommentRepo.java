package victor.training.spring.sql;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.spring.table.tables.Comment;
import victor.training.spring.table.tables.records.CommentRecord;

@Repository
public class CommentRepo {
  @Autowired
  private DSLContext dsl;

  public Flux<CommentRecord> findByPostId(Long postId) {
    return Flux.from(dsl.selectFrom(Comment.COMMENT)
            .where(Comment.COMMENT.POST_ID.eq(postId)));
  }

  public Mono<Void> save(CommentRecord comment) {
    return Mono.just(dsl.insertInto(Comment.COMMENT)
            .set(comment)
            .returningResult(Comment.COMMENT.ID)
    ).then();
  }
}
