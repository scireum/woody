/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.biz.model.AddressData;
import sirius.biz.model.BizEntity;
import sirius.biz.model.InternationalAddressData;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Composite;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.Mixable;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;

/**
 * Created by gerhardhaufler on 22.09.16.
 */

public class CompanyAccountingData extends Composite {

    public static final String[] INVOICEMEDIUMNAMES = {"PRINT", "MAIL"};
    public static final String INVOICEMEDIUM_CODELIST = "invoicemedium";
    public static final String OUTPUTLANGUAGE_CODELIST = "outputLanguage";
    public static final String COMPANYTYPE_CODELIST = "companytype";
    public static final String BUSINESSTYPE_CODELIST = "businesstype";

    @NullAllowed
    @Autoloaded
    private final InternationalAddressData invoiceAddress =
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL, NLS.get("CompanyAccountingData.invoiceAddress"));
    public static final Column INVOICEADDRESS = Column.named("invoiceAddress");

    @Trim
    @Autoloaded
    @Length(255)
    private String invoiceMedium = "PRINT";
    public static final Column INVOICEMEDIUM = Column.named("invoiceMedium");

    @Trim
    @NullAllowed
    @Autoloaded
    @Length(150)
    private String invoiceMailAdr;
    public static final Column INVOICEMAILADR = Column.named("invoiceMailAdr");

    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount ptPrice;
    public static final Column PTPRICE = Column.named("ptPrice");

    @Autoloaded
    @Length(1)
    private String outputLanguage = "0";
    public static final Column OUTPUTLANGUAGE = Column.named("outputLanguage");

    @NullAllowed
    @Autoloaded
    private final EntityRef<Person> dataPrivacyPerson = EntityRef.on(Person.class, EntityRef.OnDelete.SET_NULL);
    public static final Column DATAPRIVACYPERSON = Column.named("dataPrivacyPerson");

    @NullAllowed
    @Autoloaded
    private LocalDate dataPrivacySendDate;
    public static final Column DATAPRIVACYSENDDATE = Column.named("dataPrivacySendDate");


    public InternationalAddressData getInvoiceAddress() {
        return invoiceAddress;
    }

    public String getInvoiceMedium() {
        return invoiceMedium;
    }

    public void setInvoiceMedium(String invoiceMedium) {
        this.invoiceMedium = invoiceMedium;
    }

    public String getInvoiceMailAdr() {
        return invoiceMailAdr;
    }

    public void setInvoiceMailAdr(String invoiceMailAdr) {
        this.invoiceMailAdr = invoiceMailAdr;
    }

    public Amount getPtPrice() {
        return ptPrice;
    }

    public void setPtPrice(Amount ptPrice) {
        this.ptPrice = ptPrice;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

    public EntityRef<Person> getDataPrivacyPerson() {
        return dataPrivacyPerson;
    }

    public LocalDate getDataPrivacySendDate() {
        return dataPrivacySendDate;
    }

    public void setDataPrivacySendDate(LocalDate dataPrivacySendDate) {
        this.dataPrivacySendDate = dataPrivacySendDate;
    }
}
