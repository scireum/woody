/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.Sirius;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.settings.Extension;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.Permission;

import java.util.Optional;

@Register(classes = Controller.class)
public class ColorDefinitionController extends BizController {

    public static final String PERMISSION_MANAGE_COLORS = "permission-manage-colors";

    @DefaultRoute
    @Permission(PERMISSION_MANAGE_COLORS)
    @Routed("/colors/definitions")
    public void colors(WebContext ctx) {
        SQLPageHelper<ColorDefinition> ph =
                SQLPageHelper.withQuery(tenants.forCurrentTenant(oma.select(ColorDefinition.class)
                                                                    .orderAsc(ColorDefinition.NAME)));
        ph.withContext(ctx);
        ph.withSearchFields(QueryField.contains(ColorDefinition.NAME),
                            QueryField.contains(ColorDefinition.HEX_CODE),
                            QueryField.contains(ColorDefinition.PALETTE));

        ctx.respondWith().template("/templates/core/colors/definitions.html.pasta", ph.asPage());
    }

    //TODO via standard import framework
    @Permission(PERMISSION_MANAGE_COLORS)
    @Routed("/colors/import")
    public void importPalette(WebContext ctx) {
        String palette = ctx.get("palette").asString();
        Extension colorPalette = Sirius.getSettings().getExtension("color-palettes", palette);
        if (colorPalette == null) {
            throw Exceptions.createHandled().withSystemErrorMessage("Unknown palette: %s", palette).handle();
        }

        colorPalette.getContext().forEach((name, hexCode) -> importColor(palette, name, hexCode.toString()));

        colors(ctx);
    }

    private void importColor(String palette, String name, String hexCode) {
        try {
            ColorDefinition color = oma.select(ColorDefinition.class)
                                       .eq(ColorDefinition.TENANT, tenants.getRequiredTenant())
                                       .eq(ColorDefinition.PALETTE, palette)
                                       .eq(ColorDefinition.NAME, name)
                                       .queryFirst();
            if (color == null) {
                color = new ColorDefinition();
                color.getTenant().setValue(tenants.getRequiredTenant());
                color.setName(name);
                color.setPalette(palette);
            }

            color.setHexCode(hexCode);
            oma.update(color);
        } catch (Exception e) {
            Exceptions.handle(e);
        }
    }

    @Permission(PERMISSION_MANAGE_COLORS)
    @Routed("/colors/definition/:1/delete")
    public void delete(WebContext ctx, String id) {
        Optional<ColorDefinition> colorDefinition = tryFindForTenant(ColorDefinition.class, id);
        if (colorDefinition.isPresent()) {
            oma.delete(colorDefinition.get());
            showDeletedMessage();
        }
        ctx.respondWith().redirectToGet("/colors/definitions");
    }

    @Permission(PERMISSION_MANAGE_COLORS)
    @Routed("/colors/definition/:1")
    public void color(WebContext ctx, String id) {
        ColorDefinition colorDefinition = findForTenant(ColorDefinition.class, id);

        boolean requestHandled = prepareSave(ctx).withAfterSaveURI("/colors/definitions").saveEntity(colorDefinition);

        if (!requestHandled) {
            validate(colorDefinition);
            ctx.respondWith().template("/templates/core/colors/definition.html.pasta", colorDefinition);
        }
    }
}
