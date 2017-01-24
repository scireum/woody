/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

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
public class UnitRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    protected Tenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return Schema.getNameForType(Unit.class);
    }

    @Override
    public void computeSuggestions(String subType,
                                   String query,
                                   boolean forSearch,
                                   Consumer<Tuple<String, String>> consumer) {
        oma.select(Unit.class)
           .fields(Unit.ID, Unit.NAME, Unit.UNIQUE_PATH)
           .eqIgnoreNull(Unit.TYPE, subType == null ? null : Long.parseLong(subType))
           .eq(Unit.TENANT, tenants.getRequiredTenant())
           .where(Like.allWordsInAnyField(query, Unit.NAME))
           .orderAsc(Unit.NAME)
           .iterateAll(unit -> {
               String id = Schema.getNameForType(Unit.class) + "-" + unit.getUniquePath();
               if (forSearch) {
                   id += "*";
               }
               consumer.accept(Tuple.create(id, unit.getName()));
           });
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        if (Strings.isEmpty(uniqueObjectName)) {
            return Optional.empty();
        }

        Tuple<String, String> typeAndId = Strings.split(uniqueObjectName, "-");
        return oma.select(Unit.class)
                  .fields(Unit.ID, Unit.NAME)
                  .eq(Unit.UNIQUE_PATH, typeAndId.getSecond())
                  .eq(Unit.TENANT, tenants.getRequiredTenant())
                  .first()
                  .map(unit -> ComparableTuple.create(unit.getName(), "/unit/" + unit.getIdAsString()));
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        String prefix = Schema.getNameForType(Unit.class) + "-";
        String typeNameSuffix = " (" + NLS.get("Unit.plural") + ")";
        List<Tuple<String, String>> result = Lists.newArrayList();
        result.add(Tuple.create(Schema.getNameForType(Unit.class), NLS.get("Unit.plural")));
        result.addAll(oma.select(UnitType.class)
                         .eq(UnitType.TENANT, tenants.getRequiredTenant())
                         .orderAsc(UnitType.NAME)
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
