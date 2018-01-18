/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.lifecycles;

import sirius.db.mixing.Composite;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.NullAllowed;

import java.time.LocalDateTime;

public class LifecycleData extends Composite {

    @NullAllowed
    private final EntityRef<Lifecycle> lifecycle = EntityRef.on(Lifecycle.class, EntityRef.OnDelete.SET_NULL);

    @NullAllowed
    private final EntityRef<LifecycleState> state = EntityRef.on(LifecycleState.class, EntityRef.OnDelete.SET_NULL);

    private Stage stage = Stage.ACTIVE;

    private LocalDateTime lastStateChange = LocalDateTime.now();

    private LocalDateTime lastStageChange = LocalDateTime.now();

    private int checklistDataVersion = 1;

    //TODO make smarter / store reference to checklist / to KBA
    @Lob
    @NullAllowed
    private String checklistData;
}
