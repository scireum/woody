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
import sirius.biz.model.PersonData;
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
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.phoneCalls.Starface;

import java.time.LocalDate;

/**
 * Created by aha on 09.05.15.
 */
@Mixin(UserAccount.class)
public class Employee extends Mixable {

    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String employeeNumber;
    public static final Column EMPLOYEE_NUMBER = Column.named("employeeNumber");

    @Trim
    @Length(6)
    @NullAllowed
    @Autoloaded
    private String shortName;
    public static final Column SHORTNAME = Column.named("shortName");

    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> mentor = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);
    public static final Column MENTOR = Column.named("mentor");

    @NullAllowed
    @Autoloaded
    private final EntityRef<Department> department = EntityRef.on(Department.class, EntityRef.OnDelete.REJECT);
    public static final Column DEPARTMENT = Column.named("department");

    @NullAllowed
    @Autoloaded
    private LocalDate joinDate;
    public static final Column JOIN_DATE = Column.named("joinDate");

    @NullAllowed
    @Autoloaded
    private LocalDate terminationDate = null;
    public static final Column TERMINATION_DATE = Column.named("terminationDate");

    @Autoloaded
    private boolean inaktiv = false;
    public static final Column INAKTIV = Column.named("inaktiv");

    @NullAllowed
    @Autoloaded
    private LocalDate birthday;
    public static final Column BIRTHDAY = Column.named("birthday");

    /**
     * komplette Telefonnummer des Angestellten
     * in lesbarer Schreibweise, z. B. +49 7151 90316-21
     */
    @NullAllowed
    @Autoloaded
    @Length(150)
    private String phoneNr;
    public static final Column PHONENR = Column.named("phoneNr");

    /**
     * Nebenstellen-Nummer, z. B. aha = 21
     */
    @NullAllowed
    @Autoloaded
    @Length(5)
    private String pbxId;
    public static final Column PBXID = Column.named("pbxId");

    @NullAllowed
    @Length(50)
    private String pbxAccessToken;
    public static final Column PBXACCESSTOKEN = Column.named("pbxAccessToken");


    private final AddressData homeAddress = new AddressData(AddressData.Requirements.NONE, null);
    public static final Column ADDRESS = Column.named("address");

    private final ContactData homeContact = new ContactData(true);
    public static final Column CONTACT = Column.named("contact");

    @Part
    private static Starface stf;

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

        // build the pbxAccessToken
        if(pbxId != null && Strings.isFilled(pbxId)) {
            String pbxCleartextPassword = pbxId + pbxId + pbxId + pbxId;
            pbxAccessToken = stf.buildMd5HexString(pbxId + "*" + pbxCleartextPassword);
            pbxCleartextPassword = "";
        } else {
            pbxAccessToken = null;
        }

    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public boolean isInaktiv() {
        return inaktiv;
    }

    public void setInaktiv(boolean inaktiv) {
        this.inaktiv = inaktiv;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
    }

    public String getPbxId() {
        return pbxId;
    }

    public void setPbxId(String pbxId) {
        this.pbxId = pbxId;
    }

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

    public EntityRef<UserAccount> getMentor() {
        return mentor;
    }

    public EntityRef<Department> getDepartment() {
        return department;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public AddressData getHomeAddress() {
        return homeAddress;
    }

    public ContactData getHomeContact() {
        return homeContact;
    }

    public String getPbxAccessToken() {
        return pbxAccessToken;
    }

    public void setPbxAccessToken(String pbxAccessToken) {
        this.pbxAccessToken = pbxAccessToken;
    }
}
