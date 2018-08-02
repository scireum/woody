/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import woody.core.relations.RelationHelper;
import woody.organization.BasicElement;
import woody.organization.categories.Category;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class ThingController extends BizController {

    @Part
    private RelationHelper relations;

    @Routed(value = "/things/:1", priority = 101)
    @LoginRequired
    public void things(WebContext ctx, String categoryName) {
        Category category = oma.select(Category.class)
                               .eq(Category.TENANT, tenants.getRequiredTenant())
                               .eq(Category.TECHNICAL_NAME, categoryName)
                               .eq(Category.TYPE, ThingCategoryTypeProvider.TYPE_NAME)
                               .queryFirst();
        SmartQuery<Thing> query = oma.select(Thing.class)
                                     .fields(Thing.ID, Thing.NAME, Thing.CODE, Thing.TYPE.join(ThingType.NAME))
                                     .orderAsc(Thing.CODE)
                                     .orderAsc(Thing.NAME);
        SQLPageHelper<Thing> ph = SQLPageHelper.withQuery(tenants.forCurrentTenant(query));
        ph.withContext(ctx).withSearchFields(QueryField.contains(Thing.NAME), QueryField.contains(Thing.CODE));

        ph.addQueryFacet(BasicElement.TYPE.getName(),
                         NLS.get("BasicElement.type"),
                         qry -> queryTypes(category).asSQLQuery());

        ctx.respondWith()
           .template("/templates/organization/things/things.html.pasta",
                     ph.asPage(),
                     category,
                     queryTypes(category).queryList());
    }

    protected SmartQuery<ThingType> queryTypes(Category category) {
        return oma.select(ThingType.class)
                  .fields(ThingType.ID, ThingType.NAME)
                  .eq(ThingType.TENANT, tenants.getRequiredTenant())
                  .eq(ThingType.CATEGORY, category)
                  .orderAsc(ThingType.NAME);
    }

    @LoginRequired
    @Routed("/thing/:1/:2/delete")
    public void deleteThing(WebContext ctx, String category, String id) {
        Optional<Thing> thing = tryFindForTenant(Thing.class, id);
        if (thing.isPresent()) {
            oma.delete(thing.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/things/" + category);
    }

    @LoginRequired
    @Routed("/thing/:1")
    public void thing(WebContext ctx, String id) {
        Thing thing = findForTenant(Thing.class, id);

        if (thing.isNew()) {
            editThing(ctx, id);
        } else {
            ctx.respondWith().template("/templates/organization/things/thing-overview.html.pasta", thing);
        }
    }

    @LoginRequired
    @Routed("/thing/:1/edit")
    public void editThing(WebContext ctx, String id) {
        Thing thing = findForTenant(Thing.class, id);

        if (thing.isNew()) {
            ThingType type = findForTenant(ThingType.class, ctx.get(Thing.TYPE.getName()).asString());
            assertNotNew(type);
            thing.getType().setValue(type);
        }

        boolean requestHandled = prepareSave(ctx).withAfterSaveURI("/thing/${id}").withPostSaveHandler(isNew -> {
            thing.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
        }).saveEntity(thing);

        if (!requestHandled) {
            validate(thing);
            ctx.respondWith()
               .template("/templates/organization/things/thing-details.html.pasta",
                         thing,
                         queryTypes(thing.getType().getValue().getCategory().getValue()).queryList());
        }
    }
}
