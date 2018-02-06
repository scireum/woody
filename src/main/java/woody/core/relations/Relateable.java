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
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.Transient;
import sirius.db.mixing.constraints.Exists;

/**
 * Embedded into an entity to make it eligible for incoming relations.
 */
public class Relateable extends Composite {

    @Transient
    protected final Entity owner;

    /**
     * Creates and initializes the composite for the given entity.
     *
     * @param owner the entity which is the target for relations
     */
    public Relateable(Entity owner) {
        this.owner = owner;
    }

    public String getTargetName() {
        String targetString = ((IsRelateable) owner).getTargetString();
        if (targetString == null) {
            return owner.getUniqueName();
        } else {
            return targetString;
        }
    }

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Relation.class).eq(Relation.TARGET, owner.getUniqueName()).delete();
        }
    }

    public Exists generateRelationExistsConstraint(Class<? extends Entity> sourceType) {
        return RelationQueryTagHandler.generateRelationExistsConstraint(sourceType, getTargetName());
    }

    public String createShowRelatedQuery() {
        return RelationQueryTagHandler.createShowRelatedQuery(getTargetName(), owner.toString());
    }
}