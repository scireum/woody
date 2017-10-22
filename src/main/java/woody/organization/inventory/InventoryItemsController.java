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
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import woody.core.relations.RelationHelper;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class InventoryItemsController extends BizController {

    private static final String PERMISSION_MANAGE_INVENTORY_ITEMS = "permission-manage-inventory-items";

    @Part
    private RelationHelper relations;

    @Routed("/inventory/items")
    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_ITEMS)
    public void items(WebContext ctx) {
        PageHelper<InventoryItem> ph = PageHelper.withQuery(oma.select(InventoryItem.class)
                                                               .fields(InventoryItem.ID,
                                                                       InventoryItem.NAME,
                                                                       InventoryItem.CODE,
                                                                       InventoryItem.TYPE.join(InventoryType.NAME))
                                                               .orderAsc(InventoryItem.CODE)
                                                               .orderAsc(InventoryItem.NAME)).forCurrentTenant();
        ph.withContext(ctx).withSearchFields(InventoryItem.NAME, InventoryItem.CODE).enableAdvancedSearch();
        ctx.respondWith().template("/templates/organization/inventory/items.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_ITEMS)
    @Routed("/inventory/item/:1/delete")
    public void deleteItem(WebContext ctx, String id) {
        Optional<InventoryItem> cl = tryFindForTenant(InventoryItem.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        items(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_ITEMS)
    @Routed("/inventory/item/:1")
    public void editItem(WebContext ctx, String itemId) {
        InventoryItem item = findForTenant(InventoryItem.class, itemId);

        boolean requestHandled =
                prepareSave(ctx).withAfterCreateURI("/inventory/item/${id}").withPreSaveHandler((isNew) -> {
                    if (isNew) {
                        load(ctx, item, InventoryItem.TYPE);
                    }
                }).withPostSaveHandler((isNew) -> {
                    item.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                }).saveEntity(item);

        if (!requestHandled) {
            validate(item);
            ctx.respondWith()
               .template("/templates/organization/inventory/item.html.pasta",
                         item,
                         oma.select(InventoryType.class)
                            .eq(InventoryType.TENANT, currentTenant())
                            .orderAsc(InventoryType.NAME)
                            .queryList());
        }
    }
}
