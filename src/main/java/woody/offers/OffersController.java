/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Column;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import woody.core.tags.Tagged;
import woody.sales.Contract;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.xrm.Company;

import java.util.List;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("offers")
@Register(classes = Controller.class)
public class OffersController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @Part
    private static ServiceAccountingService sas;

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/offer/:1/sendOffer")
    public void sendOffer(WebContext ctx, String offerId) {
        Offer offer = find(Offer.class, offerId);
        System.err.println("Offer senden");
            sas.sendOffer(offer);
        String companyId = offer.getCompany().getValue().getIdAsString();
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/offer/:1/viewOffer")
    public void viewOffer(WebContext ctx, String offerId) {
        Offer offer = find(Offer.class, offerId);
            sas.viewOffer(offer);
        String companyId = offer.getCompany().getValue().getIdAsString();
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/offer/:1/reCreateOffer")
    public void reCreateOffer(WebContext ctx, String offerId) {
        Offer offer = find(Offer.class, offerId);
        System.err.println("Offer überarbeiten");
        String companyId = offer.getCompany().getValue().getIdAsString();
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/offer/:1/copyOffer")
    public void copyOffer(WebContext ctx, String offerId) {
        Offer offer = find(Offer.class, offerId);
        System.err.println("Offer kopieren");
        String companyId = offer.getCompany().getValue().getIdAsString();
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/offer/:1/confirmOffer")
    public void confirmOffer(WebContext ctx, String offerId) {
        Offer offer = find(Offer.class, offerId);
        System.err.println("Offer bestätigen");
        String companyId = offer.getCompany().getValue().getIdAsString();
        companyOffers(ctx, companyId);
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/offers")
    public void companyOffers(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Offer> query = oma.select(Offer.class)
                                     .eq(Offer.COMPANY, company)
                                     .orderDesc(Offer.NUMBER);

        Tagged.applyTagSuggestions(Offer.class, search, query);
        PageHelper<Offer> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/offers/company-offers.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/offer/:2")
    public void offer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        Offer offer = find(Offer.class, offerId);
//        Long productId =  contract.getPackageDefinition().getValue().getProduct().getId();
        assertNotNew(company);
        setOrVerify(offer, offer.getCompany(), company);
//        List<PackageDefinition>  pdList = oma.select(PackageDefinition.class)
//                                             .eq(PackageDefinition.PRODUCT, productId).queryList();

        if (ctx.isPOST()) {
            try {
                boolean wasNew = offer.isNew();
                load(ctx, offer);
                oma.update(offer);
//                contract.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily(WebContext.getContextPrefix()
                                            + "/company/"
                                            + company.getId()
                                            + "/offer/"
                                            + offer.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/offers/offer-details.html", company, offer);
    }

}
