/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.db.jdbc.SQLEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;

/**
 * Created by aha on 25.11.15.
 */
public class TagAssignment extends SQLEntity {

    private final SQLEntityRef<Tag> tag = SQLEntityRef.on(Tag.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping TAG = Mapping.named("tag");

    private long targetEntity;
    public static final Mapping TARGET_ENTITY = Mapping.named("targetEntity");

    @Length(255)
    private String targetType;
    public static final Mapping TARGET_TYPE = Mapping.named("targetType");

    public SQLEntityRef<Tag> getTag() {
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
