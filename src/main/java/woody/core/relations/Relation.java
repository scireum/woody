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
import sirius.db.jdbc.OMA;
import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.jdbc.schema.Schema;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.core.colors.Colors;

/**
 * Represents a relation between two entities.
 * <p>
 * The entities itself are represented by their <tt>unique object name</tt> to make it usable for
 * all kinds of entities.
 * <p>
 * The composites {@link Relations} and {@link Relateable} are responsible for deleting relations once an entity is
 * removed.
 */
@Index(name = "owner_lookup", columns = {"ownerType", "ownerId", "target"})
@Index(name = "target_lookup", columns = {"target", "ownerType", "ownerId"})
public class Relation extends SQLEntity {

    /**
     * Contains the type name of the source of the relation.
     * <p>
     * The <tt>unique object name</tt> of the source (owner) is split into two fields to support effective joins within
     * SQL queries.
     */
    public static final Mapping OWNER_TYPE = Mapping.named("ownerType");
    @Length(100)
    private String ownerType;

    /**
     * Contains the id of the source of the relation.
     *
     * @see #OWNER_TYPE
     */
    public static final Mapping OWNER_ID = Mapping.named("ownerId");
    private long ownerId;

    /**
     * Contains the unique object name of the destination of the relation.
     */
    public static final Mapping TARGET = Mapping.named("target");
    @Length(255)
    private String target;

    /**
     * Contains the type of the relations.
     */
    public static final Mapping TYPE = Mapping.named("type");
    private final SQLEntityRef<RelationType> type = SQLEntityRef.on(RelationType.class, SQLEntityRef.OnDelete.CASCADE);

    @Part
    private static OMA oma;

    @Part
    private static Schema schema;

    @Part
    private static RelationHelper relations;

    @Part
    private static Colors colors;

    @Length(255)
    @NullAllowed
    private String description;

    @BeforeSave
    protected void onSave() {
        String uniqueOwnerName = ownerType + "-" + ownerId;
        SQLEntity owner = oma.resolve(uniqueOwnerName).orElse(null);
        if (owner instanceof Journaled) {
            JournalData.addJournalEntry(owner,
                                        Strings.apply("The relationship '%s' to '%s' was added",
                                                      getType().getValue().getName(),
                                                      relations.getTargetName(this)));
        }
    }

    @BeforeDelete
    protected void onDelete() {
        String uniqueOwnerName = ownerType + "-" + ownerId;
        SQLEntity owner = oma.resolve(uniqueOwnerName).orElse(null);
        if (owner instanceof Journaled) {
            JournalData.addJournalEntry(owner,
                                        Strings.apply("The relationship '%s' to '%s' was deleted",
                                                      getType().getValue().getName(),
                                                      relations.getTargetName(this)));
        }
    }

    public String getRelationType() {
        return getType().getValue().getName();
    }

    public String getColor() {
        return colors.getColor(getType().getValue().getColor().getColor())
                     .orElseGet(() -> colors.getColorForType(RelationQueryTagColorTypeProvider.TYPE));
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

    public SQLEntityRef<RelationType> getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
