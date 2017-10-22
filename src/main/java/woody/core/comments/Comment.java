/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.kernel.commons.Strings;
import sirius.kernel.nls.NLS;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;

import java.time.LocalDateTime;

/**
 * Represents a comment attached to an entity.
 * <p>
 * Comments can be attached to any entity and managed using a {@link Commented} composite within the entity. For
 * entities using comments it is recommended to implement {@link HasComments} which ensures the naming convention
 * of the <tt>Commented</tt> composite.
 */
@Index(name = "target_lookup", columns = "targetEntity")
public class Comment extends Entity {

    /**
     * Users having this permission can edit all comments, not just their own.
     */
    public static final String PERMISSION_EDIT_COMMENTS = "permission-edit-comments";

    /**
     * Contains the unique object name of the entity to which this comment belongs.
     *
     * @see Entity#getUniqueName()
     */
    public static final Column TARGET_ENTITY = Column.named("targetEntity");
    @Length(255)
    private String targetEntity;

    /**
     * Contains the name of the person that created the comment.
     */
    public static final Column PERSON_NAME = Column.named("personName");
    @Length(255)
    private String personName;

    /**
     * Contains the unique name of the person which created the comment which is probably either a {@link
     * sirius.biz.tenants.UserAccount} or {@link woody.xrm.Person}.
     */
    public static final Column PERSON_ENTITY = Column.named("personEntity");
    @Length(255)
    private String personEntity;

    /**
     * Contains the timestamp when the comment was written.
     */
    public static final Column TOD = Column.named("tod");
    private LocalDateTime tod;

    /**
     * Contains the comment itself
     */
    public static final Column TEXT_CONTENT = Column.named("textContent");
    @Lob
    private String textContent;

    /**
     * Determines if the comment is public visible (i.e. to the customer in self-service areas).
     */
    public static final Column PUBLIC_VISIBLE = Column.named("publicVisible");
    private boolean publicVisible;

    /**
     * Determines if the comment has been deleted.
     */
    public static final Column DELETED = Column.named("deleted");
    private boolean deleted;

    /**
     * Determines if the comment has been edited.
     */
    public static final Column EDITED = Column.named("edited");
    private boolean edited;

    /**
     * Determines if the comment can be modified by the current user.
     *
     * @return <tt>true</tt> if comment can be modified by the current user, <tt>false</tt> otherwise
     */
    public boolean canBeEditedByCurrentUser() {
        UserInfo currentUser = UserContext.getCurrentUser();
        return Strings.areEqual(currentUser.getUserId(), personEntity) || currentUser.hasPermission(
                PERMISSION_EDIT_COMMENTS);
    }

    public String getDateString() {
        return NLS.toSpokenDate(getTod());
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public boolean isPublicVisible() {
        return publicVisible;
    }

    public void setPublicVisible(boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonEntity() {
        return personEntity;
    }

    public void setPersonEntity(String personEntity) {
        this.personEntity = personEntity;
    }

    public LocalDateTime getTod() {
        return tod;
    }

    public void setTod(LocalDateTime tod) {
        this.tod = tod;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
