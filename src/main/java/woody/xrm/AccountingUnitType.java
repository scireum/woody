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
 * Created by gerhardhaufler on 09.02.16.
 */
public enum AccountingUnitType {
    HOUR, DAY, MONTH;

    @Override
    public String toString() {
        return NLS.get(AccountingUnitType.class.getSimpleName() + "." + name());
    }

}
