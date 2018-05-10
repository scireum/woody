/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

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

/**
 * Created by aha on 18.08.15.
 */
@Register(classes = Controller.class)
public class TasksController extends BizController {

    private static final String MANAGE_TASKS = "permission-manage-tasks";

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/tasks")
    public void tasks(WebContext ctx) {
        PageHelper<Task> pageHelper = PageHelper.withQuery(oma.select(Task.class).orderAsc(Task.ID)).forCurrentTenant();
        pageHelper.withContext(ctx);

        pageHelper.withSearchFields(Task.ASSIGNEE, Task.TAGS);
        ctx.respondWith().template("templates/tasks/tasks.html.pasta", pageHelper.asPage());
    }

//    @LoginRequired
//    @Permission(MANAGE_TASKS)
//    @Routed(value = "/tasks/suggest", jsonCall = true)
//    public void companiesSuggest(WebContext ctx, JSONStructuredOutput out) {
//        MagicSearch.generateSuggestions(ctx, (q, c) -> Tagged.computeSuggestions(Task.class, q, c));
//    }

    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/tasks/:1/delete")
    public void deleteTasks(WebContext ctx, String id) {
        Optional<Task> cl = tryFindForTenant(Task.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        tasks(ctx);
    }

    @LoginRequired
    @Permission(MANAGE_TASKS)
    @Routed("/task/:1")
    public void task(WebContext ctx, String taskId) {
        Task tasks = findForTenant(Task.class, taskId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = tasks.isNew();
                if (tasks.isNew()) {
                    tasks.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, tasks);
                oma.update(tasks);
                tasks.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily( "/task/" + tasks.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/tasks/task.html", tasks);
    }
}
