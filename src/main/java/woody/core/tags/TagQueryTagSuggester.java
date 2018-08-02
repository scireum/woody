/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.QueryTagSuggester;
import sirius.db.jdbc.OMA;
import sirius.db.mixing.BaseEntity;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.QueryField;
import sirius.db.mixing.query.QueryTag;
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

    @Part
    private Mixing mixing;

    @Override
    public void computeQueryTags(@Nonnull String type,
                                 @Nullable Class<? extends BaseEntity<?>> entityType,
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
           .eq(Tag.TARGET_TYPE, mixing.getNameForType(entityType))
           .orderAsc(Tag.NAME)
           .queryString(searchTerm, QueryField.contains(Tag.NAME))
           .iterateAll(t -> {
               if (inverted) {
                   consumer.accept(new QueryTag(NotTagQueryTagHandler.TYPE_NOT_TAG,
                                                t.getEffectiveColor(),
                                                t.getIdAsString(),
                                                "!" + t.getName()));
               } else {
                   consumer.accept(new QueryTag(TagQueryTagHandler.TYPE_TAG,
                                                t.getEffectiveColor(),
                                                t.getIdAsString(),
                                                t.getName()));
               }
           });
    }
}
