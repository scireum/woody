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

/**
 * Created by gerhardhaufler on 09.02.16.
 */
class PersonSpec extends BaseSpecification {

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    def "person can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant();

        Company c = new Company();
        c.getTenant().setValue(tenants.getRequiredTenant());
        c.setName("Testcompany2");
        c.setBusinessType("Industrie");
        c.setCompanyType("Kunde");
        c.setInvoiceMedium(InvoiceMediumType.MAIL);
        oma.update(c);

        Person p = new Person();
        p.getCompany().setValue(c);

        p.getPerson().setFirstname("<Firstname1>") ;
        p.getPerson().setLastname("<Lastname1>") ;
        p.getPerson().setSalutation("Herr") ;
        p.getPerson().setTitle("Prof.");
        p.getContact().setPhone("+772233445566");
        p.getContact().setEmail("mail1@mail.com") ;

        p.setPosition("Manager");
        p.getLogin().setUsername("<User1>");
        when:
        oma.update(p);

        then:
        !p.isNew()
        and:
        p.getPerson().getLastname() == "<Lastname1>" ;
        and:
        p.getLogin().getUsername() == "<User1>" ;
        and:
        p.getContact().getEmail() == "mail1@mail.com" ;
        and:
        oma.select(Person.class).count() == 1
    }

}
