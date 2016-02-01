/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.mixing.Column;
import sirius.mixing.Entity;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;

/**
 * Created by aha on 25.11.15.
 */
public class TagAssignment extends Entity {

    private final EntityRef<Tag> tag = EntityRef.on(Tag.class, EntityRef.OnDelete.CASCADE);
    public static final Column TAG = Column.named("tag");

    @Length(length = 255)
    private String targetEntity;
    public static final Column TARGET_ENTITY = Column.named("targetEntity");

    public EntityRef<Tag> getTag() {
        return tag;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }
}
