/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.kernel.nls.NLS;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
public enum AccountingProcedure {

    RIVAL, // konkurrierende Lizenz für CPS bzw. OXOMI bzw sellSite
    VOLUME; // Volumen-Lizenz, z. B. für Volumen-Erweiterungen

    @Override
    public String toString() {
        return NLS.get(AccountingProcedure.class.getName() + "." + name());
    }

}
