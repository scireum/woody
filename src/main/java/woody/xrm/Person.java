/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.AddressData;
import sirius.biz.model.BizEntity;
import sirius.biz.model.ContactData;
import sirius.biz.model.LoginData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.TenantAware;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;
import sirius.mixing.annotations.Trim;

/**
 * Created by aha on 06.10.15.
 */
public class Person extends BizEntity {

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    private final PersonData person = new PersonData();
    public static final Column PERSON = Column.named("person");

    private final AddressData address = new AddressData();
    public static final Column ADDRESS = Column.named("addressdata");

    private final ContactData contact = new ContactData();
    public static final Column CONTACT = Column.named("contactdata");

    private final LoginData login = new LoginData();
    public static final Column LOGIN = Column.named("login");

    @Trim
    @Length(length = 100)
    @NullAllowed
    private String position;
    public static final Column POSITION = Column.named("position");

    private boolean itDecider;
    public static final Column ITDECIDER = Column.named("itDecider");

    private boolean marketingDecider;
    public static final Column MARKETINGDECIDER = Column.named("marketingDecider");

    private boolean salesDecider;
    public static final Column SALESDECIDER = Column.named("salesDecider");

    private boolean management;
    public static final Column MANAGEMENT = Column.named("management");

    public EntityRef<Company> getCompany() {
        return company;
    }

    public PersonData getPerson() {
        return person;
    }

    public LoginData getLogin() {
        return login;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isItDecider() {
        return itDecider;
    }

    public void setItDecider(boolean itDecider) {
        this.itDecider = itDecider;
    }

    public boolean isMarketingDecider() {
        return marketingDecider;
    }

    public void setMarketingDecider(boolean marketingDecider) {
        this.marketingDecider = marketingDecider;
    }

    public boolean isSalesDecider() {
        return salesDecider;
    }

    public void setSalesDecider(boolean salesDecider) {
        this.salesDecider = salesDecider;
    }

    public boolean isManagement() {
        return management;
    }

    public void setManagement(boolean management) {
        this.management = management;
    }

    public ContactData getContact() {
        return contact;
    }

    public AddressData getAddress() {
        return address;
    }
}
