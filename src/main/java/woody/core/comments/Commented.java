/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Entity;
import sirius.db.mixing.OMA;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.Transient;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Or;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by aha on 25.11.15.
 */
public class Commented extends Composite {

    @Transient
    protected final Entity owner;

    public Commented(Entity owner) {
        this.owner = owner;
    }

    @Part
    private static OMA oma;

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Comment.class).eq(Comment.TARGET_ENTITY, owner.getUniqueName()).delete();
        }
    }

    public List<Comment> getAllComments() {
        return oma.select(Comment.class)
                  .orderDesc(Comment.TOD)
                  .eq(Comment.TARGET_ENTITY, owner.getUniqueName())
                  .queryList();
    }

    public List<Comment> getPublicComments() {
        return oma.select(Comment.class)
                  .orderDesc(Comment.TOD)
                  .eq(Comment.TARGET_ENTITY, owner.getUniqueName())
                  .where(Or.of(FieldOperator.on(Comment.PERSON_ENTITY).eq(UserContext.getCurrentUser().getUserId()),
                               FieldOperator.on(Comment.PUBLIC_VISIBLE).eq(true)))
                  .queryList();
    }

    public String getAuthHash() {
        return CommentsController.computeAuthHash(owner.getUniqueName());
    }

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

    public void addCommentAsCurrentUser(String comment) {
        UserInfo currentUser = UserContext.getCurrentUser();
        addComment(currentUser.getUserObject(UserAccount.class).getPerson().toString(),
                   currentUser.getUserId(),
                   comment,
                   false);
    }

    public void addPublicCommentAsCurrentUser(String comment) {
        UserInfo currentUser = UserContext.getCurrentUser();
        addComment(currentUser.getUserObject(UserAccount.class).getPerson().toString(),
                   currentUser.getUserId(),
                   comment,
                   true);
    }
}
