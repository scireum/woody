/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.web.BizController;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.Permission;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Register(classes = Controller.class)
public class TypeColorAssignmentController extends BizController {

    @Part
    private Colors colors;

    @DefaultRoute
    @Permission(ColorDefinitionController.PERMISSION_MANAGE_COLORS)
    @Routed("/colors/assignments")
    public void assignments(WebContext ctx) {
        Map<String, TypeColorAssignment> assignments = oma.select(TypeColorAssignment.class)
                                                          .eq(TypeColorAssignment.TENANT, tenants.getRequiredTenant())
                                                          .queryList()
                                                          .stream()
                                                          .collect(Collectors.toMap(TypeColorAssignment::getType,
                                                                                    Function.identity()));

        ctx.respondWith().template("/templates/core/colors/assignments.html.pasta", assignments);
    }

    @Permission(ColorDefinitionController.PERMISSION_MANAGE_COLORS)
    @Routed("/colors/assignment/:1/delete")
    public void delete(WebContext ctx, String type) {
        Optional<TypeColorAssignment> assignment = oma.select(TypeColorAssignment.class)
                                                      .eq(TypeColorAssignment.TENANT, tenants.getRequiredTenant())
                                                      .eq(TypeColorAssignment.TYPE, type)
                                                      .first();
        if (assignment.isPresent()) {
            oma.delete(assignment.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/colors/assignments");
    }

    @Permission(ColorDefinitionController.PERMISSION_MANAGE_COLORS)
    @Routed("/colors/assignment/:1")
    public void assignment(WebContext ctx, String type) {
        TypeColorAssignment assignment = oma.select(TypeColorAssignment.class)
                                            .eq(TypeColorAssignment.TENANT, tenants.getRequiredTenant())
                                            .eq(TypeColorAssignment.TYPE, type)
                                            .queryFirst();
        if (assignment == null) {
            assignment = new TypeColorAssignment();
            assignment.getTenant().setValue(tenants.getRequiredTenant());
            assignment.setType(type);
        }

        boolean requestHandled = prepareSave(ctx).withAfterSaveURI("/colors/assignments").saveEntity(assignment);

        if (!requestHandled) {
            validate(assignment);
            ctx.respondWith().template("/templates/core/colors/assignment.html.pasta", assignment);
        }
    }
}
