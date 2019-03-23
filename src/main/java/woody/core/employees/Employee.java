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
import sirius.db.mixing.Mixable;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.OnValidate;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.phoneCalls.Starface;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Strings;
import sirius.kernel.health.Exceptions;
import woody.core.relations.Relateable;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Extends a <tt>UserAccount</tt> with information related to an employee.
 */
@Mixin(UserAccount.class)
public class Employee extends Mixable {

    /**
     * Contains the employee number assigned to the user.
     */
    public static final Column EMPLOYEE_NUMBER = Column.named("employeeNumber");
    @Unique(within = "tenant")
    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String employeeNumber;

    /**
     * Contains the short name assigned to the user.
     */
    public static final Column SHORT_NAME = Column.named("shortName");
    @Unique(within = "tenant")
    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String shortName;

    /**
     * Contains the phone extension of the user.
     */
    public static final Column PHONE_EXTENSION = Column.named("phoneExtension");
    @Trim
    @Length(30)
    @NullAllowed
    @Autoloaded
    private String phoneExtension;

    /**
     * Contains the date when the user joined the company.
     */
    public static final Column JOIN_DATE = Column.named("joinDate");
    @NullAllowed
    @Autoloaded
    private LocalDate joinDate;

    /**
     * Contains the date when the user left the company.
     */
    public static final Column DISCHARGE_DATE = Column.named("dischargeDate");
    @NullAllowed
    @Autoloaded
    private LocalDate dischargeDate;

    /**
     * Contains relations from other objects to this user.
     */
    public static final Column RELATEABLE = Column.named("relateable");
    private final Relateable relateable;

    /**
     * Represents the constructor which is used to properly initialize the <tt>Relateable</tt>.
     *
     * @param owner the entity for which this mixin was created.
     */
    public Employee(UserAccount owner) {
        relateable = new Relateable(owner);
    }

    /**
     * contains the mail-signature of the employee
     */
    @Autoloaded
    @NullAllowed
    @Length(2000)
    private String signature;
    public static final Column SIGNATURE = Column.named("signature");

    /**
     * contains the pbx-access-token
     */
    @NullAllowed
    @Length(1024)
    private String pbxAccessToken;
    public static final Column PBXACCESSTOKEN = Column.named("pbxAccessToken");

    @Part
    private static Starface stf;

    @BeforeSave
    protected void checkIntegrity(UserAccount parent) {
        if (getDischargeDate() != null && !parent.getLogin().isAccountLocked()) {
            throw Exceptions.createHandled().withNLSKey("Employee.cannotDischargeWithoutLock").handle();
        }

        // build the pbxAccessToken
        if(phoneExtension != null && Strings.isFilled(phoneExtension)) {
            String pbxCleartextPassword = stf.buildStarefacePassword(phoneExtension);
            pbxAccessToken = stf.SHA512(pbxCleartextPassword);
            pbxCleartextPassword = "";
        } else {
            pbxAccessToken = null;
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

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPbxAccessToken() {
        return pbxAccessToken;
    }

    public void setPbxAccessToken(String pbxAccessToken) {
        this.pbxAccessToken = pbxAccessToken;
    }
}
