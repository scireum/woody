/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.Tenants;
import sirius.biz.web.QueryTag;
import sirius.biz.web.QueryTagSuggester;
import sirius.db.mixing.Entity;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Like;
import sirius.db.mixing.constraints.Or;
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

    @Parts(RelationProvider.class)
    private Collection<RelationProvider> relationProviders;

    @Part
    private GlobalContext context;

    @Part
    private Colors colors;

    @Override
    public void computeQueryTags(@Nonnull String type,
                                 @Nullable Class<? extends Entity> entityType,
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
        String sourceTypeName = Schema.getNameForType(entityType);

        oma.select(RelationType.class)
           .fields(RelationType.ID,
                   RelationType.SOURCE_TYPE,
                   RelationType.TARGET_TYPE,
                   RelationType.NAME,
                   RelationType.VIEW_IN_LIST,
                   RelationType.MULTIPLE,
                   RelationType.COLOR.inner(ColorData.COLOR).join(ColorDefinition.HEX_CODE))
           .eq(RelationType.TENANT, tenants.getRequiredTenant())
           .where(Or.of(FieldOperator.on(RelationType.SOURCE_TYPE).eq(sourceTypeName),
                        Like.on(RelationType.SOURCE_TYPE).matches(sourceTypeName + "-*")))
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
