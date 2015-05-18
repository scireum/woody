/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.timerecording;

import sirius.biz.tenants.UserAccount;
import sirius.kernel.commons.Amount;
import sirius.mixing.Column;
import sirius.mixing.Entity;
import sirius.mixing.Mixable;
import sirius.mixing.annotations.BeforeSave;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.Mixin;

import javax.annotation.Nullable;

/**
 * Created by aha on 10.05.15.
 */
public class TargetTimePlan extends Entity {

    public enum TargetBase {
        DAY, WEEK, MONTH
    }

    @Nullable
    @Length(precision = 5, scale = 2)
    private Amount target;
    public static final Column TARGET = Column.named("target");

    @Nullable
    @Length(length = 20)
    private TargetBase targetBase;
    public static final Column TARGET_BASE = Column.named("targetBase");

    @Nullable
    @Length(precision = 6, scale = 2)
    private Amount maxOverTimePerMonth;
    public static final Column MAX_OVERTIME_PER_MONTH = Column.named("maxOverTimePerMonth");

    @Nullable
    @Length(precision = 2, scale = 2)
    private Amount autoPauseHours;
    public static final Column AUTO_PAUSE_HOURS = Column.named("autoPauseHours");

    @Nullable
    @Length(precision = 2, scale = 2)
    private Amount autoPauseDuration;
    public static final Column AUTO_PAUSE_DURATION = Column.named("autoPauseDuration");

    @Nullable
    @Length(precision = 2, scale = 2)
    private Amount numberOfHolidaysPerYear;
    public static final Column NUMBER_OF_HOLIDAYS_PER_YEAR = Column.named("numberOfHolidaysPerYear");

    @BeforeSave
    public void checkIntegrity() {
        if ((target == null) != (targetBase == null)) {
            //TODO error!
        }
    }

    @Nullable
    public Amount getTarget() {
        return target;
    }

    public void setTarget(@Nullable Amount target) {
        this.target = target;
    }

    @Nullable
    public TargetBase getTargetBase() {
        return targetBase;
    }

    public void setTargetBase(@Nullable TargetBase targetBase) {
        this.targetBase = targetBase;
    }

    @Nullable
    public Amount getMaxOverTimePerMonth() {
        return maxOverTimePerMonth;
    }

    public void setMaxOverTimePerMonth(@Nullable Amount maxOverTimePerMonth) {
        this.maxOverTimePerMonth = maxOverTimePerMonth;
    }

    @Nullable
    public Amount getAutoPauseHours() {
        return autoPauseHours;
    }

    public void setAutoPauseHours(@Nullable Amount autoPauseHours) {
        this.autoPauseHours = autoPauseHours;
    }

    @Nullable
    public Amount getAutoPauseDuration() {
        return autoPauseDuration;
    }

    public void setAutoPauseDuration(@Nullable Amount autoPauseDuration) {
        this.autoPauseDuration = autoPauseDuration;
    }

    @Nullable
    public Amount getNumberOfHolidaysPerYear() {
        return numberOfHolidaysPerYear;
    }

    public void setNumberOfHolidaysPerYear(@Nullable Amount numberOfHolidaysPerYear) {
        this.numberOfHolidaysPerYear = numberOfHolidaysPerYear;
    }
}
