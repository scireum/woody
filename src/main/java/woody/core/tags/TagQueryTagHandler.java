/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.QueryTagHandler;
import sirius.db.mixing.Constraint;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.constraints.Exists;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.kernel.di.std.Register;

import javax.annotation.Nonnull;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class TagQueryTagHandler implements QueryTagHandler {

    public static final String TYPE_TAG = "tag";

    @Override
    public Constraint generateConstraint(EntityDescriptor descriptor, String tagValue) {
        return Exists.matchingIn(Entity.ID, TagAssignment.class, TagAssignment.TARGET_ENTITY)
                     .where(FieldOperator.on(TagAssignment.TAG).eq(Long.parseLong(tagValue)))
                     .where(FieldOperator.on(TagAssignment.TARGET_TYPE).eq(descriptor.getTableName()));
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_TAG;
    }
}
