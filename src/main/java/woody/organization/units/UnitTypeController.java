/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import woody.core.colors.ColorData;
import woody.core.colors.ColorDefinition;
import woody.organization.BasicType;
import woody.organization.categories.Category;

import java.util.Optional;

@Register(classes = {Controller.class, UnitTypeController.class})
public class UnitTypeController extends BizController {

    private static final String PERMISSION_MANAGE_UNIT_TYPES = "permission-manage-unit-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/types")
    public void unitTypes(WebContext ctx) {
        PageHelper<UnitType> ph = PageHelper.withQuery(oma.select(UnitType.class)
                                                          .fields(UnitType.ID,
                                                                  UnitType.NAME,
                                                                  UnitType.DESCRIPTION,
                                                                  UnitType.CATEGORY.join(Category.ID),
                                                                  UnitType.CATEGORY.join(Category.NAME),
                                                                  UnitType.COLOR.inner(ColorData.COLOR)
                                                                                 .join(ColorDefinition.NAME),
                                                                  UnitType.COLOR.inner(ColorData.COLOR)
                                                                                 .join(ColorDefinition.HEX_CODE))
                                                          .orderAsc(UnitType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(UnitType.NAME, UnitType.DESCRIPTION).forCurrentTenant();

        ph.addQueryFacet(BasicType.CATEGORY.getName(),
                         NLS.get("BasicType.category"),
                         qry -> queryCategories().asSQLQuery());

        ctx.respondWith().template("/templates/organization/units/types.html.pasta", ph.asPage());
    }

    protected SmartQuery<Category> queryCategories() {
        return oma.select(Category.class)
                  .fields(Category.ID, Category.NAME)
                  .eq(Category.TENANT, currentTenant())
                  .eq(Category.TYPE, UnitCategoryTypeProvider.TYPE_NAME)
                  .orderAsc(Category.NAME);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/type/:1/delete")
    public void deleteType(WebContext ctx, String id) {
        Optional<UnitType> unitType = tryFindForTenant(UnitType.class, id);
        if (unitType.isPresent()) {
            oma.delete(unitType.get());
            showDeletedMessage();
        }
        ctx.respondWith().redirectToGet("/units/types");
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/type/:1")
    public void unitType(WebContext ctx, String typeId) {
        UnitType type = findForTenant(UnitType.class, typeId);

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(wasNew -> {
            Category category = findForTenant(Category.class, ctx.get(BasicType.CATEGORY.getName()).asString());
            assertNotNew(category);
            type.getCategory().setValue(category);
        }).withAfterSaveURI("/units/types").saveEntity(type);

        if (!requestHandled) {
            validate(type);
            ctx.respondWith()
               .template("/templates/organization/units/type.html.pasta", type, queryCategories().queryList());
        }
    }
}
