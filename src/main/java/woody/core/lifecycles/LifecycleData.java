/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.lifecycles;

import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Transient;

import java.time.LocalDateTime;

public class LifecycleData extends Composite {

    public static final Mapping LIFECYCLE = Mapping.named("lifecycle");
    @NullAllowed
    private final SQLEntityRef<Lifecycle> lifecycle = SQLEntityRef.on(Lifecycle.class, SQLEntityRef.OnDelete.SET_NULL);

    public static final Mapping STATE = Mapping.named("state");
    @NullAllowed
    private final SQLEntityRef<LifecycleState> state =
            SQLEntityRef.on(LifecycleState.class, SQLEntityRef.OnDelete.SET_NULL);

    public static final Mapping STAGE = Mapping.named("stage");
    private Stage stage = Stage.ACTIVE;

    private LocalDateTime lastStateChange = LocalDateTime.now();

    private LocalDateTime lastStageChange = LocalDateTime.now();

    @Transient
    private SQLEntity owner;

    @Transient
    private Mapping parentColumn;

    public LifecycleData(SQLEntity owner, Mapping parentColumn) {
        this.owner = owner;
        this.parentColumn = parentColumn;
    }

    @BeforeSave
    protected void onModify() {
        if (owner.isChanged(parentColumn.inner(STATE))) {
            if (getState().isEmpty()) {
                stage = Stage.ACTIVE;
            } else {
                stage = getState().getValue().getStage();
            }

            lastStateChange = LocalDateTime.now();
        }

        if (owner.isChanged(parentColumn.inner(STAGE))) {
            lastStageChange = LocalDateTime.now();
        }
    }

    public SQLEntityRef<Lifecycle> getLifecycle() {
        return lifecycle;
    }

    public SQLEntityRef<LifecycleState> getState() {
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
