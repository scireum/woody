/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.codelists.CodeLists;
import sirius.biz.model.AddressData;
import sirius.biz.model.ContactData;
import sirius.biz.model.InternationalAddressData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.mails.Mails;
import woody.core.colors.Colors;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relateable;
import woody.core.relations.Relations;
import woody.core.tags.TagQueryTagColorTypeProvider;
import woody.core.tags.Tagged;

import woody.phoneCalls.SyncAsterisk;
import woody.sales.CompanyAccountingData;
import woody.sales.Contract;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by aha on 06.10.15.
 */
public class Company extends TenantAware implements HasComments, HasRelations {

    @Part
    private static Mails mails;

    @Part
    private static CodeLists cls;

    public static final int MIN_CUSTOMERNR = 10000;
    public static final int MAX_CUSTOMERNR = 19999;
    public static final String COUNTRY_CODELIST = "country";

    @Trim
    @Autoloaded
    @Length(255)
    private String name;
    public static final Column NAME = Column.named("name");

    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String name2;
    public static final Column NAME2 = Column.named("name2");

    // the customerNr is set by the sequence.generateId --> no @Autoloaded !
    @NullAllowed
    @Length(50)
    private String customerNumber;
    public static final Column CUSTOMER_NUMBER = Column.named("customerNumber");

    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String website;
    public static final Column WEBSITE = Column.named("website");

    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String matchcode;
    public static final Column MATCHCODE = Column.named("matchcode");

    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String image;
    public static final Column IMAGE = Column.named("image");

    public static final Column ADDRESS = Column.named("address");
    private final InternationalAddressData address =
            new InternationalAddressData(AddressData.Requirements.NOT_PARTIAL, null);

    public static final Column POSTBOX_ADDRESS = Column.named("postboxAddress");
    private final InternationalAddressData postboxAddress =
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL,
                                         NLS.get("Company.postboxAddress"));

    private final ContactData contact = new ContactData(true);
    public static final Column CONTACT = Column.named("contact");

    private final CompanyAccountingData companyAccountingData = new CompanyAccountingData();
    public static final Column COMPANYACCOUNTINGDATA = Column.named("companyAccountingData");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    public static final Column RELATIONS = Column.named("relations");
    private final Relations relations = new Relations(this);

    public static final Column RELATEABLE = Column.named("relateable");
    private final Relateable relateable = new Relateable(this);

    @Override
    public String toString() {
        if (!isNew()) {
            return getName();
        } else {
            return super.toString();
        }
    }

    // used for views
    public String getCorrelationId() {
        return matchcode;
    }



   @BeforeSave
    protected void onSave() {
       // check te customerNR - if present
       if(customerNumber != null) {
           Integer nr = Integer.parseInt(customerNumber);
           if (nr < MIN_CUSTOMERNR || nr > MAX_CUSTOMERNR) {
               throw Exceptions.createHandled()
                               .withNLSKey("Company.customerNumberOutOfInterval")
                               .set("customerNumber", customerNumber)
                               .set("min", MIN_CUSTOMERNR)
                               .set("max", MAX_CUSTOMERNR)
                               .handle();
           }
       } else {
           // check the presence of a customer-number if contracts are existing
           long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
           if (count > 0 && Strings.isEmpty(customerNumber)) {
               throw Exceptions.createHandled().withNLSKey("Company.ContractsArePresent.CustomerNrIsMissing").handle();
           }
       }
       // normalize the mainPhoneNr
       String phoneNumber = this.getContact().getPhone();
       if(phoneNumber != null && Strings.isFilled(phoneNumber)) {
            this.getContact().setPhone(SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(phoneNumber, true));
       }
       // ToDo inkompatibel mit Validierung der Mailadresse in ContactData
//       //check the mainMailAddress, the mainMailAddress is stored in .ContactData.eMail
//       if(Strings.isFilled(this.getContact().getEmail())) {
//            // mimimal: a.b
//            if (this.getContact().getEmail().length() < 3) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Model.mainMailAddressToShort")
//                                .set("value", this.getContact().getEmail())
//                                .handle();
//            } else {
//                // cut out the @   aa@bbbbb.cc ---> bbbbb.cc
//                int pos = this.getContact().getEmail().indexOf("@");
//                if (pos > -1) {
//                    this.getContact().setEmail(this.getContact().getEmail().substring(pos + 1));
//                }
//                // check the presence of the "."
//                pos = this.getContact().getEmail().indexOf(".");
//                // missing   or  at the last index
//                if(pos == -1 || pos == this.getContact().getEmail().length() - 1) {
//                    throw Exceptions.createHandled()
//                                    .withNLSKey("Model.mainMailAddressError")
//                                    .set("value", this.getContact().getEmail())
//                                    .handle();
//                }
//            }
//       }
       // check the CompanyAccountingData
       String invoiceMedium = this.getCompanyAccountingData().getInvoiceMedium();
       if(invoiceMedium == null) {
           throw Exceptions.createHandled()
                           .withNLSKey("Company.invoiceMediumMissing").handle();
       }
       boolean error = true;
       for(int i=0; i< CompanyAccountingData.INVOICEMEDIUMNAMES.length; i++) {
          if(invoiceMedium.equals(CompanyAccountingData.INVOICEMEDIUMNAMES[i])) {
              error = false;
              break;
          }
       }
       if(error) {
           throw Exceptions.createHandled().withNLSKey("Company.invalidInvoiceMedium")
                           .set("invoiceMedium", invoiceMedium).handle();
       }
       if(invoiceMedium.equals("MAIL")) {
           String mailAddress = this.getCompanyAccountingData().getInvoiceMailAdr();
           if(Strings.isEmpty(mailAddress)) {
               throw Exceptions.createHandled()
                               .withNLSKey("Company.invoiceMailAdrMissing").handle();
           }
           if(!(mails.isValidMailAddress(mailAddress, null))) {
               throw Exceptions.createHandled()
                               .withNLSKey("Company.invalidInvoiceEmail")
                               .set("value", mailAddress).handle();
           }
       }

    }

//
//    // the followig methods are called by JSF
//
//    public boolean isForeignCountry() {
//        String countryCode = this.getAddress().getCountry();
//        return !("DE".equals(countryCode.toUpperCase()));
//    }
//
    public List<Person> queryPersons() {
        List<Person> personList = new ArrayList<Person>();
        personList.addAll(oma.select(Person.class)
                             .eq(Person.COMPANY, this)
                             .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                             .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME))
                             .queryList());
        return personList;
    }

    public String getUniquePath() {
        //TODO unify...
        return com.google.common.base.Strings.padStart(Long.toString(id, 36), 6, '0');
    }

    public String getCountryName() {
        String countryCode = this.getAddress().getCountry();
        Tuple<String, String> tuple = cls.getValues(COUNTRY_CODELIST, countryCode);
        return tuple.getFirst();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public InternationalAddressData getAddress() {
        return address;
    }

    public String getMatchcode() {
        return matchcode;
    }

    public void setMatchcode(String matchcode) {
        this.matchcode = matchcode;
    }

    public InternationalAddressData getPostboxAddress() {
        return postboxAddress;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    public ContactData getContact() {
        return contact;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public CompanyAccountingData getCompanyAccountingData() {
        return companyAccountingData;
    }
}
