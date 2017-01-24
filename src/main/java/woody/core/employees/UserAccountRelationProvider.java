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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by aha on 11.01.17.
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
    public void computeSuggestions(String subType,
                                   String query,
                                   boolean forSearch,
                                   Consumer<Tuple<String, String>> consumer) {
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
           .iterateAll(user -> {
               consumer.accept(Tuple.create(user.getUniqueName(), user.getLogin().getUsername()));
           });
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        if (Strings.isEmpty(uniqueObjectName)) {
            return Optional.empty();
        }

        Tuple<String, String> typeAndId = Strings.split(uniqueObjectName, "-");
        return oma.select(UserAccount.class)
                  .fields(UserAccount.ID,
                          UserAccount.LOGIN.inner(LoginData.USERNAME),
                          Column.mixin(Employee.class).inner(Employee.SHORT_NAME),
                          UserAccount.PERSON.inner(PersonData.FIRSTNAME),
                          UserAccount.PERSON.inner(PersonData.LASTNAME))
                  .eq(UserAccount.ID, Long.parseLong(typeAndId.getSecond()))
                  .first()
                  .map(user -> ComparableTuple.create(Strings.isFilled(user.as(Employee.class).getShortName()) ?
                                                      user.as(Employee.class).getShortName() :
                                                      user.getLogin().getUsername(),
                                                      "/user-account/" + user.getIdAsString()));
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
