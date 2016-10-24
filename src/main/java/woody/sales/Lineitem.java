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
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 18.09.16.
 */
//@Entity
//@Table(name = "lineitem")
//@ListSpec(orderBy = { @OrderSpec(path = "invoiceNr"),
//                      @OrderSpec(path = "position") })

public class Lineitem extends BizEntity {


    // Collmex-PositionType
    public static final int COLLMEX_NORMALPOSITION = 0;
    public int getLineitemCollmexNormalposition() {
        return  COLLMEX_NORMALPOSITION;
    }

    // private static final int COLLMEX_SUMMENPOSITION = 1; // current not used
    public static final int COLLMEX_TEXTPOSITION = 2;
    public int getLineitemCollmexTextposition() {
        return  COLLMEX_TEXTPOSITION;
    }
    // private static final int COLLMEX_KOSTENLOSPOSITION = 3; // current not used

    public static final String LINEITEMTYPE_LA = "LA";
    public static final String LINEITEMTYPE_OA = "OA";

    public static final String LINEITEMSTATUS_NEW = "NEW";        // Neu
    public static final String LINEITEMSTATUS_ACCOUNTED = "ACCOUNTED"; // Abgerechnet (in Collmex)
    public static final String LINEITEMSTATUS_IS_ZERO = "S_ZERO";    // Wert ist null, keine Abrechnung

    @Length(2)
    private String lineitemType = LINEITEMTYPE_LA;
    public static final Column LINEITEMTYPE = Column.named("lineitemType");

    private LocalDate lineitemDate;
    public static final Column LINEITEMDATE = Column.named("lineitemDate");

    @Length(255)
    private String companyName;
    public static final Column COMPANYNAME = Column.named("companyName");


    @Length(20)
    private String customerNr;
    public static final Column CUSTOMERNR = Column.named("customerNr");


    private Long invoiceNr;
    public static final Column INVOICENR = Column.named("invoiceNr");

    @Numeric(scale = 3, precision = 15)
    private Amount quantity;
    public static final Column QUANTITY = Column.named("quantity");

    @Numeric(scale = 3, precision = 15)
    private Amount price;
    public static final Column PRICE = Column.named("price");

    @Length(255)
    private String packageName;
    public static final Column PACKAGENAME = Column.named("packageName");

    @Length(20)
    private String status;
    public static final Column STATUS = Column.named("status");

    private LocalDateTime clearingDate;
    public static final Column CLEARINGDATE = Column.named("clearingDate");

    private int position = 0;
    public static final Column POSITION = Column.named("position");

    @Length(20)
    private String measurement;
    public static final Column MEASUREMENT = Column.named("measurement");

    @Length(20)
    private String article;
    public static final Column ARTICLE = Column.named("article");

    private boolean credit;
    public static final Column CREDIT = Column.named("credit");

    private boolean collmexCredit;
    public static final Column COLLMEXCREDIT = Column.named("collmexCredit");

    @Numeric(scale = 3, precision = 15)
    private Amount positionDiscount;
    public static final Column POSITION_DISCOUNT = Column.named("positionDiscount");

    @Numeric(scale = 3, precision = 15)
    private Amount finalDiscountAmount;
    public static final Column FINAL_DISCOUNT_AMOUNT = Column.named("finalDiscountAmount");

    @Numeric(scale = 3, precision = 15)
    private Amount finalDiscountSum;
    public static final Column FINAL_DISCOUNT_SUM = Column.named("finalDiscountSum");

    private int positionType;
    public static final Column POSITIONTYPE = Column.named("positionType");

    @Length(1000)
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    public LocalDate getLineitemDate() {
        return lineitemDate;
    }

    public void setLineitemDate(LocalDate lineitemDate) {
        this.lineitemDate = lineitemDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = customerNr;
    }

    public String getCustomerNr() {
        return customerNr;
    }

    public Amount getQuantity() {
        return quantity;
    }

    public void setQuantity(Amount quantity) {
        this.quantity = quantity;
    }

    public Amount getPrice() {
        return price;
    }

    public void setPrice(Amount price) {
        this.price = price;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public LocalDateTime getClearingDate() {
        return clearingDate;
    }

    public void setClearingDate(LocalDateTime clearingDate) {
        this.clearingDate = clearingDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public Amount getFinalDiscountSum() {
        return finalDiscountSum;
    }

    public void setFinalDiscountSum(Amount finalDiscountSum) {
        this.finalDiscountSum = finalDiscountSum;
    }

    public Amount getFinalDiscountAmount() {
        return finalDiscountAmount;
    }

    public void setFinalDiscountAmount(Amount finalDiscountAmount) {
        this.finalDiscountAmount = finalDiscountAmount;
    }

    public int getPositionType() {
        return positionType;
    }

    public void setPositionType(int positionType) {
        this.positionType = positionType;
    }

    public Amount getPositionDiscount() {
        return positionDiscount;
    }

    public void setPositionDiscount(Amount positionDiscount) {
        this.positionDiscount = positionDiscount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getInvoiceNr() {
        return invoiceNr;
    }

    public void setInvoiceNr(Long invoiceNr) {
        this.invoiceNr = invoiceNr;
    }

    public boolean isCollmexCredit() {
        return collmexCredit;
    }

    public void setCollmexCredit(boolean collmexCredit) {
        this.collmexCredit = collmexCredit;
    }

    public int getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public String getLineitemType() {
        return lineitemType;
    }

    public void setLineitemType(String lineitemType) {
        this.lineitemType = lineitemType;
    }

    protected void asString(StringBuilder sb) {
        sb.append(lineitemType == null ? LINEITEMTYPE_LA : lineitemType);
        sb.append("/");
        sb.append(NLS.toUserString(getLineitemDate()));
        sb.append(":");
        sb.append(companyName);
    }

}
