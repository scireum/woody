/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.quotes;

import sirius.biz.web.BizController;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;

/**
 * Created by gha on 30.10.2016.
 */
@Framework("offers")
@Register(classes = Controller.class)
public class OffersController extends BizController {
//
//    public static final String MANAGE_OFFER = "permission-manage-offers";
//    public static final String VIEW_OFFER = "permission-view-offers";
//
//    private static final String MANAGE_XRM = "permission-manage-xrm";
//
//    @Part
//    private static SalesControllerService scs;
//
//    @Part
//    private static ServiceAccountingService sas;
//
//    @Part
//    private static Templates templates;
//
//    @Part
//    private static SendMailService sms;
//
//    private FileInputStream fileInputStream;
//
//    // Taste 'senden' in Angebotsliste oder Angebots-Details  wurde gedrückt
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/template")
//    public void askTemplateOffer(WebContext ctx, String companyId, String offerId) {
//        askTemplate(ctx, companyId, offerId, ServiceAccountingService.OFFER);
//    }
//
//    /**
//     * asks for the template, generate a mail-object and displays the mail-object
//     */
//    private void askTemplate(WebContext ctx, String companyId, String offerId, String function) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//        // Init a new mail-Object an store sender and receiver
//        Mail mail = new Mail();
//        String offerUniqueName = offer.getUniqueName();
//        mail.setUsageId(offerUniqueName);
//        Person person = offer.getPerson().getValue();
//        String receiver = person.getContact().getEmail();
//        String sender = offer.getEmployee().getValue().getEmail();
//        mail.getPersonEntity().setValue(person);
//        mail.setReceiverAddress(receiver);
//        mail.setSenderAddress(sender);
//        mail.setFunction(function);
//        UserInfo userInfo = UserContext.getCurrentUser();
//        UserAccount uac = userInfo.as(UserAccount.class);
//        mail.getEmployeeEntity().setValue(uac);
//        oma.update(mail);
//        List<String> templateList = new ArrayList<String>();
//        switch (function) {
//            case ServiceAccountingService.OFFER:
//                // Mail-Template 'Amgebot_senden' vorschlagen
//                templateList.add("Angebot_senden");
//                break;
//            case ServiceAccountingService.SALES_CONFIRMATION:
//                List<Mailtemplate> templates = oma.select(Mailtemplate.class)
//                                                  .where(Like.on(Mailtemplate.NAME)
//                                                             .ignoreCase()
//                                                             .ignoreEmpty()
//                                                             .contains("AB_"))
//                                                  .orderAsc(Mailtemplate.NAME)
//                                                  .queryList();
//                for (Mailtemplate mt : templates) {
//                    templateList.add(mt.getName());
//                }
//                String text = "Auftragsbestätigung für Position(en): {0} senden?";
//                String posText = "";
//                List<OfferItem> confirmationList = sas.getConfirmationOfferItems(offer);
//                for (OfferItem oi : confirmationList) {
//                    if (posText != null && !posText.isEmpty()) {
//                        posText = posText + ", ";
//                    }
//                    posText = posText + oi.getPosition();
//                }
//                String message = MessageFormat.format(text, posText);
//                UserContext.message(Message.info(message));
//                break;
//        }
//
//        ctx.respondWith().template("view/mails/mail-details.html", templateList, mail, function);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/reCreateOffer")
//    public void reCreateOffer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//        sas.copyOffer(offer, true);
//        companyOffers(ctx, companyId);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/copyOffer")
//    public void copyOffer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//        sas.copyOffer(offer, false);
//        companyOffers(ctx, companyId);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/copyOffer")
//    public void cancelOffer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//
//        List<OfferItem> oiList =
//                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
//        boolean error = false;
//        OfferItem oi1 = null;
//        for (OfferItem oi : oiList) {
//            if (oi.isLicense() || oi.isService()) {
//                if (OfferItemState.UNUSED.equals(oi)
//                    || OfferItemState.COPY.equals(oi)
//                    || OfferItemState.CANCELED.equals(oi)) {
//                    continue;
//                }
//                oi1 = oi;
//                error = true;
//                break;
//            }
//        }
//        if (error) {
//            String text = MessageFormat.format("Die Position {0} hat den Status {1} --> Storno nicht möglich.",
//                                               oi1.getPosition(),
//                                               oi1.getState().toString());
//            UserContext.message(Message.info(text));
//            ctx.respondWith().template("view/offers/offer-details.html", company, offer);
//            return;
//        }
//        for (OfferItem oi : oiList) {
//            if (OfferItemState.UNUSED.equals(oi)
//                || OfferItemState.COPY.equals(oi)
//                || OfferItemState.CANCELED.equals(oi)) {
//                continue;
//            }
//            oi.setState(OfferItemState.CANCELED);
//            oma.update(oi);
//        }
//        offer.setState(OfferState.CLOSED);
//        String text = MessageFormat.format("Das Angebot Nr.: {0} wurde storniert.", offer.getNumber());
//        UserContext.message(Message.info(text));
//        companyOffers(ctx, companyId);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/confirmOffer")
//    public void confirmOffer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//        List<OfferItem> confirmationList = sas.getConfirmationOfferItems(offer);
//        if (confirmationList.size() == 0) {
////            String message = MessageFormat.format(
////                    "Das Angebot {0} enthält keine zu bestätigenden Positionen.",
////                    offer.getNumber());
////            UserContext.message(Message.info(message));
////            MagicSearch search = MagicSearch.parseSuggestions(ctx);
////            SmartQuery<Offer> query = oma.select(Offer.class).eq(Offer.COMPANY, company).orderDesc(Offer.NUMBER);
////
////            Tagged.applyTagSuggestions(Offer.class, search, query);
////            PageHelper<Offer> ph = PageHelper.withQuery(query);
////            ph.withContext(ctx);
////            ctx.respondWith()
////               .template("view/offers/company-offers.html", company, ph.asPage(), search.getSuggestionsString());
////            return;
//        }
//
//        askTemplate(ctx, companyId, offerId, ServiceAccountingService.SALES_CONFIRMATION);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/company/:1/offers")
//    // shows the offers of the given company
//    public void companyOffers(WebContext ctx, String companyId) {
////        Company company = findForTenant(Company.class, companyId);
////        MagicSearch search = MagicSearch.parseSuggestions(ctx);
////        SmartQuery<Offer> query = oma.select(Offer.class).eq(Offer.COMPANY, company).orderDesc(Offer.NUMBER);
////
////        Tagged.applyTagSuggestions(Offer.class, search, query);
////        PageHelper<Offer> ph = PageHelper.withQuery(query);
////        ph.withContext(ctx);
////        ctx.respondWith()
////           .template("view/offers/company-offers.html", company, ph.asPage(), search.getSuggestionsString());
//    }
//
//    @LoginRequired
//    @Permission(VIEW_OFFER)
//    @Routed("/company/:1/offer/:2")
//    // display and save the given offer
//    public void offer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//
//        if (ctx.isPOST() && getUser().hasPermission(MANAGE_OFFER)) {
//            try {
//                boolean wasNew = offer.isNew();
//                load(ctx, offer);
//                oma.update(offer);
//
//                showSavedMessage();
//                if (wasNew) {
//                    ctx.respondWith().redirectTemporarily("/company/" + company.getId() + "/offer/" + offer.getId());
//                    return;
//                }
//            } catch (Throwable e) {
//                UserContext.handle(e);
//            }
//        }
//        ctx.respondWith().template("view/offers/offer-details.html", company, offer);
//    }
//
//    @LoginRequired
//    @Permission(VIEW_OFFER)
//    @Routed("/company/:1/offer/:2/viewOffer")
//    // Display the given offer
//    public void viewOffer(WebContext ctx, String companyId, String offerId) {
//        Company company = findForTenant(Company.class, companyId);
//        assertNotNew(company);
//        Offer offer = find(Offer.class, offerId);
//        setOrVerify(offer, offer.getCompany(), company);
//        Context context = sas.prepareContext(offer, ServiceAccountingService.OFFER);
//        OutputStream out = ctx.respondWith().outputStream(HttpResponseStatus.OK, MimeHelper.APPLICATION_PDF);
//        // ToDo auf pasta umstellen
//        templates.generator().useTemplate("templates/offer.pdf.vm").applyContext(context).generateTo(out);
////        templates.generator().useTemplate("templates/offer.pdf.pasta").applyContext(context).generateTo(out);
//        File file = new File("test123.pdf");
//        try {
//            out = new FileOutputStream(file);
//            templates.generator().useTemplate("templates/offer.pdf.pasta").applyContext(context).generateTo(out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        int iii = 1;
//    }
//
//    @LoginRequired
//    @Permission(VIEW_OFFER)
//    @Routed("/company/:1/offer/:2/offerItems")
//    // Displays the offerItems of the given offer
//    public void offerOfferItems(WebContext ctx, String companyId, String offerId) {
////        Company company = findForTenant(Company.class, companyId);
////        assertNotNew(company);
////        Offer offer = findForTenant(Offer.class, offerId);
////        MagicSearch search = MagicSearch.parseSuggestions(ctx);
////        SmartQuery<OfferItem> query =
////                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);
////
////        //Tagged.applyTagSuggestions(Offer.class, search, query);
////        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
////        ph.withContext(ctx);
////        ctx.respondWith()
////           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/offerItem/:3/nextState")
//    // calculates the next state for the given offerItem
//    public void offerItemNextState(WebContext ctx, String companyId, String offerId, String offerItemId) {
////        Company company = findForTenant(Company.class, companyId);
////        assertNotNew(company);
////        Offer offer = findForTenant(Offer.class, offerId);
////        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
////        OfferItemState newState = sas.getNextState(oi);
////        if (newState != null) {
////            oi.setState(newState);
////            oma.update(oi);
////        }
////
////        MagicSearch search = MagicSearch.parseSuggestions(ctx);
////        SmartQuery<OfferItem> query =
////                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);
////
////        //Tagged.applyTagSuggestions(Offer.class, search, query);
////        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
////        ph.withContext(ctx);
////        ctx.respondWith()
////           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/offerItem/:3/createContract")
//    // calculates the next state for the given offerItem
//    public void offerItemCreateContracte(WebContext ctx, String companyId, String offerId, String offerItemId) {
////        Company company = findForTenant(Company.class, companyId);
////        assertNotNew(company);
////        Offer offer = findForTenant(Offer.class, offerId);
////        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
////        String message = sas.createContractFromOfferItem(oi);
////        oi.setState(OfferItemState.ACCEPTED);
////        oma.update(oi);
////        MagicSearch search = MagicSearch.parseSuggestions(ctx);
////        SmartQuery<OfferItem> query =
////                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION);
////
////        //Tagged.applyTagSuggestions(Offer.class, search, query);
////        PageHelper<OfferItem> ph = PageHelper.withQuery(query);
////        ph.withContext(ctx);
////        UserContext.message(Message.info(message));
////        ctx.respondWith()
////           .template("view/offers/offer-offerItems.html", company, offer, ph.asPage(), search.getSuggestionsString());
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/delete")
//    public void deleteOffer(WebContext ctx, String companyId, String offerId) {
//        Optional<Offer> offer = tryFind(Offer.class, offerId);
//        if (offer.isPresent()) {
//            assertTenant(offer.get().getCompany().getValue());
//            oma.delete(offer.get());
//            showDeletedMessage();
//        }
//        companyOffers(ctx, companyId);
//    }
//
//    @LoginRequired
//    @Permission(VIEW_OFFER)
//    @Routed("/company/:1/offer/:2/offerItem/:3")
//    // Displays and save te given offerItem
//    public void offerItem(WebContext ctx, String companyId, String offerId, String offerItemId) {
////        Company company = findForTenant(Company.class, companyId);
////        assertNotNew(company);
////        Offer offer = findForTenant(Offer.class, offerId);
////        OfferItem oi = findForTenant(OfferItem.class, offerItemId);
////        setOrVerify(oi, oi.getOffer(), offer);
////        if (ctx.isPOST() && getUser().hasPermission(MANAGE_OFFER)) {
////            try {
////                boolean wasNew = oi.isNew();
////                load(ctx, oi);
////                oma.update(oi);
////                showSavedMessage();
////                if (wasNew) {
////                    ctx.respondWith()
////                       .redirectTemporarily(WebContext.getContextPrefix()
////                                            + "/company/"
////                                            + company.getId()
////                                            + "/offer/"
////                                            + offer.getId()
////                                            + "/offerItem/"
////                                            + oi.getId());
////                    return;
////                }
////            } catch (Throwable e) {
////                UserContext.handle(e);
////            }
////        }
////        ctx.respondWith().template("view/offers/offerItem-details.html", company, offer, oi);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/company/:1/offer/:2/offerItem/:3/delete")
//    public void deleteOfferItem(WebContext ctx, String companyId, String offerId, String offerItemId) {
//        Optional<OfferItem> offerItem = tryFind(OfferItem.class, offerItemId);
//        if (offerItem.isPresent()) {
//            assertTenant(offerItem.get().getOffer().getValue().getCompany().getValue());
//            oma.delete(offerItem.get());
//            showDeletedMessage();
//        }
//        offerOfferItems(ctx, companyId, offerId);
//    }
}
