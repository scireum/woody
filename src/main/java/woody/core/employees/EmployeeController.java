/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import sirius.biz.tenants.UserAccount;
import sirius.biz.tenants.UserAccountController;
import sirius.biz.web.BizController;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class EmployeeController extends BizController {

    @Routed("/user-account/:1/employee")
    @LoginRequired
    @Permission(UserAccountController.PERMISSION_MANAGE_USER_ACCOUNTS)
    public void employee(WebContext ctx, String accountId) {
        UserAccount userAccount = find(UserAccount.class, accountId);
        assertTenant(userAccount);
        assertNotNew(userAccount);

        if (ctx.isPOST()) {
            try {
                load(ctx, userAccount);
                oma.update(userAccount);
                showSavedMessage();
            } catch (Exception e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith()
           .template("view/core/employee/user-account-employee.html",
                     userAccount);
    }
}
