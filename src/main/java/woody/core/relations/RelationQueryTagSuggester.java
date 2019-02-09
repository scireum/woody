/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.Tenants;
import sirius.biz.web.QueryTagSuggester;
import sirius.db.jdbc.OMA;
import sirius.db.mixing.BaseEntity;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.QueryTag;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Parts;
import sirius.kernel.di.std.Register;
import woody.core.colors.ColorData;
import woody.core.colors.ColorDefinition;
import woody.core.colors.Colors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by aha on 28.07.17.
 */
@Register
public class RelationQueryTagSuggester implements QueryTagSuggester {

    @Part
    private Tenants tenants;

    @Part
    private OMA oma;

    @Part
    private Mixing mixing;

    @Parts(RelationProvider.class)
    private Collection<RelationProvider> relationProviders;

    @Part
    private GlobalContext context;

    @Part
    private Colors colors;

    @Override
    public void computeQueryTags(@Nonnull String type,
                                 @Nullable Class<? extends BaseEntity<?>> entityType,
                                 @Nonnull String searchTerm,
                                 @Nonnull Consumer<QueryTag> consumer) {
        if (!searchTerm.startsWith("!:") && !searchTerm.startsWith(":")) {
            return;
        }
        if (entityType == null) {
            return;
        }
        boolean inverted = searchTerm.startsWith("!:");
        String effectiveQuery = searchTerm.substring(inverted ? 2 : 1);
        String sourceTypeName = mixing.getNameForType(entityType);

        oma.select(RelationType.class)
           .fields(RelationType.ID,
                   RelationType.SOURCE_TYPE,
                   RelationType.TARGET_TYPE,
                   RelationType.NAME,
                   RelationType.VIEW_IN_LIST,
                   RelationType.MULTIPLE,
                   RelationType.COLOR.inner(ColorData.COLOR).join(ColorDefinition.HEX_CODE))
           .eq(RelationType.TENANT, tenants.getRequiredTenant())
           .where(OMA.FILTERS.or(OMA.FILTERS.eq(RelationType.SOURCE_TYPE, sourceTypeName),
                                 OMA.FILTERS.like(RelationType.SOURCE_TYPE).matches(sourceTypeName + "-*").build()))
           .orderAsc(RelationType.SOURCE_TYPE)
           .orderAsc(RelationType.TARGET_TYPE)
           .iterateAll(relationType -> {
               if (relationType.getTargetType() == null) {
                   for (RelationProvider provider : relationProviders) {
                       provider.computeSearchSuggestions(null, effectiveQuery, suggestion -> {
                           consumer.accept(new QueryTag(RelationQueryTagHandler.TYPE_RELATION,
                                                        fetchEffectiveColor(relationType),
                                                        relationType.getIdAsString() + ":" + suggestion.getFirst(),
                                                        relationType.getName() + ": " + suggestion.getSecond()));
                       });
                   }
               } else {
                   Tuple<String, String> mainAndSubType = Strings.split(relationType.getTargetType(), "-");
                   RelationProvider provider = context.findPart(mainAndSubType.getFirst(), RelationProvider.class);
                   provider.computeSearchSuggestions(mainAndSubType.getSecond(), effectiveQuery, suggestion -> {
                       consumer.accept(new QueryTag(RelationQueryTagHandler.TYPE_RELATION,
                                                    fetchEffectiveColor(relationType),
                                                    relationType.getIdAsString() + ":" + suggestion.getFirst(),
                                                    relationType.getName() + ": " + suggestion.getSecond()));
                   });
               }
           });
    }

    protected String fetchEffectiveColor(RelationType relationType) {
        return colors.getColor(relationType.getColor().getColor())
                     .orElseGet(() -> colors.getColorForType(RelationQueryTagColorTypeProvider.TYPE));
    }
}
