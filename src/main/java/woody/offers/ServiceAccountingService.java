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
import woody.sales.Lineitem;
import woody.sales.PackageDefinition;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by gerhardhaufler on 23.12.15.
 */
public interface ServiceAccountingService {

    public static final String OFFER = "offer";
    public static final String SALES_CONFIRMATION = "sales_confirmation";


    /**
     * account all service-offers
     * @param dryRun, true = Test, false = produktiv
     * @param monitor
     * @return  List of teh generated lineitems
     */
    public DataCollector<Lineitem> accountAllServiceOffers(boolean dryRun /*, TaskMonitor monitor*/) ;

    /**
     *  @return   ProdUmsatz
     */
    public Amount getProdUmsatz();

    /**
     * @return   TestUmsatz
     */
    public Amount getTestUmsatz();

    /**
     * @return  AccountingDate
     */
    public LocalDate getAccountingDate();


    /**
     * export the invoicItem to Collmex
     */
    public void exportInvoiceItemsToCollmex();

    /**
     * copies the given offer, the state of the given offer is "canceldd", the state of the new offer is "copy"
     * @param offer
     */
    public void copyOffer(Offer offer, boolean reCreate) ;


    /**
     * prepares the context for publishing a PDF-document for the given offer
     * @param offer: offer which shall published
     * @return  Context
     */
    public Context prepareContext(Offer offer, String function);

    /**
     * checks all offeritems of the given offer
     * @param offer: offer
     * if NOT all offeritems are "OFFERS" a Exception is thrown
     */
    public void checkAllOfferItemsAreOffers(Offer offer);

    /**
     * The sales confirmation is only send, if the special sendConfirmationDate is null.
     */
    public int sendSalesConfirmation(/*View view, */ Offer offer);

    /**
     * fetches the "next" OfferItemState of the given offerItem
     * @param oi
     * @return
     */
    public OfferItemState getNextState(OfferItem oi);

    /**
     * returns true, if the button should be visible
     * @param oi
     * @return
     */
    public boolean isOfferItemVisible(OfferItem oi, OfferItemState targetState);

    /**
     * returns a list of offerItems to which send a salesConfirmation
     * @param offer
     */
    public List<OfferItem>  getConfirmationOfferItems(Offer offer);

    /**
     * checks all offerItems and update the offerState of the given offer
     * @param offer
     * @param save   true --> the offer is saved at this time
     */
    public void updateOfferState(Offer offer, boolean save);


    /**
     * sends the offer per mail to the given person and buyer in the offer
     * @param offer
     */
    public void sendOffer(Offer offer);

    public List<PackageDefinition> getAllPackageDefinitions(Object object);

}
