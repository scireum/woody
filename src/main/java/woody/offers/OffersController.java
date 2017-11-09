/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import io.netty.handler.codec.http.HttpResponseStatus;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.Message;
import sirius.web.controller.Routed;
import sirius.web.http.MimeHelper;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import sirius.web.templates.Templates;
import woody.core.mails.Mail;
import woody.core.mails.SendMailService;
import woody.core.tags.Tagged;
import woody.sales.SalesController;
import woody.sales.SalesControllerService;
import woody.xrm.Company;
import woody.xrm.Person;
import woody.xrm.XRMController;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by gha on 30.10.2016.
 */
@Framework("offers")
@Register(classes = Controller.class)
public class OffersController extends BizController {

    public static final String SENDOFFER = "sendOffer";
    public static final String CONFIRMOFFER = "confirmOffer";

    public static final String MANAGE_OFFER = "permission-manage-offers";
    public static final String VIEW_OFFER = "permission-view-offers";

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @Part
    private static SalesControllerService scs;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Templates templates;

    @Part
    private static SendMailService sms;

    // Taste 'Mail senden' wurde gedrückt
    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/mail/:3/sendOffer")
    public void sendOffer(WebContext ctx, String companyId, String offerId, String mailId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        Mail mail = find(Mail.class, mailId);
        if(mail == null) {
            ctx.respondWith().template("view/offers/offer-details.html", company, offer);
            return;
        }
        if(Strings.isEmpty(mail.getSubject())) {
            ctx.respondWith().template("view/offers/offer-details.html", company, offer);
            return;
        }
        Context context = sas.askOffer(offer);
        context.set("plainSubject", context.get("subject"));
        String plainText = templates.generator().useTemplate("templates/mail-template.vm").applyContext(context).generate();
        context.set("plainText", plainText);
        String filenamePDF = (String) context.get("filenamePDF");
        context.set("plainAttachment", filenamePDF);
        File file = (File) context.get("fileAttachment");
        DataSource dataSource = new DataSource() {

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public String getContentType() {
                return MimeHelper.APPLICATION_PDF;
            }

            @Override
            public String getName() {
                return filenamePDF;
            }
        };


        sms.prepareMail(context, ServiceAccountingService.OFFER, dataSource);
        UserContext.message(Message.info("Mail wurde versendet"));
        ctx.respondWith().template("view/offers/offer-details.html", company, offer);

    }

    // Taste 'senden' in Angebotsliste oder Angebots-Details  wurde gedrückt
    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/template")
    public void askTemplateSendOffer(WebContext ctx, String companyId, String offerId) {
        askTemplateAndSend(ctx, companyId, offerId, SENDOFFER);
    }

    private void askTemplateAndSend(WebContext ctx, String companyId, String offerId, String function) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        // Neue Mail initiieren und sender und Empfänger speichern
        Mail mail = new Mail();
        String receiver = offer.getBuyer().getValue().getContact().getEmail();
        String sender = offer.getEmployee().getValue().getEmail();
        mail.setReceiverAddress(receiver);
        mail.setSenderAddress(sender);
        oma.update(mail);
        List<String> templateList =  new ArrayList<String>();
        switch (function) {
            case SENDOFFER:
                // Mail-Template 'Amgebot_senden' vorschlagen
                templateList.add("Angebot_senden");
                break;
            case CONFIRMOFFER:

                break;
        }

        ctx.respondWith().template("view/mails/mail-details.html", company, offer, templateList, mail, function);
    }

    private void askForSalesConfirmation(Offer offer) {

        List<OfferItem> confirmationList = sas.getConfirmationOfferItems(offer);
        if (confirmationList.size() == 0) {
// ToDo            throw new BusinessException("Für dieses Angebot gibt es keine aktuelle Auftragsbestätigung zum Senden.");
        }
        String text = "Auftragsbestätigung für Position(en): {0} senden?";
        String posText = "";
        for (OfferItem oi : confirmationList) {
            if (!"".equals(posText)) {
                posText = posText + ", ";
            }
            posText = posText + oi.getPosition();
        }

        String askText = MessageFormat.format(text, posText);

//        ApplicationController.get(DialogsBean.class).ask(MessageFormat.format(text, posText), new ActionListener() {
//            @Override
//            public void action() throws Exception {
//                sas.sendSalesConfirmation(view, offer, confirmationList);
//                view.refresh();
//            }
//        });
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/mail/:3/sendNotOffer")
    public void sendNotOffer(WebContext ctx, String companyId, String offerId, String mailId) {
        List<String>  templateList =  new ArrayList<String>();
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = find(Offer.class, offerId);
        setOrVerify(offer, offer.getCompany(), company);
        Mail mail = find(Mail.class, mailId);
        Context context = sas.askOffer(offer);
        context.set("plainSubject", context.get("subject"));
        String plainText = templates.generator().useTemplate("templates/mail-template.vm").applyContext(context).generate();
        context.set("plainText", plainText);
        String filenamePDF = (String) context.get("filenamePDF");
        context.set("plainAttachment", filenamePDF);
        if(mail != null && Strings.isFilled(mail.getTemplate())) {
            templateList.add(mail.getTemplate());
        }
        UserContext.message(Message.info("Mail wurde nicht versendet"));
        ctx.respondWith().template("view/mails/mail-details.html", company, offer, templateList, mail, SENDOFFER);
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

        sas.sendSalesConfirmation(offer);
        scs.companyContracts(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
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
    public void offerOfferItems(WebContext ctx,  String companyId, String offerId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = findForTenant(Offer.class, offerId);
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<OfferItem> query = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);

        //Tagged.applyTagSuggestions(Offer.class, search, query);
        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ctx.respondWith()
           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/company/:1/offer/:2/offerItem/:3/nextState")
    public void offerItemNextState(WebContext ctx,  String companyId, String offerId, String offerItemId) {
        Company company = findForTenant(Company.class, companyId);
        assertNotNew(company);
        Offer offer = findForTenant(Offer.class, offerId);
        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
        OfferItemState newState = sas.getNextState(oi);
        if(newState != null) {
            oi.setState(newState);
            oma.update(oi);
        }

        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<OfferItem> query = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);

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
