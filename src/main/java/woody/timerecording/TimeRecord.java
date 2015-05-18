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

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by aha on 10.05.15.
 */
public class TimeRecord extends BizEntity {

    private final EntityRef<UserAccount> user = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column USER = Column.named("user");

    private LocalDate day;
    public static final Column DAY = Column.named("day");

    private LocalTime timeStart;
    public static final Column TIME_START = Column.named("timeStart");

    @Nullable
    private LocalTime timeEnd;
    public static final Column TIME_END = Column.named("timeEnd");

    @Nullable
    @Length(precision = 4, scale = 2)
    private Amount pauseInHours;
    public static final Column PAUSE_IN_HOURS = Column.named("pauseInHours");

    public EntityRef<UserAccount> getUser() {
        return user;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public LocalTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = timeStart;
    }

    @Nullable
    public LocalTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(@Nullable LocalTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    @Nullable
    public Amount getPauseInHours() {
        return pauseInHours;
    }

    public void setPauseInHours(@Nullable Amount pauseInHours) {
        this.pauseInHours = pauseInHours;
    }
}
