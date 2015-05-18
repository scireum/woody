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

/**
 * Created by aha on 10.05.15.
 */
public class MonthlyTimeRecord extends BizEntity {

    private final EntityRef<UserAccount> user = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column USER = Column.named("user");

    private int year;
    public static final Column YEAR = Column.named("year");

    private int month;
    public static final Column MONTH = Column.named("month");

    @Length(precision = 6, scale = 2)
    private Amount overtime = Amount.ZERO;
    public static final Column OVERTIME = Column.named("overtime");

    @Length(precision = 6, scale = 2)
    private Amount aggregatedOvertime = Amount.ZERO;
    public static final Column AGGREGATED_OVERTIME = Column.named("aggregatedOvertime");

    public EntityRef<UserAccount> getUser() {
        return user;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Amount getOvertime() {
        return overtime;
    }

    public void setOvertime(Amount overtime) {
        this.overtime = overtime;
    }

    public Amount getAggregatedOvertime() {
        return aggregatedOvertime;
    }

    public void setAggregatedOvertime(Amount aggregatedOvertime) {
        this.aggregatedOvertime = aggregatedOvertime;
    }
}
