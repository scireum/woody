/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.model.BizEntity;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

import java.time.LocalDate;

/**
 * Created by aha on 18.08.15.
 */
public class Project extends BizEntity {

    @Length(1024)
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    private boolean done;
    public static final Column DONE = Column.named("done");

    @NullAllowed
    private LocalDate deadline;
    public static final Column DEADLINE = Column.named("deadline");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
