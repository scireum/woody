/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.web.QueryTagHandler;
import sirius.db.mixing.Constraint;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Exists;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Register;

import javax.annotation.Nonnull;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class RelationQueryTagHandler implements QueryTagHandler {

    public static final String TYPE_RELATION = "relation";

    @Override
    public Constraint generateConstraint(EntityDescriptor descriptor, String tagValue) {
            Tuple<String, String> typeAndObjectName = Strings.split(tagValue, ":");
//            if (TYPE_RELATION.equals(suggestion.getType())) {
                if (typeAndObjectName.getSecond().endsWith("*")) {
                   return Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                                      .where(FieldOperator.on(Relation.TYPE)
                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(descriptor.getType())))
                                      .where(Like.on(Relation.TARGET).contains(typeAndObjectName.getSecond()));
                } else {
                    return  Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                                      .where(FieldOperator.on(Relation.TYPE)
                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(descriptor.getType())))
                                      .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond()));
                }
//            } else if (TYPE_NOT_RELATION.equals(suggestion.getType())) {
//                //TODO * --> like
//                query.where(Exists.notMatchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
//                                  .where(FieldOperator.on(Relation.TYPE)
//                                                      .eq(Long.parseLong(typeAndObjectName.getFirst())))
//                                  .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
//                                  .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond())));
//            }
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_RELATION;
    }
}
