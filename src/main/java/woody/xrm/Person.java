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
import sirius.biz.sequences.Sequences;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.Formatter;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relateable;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

import java.time.LocalDate;

/**
 * Created by aha on 06.10.15.
 */
public class Person extends BizEntity implements HasComments, HasRelations {

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    private final PersonData person = new PersonData();
    public static final Column PERSON = Column.named("person");

    public static final Column UNIQUE_PATH = Column.named("uniquePath");
    @Length(150)
    private String uniquePath;

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

    private final Relations relations = new Relations(this);
    public static final Column RELATIONS = Column.named("relations");

    private final Relateable relateable = new Relateable(this);
    public static final Column RELATEABLE = Column.named("relateable");

    @Part
    private static Sequences sequences;

    protected void computeUniquePath() {
        setUniquePath(encode(getCompany().getId()) + encode(sequences.generateId("persons-counter-"
                                                                                 + getCompany().getValue()
                                                                                               .getTenant()
                                                                                               .getValue()
                                                                                               .getIdAsString())));
    }

    private String encode(long id) {
        String code = com.google.common.base.Strings.padStart(Long.toString(id, 36), 6, '0');
        if (code.length() != 6) {
            throw Exceptions.createHandled().withNLSKey("Person.cannotGenerateCode").handle();
        }

        return code;
    }

    @BeforeSave
    protected void onSave() {
        if (Strings.isEmpty(uniquePath) && company.isFilled()) {
            computeUniquePath();
        }
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

    public Relateable getRelateable() {
        return relateable;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Relations getRelations() {
        return relations;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
    }
}
