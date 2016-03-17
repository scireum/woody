/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm

import sirius.biz.tenants.Tenants
import sirius.biz.tenants.TenantsHelper
import sirius.kernel.BaseSpecification
import sirius.kernel.di.std.Part
import sirius.mixing.OMA

class IndustrySpec extends BaseSpecification {

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    def "product can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant();
        Industry i = new Industry();
        i.getTenant().setValue(tenants.getRequiredTenant());
        i.setName("<Industry1>");
        oma.update(i);
        when:
        Optional opti = oma.find(Industry.class, i.getId());
        Industry ii = opti.get();
        then:
        !ii.isNew()
        and:
        ii.getName() == "<Industry1>"
        and:
        oma.select(Industry.class).count() == 1
    }

}
