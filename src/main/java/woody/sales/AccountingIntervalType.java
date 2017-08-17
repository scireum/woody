/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.kernel.nls.NLS;
import java.time.LocalDate;


/**
 * Created by gerhardhaufler on 09.02.16.
 */
public enum AccountingIntervalType {
    YEAR, HALFYEAR, QUARTER;

    @Override
    public String toString() {
        return NLS.get(AccountingIntervalType.class.getSimpleName() + "." + name());
    }

    /**
     * returns the next accountingStartDate
     * eg. date = 1. Jan. 2011, accountingIntervalType =
     * QUARTER  --> 1. Apr. 2011
     * HALFYEAR --> 1. Jul. 2011
     * YEAR     --> 1. Jan. 2012
     * eg. date = 2. Apr 2011, accountingIntervalType =
     * QUARTER  --> 1. Jul. 2011
     * HALFYEAR --> 1. Jul. 2011
     * YEAR     --> 1. Jan. 2012
     */
    public LocalDate getNextAccountingStartDate(LocalDate date) {
        int year = date.getYear();
        LocalDate firstApril = LocalDate.of(year,4,1);
        LocalDate firstJuly = LocalDate.of(year, 7, 1);
        LocalDate firstOct = LocalDate.of(year, 10,1);
        LocalDate firstJan =LocalDate.of(year+1,1,1);
        if (this == YEAR) {
            return firstJan;
        }
        if (this == HALFYEAR) {
            if (!date.isAfter(firstJuly)) {
                return firstJuly;
            } else {
                return firstJan;
            }
        }
        if (this == QUARTER) {
            if (!date.isAfter(firstApril)) {
                return firstApril;
            }
            if (!date.isAfter(firstJuly)) {
                return firstJuly;
            }
            if (!date.isAfter(firstOct)) {
                return firstOct;
            }
            if (!date.isAfter(firstJan)) {
                return firstJan;
            }
        }
        return null;

    }
}
