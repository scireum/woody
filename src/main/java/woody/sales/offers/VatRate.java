/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.offers;

import sirius.biz.model.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;

import java.text.MessageFormat;
import java.time.LocalDate;

/**
 * Created by gerhardhaufler on 11.10.16.
 */
public class VatRate extends BizEntity {


    @Autoloaded
    @Length(3)
    private String countryCode;
    public static final Column COUNTRYCODE = Column.named("countryCode");

    @Autoloaded
    private LocalDate validFrom;
    public static final Column VALIDFROM = Column.named("validFrom");

    @NullAllowed
    @Autoloaded
    @Numeric(scale = 2, precision = 15)
    private Amount vatRate;
    public static final Column VATRATE = Column.named("vatRate");


    public String toString() {
        String s = MessageFormat.format("Land: {0}, seit {1} {2}% Steuer.", countryCode, NLS.toUserString(validFrom),
                                        NLS.toUserString(vatRate));
        return s;
    }

    @BeforeSave
    protected void onSave()  {

        countryCode = countryCode.toLowerCase();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public Amount getVatRate() {
        return vatRate;
    }

    public void setVatRate(Amount vatRate) {
        this.vatRate = vatRate;
    }
}
