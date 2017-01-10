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
import sirius.biz.model.InternationalAddressData;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.nls.NLS;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

/**
 * Created by aha on 06.10.15.
 */
public class Company extends TenantAware {

    @Trim
    @Autoloaded
    @Unique(within = "tenant")
    @Length(255)
    private String name;
    public static final Column NAME = Column.named("name");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String name2;
    public static final Column NAME2 = Column.named("name2");

    @Trim
    @Autoloaded
    @Unique
    @NullAllowed
    @Length(50)
    private String customerNumber;
    public static final Column CUSTOMER_NUMBER = Column.named("customerNumber");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String website;
    public static final Column WEBSITE = Column.named("website");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String matchcode;
    public static final Column MATCHCODE = Column.named("matchcode");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String image;
    public static final Column IMAGE = Column.named("image");

    public static final Column ADDRESS = Column.named("address");
    private final InternationalAddressData address =
            new InternationalAddressData(AddressData.Requirements.NOT_PARTIAL, null);

    private final InternationalAddressData postboxAddress =
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL,
                                         NLS.get("Company.postboxAddress"));
    public static final Column POSTBOXADDRESS = Column.named("postboxAddress");

    private final ContactData contactData = new ContactData(true);
    public static final Column CONTACT_DATA = Column.named("contactData");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    @Override
    public String toString() {
        if (!isNew()) {
            return getName();
        } else {
            return super.toString();
        }
    }

    @BeforeSave
    protected void onSave() {
//        // check the presence of a customer-number if contracts are existing
//        long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
//        if (count > 0 && Strings.isEmpty(customerNr)) {
//            throw Exceptions.createHandled().withNLSKey("Company.ContractsArePresent.CustomerNrIsMissing").handle();
//        }
//        // normalize the mainPhoneNr
//        if (Strings.isFilled(this.getMainPhoneNr())) {
//            this.setMainPhoneNr(normalizePhoneNumber(this.getMainPhoneNr()));
//        }
//        //check the mainMailAddress
//        if (Strings.isFilled(this.getMainMailAddress())) {
//            // mimimal: a.b
//            if (this.getMainMailAddress().length() < 3) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Model.mainMailAddressToShort")
//                                .set("value", this.getMainMailAddress())
//                                .handle();
//            } else {
//                // cut out the @   aa@bbbbb.cc ---> bbbbb.cc
//                int pos = this.getMainMailAddress().indexOf("@");
//                if (pos > -1) {
//                    this.setMainMailAddress(this.getMainMailAddress().substring(pos + 1));
//                }
//                // check the presence of the "."
//                pos = this.getMainMailAddress().indexOf(".");
//                // missing   or  at the last index
//                if (pos == -1 || pos == this.getMainMailAddress().length() - 1) {
//                    throw Exceptions.createHandled()
//                                    .withNLSKey("Model.mainMailAddressError")
//                                    .set("value", this.getMainMailAddress())
//                                    .handle();
//                }
//            }
//        }
//
//        // check the CompanyAccountingData
//        String invoiceMedium = this.getCompanyAccountingData().getInvoiceMedium();
//        if (invoiceMedium == null) {
//            throw Exceptions.createHandled().withNLSKey("Company.invoiceMediumMissing").handle();
//        }
//        if (invoiceMedium.equals("MAIL")) {
//            String mailAddress = this.getCompanyAccountingData().getInvoiceMailAdr();
//            if (Strings.isEmpty(mailAddress)) {
//                throw Exceptions.createHandled().withNLSKey("Company.invoiceMailAdrMissing").handle();
//            }
//            if (!(mails.isValidMailAddress(mailAddress, null))) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Company.invalidInvoiceEmail")
//                                .set("value", mailAddress)
//                                .handle();
//            }
//        }
    }

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

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public InternationalAddressData getAddress() {
        return address;
    }

    public String getMatchcode() {
        return matchcode;
    }

    public void setMatchcode(String matchcode) {
        this.matchcode = matchcode;
    }

    public InternationalAddressData getPostboxAddress() {
        return postboxAddress;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
}
