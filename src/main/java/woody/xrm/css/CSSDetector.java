/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.css;

import sirius.kernel.di.std.Register;
import sirius.web.http.WebContext;
import sirius.web.security.ScopeDetector;
import sirius.web.security.ScopeInfo;

import javax.annotation.Nonnull;

/**
 * Created by aha on 12.05.15.
 */
@Register
public class CSSDetector implements ScopeDetector {

    public static final ScopeInfo CSS_SCOPE = new ScopeInfo("css", "css", "css", null, null);

    @Nonnull
    @Override
    public ScopeInfo detectScope(@Nonnull WebContext request) {
        if (request.getRequestedURI().startsWith("/css")) {
            return CSS_SCOPE;
        }
        return ScopeInfo.DEFAULT_SCOPE;
    }
}
