/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.tenants.TenantAware;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;

public class TypeColorAssignment extends TenantAware {

    public static final Column TYPE = Column.named("type");
    @Length(100)
    private String type;

    public static final Column COLOR = Column.named("color");
    private final EntityRef<ColorDefinition> color = EntityRef.on(ColorDefinition.class, EntityRef.OnDelete.REJECT);

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EntityRef<ColorDefinition> getColor() {
        return color;
    }

}
