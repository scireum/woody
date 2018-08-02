/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import sirius.biz.jdbc.tenants.Tenants;
import sirius.db.jdbc.OMA;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
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
public abstract class BasicRelationProvider implements RelationProvider {

    protected static final String CATEGORY_PREFIX = "CATEGORY:";

    @Part
    private OMA oma;

    @Part
    private Mixing mixing;

    @Part
    protected Tenants tenants;

    @NotNull
    protected abstract Class<? extends BasicElement<?>> getType();

    @NotNull
    protected abstract String getURLPrefix();

    @NotNull
    protected abstract Class<? extends BasicType> getMetaType();

    @NotNull
    protected abstract String getCategoryTypeName();

    @Nonnull
    @Override
    public String getName() {
        return Mixing.getNameForType(getType());
    }

    @Override
    public void computeSearchSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        Long typeFilterId = null;
        Long categoryFilterId = null;

        if (subType != null) {
            if (subType.startsWith(CATEGORY_PREFIX)) {
                categoryFilterId = Long.parseLong(subType.substring(9));
            } else {
                typeFilterId = Long.parseLong(subType);
            }
        }

        oma.select(getType())
           .fields(BasicElement.ID, BasicElement.NAME, BasicElement.TYPE.join(BasicType.NAME))
           .eqIgnoreNull(BasicElement.TYPE, typeFilterId)
           .eqIgnoreNull(BasicElement.TYPE.join(BasicType.CATEGORY), categoryFilterId)
           .eq(BasicElement.TENANT, tenants.getRequiredTenant())
           .queryString(query, QueryField.contains(BasicElement.NAME), QueryField.contains(BasicElement.CODE))
           .orderAsc(BasicElement.CODE)
           .orderAsc(BasicElement.NAME)
           .iterateAll(element -> {
               consumer.accept(Tuple.create(element.getUniqueName(), element.getName()));
           });
    }

    @Override
    public void computeTargetSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        computeSearchSuggestions(subType, query, consumer);
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        Tuple<String, String> typeAndId = Mixing.splitUniqueName(uniqueObjectName);
        return oma.select(getType())
                  .fields(BasicElement.ID, BasicElement.NAME)
                  .eq(BasicElement.ID, typeAndId.getSecond())
                  .eq(BasicElement.TENANT, tenants.getRequiredTenant())
                  .first()
                  .map(item -> ComparableTuple.create(item.getName(), getURLPrefix() + "/" + item.getIdAsString()));
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        List<Tuple<String, String>> result = Lists.newArrayList();

        addGeneralType(result);
        addCategoryTypes(result);
        addUserDefinedTypes(result);

        return result;
    }

    protected void addGeneralType(List<Tuple<String, String>> result) {
        result.add(Tuple.create(Mixing.getNameForType(getType()), mixing.getDescriptor(getType()).getPluralLabel()));
    }

    protected void addCategoryTypes(List<Tuple<String, String>> result) {
        String prefix = Mixing.getNameForType(getType()) + "-";
        String typeNameSuffix = " (" + mixing.getDescriptor(getType()).getPluralLabel() + ")";
        result.addAll(oma.select(Category.class)
                         .eq(Category.TENANT, tenants.getRequiredTenant())
                         .eq(Category.TYPE, getCategoryTypeName())
                         .orderAsc(Category.NAME)
                         .queryList()
                         .stream()
                         .map(category -> Tuple.create(prefix + CATEGORY_PREFIX + category.getIdAsString(),
                                                       category.getName() + typeNameSuffix))
                         .collect(Collectors.toList()));
    }

    protected void addUserDefinedTypes(List<Tuple<String, String>> result) {
        String prefix = Mixing.getNameForType(getType()) + "-";
        result.addAll(oma.select(getMetaType())
                         .fields(BasicType.ID, BasicType.NAME, BasicType.CATEGORY.join(Category.NAME))
                         .eq(BasicType.TENANT, tenants.getRequiredTenant())
                         .orderAsc(BasicType.NAME)
                         .queryList()
                         .stream()
                         .map(type -> Tuple.create(prefix + type.getIdAsString(),
                                                   type.getName()
                                                   + " ("
                                                   + type.getCategory().getValue().getName()
                                                   + ")"))
                         .collect(Collectors.toList()));
    }

    @Override
    public List<Tuple<String, String>> getTargetTypes() {
        return getSourceTypes();
    }
}
