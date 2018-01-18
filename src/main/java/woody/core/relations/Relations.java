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
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aha on 11.01.17.
 */
public class Relations extends Composite {

    @Transient
    protected final Entity owner;

    @Part
    private static GlobalContext context;

    @Part
    private static RelationHelper helper;

    public Relations(Entity owner) {
        this.owner = owner;
    }

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Relation.class)
               .eq(Relation.OWNER_ID, owner.getId())
               .eq(Relation.OWNER_TYPE, owner.getTypeName())
               .delete();
        }
    }

    public String getTargetsString() {
        String targetsString = ((HasRelations) owner).getTargetsString();
        if (targetsString == null) {
            return owner.getTypeName();
        } else {
            return targetsString;
        }
    }

    public String getSourceString() {
        return owner.getUniqueName();
    }

    public String getAuthHash() {
        return RelationController.computeAuthHash(owner.getUniqueName());
    }

    public List<Relation> getRelations() {
        return oma.select(Relation.class)
                  .fields(Relation.ID, Relation.TARGET, Relation.TYPE, Relation.TYPE.join(RelationType.NAME))
                  .eq(Relation.OWNER_ID, owner.getId())
                  .eq(Relation.OWNER_TYPE, owner.getTypeName())
                  .queryList();
    }

    public List<ComparableTuple<String, String>> getListRelations() {
        return oma.select(Relation.class)
                  .fields(Relation.ID, Relation.TARGET, Relation.TYPE, Relation.TYPE.join(RelationType.NAME))
                  .eq(Relation.OWNER_ID, owner.getId())
                  .eq(Relation.TYPE.join(RelationType.VIEW_IN_LIST), true)
                  .eq(Relation.OWNER_TYPE, owner.getTypeName())
                  .queryList()
                  .stream()
                  .map(relation -> {
                      ComparableTuple<String, String> nameAndUri = getTargetNameAndUri(relation);
                      return ComparableTuple.create(relation.getType().getValue().getName()
                                                    + ": "
                                                    + nameAndUri.getFirst(), nameAndUri.getSecond());
                  })
                  .sorted(ComparableTuple::compareTo)
                  .collect(Collectors.toList());
    }

    public ComparableTuple<String, String> getTargetNameAndUri(Relation relation) {
        return helper.getTargetNameAndUri(relation);
    }
}
