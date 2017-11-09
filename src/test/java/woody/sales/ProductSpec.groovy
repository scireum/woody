/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales

import sirius.biz.tenants.Tenants
// import sirius.biz.tenants.TenantsHelper
import sirius.db.mixing.OMA
import sirius.kernel.BaseSpecification
import sirius.kernel.di.std.Part

class ProductSpec extends BaseSpecification {

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    def "product can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant();
        Product p = new Product();
        p.getTenant().setValue(tenants.getRequiredTenant());
        p.setName("<Product1>");
        p.setArticle("<Article1>");
        p.setDescription("this is product1, in collmex named as article 1");
        when:
        oma.update(p);
        then:
        !p.isNew()
        and:
        p.getName() == "<Product1>"
        and:
        oma.select(Product.class).count() == 1
    }

}
