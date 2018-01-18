/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.offers;


import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.DataCollector;
import sirius.kernel.commons.NumberFormat;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.mails.Mails;
import sirius.web.security.UserContext;
import sirius.web.templates.Templates;
import woody.core.employees.Employee;
import woody.sales.contracts.AccountingService;
import woody.sales.contracts.Contract;
import woody.sales.contracts.Lineitem;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.xrm.Company;
import woody.xrm.Person;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

/**
 * Created by gerhardhaufler on 23.12.15.
 */
@Register(classes=ServiceAccountingService.class)
public class ServiceAccountingServiceBean implements ServiceAccountingService {

    private Amount prodUmsatz = null;
    private Amount testUmsatz = null;
    private LocalDate accountingDate = null;

    // ToDo Rechtsnachfolger für SocialService
//    @Part
//    private SocialService ss;
    @Part
    private AccountingService as;



    // ToDo Rechtsnachfolger für MailService
    @Part
 private Mails mails;

    @Part
    protected OMA oma;

    @Part
    private static Templates templates;

    @Override
    public OfferItemState getNextState(OfferItem oi) {
        if(OfferItemState.OFFER.equals(oi.getState()))  {return OfferItemState.ORDERED; }       // ORDERED follows OFFER
//        if(OfferItemState.ORDERED.equals(oi.getState()))  {return OfferItemState.CONFIRMED; }   // CONFIRMED follows ORDERED
        if(OfferItemState.CONFIRMED.equals(oi.getState()))  {return OfferItemState.DEVELOPED; } // DEVELOPED follows CONFIRMED
        if(OfferItemState.DEVELOPED.equals(oi.getState()))  {return OfferItemState.ACCEPTED; }  // ACCEPTED follows DEVELOPED
        return null;
    }

    @Override
    public boolean isOfferItemVisible(OfferItem oi, OfferItemState targetState) {
        OfferItemState nextState = getNextState(oi);
        if(nextState == null) {return false;}         // null = no next step ---> not visible
        if(targetState.equals(nextState)) {
            return true;
        }
        return false;
    }


    @Override
    public List<Lineitem> accountAllServiceOffers(final boolean dryRun /* , TaskMonitor monitor*/) {
        DataCollector<Lineitem> itemCollector = new DataCollector<Lineitem>() {
            @Override
            public void add(Lineitem entity) {
                if (!dryRun) {
                    oma.update(entity);
                }
                super.add(entity);
            }
        };

        LocalDate referenceDate = LocalDate.now();
        testUmsatz = null;
        prodUmsatz = null;
        if(dryRun)  {
            testUmsatz = Amount.ZERO;
        } else {
            prodUmsatz =Amount.ZERO;
        }

        // get a list with all offers
        List<Offer> offerList = oma.select(Offer.class).orderAsc(Offer.NUMBER).queryList();
        // process this List
        long invoiceNr = 0;
        for (Offer offer : offerList) {
            invoiceNr--;

            //get a list with all positions from this offer
            List<OfferItem> offerItemList = oma.select(OfferItem.class)
                    .eq(OfferItem.OFFER, offer)
                    .orderAsc(OfferItem.POSITION).queryList();
            for (OfferItem offerItem : offerItemList) {
                if(!OfferItemType.SERVICE.equals(offerItem.getOfferItemType())) {continue;}

                if (OfferItemState.ACCEPTED.equals(offerItem.getState())) {
                    //this position is accepted by the customer --> account it
                    Lineitem lineitem = generateLineitem(referenceDate, invoiceNr, offer, offerItem);
                    // calculate the sum-values
                    Amount price = null;
                    price = offerItem.getQuantity().times(offerItem.getSinglePrice());
                    if (offerItem.getDiscount() != null) {
                        price = price.decreasePercent(offerItem.getDiscount());
                    }
                    if (dryRun) {
                        testUmsatz = testUmsatz.add(price);
                    } else {
                        offerItem.setState(OfferItemState.ACCOUNTED);
                        oma.update(offerItem);
                        prodUmsatz = prodUmsatz.add(price);
                    }
                    itemCollector.add(lineitem);
                }
            }
        }
        // build a activity-news
        Amount summe = Amount.ZERO;
        int counter = 0;
        for (Lineitem lineitem : itemCollector.getData()) {
            counter++;
            Amount price = lineitem.getPrice().times(lineitem.getQuantity());
            if(lineitem.getPositionDiscount().isPositive())  {
               price = price.decreasePercent(lineitem.getPositionDiscount());
            }
            summe = summe.add(price);
        }

//        if (dryRun) {
//            testUmsatz = summe;
//        } else {
//            prodUmsatz = summe;
//        }
        String text =  MessageFormat
                .format("Service-Abrechnung zum Referenzdatum {0} im Modus {1} erstellt, {2} Rechnungspositionen, Netto-Umsatz: {3} EUR",
                        NLS.toUserString(referenceDate),
                        dryRun == true ? "Test" : "Produktiv", NLS.toUserString(counter),
                        NLS.toUserString(summe));
//        ss.forBackendStream(
//                DisplayMarkdownFactory.FACTORY_NAME,
//                "Service-Abrechnung",
//                MessageFormat
//                        .format("Service-Abrechnung zum Referenzdatum {0} im Modus {1} erstellt, {2} Rechnungspositionen, Netto-Umsatz: {3} EUR",
//                                NLS.toUserString(referenceDate),
//                                dryRun == true ? "Test" : "Produktiv", NLS.toUserString(counter),
//                                NLS.toUserString(summe)))
//                .loginRequired(true).setUser(Users.getCurrentUser()).publish();

        accountingDate = LocalDate.now();
        return itemCollector.getData();
    }

