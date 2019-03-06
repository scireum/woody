/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.items;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.protocol.JournalData;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.db.mixing.types.BaseEntityRef;
import sirius.kernel.commons.Amount;
import woody.xrm.Company;

public class Discount extends BizEntity {

    public static final Mapping COMPANY = Mapping.named("company");
    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, BaseEntityRef.OnDelete.CASCADE);

    public static final Mapping DISCOUNT_GROUP = Mapping.named("discountGroup");
    @Trim
    @Length(10)
    @Autoloaded
    @Unique(within = "company")
    private String discountGroup;

    public static final Mapping DISCOUNT_IN_PERCENT = Mapping.named("discountInPercent");
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount discountInPercent = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE_DISCOUNT_IN_PERCENT = Mapping.named("monthlyChargeDiscountInPercent");
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount monthlyChargeDiscountInPercent = Amount.NOTHING;

    public static final Mapping JOURNAL = Mapping.named("journal");
    private final JournalData journal = new JournalData(this);

    public SQLEntityRef<Company> getCompany() {
        return company;
    }

    public JournalData getJournal() {
        return journal;
    }

    public String getDiscountGroup() {
        return discountGroup;
    }

    public void setDiscountGroup(String discountGroup) {
        this.discountGroup = discountGroup;
    }

    public Amount getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(Amount discountInPercent) {
        this.discountInPercent = discountInPercent;
    }

    public Amount getMonthlyChargeDiscountInPercent() {
        return monthlyChargeDiscountInPercent;
    }

    public void setMonthlyChargeDiscountInPercent(Amount monthlyChargeDiscountInPercent) {
        this.monthlyChargeDiscountInPercent = monthlyChargeDiscountInPercent;
    }
}
