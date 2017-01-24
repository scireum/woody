/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.inventory;

import com.google.common.collect.Lists;
import sirius.biz.tenants.Tenants;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.relations.RelationProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by aha on 11.01.17.
 */
@Register
public class InventoryItemRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    protected Tenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return Schema.getNameForType(InventoryItem.class);
    }

    @Override
    public void computeSuggestions(String subType,
                                   String query,
                                   boolean forSearch,
                                   Consumer<Tuple<String, String>> consumer) {
        oma.select(InventoryItem.class)
           .fields(InventoryItem.ID, InventoryItem.NAME, InventoryItem.TYPE.join(InventoryType.NAME))
           .eqIgnoreNull(InventoryItem.TYPE, subType == null ? null : Long.parseLong(subType))
           .eq(InventoryItem.TENANT, tenants.getRequiredTenant())
           .where(Like.allWordsInAnyField(query, InventoryItem.NAME, InventoryItem.CODE))
           .orderAsc(InventoryItem.CODE)
           .orderAsc(InventoryItem.NAME)
           .iterateAll(item -> {
               consumer.accept(Tuple.create(item.getUniqueName(), item.getName()));
           });
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        if (Strings.isEmpty(uniqueObjectName)) {
            return Optional.empty();
        }

        Tuple<String, String> typeAndId = Strings.split(uniqueObjectName, "-");
        return oma.select(InventoryItem.class)
                  .fields(InventoryItem.ID, InventoryItem.NAME)
                  .eq(InventoryItem.ID, Long.parseLong(typeAndId.getSecond()))
                  .eq(InventoryItem.TENANT, tenants.getRequiredTenant())
                  .first()
                  .map(item -> ComparableTuple.create(item.getName(), "/inventory/" + item.getIdAsString()));
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        String prefix = Schema.getNameForType(InventoryItem.class) + "-";
        String typeNameSuffix = " (" + NLS.get("InventoryItem.plural") + ")";
        List<Tuple<String, String>> result = Lists.newArrayList();
        result.add(Tuple.create(Schema.getNameForType(InventoryItem.class), NLS.get("InventoryItem.plural")));
        result.addAll(oma.select(InventoryType.class)
                         .eq(InventoryType.TENANT, tenants.getRequiredTenant())
                         .orderAsc(InventoryType.NAME)
                         .queryList()
                         .stream()
                         .map(type -> Tuple.create(prefix + type.getIdAsString(), type.getName() + typeNameSuffix))
                         .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<Tuple<String, String>> getTargetTypes() {
        return getSourceTypes();
    }
}
