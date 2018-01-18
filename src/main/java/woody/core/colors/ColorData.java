/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.db.mixing.Composite;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.NullAllowed;

public class ColorData extends Composite {

    @NullAllowed
    private EntityRef<ColorDefinition> color = EntityRef.on(ColorDefinition.class, EntityRef.OnDelete.REJECT);



}
