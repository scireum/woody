/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.QueryTag;
import sirius.biz.web.QueryTagHandler;
import sirius.biz.web.QueryTagSuggester;
import sirius.db.mixing.Constraint;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Exists;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created by aha on 27.01.17.
 */
@Register
public class TagsQueryTagHandler implements QueryTagSuggester, QueryTagHandler {

    @Part
    private OMA oma;

    @Override
    public Constraint generateConstraint(EntityDescriptor entityDescriptor, String value) {
        boolean inverted = value.startsWith("!");
        if (inverted) {
            value = value.substring(1);
            return Exists.notMatchingIn(Entity.ID, TagAssignment.class, TagAssignment.TARGET_ENTITY)
                         .where(FieldOperator.on(TagAssignment.TAG).eq(value))
                         .where(FieldOperator.on(TagAssignment.TARGET_TYPE).eq(Schema.getNameForType(entityDescriptor.getType())));
        } else {
            return Exists.matchingIn(Entity.ID, TagAssignment.class, TagAssignment.TARGET_ENTITY)
                  .where(FieldOperator.on(TagAssignment.TAG).eq(value))
                  .where(FieldOperator.on(TagAssignment.TARGET_TYPE).eq(Schema.getNameForType(entityDescriptor.getType())));
        }
    }

    @Override
    public void computeQueryTags(@Nonnull String type,
                                 @Nullable Class<? extends Entity> entityType,
                                 @Nonnull String queryInput,
                                 @Nonnull Consumer<QueryTag> consumer) {
        String query = queryInput;
        boolean inverted = queryInput.startsWith("!");
        if (inverted) {
            query = queryInput.substring(1);
        }

        oma.select(Tag.class)
           .eq(Tag.TARGET_TYPE, type)
           .orderAsc(Tag.NAME)
           .where(Like.on(Tag.NAME).ignoreCase().ignoreEmpty().contains(query))
           .iterateAll(t -> {
               consumer.accept(new QueryTag(getName(),
                                            "default",
                                            (inverted ? "!" : "") + t.getIdAsString(),
                                            t.getName()));
           });
    }

    @Nonnull
    @Override
    public String getName() {
        return "tag";
    }
}
