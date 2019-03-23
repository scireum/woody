/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.opportunities;

import sirius.biz.model.PersonData;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Constraint;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import woody.offers.Offer;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

/**
 * Created by gha at 19.01.2017
 */
@Framework("opportunities")
@Register(classes = Controller.class)
public class OpportunityController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/opportunities")
    public void opportunities(WebContext ctx) {
        // ToDO Constraint.in aus CRM portieren.
        OpportunityState[] states = OpportunityState.getStatesColdToNegotiation();
//        Constraint constraint = null;

//        Constraint constraint = Constraints.     .in(Opportunity.OLDSTATE, states);
//        qry.or(constraint);
        SmartQuery<Opportunity> query = oma.select(Opportunity.class)
                                      .eq(Opportunity.COMPANY.join(Company.TENANT), currentTenant())
                                      .orderAsc(Opportunity.SORTDATE)
                                      .orderDesc(Opportunity.SORT_VALUE);
        PageHelper<Opportunity> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/opportunities/opportunities.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunities")
    public void companyOpportunities(WebContext ctx, String companyId) {
//        Company company = findForTenant(Company.class, companyId);
//        MagicSearch search = MagicSearch.parseSuggestions(ctx);
//        SmartQuery<Opportunity> query = oma.select(Opportunity.class)
//                                        .eq(Opportunity.COMPANY, company)
//                                        .orderAsc(Opportunity.SOURCE);
//
//        Tagged.applyTagSuggestions(Opportunity.class, search, query);
//        PageHelper<Opportunity> ph = PageHelper.withQuery(query);
//        ph.withContext(ctx);
//        ctx.respondWith()
//           .template("view/opportunities/company-opportunities.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2")
    public void opportunity(WebContext ctx, String companyId, String opportunityId) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);
        ctx.respondWith().template("templates/opportunities/opportunity-overview.html.pasta", company, opportunity);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/edit")
    public void editOpportunity(WebContext ctx, String companyId, String opportunityId) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = opportunity.isNew();
                load(ctx, opportunity);
                oma.update(opportunity);
//                opportunity.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily("/company/" + company.getId() + "/opportunity/" + opportunity.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", company, opportunity);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/delete")
    public void deleteOpportunity(WebContext ctx, String companyId, String opportunityId) {
        Optional<Opportunity> opportunity = tryFind(Opportunity.class, opportunityId);
        if (opportunity.isPresent()) {
            assertTenant(opportunity.get().getCompany().getValue());
            oma.delete(opportunity.get());
            showDeletedMessage();
        }
        companyOpportunities(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1d")
    public void plus1d(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, 1, ChronoUnit.DAYS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1w")
    public void plus1w(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, 7, ChronoUnit.DAYS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1m")
    public void plus1m(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, 1, ChronoUnit.MONTHS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1y")
    public void plus1y(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, 1, ChronoUnit.YEARS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/minus1d")
    public void minus1d(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, -1, ChronoUnit.DAYS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/minus1w")
    public void minus1w(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, -7, ChronoUnit.DAYS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/minus1m")
    public void minus1m(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, -1, ChronoUnit.MONTHS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/minus1y")
    public void minus1y(WebContext ctx, String companyId, String opportunityId) {
        Tuple<Company, Opportunity> tuple = addTime(companyId, opportunityId, -1, ChronoUnit.YEARS);
        ctx.respondWith().template("templates/opportunities/opportunity-details.html.pasta", tuple.getFirst(), tuple.getSecond());
    }

    private Tuple<Company, Opportunity> addTime(String companyId, String opportunityId, int number, TemporalUnit unit) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        opportunity.getPerson().getValue().getContact().getEmail();
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        LocalDate date = opportunity.getNextInteraction();
        if (date == null) {
            date = LocalDate.now();
        }
        date = date.plus(number, unit);
        DayOfWeek weekday = date.getDayOfWeek();
        int two = 2;
        int one = 1;
        if(number < 0) {
           two = -1;
           one = -2;
        }
        if (DayOfWeek.SATURDAY.equals(weekday)) {
            date = date.plusDays(two);
        }
        if (DayOfWeek.SUNDAY.equals(weekday)) {
            date = date.plusDays(one);
        }
        opportunity.setNextInteraction(date);
        oma.update(opportunity);
        return new Tuple(company, opportunity);
    }
}
