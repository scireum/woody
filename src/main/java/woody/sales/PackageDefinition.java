/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.biz.model.BizEntity;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;

import java.util.List;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
public class PackageDefinition extends BizEntity {

    public static final String PAKETTYPE_STANDARD = "STANDARD";
    public static final String PAKETTYPE_SPECIALAGREEMENT = "SPECIALAGREEMENT";

    public static final String ACCOUNTINGPROCEDURE_RIVAL = "RIVAL";
    public static final String ACCOUNTINGPROCEDURE_VOLUME = "VOLUME";

    public static final String ACCOUNTINGUNIT_HOUR = "HOUR";
    public static final String ACCOUNTINGUNIT_DAY = "DAY";
    public static final String ACCOUNTINGUNIT_MONTH = "MONTH";

    private final EntityRef<Product> product = EntityRef.on(Product.class, EntityRef.OnDelete.CASCADE);
    public static final Column PRODUCT = Column.named("product");

    @Trim
    @Unique(within = "tenant")
    @Length(255)
    private String name;
    public static final Column NAME = Column.named("name");

    @Length(20)
    private String accountingProcedure;
    public static final Column ACCOUNTINGPROCEDURE = Column.named("accountingProcedure");

    @Length(20)
    private String paketType;
    public static final Column PAKETTYPE = Column.named("paketType");

    @Length(20)
    private String accountingUnit;
    public static final Column ACCOUNTINGUNIT = Column.named("accountingUnit");

    private int defaultPosition;
    public static final Column DEFAULTPOSITION = Column.named("defaultPosition");

    @Numeric(scale = 3, precision = 15)
    private Amount singlePrice = Amount.NOTHING;
    public static final Column SINGLEPRICE = Column.named("singlePrice");

    @Numeric(scale = 3, precision = 15)
    private Amount unitPrice = Amount.NOTHING;
    public static final Column UNITPRICE = Column.named("unitPrice");

    @Length(1000)
    @NullAllowed
    private String parameter;
    public static final Column PARAMETER = Column.named("parameter");

    @Length(1000)
    @NullAllowed
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    @Override
    public String toString() {
        Product product = getProduct().getValue();
        String productname = product.getName();
        String pdName = productname.concat(" -> ");
        pdName = pdName.concat(getName());
        return pdName;
    }

    public EntityRef<Product> getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountingProcedure() {
        return accountingProcedure;
    }

    public void setAccountingProcedure(String accountingProcedure) {
        this.accountingProcedure = accountingProcedure;
    }

    public String getPaketType() {
        return paketType;
    }

    public void setPaketType(String paketType) {
        this.paketType = paketType;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public int getDefaultPosition() {
        return defaultPosition;
    }

    public void setDefaultPosition(int defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccountingUnit(String accountingUnit) {
        this.accountingUnit = accountingUnit;
    }

    public String getAccountingUnit() {
        return accountingUnit;
    }

    public String getDescription() {
        return description;
    }

    public String getParameter() {
        return parameter;
    }

    public Amount getSinglePrice() {
        return singlePrice;
    }

    public void setSinglePrice(Amount singlePrice) {
        this.singlePrice = singlePrice;
    }

    public Amount getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Amount unitPrice) {
        this.unitPrice = unitPrice;
    }
}
