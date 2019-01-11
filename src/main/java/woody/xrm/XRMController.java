/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
import sirius.biz.model.PersonData;

import sirius.biz.sequences.Sequences;
import sirius.biz.tenants.UserAccount;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.ScopeInfo;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import sirius.web.security.UserSettings;
import woody.core.relations.RelationHelper;


import woody.core.employees.Employee;
import woody.core.tags.Tagged;
import woody.migration.MigrationJob;
import woody.offers.ServiceAccountingService;
import woody.phoneCalls.Starface;
import woody.sales.AccountingService;
import woody.sales.Lineitem;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Optional;
import java.util.Set;

/**
 * Created by aha on 11.05.15.
 */
@Framework("companies")
@Register(classes = Controller.class)
public class XRMController extends BizController {

    public static final String PERMISSION_MANAGE_XRM = "permission-manage-xrm";

    @Part
    private static RelationHelper relations;

    @Part
    private static AccountingService as;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static MigrationJob mgj;


    @DefaultRoute
    @Routed("/")
    public void main(WebContext ctx) {
        ctx.respondWith().template("view/main/main.html.pasta");
    }

    @LoginRequired
    // ToDo permission-manage-xrm kommt nicht an, obwohl das Recht über die Rolle da sein sollte.
//    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/migrateCrmToWoody")
    public void migrateCrmToWoody(WebContext ctx) {

        UserInfo usi = UserContext.getCurrentUser();
        Set<String> permissions = usi.getPermissions();

        try {
            mgj.migrateCrmToWdody();
            ctx.respondWith().template("view/main/main.html.pasta");

        } catch (Exception e) {
            e.printStackTrace();
//            Exceptions.handle();
        }
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/exportAllLicenceLineitems")
    public void exportAllLicenceLineitems(WebContext ctx) {

        try {
            // ToDo wieder implementieren
//            as.exportLineitems(Lineitem.LINEITEMTYPE_LA,300,null);
            ctx.respondWith().template("view/main/main.html.pasta");

        } catch (Exception e) {
            Exceptions.handle();
        }
    }


    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/companies")
    public void companies(WebContext ctx) {
        PageHelper<Company> ph =
                PageHelper.withQuery(oma.select(Company.class).orderAsc(Company.NAME)).forCurrentTenant();
        ph.withSearchFields(Company.NAME,
                            Company.ADDRESS.inner(AddressData.CITY),
                            Company.CUSTOMER_NUMBER,
                            Company.MATCHCODE);

        ph.withContext(ctx);
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/xrm/companies.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/delete")
    public void deleteCompany(WebContext ctx, String id) {
        Optional<Company> cl = tryFindForTenant(Company.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        companies(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1")
    public void company(WebContext ctx, String companyId) {
        Company cl = findForTenant(Company.class, companyId);
        if (cl.isNew()) {
            ctx.respondWith().template("templates/xrm/company-details.html.pasta", cl);
        } else {
            ctx.respondWith().template("templates/xrm/company-overview.html.pasta", cl);
        }
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/edit")
    public void editCompany(WebContext ctx, String companyId) {
        Company cl = findForTenant(Company.class, companyId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = cl.isNew();
                if (cl.isNew()) {
                    cl.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, cl);
                oma.update(cl);
                cl.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily("/company/" + cl.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }

        ctx.respondWith().template("templates/xrm/company-details.html.pasta", cl);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/persons")
    public void companyPersons(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        SmartQuery<Person> query = oma.select(Person.class)
                                      .eq(Person.COMPANY, company)
                                      .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                                      .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME));
        PageHelper<Person> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ph.withSearchFields(Person.PERSON.inner(PersonData.FIRSTNAME), Person.PERSON.inner(PersonData.LASTNAME));
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/xrm/company-persons.html.pasta", company, ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/persons")
    public void persons(WebContext ctx) {
        SmartQuery<Person> query = oma.select(Person.class)
                                      .fields(Person.ID,
                                              Person.PERSON.inner(PersonData.SALUTATION),
                                              Person.PERSON.inner(PersonData.TITLE),
                                              Person.PERSON.inner(PersonData.FIRSTNAME),
                                              Person.PERSON.inner(PersonData.LASTNAME),
                                              Person.COMPANY.join(Company.ID),
                                              Person.COMPANY.join(Company.CUSTOMER_NUMBER),
                                              Person.COMPANY.join(Company.NAME))
                                      .eq(Person.COMPANY.join(Company.TENANT), currentTenant())
                                      .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                                      .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME));
        PageHelper<Person> ph = PageHelper.withQuery(query);
        ph.withSearchFields(Person.PERSON.inner(PersonData.TITLE),
                            Person.PERSON.inner(PersonData.FIRSTNAME),
                            Person.PERSON.inner(PersonData.LASTNAME),
                            Person.COMPANY.join(Company.ID),
                            Person.COMPANY.join(Company.CUSTOMER_NUMBER),
                            Person.COMPANY.join(Company.NAME));
        ph.withContext(ctx);
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/xrm/persons.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/person/:2/delete")
    public void deletePerson(WebContext ctx, String companyId, String personId) {//TODO ???
        Optional<Person> person = tryFind(Person.class, personId);
        if (person.isPresent()) {
            assertTenant(person.get().getCompany().getValue());
            oma.delete(person.get());
            showDeletedMessage();
        }
        companyPersons(ctx, companyId);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/person/:2")
    public void person(WebContext ctx, String companyId, String personId) {
        Company company = findForTenant(Company.class, companyId);
        Person person = find(Person.class, personId);
        assertNotNew(company);
        setOrVerify(person, person.getCompany(), company);

        ctx.respondWith().template("templates/xrm/person-overview.html.pasta", company, person);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/person/:2/edit")
    public void editPerson(WebContext ctx, String companyId, String personId) {
        Company company = findForTenant(Company.class, companyId);
        Person person = find(Person.class, personId);
        assertNotNew(company);
        setOrVerify(person, person.getCompany(), company);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = person.isNew();
                load(ctx, person);
                oma.update(person);
                person.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily("/company/" + company.getId() + "/person/" + person.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("templates/xrm/person-details.html.pasta", company, person);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/person/:1/css")
    public void personCSS(WebContext ctx, String personId) {
        Person person = find(Person.class, personId);
        assertNotNew(person);
        assertTenant(person.getCompany().getValue());
        if (ctx.isPOST()) {
            try {
                load(ctx, person);
                oma.update(person);
                showSavedMessage();
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("templates/xrm/person-css.html.pasta", person.getCompany().getValue(), person);
    }
}
