/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
import sirius.biz.model.ContactData;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.mixing.Column;
import sirius.mixing.OMA;
import sirius.mixing.annotations.BeforeSave;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;
import sirius.web.mails.Mails;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

/**
 * Created by aha on 06.10.15.
 */
//@Framework("companies")
public class Company extends TenantAware {

    // Company-Name
    @Trim
    @Autoloaded
    @Unique(within = "tenant")
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    @NullAllowed
    @Autoloaded
    @Length(length = 255)
    private String name2;
    public static final Column NAME2 = Column.named("name2");

    @Trim
    @Autoloaded
    @NullAllowed
    @Length(length = 50)
    private String companyType;
    public static final Column COMPANYTYPE = Column.named("companyType");

    @Trim
    @Autoloaded
    @NullAllowed
    @Length(length = 50)
    private String businessType;
    public static final Column BUSINESSTYPE = Column.named("businessType");

    @Trim
    @Autoloaded
    @Unique
    @NullAllowed
    @Length(length = 50)
    private String customerNr;
    public static final Column CUSTOMERNR = Column.named("customerNr");

    @NullAllowed
    @Autoloaded
    @Length(length = 255)
    private String homepage;
    public static final Column HOMEPAGE = Column.named("homepage");

    @NullAllowed
    @Autoloaded
    @Length(length = 255)
    private String matchcode;
    public static final Column MATCHCODE = Column.named("matchcode");

    @NullAllowed
    @Autoloaded
    @Length(length = 255)
    private String image;
    public static final Column IMAGE = Column.named("image");

    private final AddressData address = new AddressData();
    public static final Column ADDRESS = Column.named("address");

    @NullAllowed
    @Autoloaded
    private final AddressData postboxAddress = new AddressData();
    public static final Column POSTBOXADDRESS = Column.named("postboxAddress");

    @NullAllowed
    @Autoloaded
    private final ContactData contactData = new ContactData();
    public static final Column CONTACTDATA = Column.named("contactData");

    //------------------ Abrechnung -------------------------------------------
    @NullAllowed
    @Autoloaded
    private final AddressData invoiceAddress = new AddressData();
    public static final Column INVOICE_ADDRESS = Column.named("invoiceAddress");

    @NullAllowed
    @Autoloaded
    @Length(scale = 3, precision = 15)
    private Amount ptPrice;
    public static final Column PTPRICE = Column.named("ptPrice");

    @NullAllowed
    @Autoloaded
    private String invoiceMedium;
    public static final String INVOICEMEDIUM = "invoiceMedium";

    @Trim
    @Autoloaded
    @NullAllowed
    @Length(length = 255)
    private String invoiceMailAdr;
    public static final Column INVOICE_MAIL_ADR = Column.named("invoiceMailAdr");

    // -----------------------------------------------------------------------
    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    @BeforeSave
    protected void onSave() {
        // check the invoiceMedium == MAIL
        if (Strings.areEqual("MAIL", getInvoiceMedium())) {
            if (Strings.isEmpty(getInvoiceMailAdr())) {
                throw Exceptions.createHandled()
                          .withNLSKey("woody.xrm.Company.invoiceMailAdrMissing").handle();
            } else {
                if(!(mails.isValidMailAddress(getInvoiceMailAdr(), null))) {
                    throw Exceptions.createHandled()
                                    .withNLSKey("woody.xrm.Company.invalidInvoiceEmail")
                                    .set("value", this.getInvoiceMailAdr()).handle();
                }
            }
        }
        // check the presence of a customer-number if contracts are existing
        long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
        if (count > 0 && Strings.isEmpty(customerNr)) {
            throw Exceptions.createHandled()
                            .withNLSKey("woody.xrm.Company.ContractsArePresent.CustomerNrIsMissing")
                            .handle();
        }
    }


    @Part
    private static Mails mails;
    @Part
    private static OMA oma;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getCustomerNr() {
        return customerNr;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = customerNr;
    }

    public String getInvoiceMailAdr() {
        return invoiceMailAdr;
    }

    public void setInvoiceMailAdr(String invoiceEmailAdr) {
        this.invoiceMailAdr = invoiceEmailAdr;
    }

    public AddressData getAddress() {
        return address;
    }

    public AddressData getInvoiceAddress() {
        return invoiceAddress;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getMatchcode() {
        return matchcode;
    }

    public void setMatchcode(String matchcode) {
        this.matchcode = matchcode;
    }

    public AddressData getPostboxAddress() {
        return postboxAddress;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getInvoiceMedium() {
        return invoiceMedium;
    }

    public void setInvoiceMedium(String invoiceMedium) {
        this.invoiceMedium = invoiceMedium;
    }

    public Tagged getTags() {
        return tags;
    }

    public Commented getComments() {
        return comments;
    }

    public ContactData getContactData() {
        return contactData;

    }

    public Amount getPtPrice() {
        return ptPrice;
    }

    public void setPtPrice(Amount ptPrice) {
        this.ptPrice = ptPrice;
    }

}
