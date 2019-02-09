/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.PersonData;
import sirius.db.jdbc.OMA;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.security.HelperFactory;
import sirius.web.security.ScopeInfo;

import javax.annotation.Nonnull;
import java.util.List;

public class XRMHelper {

    @Register
    public static class XRMHelperFactory implements HelperFactory<XRMHelper> {

        @Nonnull
        @Override
        public Class<XRMHelper> getHelperType() {
            return XRMHelper.class;
        }

        @Nonnull
        @Override
        public String getName() {
            return "xrm";
        }

        @Nonnull
        @Override
        public XRMHelper make(@Nonnull ScopeInfo scopeInfo) {
            return new XRMHelper();
        }
    }

    @Part
    private static OMA oma;

    public List<Person> queryPersons(Company company) {
        return oma.select(Person.class)
                  .eq(Person.COMPANY, company)
                  .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                  .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME))
                  .limit(5)
                  .queryList();
    }
}
