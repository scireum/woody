/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import com.google.common.base.Strings;
import sirius.biz.sequences.Sequences;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.controller.AutocompleteHelper;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import woody.organization.BasicElement;
import woody.organization.categories.Category;
import woody.organization.efforts.EffortType;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class UnitController extends BizController {

    private static final String PERMISSION_MANAGE_UNITS = "permission-manage-units";

    @Part
    private Sequences sequences;

    @Routed(value = "/units/:1", priority = 101)
    @LoginRequired
    public void units(WebContext ctx, String categoryName) {
        Category category = resolveCategory(categoryName);
        SmartQuery<Unit> query = oma.select(Unit.class)
                                    .eq(Unit.TYPE.join(EffortType.CATEGORY), category)
                                    .fields(Unit.ID,
                                            Unit.NAME,
                                            Unit.CODE,
                                            Unit.TYPE.join(UnitType.NAME),
                                            Unit.PARENT.join(Unit.NAME),
                                            Unit.PARENT.join(Unit.TYPE).join(UnitType.NAME))
                                    .orderAsc(Unit.CODE)
                                    .orderAsc(Unit.NAME);
        SQLPageHelper<Unit> ph = SQLPageHelper.withQuery(tenants.forCurrentTenant(query));
        ph.withContext(ctx).withSearchFields(QueryField.contains(Unit.NAME), QueryField.contains(Unit.CODE));

        ph.addQueryFacet(BasicElement.TYPE.getName(),
                         NLS.get("BasicElement.type"),
                         qry -> queryTypes(category).asSQLQuery());

        ctx.respondWith()
           .template("/templates/organization/units/units.html.pasta",
                     ph.asPage(),
                     category,
                     queryTypes(category).queryList());
    }

    protected Category resolveCategory(String categoryName) {
        return oma.select(Category.class)
                  .eq(Category.TENANT, tenants.getRequiredTenant())
                  .eq(Category.TECHNICAL_NAME, categoryName)
                  .eq(Category.TYPE, UnitCategoryTypeProvider.TYPE_NAME)
                  .queryFirst();
    }

    protected SmartQuery<UnitType> queryTypes(Category category) {
        return oma.select(UnitType.class)
                  .fields(UnitType.ID, UnitType.NAME)
                  .eq(UnitType.TENANT, tenants.getRequiredTenant())
                  .eq(UnitType.CATEGORY, category)
                  .orderAsc(UnitType.NAME);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed("/units/:1/autocomplete")
    public void autocomplete(WebContext ctx, String categoryId) {
        Category category = findForTenant(Category.class, categoryId);
        AutocompleteHelper.handle(ctx,
                                  (query, result) -> oma.select(Unit.class)
                                                        .eq(Unit.TENANT, tenants.getRequiredTenant())
                                                        .eq(Unit.TYPE.join(UnitType.CATEGORY), category)
                                                        .queryString(query,
                                                                     QueryField.contains(Unit.CODE),
                                                                     QueryField.contains(Unit.NAME),
                                                                     QueryField.contains(Unit.TYPE.join(UnitType.NAME)))
                                                        .orderAsc(Unit.NAME)
                                                        .iterateAll(unit -> result.accept(new AutocompleteHelper.Completion(
                                                                unit.getIdAsString(),
                                                                unit.getName(),
                                                                null))));
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed("/units/:1/:2/delete")
    public void deleteUnit(WebContext ctx, String category, String id) {
        Optional<Unit> cl = tryFindForTenant(Unit.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/units/" + category);
    }

    @LoginRequired
    @Routed("/unit/:1")
    public void unit(WebContext ctx, String id) {
        Unit unit = findForTenant(Unit.class, id);

        if (unit.isNew()) {
            editUnit(ctx, id);
        } else {
            ctx.respondWith().template("/templates/organization/units/unit-overview.html.pasta", unit);
        }
    }

    @LoginRequired
    @Routed("/unit/:1/edit")
    public void editUnit(WebContext ctx, String id) {
        Unit unit = findForTenant(Unit.class, id);

        if (unit.isNew()) {
            UnitType type = findForTenant(UnitType.class, ctx.get(Unit.TYPE.getName()).asString());
            assertNotNew(type);
            unit.getType().setValue(type);
        }

        boolean requestHandled = prepareSave(ctx).withAfterSaveURI("/unit/${id}").withPreSaveHandler(isNew -> {
            if (isNew) {
                Unit parent = findForTenant(Unit.class, ctx.get(Unit.PARENT.getName()).asString());
                if (parent != null) {
                    assertNotNew(parent);
                }
                unit.getParent().setValue(parent);
            }
            computeUniquePath(unit);
        }).withPostSaveHandler(isNew -> {
            unit.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
        }).saveEntity(unit);

        if (!requestHandled) {
            validate(unit);
            ctx.respondWith()
               .template("/templates/organization/units/unit-details.html.pasta",
                         unit,
                         queryTypes(unit.getType().getValue().getCategory().getValue()).queryList());
        }
    }

    protected void computeUniquePath(Unit unit) {
        if (unit.getParent().isFilled()) {
            unit.setUniquePath(unit.getParent().getValue().getUniquePath() + encode(sequences.generateId(
                    "units-counter-" + tenants.getRequiredTenant().getIdAsString())));
        } else {
            unit.setUniquePath(encode(unit.getTenant().getId()));
        }
    }

    private String encode(long id) {
        String code = Strings.padStart(Long.toString(id, 36), 3, '0');
        if (code.length() != 3) {
            throw Exceptions.createHandled().withNLSKey("Unit.cannotGenerateCode").handle();
        }

        return code;
    }
}
