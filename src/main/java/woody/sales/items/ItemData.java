/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.items;

import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.types.BaseEntityRef;
import sirius.kernel.commons.Amount;

public class ItemData extends Composite {

    public static final Mapping ITEM = Mapping.named("item");
    @NullAllowed
    private final SQLEntityRef<Item> item = SQLEntityRef.on(Item.class, BaseEntityRef.OnDelete.SET_NULL);

    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Length(255)
    private String name;

    public static final Mapping ITEM_NUMBER = Mapping.named("itemNumber");
    @Length(255)
    private String itemNumber;

    public static final Mapping MATCH_CODE = Mapping.named("matchCode");
    @Length(255)
    @NullAllowed
    private String matchCode;

    public static final Mapping VAT_RATE = Mapping.named("vatRate");
    @Length(10)
    private String vatRate;

    public static final Mapping QUANTITY_UNIT = Mapping.named("quantityUnit");
    @Length(10)
    private String quantityUnit;

    public static final Mapping QUANTITY = Mapping.named("quantity");
    @Numeric(scale = 3, precision = 15)
    private Amount quantity = Amount.ONE;

    public static final Mapping ONE_OFF_PRICE = Mapping.named("oneOffPrice");
    @Numeric(scale = 3, precision = 15)
    private Amount oneOffPrice = Amount.NOTHING;

    public static final Mapping ABSOLUTE_DISCOUNT = Mapping.named("absoluteDiscount");
    @Numeric(scale = 3, precision = 15)
    private Amount absoluteDiscount = Amount.NOTHING;

    public static final Mapping DISCOUNT_IN_PERCENT = Mapping.named("discountInPercent");
    @Numeric(scale = 3, precision = 15)
    private Amount discountInPercent = Amount.NOTHING;

    public static final Mapping ONE_OFF_SUM = Mapping.named("oneOffSum");
    @Numeric(scale = 3, precision = 15)
    private Amount oneOffSum = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE = Mapping.named("monthlyCharge");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyCharge = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE_ABSOLUTE_DISCOUNT = Mapping.named("monthlyChargeAbsoluteDiscount");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyChargeAbsoluteDiscount = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE_DISCOUNT_IN_PERCENT = Mapping.named("monthlyChargeDiscountInPercent");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyChargeDiscountInPercent = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE_SUM = Mapping.named("monthlyChargeSum");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyChargeSum = Amount.NOTHING;

    @BeforeSave
    protected void updateSums() {
        this.oneOffSum = getOneOffPrice().decreasePercent(discountInPercent).times(quantity).subtract(absoluteDiscount);
        this.monthlyChargeSum = getMonthlyCharge().decreasePercent(monthlyChargeDiscountInPercent)
                                                  .times(quantity)
                                                  .subtract(absoluteDiscount);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode;
    }

    public Amount getQuantity() {
        return quantity;
    }

    public void setQuantity(Amount quantity) {
        this.quantity = quantity;
    }

    public Amount getOneOffPrice() {
        return oneOffPrice;
    }

    public void setOneOffPrice(Amount oneOffPrice) {
        this.oneOffPrice = oneOffPrice;
    }

    public Amount getAbsoluteDiscount() {
        return absoluteDiscount;
    }

    public void setAbsoluteDiscount(Amount absoluteDiscount) {
        this.absoluteDiscount = absoluteDiscount;
    }

    public Amount getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(Amount discountInPercent) {
        this.discountInPercent = discountInPercent;
    }

    public Amount getMonthlyCharge() {
        return monthlyCharge;
    }

    public void setMonthlyCharge(Amount monthlyCharge) {
        this.monthlyCharge = monthlyCharge;
    }

    public Amount getMonthlyChargeAbsoluteDiscount() {
        return monthlyChargeAbsoluteDiscount;
    }

    public void setMonthlyChargeAbsoluteDiscount(Amount monthlyChargeAbsoluteDiscount) {
        this.monthlyChargeAbsoluteDiscount = monthlyChargeAbsoluteDiscount;
    }

    public Amount getMonthlyChargeDiscountInPercent() {
        return monthlyChargeDiscountInPercent;
    }

    public void setMonthlyChargeDiscountInPercent(Amount monthlyChargeDiscountInPercent) {
        this.monthlyChargeDiscountInPercent = monthlyChargeDiscountInPercent;
    }

    public Amount getOneOffSum() {
        return oneOffSum;
    }

    public void setOneOffSum(Amount oneOffSum) {
        this.oneOffSum = oneOffSum;
    }

    public Amount getMonthlyChargeSum() {
        return monthlyChargeSum;
    }

    public void setMonthlyChargeSum(Amount monthlyChargeSum) {
        this.monthlyChargeSum = monthlyChargeSum;
    }
}
