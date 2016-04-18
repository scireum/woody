/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.TenantAware;
import sirius.kernel.commons.Amount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.OMA;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
public class PackageDefinition extends BizEntity {

    private final EntityRef<Product> product = EntityRef.on(Product.class, EntityRef.OnDelete.CASCADE);
    public static final Column PRODUCT = Column.named("product");

    @Trim
    @Unique(within = "tenant")
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    private AccountingProcedure accountingProcedure;
    public static final Column ACCOUNTINGPROCEDURE = Column.named("accountingProcedure");

    private PacketType packetType;
    public static final Column PACKETTYPE = Column.named("packetType");

    private AccountingUnitType accountingUnit;
    public static final Column ACCOUNTINGUNIT = Column.named("accountingUnit");

    private int defaultPosition;
    public static final Column DEFAULTPOSITION = Column.named("defaultPosition");

    @NullAllowed
    @Length(scale = 3, precision = 15)
    private Amount singlePrice;
    public static final Column SINGLEPRICE = Column.named("singlePrice");

    @Length(scale = 3, precision = 15)
    private Amount unitPrice;
    public static final Column UNITPRICE = Column.named("unitPrice");


    /*            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_NO_LABEL, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_MAXIMIZED, value = "false"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_SYNTAX, value = TextArea.MARKDOWN) })
    @Lob
    @Column(name = PARAMETER, nullable = true)        */

    @Length(length = 1000)
    @NullAllowed
    private String parameter;
    public static final Column PARAMETER = Column.named("parameter");

 /*   @TableColumn(position = 17)
    @FormField(position = 14, section = "description")
    @Params({
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_NO_LABEL, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_MAXIMIZED, value = "false"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_SYNTAX, value = TextArea.MARKDOWN) })
    @Lob
    @Column(name = DESCRIPTION, nullable = true)   */
    @Length(length = 1000)
    @NullAllowed private String description;
    public static final Column DESCRIPTION = Column.named("description");


    @Override
    public String toString() {
        return getName();
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

    public AccountingProcedure getAccountingProcedure() {
        return accountingProcedure;
    }

    public void setAccountingProcedure(AccountingProcedure accountingProcedure) {
        this.accountingProcedure = accountingProcedure;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
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

    public void setAccountingUnit(AccountingUnitType accountingUnit) {
        this.accountingUnit = accountingUnit;
    }

    public AccountingUnitType getAccountingUnit() {
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
