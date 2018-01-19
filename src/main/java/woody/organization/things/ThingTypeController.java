/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

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

@Register(classes = {Controller.class, ThingTypeController.class})
public class ThingTypeController extends BizController {

    private static final String PERMISSION_MANAGE_THING_TYPES = "permission-manage-thing-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_THING_TYPES)
    @Routed("/things/types")
    public void types(WebContext ctx) {
        PageHelper<ThingType> ph = PageHelper.withQuery(oma.select(ThingType.class)
                                                           .fields(ThingType.ID,
                                                                   ThingType.NAME,
                                                                   ThingType.DESCRIPTION,
                                                                   ThingType.CATEGORY.join(Category.ID),
                                                                   ThingType.CATEGORY.join(Category.NAME),
                                                                   ThingType.COLOR.inner(ColorData.COLOR)
                                                                                  .join(ColorDefinition.NAME),
                                                                   ThingType.COLOR.inner(ColorData.COLOR)
                                                                                  .join(ColorDefinition.HEX_CODE))
                                                           .orderAsc(ThingType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(ThingType.NAME, ThingType.DESCRIPTION).forCurrentTenant();

        ph.addQueryFacet(BasicType.CATEGORY.getName(),
                         NLS.get("BasicType.category"),
                         qry -> queryCategories().asSQLQuery());

        ctx.respondWith().template("/templates/organization/things/types.html.pasta", ph.asPage());
    }

    protected SmartQuery<Category> queryCategories() {
        return oma.select(Category.class)
                  .fields(Category.ID, Category.NAME)
                  .eq(Category.TENANT, currentTenant())
                  .eq(Category.TYPE, ThingCategoryTypeProvider.TYPE_NAME)
                  .orderAsc(Category.NAME);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_THING_TYPES)
    @Routed("/things/type/:1/delete")
    public void deleteType(WebContext ctx, String id) {
        Optional<ThingType> projectType = tryFindForTenant(ThingType.class, id);
        if (projectType.isPresent()) {
            oma.delete(projectType.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/things/types");
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_THING_TYPES)
    @Routed("/things/type/:1")
    public void type(WebContext ctx, String typeId) {
        ThingType type = findForTenant(ThingType.class, typeId);

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(wasNew -> {
            Category category = findForTenant(Category.class, ctx.get(BasicType.CATEGORY.getName()).asString());
            assertNotNew(category);
            type.getCategory().setValue(category);
        }).withAfterSaveURI("/things/types").saveEntity(type);

        if (!requestHandled) {
            validate(type);
            ctx.respondWith()
               .template("/templates/organization/things/type.html.pasta", type, queryCategories().queryList());
        }
    }
}
