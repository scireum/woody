/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.Tenants;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Entity;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.Transient;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Parts;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by aha on 11.01.17.
 */
public class Relations extends Composite {

    @Transient
    protected final Entity owner;

    public Relations(Entity owner) {
        this.owner = owner;
    }

    @Part
    private static Tenants tenants;

    @Parts(RelationProvider.class)
    private static Collection<RelationProvider> providers;

    @Part
    private static GlobalContext context;

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(Relation.class)
               .eq(Relation.OWNER_ID, owner.getId())
               .eq(Relation.OWNER_TYPE, owner.getTypeName())
               .delete();
        }
    }

    public String getAuthHash() {
        return RelationsController.computeAuthHash(owner.getUniqueName());
    }

    public List<Relation> getRelations() {
        List<Relation> result = oma.select(Relation.class)
                                   .fields(Relation.ID,
                                           Relation.TARGET,
                                           Relation.TYPE,
                                           Relation.TYPE.join(RelationType.NAME))
                                   .eq(Relation.OWNER_ID, owner.getId())
                                   .eq(Relation.OWNER_TYPE, owner.getTypeName())
                                   .queryList();
//        result.sort(Comparator.<Relation, String>comparing(relation -> relation.getType()
//                                                                               .getValue()
//                                                                               .getName()).thenComparing(helper::getTargetName));
        return result;
    }

    public List<ComparableTuple<String, String>> getListRelations() {
        List<ComparableTuple<String, String>> relations = oma.select(Relation.class)
                                                             .fields(Relation.ID,
                                                                     Relation.TARGET,
                                                                     Relation.TYPE,
                                                                     Relation.TYPE.join(RelationType.NAME))
                                                             .eq(Relation.OWNER_ID, owner.getId())
                                                             .eq(Relation.TYPE.join(RelationType.VIEW_IN_LIST), true)
                                                             .eq(Relation.OWNER_TYPE, owner.getTypeName())
                                                             .queryList()
                                                             .stream()
                                                             .map(relation -> {
                                                                 return getTargetNameAndUri(relation).map(t -> ComparableTuple
                                                                         .create(relation.getType().getValue().getName()
                                                                                 + ": "
                                                                                 + t.getFirst(), t.getSecond()))
                                                                                                     .orElse(ComparableTuple
                                                                                                                     .createTuple());
                                                             })
                                                             .collect(Collectors.toList());

        relations.sort(ComparableTuple::compareTo);
        return relations;
    }

    public String getTargetName(Relation relation) {
        return getTargetNameAndUri(relation).map(ComparableTuple::getFirst).orElse("-");
    }

    public static Optional<ComparableTuple<String, String>> getTargetNameAndUri(Relation relation) {
        try {
            Tuple<String, String> typeAndName = Strings.split(relation.getTarget(), "-");
            return context.findPart(typeAndName.getFirst(), RelationProvider.class)
                          .resolveNameAndUri(relation.getTarget());
        } catch (Throwable e) {
            return Optional.of(ComparableTuple.create(null, null));
        }
    }

//    -> Generic postComment
//    -> Generic deleteComment
//    -> Generic addRelation
//    -> Generic deleteComment

//    public static void computeSuggestions(Class<? extends Entity> type,
//                                          String query,
//                                          Consumer<MagicSearch.Suggestion> consumer) {
//        if (!query.startsWith("!:") && !query.startsWith(":")) {
//            return;
//        }
//        boolean inverted = query.startsWith("!:");
//        String effectiveQuery = query.substring(inverted ? 2 : 1);
//        String sourceTypeName = Schema.getNameForType(type);
//
//        oma.select(RelationType.class)
//           .eq(RelationType.TENANT, tenants.getRequiredTenant())
//           .where(Or.of(FieldOperator.on(RelationType.SOURCE_TYPE).eq(sourceTypeName),
//                        Like.on(RelationType.SOURCE_TYPE).matches(sourceTypeName + "-*")))
//           .orderAsc(RelationType.SOURCE_TYPE)
//           .orderAsc(RelationType.TARGET_TYPE)
//           .iterateAll(relationType -> {
//               if (relationType.getTargetType() == null) {
//                   for (RelationProvider provider : context.getParts(RelationProvider.class)) {
//                       provider.computeSuggestions(null, effectiveQuery, true, suggestion -> {
//                           consumer.accept(new MagicSearch.Suggestion(relationType.getName()
//                                                                      + ": "
//                                                                      + suggestion.getSecond()).withValue(relationType.getIdAsString()
//                                                                                                          + ":"
//                                                                                                          + suggestion.getFirst())
//                                                                                               .withType(inverted ?
//                                                                                                         TYPE_NOT_RELATION :
//                                                                                                         TYPE_RELATION)
//                                                                                               .withCSS(inverted ?
//                                                                                                        CSS_RELATION :
//                                                                                                        CSS_NOT_RELATION));
//                       });
//                   }
//               } else {
//                   Tuple<String, String> mainAndSubType = Strings.split(relationType.getTargetType(), "-");
//                   RelationProvider provider = context.findPart(mainAndSubType.getFirst(), RelationProvider.class);
//                   provider.computeSuggestions(mainAndSubType.getSecond(), effectiveQuery, true, suggestion -> {
//                       consumer.accept(new MagicSearch.Suggestion(relationType.getName()
//                                                                  + ": "
//                                                                  + suggestion.getSecond()).withValue(relationType.getIdAsString()
//                                                                                                      + ":"
//                                                                                                      + suggestion.getFirst())
//                                                                                           .withType(inverted ?
//                                                                                                     TYPE_NOT_RELATION :
//                                                                                                     TYPE_RELATION)
//                                                                                           .withCSS(inverted ?
//                                                                                                    CSS_RELATION :
//                                                                                                    CSS_NOT_RELATION));
//                   });
//               }
//           });
//    }
//
//    public static void applySuggestions(Class<? extends Entity> type,
//                                        MagicSearch search,
//                                        SmartQuery<? extends Entity> query) {
//        for (MagicSearch.Suggestion suggestion : search.getSuggestions()) {
//            Tuple<String, String> typeAndObjectName = Strings.split(suggestion.getValue(), ":");
//            if (TYPE_RELATION.equals(suggestion.getType())) {
//                if (typeAndObjectName.getSecond().endsWith("*")) {
//                    query.where(Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
//                                      .where(FieldOperator.on(Relation.TYPE)
//                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
//                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
//                                      .where(Like.on(Relation.TARGET).contains(typeAndObjectName.getSecond())));
//                } else {
//                    query.where(Exists.matchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
//                                      .where(FieldOperator.on(Relation.TYPE)
//                                                          .eq(Long.parseLong(typeAndObjectName.getFirst())))
//                                      .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
//                                      .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond())));
//                }
//            } else if (TYPE_NOT_RELATION.equals(suggestion.getType())) {
//                //TODO * --> like
//                query.where(Exists.notMatchingIn(Entity.ID, Relation.class, Relation.OWNER_ID)
//                                  .where(FieldOperator.on(Relation.TYPE)
//                                                      .eq(Long.parseLong(typeAndObjectName.getFirst())))
//                                  .where(FieldOperator.on(Relation.OWNER_TYPE).eq(Schema.getNameForType(type)))
//                                  .where(FieldOperator.on(Relation.TARGET).eq(typeAndObjectName.getSecond())));
//            }
//        }
//    }
}
