/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.timerecording;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.kernel.commons.Amount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;

/**
 * Created by aha on 10.05.15.
 */
public class YearlyTimeRecord extends BizEntity {

    private final EntityRef<UserAccount> user = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column USER = Column.named("user");

    private int year;
    public static final Column YEAR = Column.named("year");

    @NullAllowed
    @Length(precision = 6, scale = 2)
    private Amount numberOfHolidays = Amount.ZERO;
    public static final Column NUMBER_OF_HOLIDAYS = Column.named("numberOfHolidays");

    @NullAllowed
    @Length(precision = 6, scale = 2)
    private Amount numberOfHolidaysLeft = Amount.ZERO;
    public static final Column NUMBER_OF_HOLIDAYS_LEFT = Column.named("numberOfHolidaysLeft");

    @NullAllowed
    @Length(precision = 6, scale = 2)
    private Amount numberOfHolidaysProbablyLeft = Amount.ZERO;
    public static final Column NUMBER_OF_HOLIDAYS_PROBABLY_LEFT = Column.named("numberOfHolidaysProbablyLeft");

    @NullAllowed
    @Length(length = 1024)
    private String comment;

    public EntityRef<UserAccount> getUser() {
        return user;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Amount getNumberOfHolidays() {
        return numberOfHolidays;
    }

    public void setNumberOfHolidays(Amount numberOfHolidays) {
        this.numberOfHolidays = numberOfHolidays;
    }

    public Amount getNumberOfHolidaysLeft() {
        return numberOfHolidaysLeft;
    }

    public void setNumberOfHolidaysLeft(Amount numberOfHolidaysLeft) {
        this.numberOfHolidaysLeft = numberOfHolidaysLeft;
    }

    public Amount getNumberOfHolidaysProbablyLeft() {
        return numberOfHolidaysProbablyLeft;
    }

    public void setNumberOfHolidaysProbablyLeft(Amount numberOfHolidaysProbablyLeft) {
        this.numberOfHolidaysProbablyLeft = numberOfHolidaysProbablyLeft;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
