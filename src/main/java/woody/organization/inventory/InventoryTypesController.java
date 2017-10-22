/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.inventory;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import java.util.Optional;

@Register(classes = {Controller.class, InventoryTypesController.class})
public class InventoryTypesController extends BizController {

    private static final String PERMISSION_MANAGE_INVENTORY_TYPES = "permission-manage-inventory-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_TYPES)
    @Routed("/inventory/types")
    public void inventoryTypes(WebContext ctx) {
        PageHelper<InventoryType> ph = PageHelper.withQuery(oma.select(InventoryType.class).orderAsc(InventoryType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(InventoryType.NAME).forCurrentTenant();

        ctx.respondWith().template("/templates/organization/inventory/types.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_TYPES)
    @Routed("/inventory/type/:1/delete")
    public void deleteRelationType(WebContext ctx, String id) {
        Optional<InventoryType> type = tryFindForTenant(InventoryType.class, id);
        if (type.isPresent()) {
            oma.delete(type.get());
            showDeletedMessage();
        }
        inventoryTypes(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_TYPES)
    @Routed("/inventory/type/:1")
    public void inventoryType(WebContext ctx, String typeId) {
        InventoryType type = findForTenant(InventoryType.class, typeId);
        //TODO in findForTenant
        type.getTenant().setValue(currentTenant());
        if (ctx.isPOST()) {
            try {
                boolean wasNew = type.isNew();
                load(ctx, type);
                oma.update(type);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectToGet("/inventory/type/" + type.getId());
                    return;
                }
            } catch (Exception e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("/templates/organization/inventory/type.html.pasta", type, this);
    }
}