    /**
     * generates a lineitem for account a service -item based on a offeritem with te given data
     * @param referenceDate
     * @param invoiceNr
     * @param offer
     * @param offerItem
     * @return
     */
    private Lineitem generateLineitem(LocalDate referenceDate, long invoiceNr, Offer offer, OfferItem offerItem) {
        Lineitem lineitem = new Lineitem();
        lineitem.setFinalDiscountSum(Amount.ZERO); // finalDiscountSum is not used
        lineitem.setCollmexCredit(false);
        lineitem.setCredit(false);
        lineitem.setLineitemType(Lineitem.LINEITEMTYPE_OA);
        lineitem.setLineitemDate(referenceDate);
        lineitem.setPrice(offerItem.getSinglePrice());
        lineitem.setInvoiceNr(invoiceNr);
        lineitem.setStatus(Lineitem.LINEITEMSTATUS_NEW);
        lineitem.setClearingDate(LocalDateTime.now());
        lineitem.setCompanyName(offer.getCompany().getValue().getName());
        String customerNr = offer.getCompany().getValue().getCustomerNumber();
        if (Strings.isEmpty(customerNr)) {
            // no accounting without a customerNr!!!
            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.customerNrMissing")
                        .set("company", lineitem.getCompanyName()).handle();
        }
        lineitem.setCustomerNr(customerNr);
        lineitem.setMeasurement(offerItem.getQuantityUnit());
        lineitem.setPackageName(offerItem.getPackageDefinition().getValue().getName());
        lineitem.setQuantity(offerItem.getQuantity());
        lineitem.setDescription(makeDescription(offerItem));
        lineitem.setPosition(offerItem.getPosition());
        lineitem.setArticle(offerItem.getBaseProduct().getValue().getArticle());
        lineitem.setPositionDiscount(offerItem.getDiscount());
        if (offerItem.isInfoText() ) {
            lineitem.setPositionType(lineitem.getLineitemCollmexTextposition());
        } else {
            lineitem.setPositionType(lineitem.getLineitemCollmexNormalposition());
        }
        return lineitem;
    }

    /**
     * makes the description-String for a lineitem based on the given offerItem
     * @param offerItem
     * @return String for a lineitem-description
     */
    private String makeDescription(OfferItem offerItem) {

        Offer offer = offerItem.getOffer().getValue();
        String s = MessageFormat.format("Angebot Nr. {0} vom {1}: {2}, Position {3}: {4}, Leistungserbringung am: {5}",
                NLS.toUserString(offer.getNumber()), NLS.toUserString(offer.getDate()), offer.getKeyword(), NLS.toUserString(offerItem.getPosition()),
            offerItem.getKeyword(), NLS.toUserString(offerItem.getAcceptanceDate()));
        return s;
    }

