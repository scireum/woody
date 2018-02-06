/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.health.Exceptions;
import woody.organization.BasicElement;

import java.time.LocalDate;

/**
 * Created by aha on 13.01.17.
 */
public class Effort extends BasicElement<EffortType> {

    public static final Column START_DATE = Column.named("startDate");
    @NullAllowed
    @Autoloaded
    private LocalDate startDate;

    public static final Column END_DATE = Column.named("endDate");
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
    protected EntityRef<EffortType> initializeTypeRef() {
        return EntityRef.on(EffortType.class, EntityRef.OnDelete.REJECT);
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
