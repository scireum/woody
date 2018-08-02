/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.jdbc.model.AddressData;
import sirius.biz.jdbc.model.BizEntity;
import sirius.biz.jdbc.model.ContactData;
import sirius.biz.jdbc.model.InternationalAddressData;
import sirius.biz.jdbc.model.LoginData;
import sirius.biz.jdbc.model.PersonData;
import sirius.biz.sequences.Sequences;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
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

    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping COMPANY = Mapping.named("company");

    private final PersonData person = new PersonData();
    public static final Mapping PERSON = Mapping.named("person");

    public static final Mapping UNIQUE_PATH = Mapping.named("uniquePath");
    @Length(150)
    private String uniquePath;

    @Autoloaded
    @NullAllowed
    private LocalDate birthday;
    public static final Mapping BIRTHDAY = Mapping.named("birthday");

    private final InternationalAddressData address = new InternationalAddressData(AddressData.Requirements.NONE, null);
    public static final Mapping ADDRESS = Mapping.named("address");

    private final ContactData contact = new ContactData(true);
    public static final Mapping CONTACT = Mapping.named("contact");

    private final LoginData login = new LoginData();
    public static final Mapping LOGIN = Mapping.named("login");

    @Trim
    @Length(100)
    @NullAllowed
    private String position;
    public static final Mapping POSITION = Mapping.named("position");

    private final Tagged tags = new Tagged(this);
    public static final Mapping TAGS = Mapping.named("tags");

    private final Commented comments = new Commented(this);
    public static final Mapping COMMENTS = Mapping.named("comments");

    private final Relations relations = new Relations(this);
    public static final Mapping RELATIONS = Mapping.named("relations");

    private final Relateable relateable = new Relateable(this);
    public static final Mapping RELATEABLE = Mapping.named("relateable");

    public static final Mapping QUIT = Mapping.named("quit");
    private boolean quit = false;

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

    public SQLEntityRef<Company> getCompany() {
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

    @Override
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

    @Override
    public Relations getRelations() {
        return relations;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
    }

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }
}
