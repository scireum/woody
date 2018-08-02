/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.db.jdbc.OMA;
import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.constraints.Exists;
import sirius.db.jdbc.constraints.SQLConstraint;
import sirius.db.mixing.BaseEntity;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.QueryTag;
import sirius.db.mixing.query.QueryTagHandler;
import sirius.db.mixing.query.constraints.FilterFactory;
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
public class RelationQueryTagHandler implements QueryTagHandler<SQLConstraint> {

    public static final String TYPE_RELATION = "relation";

    @Part
    private static Colors colors;
    @Part
    private static Mixing mixing;

    @Override
    public SQLConstraint generateConstraint(FilterFactory<SQLConstraint> filters,
                                            EntityDescriptor descriptor,
                                            String tagValue) {
        Tuple<String, String> typeAndObjectName = Strings.split(tagValue, ":");
        Exists constraint = createConstraintForTarget(typeAndObjectName.getSecond());
        constraint.where(OMA.FILTERS.eq(Relation.OWNER_TYPE, mixing.getNameForType(descriptor.getType())));
        if (Strings.isFilled(typeAndObjectName.getFirst())) {
            constraint.where(OMA.FILTERS.eq(Relation.TYPE, Long.parseLong(typeAndObjectName.getFirst())));
        }
        return constraint;
    }

    protected static Exists createConstraintForTarget(String target) {
        if (target.endsWith("*")) {
            return OMA.FILTERS.existsIn(SQLEntity.ID, Relation.class, Relation.OWNER_ID)
                              .where(OMA.FILTERS.like(Relation.TARGET).startsWith(target).build());
        } else {
            return OMA.FILTERS.existsIn(SQLEntity.ID, Relation.class, Relation.OWNER_ID)
                              .where(OMA.FILTERS.eq(Relation.TARGET, target));
        }
    }

    protected static Exists generateRelationExistsConstraint(Class<? extends BaseEntity<?>> sourceType, String target) {
        return createConstraintForTarget(target).where(OMA.FILTERS.eq(Relation.OWNER_TYPE,
                                                                      mixing.getNameForType(sourceType)))
                                                .where(OMA.FILTERS.eq(Relation.TYPE.join(RelationType.LIST_REVERSE),
                                                                      true));
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
