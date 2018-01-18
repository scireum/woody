/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization;

import sirius.db.mixing.OMA;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.security.HelperFactory;
import sirius.web.security.ScopeInfo;
import sirius.web.security.UserContext;
import woody.core.relations.Relateable;
import woody.organization.categories.Category;
import woody.organization.things.Thing;
import woody.organization.things.ThingType;
import woody.organization.units.Unit;
import woody.organization.units.UnitType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class OrganizationHelper {

    @Register
    public static class OrganizationHelperFactory implements HelperFactory<OrganizationHelper> {

        @Nonnull
        @Override
        public Class<OrganizationHelper> getHelperType() {
            return OrganizationHelper.class;
        }

        @Nonnull
        @Override
        public String getName() {
            return "organization";
        }

        @Nonnull
        @Override
        public OrganizationHelper make(@Nonnull ScopeInfo scopeInfo) {
            return new OrganizationHelper();
        }
    }

    @Part
    private static OMA oma;

    private static Cache<String, List<Category>> categoriesPerTenant =
            CacheManager.createCache("categories-per-tenant");

    public static void flushCategoryCache(Long tenantId) {
        if (tenantId == null) {
            return;
        }
        categoriesPerTenant.remove(String.valueOf(tenantId));
    }

    public List<Tuple<String, String>> getAccessibleCategories() {
        return getCategories().stream()
                              .map(category -> Tuple.create(category.getProvider().makeUrl(category.getTechnicalName()),
                                                            category.getName()))
                              .collect(Collectors.toList());
    }

    public List<Category> getCategories() {
        //TODO filter by role
        return categoriesPerTenant.get(UserContext.getCurrentUser().getTenantId(), this::fetchCategoriesPerTenant);
    }

    private List<Category> fetchCategoriesPerTenant(String tenantId) {
        return oma.select(Category.class)
                  .eq(Category.TENANT, Long.parseLong(tenantId))
                  .orderAsc(Category.NAME)
                  .queryList();
    }

    public List<Thing> queryRelatedThings(Relateable target, Category category) {
        return oma.select(Thing.class)
                                .fields(Thing.ID, Thing.NAME, Thing.CODE, Thing.TYPE.join(ThingType.NAME))
                                .eq(Thing.TENANT, Long.parseLong(UserContext.getCurrentUser().getTenantId()))
                                .eq(Thing.TYPE.join(ThingType.CATEGORY), category)
                                .where(target.generateRelationExistsConstraint(Thing.class))
                                .orderAsc(Thing.CODE)
                                .orderAsc(Thing.NAME)
                                .limit(5)
                                .queryList();
    }

    public List<Unit> queryRelatedUnits(Relateable target, Category category) {
        return oma.select(Unit.class)
                                .fields(Unit.ID, Unit.NAME, Unit.CODE, Unit.TYPE.join(UnitType.NAME))
                                .eq(Unit.TENANT, Long.parseLong(UserContext.getCurrentUser().getTenantId()))
                                .eq(Unit.TYPE.join(UnitType.CATEGORY), category)
                                .where(target.generateRelationExistsConstraint(Unit.class))
                                .orderAsc(Unit.CODE)
                                .orderAsc(Unit.NAME)
                                .limit(5)
                                .queryList();
    }

}
