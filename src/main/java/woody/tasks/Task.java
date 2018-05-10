/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.tenants.TenantAware;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.nls.NLS;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Holds basic information about a task to be done.
 * <p>
 * This includes information about the reporter, assignee, description and more.
 */
@SuppressWarnings({"squid:S1845", "squid:S2160", "squid:MaximumInheritanceDepth"})
public class Task extends TenantAware {

    /**
     * The creator of the task.
     */
    public static final Column REPORTER = Column.named("reporter");
    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> reporter = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);

    /**
     * The currently assigned {@link UserAccount user} who works on the task.
     */
    public static final Column ASSIGNEE = Column.named("assignee");
    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> assignee = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);

    public static final Column TITLE = Column.named("title");
    @Autoloaded
    @Length(255)
    private String title;

    public static final Column DESCRIPTION = Column.named("description");
    @Length(2048)
    @Autoloaded
    private String description;

    /**
     * The {@link LocalDate} defining the last possible day the task needs to be finished.
     */
    public static final Column DEADLINE = Column.named("deadline");
    @NullAllowed
    @Autoloaded
    private LocalDate deadline;

    /**
     * The actual {@link LocalDateTime} the task is finished.
     */
    public static final Column CLOSED = Column.named("closed");
    @NullAllowed
    private LocalDateTime closed;

    /**
     * Provides information about the current state of this task.
     */
    public static final Column STATE = Column.named("state");
    @Autoloaded
    private TaskState state;

    private final Tagged tags = new Tagged(this);

    private final Commented comments = new Commented(this);


    public String getAge() {
        return String.valueOf(ChronoUnit.DAYS.between(getTrace().getCreatedAt().toLocalDate(), LocalDate.now()));
    }

    @Override
    public String toString() {
        return isNew() ? NLS.get("Task.new") : Strings.apply("%s %s", NLS.get("Model.task"), id);
    }

    public EntityRef<UserAccount> getReporter() {
        return reporter;
    }

    public EntityRef<UserAccount> getAssignee() {
        return assignee;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getClosed() {
        return closed;
    }

    public void setClosed(LocalDateTime closed) {
        this.closed = closed;
    }

    public Tagged getTags() {
        return tags;
    }

    public Commented getComments() {
        return comments;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }
}
