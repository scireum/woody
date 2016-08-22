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
import sirius.db.mixing.Column;
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
    private String customerNr;
    public static final Column CUSTOMERNR = Column.named("customerNr");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String homepage;
    public static final Column HOMEPAGE = Column.named("homepage");

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

    private final AddressData address = new AddressData(AddressData.Requirements.NOT_PARTIAL, null);
    public static final Column ADDRESS = Column.named("address");

    @NullAllowed
    @Autoloaded
    private final AddressData postboxAddress =
            new AddressData(AddressData.Requirements.NOT_PARTIAL, NLS.get("Company.postboxAddress"));
    public static final Column POSTBOXADDRESS = Column.named("postboxAddress");

    @NullAllowed
    @Autoloaded
    private final ContactData contactData = new ContactData(true);
    public static final Column CONTACTDATA = Column.named("contactData");

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

    public String getCustomerNr() {
        return customerNr;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = customerNr;
    }

    public AddressData getAddress() {
        return address;
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
