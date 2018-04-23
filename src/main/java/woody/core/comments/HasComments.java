/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

/**
 * Marks an entity which contains a {@link Commented} composite.
 */
public interface HasComments {

    /**
     * Returns the comments facility for this entity.
     *
     * @return the comments facility used to manage comments for the entity.
     */
    Commented getComments();

}
