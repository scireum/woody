/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.AfterDelete;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relateable;
import woody.core.relations.Relation;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

/**
 * Created by aha on 13.01.17.
 */
@Index(name = "unique_path_lookup", columns = {"uniquePath"})
public class Unit extends TenantAware implements HasComments, HasRelations, Journaled {

    public static final Column PARENT = Column.named("parent");
    @NullAllowed
    private final EntityRef<Unit> parent = EntityRef.on(Unit.class, EntityRef.OnDelete.REJECT);

    public static final Column TYPE = Column.named("type");
    private final EntityRef<UnitType> type = EntityRef.on(UnitType.class, EntityRef.OnDelete.REJECT);

    public static final Column UNIQUE_PATH = Column.named("uniquePath");
    @Length(150)
    private String uniquePath;

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

    //TODO check for cycles...

    @AfterDelete
    protected void checkChildren() {
        oma.select(Relation.class).eq(Relation.TARGET, getUniquePath()).delete();
    }

    public EntityRef<UnitType> getType() {
        return type;
    }

    public EntityRef<Unit> getParent() {
        return parent;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
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

    public Relateable getRelateable() {
        return relateable;
    }

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
