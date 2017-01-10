/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import sirius.biz.model.AddressData;
import sirius.biz.model.ContactData;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.Mixable;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.health.Exceptions;

import java.time.LocalDate;

/**
 * Created by aha on 09.05.15.
 */
@Mixin(UserAccount.class)
public class Employee extends Mixable {

    public static final Column EMPLOYEE_NUMBER = Column.named("employeeNumber");
    @Unique(within = "tenant")
    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String employeeNumber;

    public static final Column PHONE_EXTENSION = Column.named("phoneExtension");
    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String phoneExtension;

    public static final Column MENTOR = Column.named("mentor");
    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> mentor = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);

    public static final Column DEPARTMENT = Column.named("department");
    @NullAllowed
    @Autoloaded
    private final EntityRef<Department> department = EntityRef.on(Department.class, EntityRef.OnDelete.REJECT);

    public static final Column JOIN_DATE = Column.named("joinDate");
    @NullAllowed
    @Autoloaded
    private LocalDate joinDate;

    public static final Column DISCHARGE_DATE = Column.named("dischargeDate");
    @NullAllowed
    @Autoloaded
    private LocalDate dischargeDate;

    public static final Column BIRTHDAY = Column.named("birthday");
    @NullAllowed
    @Autoloaded
    private LocalDate birthday;

    public static final Column ADDRESS = Column.named("address");
    private final AddressData personalAddress = new AddressData(AddressData.Requirements.NONE, null);

    public static final Column CONTACT = Column.named("contact");
    private final ContactData personalContact = new ContactData(true);

    @BeforeSave
    protected void checkIntegrity(UserAccount parent) {
        if (getDischargeDate() != null && !parent.getLogin().isAccountLocked()) {
            throw Exceptions.createHandled().withNLSKey("Employee.cannotDischargeWithoutLock").handle();
        }
        if (getMentor().isFilled()) {
            parent.assertSameTenant(() -> parent.getDescriptor()
                                                .getProperty(Column.mixin(Employee.class).inner(MENTOR))
                                                .getLabel(), getMentor().getValue());
        }
        if (getDepartment().isFilled()) {
            parent.assertSameTenant(() -> parent.getDescriptor()
                                                .getProperty(Column.mixin(Employee.class).inner(DEPARTMENT))
                                                .getLabel(), getDepartment().getValue());
        }
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public EntityRef<UserAccount> getMentor() {
        return mentor;
    }

    public EntityRef<Department> getDepartment() {
        return department;
    }

    public AddressData getPersonalAddress() {
        return personalAddress;
    }

    public ContactData getPersonalContact() {
        return personalContact;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }
}
