/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.model.BizEntity;
import sirius.mixing.Column;
import sirius.mixing.Entity;
import sirius.mixing.EntityRef;

/**
 * Created by aha on 18.08.15.
 */
public class TaskListEntry extends Entity {

    private final EntityRef<TaskList> list = EntityRef.on(TaskList.class, EntityRef.OnDelete.CASCADE);
    public static final Column LIST = Column.named("list");

    private final EntityRef<Task> task = EntityRef.on(Task.class, EntityRef.OnDelete.CASCADE);
    public static final Column TASK = Column.named("task");

    private int position;
    public static final Column POSITION = Column.named("position");

    public EntityRef<TaskList> getList() {
        return list;
    }

    public EntityRef<Task> getTask() {
        return task;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
