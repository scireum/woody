/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Composite;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.NullAllowed;

public class ColorData extends Composite {

    public static final Column COLOR = Column.named("color");
    @NullAllowed
    @Autoloaded
    private final EntityRef<ColorDefinition> color = EntityRef.on(ColorDefinition.class, EntityRef.OnDelete.REJECT);

    public EntityRef<ColorDefinition> getColor() {
        return color;
    }

}
