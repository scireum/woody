/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.kernel.commons.Strings;
import sirius.mixing.Column;
import sirius.mixing.Entity;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.Lob;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;

import java.time.LocalDateTime;

/**
 * Created by aha on 25.11.15.
 */
public class Comment extends Entity {

    public static final String PERMISSION_EDIT_COMMENTS = "permission-edit-comments";

    @Length(length = 255)
    private String targetEntity;
    public static final Column TARGET_ENTITY = Column.named("targetEntity");

    @Length(length = 255)
    private String personName;
    public static final Column PERSON_NAME = Column.named("personName");

    @Length(length = 255)
    private String personEntity;
    public static final Column PERSON_ENTITY = Column.named("personEntity");

    private LocalDateTime tod;
    public static final Column TOD = Column.named("tod");

    @Lob
    private String textContent;
    public static final Column TEXT_CONTENT = Column.named("textContent");

    private boolean publicVisible;
    public static final Column PUBLIC_VISIBLE = Column.named("publicVisible");

    public boolean canBeEditedByCurrentUser() {
        UserInfo currentUser = UserContext.getCurrentUser();
        return Strings.areEqual(currentUser.getUserId(), personEntity) || currentUser.hasPermission(
                PERMISSION_EDIT_COMMENTS);
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
}