    @Override
    public Context prepareContext(Offer offer, String function) {
        // ToDO Pfad für scireum-logo auch bei Server richtig?
        Company company = offer.getCompany().getValue();
        LocalDate referenceDate = offer.getDate();
        Amount vatRate = getVatRateForCompany(company, referenceDate);

        String offerline = "";
        String headlinePrefix = "";
        List<OfferItem> offerItemList = null;
        Context context = new Context();

        //calculate priceNettoSum, priceVatSum and priceBruttoSum of these offerItems
        Amount priceNettoSum = Amount.ZERO;
        Amount priceBruttoSum = Amount.ZERO;
        Amount priceVatSum = Amount.ZERO;
        Amount cyclicPriceNettoSum = Amount.ZERO;
        Amount cyclicPriceBruttoSum = Amount.ZERO;
        Amount cyclicPriceVatSum = Amount.ZERO;
        Amount priceNettoSumBlock = Amount.ZERO;
        Amount cyclicPriceNettoSumBlock = Amount.ZERO;
        String offerState = "";
        String positions = "";

        switch (function) {
            default:
                return null;
            case OFFER:
                offerItemList = oma.select(OfferItem.class)
                        .eq(OfferItem.OFFER, offer)
                        .orderAsc(OfferItem.POSITION).queryList();
                context.set("validityPeriod", " Wir binden uns 30 Tage an dieses Angebot.") ;
                //remark = remark + " Die monatlichen Kosten werden zu Vertragsbeginn für das laufende Jahr und dann jährlich zum jeweiligen Jahresbeginn in Rechnung gestellt.";

                break;
            case SALES_CONFIRMATION:
                offerItemList = getConfirmationOfferItems(offer);
                context.set("validityPeriod", "");
                break;
        }

        offer.setLicenceItemPresent(false) ;
        offer.setServiceItemPresent(false) ;
        String licenceItemCyclicUnit = "";

        for (OfferItem item : offerItemList) {
            String nn = "";
            positions = positions + item.getPosition() + ", ";
            if(OfferItemType.SUM.equals(item.getOfferItemType())) {
               item.setPrice(priceNettoSumBlock);
               item.setCyclicPrice(cyclicPriceNettoSumBlock);
               priceNettoSumBlock = Amount.ZERO;
               cyclicPriceNettoSumBlock = Amount.ZERO;
            }   else {
                EntityRef<PackageDefinition> er = item.getPackageDefinition();
                if(er != null) {
                    PackageDefinition pd =  item.getPackageDefinition().getValue();
                    if(pd != null) {
                         nn = item.getPackageDefinition().getValue().getName();
                        int ggg = 1;
                    }
                }
                // singleprice
                Amount vatItem = Amount.ZERO;
                Amount bruttoPrice = Amount.ZERO;
                if(item.isService()) {
                    priceNettoSum = priceNettoSum.add(item.getPrice());
                    priceNettoSumBlock = priceNettoSumBlock.add(item.getPrice());
                    vatItem = item.getPrice();
                    bruttoPrice = item.getPrice();
                }
                if(item.isLicense()) {
                    Amount singlePrice = item.getSinglePrice();
                    if(singlePrice == null) {
                        singlePrice = Amount.ZERO;
                    }
                    singlePrice = singlePrice.times(item.getQuantity());
                    Amount discount = Amount.ZERO;
                    if(item.getDiscount() != null) {
                        discount =  item.getDiscount();
                    }
                    singlePrice = singlePrice.decreasePercent(discount);
                    item.setOfferSinglePrice(singlePrice);
                    priceNettoSum = priceNettoSum.add(singlePrice);
                    priceNettoSumBlock = priceNettoSumBlock.add(singlePrice);
                    vatItem = singlePrice;
                    bruttoPrice = singlePrice;
                }
                if(vatItem == null) {
                    vatItem = Amount.ZERO;
                }
                vatItem = vatItem.times(vatRate);
                vatItem = vatItem.divideBy(Amount.ONE_HUNDRED);
                priceVatSum = priceVatSum.add(vatItem);
                if(bruttoPrice == null) {
                    bruttoPrice = Amount.ZERO;
                }
                bruttoPrice = bruttoPrice.add(vatItem);
                priceBruttoSum = priceBruttoSum.add(bruttoPrice);

                //cyclicPrice
                if(item.isLicense()) {
                    cyclicPriceNettoSum = cyclicPriceNettoSum.add(item.getPrice());
                    cyclicPriceNettoSumBlock = cyclicPriceNettoSumBlock.add(item.getPrice());
                    vatItem = item.getPrice();
                    if(vatItem != null) {
                        vatItem = vatItem.times(vatRate);
                        vatItem = vatItem.divideBy(Amount.ONE_HUNDRED);
                    } else {
                        vatItem = Amount.ZERO;
                    }
                    cyclicPriceVatSum = cyclicPriceVatSum.add(vatItem);
                    bruttoPrice = item.getPrice();
                    bruttoPrice = bruttoPrice.add(vatItem);
                    cyclicPriceBruttoSum = cyclicPriceBruttoSum.add(bruttoPrice);
                }
            }
            String termsOfPayment = "";
            if(cyclicPriceNettoSum.isNonZero())  {
                termsOfPayment = "Die monatlichen Lizenz-Kosten werden zu Vertragsbeginn für das laufende Jahr und dann jährlich zum jeweiligen Jahresbeginn in Rechnung gestellt.";
            }
            termsOfPayment = termsOfPayment + "  Rechnungen sind innerhalb von 30 Tagen ohne Abzug zahlbar.";
            context.set("termsOfPayment", termsOfPayment);
            if(OfferItemState.CANCELED.equals(item.getState()))  {
                offerState = "annulliert";
            }
            if(OfferItemState.COPY.equals(item.getState()))  {
                offerState = "Kopie";
            }
            if(OfferItemType.SERVICE.equals(item.getOfferItemType())) {
                offer.setServiceItemPresent(true);
            }
            if(OfferItemType.LICENSE.equals(item.getOfferItemType())) {
                offer.setLicenceItemPresent(true);
                licenceItemCyclicUnit = item.getQuantityUnit();
            }

        }
        positions = positions.substring(0, positions.length()-2);
        context.set("positions", positions);
        String filePostfix = positions.replace(", ", "_");

        switch (function) {
            default:
                return null;
            case OFFER:
                offerline = "auf der Basis unserer Geschäftsbedingungen bieten wir Ihnen an:";
                context.set("filenamePDF", "Angebot_"+offer.getNumber()+".pdf");
                break;
            case SALES_CONFIRMATION:
                offerline = "wir danken für die Beauftragung der nachfolgend aufgeführten Positionen, die wir gerne auf der Basis unserer Geschäftsbedingungen bestätigen: ";
                headlinePrefix = "Auftragsbestätigung zum ";
                context.set("filenamePDF", "Auftragsbestaetigung_"+offer.getNumber()+"_"+filePostfix+".pdf");
                break;
        }

        //prepare the context
        String offerStateString = "";
        if(Strings.isFilled(offerState)) {
            offerStateString = ", Status: " + offerState + "!";
        }

        String dateString = NLS.toUserString(LocalDate.now());
        context.set("street", company.getAddress().getStreet());
        String city = company.getAddress().getZip() + " " +  company.getAddress().getCity();
        context.set("city", city);
        context.set("dateString", dateString);
        context.set("offerline", offerline);
        context.set("licenceItemCyclicUnit", licenceItemCyclicUnit);
        context.set("headlinePrefix", headlinePrefix);
//        context.set("salutation", offer.getPerson().getValue().getLetterSalutation());
        context.set("offerState", offerStateString);
        context.set("offer", offer);
        context.set("offerItemList", offerItemList);

        context.set("company", company);
        Person person = offer.getPerson().getValue();
        context.set("personName", person.getPerson().toString());
        context.set("personPhone", person.getContact().getPhone());
        context.set("personMail", person.getContact().getEmail());
        Person buyer = offer.getBuyer().getValue();
        if(buyer != null) {
            context.set("buyerPhone", buyer.getContact().getPhone());
            context.set("buyerName", buyer.getPerson().toString());
            context.set("buyerMail", buyer.getContact().getEmail());
        }

        Employee employee = offer.getEmployee().getValue().as(Employee.class) ;
        UserAccount user = offer.getEmployee().getValue().as(UserAccount.class) ;
        context.set("employeeName", user.toString());
//        context.set("employeePhone", employee.getPhoneNr());
        context.set("employeeMail", user.getEmail());
        context.set("priceBruttoSum", priceBruttoSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        context.set("priceNettoSum", priceNettoSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        context.set("priceVatSum", priceVatSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        if(vatRate != null) {
            context.set("vatRate", NLS.toUserString(vatRate.times(Amount.ONE_HUNDRED) + "%"));
        }
        context.set("cyclicPriceBruttoSum", cyclicPriceBruttoSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        context.set("cyclicPriceNettoSum", cyclicPriceNettoSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        context.set("cyclicPriceVatSum", cyclicPriceVatSum.toString(NumberFormat.TWO_DECIMAL_PLACES));
        context.set("isLicenceItemPresent", offer.isLicenceItemPresent()) ;
        return context;

        // TODO in offer.html umstellen: ${toolkit.nl2br(${toolkit.escapeXML($item.text)})}
    }

    @Override
    public List<OfferItem> getConfirmationOfferItems(Offer offer) {
        List<OfferItem> offerItemList;
        offerItemList = oma.select(OfferItem.class)
                .eq(OfferItem.OFFER, offer)
                .eq(OfferItem.STATE, OfferItemState.ORDERED)
                .eq(OfferItem.SALESCONFIRMATIONDATE, null)
                .orderAsc(OfferItem.POSITION).queryList();
        return offerItemList;
    }

    @Override
    public void updateOfferState(Offer offer, boolean save) {
        boolean open = false;
        boolean busy = false;
        boolean closed = false;
        List<OfferItem> oiList = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION)
                                    .queryList();
        for (OfferItem oi: oiList) {
            if(OfferItemState.OFFER.equals(oi.getState())) {open = true;}
            if(OfferItemState.ORDERED.equals(oi.getState())) {busy = true;}
            if(OfferItemState.CONFIRMED.equals(oi.getState())) {busy = true;}
            if(OfferItemState.DEVELOPED.equals(oi.getState())) {busy = true;}
            if(OfferItemState.ACCEPTED.equals(oi.getState())) {busy = true;}
            if(OfferItemState.ACCOUNTED.equals(oi.getState())) {closed = true;}
            if(OfferItemState.CANCELED.equals(oi.getState())) {closed = true;}
        }
        if(open && !busy && !closed) {offer.setState(OfferState.OPEN);}
        if(closed && !busy & !open) {offer.setState(OfferState.CLOSED); }
        if(busy) {offer.setState(OfferState.BUSY); }
        if(save) {
            oma.update(offer);
        }
    }

    @Override
     public void checkAllOfferItemsAreOffers(Offer offer) {
            List<OfferItem> offerItemList = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
                                               .orderAsc(OfferItem.POSITION).queryList();
            for (OfferItem oi : offerItemList) {
                if(!oi.isInfoText()) {
                    if (!(OfferItemState.OFFER.equals(oi.getState()))) {
                        String text = MessageFormat.format(
                                "Die Angebotsposition Nr. {0} des Angebots Nr. {1} hat den Status ''{2}'' und kann deshalb nicht weiterverarbeitet werden--> Abbruch",
                                oi.getPosition(), oi.getOffer().getValue().getNumber(), oi.getState().toString());
                        throw Exceptions.createHandled().withNLSKey("OfferItem.notAllOffers")
                                        .set("pos", oi.getPosition()).set("offerNr", offer.getNumber())
                                .set("state", oi.getState()).handle();
                    }
                }
            }
     }

    @Override
    public int sendSalesConfirmation(/* View view, */ Offer offer) {
        List<OfferItem> confirmationList = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
                .eq(OfferItem.STATE, OfferItemState.CONFIRMED)
                .eq(OfferItem.SALESCONFIRMATIONDATE, null).orderAsc(OfferItem.POSITION).queryList();
        if(confirmationList == null || confirmationList.size() <= 0) {
          throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.salesConfirmationNoOrders")
                  .set("number", offer.getNumber()).handle();
        }
        int mailCounter = 0;
//      Date confimationDate = new Date();    // ToDo ConfirmationDate erst setzen, wenn Mail gesendet wurde
        Person person = offer.getPerson().getValue();
        String mailAdress = person.getContact().getEmail();
        if(Strings.isFilled(mailAdress)) {
            Context context = prepareContext(offer, ServiceAccountingService.SALES_CONFIRMATION) ;
            File file = createPdfFromContext(context, "templates/offer.pdf.vm");
// ToDo send file

            mailCounter++;
        } else {
            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.salesConfirmationNoMailAdr")
                    .set("person", person.getPerson().getAddressableName()).handle();
        }
        return mailCounter;
    }



    @Override
    public void sendOffer(Offer offer)  {
//      Date confimationDate = new Date();    // ToDo ConfirmationDate erst setzen, wenn Mail gesendet wurde
        Person person = offer.getPerson().getValue();
        String mailAdress = person.getContact().getEmail();
        if(Strings.isFilled(mailAdress)) {
            Context context = prepareContext(offer, ServiceAccountingService.OFFER) ;
            File file = createPdfFromContext(context, "templates/offer.pdf.vm");
// ToDo send file

        } else {
            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.salesConfirmationNoMailAdr")
                            .set("person", person.getPerson().getAddressableName()).handle();
        }
    }

    @Override
    public List<PackageDefinition> getAllPackageDefinitions(Object object) {
        if(object.getClass().isInstance(OfferItem.class)) {
            OfferItem offerItem = (OfferItem) object;
            Product baseProduct = offerItem.getBaseProduct().getValue();
            List<PackageDefinition> pdList = oma.select(PackageDefinition.class)
                                                .eqIgnoreNull(PackageDefinition.PRODUCT, baseProduct).queryList();
            return pdList;
        }
        if(object.getClass().isInstance(Contract.class)) {
            Contract contract = (Contract) object;
            List<PackageDefinition> pdList = oma.select(PackageDefinition.class).queryList();
            return pdList;
        }
        return null;
    }

    private File createPdfFromContext(Context context, String templateName) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        String filenamePdf = (String) context.get("filenamePDF");
        File file = new File(filenamePdf);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream (new File(filenamePdf));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            templates.generator().useTemplate(templateName).applyContext(context).generateTo(baos);
            baos.writeTo(fos);
        } catch(Exception ioe) {
            // Handle exception here
            Exceptions.handle(ioe);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
               Exceptions.handle(e);
            }
        }
        return file;
    }

    @Override
    public void copyOffer(Offer offer, boolean reCreate) {
        List<OfferItem> offerItemList = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION)
                                    .queryList();
        if(reCreate) {
            // check whether the offerItem-states are == OFFER
            checkAllOfferItemsAreOffers(offer);
        }
        // create the new offer and copy
        Offer newOffer = new Offer();

        newOffer.setKeyword(offer.getKeyword());
        newOffer.getCompany().setId(offer.getCompany().getValue().getId());
        newOffer.getPerson().setId(offer.getPerson().getId());
        UserAccount user = offer.getEmployee().getValue().as(UserAccount.class) ;
        newOffer.getEmployee().setId(user.getId());
        newOffer.getBuyer().setId(offer.getBuyer().getId());
        newOffer.setReference(offer.getReference());
        newOffer.setDate(LocalDate.now());
        newOffer.setState(offer.getState());
        // save the new offer
        oma.update(newOffer);

        //create the new offerItems and copy
        for(OfferItem o :offerItemList) {
            OfferItem newOfferitem = new OfferItem();
            newOfferitem.setState(OfferItemState.COPY);
            newOfferitem.setKeyword(o.getKeyword());
            newOfferitem.getBaseProduct().setId(o.getBaseProduct().getId());
            newOfferitem.getPackageDefinition().setId(o.getPackageDefinition().getId());
            newOfferitem.getOffer().setId(newOffer.getId());
            newOfferitem.setDiscount(o.getDiscount());
            newOfferitem.setOfferItemType(o.getOfferItemType());
            newOfferitem.setPosition(o.getPosition());
            newOfferitem.setPrice(o.getPrice());
            newOfferitem.setPriceBase(o.getPriceBase());
            newOfferitem.setQuantity(o.getQuantity());
            newOfferitem.setQuantityUnit(o.getQuantityUnit());
            newOfferitem.setSinglePrice(o.getSinglePrice());
            newOfferitem.setText(o.getText());
            newOfferitem.setHistory(MessageFormat.format("*** kopiert von Angebot {0} ***",offer.getNumber()));
            // save the new offerItem
            oma.update(newOfferitem);

            if(reCreate) {
                // modify the old offerItem and save it
                o.setState(OfferItemState.CANCELED);
                String s = "*** ersetzt durch Angebot" + newOffer.getNumber() + " ***";
                s = s + "<br>" + o.getHistory();
                o.setHistory(s);
                oma.update(o);
            }
        }
    }

