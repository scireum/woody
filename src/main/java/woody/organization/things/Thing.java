/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import sirius.db.jdbc.SQLEntityRef;
import woody.organization.BasicElement;

/**
 * Created by aha on 13.01.17.
 */
public class Thing extends BasicElement<ThingType> {

    @Override
    protected SQLEntityRef<ThingType> initializeTypeRef() {
        return SQLEntityRef.on(ThingType.class, SQLEntityRef.OnDelete.REJECT);
    }
}
