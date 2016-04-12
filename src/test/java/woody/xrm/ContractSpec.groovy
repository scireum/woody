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
import sirius.kernel.commons.Amount
import java.time.LocalDate;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
class ContractSpec extends BaseSpecification {

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    def "contract can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant();
        Company co = new Company();
        co.getTenant().setValue(tenants.getRequiredTenant());
        co.setName("Testcompany1");
        co.setBusinessType("Industrie");
        co.setCompanyType("Kunde");
        co.setInvoiceMedium("MAIL");
        oma.update(co);

        Person p = new Person();
        p.getCompany().setValue(co);
        p.getPerson().setFirstname("<Firstname1>") ;
        p.getPerson().setLastname("<Lastname1>") ;
        p.getPerson().setSalutation("Herr") ;
        p.getPerson().setTitle("Prof.");
        p.getContact().setPhone("+772233445566");
        p.getContact().setEmail("mail1@mail.com") ;
        p.setPosition("Manager");
        p.getLogin().setUsername("<User1>");
        oma.update(p);

        Product pr = new Product();
        pr.getTenant().setValue(tenants.getRequiredTenant());
        pr.setName("<Product2>");
        pr.setArticle("<Article2>");
        oma.update(pr);

        PackageDefinition pd = new PackageDefinition();
        pd.getProduct().setValue(pr);
        pd.setName("<PaketDef1_Product2>");
        pd.setDescription("this is packetDefinition 1 of the product 2");
        pd.setAccountingProcedure(AccountingProcedure.RIVAL) ;
        pd.setAccountingUnit(AccountingUnitType.MONTH);
        pd.setDefaultPosition(10);
        pd.setPacketType(PacketType.STANDARD) ;
        pd.setUnitPrice(Amount.of(500D));
        pd.setSinglePrice(Amount.of(0D));
        oma.update(pd);

        PackageDefinition pd2 = new PackageDefinition();
        pd2.getProduct().setValue(pr);
        pd2.setName("<PaketDef2_Product2>");
        pd2.setDescription("this is packetDefinition 2 of the product 2");
        pd2.setAccountingProcedure(AccountingProcedure.RIVAL) ;
        pd2.setAccountingUnit(AccountingUnitType.MONTH);
        pd2.setDefaultPosition(10);
        pd2.setPacketType(PacketType.STANDARD) ;
        pd2.setUnitPrice(Amount.of(1000D));
        pd2.setSinglePrice(Amount.of(0D));
        oma.update(pd2);

        Contract  c = new Contract();
        c.getCompany().setValue(co);
        c.getContractPartner().setValue(p);
        c.setAccountingGroup("1");
        c.setAccountingInterval(AccountingIntervalType.YEAR);
        c.getPackageDefinition().setValue(pd);
        c.setSigningDate(LocalDate.of(2015,12,07));
        c.setStartDate(LocalDate.of(2016,02,01)) ;
        c.getPackageDefinition().setValue(pd2);
        oma.update(c)
        when:

        Optional<Contract> opti = oma.find(Contract.class, c.getId()) ;
        then:
        Contract cc = (Contract) opti.get();
        cc.getPackageDefinition().getValue().getProduct().getValue().getName() == "<Product2>" ;
        and:
        cc.getPackageDefinition().getValue().getName() == "<PaketDef2_Product2>";
        and:
        cc.getStartDate() == LocalDate.of(2016,02,01);
        and:
        oma.select(Contract.class).count() == 1
    }

}
