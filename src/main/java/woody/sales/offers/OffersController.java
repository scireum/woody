/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.offers;

import io.netty.handler.codec.http.HttpResponseStatus;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.commons.Context;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.MimeHelper;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.templates.Templates;
import woody.core.tags.Tagged;
import woody.xrm.Company;
import woody.xrm.XRMController;

import java.io.OutputStream;
import java.util.Optional;

/**
 * Created by gha on 30.10.2016.
 */
@Framework("offers")
@Register(classes = Controller.class)
public class OffersController extends BizController {

    public static final String MANAGE_OFFER = "permission-manage-offers";
    public static final String VIEW_OFFER = "permission-view-offers";

    private static final String MANAGE_XRM = "permission-manage-xrm";


    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Templates templates;


    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/sendOffer")
    public void sendOffer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        sas.sendOffer(offer);
        companyOffers(ctx, companyId);
    }


    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/reCreateOffer")
    public void reCreateOffer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        sas.copyOffer(offer, true);
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/copyOffer")
    public void copyOffer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        sas.copyOffer(offer, false);
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/confirmOffer")
    public void confirmOffer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(XRMController.PERMISSION_MANAGE_XRM)
    @Routed("/company/:1/offers")
    public void companyOffers(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Offer> query = oma.select(Offer.class).eq(Offer.COMPANY, company).orderDesc(Offer.NUMBER);

        Tagged.applyTagSuggestions(Offer.class, search, query);
        PageHelper<Offer> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/offers/company-offers.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(VIEW_OFFER)
    @Routed("/company/:1/offer/:2")
    public void offer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);

        if (ctx.isPOST() && getUser().hasPermission(MANAGE_OFFER)) {
           try {
                boolean wasNew = offer.isNew();
                load(ctx, offer);
                oma.update(offer);

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

    @LoginRequired
    @Permission(VIEW_OFFER)
    @Routed("/company/:1/offer/:2/viewOffer")
    public void viewOffer(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        Context context = sas.prepareContext(offer, ServiceAccountingService.OFFER);
        OutputStream out = ctx.respondWith().outputStream(HttpResponseStatus.OK, MimeHelper.APPLICATION_PDF);
        templates.generator().useTemplate("templates/offer.pdf.vm").applyContext(context).generateTo(out);
    }

    @LoginRequired
    @Permission(VIEW_OFFER)
    @Routed("/company/:1/offer/:2/offerItems")
    public void offerOfferItems(WebContext ctx, String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = findForTenant(Offer.class, offerId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<OfferItem> query =
                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);

        //Tagged.applyTagSuggestions(Offer.class, search, query);
        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/offerItem/:3/nextState")
    public void offerItemNextState(WebContext ctx, String companyId, String offerId, String offerItemId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = findForTenant(Offer.class, offerId);
        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
        OfferItemState newState = sas.getNextState(oi);
        if (newState != null) {
            oi.setState(newState);
            oma.update(oi);
        }

        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<OfferItem> query =
                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);

        //Tagged.applyTagSuggestions(Offer.class, search, query);
        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/delete")
    public void deleteOffer(WebContext ctx, String companyId, String offerId) {
        Optional<Offer> offer = tryFind(Offer.class, offerId);
        if (offer.isPresent()) {
            assertTenant(offer.get().getCompany().getValue());
            oma.delete(offer.get());
            showDeletedMessage();
        }
        companyOffers(ctx, companyId);
    }

    @LoginRequired
    @Permission(VIEW_OFFER)
    @Routed("/company/:1/offer/:2/offerItem/:3")
    public void offerItem(WebContext ctx,  String companyId, String offerId, String offerItemId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = findForTenant(Offer.class, offerId);
        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
        setOrVerify(oi, oi.getOffer(), offer);
        if (ctx.isPOST() && getUser().hasPermission(MANAGE_OFFER)) {
            try {
                boolean wasNew = oi.isNew();
                load(ctx, oi);
                oma.update(oi);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily(WebContext.getContextPrefix()
                                            + "/company/"
                                            + company.getId()
                                            + "/offer/"
                                            + offer.getId()
                                            + "/offerItem/"
                                            + oi.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/offers/offerItem-details.html", company, offer, oi);
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/offerItem/:3/delete")
    public void deleteOfferItem(WebContext ctx, String companyId, String offerId, String offerItemId) {
        Optional<OfferItem> offerItem = tryFind(OfferItem.class, offerItemId);
        if (offerItem.isPresent()) {
            assertTenant(offerItem.get().getOffer().getValue().getCompany().getValue());
            oma.delete(offerItem.get());
            showDeletedMessage();
        }
        offerOfferItems(ctx, companyId, offerId);
    }

}
