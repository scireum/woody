/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.quotes;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.sequences.Sequences;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;
import woody.xrm.Company;
import woody.xrm.PersonRef;

import java.time.LocalDate;

public class Offer extends BizEntity implements HasComments, HasRelations {

    public static final Mapping COMPANY = Mapping.named("company");
    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, SQLEntityRef.OnDelete.REJECT);

    public static final Mapping OFFER_NUMBER = Mapping.named("offerNumber");
    @Length(20)
    private String offerNumber;

    public static final Mapping DESCRIPTION = Mapping.named("description");
    @Autoloaded
    @Length(255)
    private String description;

    public static final Mapping PERSON = Mapping.named("person");
    private final PersonRef person = new PersonRef(this, PERSON);

    public static final Mapping BUYER = Mapping.named("buyer");
    private final PersonRef buyer = new PersonRef(this, BUYER);

    public static final Mapping DATE = Mapping.named("date");
    @Autoloaded
    private LocalDate date;

    @Autoloaded
    @Length(100)
    @NullAllowed
    private String reference;
    public static final Mapping REFERENCE = Mapping.named("reference");

    private final Tagged tags = new Tagged(this);
    public static final Mapping TAGS = Mapping.named("tags");

    private final Commented comments = new Commented(this);
    public static final Mapping COMMENTS = Mapping.named("comments");

    private final Relations relations = new Relations(this);
    public static final Mapping RELATIONS = Mapping.named("relations");

    @Part
    private static Sequences sequences;

    @BeforeSave
    protected void onSave() {
        if (Strings.isEmpty(offerNumber) && getCompany().isFilled()) {
            offerNumber = String.valueOf(sequences.generateId("QUOTES-" + getCompany().getValue().getTenant().getId()));
        }

        if (date == null) {
            date = LocalDate.now();
        }
    }

    public SQLEntityRef<Company> getCompany() {
        return company;
    }

    public String getOfferNumber() {
        return offerNumber;
    }

    public void setOfferNumber(String offerNumber) {
        this.offerNumber = offerNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PersonRef getPerson() {
        return person;
    }

    public PersonRef getBuyer() {
        return buyer;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }
}
