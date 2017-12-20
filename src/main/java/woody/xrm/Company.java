/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.codelists.CodeLists;
import sirius.biz.model.InternationalAddressData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.mails.Mails;
import woody.core.comments.Commented;
import woody.core.mails.Mailed;
import woody.core.tags.Tagged;
import woody.phoneCalls.SyncAsterisk;
import woody.sales.CompanyAccountingData;
import woody.sales.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aha on 06.10.15.
 */
public class Company extends TenantAware {

    public static final int MIN_CUSTOMERNR = 10001;
    public static final int MAX_CUSTOMERNR = 19999;
    public static final String COUNTRY_CODELIST = "country";

    @Trim
    @Autoloaded
    @Unique(within = "tenant")
    @Length(255)
    private String name;
    public static final Column NAME = Column.named("name");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String name2;
    public static final Column NAME2 = Column.named("name2");

    /* the customerNr is set by the sequence.generateId */
    @NullAllowed
    @Length(50)
    private String customerNr;
    public static final Column CUSTOMERNR = Column.named("customerNr");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String homepage;
    public static final Column HOMEPAGE = Column.named("homepage");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String matchcode;
    public static final Column MATCHCODE = Column.named("matchcode");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String image;
    public static final Column IMAGE = Column.named("image");

    private final InternationalAddressData address = new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL, null);
    public static final Column ADDRESS = Column.named("address");

    @NullAllowed
    @Autoloaded
    private final InternationalAddressData postboxAddress =
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL, NLS.get("Company.postboxAddress"));
    public static final Column POSTBOXADDRESS = Column.named("postboxAddress");

    @NullAllowed
    @Autoloaded
    private final CompanyAccountingData companyAccountingData =  new CompanyAccountingData();
    public static final Column COMPANYACCOUNTINGDATA = Column.named("companyAccountingData");

    @NullAllowed
    @Autoloaded
    @Length(50)
    private String mainPhoneNr;
    public static final Column MAINPHONENR = Column.named("mainPhoneNr");

    @NullAllowed
    @Autoloaded
    @Length(50)
    private String mainMailAddress;
    public static final Column MAINMAILADDRESS = Column.named("mainMailAddress");

    private final Tagged tags = new Tagged(this);
    public static final Column TAGS = Column.named("tags");

    private final Commented comments = new Commented(this);
    public static final Column COMMENTS = Column.named("comments");

    private final Mailed mailed = new Mailed(this);
    public static final Column MAILED = Column.named("mailed");

    @Override
    public String toString() {
        if (!isNew()) {
            return getName();
        } else {
            return super.toString();
        }
    }


    @Part
    private static Mails mails;

   @BeforeSave
    protected void onSave() {
       // check te customerNR - if present
       if(customerNr != null) {
           Integer nr = Integer.parseInt(customerNr);
           if(nr < MIN_CUSTOMERNR || nr > MAX_CUSTOMERNR) {
               throw Exceptions.createHandled().withNLSKey("Company.customerNrOutOfInterval")
                     .set("customerNr", customerNr).set("min", MIN_CUSTOMERNR).set("max", MAX_CUSTOMERNR).handle();
           }
       }
        // check the presence of a customer-number if contracts are existing
        long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
        if (count > 0 && Strings.isEmpty(customerNr)) {
            throw Exceptions.createHandled()
                            .withNLSKey("Company.ContractsArePresent.CustomerNrIsMissing")
                            .handle();
        }
        // normalize the mainPhoneNr
       String phoneNumber = this.getMainPhoneNr();
        if(phoneNumber != null && Strings.isFilled(phoneNumber)) {
            this.setMainPhoneNr(SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(phoneNumber, true));
        }
        //check the mainMailAddress
        if(Strings.isFilled(this.getMainMailAddress())) {
            // mimimal: a.b
            if (this.getMainMailAddress().length() < 3) {
                throw Exceptions.createHandled()
                                .withNLSKey("Model.mainMailAddressToShort")
                                .set("value", this.getMainMailAddress())
                                .handle();
            } else {
                // cut out the @   aa@bbbbb.cc ---> bbbbb.cc
                int pos = this.getMainMailAddress().indexOf("@");
                if (pos > -1) {
                     this.setMainMailAddress(this.getMainMailAddress().substring(pos + 1));
                }
                // check the presence of the "."
                pos = this.getMainMailAddress().indexOf(".");
                // missing   or  at the last index
                if(pos == -1 || pos == this.getMainMailAddress().length() - 1) {
                    throw Exceptions.createHandled()
                                    .withNLSKey("Model.mainMailAddressError")
                                    .set("value", this.getMainMailAddress())
                                    .handle();

                }
            }
        }
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

    // the followig methods are called by JSF

    public boolean isForeignCountry() {
        String countryCode = this.getAddress().getCountry();
        return !("DE".equals(countryCode.toUpperCase()));
    }

    public List<Person> queryPersons() {
        List<Person> personList = new ArrayList<Person>();
        personList.addAll(
         oma.select(Person.class).eq(Person.COMPANY, this)
                  .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
                  .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME))
                  .queryList());
        return personList;
    }

    @Part
    private static CodeLists cls;

    public String getCountryName() {
        String countryCode = this.getAddress().getCountry();
        Tuple<String, String>  tuple = cls.getValues(COUNTRY_CODELIST, countryCode);
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

    public String getCustomerNr() {
        return customerNr;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = customerNr;
    }

    public InternationalAddressData getAddress() {
        return address;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
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

    public Commented getComments() {
        return comments;
    }

    public String getMainPhoneNr() {
        return mainPhoneNr;
    }

    public void setMainPhoneNr(String mainPhoneNr) {
        this.mainPhoneNr = mainPhoneNr;
    }

    public String getMainMailAddress() {
        return mainMailAddress;
    }

    public void setMainMailAddress(String mainMailAddress) {
        this.mainMailAddress = mainMailAddress;
    }

    public CompanyAccountingData getCompanyAccountingData() {
        return companyAccountingData;
    }

    public Mailed getMailed() {
        return mailed;
    }

}
