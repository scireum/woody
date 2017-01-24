/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
import sirius.biz.model.ContactData;
import sirius.biz.model.PersonData;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.commons.DataCollector;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.controller.AutocompleteHelper;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.services.JSONStructuredOutput;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;
import woody.sales.contracts.AccountingService;
import woody.sales.contracts.Contract;
import woody.sales.contracts.Lineitem;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("companies")
@Register(classes = Controller.class)
public class XRMController extends BizController {

    public static final String PERMISSION_MANAGE_XRM = "permission-manage-xrm";

    @Part
    private AccountingService asb;

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/exportLineitems")
    public void exportLineitems(WebContext ctx) {

        try {
            asb.exportLicenceLineitems(1000, null);
            int vvv = 1;
        } catch (Exception e) {
            Exceptions.handle();
        }
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/licenceAccounting")
    public void licenceAccounting(WebContext ctx) {
        LocalDate referenceDate = LocalDate.of(2017, 1, 2);
        boolean dryRun = false;
        boolean foreignCountry = false;
        DataCollector<Lineitem> lineitemCollection = asb.accountAllContracts(dryRun, referenceDate, null,
                                                         /*TaskMonitor monitor,*/ foreignCountry);
        int vvv = 1;
    }

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/companies")
    public void companies(WebContext ctx) {
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Company> query = oma.select(Company.class).orderAsc(Company.NAME);
        search.applyQueries(query,
                            Company.NAME,
                            Company.ADDRESS.inner(AddressData.CITY),
                            Company.CUSTOMER_NUMBER,
                            Company.MATCHCODE);
        Tagged.applyTagSuggestions(Company.class, search, query);
        Relations.applySuggestions(Company.class, search, query);
        PageHelper<Company> ph = PageHelper.withQuery(query).forCurrentTenant();
        ph.withContext(ctx);
        ctx.respondWith().template("view/xrm/companies.html", ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed(value = "/companies/suggest", jsonCall = true)
    public void companiesSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> {
            Tagged.computeSuggestions(Company.class, q, c);
            Relations.computeSuggestions(Company.class, q, c);
        });
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/companies/autocomplete")
    public void autocomplete(WebContext ctx) {
        AutocompleteHelper.handle(ctx,
                                  (query, result) -> oma.select(Company.class)
                                                        .eq(Company.TENANT, currentTenant())
                                                        .where(Like.allWordsInAnyField(query,
                                                                                       Company.NAME,
                                                                                       Company.NAME2,
                                                                                       Company.CUSTOMER_NUMBER,
                                                                                       Company.MATCHCODE))
                                                        .orderAsc(Company.NAME)
                                                        .iterateAll(company -> result.accept(new AutocompleteHelper.Completion(
                                                                company.getIdAsString(),
                                                                company.getName(),
                                                                null))));
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
            ctx.respondWith().template("view/xrm/company-details.html", cl);
        } else {
            ctx.respondWith().template("view/xrm/company-overview.html", cl);
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
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/company/" + cl.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/xrm/company-details.html", cl);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed(value = "/persons/suggest", jsonCall = true)
    public void personsSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> Tagged.computeSuggestions(Person.class, q, c));
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/persons")
    public void companyPersons(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Person> query = oma.select(Person.class)
                                      .eq(Person.COMPANY, company)
                                      .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                                      .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME));
        search.applyQueries(query,
                            Person.PERSON.inner(PersonData.LASTNAME),
                            Person.PERSON.inner(PersonData.FIRSTNAME),
                            Person.CONTACT.inner(ContactData.PHONE),
                            Person.CONTACT.inner(ContactData.EMAIL),
                            Person.CONTACT.inner(ContactData.MOBILE));
        Tagged.applyTagSuggestions(Person.class, search, query);
        PageHelper<Person> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/xrm/company-persons.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/contracts")
    public void companyContracts(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Contract> query = oma.select(Contract.class)
                                        .eq(Contract.COMPANY, company)
                                        .orderAsc(Contract.ACCOUNTINGGROUP)
                                        .orderAsc(Contract.STARTDATE);

        Tagged.applyTagSuggestions(Contract.class, search, query);
        PageHelper<Contract> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/sales/company-contracts.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/persons")
    public void persons(WebContext ctx) {
        // ToDo: Wofür ist dieser Code?
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
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
        search.applyQueries(query,
                            Person.PERSON.inner(PersonData.LASTNAME),
                            Person.PERSON.inner(PersonData.FIRSTNAME),
                            Person.COMPANY.join(Company.NAME),
                            Person.CONTACT.inner(ContactData.PHONE),
                            Person.CONTACT.inner(ContactData.EMAIL),
                            Person.CONTACT.inner(ContactData.MOBILE));

        Tagged.applyTagSuggestions(Person.class, search, query);
        PageHelper<Person> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith().template("view/xrm/persons.html", ph.asPage(), search.getSuggestionsString());
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

        ctx.respondWith().template("view/xrm/person-overview.html", company, person);
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
                    ctx.respondWith()
                       .redirectTemporarily(WebContext.getContextPrefix()
                                            + "/company/"
                                            + company.getId()
                                            + "/person/"
                                            + person.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/xrm/person-details.html", company, person);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/person/:2/css")
    public void personCSS(WebContext ctx, String companyId, String personId) {
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
        ctx.respondWith().template("view/xrm/person-css.html", person.getCompany().getValue(), person);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/contract/:2/delete")
    public void deleteContract(WebContext ctx, String companyId, String contractId) {
        Optional<Contract> contract = tryFind(Contract.class, contractId);
        if (contract.isPresent()) {
            assertTenant(contract.get().getCompany().getValue());
            oma.delete(contract.get());
            showDeletedMessage();
        }
        companyContracts(ctx, companyId);
    }
}
