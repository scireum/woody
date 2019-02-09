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

/**
 * Provides the editor UI for employee information embedded within the user account management.
 */
@Register(classes = Controller.class)
public class EmployeeController extends BizController {

    /**
     * Handles the page within the user account editor which is responsible for editing the employee data.
     *
     * @param ctx       the current request
     * @param accountId the id of the user account to edit
     */
    @Routed("/user-account/:1/employee")
    @LoginRequired
    @Permission(UserAccountController.PERMISSION_MANAGE_USER_ACCOUNTS)
    public void employee(WebContext ctx, String accountId) {
        UserAccount userAccount = findForTenant(UserAccount.class, accountId);

        prepareSave(ctx).saveEntity(userAccount);
        validate(userAccount);
        ctx.respondWith().template("/templates/core/employee/user-account-employee.html.pasta", userAccount);
    }
}
