/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.tenants.UserAccount;
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

/**
 * Provides methods to handle {@link Task tasks}.
 */
@Register(classes = Controller.class)
public class TasksController extends BizController {

    private static final String MANAGE_TASKS = "permission-manage-tasks";

    /**
     * Provides a default tasks overview.
     *
     * @param ctx the current request
     */
    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/tasks")
    public void tasks(WebContext ctx) {
        PageHelper<Task> pageHelper = PageHelper.withQuery(oma.select(Task.class).orderAsc(Task.ID)).forCurrentTenant();
        pageHelper.withContext(ctx);

        pageHelper.withSearchFields(Task.ASSIGNEE, Task.TITLE);
        ctx.respondWith().template("templates/tasks/tasks.html.pasta", pageHelper.asPage());
    }

    /**
     * Provides an edit view for a currently selected {@link Task}.
     *
     * @param ctx    the current request
     * @param taskId the id of the current {@link Task}
     */
    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/task/:1")
    public void task(WebContext ctx, String taskId) {
        Task task = findForTenant(Task.class, taskId);

        if (task.isNew()) {
            task.getReporter().setValue(UserContext.getCurrentUser().as(UserAccount.class));
            task.setState(TaskState.OPEN);
        }


        boolean requestHandled = prepareSave(ctx).withAfterCreateURI("/task/${id}")
                .withAfterSaveURI(ctx.getRequestedURI())
                .saveEntity(task);

        if (!requestHandled) {
            ctx.respondWith().template("templates/tasks/task.html.pasta", task);
        }
    }

    /**
     * Provides a route to delete a task
     *
     * @param ctx the current request
     * @param id  the id of the task to delete
     */
    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/tasks/:1/delete")
    public void deleteTasks(WebContext ctx, String id) {
        Task task = findForTenant(Task.class, id);
        oma.delete(task);
        showDeletedMessage();

        tasks(ctx);
    }
}
