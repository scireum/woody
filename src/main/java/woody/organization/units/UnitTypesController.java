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
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import java.util.Optional;

@Register(classes = {Controller.class, UnitTypesController.class})
public class UnitTypesController extends BizController {

    private static final String PERMISSION_MANAGE_UNIT_TYPES = "permission-manage-unit-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/types")
    public void unitTypes(WebContext ctx) {
        PageHelper<UnitType> ph = PageHelper.withQuery(oma.select(UnitType.class).orderAsc(UnitType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(UnitType.NAME).forCurrentTenant();

        ctx.respondWith().template("view/core/units/types.html", ph.asPage(), this);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/type/:1/delete")
    public void deleteRelationType(WebContext ctx, String id) {
        Optional<UnitType> unitType = tryFindForTenant(UnitType.class, id);
        if (unitType.isPresent()) {
            oma.delete(unitType.get());
            showDeletedMessage();
        }
        unitTypes(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNIT_TYPES)
    @Routed("/units/type/:1")
    public void unitType(WebContext ctx, String typeId) {
        UnitType type = findForTenant(UnitType.class, typeId);
        //TODO in findForTenant
        type.getTenant().setValue(currentTenant());
        if (ctx.isPOST()) {
            try {
                boolean wasNew = type.isNew();
                load(ctx, type);
                oma.update(type);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectToGet("/units/type/" + type.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/core/units/type.html", type, this);
    }
}
