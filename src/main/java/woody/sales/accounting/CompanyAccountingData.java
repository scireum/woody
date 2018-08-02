/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.jdbc.model.InternationalAddressData;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;

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
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL,
                                         NLS.get("CompanyAccountingData.invoiceAddress"));
    public static final Mapping INVOICEADDRESS = Mapping.named("invoiceAddress");

    @Trim
    @Autoloaded
    @Length(255)
    private String invoiceMedium = "PRINT";
    public static final Mapping INVOICEMEDIUM = Mapping.named("invoiceMedium");

    @Trim
    @NullAllowed
    @Autoloaded
    @Length(150)
    private String invoiceMailAdr;
    public static final Mapping INVOICEMAILADR = Mapping.named("invoiceMailAdr");

    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount ptPrice;
    public static final Mapping PTPRICE = Mapping.named("ptPrice");

    @Autoloaded
    @Length(1)
    private String outputLanguage = "0";
    public static final Mapping OUTPUTLANGUAGE = Mapping.named("outputLanguage");

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
}
