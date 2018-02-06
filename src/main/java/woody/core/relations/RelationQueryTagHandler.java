/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.web.QueryTag;
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
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.colors.Colors;

import javax.annotation.Nonnull;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class RelationQueryTagHandler implements QueryTagHandler {

    public static final String TYPE_RELATION = "relation";

    @Part
    private static Colors colors;

    @Override
    public Constraint generateConstraint(EntityDescriptor descriptor, String tagValue) {
        Tuple<String, String> typeAndObjectName = Strings.split(tagValue, ":");
        Exists constraint = createConstraintForTarget(typeAndObjectName.getSecond());
        constraint.where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(descriptor.getType())));
        if (Strings.isFilled(typeAndObjectName.getFirst())) {
            constraint.where(FieldOperator.on(Relation.TYPE).eq(Long.parseLong(typeAndObjectName.getFirst())));
        }
        return constraint;
    }

    protected static Exists createConstraintForTarget(String target) {
        if (target.endsWith("*")) {
            return Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                         .where(Like.on(Relation.TARGET).contains(target));
        } else {
            return Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                         .where(FieldOperator.on(Relation.TARGET).eq(target));
        }
    }

    protected static Exists generateRelationExistsConstraint(Class<? extends Entity> sourceType, String target) {
        return createConstraintForTarget(target).where(FieldOperator.on(Relation.OWNER_TYPE)
                                                                    .eq(Schema.getNameForType(sourceType)))
                                                .where(FieldOperator.on(Relation.TYPE.join(RelationType.LIST_REVERSE))
                                                                    .eq(true));
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_RELATION;
    }

    public static String createShowRelatedQuery(String uniqueName, String visibleName) {
        return new QueryTag(TYPE_RELATION,
                            colors.getColorForType(RelationQueryTagColorTypeProvider.TYPE),
                            ":" + uniqueName,
                            NLS.fmtr("RelationQueryTagHandler.relatesTo")
                               .set("target", visibleName)
                               .format()).toString();
    }
}
