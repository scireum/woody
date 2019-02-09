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
 * Created by gerhardhaufler on 24.08.2016.
 */
public enum OfferState {
    // NEVER CHANGE THIS ORDER!
    OPEN,   // Es wurde noch nichts beauftragt
    BUSY,   // Mindestens eine Position wurde bestellt
    CLOSED; // Alle Positionen sind abgerechnet oder gecancelt

    @Override
    public String toString() {
        return NLS.get(OfferState.class.getSimpleName() + "." + name());
    }

}
