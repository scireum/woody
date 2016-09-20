/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales

import sirius.biz.tenants.Tenants
import sirius.biz.tenants.TenantsHelper
import sirius.db.mixing.OMA
import sirius.kernel.BaseSpecification
import sirius.kernel.commons.Amount
import sirius.kernel.di.std.Part


/**
 * Created by gerhardhaufler on 09.02.16.
 */
class PackageDefinitionSpec extends BaseSpecification {

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    def "packageDefinition can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant();

        Product p = new Product();
        p.getTenant().setValue(tenants.getRequiredTenant());
        p.setName("<Product2>");
        p.setArticle("<Article2>");
        oma.update(p);

        PackageDefinition pd = new PackageDefinition();
        pd.getProduct().setValue(p);
        pd.setName("<PaketDef1_Product2>");
        pd.setDescription("this is packetDefinition 1 of the product 2");
        pd.setAccountingProcedure(PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL);
        pd.setAccountingUnit(PackageDefinition.ACCOUNTINGUNIT_MONTH);
        pd.setDefaultPosition(10);
        pd.setPacketType(PackageDefinition.PAKETTYPE_STANDARD);
        pd.setUnitPrice(Amount.of(500D));
        pd.setSinglePrice(Amount.of(0D));
        oma.update(pd);
        when:
        PackageDefinition pd2 = oma.select(PackageDefinition.class).eq(PackageDefinition.ID, pd.getId()).queryFirst();

        then:
        !pd2.isNew()
        and:
        pd2.getProduct().getValue().getName() == "<Product2>";
        and:
        pd2.getName() == "<PaketDef1_Product2>";
        and:
        pd2.getUnitPrice() == Amount.of(500D);
        and:
        oma.select(PackageDefinition.class).count() == 1
    }

}
