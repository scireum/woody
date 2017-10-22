/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.QueryTag;
import sirius.biz.web.QueryTagSuggester;
import sirius.db.mixing.Entity;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class TagQueryTagSuggester implements QueryTagSuggester {

    @Part
    private OMA oma;

    @Override
    public void computeQueryTags(@Nonnull String type,
                                 @Nullable Class<? extends Entity> entityType,
                                 @Nonnull String searchTerm,
                                 @Nonnull Consumer<QueryTag> consumer) {
        if (entityType == null) {
            return;
        }
        boolean inverted = searchTerm.startsWith("!");
        if (inverted) {
            searchTerm = searchTerm.substring(1);
        }
        oma.select(Tag.class)
           .eq(Tag.TARGET_TYPE, Schema.getNameForType(entityType))
           .orderAsc(Tag.NAME)
           .where(Like.on(Tag.NAME).ignoreCase().ignoreEmpty().contains(searchTerm))
           .iterateAll(t -> {
               if (inverted) {
                   consumer.accept(new QueryTag(NotTagQueryTagHandler.TYPE_NOT_TAG,
                                                "black",
                                                t.getIdAsString(),
                                                "!" + t.getName()));
               } else {
                   consumer.accept(new QueryTag(TagQueryTagHandler.TYPE_TAG, "black", t.getIdAsString(), t.getName()));
               }
           });
    }
}
