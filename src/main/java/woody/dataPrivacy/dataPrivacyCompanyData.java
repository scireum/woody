/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.dataPrivacy;

import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.NullAllowed;
import woody.xrm.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 17.09.19.
 */
public class DataPrivacyCompanyData {

    @NullAllowed
    @Autoloaded
    private final EntityRef<Person> dataPrivacyPerson = EntityRef.on(Person.class, EntityRef.OnDelete.SET_NULL);
    public static final Column DATAPRIVACYPERSON = Column.named("dataPrivacyPerson");

    @NullAllowed
    @Autoloaded
    private LocalDateTime dataPrivacySendDate;
    public static final Column DATAPRIVACYSENDDATE = Column.named("dataPrivacySendDate");

    @NullAllowed
    @Autoloaded
    private LocalDateTime dataPrivacyReceivingDate;
    public static final Column DATAPRIVACYRECEIVINGDATE = Column.named("dataPrivacyReceivingDate");

    @NullAllowed
    @Autoloaded
    private LocalDate dataPrivacyAvvVersionDate;
    public static final Column DATAPRIVACYAVVVERSIONDATE = Column.named("dataPrivacyAvvVersionDate");


    public EntityRef<Person> getDataPrivacyPerson() {
        return dataPrivacyPerson;
    }

    public LocalDateTime getDataPrivacySendDate() {
        return dataPrivacySendDate;
    }

    public void setDataPrivacySendDate(LocalDateTime dataPrivacySendDate) {
        this.dataPrivacySendDate = dataPrivacySendDate;
    }

    public LocalDateTime getDataPrivacyReceivingDate() {
        return dataPrivacyReceivingDate;
    }

    public void setDataPrivacyReceivingDate(LocalDateTime dataPrivacyReceivingDate) {
        this.dataPrivacyReceivingDate = dataPrivacyReceivingDate;
    }

    public LocalDate getDataPrivacyAvvVersionDate() {
        return dataPrivacyAvvVersionDate;
    }

    public void setDataPrivacyAvvVersionDate(LocalDate dataPrivacyAvvVersionDate) {
        this.dataPrivacyAvvVersionDate = dataPrivacyAvvVersionDate;
    }
}
