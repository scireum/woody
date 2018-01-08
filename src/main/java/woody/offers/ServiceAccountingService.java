/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;


import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Context;

import sirius.kernel.commons.DataCollector;
import woody.offers.Offer;
import woody.offers.OfferItem;
import woody.offers.OfferItemState;
import woody.sales.Contract;
import woody.sales.Lineitem;
import woody.sales.PackageDefinition;
import woody.xrm.Company;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by gerhardhaufler on 23.12.15.
 */
public interface ServiceAccountingService {

    public static final String NORMAL_MAIL = "normalMail";
    public static final String OFFER = "offer";
    public static final String SALES_CONFIRMATION = "salesConfirmation";

    /**
     * account all service-offers
     *
     * @param dryRun, true = Test, false = produktiv
     * @return List of teh generated lineitems
     */
    public DataCollector<Lineitem> accountAllServiceOffers(boolean dryRun /*, TaskMonitor monitor*/);

    /**
     * @return ProdUmsatz
     */
    public Amount getProdUmsatz();

    /**
     * @return TestUmsatz
     */
    public Amount getTestUmsatz();

//    /**
//     * export the invoicItem to Collmex
//     */
//    public void exportInvoiceItemsToCollmex();

    /**
     * copies the given offer, the state of the given offer is "canceldd", the state of the new offer is "copy"
     *
     * @param offer
     */
    public void copyOffer(Offer offer, boolean reCreate);

    /**
     * prepares the context for publishing a PDF-document for the given offer
     *
     * @param offer: offer which shall published
     * @return Context
     */
    public Context prepareContext(Offer offer, String function);

    /**
     * checks all offeritems of the given offer
     *
     * @param offer:       offer
     * @param copyAllowed: if true: the state 'copy' is allowed
     *                     if NOT all offeritems are "OFFERS" ( or COPY) a Exception is thrown
     */
    public void checkAllOfferItemsAreOffers(Offer offer, boolean copyAllowed);

    /**
     * The sales confirmation is only send, if the special sendConfirmationDate is null.
     */
    public int sendSalesConfirmation(/*View view, */ Offer offer);

    /**
     * fetches the "next" OfferItemState of the given offerItem
     *
     * @param oi
     * @return
     */
    public OfferItemState getNextState(OfferItem oi);

    /**
     * returns true, if the button should be visible
     *
     * @param oi
     * @return
     */
    public boolean isOfferItemVisible(OfferItem oi, OfferItemState targetState);

    /**
     * returns a list of offerItems to which send a salesConfirmation
     *
     * @param offer
     */
    public List<OfferItem> getConfirmationOfferItems(Offer offer);

    /**
     * checks all offerItems and update the offerState of the given offer
     *
     * @param offer
     * @param save  true --> the offer is saved at this time
     */
    public void updateOfferState(Offer offer, boolean save);


    public List<PackageDefinition> getAllPackageDefinitions(Object object);

    /**
     * creates a contract from the given offerItem
     */
    public String createContractFromOfferItem(OfferItem offerItem);

    /**
     * checks the given value
     *
     * @param value
     * @param notNegative: true --> if the value is negative a exception is thrown
     * @param notZero:     true --> if the value is zero a exception is thrown
     * @param notPositive: true --> if the value is positive a exception is thrown
     * @param testLimit:   true --> the given limit is tested. if the value is < or > the limit a exception is thrown
     * @param limit:       limit
     * @param name:        name of the value
     */
    public void checkValue(Amount value,
                           boolean notNegative,
                           boolean notZero,
                           boolean notPositive,
                           boolean testLimit,
                           Amount limit,
                           String name);

    /**
     * creates a Pdf-file with the given template and context
     * @param context         context
     * @param templateName    template
     * @return                pdf-file
     */
    public File createPdfFromContext(Context context, String templateName);

    /**
     * generates typicall Infos about all offers of the given company
     * @param company
     * @return
     */
    public List<OfferInfo> generateOfferInfo(Company company);

    /**
     * prepares the context for the given contract
     * @param contract
     * @return
     */
    public Context prepareContractContext(Contract contract);

    /**
     * builds a Md5 -Herx-String of the given string
     */
    public String buildMd5HexString(String s);

}