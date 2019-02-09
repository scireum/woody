/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.items;

import sirius.biz.jdbc.BizEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.db.mixing.types.BaseEntityRef;
import sirius.kernel.commons.Amount;
import woody.xrm.Company;

public class PriceFactor extends BizEntity {

    public static final Mapping COMPANY = Mapping.named("company");
    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, BaseEntityRef.OnDelete.CASCADE);

    public static final Mapping PRICE_FACTOR_GROUP = Mapping.named("priceFactorGroup");
    @Trim
    @Length(10)
    @NullAllowed
    @Unique(within = "company")
    private String priceFactorGroup;

    public static final Mapping FACTOR = Mapping.named("factor");
    @Numeric(scale = 3, precision = 15)
    private Amount factor = Amount.ONE;

    public static final Mapping MONTHLY_CHARGE_FACTOR = Mapping.named("monthlyChargeFactor");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyChargeFactor = Amount.ONE;

    public SQLEntityRef<Company> getCompany() {
        return company;
    }

    public String getPriceFactorGroup() {
        return priceFactorGroup;
    }

    public void setPriceFactorGroup(String priceFactorGroup) {
        this.priceFactorGroup = priceFactorGroup;
    }

    public Amount getFactor() {
        return factor;
    }

    public void setFactor(Amount factor) {
        this.factor = factor;
    }

    public Amount getMonthlyChargeFactor() {
        return monthlyChargeFactor;
    }

    public void setMonthlyChargeFactor(Amount monthlyChargeFactor) {
        this.monthlyChargeFactor = monthlyChargeFactor;
    }
}
