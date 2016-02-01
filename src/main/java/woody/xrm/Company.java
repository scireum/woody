/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
import sirius.biz.tenants.TenantAware;
import sirius.mixing.Column;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

/**
 * Created by aha on 06.10.15.
 */
public class Company extends TenantAware {

    @Trim
    @Unique(within = "tenant")
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    @Trim
    @Unique
    @NullAllowed
    @Length(length = 50)
    private String accountNumber;
    public static final Column ACCOUNT_NUMBER = Column.named("accountNumber");

    @Trim
    @NullAllowed
    @Length(length = 150)
    private String email;
    public static final Column EMAIL = Column.named("email");

    @Trim
    @NullAllowed
    @Length(length = 150)
    private String phone;
    public static final Column PHONE = Column.named("phone");

    @Trim
    @NullAllowed
    @Length(length = 150)
    private String fax;
    public static final Column FAX = Column.named("fax");

    @Trim
    @NullAllowed
    @Length(length = 150)
    private String invoiceEmail;
    public static final Column INVOICE_EMAIL = Column.named("invoiceEmail");

    private final AddressData address = new AddressData();
    public static final Column ADDRESS = Column.named("address");

    private final AddressData invoiceAddress = new AddressData();
    public static final Column INVOICE_ADDRESS = Column.named("invoiceAddress");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    public AddressData getAddress() {
        return address;
    }

    public AddressData getInvoiceAddress() {
        return invoiceAddress;
    }
}
