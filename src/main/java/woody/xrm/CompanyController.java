/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.codelists.CodeList;
import sirius.biz.codelists.CodeListEntry;
import sirius.biz.model.PersonData;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Priorized;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.mixing.Column;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import java.util.List;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("companies")
@Register(classes = Controller.class)
public class CompanyController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/companies")
    public void companies(WebContext ctx) {
        PageHelper<Company> ph =
                PageHelper.withQuery(oma.select(Company.class).orderAsc(Company.NAME)).forCurrentTenant();
        ph.withContext(ctx);
        ph.withSearchFields(Company.NAME, Company.CUSTOMERNR, Company.ID);
        ctx.respondWith().template("view/xrm/companies.html", ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1")
    public void company(WebContext ctx, String companyId) {
        Company cl = companyHandler(ctx, companyId, false);
        ctx.respondWith().template("view/xrm/company-details.html", cl);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/person/:1")
    public void person(WebContext ctx, String personId) {
        Person person = personHandler(ctx, personId, false);
        ctx.respondWith().template("view/xrm/person-details.html", person);
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/acc")
    public void companyAccounting(WebContext ctx, String companyId) {
        Company cl = companyHandler(ctx, companyId, false);
        ctx.respondWith().template("view/xrm/company-accounting.html", cl);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/persons")
    public void companyPersons(WebContext ctx, String companyId) {
        PageHelper<Person> ph =
                PageHelper.withQuery(oma.select(Person.class).eq(Person.COMPANY, companyId)
                                        .orderAsc(Column.named(Person.PERSON + "_" + PersonData.LASTNAME + ", " +
                                         Person.PERSON + "_" + PersonData.FIRSTNAME)));
        ph.withContext(ctx);
        ph.withSearchFields(PersonData.LASTNAME, Company.NAME);
        Optional oCompany = oma.find(Company.class, companyId);
        Company company = (Company) oCompany.get();
        currentCompany = company;
        ctx.respondWith().template("view/xrm/company-persons.html", ph.asPage());
    }

    private Company currentCompany;

    private Company companyHandler(WebContext ctx, String companyId, boolean forceDetails) {
        Company cl = findForTenant(Company.class, companyId);
        currentCompany = cl;
        if (ctx.isPOST()) {
            try {
                boolean wasNew = cl.isNew();
                if (cl.isNew()) {
                    cl.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, cl);
                oma.update(cl);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/company/" + cl.getId());
                    return cl;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        return cl;

    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/delete")
    public void deleteCompany(WebContext ctx, String id) {
        Optional<Company> cl = tryFindForTenant(Company.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        companies(ctx);
    }

    private Person personHandler(WebContext ctx, String personId, boolean forceDetails) {
        Person person = findForTenant(Person.class, personId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = person.isNew();
                if (person.isNew()) {
                    // do nothing
                } // ToDO load funktioniert bei dem Neu-Anlegen einer Person nicht richtig. Der username wird gespeichert, der lastname nicht
                load(ctx, person);
                if(person.getCompany().getValue() == null) {
                    person.getCompany().setValue(currentCompany);
                }
                oma.update(person);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/person/" + person.getId());
                    return person;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        return person;

    }
}
