/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;

/**
 * Created by aha on 18.08.15.
 */
public class Task extends BizEntity {

    @Length(length = 255)
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    @NullAllowed
    private final EntityRef<UserAccount> assignee = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);
    public static final Column ASSIGNEE = Column.named("assignee");

    private boolean hidden;
    public static final Column HIDDEN = Column.named("hidden");

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EntityRef<UserAccount> getAssignee() {
        return assignee;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
