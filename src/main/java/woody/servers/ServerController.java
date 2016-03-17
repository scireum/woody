/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.HandledException;
import sirius.web.controller.Controller;

import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

/**
 * Created by aha on 14.05.15.
 */
@Register(classes = Controller.class, framework = Servers.FRAMEWORK_SERVERS)
public class ServerController extends BizController {

    public static final String PERMISSION_MANAGE_SERVERS = "permission-manage-servers";

    @DefaultRoute //TODO Highly questionable
    @Routed("/user-account/:1/server-credentials")
    @LoginRequired
    @Permission(ServerController.PERMISSION_MANAGE_SERVERS)
    public void serverCredentials(WebContext ctx, String accountId) {
        UserAccount userAccount = find(UserAccount.class, accountId);
        assertTenant(userAccount);
        assertNotNew(userAccount);
        if (ctx.isPOST()) {
            try {
                load(ctx, userAccount);
                oma.update(userAccount);
                showSavedMessage();
            } catch (HandledException e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/servers/user-account-server-credentials.html", userAccount);
    }
}
