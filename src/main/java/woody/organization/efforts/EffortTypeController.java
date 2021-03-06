/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
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

@Register(classes = {Controller.class, EffortTypeController.class})
public class EffortTypeController extends BizController {

    private static final String PERMISSION_MANAGE_EFFORT_TYPES = "permission-manage-effort-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_EFFORT_TYPES)
    @Routed("/efforts/types")
    public void types(WebContext ctx) {
        SmartQuery<EffortType> query = oma.select(EffortType.class)
                                          .fields(EffortType.ID,
                                                  EffortType.NAME,
                                                  EffortType.DESCRIPTION,
                                                  EffortType.CATEGORY.join(Category.ID),
                                                  EffortType.CATEGORY.join(Category.NAME),
                                                  EffortType.COLOR.inner(ColorData.COLOR).join(ColorDefinition.NAME),
                                                  EffortType.COLOR.inner(ColorData.COLOR)
                                                                  .join(ColorDefinition.HEX_CODE))
                                          .orderAsc(EffortType.NAME);
        SQLPageHelper<EffortType> ph = SQLPageHelper.withQuery(tenants.forCurrentTenant(query));
        ph.withContext(ctx);
        ph.withSearchFields(QueryField.contains(EffortType.NAME), QueryField.contains(EffortType.DESCRIPTION));

        ph.addQueryFacet(BasicType.CATEGORY.getName(),
                         NLS.get("BasicType.category"),
                         qry -> queryCategories().asSQLQuery());

        ctx.respondWith().template("/templates/organization/efforts/types.html.pasta", ph.asPage());
    }

    protected SmartQuery<Category> queryCategories() {
        return oma.select(Category.class)
                  .fields(Category.ID, Category.NAME)
                  .eq(Category.TENANT, tenants.getRequiredTenant())
                  .eq(Category.TYPE, EffortCategoryTypeProvider.TYPE_NAME)
                  .orderAsc(Category.NAME);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_EFFORT_TYPES)
    @Routed("/efforts/type/:1/delete")
    public void deleteType(WebContext ctx, String id) {
        Optional<EffortType> projectType = tryFindForTenant(EffortType.class, id);
        if (projectType.isPresent()) {
            oma.delete(projectType.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/efforts/types");
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_EFFORT_TYPES)
    @Routed("/efforts/type/:1")
    public void type(WebContext ctx, String typeId) {
        EffortType type = findForTenant(EffortType.class, typeId);

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(wasNew -> {
            Category category = findForTenant(Category.class, ctx.get(BasicType.CATEGORY.getName()).asString());
            assertNotNew(category);
            type.getCategory().setValue(category);
        }).withAfterSaveURI("/efforts/types").saveEntity(type);

        if (!requestHandled) {
            validate(type);
            ctx.respondWith()
               .template("/templates/organization/efforts/type.html.pasta", type, queryCategories().queryList());
        }
    }
}
