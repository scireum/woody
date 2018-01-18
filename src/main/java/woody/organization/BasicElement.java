/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization;

import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.nls.NLS;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.IsRelateable;
import woody.core.relations.Relateable;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

/**
 * Created by aha on 13.01.17.
 */
public abstract class BasicElement<T extends BasicType> extends TenantAware
        implements HasComments, HasRelations, IsRelateable, Journaled {

    public static final Column TYPE = Column.named("type");
    private final EntityRef<T> type;

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Trim
    @Autoloaded
    private String name;

    public static final Column CODE = Column.named("code");
    @Length(100)
    @Trim
    @NullAllowed
    @Autoloaded
    private String code;

    public static final Column DESCRIPTION = Column.named("description");
    @Length(1024)
    @Trim
    @NullAllowed
    @Autoloaded
    private String description;

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    public static final Column RELATIONS = Column.named("relations");
    private final Relations relations = new Relations(this);

    public static final Column RELATEABLE = Column.named("relateable");
    private final Relateable relateable = new Relateable(this);

    public static final Column JOURNAL = Column.named("journal");
    private final JournalData journal = new JournalData(this);

    protected BasicElement() {
        type = initializeTypeRef();
    }

    @Override
    public String getTargetsString() {
        if (getType().isEmpty()) {
            return getTypeName();
        }

        return getTypeName()
               + ","
               + getTypeName()
               + "-"
               + getType().getId()
               + ","
               + getTypeName()
               + "-CATEGORY:"
               + getType().getValue().getCategory().getId();
    }

    @Override
    public String toString() {
        if (isNew()) {
            return NLS.get("Model.create") + ": " + getType().getValue().getName();
        }
        return getName();
    }

    protected abstract EntityRef<T> initializeTypeRef();

    public EntityRef<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Relateable getRelateable() {
        return relateable;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    @Override
    public JournalData getJournal() {
        return journal;
    }
}
