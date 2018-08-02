/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Composite;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;

public class PersonRef extends Composite {

    public static final Mapping PERSON = Mapping.named("person");
    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Person> person = SQLEntityRef.on(Person.class, SQLEntityRef.OnDelete.SET_NULL);

    public static final Mapping PERSON_NAME = Mapping.named("personName");
    @Length(255)
    @NullAllowed
    private String personName;

    private final SQLEntity owner;
    private final Mapping ownerColumn;

    public PersonRef(SQLEntity owner, Mapping ownerColumn) {
        this.owner = owner;
        this.ownerColumn = ownerColumn;
    }

    @BeforeSave
    protected void storeName() {
        if (owner.isChanged(ownerColumn.inner(PERSON))) {
            if (person.isFilled()) {
                setPersonName(person.getValue().getPerson().toString());
            } else {
                setPersonName(null);
            }
        }
    }

    public SQLEntityRef<Person> getPerson() {
        return person;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }
}
