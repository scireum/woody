/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.db.mixing.EntityRef;
import woody.organization.BasicElement;

/**
 * Created by aha on 13.01.17.
 */
public class Effort extends BasicElement<EffortType> {

    @Override
    protected EntityRef<EffortType> initializeTypeRef() {
        return EntityRef.on(EffortType.class, EntityRef.OnDelete.REJECT);
    }
}
