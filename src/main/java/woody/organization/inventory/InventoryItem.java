/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.inventory;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relateable;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

/**
 * Created by aha on 13.01.17.
 */
public class InventoryItem extends TenantAware implements HasComments, HasRelations {

    public static final Column TYPE = Column.named("type");
    private final EntityRef<InventoryType> type = EntityRef.on(InventoryType.class, EntityRef.OnDelete.REJECT);

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
    private Relations relations = new Relations(this);

    public static final Column RELATEABLE = Column.named("relateable");
    private Relateable relateable = new Relateable(this);

    //TODO check for cycles...

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

    public void setRelateable(Relateable relateable) {
        this.relateable = relateable;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    public EntityRef<InventoryType> getType() {
        return type;
    }
}
