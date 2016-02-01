/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.BizEntity;
import sirius.biz.model.LoginData;
import sirius.biz.model.PersonData;
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
    private static final Column COMPANY = Column.named("company");

    @Trim
    @Length(length = 150)
    @NullAllowed
    private String email;
    public static final Column EMAIL = Column.named("email");

    @Trim
    @Length(length = 150)
    @NullAllowed
    private String phone;
    public static final Column PHONE = Column.named("phone");

    @Trim
    @Length(length = 150)
    @NullAllowed
    private String fax;
    public static final Column FAX = Column.named("fax");

    @Trim
    @Length(length = 150)
    @NullAllowed
    private String mobile;
    public static final Column MOBILE = Column.named("mobile");

    private final PersonData person = new PersonData();
    public static final Column PERSON = Column.named("person");

    private final LoginData login = new LoginData();
    public static final Column LOGIN = Column.named("login");

    public EntityRef<Company> getCompany() {
        return company;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public PersonData getPerson() {
        return person;
    }

    public LoginData getLogin() {
        return login;
    }
}
