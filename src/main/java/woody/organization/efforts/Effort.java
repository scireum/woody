/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.health.Exceptions;
import woody.organization.BasicElement;

import java.time.LocalDate;

/**
 * Created by aha on 13.01.17.
 */
public class Effort extends BasicElement<EffortType> {

    public static final Mapping START_DATE = Mapping.named("startDate");
    @NullAllowed
    @Autoloaded
    private LocalDate startDate;

    public static final Mapping END_DATE = Mapping.named("endDate");
    @NullAllowed
    @Autoloaded
    private LocalDate endDate;

    @BeforeSave
    protected void onModify() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw Exceptions.createHandled().withNLSKey("Effort.invalidDates").handle();
        }

        if (startDate != null && endDate == null) {
            throw Exceptions.createHandled().withNLSKey("Effort.mustUseEndDate").handle();
        }
    }

    @Override
    protected SQLEntityRef<EffortType> initializeTypeRef() {
        return SQLEntityRef.on(EffortType.class, SQLEntityRef.OnDelete.REJECT);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
