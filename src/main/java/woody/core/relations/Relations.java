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
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.di.std.Part;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aha on 11.01.17.
 */
public class Relations extends Composite {

    @Transient
    protected final Entity owner;

    public Relations(Entity owner) {
        this.owner = owner;
    }

    @Part
    private static OMA oma;

    @Part
    private static RelationHelper helper;

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Relation.class)
               .eq(Relation.OWNER_ID, owner.getId())
               .eq(Relation.OWNER_TYPE, owner.getTypeName())
               .delete();
        }
    }

    public String getAuthHash() {
        return RelationsController.computeAuthHash(owner.getUniqueName());
    }

    public List<Relation> getRelations() {
        List<Relation> result = oma.select(Relation.class)
                                   .fields(Relation.ID,
                                           Relation.TARGET,
                                           Relation.TYPE,
                                           Relation.TYPE.join(RelationType.NAME))
                                   .eq(Relation.OWNER_ID, owner.getId())
                                   .eq(Relation.OWNER_TYPE, owner.getTypeName())
                                   .queryList();
        result.sort(Comparator.<Relation, String>comparing(relation -> relation.getType()
                                                                               .getValue()
                                                                               .getName()).thenComparing(helper::getTargetName));
        return result;
    }

    public List<ComparableTuple<String, String>> getListRelations() {
        List<Relation> relations = oma.select(Relation.class)
                                      .fields(Relation.ID,
                                              Relation.TARGET,
                                              Relation.TYPE,
                                              Relation.TYPE.join(RelationType.NAME))
                                      .eq(Relation.OWNER_ID, owner.getId())
                                      .eq(Relation.TYPE.join(RelationType.VIEW_IN_LIST), true)
                                      .eq(Relation.OWNER_TYPE, owner.getTypeName())
                                      .queryList();
        List<ComparableTuple<String, String>> result = relations.stream().map(relation -> {
            ComparableTuple<String, String> nameAndUri = helper.getTargetNameAndUri(relation);
            nameAndUri.setFirst(relation.getType().getValue().getName() + ": " + nameAndUri.getFirst());
            return nameAndUri;
        }).collect(Collectors.toList());

        result.sort(ComparableTuple::compareTo);
        return result;
    }

}
