/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.UserAccount;
import sirius.biz.tenants.UserAccountController;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.HandledException;
import sirius.kernel.health.Log;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.services.JSONStructuredOutput;
import woody.core.tags.Tagged;
import woody.xrm.Company;

import java.util.Optional;

/**
 * Created by aha on 14.05.15.
 */
@Register(classes = Controller.class, framework = ServerController.FRAMEWORK_SERVERS)
public class ServerController extends BizController {

    public static final String FRAMEWORK_SERVERS = "woody.servers";
    public static final String PERMISSION_MANAGE_SERVERS = "permission-manage-servers";

    public static final Log LOG = Log.get("servers");

    @Routed("/user-account/:1/server-credentials")
    @LoginRequired
    @Permission(UserAccountController.PERMISSION_MANAGE_USER_ACCOUNTS)
    @Permission(PERMISSION_MANAGE_SERVERS)
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

    @DefaultRoute
    @Routed("/servers")
    @LoginRequired
    @Permission(PERMISSION_MANAGE_SERVERS)
    public void servers(WebContext ctx) {
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Server> query = oma.select(Server.class)
                                      .fields(Server.ID,
                                              Server.STATE,
                                              Server.NAME,
                                              Server.URL,
                                              Server.CUSTOMER.join(Company.NAME))
                                      .orderAsc(Server.STATE)
                                      .orderAsc(Server.NAME);
        search.applyQueries(query, Server.NAME, Server.CUSTOMER.join(Company.NAME), Server.TOKEN, Server.URL);
        Tagged.applyTagSuggestions(Server.class, search, query);
        PageHelper<Server> ph = PageHelper.withQuery(query).forCurrentTenant();
        ph.withContext(ctx);
        ctx.respondWith().template("view/servers/servers.html", ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_SERVERS)
    @Routed(value = "/servers/suggest", jsonCall = true)
    public void serversSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> Tagged.computeSuggestions(Server.class, q, c));
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_SERVERS)
    @Routed("/server/:1/delete")
    public void deleteServer(WebContext ctx, String id) {
        Optional<Server> cl = tryFindForTenant(Server.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        servers(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_SERVERS)
    @Routed("/server/:1")
    public void server(WebContext ctx, String serverId) {
        Server server = findForTenant(Server.class, serverId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = server.isNew();
                if (server.isNew()) {
                    server.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, server);
                oma.update(server);
                server.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/server/" + server.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/servers/server.html", server);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_SERVERS)
    @Routed("/server/:1/postComment")
    public void postComment(WebContext ctx, String serverId) {
        Server server = findForTenant(Server.class, serverId);
        assertNotNew(server);
        server.getComments()
              .addComment(getUser().getUserObject(UserAccount.class).getPerson().toString(),
                          getUser().getUserId(),
                          ctx.get("comment").asString(),
                          ctx.get("publicVisible").asBoolean());

        ctx.respondWith().template("view/servers/server.html", server);
    }
}
