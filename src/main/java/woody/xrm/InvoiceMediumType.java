/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.kernel.nls.NLS;

/**
 * Created by gerhardhaufler on 05.02.16.
 */
public enum InvoiceMediumType {
    PRINT,         // Ausdruck
    MAIL;          // Versand als Mail

    @Override
    public String toString() {
        return NLS.get(InvoiceMediumType.class.getName() + "." + name());
    }

}
