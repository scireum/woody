/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;


import sirius.biz.jdbc.model.BizEntity;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 18.09.16.
 */

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

    public static final String LINEITEMTYPE_LA = "LA"; // Licence-Lineitem
    public static final String LINEITEMTYPE_OA = "OA"; // Offer-Lineitem

    public static final String LINEITEMSTATUS_NEW = "NEW";             // Neu
    public static final String LINEITEMSTATUS_ACCOUNTED = "ACCOUNTED"; // Abgerechnet (in Collmex)
    public static final String LINEITEMSTATUS_IS_ZERO = "IS_ZERO";     // Wert ist null, keine Abrechnung

    /* type of the lineitem, license-lineitem or offer-lineitem */
    @Length(2)
    private String lineitemType = LINEITEMTYPE_LA;
    public static final Mapping LINEITEMTYPE = Mapping.named("lineitemType");

    /* reference-Date of the accounting */
    private LocalDate referenceDate;
    public static final Mapping REFERENCEDATE = Mapping.named("referenceDate");

    /* all lineitems of the same accounting-job have the same timestamp */
    private LocalDateTime accountingDate;
    public static final Mapping ACCOUNTINGDATE = Mapping.named("accountingDate");

    /* date when the lineitem is exported to collmex, the state changes from NEW to ACCOUNTED */
    private LocalDateTime exportDate;
    public static final Mapping EXPORTDATE = Mapping.named("exportDate");

    /* name of the company */
    @Length(255)
    private String companyName;
    public static final Mapping COMPANYNAME = Mapping.named("companyName");

    /* the customer-number, refers in collmex to the company */
    @Length(20)
    private String customerNr;
    public static final Mapping CUSTOMERNR = Mapping.named("customerNr");

    /* relative negative invoice-numbers to separate the invoices */
    private Long invoiceNr;
    public static final Mapping INVOICENR = Mapping.named("invoiceNr");

    /* quantity of the accounted item */
    @Numeric(scale = 3, precision = 15)
    private Amount quantity = Amount.NOTHING;
    public static final Mapping QUANTITY = Mapping.named("quantity");

    /* 100% of the price of 1 item */
    @Numeric(scale = 3, precision = 15)
    private Amount price = Amount.NOTHING;
    public static final Mapping PRICE = Mapping.named("price");

    /* name of the package */
    @Length(255)
    private String packageName;
    public static final Mapping PACKAGENAME = Mapping.named("packageName");

    /* status of the lineitem, e.g. NEW or ACCOUNTED or IS_ZERO  */
    @Length(20)
    private String status;
    public static final Mapping STATUS = Mapping.named("status");

    /* position-Number */
    private int position = 0;
    public static final Mapping POSITION = Mapping.named("position");

    /* measurement for the quantity (quantity-unit) */
    @Length(20)
    private String measurement;
    public static final Mapping MEASUREMENT = Mapping.named("measurement");

    /* article-name for analyzes in collmex */
    @Length(20)
    private String article;
    public static final Mapping ARTICLE = Mapping.named("article");

    /* flag: this lineitem is a credit (Gutschrift) */
    private boolean credit;
    public static final Mapping CREDIT = Mapping.named("credit");

    /* flag: this lineitem is handled at the export to collmex as a credit */
    private boolean collmexCredit;
    public static final Mapping COLLMEXCREDIT = Mapping.named("collmexCredit");

    /* the position-discount is a relativ discount in percent for this lineitem */
    /* collmex calculates: value = price * ((100-positionDiscount) / 100)       */
    @Numeric(scale = 3, precision = 15)
    private Amount positionDiscount = Amount.NOTHING;
    public static final Mapping POSITION_DISCOUNT = Mapping.named("positionDiscount");

    /* the finalDiscountAmount is a reduction in money for this lineitem */
    /* export-price = price - finalDiscountAmount                        */
    @Numeric(scale = 3, precision = 15)
    private Amount finalDiscountAmount = Amount.NOTHING;
    public static final Mapping FINAL_DISCOUNT_AMOUNT = Mapping.named("finalDiscountAmount");

    /* not used */
    @Numeric(scale = 3, precision = 15)
    private Amount finalDiscountSum = Amount.NOTHING;
    public static final Mapping FINAL_DISCOUNT_SUM = Mapping.named("finalDiscountSum");

    /* the positionType of this lineitem in collmex, constants see at the top */
    private int positionType;
    public static final Mapping POSITIONTYPE = Mapping.named("positionType");

    /* description of the lineitem */
    @Length(1000)
    private String description;
    public static final Mapping DESCRIPTION = Mapping.named("description");

    /* code for the language of the invoice */
    @Length(1)
    private String outputLanguage = "0";
    public static final Mapping OUTPUTLANGUAGE = Mapping.named("outputLanguage");

    public String toString() {
        StringBuilder sb = new StringBuilder();
        asString(sb);
        return sb.toString();
    }

    protected void asString(StringBuilder sb) {
        sb.append(lineitemType == null ? LINEITEMTYPE_LA : lineitemType);
        sb.append("/");
        sb.append(NLS.toUserString(getAccountingDate()));
        sb.append(":");
        sb.append(companyName);
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
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

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public LocalDateTime getAccountingDate() {
        return accountingDate;
    }

    public void setAccountingDate(LocalDateTime accountingDate) {
        this.accountingDate = accountingDate;
    }

    public LocalDateTime getExportDate() {
        return exportDate;
    }

    public void setExportDate(LocalDateTime exportDate) {
        this.exportDate = exportDate;
    }
}
