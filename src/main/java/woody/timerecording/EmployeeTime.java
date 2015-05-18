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
import sirius.mixing.EntityRef;
import sirius.mixing.Mixable;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.Mixin;
import sirius.mixing.annotations.NullAllowed;

import java.time.LocalDate;

/**
 * Created by aha on 10.05.15.
 */
@Mixin(UserAccount.class)
public class EmployeeTime extends Mixable {

    @NullAllowed
    private final EntityRef<TargetTimePlan> targetTimePlan = EntityRef.on(TargetTimePlan.class, EntityRef.OnDelete.REJECT);

    @NullAllowed
    private LocalDate planActiveSince;

    @Length(precision = 6, scale = 2)
    private Amount initialOvertime;

    public EntityRef<TargetTimePlan> getTargetTimePlan() {
        return targetTimePlan;
    }

    public LocalDate getPlanActiveSince() {
        return planActiveSince;
    }

    public void setPlanActiveSince(LocalDate planActiveSince) {
        this.planActiveSince = planActiveSince;
    }

    public Amount getInitialOvertime() {
        return initialOvertime;
    }

    public void setInitialOvertime(Amount initialOvertime) {
        this.initialOvertime = initialOvertime;
    }
}
