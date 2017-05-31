/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

/**
 * Marks an entity as one that has outgoing relations.
 * <p>
 * Note that also a matching {@link RelationProvider} is required.
 */
public interface HasRelations {

    /**
     * Returns the facility which manages outgoing relations for the entity.
     *
     * @return the relations management facility
     */
    Relations getRelations();
}
