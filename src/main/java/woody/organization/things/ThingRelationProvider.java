/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import com.google.common.collect.Lists;
import sirius.biz.tenants.Tenants;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.relations.RelationProvider;
import woody.organization.categories.Category;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by aha on 11.01.17.
 */
@Register
public class ThingRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    protected Tenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return Schema.getNameForType(Thing.class);
    }

    @Override
    public void computeSearchSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        Long typeFilterId = null;
        Long categoryFilterId = null;

        if (subType != null) {
            if (subType.startsWith("CATEGORY:")) {
                categoryFilterId = Long.parseLong(subType.substring(9));
            } else {
                typeFilterId = Long.parseLong(subType);
            }
        }

        oma.select(Thing.class)
           .fields(Thing.ID, Thing.NAME, Thing.TYPE.join(ThingType.NAME))
           .eqIgnoreNull(Thing.TYPE, typeFilterId)
           .eqIgnoreNull(Thing.TYPE.join(ThingType.CATEGORY), categoryFilterId)
           .eq(Thing.TENANT, tenants.getRequiredTenant())
           .where(Like.allWordsInAnyField(query, Thing.NAME, Thing.CODE))
           .orderAsc(Thing.CODE)
           .orderAsc(Thing.NAME)
           .iterateAll(thing -> {
               consumer.accept(Tuple.create(thing.getUniqueName(), thing.getName()));
           });
    }

    @Override
    public void computeTargetSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        computeSearchSuggestions(subType, query, consumer);
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        Tuple<String, Long> typeAndId = Schema.parseUniqueName(uniqueObjectName);
        return oma.select(Thing.class)
                  .fields(Thing.ID, Thing.NAME)
                  .eq(Thing.ID, typeAndId.getSecond())
                  .eq(Thing.TENANT, tenants.getRequiredTenant())
                  .first()
                  .map(item -> ComparableTuple.create(item.getName(), "/thing/" + item.getIdAsString()));
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        String prefix = Schema.getNameForType(Thing.class) + "-";
        String typeNameSuffix = " (" + NLS.get("Thing.plural") + ")";
        List<Tuple<String, String>> result = Lists.newArrayList();
        result.add(Tuple.create(Schema.getNameForType(Thing.class), NLS.get("Thing.plural")));
        result.addAll(oma.select(Category.class)
                         .eq(Category.TENANT, tenants.getRequiredTenant())
                         .eq(Category.TYPE, ThingCategoryTypeProvider.TYPE_NAME)
                         .orderAsc(Category.NAME)
                         .queryList()
                         .stream()
                         .map(category -> Tuple.create(prefix + "CATEGORY:" + category.getIdAsString(),
                                                       category.getName() + typeNameSuffix))
                         .collect(Collectors.toList()));
        result.addAll(oma.select(ThingType.class)
                         .fields(ThingType.ID,
                                 ThingType.NAME,
                                 ThingType.CATEGORY.join(Category.ID),
                                 ThingType.CATEGORY.join(Category.NAME))
                         .eq(ThingType.TENANT, tenants.getRequiredTenant())
                         .orderAsc(ThingType.NAME)
                         .queryList()
                         .stream()
                         .map(type -> Tuple.create(prefix + type.getIdAsString(),
                                                   type.getName()
                                                   + " ("
                                                   + type.getCategory().getValue().getName()
                                                   + ")"))
                         .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<Tuple<String, String>> getTargetTypes() {
        return getSourceTypes();
    }
}
