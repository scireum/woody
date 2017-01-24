/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;

/**
 * Created by aha on 11.01.17.
 */
@Index(name = "owner_lookup", columns = {"ownerType", "ownerId", "target"})
@Index(name = "target_lookup", columns = {"target", "ownerType", "ownerId"})
public class Relation extends Entity {

    public static final Column OWNER_ID = Column.named("ownerId");
    private long ownerId;

    public static final Column OWNER_TYPE = Column.named("ownerType");
    @Length(100)
    private String ownerType;

    public static final Column TARGET = Column.named("target");
    @Length(255)
    private String target;

    public static final Column TYPE = Column.named("type");
    private final EntityRef<RelationType> type = EntityRef.on(RelationType.class, EntityRef.OnDelete.CASCADE);

    @Part
    private static OMA oma;

    @Part
    private static Schema schema;

    @Length(255)
    @NullAllowed
    private String description;

    @BeforeSave
    protected void onSave() {
        String uniqueOwnerName = ownerType + "-" + ownerId;
        Entity owner = oma.resolve(uniqueOwnerName).orElse(null);
        if (owner instanceof Journaled) {
            JournalData.addJournalEntry(owner,
                                        Strings.apply("The relationship '%s' to '%s' was added",
                                                      getType().getValue().getName(),
                                                      Relations.getTargetNameAndUri(this).get().getFirst()));
        }
    }

    @BeforeDelete
    protected void onDelete() {
        String uniqueOwnerName = ownerType + "-" + ownerId;
        Entity owner = oma.resolve(uniqueOwnerName).orElse(null);
        if (owner instanceof Journaled) {
            JournalData.addJournalEntry(owner,
                                        Strings.apply("The relationship '%s' to '%s' was deleted",
                                                      getType().getValue().getName(),
                                                      Relations.getTargetNameAndUri(this).get().getFirst()));
        }
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public EntityRef<RelationType> getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
