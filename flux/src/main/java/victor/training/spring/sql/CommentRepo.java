package victor.training.spring.sql;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import victor.training.spring.table.tables.Comment;
import victor.training.spring.table.tables.records.CommentRecord;

import java.util.List;

@Repository
public class CommentRepo {
  @Autowired
  private DSLContext dsl;

  public List<CommentRecord> findByPostId(Long postId) {
    return dsl.selectFrom(Comment.COMMENT)
            .where(Comment.COMMENT.POST_ID.eq(postId))
            .fetch();
  }

  public Long save(CommentRecord comment) {
    return dsl.insertInto(Comment.COMMENT)
            .set(comment)
            .returningResult(Comment.COMMENT.ID)
            .fetch()
            .get(0)
            .value1();
  }
}
