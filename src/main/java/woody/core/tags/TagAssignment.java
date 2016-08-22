/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;

/**
 * Created by aha on 25.11.15.
 */
public class TagAssignment extends Entity {

    private final EntityRef<Tag> tag = EntityRef.on(Tag.class, EntityRef.OnDelete.CASCADE);
    public static final Column TAG = Column.named("tag");

    private long targetEntity;
    public static final Column TARGET_ENTITY = Column.named("targetEntity");

    @Length(255)
    private String targetType;
    public static final Column TARGET_TYPE = Column.named("targetType");

    public EntityRef<Tag> getTag() {
        return tag;
    }

    public long getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(long targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
