/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.di.std.Part;
import sirius.kernel.nls.NLS;

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

    public static final String PAKETTYPE_CODELIST = "pakettype";
    public static final String ACCOUNTINGUNIT_CODELIST = "accountingUnit";
    public static final String ACCOUNTINGPROCEDURE_CODELIST = "accountingProcedure";

    private final SQLEntityRef<Product> product = SQLEntityRef.on(Product.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping PRODUCT = Mapping.named("product");

    @Autoloaded
    @Trim
    @Length(255)
    private String name;
    public static final Mapping NAME = Mapping.named("name");

    @Autoloaded
    @Length(20)
    private String accountingProcedure;
    public static final Mapping ACCOUNTINGPROCEDURE = Mapping.named("accountingProcedure");

    @Autoloaded
    @Length(20)
    private String paketType;
    public static final Mapping PAKETTYPE = Mapping.named("paketType");

    @Autoloaded
    @Length(20)
    private String accountingUnit;
    public static final Mapping ACCOUNTINGUNIT = Mapping.named("accountingUnit");

    @Autoloaded
    private int defaultPosition;
    public static final Mapping DEFAULTPOSITION = Mapping.named("defaultPosition");

    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    private Amount singlePrice = Amount.NOTHING;
    public static final Mapping SINGLEPRICE = Mapping.named("singlePrice");

    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    private Amount unitPrice = Amount.NOTHING;
    public static final Mapping UNITPRICE = Mapping.named("unitPrice");

    @Autoloaded
    @Length(1000)
    @NullAllowed
    private String parameter;
    public static final Mapping PARAMETER = Mapping.named("parameter");

    @Autoloaded
    @Length(1000)
    @NullAllowed
    private String description;
    public static final Mapping DESCRIPTION = Mapping.named("description");

    @Override
    public String toString() {
        Product product = getProduct().getValue();
        String productname = product.getName();
        String pdName = productname.concat(" -> ");
        pdName = pdName.concat(getName());
        return pdName;
    }

    @Part
    private static AccountingService as;

    @Part
    private static ServiceAccountingService sas;

    @BeforeSave
    private void beforeSave() {
        // check the values (interval-test)
        sas.checkValue(unitPrice, true, false, false, false, null, NLS.get("PackageDefinition.unitPrice"));
        sas.checkValue(singlePrice, true, false, false, false, null, NLS.get("PackageDefinition.singlePrice"));

        // check te parameter-syntax
        //   as.checkParameterSyntax(parameter);
    }

    public SQLEntityRef<Product> getProduct() {
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
