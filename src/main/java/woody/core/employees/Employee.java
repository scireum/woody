/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import sirius.biz.model.AddressData;
import sirius.biz.tenants.UserAccount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.Mixable;
import sirius.mixing.annotations.*;

import java.time.LocalDate;

/**
 * Created by aha on 09.05.15.
 */
@Mixin(UserAccount.class)
public class Employee extends Mixable {

    @Trim
    @Length(length = 30)
    @NullAllowed
    private String employeeNumber;
    public static final Column EMPLOYEE_NUMBER = Column.named("employeeNumber");

    @NullAllowed
    private final EntityRef<UserAccount> mentor = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);
    public static final Column MENTOR = Column.named("mentor");

    @NullAllowed
    private final EntityRef<Department> department = EntityRef.on(Department.class, EntityRef.OnDelete.REJECT);
    public static final Column DEPARTMENT = Column.named("department");

    @NullAllowed
    private LocalDate joinDate;
    public static final Column JOIN_DATE = Column.named("joinDate");

    @NullAllowed
    private LocalDate birthday;
    public static final Column BIRTHDAY = Column.named("birthday");

    @Trim
    @Length(length = 50)
    @NullAllowed
    private String homePhone;
    public static final Column HOME_PHONE = Column.named("homePhone");

    @Trim
    @Length(length = 50)
    @NullAllowed
    private String mobilePhone;
    public static final Column MOBILE_PHONE = Column.named("mobilePhone");

    @NullAllowed
    private final AddressData homeAddress = new AddressData();
    public static final Column HOME_ADDRESS = Column.named("homeAddress");

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
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

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public AddressData getHomeAddress() {
        return homeAddress;
    }

    public EntityRef<UserAccount> getMentor() {
        return mentor;
    }

    public EntityRef<Department> getDepartment() {
        return department;
    }

    @BeforeSave
    protected void checkIntegrity(UserAccount parent) {
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
}
