/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
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
import woody.servers.Servers;

/**
 * Created by aha on 14.05.15.
 */
@Register(classes = Controller.class, framework = Servers.FRAMEWORK_SERVERS)
public class CompanyController extends BizController {

    public static final String PERMISSION_MANAGE_XRM = "permission-manage-xrm";

    @DefaultRoute
    @Routed("/companies")
    @LoginRequired
    @Permission(CompanyController.PERMISSION_MANAGE_XRM)
    public void companies(WebContext ctx) {
        PageHelper<Company> ph =
                PageHelper.withQuery(oma.select(Company.class).orderAsc(Company.ACCOUNT_NUMBER).orderAsc(Company.NAME))
                          .forCurrentTenant();
        ph.withContext(ctx);
        ph.withSearchFields(Company.NAME,
                            Company.ACCOUNT_NUMBER,
                            Company.ADDRESS.inner(AddressData.STREET),
                            Company.ADDRESS.inner(AddressData.CITY));
        ctx.respondWith().template("view/companies/companies.html", ph.asPage());
    }

    @Routed("/company/:1")
    @LoginRequired
    @Permission(CompanyController.PERMISSION_MANAGE_XRM)
    public void company(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = company.isNew();
                if (company.isNew()) {
                    company.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, company);
                oma.update(company);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily(WebContext.getContextPrefix() + "/company/" + company.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/companies/company.html", company);
    }
}
