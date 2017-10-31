/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.projects;

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

@Register(classes = {Controller.class, ProjectTypesController.class})
public class ProjectTypesController extends BizController {

    private static final String PERMISSION_MANAGE_PROJECT_TYPES = "permission-manage-project-types";

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_PROJECT_TYPES)
    @Routed("/projects/types")
    public void projectsTypes(WebContext ctx) {
        PageHelper<ProjectType> ph = PageHelper.withQuery(oma.select(ProjectType.class).orderAsc(ProjectType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(ProjectType.NAME).forCurrentTenant();

        ctx.respondWith().template("/templates/organization/projects/types.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_PROJECT_TYPES)
    @Routed("/projects/type/:1/delete")
    public void deleteType(WebContext ctx, String id) {
        Optional<ProjectType> projectType = tryFindForTenant(ProjectType.class, id);
        if (projectType.isPresent()) {
            oma.delete(projectType.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/projects/types");
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_PROJECT_TYPES)
    @Routed("/projects/type/:1")
    public void unitType(WebContext ctx, String typeId) {
        ProjectType type = findForTenant(ProjectType.class, typeId);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = type.isNew();
                load(ctx, type);
                oma.update(type);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectToGet("/projects/type/" + type.getId());
                    return;
                }
            } catch (Exception e) {
                UserContext.handle(e);
            }
        }

        ctx.respondWith().template("/templates/organization/projects/type.html.pasta", type, this);
    }
}
