/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.kernel.nls.NLS;

import java.util.Calendar;
import java.util.Date;

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
     * eg. date = 1. Apr 2011, accountingIntervalType =
     * QUARTER  --> 1. Apr. 2011
     * HALFYEAR --> 1. Jul. 2011
     * YEAR     --> 1. Jan. 2012
     * eg. date = 2. Apr 2011, accountingIntervalType =
     * QUARTER  --> 1. Jul. 2011
     * HALFYEAR --> 1. Jul. 2011
     * YEAR     --> 1. Jan. 2012
     */
    public Date getNextAccountingStartDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal = clearTimeFields(cal);
        int year = cal.get(Calendar.YEAR);
        Calendar firstApril =setCalendar(1,4,year);
        Calendar firstJuly =setCalendar(1,7,year);
        Calendar firstOct =setCalendar(1,10,year);
        Calendar firstJan =setCalendar(1,1,year+1);

        if (this == YEAR) {
            return firstJan.getTime();
        }
        if (this == HALFYEAR) {
            if (!cal.after(firstJuly)) {
                return firstJuly.getTime();
            } else {
                return firstJan.getTime();
            }
        }
        if (this == QUARTER) {
            if (!cal.after(firstApril)) {
                return firstApril.getTime();
            }
            if (!cal.after(firstJuly)) {
                return firstJuly.getTime();
            }
            if (!cal.after(firstOct)) {
                return firstOct.getTime();
            }
            if (!cal.after(firstJan)) {
                return firstJan.getTime();
            }
        }
        return null;
    }

    private Calendar setCalendar(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.YEAR, year);
        cal = clearTimeFields(cal);
        return cal;
    }

    /**
     * Clears all TOD (time of day) information in the given calendar.
     */
    private Calendar clearTimeFields(Calendar cal) {
        cal.clear(Calendar.HOUR);
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        return cal;
    }


    /**
     * returns the accountingInterval in months
     * @return 	-1: there is an incorrect, not known accountingIntervalType
     * 			>0: accountingInterval in months
     */
    public int getAccountingIntervalInMonth() {
        if(this == YEAR) {
            return 12;
        }
        if(this == HALFYEAR) {
            return 6;
        }
        if(this == QUARTER) {
            return 3;
        }
        return -1;

    }

}
