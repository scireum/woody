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
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.Column;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Like;
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
 * Helps to build relations from and to <tt>UserAcconts</tt> our in our case {@link Employee}s.
 */
@Register
public class UserAccountRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    protected Tenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return Schema.getNameForType(UserAccount.class);
    }

    @Override
    public void computeSearchSuggestions(String subType, String query, Consumer<Tuple<String, String>> consumer) {
        oma.select(UserAccount.class)
           .fields(UserAccount.ID,
                   UserAccount.LOGIN.inner(LoginData.USERNAME),
                   Column.mixin(Employee.class).inner(Employee.SHORT_NAME),
                   UserAccount.PERSON.inner(PersonData.FIRSTNAME),
                   UserAccount.PERSON.inner(PersonData.LASTNAME))
           .eq(UserAccount.TENANT, tenants.getRequiredTenant())
           .where(Like.allWordsInAnyField(query,
                                          UserAccount.LOGIN.inner(LoginData.USERNAME),
                                          Column.mixin(Employee.class).inner(Employee.SHORT_NAME),
                                          UserAccount.PERSON.inner(PersonData.LASTNAME),
                                          UserAccount.PERSON.inner(PersonData.FIRSTNAME)))
           .orderAsc(Column.mixin(Employee.class).inner(Employee.SHORT_NAME))
           .orderAsc(UserAccount.LOGIN.inner(LoginData.USERNAME))
           .orderAsc(UserAccount.PERSON.inner(PersonData.LASTNAME))
           .orderAsc(UserAccount.PERSON.inner(PersonData.FIRSTNAME))
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
        Tuple<String, Long> typeAndId = Schema.parseUniqueName(uniqueObjectName);
        return oma.select(UserAccount.class)
                  .fields(UserAccount.ID,
                          UserAccount.LOGIN.inner(LoginData.USERNAME),
                          Column.mixin(Employee.class).inner(Employee.SHORT_NAME),
                          UserAccount.PERSON.inner(PersonData.FIRSTNAME),
                          UserAccount.PERSON.inner(PersonData.LASTNAME))
                  .eq(UserAccount.ID, typeAndId.getSecond())
                  .first()
                  .map(user -> ComparableTuple.create(renderUserAccount(user),
                                                      "/user-account/" + user.getIdAsString()));
    }

    protected String renderUserAccount(UserAccount user) {
        return Strings.isFilled(user.as(Employee.class).getShortName()) ?
               user.as(Employee.class).getShortName() :
               user.getLogin().getUsername();
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
