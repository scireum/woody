/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import woody.organization.BasicElement;
import woody.organization.categories.Category;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class EffortController extends BizController {

    @Routed(value = "/efforts/:1", priority = 101)
    @LoginRequired
    public void efforts(WebContext ctx, String categoryName) {
        Category category = oma.select(Category.class)
                               .eq(Category.TENANT, currentTenant())
                               .eq(Category.TECHNICAL_NAME, categoryName)
                               .eq(Category.TYPE, EffortCategoryTypeProvider.TYPE_NAME)
                               .queryFirst();
        PageHelper<Effort> ph = PageHelper.withQuery(oma.select(Effort.class)
                                                       .eq(Effort.TYPE.join(EffortType.CATEGORY), category)
                                                       .fields(Effort.ID,
                                                               Effort.NAME,
                                                               Effort.CODE,
                                                               Effort.TYPE.join(EffortType.NAME))
                                                       .orderAsc(Effort.CODE)
                                                       .orderAsc(Effort.NAME)).forCurrentTenant();
        ph.withContext(ctx).withSearchFields(Effort.NAME, Effort.CODE).enableAdvancedSearch();

        ph.addQueryFacet(BasicElement.TYPE.getName(),
                         NLS.get("BasicElement.type"),
                         qry -> queryTypes(category).asSQLQuery());

        ctx.respondWith()
           .template("/templates/organization/efforts/efforts.html.pasta",
                     ph.asPage(),
                     category,
                     queryTypes(category).queryList());
    }

    protected SmartQuery<EffortType> queryTypes(Category category) {
        return oma.select(EffortType.class)
                  .fields(EffortType.ID, EffortType.NAME)
                  .eq(EffortType.TENANT, currentTenant())
                  .eq(EffortType.CATEGORY, category)
                  .orderAsc(EffortType.NAME);
    }

    @LoginRequired
    @Routed("/efforts/:1/:2/delete")
    public void deleteEffort(WebContext ctx, String category, String id) {
        Optional<Effort> effort = tryFindForTenant(Effort.class, id);
        if (effort.isPresent()) {
            oma.delete(effort.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/efforts/" + category);
    }

    @LoginRequired
    @Routed("/effort/:1")
    public void effort(WebContext ctx, String id) {
        Effort effort = findForTenant(Effort.class, id);

        if (effort.isNew()) {
            editEffort(ctx, id);
        } else {
            ctx.respondWith().template("/templates/organization/efforts/effort-overview.html.pasta", effort);
        }
    }

    @LoginRequired
    @Routed("/effort/:1/edit")
    public void editEffort(WebContext ctx, String id) {
        Effort effort = findForTenant(Effort.class, id);

        if (effort.isNew()) {
            EffortType type = findForTenant(EffortType.class, ctx.get(Effort.TYPE.getName()).asString());
            assertNotNew(type);
            effort.getType().setValue(type);
        }

        boolean requestHandled = prepareSave(ctx).withAfterCreateURI("/effort/${id}").withPostSaveHandler(isNew -> {
            effort.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
        }).saveEntity(effort);

        if (!requestHandled) {
            validate(effort);
            ctx.respondWith()
               .template("/templates/organization/efforts/effort-details.html.pasta",
                         effort,
                         queryTypes(effort.getType().getValue().getCategory().getValue()).queryList());
        }
    }
}
