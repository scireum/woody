/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody;

import sirius.kernel.health.Exceptions;

/**
 * Created by gerhardhaufler on 19.09.16.
 */
public class BusinessException extends Throwable {

    public BusinessException(String text) {
        throw Exceptions.createHandled()
                        .withSystemErrorMessage(text)
                        .handle();

    }


}
