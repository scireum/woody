/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.DataCollector;
import sirius.kernel.commons.NumberFormat;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.http.MimeHelper;
import sirius.web.mails.Mails;
import sirius.web.security.UserContext;
import sirius.web.templates.Templates;
import woody.core.employees.Employee;
import woody.sales.AccountingIntervalType;
import woody.sales.AccountingService;
import woody.sales.Contract;
import woody.sales.ContractSinglePriceType;
import woody.sales.Lineitem;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.xrm.Company;
import woody.xrm.Person;

import javax.activation.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gerhardhaufler on 23.12.15.
 */
@Register(classes = ServiceAccountingService.class)
public class ServiceAccountingServiceBean implements ServiceAccountingService {

    private Amount prodUmsatz = null;
    private Amount testUmsatz = null;

    // ToDo Rechtsnachfolger für SocialService
//    @Part
//    private SocialService ss;
    @Part
    private static AccountingService as;

    @Part
    private static ServiceAccountingService sas;

    @ConfigValue(value = "mail.scireumSupportMailAddress", required = true)
    private String scireumSupportMailAddress = "support@scireum.de";

    @Part
    private Mails mails;

    @Part
    protected OMA oma;

    @Part
    private static Templates templates;

    @Override
    public OfferItemState getNextState(OfferItem oi) {
        if (OfferItemState.OFFER.equals(oi.getState())) {
            return OfferItemState.ORDERED;
        }       // ORDERED follows OFFER
//        if(OfferItemState.ORDERED.equals(oi.getState()))  {return OfferItemState.CONFIRMED; }   // CONFIRMED follows ORDERED
        if (OfferItemState.CONFIRMED.equals(oi.getState())) {
            return OfferItemState.DEVELOPED;
        } // DEVELOPED follows CONFIRMED
        if (OfferItemState.DEVELOPED.equals(oi.getState())) {
            return OfferItemState.ACCEPTED;
        }  // ACCEPTED follows DEVELOPED
        return null;
    }

    @Override
    public boolean isOfferItemVisible(OfferItem oi, OfferItemState targetState) {
        OfferItemState nextState = getNextState(oi);
        if (nextState == null) {
            return false;
        }         // null = no next step ---> not visible
        if (targetState.equals(nextState)) {
            return true;
        }
        return false;
    }

