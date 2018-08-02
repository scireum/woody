/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm

import sirius.biz.jdbc.tenants.Tenants
import sirius.db.jdbc.OMA
import sirius.kernel.BaseSpecification
import sirius.kernel.commons.Amount
import sirius.kernel.di.std.Part

class CompanySpec extends BaseSpecification {

    @Part
    private static OMA oma

    @Part
    private static Tenants tenants

    def "company can be persisted into the database"() {
        given:
        TenantsHelper.installTestTenant()
        Company c = new Company()
        c.getTenant().setValue(tenants.getRequiredTenant())
        c.setName("Testcompany1")
        c.setBusinessType("Industrie")
        c.setCompanyType("Kunde")
        c.setCustomerNr("0815_4711")
        c.setInvoiceMedium("MAIL")
        c.setInvoiceEmailAdr("mail47111@mail.com")
        c.setMatchcode("007")
        c.getContactData().setPhone("01234/56789")
        c.getAddress().setCity("London")
        c.getAddress().setCountry("GB")
        c.getInvoiceAddress().setZip("12345")
        c.getPostboxAddress().setStreet("Postbox 12345")
        c.setPtPrice(Amount.of(Double.parseDouble("1234.56")))
        oma.update(c)
        when:
        Optional opti = oma.find(Company.class, c.getId())
        Company cc = (Company) opti.get()
        then:
        !cc.isNew()
        and:
        cc.getName() == "Testcompany1"
        and:
        oma.select(Company.class).count() == 1
        and:
        cc.getContactData().getPhone() == "0049123456789"
        and:
        cc.getInvoiceMedium() == "MAIL"
        and:
        cc.getAddress().getCity() == "London"
        and:
        cc.getInvoiceAddress().getZip() == "12345"
        and:
        cc.getPostboxAddress().getStreet() == "Postbox 12345"
        and:
        cc.getMatchcode() == "007"
        and:
        cc.getPtPrice() == Amount.of(1234.56D)
        and:
        cc.getCustomerNr() == "0815_4711"
    }

}
