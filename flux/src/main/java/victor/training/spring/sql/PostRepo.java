package victor.training.spring.sql;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.spring.table.tables.Post;
import victor.training.spring.table.tables.records.PostRecord;

@Repository
public class PostRepo {
    @Autowired
    private DSLContext dsl;
    public Mono<Long> save(PostRecord post) {
//        Mono.from(...)
        return Mono.from(dsl.insertInto(Post.POST)
                .set(post)
                .returningResult(Post.POST.ID))
                .map(r->r.value1());
    }

    public Mono<PostRecord> findById(Long postId) {
        return Mono.from(dsl.selectFrom(Post.POST)
                .where(Post.POST.ID.eq(postId)))
                ;

    }

    public Flux<PostRecord> findAll() {
        return Flux.from(dsl.selectFrom(Post.POST));
    }
}
