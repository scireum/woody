/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.inventory;

import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.services.JSONStructuredOutput;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class InventoryItemsController extends BizController {

    private static final String PERMISSION_MANAGE_INVENTORY_ITEMS = "permission-manage-inventory-items";

    @Routed("/inventory/items")
    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_ITEMS)
    public void items(WebContext ctx) {
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<InventoryItem> query = oma.select(InventoryItem.class)
                                             .fields(InventoryItem.ID,
                                                     InventoryItem.NAME,
                                                     InventoryItem.CODE,
                                                     InventoryItem.TYPE.join(InventoryType.NAME))
                                             .orderAsc(InventoryItem.CODE)
                                             .orderAsc(InventoryItem.NAME);
        search.applyQueries(query, InventoryItem.NAME, InventoryItem.CODE, InventoryItem.TYPE.join(InventoryType.NAME));
        Tagged.applyTagSuggestions(InventoryItem.class, search, query);
        Relations.applySuggestions(InventoryItem.class, search, query);
        PageHelper<InventoryItem> ph = PageHelper.withQuery(query).forCurrentTenant();
        ph.withContext(ctx);
        ctx.respondWith().template("view/core/inventory/items.html", ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_INVENTORY_ITEMS)
    @Routed(value = "/inventory/items/suggest", jsonCall = true)
    public void itemsSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> {
            Tagged.computeSuggestions(InventoryItem.class, q, c);
            Relations.computeSuggestions(InventoryItem.class, q, c);
        });
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

        boolean requestHandled = prepareSave(ctx).editAfterCreate()
                                                 .withAfterCreateURI("/inventory/item/${id}")
                                                 .withAfterCreateURI("/inventory/items")
                                                 .withPreSaveHandler((isNew) -> {
                                                     if (isNew) {
                                                         load(ctx, item, InventoryItem.TYPE);
                                                     }
                                                 })
                                                 .withPostSaveHandler((isNew) -> {
                                                     item.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                                                 })
                                                 .saveEntity(item);

        if (!requestHandled) {
            validate(item);
            ctx.respondWith()
               .template("view/core/inventory/item.html",
                         item,
                         oma.select(InventoryType.class)
                            .eq(InventoryType.TENANT, currentTenant())
                            .orderAsc(InventoryType.NAME)
                            .queryList());
        }
    }
}
