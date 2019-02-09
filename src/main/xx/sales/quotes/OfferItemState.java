/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.quotes;

import sirius.kernel.nls.NLS;

/**
 * Created by gerhardhaufler on 18.04.14.
 */
public enum OfferItemState {
    // NEVER CHANGE THIS ORDER!
    // <code>
    // Status     Service                                                      License
    // -----------------------------------------------------------------------------------------------
    OFFER,     // Angebot                                                      Angebot
    ORDERED,   // Position wurde vom Kunden bestellt                           bestellt
    CONFIRMED, // Auftragsbestätigung für diese Position wurde versendet       bestätigt
    DEVELOPED, // Position wurde von scireum entwickelt                        ***
    ACCEPTED,  // Position wurde vom Kunden abgenommen                         Vertrag ist angelegt
    ACCOUNTED, // Position wurde abgerechnet                                   abgerechnet
    CANCELED,  // Position wurde anulliert                                     annulliert
    UNUSED,    // Position ist nicht benutzt                                   nicht benutzt
    COPY;      // Position wurde kopiert                                       kopiert
    //</code>

    @Override
    public String toString() {
        return NLS.get(OfferItemState.class.getSimpleName() + "." + name());
    }

    public String getOfferItemStatePostFix() {
        OfferItemState o =this;
        int i = 0;
        if(OfferItemState.OFFER.equals(o)) i= 9;
        if(OfferItemState.ORDERED.equals(o)) i= 11;
        if(OfferItemState.CONFIRMED.equals(o)) i= 8;
        if(OfferItemState.DEVELOPED.equals(o)) i= 7;
        if(OfferItemState.ACCEPTED.equals(o)) i= 1;
        if(OfferItemState.ACCOUNTED.equals(o)) i= 3;
        if(OfferItemState.CANCELED.equals(o)) i= 7;
        if(OfferItemState.UNUSED.equals(o)) i= 1;
        if(OfferItemState.COPY.equals(o)) i= 13;
        String s = "                                                                                    ";
        return s.substring(0,i);
    }

}
