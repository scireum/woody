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
import sirius.biz.model.InternationalAddressData;
import sirius.biz.model.LoginData;
import sirius.biz.model.PersonData;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.nls.Formatter;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by aha on 06.10.15.
 */
public class Person extends BizEntity {

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    private final PersonData person = new PersonData();
    public static final Column PERSON = Column.named("person");

    @Autoloaded
    @NullAllowed
    private LocalDate birthday;
    public static final Column BIRTHDAY = Column.named("birthday");

    private final InternationalAddressData address = new InternationalAddressData(AddressData.Requirements.NONE, null);
    public static final Column ADDRESS = Column.named("address");

    private final ContactData contact = new ContactData(true);
    public static final Column CONTACT = Column.named("contact");

    private final LoginData login = new LoginData();
    public static final Column LOGIN = Column.named("login");

    @Trim
    @Length(100)
    @NullAllowed
    private String position;
    public static final Column POSITION = Column.named("position");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    private boolean offline = false;
    public static final Column OFFLINE = Column.named("offline");

    @BeforeSave
    protected void verify() {
        if (Strings.isEmpty(getLogin().getUsername())) {
            getLogin().setUsername(getContact().getEmail());
        }
    }

    @Override
    public String toString() {
        if (!isNew()) {
            return getPerson().toString();
        } else {
            return super.toString();
        }
    }


    public String getLetterSalutation() {
        String text = "Sehr geehrte";
        if ("SIR".equals(this.getPerson().getSalutation())) {
            text = text.concat("r");
        }
        return Formatter.create("[${text} ][${salutation} ][${title} ]${lastname},")
                        .set("text", text)
                        .set("salutation", this.getPerson().getTranslatedSalutation())
                        .set("title", this.getPerson().getTitle())
                        .set("lastname", this.getPerson().getLastname())
                        .smartFormat();
    }

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

    public ContactData getContact() {
        return contact;
    }

    public InternationalAddressData getAddress() {
        return address;
    }

    public Tagged getTags() {
        return tags;
    }

    public Commented getComments() {
        return comments;
    }

    public boolean isOffline() {
        return offline;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