    @Override
    public Amount getProdUmsatz() {
        return prodUmsatz;
    }

    @Override
    public Amount getTestUmsatz() {
        return testUmsatz;
    }

    @Override
    public LocalDate getAccountingDate() {
        return accountingDate;
    }

    @Override
    public void exportInvoiceItemsToCollmex() {
        // get all new invoiceItems
        List<Lineitem> invoiceItemList = oma.select(Lineitem.class)
                .eq(Lineitem.STATUS, Lineitem.LINEITEMSTATUS_NEW)
                .eq(Lineitem.LINEITEMTYPE, Lineitem.LINEITEMTYPE_OA)
               .orderDesc(Lineitem.INVOICENR)
                .orderAsc(Lineitem.POSITION).queryList();
        if (invoiceItemList.size() <= 0) {
            //ToDo Rechtsnachfolger für ss.orBackendStream(
//                    DisplayMarkdownFactory.FACTORY_NAME,
//            ss.forBackendStream(
//                    DisplayMarkdownFactory.FACTORY_NAME,
//                    "Service-Abrechnung",
//                    MessageFormat
//                            .format("In der Tabelle 'lineitem' wurden keine lineitems mit dem Typ {0} zum exportieren gefunden",
//                                    LineitemType.OA))
//                    .loginRequired(true).setUser(Users.getCurrentUser())
//                    .publish();

        }
        // export them to the file
        File file = as .createCsvFilename("invoiceItem",-1);
        try {
            FileOutputStream output = new FileOutputStream(file);
            Writer fw = new OutputStreamWriter(output, "ISO_8859_1");
            PrintWriter pw = new PrintWriter(fw);
            Amount sum = Amount.ZERO;
            try {
                int i = 0;
                for (Lineitem invoiceItem : invoiceItemList) {
                    i++;
                    sum = sum.add(generateCollmexInvoiceLine(pw, invoiceItem, null));
                }
                pw.flush(); // flush the printwriter to get all data to the file
                // build a activity-news
                String text = MessageFormat.format(
                        "{0} Service-Rechnungspositionen exportiert, Netto-Umsatz: {1} EUR, Datei: {2}",
                        NLS.toUserString(i),
                        NLS.toUserString(sum),
                        file.getAbsoluteFile());
                // ToDo  ss.forBackendStream(
//                ss.forBackendStream(
//                        DisplayMarkdownFactory.FACTORY_NAME,
//                        "Service-Abrechnung",text)
//                        .loginRequired(true).setUser(Users.getCurrentUser())
//                        .publish();
            } finally {
                pw.close();
            }
        } catch (Exception e) {
            UserContext.handle(e);
        }

    }

