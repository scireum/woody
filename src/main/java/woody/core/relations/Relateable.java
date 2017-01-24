/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.db.mixing.Composite;
import sirius.db.mixing.Entity;
import sirius.db.mixing.OMA;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.Transient;
import sirius.kernel.di.std.Part;

/**
 * Created by aha on 11.01.17.
 */
public class Relateable extends Composite {

    @Transient
    protected final Entity owner;


    public Relateable(Entity owner) {
        this.owner = owner;
    }

    @Part
    private static OMA oma;

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Relation.class)
               .eq(Relation.TARGET, owner.getUniqueName())
               .delete();
        }
    }

}
