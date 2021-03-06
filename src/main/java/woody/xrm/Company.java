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
import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.nls.NLS;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relateable;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

/**
 * Created by aha on 06.10.15.
 */
public class Company extends SQLTenantAware implements HasComments, HasRelations {

    public static final String COUNTRY_CODELIST = "country";

    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Autoloaded
    @Length(255)
    private String name;

    public static final Mapping NAME2 = Mapping.named("name2");
    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String name2;

    public static final Mapping CUSTOMER_NUMBER = Mapping.named("customerNumber");
    @NullAllowed
    @Length(50)
    @Trim
    @Autoloaded
    private String customerNumber;

    public static final Mapping WEBSITE = Mapping.named("website");
    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String website;

    public static final Mapping MATCHCODE = Mapping.named("matchcode");
    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String matchcode;

    public static final Mapping IMAGE = Mapping.named("image");
    @NullAllowed
    @Autoloaded
    @Length(255)
    @Trim
    private String image;

    public static final Mapping ADDRESS = Mapping.named("address");
    private final InternationalAddressData address =
            new InternationalAddressData(AddressData.Requirements.NOT_PARTIAL, null);

    public static final Mapping POSTBOX_ADDRESS = Mapping.named("postboxAddress");
    private final InternationalAddressData postboxAddress =
            new InternationalAddressData(InternationalAddressData.Requirements.NOT_PARTIAL,
                                         NLS.get("Company.postboxAddress"));

    public static final Mapping CONTACT = Mapping.named("contact");
    private final ContactData contact = new ContactData(true);

    public static final Mapping TAGS = Mapping.named("tags");
    private final Tagged tags = new Tagged(this);

    public static final Mapping COMMENTS = Mapping.named("comments");
    private final Commented comments = new Commented(this);

    public static final Mapping RELATIONS = Mapping.named("relations");
    private final Relations relations = new Relations(this);

    public static final Mapping RELATEABLE = Mapping.named("relateable");
    private final Relateable relateable = new Relateable(this);

    @Override
    public String toString() {
        if (!isNew()) {
            return getName();
        } else {
            return super.toString();
        }
    }

    @Override
    public String getTargetsString() {
        return "XRM-COMPANY";
    }

    //
//    @Part
//    private static Mails mails;
//
//   @BeforeSave
//    protected void onSave() {
//       // check te customerNR - if present
//       if(customerNr != null) {
//           Integer nr = Integer.parseInt(customerNr);
//           if(nr < MIN_CUSTOMERNR || nr > MAX_CUSTOMERNR) {
//               throw Exceptions.createHandled().withNLSKey("Company.customerNrOutOfInterval")
//                     .set("customerNr", customerNr).set("min", MIN_CUSTOMERNR).set("max", MAX_CUSTOMERNR).handle();
//           }
//       }
//        // check the presence of a customer-number if contracts are existing
//        long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
//        if (count > 0 && Strings.isEmpty(customerNr)) {
//            throw Exceptions.createHandled()
//                            .withNLSKey("Company.ContractsArePresent.CustomerNrIsMissing")
//                            .handle();
//        }
//        // normalize the mainPhoneNr
//       String phoneNumber = this.getMainPhoneNr();
//        if(phoneNumber != null && Strings.isFilled(phoneNumber)) {
//            this.setMainPhoneNr(SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(phoneNumber, true));
//        }
//        //check the mainMailAddress
//        if(Strings.isFilled(this.getMainMailAddress())) {
//            // mimimal: a.b
//            if (this.getMainMailAddress().length() < 3) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Model.mainMailAddressToShort")
//                                .set("value", this.getMainMailAddress())
//                                .handle();
//            } else {
//                // cut out the @   aa@bbbbb.cc ---> bbbbb.cc
//                int pos = this.getMainMailAddress().indexOf("@");
//                if (pos > -1) {
//                     this.setMainMailAddress(this.getMainMailAddress().substring(pos + 1));
//                }
//                // check the presence of the "."
//                pos = this.getMainMailAddress().indexOf(".");
//                // missing   or  at the last index
//                if(pos == -1 || pos == this.getMainMailAddress().length() - 1) {
//                    throw Exceptions.createHandled()
//                                    .withNLSKey("Model.mainMailAddressError")
//                                    .set("value", this.getMainMailAddress())
//                                    .handle();
//
//                }
//            }
//        }
//       // check the CompanyAccountingData
//       String invoiceMedium = this.getCompanyAccountingData().getInvoiceMedium();
//       if(invoiceMedium == null) {
//           throw Exceptions.createHandled()
//                           .withNLSKey("Company.invoiceMediumMissing").handle();
//       }
//       boolean error = true;
//       for(int i=0; i< CompanyAccountingData.INVOICEMEDIUMNAMES.length; i++) {
//          if(invoiceMedium.equals(CompanyAccountingData.INVOICEMEDIUMNAMES[i])) {
//              error = false;
//              break;
//          }
//       }
//       if(error) {
//           throw Exceptions.createHandled().withNLSKey("Company.invalidInvoiceMedium")
//                           .set("invoiceMedium", invoiceMedium).handle();
//       }
//       if(invoiceMedium.equals("MAIL")) {
//           String mailAddress = this.getCompanyAccountingData().getInvoiceMailAdr();
//           if(Strings.isEmpty(mailAddress)) {
//               throw Exceptions.createHandled()
//                               .withNLSKey("Company.invoiceMailAdrMissing").handle();
//           }
//           if(!(mails.isValidMailAddress(mailAddress, null))) {
//               throw Exceptions.createHandled()
//                               .withNLSKey("Company.invalidInvoiceEmail")
//                               .set("value", mailAddress).handle();
//           }
//       }
//
//    }
//
//    // the followig methods are called by JSF
//
//    public boolean isForeignCountry() {
//        String countryCode = this.getAddress().getCountry();
//        return !("DE".equals(countryCode.toUpperCase()));
//    }
//
//    public List<Person> queryPersons() {
//        List<Person> personList = new ArrayList<Person>();
//        personList.addAll(
//         oma.select(Person.class).eq(Person.COMPANY, this)
//                  .orderAsc(Person.PERSON.inner(PersonData.LASTNAME))
//                  .orderAsc(Person.PERSON.inner(PersonData.FIRSTNAME))
//                  .queryList());
//        return personList;
//=======
    public String getUniquePath() {
        //TODO unify...
        return com.google.common.base.Strings.padStart(Long.toString(id, 36), 6, '0');
    }

