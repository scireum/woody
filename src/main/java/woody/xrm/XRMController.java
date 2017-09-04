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
import sirius.biz.sequences.Sequences;
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.commons.DataCollector;
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
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import sirius.web.services.JSONStructuredOutput;

import woody.core.employees.Employee;
import woody.core.tags.Tagged;
import woody.offers.ServiceAccountingService;
import woody.phoneCalls.Starface;
import woody.sales.AccountingService;
import woody.sales.Lineitem;
import woody.sales.Contract;
import woody.opportunities.Opportunity;

import java.security.Provider;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("companies")
@Register(classes = Controller.class)
public class XRMController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @Part
    private AccountingService asb;

    @Part
    private ServiceAccountingService sas;


    @DefaultRoute
    @Routed("/")
    public void main(WebContext ctx) {
        ctx.respondWith().template("view/main/main.html");
    }



    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/exportAllLicenceLineitems")
    public void exportAllLicenceLineitems(WebContext ctx) {

        try {
            asb.exportLineitems(Lineitem.LINEITEMTYPE_LA,300,null);
            ctx.respondWith().template("view/main/main.html");

        } catch (Exception e) {
            Exceptions.handle();
        }
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/exportAuslandLicenceLineitems")
    public void exportAuslandLicenceLineitems(WebContext ctx) {

        try {
            asb.exportLineitems(Lineitem.LINEITEMTYPE_LA, 300,"Ausland");
            ctx.respondWith().template("view/main/main.html");

        } catch (Exception e) {
            Exceptions.handle();
        }
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/exportAllServiceLineitems")
    public void exportAllServiceLineitems(WebContext ctx) {

        try {
            asb.exportLineitems(Lineitem.LINEITEMTYPE_OA,300,null);
            ctx.respondWith().template("view/main/main.html");

        } catch (Exception e) {
            Exceptions.handle();
        }
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/exportAuslandServiceLineitems")
    public void exportAuslandServiceLineitems(WebContext ctx) {

        try {
            asb.exportLineitems(Lineitem.LINEITEMTYPE_OA, 300,"Ausland");
            ctx.respondWith().template("view/main/main.html");

        } catch (Exception e) {
            Exceptions.handle();
        }
    }

    private boolean block = false;

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/licenceAccounting")
    public void licenceAccounting(WebContext ctx) {
        if(!block) {
            block = true;
            LocalDate referenceDate = LocalDate.of(2018, 1, 4);
            boolean dryRun = false;
            boolean foreignCountry = false;
            DataCollector<Lineitem> lineitemCollection = asb.accountAllContracts(dryRun, referenceDate, null,
                                                                   /*TaskMonitor monitor,*/ foreignCountry);
            ctx.respondWith().template("view/main/main.html");
        }
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/serviceAccounting")
    public void serviceAccounting(WebContext ctx) {
        boolean dryRun = false;
        DataCollector<Lineitem> lineitemCollection = (DataCollector<Lineitem>) sas.accountAllServiceOffers(dryRun);;
        int vvv = 1;
        ctx.respondWith().template("view/main/main.html");
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/companies")
    public void companies(WebContext ctx) {
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Company> query = oma.select(Company.class).orderAsc(Company.NAME);
        search.applyQueries(query,
                            Company.NAME,
                            Company.ADDRESS.inner(AddressData.CITY),
                            Company.CUSTOMERNR,
                            Company.MATCHCODE);
        Tagged.applyTagSuggestions(Company.class, search, query);
        PageHelper<Company> ph = PageHelper.withQuery(query).forCurrentTenant();
        ph.withContext(ctx);
        ctx.respondWith().template("view/xrm/companies.html", ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed(value = "/companies/suggest", jsonCall = true)
    public void companiesSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> Tagged.computeSuggestions(Company.class, q, c));
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

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1")
    public void company(WebContext ctx, String companyId) {
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

    @Part
    private static Sequences sequences;

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/customerNr")
    public void companyCustomerNr(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        if(company.getCustomerNr() == null) {
            // if the customerNr == null --> create a number
            String customerNr = String.valueOf(sequences.generateId("COMPANIES-" + company.getTenant().getId()));
            company.setCustomerNr(customerNr);
            oma.update(company);
        }
       ctx.respondWith().template("view/xrm/company-details.html", company);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/yearInfo")
    public void companyYearInfo(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        asb.createYearInformationForCompany(company, 2018);
        ctx.respondWith().template("view/xrm/company-details.html", company);
    }



    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/postComment")
    public void postComment(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        company.getComments()
               .addComment(getUser().getUserObject(UserAccount.class).getPerson().toString(),
                           getUser().getUserId(),
                           ctx.get("comment").asString(),
                           ctx.get("publicVisible").asBoolean());

        ctx.respondWith().template("view/xrm/company-details.html", company);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed(value = "/persons/suggest", jsonCall = true)
    public void personsSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> Tagged.computeSuggestions(Person.class, q, c));
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
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
    @Permission(MANAGE_XRM)
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
                                              Person.COMPANY.join(Company.NAME),
                                              Person.CONTACT.inner(ContactData.EMAIL),
                                              Person.CONTACT.inner(ContactData.PHONE))
                                      .eq(Person.COMPANY.join(Company.TENANT), tenants.getRequiredTenant())
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
    @Permission(MANAGE_XRM)
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
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/person/:2")
    public void person(WebContext ctx, String companyId, String personId) {
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

    @Part
    private static Starface stf;

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/person/:2/createPhonecall")
    public void personCreatePhonecall(WebContext ctx, String companyId, String personId) {
        Company company = findForTenant(Company.class, companyId);
        Person person = find(Person.class, personId);
        assertNotNew(company);
        setOrVerify(person, person.getCompany(), company);

        String phonenumber = person.getContact().getPhone();
        UserInfo userInfo = UserContext.getCurrentUser();
        UserAccount uac = userInfo.getUserObject(UserAccount.class);
        Employee employee = uac.as(Employee.class);
        stf.createPhoneCall(employee, phonenumber);
        ctx.respondWith().template("view/xrm/person-details.html", company, person);
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
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
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/person/:2/postComment")
    public void postPersonComment(WebContext ctx, String companyId, String personId) {
        Person person = find(Person.class, personId);
        assertNotNew(person);
        assertTenant(person.getCompany().getValue());

        person.getComments()
              .addComment(getUser().getUserObject(UserAccount.class).getPerson().toString(),
                          getUser().getUserId(),
                          ctx.get("comment").asString(),
                          ctx.get("publicVisible").asBoolean());

        ctx.respondWith().template("view/xrm/person-details.html", person.getCompany().getValue(), person);
    }


}
