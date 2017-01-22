/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import sirius.biz.model.BizEntity;
import sirius.biz.sequences.Sequences;
import sirius.biz.tenants.Tenant;
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Transient;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.core.employees.Employee;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by gerhardhaufler on 11.10.16.
 */

public class Offer extends BizEntity {


    private static final int MIN_OFFER_NR = 20001;

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    @Unique(within = "tenant")
    @Length(20)
    private String number;
    public static final Column NUMBER = Column.named("number");

    private OfferState state = OfferState.OPEN;
    public static final Column STATE = Column.named("state");

    @Autoloaded
    @Length(100)
    private String keyword;
    public static final Column KEYWORD = Column.named("keyword");

    @Autoloaded
    private final EntityRef<Person> person = EntityRef.on(Person.class, EntityRef.OnDelete.REJECT);
    public static final Column PERSON = Column.named("person");

    @Autoloaded
    private final EntityRef<UserAccount> employee = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column EMPLOYEE = Column.named("employee");

    @Autoloaded
    private LocalDate date;
    public static final Column DATE = Column.named("date");

    @Autoloaded
    @NullAllowed
    private final EntityRef<Person> buyer = EntityRef.on(Person.class, EntityRef.OnDelete.REJECT);
    public static final Column BUYER = Column.named("buyer");

    @Autoloaded
    @Length(100)
    @NullAllowed
    private String reference;
    public static final Column REFERENCE = Column.named("reference");

    @Transient
    private boolean serviceItemPresent;
    public static final Column SERVICEITEMPRESENT = Column.named("serviceItemPresent");

    @Transient
    private boolean licenceItemPresent;
    public static final Column LICENCEITEMPRESENT = Column.named("licenceItemPresent");

    public String toString() {
        String s = "Angebot ";
        if(number != null) {
            s = s.concat(number);
        }
        return s;
    }

    public String getEmployeeShortName() {
        UserAccount uac = this.getEmployee().getValue();
        Employee employee = uac.as(Employee.class);
        return employee.getShortName();
    }

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Sequences sequences;

    @BeforeSave
    protected void onSave() {
        // check te Role of the user
        UserInfo userInfo = UserContext.getCurrentUser();
        userInfo.assertPermission("offers");
        // Calculate the offer-number if the offer is new (id == 0)
        if (Strings.isEmpty(number)) {
            number = String.valueOf(sequences.generateId("OFFER"));

        }
        // get the employee
        if (employee.getValue() == null) {
            employee.setId(userInfo.getUserObject(UserAccount.class).getId());
        }
        //set the offer-date
        if (date == null) {
            date = LocalDate.now();
        }

        // update the offerState
        sas.updateOfferState(this, false);
    }

    public EntityRef<Company> getCompany() {
        return company;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public OfferState getState() {
        return state;
    }

    public void setState(OfferState state) {
        this.state = state;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public EntityRef<Person> getPerson() {
        return person;
    }

    public EntityRef<UserAccount> getEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EntityRef<Person> getBuyer() {
        return buyer;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isServiceItemPresent() {
        return serviceItemPresent;
    }

    public void setServiceItemPresent(boolean serviceItemPresent) {
        this.serviceItemPresent = serviceItemPresent;
    }

    public boolean isLicenceItemPresent() {
        return licenceItemPresent;
    }

    public void setLicenceItemPresent(boolean licenceItemPresent) {
        this.licenceItemPresent = licenceItemPresent;
    }
}