    /**
     * generates a invoice-position in Collmex based on the given invoiceitem
     * @param pw  printwriter
     * @param invoiceItem
     * @param invoiceDate
     * @return sum = quantity * (price * (1-discount))
     */
    private Amount generateCollmexInvoiceLine(PrintWriter pw,
                                              Lineitem invoiceItem, Calendar invoiceDate) {
        Amount sum = Amount.ZERO;

        // step 2: generate a csv-line for the export in Collmex-Notation
        final int csvLae = 82; // field #1 - #82
        String[] csv = new String[csvLae + 1]; // csv[0] - csv[82],
        // csv[0] is not used
        csv[1] = "CMXINV"; // Satzart C Festwert CMXINV
        csv[2] = NLS.toUserString(invoiceItem.getInvoiceNr()); // Rechnungsnummer I
        // 8 Die
        // Rechnungsnummer
        // identifiziert die Rechnung eindeutig. Siehe auch Nummernvergabe.
        // 03 = Position I 8 Positionsnummer der Rechnungsposition. Wenn nicht
        // angegeben, wird die Positionsnummer automatisch fortlaufend vergeben.

        csv[4] = "0"; // 04 = Rechnungsart I 8, // 0 = Rechnung , 1 = Gutschrift
        // 2 = Abschlagsrechnung, 3 = Barverkauf
        csv[5] = "1"; // 05 = Firma Nr I 8 Interne Nummer der Firma, wie unter
        // Verwaltung -> Firma anzeigen und ändern angezeigt.
        // 06 = Auftrag Nr I 8 Nummer des Kundenauftrags, auf den sich die
        // Rechnung bezieht.
        csv[7] = NLS.toUserString(invoiceItem.getCustomerNr()); // 07 = Kunden-Nr I
        // 8
        // Der Kunde muss in Collmex existieren.
        // Referenz ausschliesslich über die Kundennummer
        // 08 Anrede C 10 Felder 8 - 27 ist die Kundenadresse. Nur für den
        // Export. Die Felder werden beim Import ignoriert.
        // 09 Titel C 10
        // 10 Vorname C 40
        // 11 Name C 40
        // 12 Firma C 40
        // 13 Abteilung C 40
        // 14 Strasse C 40
        // 15 PLZ C 10
        // 16 Ort C 20
        // 17 Land C 2 ISO Codes
        // 18 Telefon C 20
        // 19 Telefon2 C 20
        // 20 Telefax C 20
        // 21 E-Mail C 50
        // 22 Kontonr C 20
        // 23 Blz C 20
        // 24 Abweichender Kontoinhaber C 30
        // 25 IBAN C 20
        // 26 BIC C 20
        // 27 Bank C 20
        // 28 = USt.IdNr C 20
        csv[29] = "0"; // Privatperson I8, 0 = keine Privatperson , 1 =
        // Privatperson
        if (invoiceDate == null) { // 30 // Rechnungsdatum
            csv[30] = NLS.toUserString(invoiceItem.getLineitemDate());
        } else {
            csv[30] = NLS.toUserString(invoiceDate);
        }
        // D 8 Falls nicht angegeben, wird das aktuelle Datum gesetzt.
        // 31 Preisdatum D 8 Falls nicht angegeben, wird das Rechnungsdatum
        // gesetzt.
        // csv[32] = "0"; // Zahlungsbedingung I 8 Als Zahl codiert, beginnend
        // mit
        // 0 für 30T ohne Abzug, wie im Programm unter
        // "Einstellungen". Falls nicht angegeben, wird die
        // Zahlungsbedingung vom Kunden übernommen.
        // 33 Währung (ISO-Codes) C 3 Falls nicht angegeben, wird die Währung
        // vom Kunnden übernommen.
        // 34 Preisgruppe I 8 Interne Nummer der Preisgruppe, wie sie bei der
        // Pflege der Preisgruppe in der ersten Spalte angezeigt wird. Falls
        // nicht angegeben, wird die Preisgruppe vom Kunden übernommen.
        // 35 Rabattgruppe I 8 Interne Nummer der Rabattgruppe, wie sie bei der
        // Pflege der Rabattgruppe in der ersten Spalte angezeigt wird. Falls
        // nicht angegeben, wird die Rabattgruppe vom Kunden übernommen.
        // 36 Schluss-Rabatt I 8 Schluss-Rabatt in Prozent.
        // 37 Rabattgrund C 255 Grund für den Rabatt.
        // 38 Rechnungskoptext

        // csv[39] = COLLMEX_NULL; // 39 Schlusstext C 1024 Falls (NULL), wird
        // der Text aus den Standard-Textbausteinen ermittelt.

        csv[40] = "Position am " + NLS.toUserString(LocalDateTime.now())
                + " exportiert"; // 40 Internes Memo C 1024
        csv[41] = "0"; // 41 Gelöscht I 8 0 = nicht gelöscht, 1 = gelöscht.
        csv[42] = "0"; // 42 Sprache I 8 0 = Deutsch, 1 = Englisch.
        // 43 Bearbeiter I 8 Mitarbeiternummer des Bearbeiters. Falls nicht
        // angegeben, wird der Bearbeiter automatisch bestimmt.
        // 44 Vermittler I 8 Mitarbeiter Nummer des Vermittlers für die
        // Provisionsabrechnung.
        // 45 Systemname C 20 Name des externen Systems, das den Auftrag
        // angelegt hat
        // 46 Status I 8 Nur für Export. 0 = neu, 10 = Zu buchen, 20 = offen, 30
        // = gemahnt, 40 = erledigt, 100 = gelöscht
        // csv[47] = NLS.toUserString(lineitem.getFinalDiscountSum()); // kein
        // Schluss-Rabatt 20.2.2012
        // 47 Schluss-Rabatt 2, M 18, Schluss-Rabatt als Betrag.
        // 48 Schluss-Rabatt-2-Grund C 20 Grund für den Rabatt.
        // 49 Versandart I 8 Interne Nummer der Versandart, wie sie bei der
        // Pflege der Versandart angezeigt wird. Falls nicht angegeben, wird die
        // Versandart automatisch bestimmt über die Versandgruppen der im
        // Auftrag enthaltenen Produkte (nur Collmex pro).
        // 50 Versandkosten M 18 Falls nicht angegeben, werden die Versandkosten
        // automatisch über die Versandart bestimmt.
        // 51 Nachnahmegebühr M 18 Falls nicht angegeben, wird die
        // Nachnahmegebühr automatisch über die Versandart bestimmt.
        // 52 Lieferdatum D 8 Optional.
        // 53 Lieferbedingung C 3 International genormte INCOTERMS. Falls nicht
        // angegeben, wird die Lieferbedingung aus der Versandart übernommen.
        // 54 Lieferbedingung Zusatz C 40 Ort der Lieferbedingung
        // 55 Anrede C 10 Felder 46 - 58: Optional. Abweichende Lieferadresse.
        // 56 Titel C 10
        // 57 Vorname C 40
        // 58 Name C 40
        // 59 Firma C 40
        // 60 Abteilung C 40
        // 61 Strasse C 40
        // 62 PLZ C 10
        // 63 Ort C 20
        // 64 Land C 2 ISO Codes
        // 65 Telefon C 20
        // 66 Telefon2 C 20
        // 67 Telefax C 20
        // 68 E-Mail C 50
        csv[69] = NLS.toUserString(invoiceItem.getPositionType());
        // Positionstyp I 8
        // 0 = Normalposition,
        // 1 = Summenposition,
        // 2 = Textposition,
        // 3 = Kostenlos.
        csv[70] = invoiceItem.getArticle();// Produktnummer C 20 Falls nicht
        // angegeben, muss die
        // Produktbeschreibung gefüllt sein. Die Produktnummer muss in Collmex
        // existieren.
        csv[71] = invoiceItem.getDescription();// 71 Produktbeschreibung C
        // 10.000
        // Falls nicht angegeben, wird die
        // Beschreibung aus dem Produkt
        // übernommen.
        String measurement =  invoiceItem.getMeasurement();
        if("PT".equals(measurement)) {
            measurement= "DAY";    // Anpassung PT --> DAY wg. Collmex
        }
        csv[72] = measurement; // 72 Mengeneinheit C 3 ISO Codes.

        // DAY = Personentag
        // PCE = Stück,
        // MON = Monat, Falls nicht
        // angegeben, wird die Mengeneinheit vom in Collmex gespeicherten Produkt
        // übernommen.
        csv[73] = NLS.toUserString(invoiceItem.getQuantity()); // 73 Menge N 18
        // Auftragsmenge
        csv[74] = NLS.toUserString(invoiceItem.getPrice());
        // Einzelpreis M 18 Falls nicht angegeben, wird / der
        // Preis über das Produkt bestimmt.
        csv[75] = NLS.toUserString(invoiceItem.getQuantity()); // Preismenge N 18
        // Falls
        // nicht angegeben, wird
        // die Preismenge über
        // das Produkt bestimmt
        // bzw. auf 1 gesetzt.
        csv[76] = NLS.toUserString(invoiceItem.getPositionDiscount());// 76
        // Positionsrabatt
        // M 18
        // Positionsrabatt
        // in
        // Prozent
        // mit
        // zwei Nachkommastellen.
        // 77 Positionswert M 18 Nur für Export. Beim Import wird der
        // Positionswert automatisch aus Preis und Rabatt berechnet.
        csv[78] = "0"; // 78 Produktart I 18 Wird beim Import nur verwendet,
        // wenn kein Produkt gesetzt ist. 0 = Ware, 1 =
        // Dienstleistung, 2 = Mitgliedsbeitrag (nur Collmex
        // Verein), 3 = Baudienstleistung
        csv[79] = "0";// 79 Steuerklassifikation I 8 Wird beim Import nur
        // verwendet, wenn kein Produkt gesetzt ist. Andernfalls
        // wird die Steuerklassifikation aus dem Produkt
        // ermittelt. 0 = voller Steuersatz, 1 = halber
        // Steuersatz, 2 = steuerfrei.
        csv[80] = "0"; // 80 Steuer auch im Ausland I 8 Wird beim Import nur
        // verwendet, wenn kein Produkt gesetzt ist und die
        // Produktart eine Dienstleistung ist. 0 = Sonstige
        // Leistung ist im Inland nicht steuerbar bei
        // Leistungsort im Ausland, 1 = Sonstige Leistung ist im
        // Inland steuerbar auch bei Leistungsort im Ausland.
        csv[81] = "0";// 81 Kundenauftragsposition I 8 Position des
        // Kundenauftrags, auf das sich die Rechnungsposition
        // bezieht.
        csv[82] = "0"; // Erlösart I 8 Nur für Export. 0 = voller Steuersatz

        // Step 3: build the CSV-string from the array 'csv'
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= csvLae; i++) {
            if (csv[i] != null) {
                sb.append(csv[i]);
            }
            sb.append(";");
        }
        // Step 4: print the CSV-string to the file
        pw.println(sb.toString());

