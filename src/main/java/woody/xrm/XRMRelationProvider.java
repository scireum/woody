/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import com.google.common.collect.Lists;
import sirius.biz.model.AddressData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.Tenants;
import sirius.db.mixing.OMA;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.relations.RelationProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by aha on 11.01.17.
 */
@Register
public class XRMRelationProvider implements RelationProvider {

    @Part
    private OMA oma;

    @Part
    protected Tenants tenants;

    @Nonnull
    @Override
    public String getName() {
        return "XRM";
    }

    @Override
    public void computeSuggestions(String subType,
                                   String query,
                                   boolean forSearch,
                                   Consumer<Tuple<String, String>> consumer) {
        if (subType == null || "PERSON".equals(subType)) {
            oma.select(Person.class)
               .fields(Person.ID,
                       Person.UNIQUE_PATH,
                       Person.PERSON.inner(PersonData.TITLE),
                       Person.PERSON.inner(PersonData.FIRSTNAME),
                       Person.PERSON.inner(PersonData.LASTNAME),
                       Person.PERSON.inner(PersonData.SALUTATION))
               .eq(Person.COMPANY.join(Company.TENANT), tenants.getRequiredTenant())
               .where(Like.allWordsInAnyField(query,
                                              Person.PERSON.inner(PersonData.LASTNAME),
                                              Person.PERSON.inner(PersonData.FIRSTNAME)))
               .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
               .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME))
               .iterateAll(person -> {
                   String id = "XRM-" + person.getUniquePath();
                   if (forSearch) {
                       id += "*";
                   }

                   consumer.accept(Tuple.create(id, person.getPerson().toString()));
               });
        }
        if (subType == null || "COMPANY".equals(subType)) {
            oma.select(Company.class)
               .fields(Company.ID, Company.NAME)
               .eq(Company.TENANT, tenants.getRequiredTenant())
               .where(Like.allWordsInAnyField(query,
                                              Company.CUSTOMER_NUMBER,
                                              Company.NAME,
                                              Company.NAME2,
                                              Company.ADDRESS.inner(AddressData.CITY)))
               .orderAsc(Company.NAME)
               .iterateAll(company -> {
                   String id = "XRM-" + company.getUniquePath();
                   if (forSearch) {
                       id += "*";
                   }
                   consumer.accept(Tuple.create(id, company.getName()));
               });
        }
    }

    @Override
    public Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName) {
        if (Strings.isEmpty(uniqueObjectName)) {
            return Optional.empty();
        }

        Tuple<String, String> typeAndId = Strings.split(uniqueObjectName, "-");
        if (typeAndId.getSecond().length() <= 6) {
            return oma.select(Company.class)
                      .fields(Company.ID, Company.NAME)
                      .eq(Company.ID, Long.parseLong(typeAndId.getSecond(), 36))
                      .first()
                      .map(company -> ComparableTuple.create(company.getName(), "/company/" + company.getId()));
        } else {
            return oma.select(Person.class)
                      .fields(Person.PERSON.inner(PersonData.TITLE),
                              Person.COMPANY,
                              Person.ID,
                              Person.PERSON.inner(PersonData.FIRSTNAME),
                              Person.PERSON.inner(PersonData.LASTNAME),
                              Person.PERSON.inner(PersonData.SALUTATION))
                      .eq(Person.UNIQUE_PATH, typeAndId.getSecond())
                      .first()
                      .map(person -> ComparableTuple.create(person.toString(),
                                                            "/company/"
                                                            + person.getCompany().getId()
                                                            + "/person/"
                                                            + person.getId()));
        }
    }

    @Override
    public List<Tuple<String, String>> getSourceTypes() {
        List<Tuple<String, String>> result = Lists.newArrayList();
        result.add(Tuple.create("XRM-PERSON", NLS.get("Person.plural")+"(Y)"));
        result.add(Tuple.create("XRM-COMPANY", NLS.get("Company.plural")+"(Y)"));
        return result;
    }

    @Override
    public List<Tuple<String, String>> getTargetTypes() {
        return getSourceTypes();
    }
}