    @BeforeSave
    protected void onSave() {
//        // check the presence of a customer-number if contracts are existing
//        long count = oma.select(Contract.class).eq(Contract.COMPANY, this).count();
//        if (count > 0 && Strings.isEmpty(customerNr)) {
//            throw Exceptions.createHandled().withNLSKey("Company.ContractsArePresent.CustomerNrIsMissing").handle();
//        }
//        // normalize the mainPhoneNr
//        if (Strings.isFilled(this.getMainPhoneNr())) {
//            this.setMainPhoneNr(normalizePhoneNumber(this.getMainPhoneNr()));
//        }
//        //check the mainMailAddress
//        if (Strings.isFilled(this.getMainMailAddress())) {
//            // mimimal: a.b
//            if (this.getMainMailAddress().length() < 3) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Model.mainMailAddressToShort")
//                                .set("value", this.getMainMailAddress())
//                                .handle();
//            } else {
//                // cut out the @   aa@bbbbb.cc ---> bbbbb.cc
//                int pos = this.getMainMailAddress().indexOf("@");
//                if (pos > -1) {
//                    this.setMainMailAddress(this.getMainMailAddress().substring(pos + 1));
//                }
//                // check the presence of the "."
//                pos = this.getMainMailAddress().indexOf(".");
//                // missing   or  at the last index
//                if (pos == -1 || pos == this.getMainMailAddress().length() - 1) {
//                    throw Exceptions.createHandled()
//                                    .withNLSKey("Model.mainMailAddressError")
//                                    .set("value", this.getMainMailAddress())
//                                    .handle();
//                }
//            }
//        }
//
//        // check the CompanyAccountingData
//        String invoiceMedium = this.getCompanyAccountingData().getInvoiceMedium();
//        if (invoiceMedium == null) {
//            throw Exceptions.createHandled().withNLSKey("Company.invoiceMediumMissing").handle();
//        }
//        if (invoiceMedium.equals("MAIL")) {
//            String mailAddress = this.getCompanyAccountingData().getInvoiceMailAdr();
//            if (Strings.isEmpty(mailAddress)) {
//                throw Exceptions.createHandled().withNLSKey("Company.invoiceMailAdrMissing").handle();
//            }
//            if (!(mails.isValidMailAddress(mailAddress, null))) {
//                throw Exceptions.createHandled()
//                                .withNLSKey("Company.invalidInvoiceEmail")
//                                .set("value", mailAddress)
//                                .handle();
//            }
//        }
    }

    @Part
    private static CodeLists<?, ?, ?> cls;

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
}
