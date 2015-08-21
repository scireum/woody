/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.kernel.health.HandledException;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;

/**
 * Created by aha on 18.08.15.
 */
public class TasksController implements Controller {

    @Override
    public void onError(WebContext webContext, HandledException e) {

    }

    @Routed("/tasks")
    public void tasks(WebContext ctx) {

    }

    @Routed("/task/:1")
    public void task(WebContext ctx, String taskId) {

    }

    @Routed("/tasks/filter")
    public void filters(WebContext ctx) {

    }

    @Routed("/tasks/filter/:1")
    public void filter(WebContext ctx, String filterId) {

    }

}
