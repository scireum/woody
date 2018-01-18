/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.tenants.TenantAware;
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.di.std.Part;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by aha on 18.08.15.
 */
public class Task extends TenantAware {

    @Length(1024)
    @Autoloaded
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> reporter = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);
    public static final Column REPORTER = Column.named("reporter");

    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> assignee = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);
    public static final Column ASSIGNEE = Column.named("assignee");

    @Autoloaded
    private boolean hidden;
    public static final Column HIDDEN = Column.named("hidden");

    @Autoloaded
    private boolean done;
    public static final Column DONE = Column.named("done");

    @NullAllowed
    @Autoloaded
    private LocalDate deadline;
    public static final Column DEADLINE = Column.named("deadline");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    @Part
    private Tenants tenants;

    @BeforeSave
    protected void verify() {
        if (getReporter().isEmpty()) {
            getReporter().setValue(tenants.getRequiredUser());
        }
        if (isHidden()) {
            getAssignee().setValue(tenants.getRequiredUser());
        }
    }

    public String getAge() {
        return String.valueOf(ChronoUnit.DAYS.between(getTrace().getCreatedAt().toLocalDate(), LocalDate.now()));
    }

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

    public EntityRef<UserAccount> getReporter() {
        return reporter;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Tagged getTags() {
        return tags;
    }

    public Commented getComments() {
        return comments;
    }
}
