/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.opportunities;
import sirius.biz.model.BizEntity;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.OMA;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.web.security.UserContext;
import woody.campaigns.Campaign;
import woody.core.comments.Comment;
import woody.core.employees.Employee;
import woody.core.tags.Tagged;
import woody.offers.OfferState;
import woody.sales.Product;
import woody.xrm.Company;
import woody.xrm.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Opportunity = Gelegenheit für ein Geschäft. Here are stored all opportunities
 */
public class Opportunity extends BizEntity {

    @Autoloaded
    @Length(255)
    private String source;
    public static final Column SOURCE = Column.named("source");

    /** Zu dieser Opportunity gehörende Kampagne */
    @Autoloaded
    @NullAllowed
    private final EntityRef<Campaign> campaign = EntityRef.on(Campaign.class, EntityRef.OnDelete.CASCADE);
    public static final String CAMPAIGN = "campaign";

    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    @NullAllowed
    private Amount contractValue = Amount.NOTHING;
    public static final Column CONTRACT_VALUE = Column.named("contractValue");

    @Autoloaded
    private OpportunityState oldState = OpportunityState.COLD;
    public static final Column OLDSTATE = Column.named("oldState");

    @Autoloaded
    private OpportunityState newState = OpportunityState.COLD;
    public static final Column NEWSTATE = Column.named("newState");

    @Autoloaded
    @NullAllowed
    private LocalDate nextInteraction;
    public static final Column NEXT_INTERACTION = Column.named("nextInteraction");

    @Autoloaded
    @NullAllowed
    private LocalDate sortDate;
    public static final Column SORTDATE = Column.named("sortDate");

    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount sortValue = Amount.NOTHING;
    public static final Column SORT_VALUE = Column.named("sortValue");

    @Autoloaded
    @NullAllowed
    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    /* person is the contact-person */
    @Autoloaded
    @NullAllowed
    private final EntityRef<Person> person = EntityRef.on(Person.class, EntityRef.OnDelete.CASCADE);
    public static final Column PERSON = Column.named("person");

    @Autoloaded
    @NullAllowed
    private final EntityRef<UserAccount> userAccount = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);
    public static final Column USERACCOUNT = Column.named("userAccount");

    @Autoloaded
    @NullAllowed
    private final EntityRef<Product> product = EntityRef.on(Product.class, EntityRef.OnDelete.CASCADE);
    public static final Column PRODUCT = Column.named("product");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");


    // these methods are called by java-script
    @Part
    private static Tenants tenants;

    public List<Product> getAllProducts() {
        List<Product> list = oma.select(Product.class).eq(Product.TENANT, tenants.getRequiredTenant())
            .orderAsc(Product.NAME).queryList();
        return list;
    }

    public List<Person> getAllPersons() {
        Company company = this.getCompany().getValue();
        List<Person> list = oma.select(Person.class).eq(Person.COMPANY, company)
                               .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))  .queryList();
        return list;
    }

    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = oma.select(UserAccount.class).eq(UserAccount.TENANT, tenants.getRequiredTenant())
                                    .orderAsc(UserAccount.PERSON.inner(PersonData.LASTNAME)).queryList();
//        UserAccount user = null;
//        Employee employee = user.as(Employee.class);
//        employee.getShortName();
        return list;
    }

    public List<OpportunityState> getOpportunityStateValues() {
        OpportunityState[] states = OpportunityState.values();
        List<OpportunityState> list = new ArrayList();
        for(int i=0; i<states.length; i++) {
            list.add(states[i]);
        }
        return list;
    }

    public boolean isEmailAdrPresent() {
        if(this.getPerson() == null ) {return false;}
        if(this.getPerson().getValue().getContact() == null) {return false;}
        if(Strings.isFilled(this.getPerson().getValue().getContact().getEmail())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPhoneNrPresent() {
        if(this.getPerson() == null ) {return false;}
        if(this.getPerson().getValue().getContact() == null) {return false;}
        if(Strings.isFilled(this.getPerson().getValue().getContact().getPhone())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMobileNrPresent() {
        if(this.getPerson() == null ) {return false;}
        if(this.getPerson().getValue().getContact() == null) {return false;}
        if(Strings.isFilled(this.getPerson().getValue().getContact().getMobile())) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Strings.join(" ",
                             company == null ? null : company.getValue().toString(),
                             product == null ? null : "- " + product.getValue().getName(),
                             source == null ? null : "(" + source + ")"));
        return sb.toString();
    }

    public Employee getEmployee() {
        return userAccount.getValue().as(Employee.class);
    }



    @BeforeSave
    protected void onSave() {

        // ToDo Hinweis machen
//        if (this.product == null) {
//                ApplicationController.addErrorMessage("Hinweis: bei der Chance: " + this.toString() + " bitte das Produkt angeben.");
//                runOneTime = true;
//            }
//        }

        // we can only check the changes if the opportunity is not new
        if (this.isNew()) {
            this.oldState = this.newState;
        } else {
            // check the change of the state if the opportunity ist NOT new
            if (!(this.newState.equals(this.oldState))) {
                OpportunityStateChanges osc = new OpportunityStateChanges();
                osc.getOpportunity().setValue(this);
                osc.getUserAccount().setValue(UserContext.getCurrentUser().as(UserAccount.class));
                osc.setNewState(this.newState);
                osc.setOldState(this.oldState);
                oma.update(osc);

                this.oldState = this.newState;

                // add a comment
                Comment comment = new Comment();
                comment.setTextContent(osc.toString());
                comment.setTod(osc.getDatetime());
                comment.setTargetEntity(this.getUniqueName());
                oma.update(comment);
            }
        }

        // set the sortDate
        LocalDate today = LocalDate.now();
        if(nextInteraction == null) {
            sortDate = today;
        } else {
            if(nextInteraction.isBefore(today)) {
                // nextInteraction is late  --> set today
                sortDate = today;
            } else {
                // set nextInteraction
                sortDate = nextInteraction;
            }
        }

        // set the sortValue
        if(contractValue == null) {
            sortValue = Amount.ZERO;
        } else {
            sortValue = contractValue;
        }
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

    public EntityRef<Campaign> getCampaign() {
        return campaign;
    }

    public OpportunityState getOldState() {
        return oldState;
    }

    public void setOldState(OpportunityState oldState) {
        this.oldState = oldState;
    }

    public OpportunityState getNewState() {
        return newState;
    }

    public void setNewState(OpportunityState newState) {
        this.newState = newState;
    }

    public LocalDate getSortDate() {
        return sortDate;
    }

    public void setSortDate(LocalDate sortDate) {
        this.sortDate = sortDate;
    }

    public Amount getSortValue() {
        return sortValue;
    }

    public void setSortValue(Amount sortValue) {
        this.sortValue = sortValue;
    }

    public Tagged getTags() {
        return tags;
    }

    public LocalDate getNextInteraction() {
        return nextInteraction;
    }

    public void setNextInteraction(LocalDate nextInteraction) {
        this.nextInteraction = nextInteraction;
    }

    public EntityRef<Company> getCompany() {
        return company;
    }

    public EntityRef<Person> getPerson() {
        return person;
    }

    public EntityRef<UserAccount> getUserAccount() {
        return userAccount;
    }

    public EntityRef<Product> getProduct() {
        return product;
    }
}
