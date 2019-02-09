/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.opportunities;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.jdbc.model.BizEntity;
import sirius.biz.jdbc.model.PersonData;
import sirius.biz.jdbc.tenants.Tenants;
import sirius.biz.jdbc.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.sales.accounting.Product;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;
import java.util.List;

/**
 * Opportunity = Gelegenheit für ein Geschäft. Here are stored all opportunities
 */
public class Opportunity extends BizEntity {

    @Autoloaded
    @Length(255)
    private String source;
    public static final Mapping SOURCE = Mapping.named("source");

    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    @NullAllowed
    private Amount contractValue;
    public static final Mapping CONTRACT_VALUE = Mapping.named("contractValue");

    @Autoloaded
    private OpportunityState state = OpportunityState.OPEN;
    public static final Mapping STATE = Mapping.named("state");

    @Autoloaded
    @NullAllowed
    private LocalDate nextInteraction;
    public static final Mapping NEXT_INTERACTION = Mapping.named("nextInteraction");

    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping COMPANY = Mapping.named("company");

    /* person is the contact-person */
    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Person> person = SQLEntityRef.on(Person.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping PERSON = Mapping.named("person");

    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<UserAccount> useraccount =
            SQLEntityRef.on(UserAccount.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping EMPLOYEE = Mapping.named("employee");

    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Product> product = SQLEntityRef.on(Product.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping PRODUCT = Mapping.named("product");

    // ToDo tags, wie modellieren?
//    @Filter(position = 1)
//    @FormField(position = 50, editGroup = "tags")
//    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, targetEntity = Tag.class)
//    @JoinTable(name = "tagopportunityassignment",
//            joinColumns = @JoinColumn(name = "person"),
//            inverseJoinColumns = @JoinColumn(name = "tag"))
//    private Set<Tag> tags = new HashSet<Tag>();
//    public static final String TAGS = "tags";

    // these methods are called by java-script
    @Part
    private static Tenants tenants;

    public List<Product> getAllProducts() {
        List<Product> list = oma.select(Product.class)
                                .eq(Product.TENANT, tenants.getRequiredTenant())
                                .orderAsc(Product.NAME)
                                .queryList();
        return list;
    }

    public List<Person> getAllPersons() {
        Company company = this.getCompany().getValue();
        List<Person> list = oma.select(Person.class)
                               .eq(Person.COMPANY, company)
                               .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                               .queryList();
        return list;
    }

    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = oma.select(UserAccount.class)
                                    .eq(UserAccount.TENANT, tenants.getRequiredTenant())
                                    .orderAsc(UserAccount.PERSON.inner(PersonData.LASTNAME))
                                    .queryList();
        return list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Strings.join(" ",
                               company == null ? null : company.getValue().toString(),
                               product == null ? null : "- " + product.getValue().getName(),
                               source == null ? null : "(" + source + ")"));
        return sb.toString();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Amount getContractValue() {
        return contractValue;
    }

    public void setContractValue(Amount contractValue) {
        this.contractValue = contractValue;
    }

    public OpportunityState getState() {
        return state;
    }

    public void setState(OpportunityState state) {
        this.state = state;
    }

    public LocalDate getNextInteraction() {
        return nextInteraction;
    }

    public void setNextInteraction(LocalDate nextInteraction) {
        this.nextInteraction = nextInteraction;
    }

    public SQLEntityRef<Company> getCompany() {
        return company;
    }

    public SQLEntityRef<Person> getPerson() {
        return person;
    }

    public SQLEntityRef<UserAccount> getUseraccount() {
        return useraccount;
    }

    public SQLEntityRef<Product> getProduct() {
        return product;
    }
}
