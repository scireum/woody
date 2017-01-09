/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.phoneCalls;

/**
 * Created by gerhardhaufler on 04.01.17.
 */
public interface PhoneCallImporter {
    /**
     * fetches the phoneCalls from starface
     * @return  number of fetched phonecalls
     */
    public int fetchPhoneCalls();
}
