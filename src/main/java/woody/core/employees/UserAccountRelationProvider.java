/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import sirius.biz.model.LoginData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.UserAccount;
import sirius.biz.tenants.UserAccountData;
import sirius.biz.tenants.jdbc.SQLTenants;
import sirius.biz.tenants.jdbc.SQLUserAccount;
import sirius.db.jdbc.OMA;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.relations.RelationProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Helps to build relations from and to {@link UserAccount user accounts} our in our case {@link Employee employees}.
 */
@Register
public class UserAccountRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    private Mixing mixing;

    @Part
    protected SQLTenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return UserAccount.class.getSimpleName().toUpperCase();
    }

    @Override
    public void computeSearchSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        oma.select(SQLUserAccount.class)
           .fields(SQLUserAccount.ID,
                   UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.LOGIN).inner(LoginData.USERNAME),
                   Mapping.mixin(Employee.class).inner(Employee.SHORT_NAME),
                   UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.FIRSTNAME),
                   UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.LASTNAME))
           .eq(UserAccount.TENANT, tenants.getRequiredTenant())
           .queryString(query,
                        QueryField.contains(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.LOGIN)
                                                                         .inner(LoginData.USERNAME)),
                        QueryField.contains(Mapping.mixin(Employee.class).inner(Employee.SHORT_NAME)),
                        QueryField.contains(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON)
                                                                         .inner(PersonData.LASTNAME)),
                        QueryField.contains(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON)
                                                                         .inner(PersonData.FIRSTNAME)))
           .orderAsc(Mapping.mixin(Employee.class).inner(Employee.SHORT_NAME))
           .orderAsc(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.LOGIN).inner(LoginData.USERNAME))
           .orderAsc(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.LASTNAME))
           .orderAsc(UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.FIRSTNAME))
           .iterateAll(user -> consumer.accept(Tuple.create(getName() + "-" + user.getIdAsString(),
                                                            renderUserAccount(user))));
    }

    @Override
    public void computeTargetSuggestions(@Nullable String subType,
                                         @Nonnull String query,
                                         @Nonnull Consumer<Tuple<String, String>> consumer) {
        computeSearchSuggestions(subType, query, consumer);
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        Tuple<String, String> typeAndId = Mixing.splitUniqueName(uniqueObjectName);
        return oma.select(SQLUserAccount.class)
                  .fields(SQLUserAccount.ID,
                          UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.LOGIN).inner(LoginData.USERNAME),
                          Mapping.mixin(Employee.class).inner(Employee.SHORT_NAME),
                          UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.FIRSTNAME),
                          UserAccount.USER_ACCOUNT_DATA.inner(UserAccountData.PERSON).inner(PersonData.LASTNAME))
                  .eq(SQLUserAccount.ID, typeAndId.getSecond())
                  .first()
                  .map(user -> ComparableTuple.create(renderUserAccount(user),
                                                      "/user-account/" + user.getIdAsString()));
    }

    protected String renderUserAccount(SQLUserAccount user) {
        return Strings.isFilled(user.as(Employee.class).getShortName()) ?
               user.as(Employee.class).getShortName() :
               user.getUserAccountData().getLogin().getUsername();
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        return Collections.singletonList(Tuple.create(getName(), NLS.get("UserAccount.plural")));
    }

    @Override
    public List<Tuple<String, String>> getTargetTypes() {
        return getSourceTypes();
    }
}