    @Override
    public DataCollector<Lineitem> accountAllServiceOffers(final boolean dryRun /* , TaskMonitor monitor*/) {
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
        LocalDateTime accountingDate = LocalDateTime.now();
        testUmsatz = null;
        prodUmsatz = null;
        String modus = "???";
        if (dryRun) {
            testUmsatz = Amount.ZERO;
            modus = "TEST";
        } else {
            prodUmsatz = Amount.ZERO;
            modus = "PRODUKTIV";
        }

        System.err.println("Start Service-Abrechnung im Modus: " + modus);

        //Long invoiceNr = as.getMinInvoiceNr();
        Long invoiceNr = 0L;

        // get a list with all offers
        List<Offer> offerList = oma.select(Offer.class).orderAsc(Offer.NUMBER).queryList();

        // process this List
        for (Offer offer : offerList) {
            invoiceNr--;
            System.out.println(offer.toString());
            //get a list with all positions from this offer
            List<OfferItem> offerItemList =
                    oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
            for (OfferItem offerItem : offerItemList) {
//                System.out.println(offerItem.toString());
//                oma.update(offerItem);   // Migration
                if (!OfferItemType.SERVICE.equals(offerItem.getOfferItemType())) {
                    continue;
                }

                if (OfferItemState.ACCEPTED.equals(offerItem.getState())) {
                    //this position is accepted by the customer --> account it
                    // System.err.println(offer.toString()+"    " + offerItem.toString() + "");
                    Lineitem lineitem = generateLineitem(referenceDate, invoiceNr, offer, offerItem, accountingDate);
                    // calculate the sum-values
                    Amount quantity = offerItem.getQuantity().fill(Amount.ONE);
                    Amount price = quantity.times(offerItem.getSinglePrice());
                    Amount discount = offerItem.getDiscount().fill(Amount.ZERO);
                    price = price.decreasePercent(discount);

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
            if (lineitem.getPositionDiscount() != null && lineitem.getPositionDiscount().isPositive()) {
                price = price.decreasePercent(lineitem.getPositionDiscount());
            }
            summe = summe.add(price);
        }

        String text = MessageFormat.format(
                "Service-Abrechnung zum Referenzdatum {0} im Modus: {1} erstellt, {2} Rechnungspositionen, Netto-Umsatz: {3} EUR",
                NLS.toUserString(referenceDate),
                modus,
                NLS.toUserString(counter),
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
        System.err.println(text);

        return itemCollector;
    }

    /**
     * generates a lineitem for account a service -item based on a offeritem with te given data
     *
     * @param referenceDate
     * @param invoiceNr
     * @param offer
     * @param offerItem
     * @return
     */
    private Lineitem generateLineitem(LocalDate referenceDate,
                                      long invoiceNr,
                                      Offer offer,
                                      OfferItem offerItem,
                                      LocalDateTime accountingDate) {

//        Company company = offer.getCompany().getValue();
//        String customerNr = company.getCustomerNr();
//        if (Strings.isEmpty(customerNr)) {
//            // no accounting without a customerNr!!!
//            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.customerNrMissing")
//                            .set("company", company.getName()).handle();
//        }
//
//        Lineitem lineitem = new Lineitem();
//        lineitem.setCustomerNr(customerNr);
//        String outputLanguage = company.getCompanyAccountingData().getOutputLanguage();
//        lineitem.setOutputLanguage(outputLanguage);
//        lineitem.setFinalDiscountSum(Amount.ZERO); // finalDiscountSum is not used
//        lineitem.setFinalDiscountAmount(Amount.ZERO); // finalDiscountAmount is not used
//        lineitem.setCollmexCredit(false);
//        lineitem.setCredit(false);
//        lineitem.setLineitemType(Lineitem.LINEITEMTYPE_OA);
//        lineitem.setAccountingDate(accountingDate);
//        lineitem.setExportDate(accountingDate);
//        lineitem.setReferenceDate(referenceDate);
//        lineitem.setPrice(offerItem.getSinglePrice());
//        lineitem.setInvoiceNr(invoiceNr);
//        lineitem.setStatus(Lineitem.LINEITEMSTATUS_NEW);
//
//        lineitem.setCompanyName(offer.getCompany().getValue().getName());
//        // ToDo testen ob das zu collmex passt
//        String s =  offerItem.getAccountingUnitComplete();
//        if(s == null || s.isEmpty()) {
//            int ggg = 1;
//        }
//        lineitem.setMeasurement(offerItem.getAccountingUnitComplete());
//
//        lineitem.setPackageName(offerItem.getPackageDefinition().getValue().getName());
//        lineitem.setQuantity(offerItem.getQuantity().fill(Amount.ONE));
//        lineitem.setDescription(makeDescription(offerItem));
//        lineitem.setPosition(offerItem.getPosition());
//        Product product = offerItem.getPackageDefinition().getValue().getProduct().getValue();
//        lineitem.setArticle(product.getArticle());
//        Amount discount = offerItem.getDiscount();
//        lineitem.setPositionDiscount(discount);
//        if (offerItem.isInfoText() ) {
//            lineitem.setPositionType(lineitem.getLineitemCollmexTextposition());
//        } else {
//            lineitem.setPositionType(lineitem.getLineitemCollmexNormalposition());
//        }
//        return lineitem;
        return null;
    }

    /**
     * makes the description-String for a lineitem based on the given offerItem
     *
     * @param offerItem
     * @return String for a lineitem-description
     */
    private String makeDescription(OfferItem offerItem) {

        Offer offer = offerItem.getOffer().getValue();
        String s = MessageFormat.format("Angebot Nr. {0} vom {1}: {2}, Position {3}: {4}, Leistungserbringung am: {5}.",
                                        NLS.toUserString(offer.getNumber()),
                                        NLS.toUserString(offer.getDate()),
                                        offer.getKeyword(),
                                        NLS.toUserString(offerItem.getPosition()),
                                        offerItem.getKeyword(),
                                        NLS.toUserString(offerItem.getAcceptanceDate()));
        s = checkDiscounts(s, offerItem);
        return s;
    }

    private String checkDiscounts(String description, OfferItem offerItem) {
        if (offerItem.getDiscount() != null && offerItem.getDiscount().isPositive()) {
            Amount discount = offerItem.getQuantity().times(offerItem.getDiscount());
            discount = discount.divideBy(Amount.ONE_HUNDRED);
            discount = discount.times(offerItem.getSinglePrice());
            description += MessageFormat.format(" Es wird ein Rabatt von {0}% = {1} EUR berücksichtigt.",
                                                NLS.toUserString(offerItem.getDiscount()),
                                                NLS.toUserString(discount));
        }
        return description;
    }

    @Override
    public Context prepareContext(Offer offer, String function) {
        // ToDO Pfad für scireum-logo auch bei Server richtig?
        List<OfferItem> offerItemList = null;
        Context context = new Context();
        String offerState = "";
        String positions = "";
        String mailText = "";
        Amount priceNettoSum = Amount.ZERO;
        Amount priceBruttoSum = Amount.ZERO;
        Amount priceVatSum = Amount.ZERO;
        Amount cyclicPriceNettoSum = Amount.ZERO;
        Amount cyclicPriceBruttoSum = Amount.ZERO;
        Amount cyclicPriceVatSum = Amount.ZERO;
        Amount priceNettoSumBlock = Amount.ZERO;
        Amount cyclicPriceNettoSumBlock = Amount.ZERO;
        Amount payMaintenanceSum = Amount.of(0);
        Amount cyclicPriceNettoSumPositiv = Amount.of(0);
        String licenceItemCyclicUnit = "";
        String offerline = "";
        String headlinePrefix = "";
        Amount vatRate = Amount.NOTHING;
        LocalDate referenceDate = null;
        Company company = null;
        String subject = "";
        String filePostfix = "";
        String offerStateString = "";
        boolean isCompanyVAT = false;
        switch (function) {
            default:
                break;
            case OFFER:
            case SALES_CONFIRMATION:

                company = offer.getCompany().getValue();
                referenceDate = offer.getDate();
                vatRate = getVatRateForCompany(company, referenceDate);
                if(vatRate.isPositive()) {
                    isCompanyVAT = true;
                }
                break;
        }
        switch (function) {
            default:
                break;
            case OFFER:
                offerItemList =
                        oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
                context.set("validityPeriod", " Wir binden uns 30 Tage an dieses Angebot.");
                //remark = remark + " Die monatlichen Kosten werden zu Vertragsbeginn für das laufende Jahr und dann jährlich zum jeweiligen Jahresbeginn in Rechnung gestellt.";

                break;
            case SALES_CONFIRMATION:
                offerItemList = getConfirmationOfferItems(offer);
                context.set("validityPeriod", "");
                break;
        }

        switch (function) {
            default:
                break;
            case OFFER:
            case SALES_CONFIRMATION:
                offer.setLicenceItemPresent(false);
                offer.setServiceItemPresent(false);

                boolean isPayMaintenacePresent = false;

                for (OfferItem item : offerItemList) {
                    String nn = "";
                    positions = positions + item.getPosition() + ", ";
                    if (OfferItemType.SUM.equals(item.getOfferItemType())) {
                        item.setSinglePrice(priceNettoSumBlock);
                        item.setCyclicPrice(cyclicPriceNettoSumBlock);
                        priceNettoSumBlock = Amount.ZERO;
                        cyclicPriceNettoSumBlock = Amount.ZERO;
                    } else {
                        EntityRef<PackageDefinition> er = item.getPackageDefinition();
                        if (er != null) {
                            PackageDefinition pd = item.getPackageDefinition().getValue();
                            if (pd != null) {
                                nn = item.getPackageDefinition().getValue().getName();
                                int ggg = 1;
                            }
                        }
                        // singleprice
                        Amount vatItem = Amount.ZERO;
                        Amount bruttoPrice = Amount.ZERO;
                        Amount singlePricePosition = item.getSinglePrice().fill(Amount.ZERO);
                        Amount discount = item.getDiscount().fill(Amount.ZERO);
                        if (item.isService()) {
                            Amount price = singlePricePosition.times(item.getQuantity());
                            price = price.decreasePercent(discount);
                            vatItem = price;
                            item.setSinglePricePosition(price);
                            bruttoPrice = price;
                            priceNettoSum = priceNettoSum.add(price);
                            priceNettoSumBlock = priceNettoSumBlock.add(price);
                        }
                        if (item.isLicense()) {
                            singlePricePosition = singlePricePosition.times(item.getQuantity());
                            singlePricePosition = singlePricePosition.decreasePercent(discount);
                            item.setSinglePricePosition(singlePricePosition);
                            priceNettoSum = priceNettoSum.add(singlePricePosition);
                            priceNettoSumBlock = priceNettoSumBlock.add(singlePricePosition);
                            vatItem = singlePricePosition;
                            bruttoPrice = singlePricePosition;
                        }
                        vatItem = vatItem.times(vatRate).divideBy(Amount.ONE_HUNDRED);
                        priceVatSum = priceVatSum.add(vatItem);
                        bruttoPrice = bruttoPrice.add(vatItem);
                        priceBruttoSum = priceBruttoSum.add(bruttoPrice);

                        //cyclicPrice
                        if (item.isLicense()) {
                            Amount cyclicPricePosition = item.getCyclicPrice().fill(Amount.ZERO);
                            cyclicPricePosition = cyclicPricePosition.times(item.getQuantity());
                            cyclicPricePosition = cyclicPricePosition.decreasePercent(discount);
                            item.setCyclicPricePosition(cyclicPricePosition);
                            cyclicPriceNettoSum = cyclicPriceNettoSum.add(cyclicPricePosition);
                            cyclicPriceNettoSumBlock = cyclicPriceNettoSumBlock.add(cyclicPricePosition);
                            vatItem = cyclicPricePosition;
                            vatItem = vatItem.times(vatRate).divideBy(Amount.ONE_HUNDRED);
                            cyclicPriceVatSum = cyclicPriceVatSum.add(vatItem);
                            bruttoPrice = cyclicPricePosition;
                            bruttoPrice = bruttoPrice.add(vatItem);
                            cyclicPriceBruttoSum = cyclicPriceBruttoSum.add(bruttoPrice);
                            if(cyclicPricePosition.isPositive()) {
                                cyclicPriceNettoSumPositiv = cyclicPriceNettoSumPositiv.add(cyclicPricePosition);
                            }
                        }
                    }
                    if (OfferItemState.CANCELED.equals(item.getState())) {
                        offerState = "annulliert";
                    }
                    if (OfferItemState.COPY.equals(item.getState())) {
                        offerState = "Kopie";
                    }
                    if (OfferItemType.SERVICE.equals(item.getOfferItemType())) {
                        offer.setServiceItemPresent(true);
                    }
                    if (OfferItemType.LICENSE.equals(item.getOfferItemType())) {
                        offer.setLicenceItemPresent(true);
                        licenceItemCyclicUnit = item.getTranslatedAccountingUnit();
                    }
                    if(OfferItemType.LICENSE.equals(item.getOfferItemType()) ||
                       OfferItemType.SERVICE.equals(item.getOfferItemType())) {
                        if (item.isPayMaintenance()) {
                            isPayMaintenacePresent = true;
                            payMaintenanceSum = payMaintenanceSum.add(item.getPayMaintenancePrice());
                        }
                    }

                    // speichern
                    oma.update(item);
                }

                context.set("isPayMaintenacePresent",isPayMaintenacePresent);
                context.set("payMaintenanceSumString", payMaintenanceSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("payMaintenanceSumYearString", payMaintenanceSum.times(Amount.of(12)).toString(NumberFormat.TWO_DECIMAL_PLACES).asString());


                positions = positions.substring(0, positions.length() - 2);
                context.set("positions", positions);
                filePostfix = positions.replace(", ", "_");

                String termsOfPayment = "";
                if (cyclicPriceNettoSum.isNonZero()) {
                    termsOfPayment =
                            "Die monatlichen Lizenz-Kosten werden zu Vertragsbeginn für das laufende Jahr und dann jährlich zum jeweiligen Jahresbeginn in Rechnung gestellt.";
                }
                termsOfPayment = termsOfPayment
                                 + " Das monatliche Supportkontingent entspricht 30 Prozent der monatlichen Lizenzgebühr, ein Personentag sind zurzeit 800,00 EUR.";
                termsOfPayment = termsOfPayment + "  Rechnungen sind innerhalb von 30 Tagen ohne Abzug zahlbar.";
                context.set("termsOfPayment", termsOfPayment);
                break;
        }

        switch (function) {
            default:
                break;
            case OFFER:
                offerline = "auf der Basis unserer Geschäftsbedingungen bieten wir Ihnen an:";
                context.set("filenamePDF", "Angebot_" + offer.getNumber() + ".pdf");
                mailText = "in dem beigefügten Dokument erhalten Sie das Angebot Nr. " + offer.getNumber();
                subject = "Angebot "
                          + offer.getNumber()
                          + " vom "
                          + NLS.toUserString(offer.getDate())
                          + ", "
                          + offer.getKeyword();
                break;
            case SALES_CONFIRMATION:
                offerline =
                        "wir danken für die Beauftragung der nachfolgend aufgeführten Positionen, die wir gerne auf der Basis unserer Geschäftsbedingungen bestätigen: ";
                headlinePrefix = "Auftragsbestätigung zum ";
                context.set("filenamePDF", "Auftragsbestaetigung_" + offer.getNumber() + "_" + filePostfix + ".pdf");

                mailText =
                        "in dem beigefügten PDF-Dokument erhalten Sie die Auftragsbestäigung zu Ihrer Bestellung.<br></br><br></br>";
                mailText = mailText + "Für Erläuterungen und Rückfragen stehen wir gerne zur Verfügung.";
                subject = "Auftragsbestätigung "
                          + offer.getNumber()
                          + " Positionen: "
                          + context.get("positions")
                          + ", "
                          + offer.getKeyword();
                break;
        }

        switch (function) {
            case OFFER:
            case SALES_CONFIRMATION:
                //prepare the context

                if (Strings.isFilled(offerState) ) {
                    offerStateString = ", Status: " + offerState + "!";
                }
                Person person = offer.getPerson().getValue();
                context.set("person", person);
                context.set("isCompanyVAT", isCompanyVAT);
                context.set("subject", subject);
                String dateString = NLS.toUserString(LocalDate.now());
                context.set("mailText", mailText);
                context.set("street", company.getAddress().getStreet());
//                String city = company.getAddress().getZip() + " " + company.getAddress().getCity();
//                context.set("city", city);
                context.set("dateString", dateString);
                context.set("offerline", offerline);
                context.set("licenceItemCyclicUnit", licenceItemCyclicUnit);
                context.set("headlinePrefix", headlinePrefix);
                context.set("letterHeadline", sas.getLetterHeadline(offer.getPerson().getValue()));
                context.set("offerState", offerStateString);
                context.set("offer", offer);
                context.set("offerItemList", offerItemList);

                context.set("company", company);

                context.set("personName", person.getPerson().toString());
                context.set("personPhone", person.getContact().getPhone());
                context.set("personMail", person.getContact().getEmail());
                Person buyer = null;
                if(offer.getBuyer() != null) {
                    buyer = offer.getBuyer().getValue();
                }
//                if (buyer != null) {
//                    context.set("buyerPhone", buyer.getContact().getPhone());
//                    context.set("buyerName", buyer.getPerson().toString());
//                    context.set("buyerMail", buyer.getContact().getEmail());
//                } else {
//                    context.set("buyerPhone", null);
//                    context.set("buyerName", null);
//                    context.set("buyerMail", null);
//                }

                Employee employee = offer.getEmployee().getValue().as(Employee.class);
             //   String signature = employee.getSignature();
             //   context.set("employeeSignature", signature);
                UserAccount user = offer.getEmployee().getValue().as(UserAccount.class);
                context.set("uac", user);
                context.set("employeePhone", "07151/90316-"+employee.getPhoneExtension());
//                context.set("employeeMail", user.getEmail());
                context.set("priceBruttoSum", priceBruttoSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("priceNettoSum", priceNettoSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("priceVatSum", priceVatSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                if (vatRate != null) {
                    context.set("vatRate", NLS.toUserString(vatRate));
                }
                context.set("cyclicPriceBruttoSum", cyclicPriceBruttoSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("cyclicPriceNettoSum", cyclicPriceNettoSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("cyclicPriceVatSum", cyclicPriceVatSum.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                context.set("isLicenseItemPresent", offer.isLicenceItemPresent());

                if (offer.isOfferPeriodPresent()) {
                    context.set("isOfferPeriodPresent", offer.isOfferPeriodPresent());
                    context.set("offerPeriodStart", NLS.toUserString(offer.getOfferPeriodStart()));
                    context.set("offerPeriodEnd", NLS.toUserString(offer.getOfferPeriodEnd()));
                    LocalDate offerPeriodStart = offer.getOfferPeriodStart();
                    LocalDate offerPeriodEnd = offer.getOfferPeriodEnd();

                    int monthStart = offerPeriodStart.getMonthValue();
                    int monthEnd = offerPeriodEnd.getMonthValue();
                    int yearStart = offerPeriodStart.getYear();
                    int yearEnd = offerPeriodEnd.getYear();
                    int months = (yearEnd - yearStart) * 12;
                    months = months + monthEnd - monthStart + 1;
                    context.set("offerPeriodMonths", NLS.toUserString(months));

                    Amount offerPeriodNetto = cyclicPriceNettoSum.times(Amount.of(months));

                    context.set("offerPeriodNetto", offerPeriodNetto.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    Amount vatOfferPeriodLicence = offerPeriodNetto.times(vatRate).divideBy(Amount.ONE_HUNDRED);
                    context.set("vatOfferPeriodLicence",
                                vatOfferPeriodLicence.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    Amount offerPeriodLicenceBrutto = offerPeriodNetto.add(vatOfferPeriodLicence);
                    context.set("offerPeriodLicenceBrutto",
                                offerPeriodLicenceBrutto.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());

                    // calculate for the next whole year
                    LocalDate offerPeriodStartNy = offerPeriodStart.plusDays(1);
                    LocalDate offerPeriodEndNy = offerPeriodEnd.plusYears(1);
                    context.set("offerPeriodStartNy", NLS.toUserString(offerPeriodStartNy));
                    context.set("offerPeriodEndNy", NLS.toUserString(offerPeriodEndNy));

                    int monthsNy = 12;
                    context.set("offerPeriodMonthsNy", NLS.toUserString(monthsNy));
                    context.set("cyclicPriceNettoSumPositiv", cyclicPriceNettoSumPositiv.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());

                    Amount offerPeriodNettoNy = cyclicPriceNettoSumPositiv.times(Amount.of(monthsNy));
                    context.set("offerPeriodNettoNy", offerPeriodNettoNy.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    Amount vatOfferPeriodLicenceNy =  offerPeriodNettoNy.times(vatRate).divideBy(Amount.ONE_HUNDRED);
                    context.set("vatOfferPeriodLicenceNy", vatOfferPeriodLicenceNy.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    Amount offerPeriodLicenceBruttoNy =  offerPeriodNettoNy.add(vatOfferPeriodLicenceNy);
                    context.set("offerPeriodLicenceBruttoNy", offerPeriodLicenceBruttoNy.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());

                    context.set("priceBruttoSumNy", Amount.ZERO.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    context.set("priceNettoSumNy", Amount.ZERO.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());
                    context.set("priceVatSumNy", Amount.ZERO.toString(NumberFormat.TWO_DECIMAL_PLACES).asString());


                } else {
                    context.set("isOfferPeriodPresent", false);
                }


                String buyerMessage = "";
                if (offer.getBuyer().isFilled()) {
                    buyerMessage = offer.getBuyer().getValue().getPerson().getAddressableName()
                                   + " erhält diese Mail ebenfalls zur Information.";
                }
                context.set("buyerMessage", buyerMessage);
                context.set("isOffer", false);
                if (OFFER.equals(function)) {
                    context.set("isOffer", true);
                }
                break;
        }

        return context;

        // TODO in offer.html umstellen: ${toolkit.nl2br(${toolkit.escapeXML($item.text)})}
    }

    @Override
    public String getLetterHeadline(Person person) {
        String line = "Sehr geehrte";
        if("SIR".equals(person.getPerson().getSalutation())) {
            line = line + "r";
        }
        line = line + " " + person.getPerson().getAddressableName() + ",";
        return line;
    }

    @Override
    public List<OfferItem> getConfirmationOfferItems(Offer offer) {
        List<OfferItem> offerItemList;
        offerItemList = oma.select(OfferItem.class)
                           .eq(OfferItem.OFFER, offer)
                           .eq(OfferItem.STATE, OfferItemState.ORDERED)
                           .eq(OfferItem.SALESCONFIRMATIONDATE, null)
                           .orderAsc(OfferItem.POSITION)
                           .queryList();
        return offerItemList;
    }

    @Override
    public void updateOfferState(Offer offer, boolean save) {
        boolean open = false;
        boolean busy = false;
        boolean closed = false;
        List<OfferItem> oiList =
                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
        for (OfferItem oi : oiList) {
            if (OfferItemState.OFFER.equals(oi.getState())) {
                open = true;
            }
            if (OfferItemState.ORDERED.equals(oi.getState())) {
                busy = true;
            }
            if (OfferItemState.CONFIRMED.equals(oi.getState())) {
                busy = true;
            }
            if (OfferItemState.DEVELOPED.equals(oi.getState())) {
                busy = true;
            }
            if (OfferItemState.ACCEPTED.equals(oi.getState())) {
                busy = true;
            }
            if (OfferItemState.ACCOUNTED.equals(oi.getState())) {
                closed = true;
            }
            if (OfferItemState.CANCELED.equals(oi.getState())) {
                closed = true;
            }
        }
        if (open && !busy && !closed) {
            offer.setState(OfferState.OPEN);
        }
        if (closed && !busy & !open) {
            offer.setState(OfferState.CLOSED);
        }
        if (busy) {
            offer.setState(OfferState.BUSY);
        }
        if (save) {
            oma.update(offer);
        }
    }

    @Override
    public void checkAllOfferItemsAreOffers(Offer offer, boolean copyAllowed) {
        List<OfferItem> offerItemList =
                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
        for (OfferItem oi : offerItemList) {
            if (oi.isInfoText()) {
                continue;
            }
            if (oi.isSum()) {
                continue;
            }
            if (copyAllowed) {
                if (OfferItemState.COPY.equals(oi.getState())) {
                    continue;
                }
            }
            if (!(OfferItemState.OFFER.equals(oi.getState()))) {
                String text = MessageFormat.format(
                        "Die Angebotsposition Nr. {0} des Angebots Nr. {1} hat den Status ''{2}'' und kann deshalb nicht weiterverarbeitet werden--> Abbruch",
                        oi.getPosition(),
                        oi.getOffer().getValue().getNumber(),
                        oi.getState().toString());
                throw Exceptions.createHandled()
                                .withNLSKey("OfferItem.notAllOffers")
                                .set("pos", oi.getPosition())
                                .set("offerNr", offer.getNumber())
                                .set("state", oi.getState())
                                .handle();
            }
        }
    }

    @Override
    public int sendSalesConfirmation(/* View view, */ Offer offer) {
        int mailCounter = 0;
        List<OfferItem> confirmationList = getConfirmationOfferItems(offer);
//        Alte Lösung:
//        List<OfferItem> confirmationList = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
//                .eq(OfferItem.STATE, OfferItemState.ORDERED)
//                .eq(OfferItem.SALESCONFIRMATIONDATE, null).orderAsc(OfferItem.POSITION).queryList();
        if (confirmationList == null || confirmationList.size() <= 0) {
            throw Exceptions.createHandled()
                            .withNLSKey("ServiceAccountingServiceBean.salesConfirmationNoOrders")
                            .set("number", offer.getNumber())
                            .handle();
        }

        Person person = offer.getPerson().getValue();
        String mailAdress = person.getContact().getEmail();
        if (Strings.isEmpty(mailAdress)) {
            throw Exceptions.createHandled()
                            .withNLSKey("ServiceAccountingServiceBean.salesConfirmationNoMailAdr")
                            .set("person", person.getPerson().getAddressableName())
                            .handle();
        }

        // check the contractStartDate for licenses.
        // If the date == null create the contractStartDate as the first day of the next month
        for (OfferItem offerItem : confirmationList) {
            if (offerItem.isLicense()) {
                if (offerItem.getContractStartDate() == null) {
                    LocalDate now = LocalDate.now();
                    int year = now.getYear();
                    int month = now.getMonthValue() + 1;
                    if (month > 12) {
                        month = 1;
                        year = year + 1;
                    }
                    LocalDate contractStartDate = LocalDate.of(year, month, 1);
                    offerItem.setContractStartDate(contractStartDate);
                    oma.update(offerItem);
                }
            }
        }

        // build the pdf-File and add the File as attachment to the mail
        Context context = prepareContext(offer, ServiceAccountingService.SALES_CONFIRMATION);
        // ToDO auf .pasta umstellen
        File fileAttachment = createPdfFromContext(context, "templates/offer.pdf.vm");

        // build the mail-content
        String subject = (String) context.get("subject");

        // send the mail
        String template = "mail-template";
        sendMail(mailAdress, subject, template, context, fileAttachment);
        mailCounter++;

        // set the salesConfirmationDate = now and the state = CONFIRMED
        LocalDate confirmationDate = LocalDate.now();
        for (OfferItem oi : confirmationList) {
            oi.setSalesConfirmationDate(confirmationDate);
            oi.setState(OfferItemState.CONFIRMED);
            oma.update(oi);
        }

        // Ggfs. Verträge aus den Auftragsbestätigungen anlegen
        List<String> messageList = new ArrayList<String>();
        for (OfferItem offerItem : confirmationList) {
            messageList.add(createContractFromOfferItem(offerItem));
        }
        // ToDo: messageList anzeigen.

        return mailCounter;
    }

    private void sendMail(String mailAdress,
                          String subject,
                          String template,
                          Context mailContext,
                          final File fileAttachment) {

        // ToDo mail anzeigen und fragen ob diese gesendet werden soll

        DataSource attachment = new DataSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(fileAttachment);
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
                return fileAttachment.getName();
            }
        };

        mails.createEmail()
             .addAttachment(attachment)
             .fromEmail(scireumSupportMailAddress)
             .toEmail(mailAdress)
             .subject(subject)
          //   .useMailTemplate(template, mailContext)
             .simulate(false)
             .send();
    }

    @Override
    public List<PackageDefinition> getAllPackageDefinitions(Object object) {
        if (object.getClass().isInstance(OfferItem.class)) {
            OfferItem offerItem = (OfferItem) object;
            Product product = offerItem.getPackageDefinition().getValue().getProduct().getValue();
            List<PackageDefinition> pdList =
                    oma.select(PackageDefinition.class).eqIgnoreNull(PackageDefinition.PRODUCT, product).queryList();
            return pdList;
        }
        if (object.getClass().isInstance(Contract.class)) {
            Contract contract = (Contract) object;
            List<PackageDefinition> pdList = oma.select(PackageDefinition.class).queryList();
            return pdList;
        }
        return null;
    }

    @Override
    public String createContractFromOfferItem(OfferItem offerItem) {
        String message = MessageFormat.format("Pos. {0}: {1} ",
                                              NLS.toUserString(offerItem.getPosition()),
                                              offerItem.getKeyword());
        if (!offerItem.isLicense()) {
            message = message + " keine Lizenz --> kein Vertrag.";
            return message;
        }

        Offer offer = offerItem.getOffer().getValue();
        Company company = offer.getCompany().getValue();
        PackageDefinition pd = offerItem.getPackageDefinition().getValue();

        String accGroup = "4711";

        Contract contract = new Contract();
        contract.getCompany().setValue(company);
        contract.setAccountingGroup(accGroup);
        contract.getPackageDefinition().setValue(pd);
        contract.getContractPartner().setValue(offer.getPerson().getValue());
        contract.setAccountingInterval(AccountingIntervalType.YEAR);
        Amount discount = offerItem.getDiscount().fill(Amount.ZERO);
        if (discount.isPositive()) {
            contract.setDiscountPercent(discount);
        } else {
            contract.setDiscountPercent(Amount.NOTHING);
        }
        Amount quantity = offerItem.getQuantity().fill(Amount.ONE);
        contract.setQuantity(quantity.getAmount().intValue());
        Amount unitPrice = offerItem.getCyclicPrice().fill(Amount.ZERO);
        contract.setUnitPrice(unitPrice);
        Amount singlePrice = offerItem.getSinglePrice().fill(Amount.ZERO);
        if (singlePrice.isPositive()) {
            contract.setSinglePrice(singlePrice);
            contract.setSinglePriceState(ContractSinglePriceType.ACCOUNT_NOW);
        } else {
            contract.setSinglePrice(Amount.NOTHING);
            contract.setSinglePriceState(ContractSinglePriceType.NO_SINGLEPRICE);
        }
        contract.setSigningDate(offerItem.getOrderDate());
        //position is set in the beforeSave-Method
        if (offerItem.getContractStartDate() == null) {
            Exceptions.createHandled().withNLSKey("OfferItem.contractStartDateMissing").handle();
        } else {
            contract.setStartDate(offerItem.getContractStartDate());
        }
        if (Strings.isFilled(pd.getParameter())) {
            contract.setParameter(pd.getParameter());
        }
        List<Contract> contractList = oma.select(Contract.class)
                                         .eq(Contract.COMPANY, company)
                                         .eq(Contract.PACKAGEDEFINITION, pd)
                                         .eq(Contract.ACCOUNTINGGROUP, accGroup)
                                         .queryList();

        oma.update(contract);
        Long id = contract.getId();
        message = MessageFormat.format("{0}, Vertrag mit Id: {1}  in der Abrechnungsgruppe {2} angelegt.",
                                       message,
                                       NLS.toUserString(id),
                                       accGroup);
        if (!contractList.isEmpty()) {
            message = MessageFormat.format(
                    "{0}. Hinweis: es existieren bereits {1} andere {2}-Verträge in der Abrechnungsgruppe {3}.",
                    message,
                    NLS.toUserString(contractList.size()),
                    pd.getName(),
                    accGroup);
        }
        return message;
    }

    @Override
    public void checkValue(Amount value,
                           boolean notNegative,
                           boolean notZero,
                           boolean notPositive,
                           boolean testLimit,
                           Amount limit,
                           String name) {
        if (notNegative) {
            if (value.isNegative()) {
                throw Exceptions.createHandled().withNLSKey("Contract.valueIsNegative").set("name", name).handle();
            }
        }
        if (notZero) {
            if (!value.isZero()) {
                throw Exceptions.createHandled().withNLSKey("Contract.valueIsNotZero").set("name", name).handle();
            }
        }
        if (notPositive) {
            if (value.isPositive()) {
                throw Exceptions.createHandled().withNLSKey("Contract.valueIsPositive").set("name", name).handle();
            }
        }
        if (testLimit) {
            if (limit.isNegative()) {
                if (value.compareTo(limit) < 0) {
                    throw Exceptions.createHandled()
                                    .withNLSKey("Contract.valueIsGreater")
                                    .set("name", name)
                                    .set("limit", NLS.toUserString(limit))
                                    .handle();
                }
            } else {
                if (value.compareTo(limit) > 0) {
                    throw Exceptions.createHandled()
                                    .withNLSKey("Contract.valueIsGreater")
                                    .set("name", name)
                                    .set("limit", NLS.toUserString(limit))
                                    .handle();
                }
            }
        }
    }

    @Override
    public File createPdfFromContext(Context context, String templateName) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        String filenamePdf = (String) context.get("filenamePDF");
        File file = new File(filenamePdf);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(filenamePdf));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            templates.generator().useTemplate(templateName).applyContext(context).generateTo(baos);

            baos.writeTo(fos);
        } catch (Exception ioe) {
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
        List<OfferItem> offerItemList =
                oma.select(OfferItem.class).eq(OfferItem.OFFER, offer).orderAsc(OfferItem.POSITION).queryList();
        if (reCreate) {
            // check whether the offerItem-states are == OFFER or COPY
            checkAllOfferItemsAreOffers(offer, true);
        }
        // create the new offer and copy
        Offer newOffer = new Offer();

        newOffer.setKeyword(offer.getKeyword());
        newOffer.getCompany().setId(offer.getCompany().getValue().getId());
        newOffer.getPerson().setId(offer.getPerson().getId());
        UserAccount user = offer.getEmployee().getValue().as(UserAccount.class);
        newOffer.getEmployee().setId(user.getId());
        newOffer.getBuyer().setId(offer.getBuyer().getId());
        newOffer.setReference(offer.getReference());
        newOffer.setDate(LocalDate.now());
        newOffer.setState(offer.getState());

        // save the new offer
        oma.update(newOffer);

        //create the new offerItems and copy
        for (OfferItem o : offerItemList) {
            OfferItem newOfferitem = new OfferItem();
            newOfferitem.setState(OfferItemState.COPY);
            newOfferitem.setKeyword(o.getKeyword());
            newOfferitem.getPackageDefinition().setId(o.getPackageDefinition().getId());
            newOfferitem.getOffer().setId(newOffer.getId());
            newOfferitem.setDiscount(o.getDiscount());
            newOfferitem.setOfferItemType(o.getOfferItemType());
            newOfferitem.setPosition(o.getPosition());
            newOfferitem.setPriceBase(o.getPriceBase());
            newOfferitem.setQuantity(o.getQuantity());
//            newOfferitem.setAccountingUnitComplete(o.getAccountingUnitComplete());
            newOfferitem.setSinglePrice(o.getSinglePrice());
            newOfferitem.setCyclicPrice(o.getCyclicPrice());
            newOfferitem.setText(o.getText());
            newOfferitem.setHistory(MessageFormat.format("*** kopiert von Angebot {0} ***", offer.getNumber()));
            // save the new offerItem
            oma.update(newOfferitem);

            if (reCreate) {
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

/*    // 04.01.2018: Code durch exportLineitems in AccountingServiceBean ersetzt.

    @Override
    public void exportInvoiceItemsToCollmex() {
        LocalDate invoiceDate = LocalDate.now();
        LocalDateTime timeStamp = LocalDateTime.now();
        // get all new invoiceItems
        List<Lineitem> invoiceItemList = oma.select(Lineitem.class)
                .eq(Lineitem.STATUS, Lineitem.LINEITEMSTATUS_NEW)
                .eq(Lineitem.LINEITEMTYPE, Lineitem.LINEITEMTYPE_OA)
                .orderDesc(Lineitem.INVOICENR)
                .orderAsc(Lineitem.POSITION).queryList();
        if (invoiceItemList.size() <= 0) {

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
        File file = as.createCsvFilename("invoiceItem",-1, timeStamp);
        try {
            FileOutputStream output = new FileOutputStream(file);
            Writer fw = new OutputStreamWriter(output, "ISO_8859_1");
            PrintWriter pw = new PrintWriter(fw);
            Amount sum = Amount.ZERO;
            try {
                int i = 0;
                for (Lineitem invoiceItem : invoiceItemList) {
                    i++;
                    sum = sum.add(generateCollmexInvoiceLine(pw, invoiceItem, invoiceDate, timeStamp));
                }
                pw.flush(); // flush the printwriter to get all data to the file
                // build a activity-news
                String text = MessageFormat.format(
                        "{0} Service-Rechnungspositionen exportiert, Netto-Umsatz: {1} EUR, Datei: {2}",
                        NLS.toUserString(i),
                        NLS.toUserString(sum),
                        file.getAbsoluteFile());
                System.out.println(text);

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

*/

//    /**
//     * generates a invoice-position in Collmex based on the given invoiceitem
//     * @param pw  printwriter
//     * @param invoiceItem
//     * @param invoiceDate
//     * @return sum = quantity * (price * (1-discount))
//     */
/*
    private Amount generateCollmexInvoiceLine(PrintWriter pw, Lineitem invoiceItem, LocalDate invoiceDate,
                                              LocalDateTime timeStamp) {
        Amount sum = Amount.ZERO;

        // step 2: generate a csv-line for the export in Collmex-Notation
        final int csvLae = 82; // field #1 - #82
        String[] csv = new String[csvLae + 1]; // csv[0] - csv[82],
        // csv[0] is not used
        csv[1] = "CMXINV"; // Satzart C Festwert CMXINV
        csv[2] = NLS.toUserString(invoiceItem.getInvoiceNr()); // Rechnungsnummer I
        // 8 Die Rechnungsnummer identifiziert die Rechnung eindeutig. Siehe auch Nummernvergabe.
        // 03 = Position I 8 Positionsnummer der Rechnungsposition. Wenn nicht
        // angegeben, wird die Positionsnummer automatisch fortlaufend vergeben.

        csv[4] = "0"; // 04 = Rechnungsart I 8, // 0 = Rechnung , 1 = Gutschrift
        // 2 = Abschlagsrechnung, 3 = Barverkauf
        csv[5] = "1"; // 05 = Firma Nr I 8 Interne Nummer der Firma, wie unter
        // Verwaltung -> Firma anzeigen und ändern angezeigt.
        // 06 = Auftrag Nr I 8 Nummer des Kundenauftrags, auf den sich die
        // Rechnung bezieht.
        csv[7] = NLS.toUserString(invoiceItem.getCustomerNr()); // 07 = Kunden-Nr I8
        // Der Kunde muss in Collmex existieren. Referenz ausschliesslich über die Kundennummer
        // 08 Anrede C 10 Felder 8 - 27 ist die Kundenadresse. Nur für den Export. Die Felder werden beim Import ignoriert.
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
        csv[29] = "0"; // Privatperson I8, 0 = keine Privatperson , 1 = Privatperson
        csv[30] = NLS.toUserString(invoiceDate);

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
        csv[42] = invoiceItem.getOutputLanguage(); // 42 Sprache I 8 0 = Deutsch, 1 = Englisch.
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
        invoiceItem.setExportDate(timeStamp);
        oma.update(invoiceItem);
        // Step 6: calculate the position-sum
        Amount quantity = invoiceItem.getQuantity().fill(Amount.ONE);
        Amount price = invoiceItem.getPrice().fill(Amount.ZERO);
//        if(quantity == null || quantity.isEmpty()) {
//            quantity = Amount.ONE;
//        }
        sum = price.times(quantity);
        Amount discount =  invoiceItem.getPositionDiscount().fill(Amount.ZERO);
        sum = sum.decreasePercent(discount);
//        if(invoiceItem.getPositionDiscount() != null) {
//            sum = sum.decreasePercent(invoiceItem.getPositionDiscount());
//        }
        // System.err.println(invoiceItem.getCompanyName() + "   " + NLS.toUserString(sum));
        return sum;

    }

*/

    private Amount getVatRateForCompany(Company company, LocalDate referenceDate) {

        String countryCode = company.getAddress().getCountry();
        if (Strings.isEmpty(countryCode)) {
            throw Exceptions.createHandled()
                            .withNLSKey("ServiceAccountingServiceBean.countryCodeMissing")
                            .set("company", company.toString())
                            .handle();
        }
        if (referenceDate == null) {
            throw Exceptions.createHandled().withNLSKey("ServiceAccountingServiceBean.referenceDateMissing").handle();
        }
        List<VatRate> vatRateList =
                oma.select(VatRate.class).eq(VatRate.COUNTRYCODE, countryCode).orderDesc(VatRate.VALIDFROM).queryList();
        for (VatRate vr : vatRateList) {
            if (!vr.getValidFrom().isAfter(referenceDate)) {
                return vr.getVatRate();
            }
        }
        return null;
    }

    @Override
    public List<OfferInfo> generateOfferInfo(Company company) {
        List<OfferInfo> list = new ArrayList<OfferInfo>();
        List<OfferItem> oiList = oma.select(OfferItem.class)
                                    .eq(OfferItem.OFFERITEMTYPE, OfferItemType.SERVICE)
                                    .orderAsc(OfferItem.OFFER.join(Offer.NUMBER))
                                    .queryList();
        int nr = 0;
        if (!oiList.isEmpty()) {
            for (OfferItem oi : oiList) {
                Offer offer = oi.getOffer().getValue();
                Company oiCompany = offer.getCompany().getValue();
                if (!company.equals(oiCompany)) {
                    continue;
                }
                Amount quantity = oi.getQuantity();
                Amount price = oi.getSinglePrice();
                price = price.times(quantity);
                if (oi.isDiscountPresent()) {
                    price.decreasePercent(oi.getDiscount());
                }
                OfferItemState state = oi.getState();
                LocalDate date = getStateDate(oi, state);
                nr++;
                OfferInfo oInfo = new OfferInfo(offer, oi, oiCompany, price, state, date, quantity, nr);
//                System.out.println(NLS.toUserString(nr) +";"+ NLS.toUserString(date, false) + ";"
//                        + company.toString()+";"+offer.toString()+";"+NLS.toUserString(oi.getPosition())+";"
//                        +price.toString()+";"+state.toString() + ";" + quantity.toString());
                list.add(oInfo);
            }
        }
        return list;
    }

    private LocalDate getStateDate(OfferItem oi, OfferItemState state) {
        if (OfferItemState.OFFER.equals(oi.getState())) {
            return oi.getOfferDate();
        }
        if (OfferItemState.ORDERED.equals(oi.getState())) {
            return oi.getOrderDate();
        }
        if (OfferItemState.CONFIRMED.equals(oi.getState())) {
            return oi.getSalesConfirmationDate();
        }
        if (OfferItemState.DEVELOPED.equals(oi.getState())) {
            return oi.getCompletionDate();
        }
        if (OfferItemState.ACCEPTED.equals(oi.getState())) {
            return oi.getAcceptanceDate();
        }
        if (OfferItemState.ACCOUNTED.equals(oi.getState())) {
            return oi.getAccountingDate();
        }
        if (OfferItemState.CANCELED.equals(oi.getState())) {
            return oi.getOfferDate();
        }
        return null;
    }

    @Override
    public Context prepareContractContext(Contract contract) {
        Context context = new Context();

        String subject = "Vertrag " + contract.toContractName();
        context.set("subject", subject);

        String termsOfPayment =
                "Die monatlichen Lizenz-Kosten werden zu Vertragsbeginn für das laufende Jahr und dann im jeweiligen Abrechnungszyklus in Rechnung gestellt.";

        termsOfPayment = termsOfPayment
                         + " Das monatliche Supportkontingent entspricht 30 Prozent der monatlichen Lizenzgebühr, ein Personentag sind zurzeit 800,00 EUR.";
        termsOfPayment = termsOfPayment + "  Rechnungen sind innerhalb von 30 Tagen ohne Abzug zahlbar.";
        context.set("termsOfPayment", termsOfPayment);

        context.set("filenamePDF", "Dokumentation_Vertrag_" + contract.toContractName() + ".pdf");

        //prepare the context
        String dateString = NLS.toUserString(LocalDate.now());
        context.set("dateString", dateString);

        context.set("salutation", contract.getContractPartner().getValue().getPerson().getTranslatedSalutation());

        context.set("contract", contract);

        Company company = contract.getCompany().getValue();
        context.set("company", company);
        Person person = contract.getContractPartner().getValue();
        context.set("person", person);

        Employee employee = UserContext.getCurrentUser().as(Employee.class);
        context.set("employee", employee);
        Integer quantity = contract.getQuantity();
        if (quantity == null) {
            quantity = 1;
        }
        context.set("quantity", quantity);
        Amount unitPrice = contract.getUnitPrice();
        if (unitPrice == null) {
            unitPrice = Amount.ZERO;
        }
        context.set("unitPrice", unitPrice);
        Amount totalUnitPrice = unitPrice.times(Amount.of(quantity));
        context.set("totalUnitPrice", totalUnitPrice);

        Amount totalSinglePrice = null;
        if (contract.getSinglePrice() != null) {
            totalSinglePrice = contract.getSinglePrice().times(Amount.of(quantity));
        }
        context.set("totalSinglePrice", totalSinglePrice);
        Product product = contract.getPackageDefinition().getValue().getProduct().getValue();
        String productName = product.toString();
        context.set("product", productName);
        return context;
    }

    @Override
    public String buildMd5HexString(String s) {
        //        String md5 = BaseEncoding.base64().encode(Hashing.md5().hashString(s, Charsets.UTF_8).asBytes());
        byte[] md5HashBytes = Hashing.md5().hashString(s, Charsets.UTF_8).asBytes();
        StringBuilder sb = new StringBuilder(md5HashBytes.length * 2);
        for (int i = 0; i < md5HashBytes.length; i++) {
            sb.append(Character.forDigit((md5HashBytes[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(md5HashBytes[i] & 0x0f, 16));
        }
        String md5 = sb.toString();
        return md5;
    }

    @Override
    public List<Tuple<String, Amount>> licenseSalesPerYear(Company company, int year) {

        LocalDate thisYearStart = LocalDate.of(year, 1, 1);
        LocalDate nextYearStart = thisYearStart.plusYears(1);
        List<Contract> contractList = oma.select(Contract.class)
                                         .eq(Contract.COMPANY, company).queryList();

        HashMap<Product, Amount> map = new HashMap();

        for (Contract contract : contractList) {
            if (contract.getEndDate() == null || (contract.getEndDate().isAfter(thisYearStart)))  {
                if(contract.getStartDate().isBefore(nextYearStart)) {
                    Product product = contract.getPackageDefinition().getValue().getProduct().getValue();
                    Amount value = contract.calculateMonthPrice();
                    value = value.times(Amount.of(12));
                    Amount mapValue = map.get(product);
                    if (mapValue == null) {
                        mapValue = value;
                    } else {
                        mapValue = mapValue.add(value);
                    }
                    map.put(product, mapValue);
                }
            }
        }

        List<Tuple<String, Amount>> tupleList = new ArrayList();
        for(Product product : map.keySet()) {
            Amount value = map.get(product);
            value = value.round(NumberFormat.NO_DECIMAL_PLACES);
            Tuple<String, Amount> tuple = new Tuple(product.getName(), value);
            tupleList.add(tuple);
        }

        Amount companySum = Amount.ZERO;
        for(Tuple<String, Amount> tuple : tupleList) {
            companySum = companySum.add(tuple.getSecond());
        }
        if(tupleList.size() > 1) {
            tupleList.add(new Tuple("-------------------------------", null));
            tupleList.add(new Tuple("Gesamt: ", companySum));
        }

        return tupleList;

    }

    @Override
    public List<List<String>> displayOfferSums(Company company, LocalDate startDate, LocalDate endDate) {
        List<List<String>> messageList = new ArrayList();

        boolean printHeader = true;
        List<OfferInfo> oInfoList = generateOfferInfo(company);
        OfferItemState[] states = {OfferItemState.OFFER, OfferItemState.ORDERED, OfferItemState.CONFIRMED,
                                   OfferItemState.DEVELOPED, OfferItemState.ACCEPTED, OfferItemState.ACCOUNTED, OfferItemState.CANCELED};
        int anzSum = 0;
        for (OfferItemState state : states) {
            Amount sum = Amount.ZERO;
            Amount quantity = Amount.ZERO;
            int anz = 0;
            for (OfferInfo oInfo : oInfoList) {
                if (state.equals(oInfo.getState())) {
                    if (oInfo.getDate().isAfter(startDate)) {
                        if (oInfo.getDate().isAfter(endDate)) {
                            continue;
                        }
                        anz++;
                        sum = sum.add(oInfo.getValue());
                        quantity = quantity.add(oInfo.getQuantity());
                    }
                }
            }
            anzSum = anzSum + anz;

            if(anz > 0) {
                String[] line = {NLS.toUserString(anz), "Angebote mit dem Status:", state.toString(), sum.toString() + " EUR", quantity.toString() + " PT"};
                List<String> message = new ArrayList();
                message.add(NLS.toUserString(anz));
                message.add("Angebote mit dem Status:");
                message.add(state.toString());
                message.add(sum.toString() + " EUR");
                DecimalFormat df = new DecimalFormat();
                df.applyPattern("###,###.0");
                BigDecimal value = quantity.getAmount();
                String quantityString = df.format(value) + " PT";
                message.add(quantityString);
                messageList.add(message);
            }
        }
        return messageList;
    }


}
