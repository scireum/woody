/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.css;

import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Interceptor;
import sirius.web.http.WebContext;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;

import java.lang.reflect.Method;

/**
 * Created by aha on 08.05.15.
 */
@Register
public class CSSInterceptor implements Interceptor {

    @Override
    public boolean before(WebContext webContext, boolean b, Controller controller, Method method) throws Exception {
        return false;
    }

    @Override
    public boolean beforePermissionError(String permission,
                                         WebContext ctx,
                                         boolean jsonCall,
                                         Controller controller,
                                         Method method) throws Exception {
        if (UserContext.getCurrentScope() != CSSDetector.CSS_SCOPE) {
            return false;
        }
        if (UserInfo.PERMISSION_LOGGED_IN.equals(permission)) {
            ctx.respondWith().template("view/xrm/css/login.html", ctx.getRequest().getUri());
        } else {
            //TODO
            ctx.respondWith().template("view/xrm/css/permission-error.html", ctx.getRequest().getUri(), permission);
        }
        return true;
    }
}
