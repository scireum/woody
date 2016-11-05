/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import sirius.kernel.nls.NLS;

/**
 * Created by gerhardhaufler on 18.04.14.
 */
public enum OfferItemState {
    // NEVER CHANGE THIS ORDER!
    OFFER,   // Angebot
    ORDERED, // Position wurde vom Kunden bestellt
    CONFIRMED, // Auftragsbestätigung für diese Position wurde versendet
    DEVELOPED,   // Position wurde von scireum entwickelt
    ACCEPTED, // Position wurde vom Kunden abgenommen
    ACCOUNTED, // Position wurde abgerechnet
    CANCELED,  // Position wurde anulliert
    UNUSED,  // Position ist nicht benutzt
    COPY;    // Position wurde kopiert

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
        StringBuffer s = new StringBuffer();
        for(int k = 0; k<i; k++) {
          s.append("&nbsp;");
        }
        return s.toString();
    }

}
