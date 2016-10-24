/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import sirius.biz.model.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Transient;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.core.employees.Employee;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;

/**
 * Created by gerhardhaufler on 11.10.16.
 */
public class Offer extends BizEntity {

    private static final int MIN_OFFER_NR = 20001;

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");


    @Unique
//  ToDo           @Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true") })
    @Length(20)
    private String number;
    public static final Column NUMBER = Column.named("number");

    @Autoloaded
    private OfferState state = OfferState.OPEN;
    public static final Column STATE = Column.named("state");

    @Autoloaded
    @Length(100)
    private String keyword;
    public static final Column KEYWORD = Column.named("keyword");

// ToDo    @FieldLink(source = "company", target = "company")
    @Autoloaded
    private final EntityRef<Person> person = EntityRef.on(Person.class, EntityRef.OnDelete.CASCADE);
    public static final Column PERSON = Column.named("person");

// ToDo    @Params(@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "false"))
    @Autoloaded

// ToDO Employee ist extend Mixable --> Fehler bei  EntityRef.on(Employee.class
//  private final EntityRef<Employee> employee = EntityRef.on(Employee.class, EntityRef.OnDelete.CASCADE);
    private Employee employee;  // ToDo Testeintrag, damit es compiliert, muss wieder raus
    public static final Column EMPLOYEE = Column.named("employee");

// ToDo    @Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")
    @Autoloaded
    private LocalDate date;
    public static final Column DATE = Column.named("date");

// ToDo    @FieldLink(source = "company", target = "company")
    @Autoloaded
    @NullAllowed
    private final EntityRef<Person> buyer = EntityRef.on(Person.class, EntityRef.OnDelete.CASCADE);
    public static final Column BUYER = Column.named("buyer");

    @Autoloaded
    @Length(100)
    @NullAllowed
    private String reference;
    public static final Column REFERENCE = Column.named("reference");

    @Transient
    private boolean serviceItemPresent;
    public static final Column SERVICEITEMPRESENT = Column.named("serviceItemPresent");

    @Transient
    private boolean licenceItemPresent;
    public static final Column LICENCEITEMPRESENT = Column.named("licenceItemPresent");


    protected void asString(StringBuilder sb) {
        sb.append("Angebot ") ;
        sb.append(number);
    }

    public String toString() {
        String s = "Angebot ".concat(number);
        return s;
    }
    @Part
    private static ServiceAccountingService sas;

    @BeforeSave
    protected void onSave()   {
        // check te Role of the user
        // ToDo RechtsNachfolger für User.hasRole(CRM.GL)
        UserInfo userInfo = UserContext.getCurrentUser() ;
        userInfo.hasPermission("GL");
//        if (Users.hasRole(CRM.GL) || Users.hasRole(CRM.OFFER)) {
//            // do nothing
//        } else {
//            throw Exceptions.createHandled().withNLSKey("Offer.noSavePermission")
//                        .set("value", number).handle();

//        }
        // Calculate the offer-number if the offer is new (id == 0)
        if(this.getId() == 0 ) {

// ToDo Rechtsnachfolger für PersistentSerialNumberGenerator.generateSerialNumber
//            oma.transaction(new TXN() {
//                @Override
//                public void doWork(Session session) throws Exception {
//                    number = PersistentSerialNumberGenerator.generateSerialNumber(session, "OFFER", MIN_OFFER_NR)  ;
//                }
//            });

        }
        int offerNr = -1;
        try {
            offerNr = Integer.parseInt(number);
        }
        catch(Exception e) {
            throw Exceptions.createHandled().withNLSKey("Offer.numberWrong")
                            .set("value", number).handle();
        }

        if(offerNr < MIN_OFFER_NR) {
            throw Exceptions.createHandled().withNLSKey("Offer.numberToLess")
                            .set("value", number).set("minOfferNr", MIN_OFFER_NR).handle();
        }
        // get the employee
        if(employee == null) {
            UserInfo userInfo1 = UserContext.getCurrentUser();
            employee = userInfo1.as(Employee.class);
            // ToDO testen ob das Ersatz für CRM.getCurrent   ist
//            employee = CRM.getCurrent();
        }
        //set the offer-date
        if(date == null) {
            date = LocalDate.now();
        }

        // update the offerState
        sas.updateOfferState(this);
    }

    public EntityRef<Company> getCompany() {
        return company;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public OfferState getState() {
        return state;
    }

    public void setState(OfferState state) {
        this.state = state;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public EntityRef<Person> getPerson() {
        return person;
    }

    // Todo wieder reinmachen
    //   public EntityRef<Employee> getEmployee() {
    //       return employee;
    //}

    public Employee getEmployee() {return employee;}

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EntityRef<Person> getBuyer() {
        return buyer;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isServiceItemPresent() {
        return serviceItemPresent;
    }

    public void setServiceItemPresent(boolean serviceItemPresent) {
        this.serviceItemPresent = serviceItemPresent;
    }

    public boolean isLicenceItemPresent() {
        return licenceItemPresent;
    }

    public void setLicenceItemPresent(boolean licenceItemPresent) {
        this.licenceItemPresent = licenceItemPresent;
    }
}