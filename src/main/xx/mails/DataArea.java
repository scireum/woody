/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import java.io.InputStream;

/**
 * Created by gerhardhaufler on 24.10.17.
 */
public class DataArea {
    // ToDo Rechtsnachfolger für DataArea

    public DataArea getAttachmentsArea(NamedObject no) {
        return new DataArea();
    }

    public void addFile(String path, String filename, InputStream inputStream) {

    }
}
