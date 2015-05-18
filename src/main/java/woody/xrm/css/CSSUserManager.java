/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.css;

import sirius.biz.tenants.TenantUserManager;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.extensions.Extension;
import sirius.mixing.OMA;
import sirius.web.http.WebContext;
import sirius.web.security.*;

import javax.annotation.Nonnull;

/**
 * Created by aha on 12.05.15.
 */
public class CSSUserManager extends GenericUserManager {

    public CSSUserManager(ScopeInfo scope, Extension config) {
        super(scope, config);
    }

    /**
     * Creates a new user manager for the given scope and configuration.
     */
    @Register(name = "css")
    public static class Factory implements UserManagerFactory {

        @Nonnull
        @Override
        public UserManager createManager(@Nonnull ScopeInfo scope, @Nonnull Extension config) {
            return new CSSUserManager(scope, config);
        }

    }

    @Part
    private static OMA oma;

    @Override
    protected UserInfo findUserByName(WebContext ctx, String user) {
        return null;
    }

    @Override
    protected UserInfo findUserByCredentials(WebContext ctx, String user, String password) {
        return null;
    }

    @Override
    protected Object getUserObject(UserInfo u) {
        return null;
    }
}
