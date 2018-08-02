/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.db.jdbc.OMA;
import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.constraints.SQLConstraint;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.query.QueryTagHandler;
import sirius.db.mixing.query.constraints.FilterFactory;
import sirius.kernel.di.std.Register;

import javax.annotation.Nonnull;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class NotTagQueryTagHandler implements QueryTagHandler<SQLConstraint> {

    public static final String TYPE_NOT_TAG = "notTag";

    @Override
    public SQLConstraint generateConstraint(FilterFactory<SQLConstraint> filters,
                                            EntityDescriptor descriptor,
                                            String tagValue) {
        return OMA.FILTERS.not(OMA.FILTERS.existsIn(SQLEntity.ID, TagAssignment.class, TagAssignment.TARGET_ENTITY)
                                          .where(filters.eq(TagAssignment.TAG, Long.parseLong(tagValue)))
                                          .where(filters.eq(TagAssignment.TARGET_TYPE, descriptor.getRelationName())));
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_NOT_TAG;
    }
}
