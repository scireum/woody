/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.Composite;
import sirius.db.mixing.annotations.NullAllowed;

public class ColorData extends Composite {

    public static final Mapping COLOR = Mapping.named("color");
    @NullAllowed
    @Autoloaded
    private final SQLEntityRef<ColorDefinition> color = SQLEntityRef.on(ColorDefinition.class, SQLEntityRef.OnDelete.REJECT);

    public SQLEntityRef<ColorDefinition> getColor() {
        return color;
    }

}
