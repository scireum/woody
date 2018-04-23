/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.lifecycles;

import sirius.db.mixing.Column;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Transient;

import java.time.LocalDateTime;

public class LifecycleData extends Composite {

    public static final Column LIFECYCLE = Column.named("lifecycle");
    @NullAllowed
    private final EntityRef<Lifecycle> lifecycle = EntityRef.on(Lifecycle.class, EntityRef.OnDelete.SET_NULL);

    public static final Column STATE = Column.named("state");
    @NullAllowed
    private final EntityRef<LifecycleState> state = EntityRef.on(LifecycleState.class, EntityRef.OnDelete.SET_NULL);

    public static final Column STAGE = Column.named("stage");
    private Stage stage = Stage.ACTIVE;

    private LocalDateTime lastStateChange = LocalDateTime.now();

    private LocalDateTime lastStageChange = LocalDateTime.now();

    @Transient
    private Entity owner;

    @Transient
    private Column parentColumn;

    public LifecycleData(Entity owner, Column parentColumn) {
        this.owner = owner;
        this.parentColumn = parentColumn;
    }

    @BeforeSave
    protected void onModify() {
        if (owner.isColumnChanged(parentColumn.inner(STATE))) {
            if (getState().isEmpty()) {
                stage = Stage.ACTIVE;
            } else {
                stage = getState().getValue().getStage();
            }

            lastStateChange = LocalDateTime.now();
        }

        if (owner.isColumnChanged(parentColumn.inner(STAGE))) {
            lastStageChange = LocalDateTime.now();
        }
    }

    public EntityRef<Lifecycle> getLifecycle() {
        return lifecycle;
    }

    public EntityRef<LifecycleState> getState() {
        return state;
    }

    public Stage getStage() {
        return stage;
    }

    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }

    public LocalDateTime getLastStageChange() {
        return lastStageChange;
    }
}
