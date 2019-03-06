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
import sirius.biz.sequences.Sequences;
import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.nls.NLS;
import woody.core.colors.Colors;
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
public abstract class BasicElement<T extends BasicType> extends SQLTenantAware
        implements HasComments, HasRelations, IsRelateable, Journaled {

    public static final Mapping TYPE = Mapping.named("type");
    private final SQLEntityRef<T> type;

    public static final Mapping NAME = Mapping.named("name");
    @Length(100)
    @Trim
    @Autoloaded
    private String name;

    public static final Mapping CODE = Mapping.named("code");
    @Length(100)
    @Trim
    @NullAllowed
    @Autoloaded
    private String code;

    public static final Mapping DESCRIPTION = Mapping.named("description");
    @Length(1024)
    @Trim
    @NullAllowed
    @Autoloaded
    private String description;

    private final Tagged tags = new Tagged(this);
    public static final Mapping TAGS = Mapping.named("tags");

    private final Commented comments = new Commented(this);
    public static final Mapping COMMENTS = Mapping.named("comments");

    public static final Mapping RELATIONS = Mapping.named("relations");
    private final Relations relations = new Relations(this);

    public static final Mapping RELATEABLE = Mapping.named("relateable");
    private final Relateable relateable = new Relateable(this);

    public static final Mapping JOURNAL = Mapping.named("journal");
    private final JournalData journal = new JournalData(this);

    @Part
    private static Sequences sequences;

    @Part
    private static Colors colors;

    protected BasicElement() {
        type = initializeTypeRef();
    }

    @BeforeSave
    protected void onSave() {
        if (Strings.isEmpty(code)) {
            String codePrefix = getType().getValue().getCodePrefix();
            if (Strings.isFilled(codePrefix)) {
                code = codePrefix + "-" + generateCode();
            }
        }
    }

    private long generateCode() {
        return sequences.generateId(Mixing.getNameForType(getType().getValue().getClass())
                                    + "-"
                                    + getType().getId()
                                    + "-"
                                    + getTenant().getId());
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

    public String getColor() {
        return colors.getColor(getType().getValue().getColor().getColor())
                     .orElseGet(() -> getType().getValue().getCategory().getValue().getEffectiveColor());
    }

    @Override
    public String toString() {
        if (isNew()) {
            return NLS.get("Model.create") + ": " + getType().getValue().getName();
        }
        return getName();
    }

    protected abstract SQLEntityRef<T> initializeTypeRef();

    public SQLEntityRef<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getType().getValue().getName() + ": " + name;
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
