/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.kernel.di.std.Part;

public class TypeColorAssignment extends SQLTenantAware {

    public static final Mapping TYPE = Mapping.named("type");
    @Length(100)
    private String type;

    public static final Mapping COLOR = Mapping.named("color");
    @Autoloaded
    private final SQLEntityRef<ColorDefinition> color =
            SQLEntityRef.on(ColorDefinition.class, SQLEntityRef.OnDelete.REJECT);

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

    public SQLEntityRef<ColorDefinition> getColor() {
        return color;
    }
}
