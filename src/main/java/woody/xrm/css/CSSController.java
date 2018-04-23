/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.css;

import io.netty.handler.codec.http.HttpResponseStatus;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.HandledException;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;

/**
 * Created by aha on 12.05.15.
 */
@Register
public class CSSController implements Controller {
    @Override
    public void onError(WebContext ctx, HandledException error) {

    }

    @Routed("/css/profile")
    @LoginRequired
    public void profile(WebContext ctx) {
        ctx.respondWith().error(HttpResponseStatus.NOT_FOUND);
    }
}
