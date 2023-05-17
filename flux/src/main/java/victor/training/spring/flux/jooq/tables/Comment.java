/*
 * This file is generated by jOOQ.
 */
package victor.training.spring.flux.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import victor.training.spring.flux.jooq.Keys;
import victor.training.spring.flux.jooq.Public;
import victor.training.spring.flux.jooq.tables.records.CommentRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Comment extends TableImpl<CommentRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.comment</code>
     */
    public static final Comment COMMENT = new Comment();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CommentRecord> getRecordType() {
        return CommentRecord.class;
    }

    /**
     * The column <code>public.comment.id</code>.
     */
    public final TableField<CommentRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.comment.comment</code>.
     */
    public final TableField<CommentRecord, String> COMMENT_ = createField(DSL.name("comment"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.comment.name</code>.
     */
    public final TableField<CommentRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.comment.post_id</code>.
     */
    public final TableField<CommentRecord, Long> POST_ID = createField(DSL.name("post_id"), SQLDataType.BIGINT, this, "");

    private Comment(Name alias, Table<CommentRecord> aliased) {
        this(alias, aliased, null);
    }

    private Comment(Name alias, Table<CommentRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.comment</code> table reference
     */
    public Comment(String alias) {
        this(DSL.name(alias), COMMENT);
    }

    /**
     * Create an aliased <code>public.comment</code> table reference
     */
    public Comment(Name alias) {
        this(alias, COMMENT);
    }

    /**
     * Create a <code>public.comment</code> table reference
     */
    public Comment() {
        this(DSL.name("comment"), null);
    }

    public <O extends Record> Comment(Table<O> child, ForeignKey<O, CommentRecord> key) {
        super(child, key, COMMENT);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<CommentRecord> getPrimaryKey() {
        return Keys.COMMENT_PKEY;
    }

    @Override
    public List<ForeignKey<CommentRecord, ?>> getReferences() {
        return Arrays.asList(Keys.COMMENT__FKS1SLVNKUEMJSQ2KJ4H3VHX7I1);
    }

    private transient Post _post;

    /**
     * Get the implicit join path to the <code>public.post</code> table.
     */
    public Post post() {
        if (_post == null)
            _post = new Post(this, Keys.COMMENT__FKS1SLVNKUEMJSQ2KJ4H3VHX7I1);

        return _post;
    }

    @Override
    public Comment as(String alias) {
        return new Comment(DSL.name(alias), this);
    }

    @Override
    public Comment as(Name alias) {
        return new Comment(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Comment rename(String name) {
        return new Comment(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Comment rename(Name name) {
        return new Comment(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, String, String, Long> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}