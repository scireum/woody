/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.kernel.di.std.Part;

public class TypeColorAssignment extends TenantAware {

    public static final Column TYPE = Column.named("type");
    @Length(100)
    private String type;

    public static final Column COLOR = Column.named("color");
    @Autoloaded
    private final EntityRef<ColorDefinition> color = EntityRef.on(ColorDefinition.class, EntityRef.OnDelete.REJECT);

    @Part
    private static Colors colors;

    @BeforeSave
    @BeforeDelete
    protected void onModify() {
        colors.flushColorAssignment(String.valueOf(getTenant().getId()), getType());
    }

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
