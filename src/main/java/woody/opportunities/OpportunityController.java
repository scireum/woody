/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.opportunities;


import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import woody.core.tags.Tagged;

import woody.xrm.Company;

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
    @Routed("/company/:1/opportunities")
    public void companyOpportunities(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Opportunity> query = oma.select(Opportunity.class)
                                        .eq(Opportunity.COMPANY, company)
                                        .orderAsc(Opportunity.SOURCE);

        Tagged.applyTagSuggestions(Opportunity.class, search, query);
        PageHelper<Opportunity> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/opportunities/company-opportunities.html", company, ph.asPage(), search.getSuggestionsString());
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2")
    public void opportunity(WebContext ctx, String companyId, String opportunityId) {
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
                       .redirectTemporarily(WebContext.getContextPrefix()
                                            + "/company/"
                                            + company.getId()
                                            + "/opportunity/"
                                            + opportunity.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/opportunities/opportunity-details.html", company, opportunity);
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
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        opportunity = addTime(opportunity, 1, ChronoUnit.DAYS);

        ctx.respondWith().template("view/opportunities/opportunity-details.html", company, opportunity);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1w")
    public void plus1w(WebContext ctx, String companyId, String opportunityId) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        opportunity = addTime(opportunity, 7, ChronoUnit.DAYS);

        ctx.respondWith().template("view/opportunities/opportunity-details.html", company, opportunity);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1m")
    public void plus1m(WebContext ctx, String companyId, String opportunityId) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        opportunity = addTime(opportunity, 1, ChronoUnit.MONTHS);

        ctx.respondWith().template("view/opportunities/opportunity-details.html", company, opportunity);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/opportunity/:2/plus1y")
    public void plus1y(WebContext ctx, String companyId, String opportunityId) {
        Company company = findForTenant(Company.class, companyId);
        Opportunity opportunity = find(Opportunity.class, opportunityId);
        assertNotNew(company);
        setOrVerify(opportunity, opportunity.getCompany(), company);

        opportunity = addTime(opportunity, 1, ChronoUnit.YEARS);

        ctx.respondWith().template("view/opportunities/opportunity-details.html", company, opportunity);
    }

    private Opportunity addTime(Opportunity opportunity, int i, TemporalUnit unit) {
        LocalDate date = opportunity.getNextInteraction();
        if(date == null) {
            date = LocalDate.now();
        }
        date = date.plus(i, unit);
        DayOfWeek weekday = date.getDayOfWeek();
        if(DayOfWeek.SATURDAY.equals(weekday)) {
            date = date.plusDays(2);
        }
        if(DayOfWeek.SUNDAY.equals(weekday)) {
            date = date.plusDays(1);
        }
        opportunity.setNextInteraction(date);
        oma.update(opportunity);
        return opportunity;
    }
}
