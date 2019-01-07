/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

/**
 * Created by gerhardhaufler on 01.01.19.
 */
public interface MigrationJob {

    public void migrateCrmToWdody();

    public void deleteWoody();


}
