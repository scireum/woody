/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.offers;

import sirius.kernel.nls.NLS;

/**
 * Created by gerhardhaufler on 18.04.14.
 */
public enum OfferItemType {
    // NEVER CHANGE THIS ORDER!
    SERVICE,   // Service (Dienstleistung)
    LICENSE,   // Lizenz
    INFOTEXT,  // InfoText
    SUM;       // Summe bis zu dieser Position

    @Override
    public String toString() {
        return NLS.get(OfferItemType.class.getSimpleName() + "." + name());
    }
}
