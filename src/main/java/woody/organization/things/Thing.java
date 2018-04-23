/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import sirius.db.mixing.EntityRef;
import woody.organization.BasicElement;

/**
 * Created by aha on 13.01.17.
 */
public class Thing extends BasicElement<ThingType> {

    @Override
    protected EntityRef<ThingType> initializeTypeRef() {
        return EntityRef.on(ThingType.class, EntityRef.OnDelete.REJECT);
    }
}
