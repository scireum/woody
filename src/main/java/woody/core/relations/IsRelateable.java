/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

public interface IsRelateable {

    Relateable getRelateable();

    default String getTargetString() {
        return null;
    }
}
