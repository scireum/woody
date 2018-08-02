/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.db.jdbc.SQLEntity;
import sirius.db.mixing.Composite;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.Transient;
import sirius.kernel.commons.Strings;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides the comment facility which is embedded into an entity as composite.
 * <p>
 * This way comments are automatically deleted once the entity itself is deleted.
 */
public class Commented extends Composite {

    @Transient
    protected final SQLEntity owner;

    /**
     * Creates a new comments facility for the given entity.
     */
    public Commented(SQLEntity owner) {
        this.owner = owner;
    }

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Comment.class).eq(Comment.TARGET_ENTITY, owner.getUniqueName()).delete();
        }
    }

    public String getReferenceId() {
        return owner.getUniqueName();
    }

    /**
     * Returns all comments available for this entity.
     *
     * @return a list of all comments for this entity
     */
    public List<Comment> getAllComments() {
        return oma.select(Comment.class)
                  .orderDesc(Comment.TOD)
                  .eq(Comment.DELETED, false)
                  .eq(Comment.TARGET_ENTITY, owner.getUniqueName())
                  .queryList();
    }

    /**
     * Returns all public visible comments for this entity
     *
     * @return a list of all public visible comments for this entity
     */
    public List<Comment> getPublicComments() {
        return oma.select(Comment.class)
                  .orderDesc(Comment.TOD)
                  .eq(Comment.DELETED, false)
                  .eq(Comment.TARGET_ENTITY, owner.getUniqueName())
                  .eq(Comment.PUBLIC_VISIBLE, true)
                  .queryList();
    }

    /**
     * Computes the autorisation hash required to use the REST API provided by {@link CommentsController}.
     *
     * @return the autorisation hash for this entity to use the rest api
     */
    public String getAuthHash() {
        return CommentsController.computeAuthHash(owner.getUniqueName());
    }

    /**
     * Adds a new comment to this entity.
     *
     * @param personName    the name of the person which created the comment
     * @param personEntity  the unique name of the person which created the comment
     * @param comment       the comment itself
     * @param publicVisible determines if the comment is public visible
     */
    public void addComment(String personName, String personEntity, String comment, boolean publicVisible) {
        if (Strings.isEmpty(comment)) {
            return;
        }

        if (owner.isNew()) {
            throw new IllegalStateException("owner must not be new");
        }

        Comment commentEntity = new Comment();
        commentEntity.setTargetEntity(owner.getUniqueName());
        commentEntity.setPersonName(personName);
        commentEntity.setPersonEntity(personEntity);
        commentEntity.setTod(LocalDateTime.now());
        commentEntity.setTextContent(comment);
        commentEntity.setPublicVisible(publicVisible);
        oma.update(commentEntity);
    }
}
