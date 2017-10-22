/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.css;

import com.google.common.collect.Sets;
import sirius.biz.model.LoginData;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.db.mixing.OMA;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.settings.Extension;
import sirius.web.http.WebContext;
import sirius.web.security.GenericUserManager;
import sirius.web.security.ScopeInfo;
import sirius.web.security.UserInfo;
import sirius.web.security.UserManager;
import sirius.web.security.UserManagerFactory;
import woody.xrm.Person;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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

    private static Cache<String, Set<String>> rolesCache = CacheManager.createCache("css-roles");
    private static Cache<String, Person> userAccountCache = CacheManager.createCache("css-users");

    @Override
    public UserInfo findUserByName(WebContext webContext, String user) {
        if (Strings.isEmpty(user)) {
            return null;
        }
        Optional<Person> personOptional =
                oma.select(Person.class).eq(Person.LOGIN.inner(LoginData.USERNAME), user).one();
        if (personOptional.isPresent()) {
            if (personOptional.get().getLogin().isAccountLocked()) {
                throw Exceptions.createHandled().withNLSKey("LoginData.accountIsLocked").handle();
            }
            Person person = personOptional.get();
            userAccountCache.put(person.getIdAsString(), person);
            rolesCache.remove(person.getIdAsString());

            return UserInfo.Builder.createUser(String.valueOf(person.getId()))
                                   .withUsername(person.getLogin().getUsername())
                                   .withTenantId(String.valueOf(person.getCompany().getId()))
                                   .withTenantName(person.getCompany().getValue().getName())
                                   .withEmail(person.getContact().getEmail())
                                   .withPermissions(computeRoles(null, String.valueOf(person.getId())))
                                   .withSettingsSupplier(ui -> getScopeSettings())
                                   .withUserSupplier(this::getUserObject)
                                   .build();
        } else {
            return null;
        }
    }

    @Override
    public UserInfo findUserByCredentials(WebContext webContext, String user, String password) {
        if (Strings.isEmpty(password)) {
            return null;
        }
        UserInfo result = findUserByName(webContext, user);
        if (result == null) {
            return null;
        }
        LoginData loginData = result.getUserObject(UserAccount.class).getLogin();
        if (LoginData.hashPassword(loginData.getSalt(), password).equals(loginData.getPasswordHash())) {
            return result;
        }
        return null;
    }

    @Override
    protected void recordUserLogin(WebContext ctx, UserInfo user) {
        try {
            UserAccount account = (UserAccount) getUserObject(user);
            account.getTrace().setSilent(true);
            account.getLogin().setNumberOfLogins(account.getLogin().getNumberOfLogins() + 1);
            account.getLogin().setLastLogin(LocalDateTime.now());
            oma.update(account);
        } catch (Throwable e) {
            Exceptions.handle(BizController.LOG, e);
        }
    }

    @Override
    protected Object getUserObject(UserInfo userInfo) {
        return getUserById(userInfo.getUserId());
    }

    protected Person getUserById(String id) {
        return userAccountCache.get(id, i -> oma.findOrFail(Person.class, i));
    }

    @Override
    protected Set<String> computeRoles(WebContext ctx, String userId) {
        Set<String> cachedRoles = rolesCache.get(userId);
        if (cachedRoles != null) {
            return cachedRoles;
        }
        Set<String> roles = Sets.newTreeSet();
        Person user = getUserById(userId);
        if (user != null && !user.getLogin().isAccountLocked()) {
//            roles.addAll(user.getPermissions().getPermissions());
//            roles.addAll(user.getTenant().getValue().getPermissions().getPermissions());
            roles.add(UserInfo.PERMISSION_LOGGED_IN);
            roles = transformRoles(roles, false);
        }
        rolesCache.put(userId, roles);
        return roles;
    }
}
