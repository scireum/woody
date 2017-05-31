/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.Tenants;
import sirius.biz.web.MagicSearch;
import sirius.db.mixing.Entity;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.SmartQuery;
import sirius.db.mixing.constraints.Exists;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Like;
import sirius.db.mixing.constraints.Or;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import java.util.function.Consumer;

/**
 * Created by aha on 25.01.17.
 */
@Register(classes = RelationHelper.class)
public class RelationHelper {

    public static final String TYPE_NOT_RELATION = "notrelation";
    public static final String TYPE_RELATION = "relation";
    public static final String CSS_NOT_RELATION = "suggestion-notrelation";
    public static final String CSS_RELATION = "suggestion-relation";

    @Part
    private OMA oma;

    @Part
    private Tenants tenants;

    @Part
    private GlobalContext context;

    private static final ComparableTuple<String, String> EMPTY = ComparableTuple.create("-", null);

    private final Cache<String, ComparableTuple<String, String>> targetNameCache =
            CacheManager.createCache("relation-targets");

    public void computeSuggestions(Class<? extends Entity> type,
                                   String query,
                                   Consumer<MagicSearch.Suggestion> consumer) {
        if (!query.startsWith("!:") && !query.startsWith(":")) {
            return;
        }
        boolean inverted = query.startsWith("!:");
        String effectiveQuery = query.substring(inverted ? 2 : 1);
        String sourceTypeName = Schema.getNameForType(type);

        oma.select(RelationType.class)
           .eq(RelationType.TENANT, tenants.getRequiredTenant())
           .where(Or.of(FieldOperator.on(RelationType.SOURCE_TYPE).eq(sourceTypeName),
                        Like.on(RelationType.SOURCE_TYPE).matches(sourceTypeName + "-*")))
           .orderAsc(RelationType.SOURCE_TYPE)
           .orderAsc(RelationType.TARGET_TYPE)
           .iterateAll(relationType -> {
               if (relationType.getTargetType() == null) {
                   for (RelationProvider provider : context.getParts(RelationProvider.class)) {
                       provider.computeSearchSuggestions(null, effectiveQuery, suggestion -> {
                           consumer.accept(new MagicSearch.Suggestion(relationType.getName()
                                                                      + ": "
                                                                      + suggestion.getSecond()).withValue(relationType.getIdAsString()
                                                                                                          + ":"
                                                                                                          + suggestion.getFirst())
                                                                                               .withType(inverted ?
                                                                                                         TYPE_NOT_RELATION :
                                                                                                         TYPE_RELATION)
                                                                                               .withCSS(inverted ?
                                                                                                        CSS_RELATION :
                                                                                                        CSS_NOT_RELATION));
                       });
                   }
               } else {
                   Tuple<String, String> mainAndSubType = Strings.split(relationType.getTargetType(), "-");
                   RelationProvider provider = context.findPart(mainAndSubType.getFirst(), RelationProvider.class);
                   provider.computeSearchSuggestions(mainAndSubType.getSecond(), effectiveQuery, suggestion -> {
                       consumer.accept(new MagicSearch.Suggestion(relationType.getName()
                                                                  + ": "
                                                                  + suggestion.getSecond()).withValue(relationType.getIdAsString()
                                                                                                      + ":"
                                                                                                      + suggestion.getFirst())
                                                                                           .withType(inverted ?
                                                                                                     TYPE_NOT_RELATION :
                                                                                                     TYPE_RELATION)
                                                                                           .withCSS(inverted ?
                                                                                                    CSS_RELATION :
                                                                                                    CSS_NOT_RELATION));
                   });
               }
           });
    }

    public void applySuggestions(Class<? extends Entity> type, MagicSearch search, SmartQuery<? extends Entity> query) {
        for (MagicSearch.Suggestion suggestion : search.getSuggestions()) {
            Tuple<String, String> typeAndObjectName = Strings.split(suggestion.getValue(), ":");
            if (TYPE_RELATION.equals(suggestion.getType())) {
                if (typeAndObjectName.getSecond().endsWith("*")) {
                    query.where(Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                                      .where(FieldOperator.on(Relation.TYPE)
                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
                                      .where(Like.on(Relation.TARGET).contains(typeAndObjectName.getSecond())));
                } else {
                    query.where(Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                                      .where(FieldOperator.on(Relation.TYPE)
                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
                                      .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond())));
                }
            } else if (TYPE_NOT_RELATION.equals(suggestion.getType())) {
                //TODO * --> like
                query.where(Exists.notMatchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
                                  .where(FieldOperator.on(Relation.TYPE)
                                                      .eq(Long.parseLong(typeAndObjectName.getFirst())))
                                  .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
                                  .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond())));
            }
        }
    }

    public String getTargetName(Relation relation) {
        return getTargetNameAndUri(relation).getFirst();
    }

    public ComparableTuple<String, String> getTargetNameAndUri(Relation relation) {
        return targetNameCache.get(relation.getTarget(), this::computeTargetNameAndUri);
    }

    private ComparableTuple<String, String> computeTargetNameAndUri(String targetName) {
        try {
            if (Strings.isEmpty(targetName)) {
                return EMPTY;
            }

            Tuple<String, String> typeAndName = Strings.split(targetName, "-");
            return context.findPart(typeAndName.getFirst(), RelationProvider.class)
                          .resolveNameAndUri(targetName)
                          .orElse(EMPTY);
        } catch (Throwable e) {
            Exceptions.ignore(e);
            return EMPTY;
        }
    }
}