        // Step 5: set status and the clearingDate
        invoiceItem.setStatus(Lineitem.LINEITEMSTATUS_ACCOUNTED);
        invoiceItem.setClearingDate(LocalDateTime.now());
        oma.update(invoiceItem);
        // Step 6: calculate the position-sum
        sum = invoiceItem.getPrice().times(invoiceItem.getQuantity());
        if(invoiceItem.getPositionDiscount() != null) {
            sum = sum.decreasePercent(invoiceItem.getPositionDiscount());
        }
        // System.err.println(invoiceItem.getCompanyName() + "   " + NLS.toUserString(sum));
        return sum;

    }
    private Amount getVatRateForCompany(Company company, LocalDate referenceDate)  {

        String countryCode = company.getAddress().getCountry();
        if(Strings.isEmpty(countryCode)) {
          throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.countryCodeMissing")
                    .set("company", company.toString()).handle();
        }
        if(referenceDate == null) {
            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.referenceDateMissing")
                            .handle();
        }
        List<VatRate> vatRateList = oma.select(VatRate.class).eq(VatRate.COUNTRYCODE, countryCode)
                                       .orderDesc(VatRate.VALIDFROM).queryList();
        for(VatRate vr: vatRateList) {
            if(!vr.getValidFrom().isAfter(referenceDate)) {
                return vr.getVatRate();
            }
        }
        return null;
    }


}
