/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.jdbc.model.BizEntity;
import sirius.biz.jdbc.tenants.Tenants;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.OMA;
import sirius.db.mixing.Mapping;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;

import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gerhardhaufler on 10.02.16.
 */
public class Contract extends BizEntity {

    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping COMPANY = Mapping.named("company");

    public static final Mapping ACCOUNTINGGROUP = Mapping.named("accountingGroup");
    @Trim
    @Length(50)
    @Autoloaded
    private String accountingGroup;

    @Autoloaded
    private final SQLEntityRef<Person> contractPartner = SQLEntityRef.on(Person.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping CONTRACTPARTNER = Mapping.named("contractPartner");

    @Autoloaded
    private LocalDate signingDate;
    public static final Mapping SIGNINGDATE = Mapping.named("signingDate");

    @Autoloaded
    private final SQLEntityRef<PackageDefinition> packageDefinition =
            SQLEntityRef.on(PackageDefinition.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping PACKAGEDEFINITION = Mapping.named("packageDefinition");

    @NullAllowed
    @Autoloaded
    private Integer quantity = null;
    public static final Mapping QUANTITY = Mapping.named("quantity");

    @Autoloaded
    private LocalDate startDate;
    public static final Mapping STARTDATE = Mapping.named("startDate");

    @NullAllowed
    @Autoloaded
    private LocalDate endDate;
    public static final Mapping ENDDATE = Mapping.named("endDate");

    @NullAllowed
    @Length(255)
    @Autoloaded
    private String posLine;
    public static final Mapping POS_LINE = Mapping.named("posLine");

    /* this is the contractSinglePrice */
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount singlePrice = Amount.NOTHING;
    public static final Mapping SINGLEPRICE = Mapping.named("singlePrice");

    //    @Filter(position = 10)
    @Autoloaded
    private ContractSinglePriceType singlePriceState = ContractSinglePriceType.NO_SINGLEPRICE;
    public static final Mapping SINGLEPRICESTATE = Mapping.named("singlePriceState");

    /* this is the contractUnitPrice */
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount unitPrice = Amount.NOTHING;;
    public static final Mapping UNITPRICE = Mapping.named("unitPrice");

    /*
     * with the position-value you can control the order of the lineitems in the
     * invoice
     */
    @Autoloaded
    private int position = 0;
    public static final Mapping POSITION = Mapping.named("position");

    @Autoloaded
    @NullAllowed
    @Length(1500)
    private String parameter;
    public static final Mapping PARAMETER = Mapping.named("parameter");

    @Autoloaded
    @NullAllowed
    @Length(1500)
    private String comments;
    public static final Mapping COMMENTS = Mapping.named("comments");

    private boolean noAccounting = false;
    public static final Mapping NOACCOUNTING = Mapping.named("noAccounting");
    /**
     * the accountingInterval is a enumeration
     *
     * @see AccountingIntervalType
     */
    @Autoloaded
    private AccountingIntervalType accountingInterval;
    public static final Mapping ACCOUNTINGINTERVAL = Mapping.named("accountingInterval");

    @Autoloaded
    @NullAllowed
    private LocalDate accountedTo;
    public static final Mapping ACCOUNTEDTO = Mapping.named("accountedTo");

    // the discount is written as percent-value: 7,5% --> 7.5
    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    private Amount discountPercent = Amount.NOTHING;
    public static final Mapping DISCOUNTPERCENT = Mapping.named("discountPercent");

    // this is a absoluteDiscount, eg. price = 100, absoluteDiscount = 15
    // end-price = 100 - 15 = 85
    @Autoloaded
    @Numeric(scale = 3, precision = 15)
    private Amount discountAbsolute = Amount.NOTHING;
    public static final Mapping DISCOUNTABSOLUTE = Mapping.named("discountAbsolute");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompany().getValue().getName());
        sb.append(":");
        String LEER = "leer";
        String name = LEER;
        if(this.getId() == -1) {
            sb.append("empty_contract");
            return sb.toString();
        }
        if (getPackageDefinition() != null) {
            if (getPackageDefinition().getValue().getProduct() != null) {
                name = getPackageDefinition().getValue().getProduct().getValue().getName();
            }
        }
        sb.append(name);
        sb.append("-");
        String packageName = LEER;
        if (getPackageDefinition() != null) {
            packageName = getPackageDefinition().getValue().getName();
        }
        sb.append(packageName);
        sb.append(" ");
        sb.append("(");
        sb.append(NLS.toUserString(getStartDate()));
        sb.append("-");
        sb.append(NLS.toUserString(getAccountedTo() == null ? LEER : getAccountedTo()));
        sb.append("-");
        sb.append(NLS.toUserString(getEndDate() == null ? LEER : getEndDate()));
        sb.append(")");
        return sb.toString();
    }

    @Part
    private static OMA oma;

    @Part
    private static AccountingService asb;

    @Part
    private static ServiceAccountingService sas;

    @BeforeSave
    protected void onSave() {
        // Korrektur aus CRM-89
        if(!this.getCompany().isFilled()) {
            if(this.getContractPartner().getValue() != null)  {
                if(this.getContractPartner().getValue().getCompany().getValue() != null) {
                    this.getCompany().setValue(this.getContractPartner().getValue().getCompany().getValue());
                }
            }
        }

        if(Strings.isEmpty((quantity))) {
            quantity = 1;
        }

        // check the parameter-syntax
      //  asb.checkParameterSyntax(this.getParameter());

        // check the customerNr of the company, because the customerNr is needed to account the contract
        checkCustomerNr(getCompany().getValue());

        // check the dates of start, end and accountedTo, in every date the day shold be "1"
        if (getStartDate() != null && getStartDate().getDayOfMonth() != 1) {
            throw Exceptions.createHandled().withNLSKey("woody.xrm.Contract.doesNotStartOnFirstOfMonth").handle();
        }
        if (getEndDate() != null && getEndDate().getDayOfMonth() != 1) {
            throw Exceptions.createHandled().withNLSKey("woody.xrm.Contract.doesNotEndOnFirstOfMonth").handle();
        }
        if (getAccountedTo() != null && getAccountedTo().getDayOfMonth() != 1) {
            throw Exceptions.createHandled().withNLSKey("woody.xrm.Contract.isNotAccountedToFirstOfMonth").handle();
        }

        // check the unitPrice. If the unitPrice is null, fetch the unitPrice from the packetDefinition
        if (getUnitPrice().isEmpty()) {
            PackageDefinition packageDefinition = oma.select(PackageDefinition.class)
                      .eq(PackageDefinition.ID, this.getPackageDefinition().getValue().getId()).queryFirst();
            if (packageDefinition != null) {
                if (packageDefinition.getUnitPrice().isPositive()) {
                    this.setUnitPrice(packageDefinition.getUnitPrice());
                }
            }
        }

        // check the singlePrice. If the singlePrice is null, fetch the singlePrice from the packetDefinition
        if (getSinglePrice().isEmpty()) {
            PackageDefinition packageDefinition = oma.select(PackageDefinition.class)
                       .eq(PackageDefinition.ID, this.getPackageDefinition().getValue().getId()).queryFirst();
            if (packageDefinition != null) {
                if (packageDefinition.getSinglePrice().isPositive() )  {
                    this.setSinglePrice(packageDefinition.getSinglePrice());
                    if(packageDefinition.getSinglePrice().isPositive()) {
                        this.setSinglePriceState(ContractSinglePriceType.ACCOUNT_NOW);
                    } else {
                        this.setSinglePriceState(ContractSinglePriceType.NO_SINGLEPRICE);
                        this.setSinglePrice(Amount.NOTHING);
                    }
                }
            }
        }

        //check the singlePriceState
    //    asb.checkContractSinglePriceState(this);

        // check the start and end-date in relation to now. A warning is generated if the duration is > 180 days
        if (getStartDate() != null && (LocalDate.now().minusDays(180).isAfter(getStartDate()) || LocalDate.now()
                   .minusDays(180).isBefore(getStartDate()))) {

            //  ToDo Warnung ausgeben  Datum liegt 1/2 Jahr in der Vergangenheit oder Zukunft.

        }
        if (getEndDate() != null && (LocalDate.now().minusDays(180).isAfter(getEndDate()) || LocalDate.now()
                  .minusDays(180).isBefore(getEndDate()))) {

            //  ToDo Warnung ausgeben  Datum liegt 1/2 Jahr in der Vergangenheit oder Zukunft.

        }

        // set the position to a defaultPosition from the PackageDefinition
        int position = this.getPosition();
        if (position == 0) {
            PackageDefinition pd = getPackageDefinition().getValue();
            if (pd.getDefaultPosition() > 0) {
                this.setPosition(pd.getDefaultPosition());
            }
        }

        if (!isNew()) {
            // check whether the packageDefinition has changed --> not so good!
            if (getDescriptor().isChanged(this, getDescriptor().getProperty(PACKAGEDEFINITION))) {
                int ggg = 1;
                throw Exceptions.createHandled().withNLSKey("woody.xrm.Contract.noPackageDefinitionChange").handle();
            }
        }

        // check the values (Interval-test)
        sas.checkValue(Amount.of(quantity), true, false, false, false, null, NLS.get("Contract.quantity"));
        sas.checkValue(unitPrice, true, false, false, false, null, NLS.get("Contract.unitPrice"));
        sas.checkValue(singlePrice, true, false, false, false, null, NLS.get("Contract.singlePrice"));
        sas.checkValue(discountPercent, true, false, false, true, Amount.of(100), NLS.get("Contract.discountPercent"));
        sas.checkValue(discountAbsolute, true, false, false, false, null, NLS.get("Contract.discountAbsolute"));

        // CRM-7: Wenn ein unitPrice 0 vorgegeben wird und in der Packagedefinition der unitPrice <> 0 ist muss
        // noAccounting auf true stehen.
        if(getPackageDefinition() != null) {
            PackageDefinition pd = getPackageDefinition().getValue();
            if(pd.getUnitPrice().isFilled()) {
                if(pd.getUnitPrice().isPositive()) {
                    if(getUnitPrice().isZero() ) {
                        if(!isNoAccounting()) {
                            throw Exceptions.createHandled().withNLSKey("Contract.unitPriceWrong")
                                            .set("contract", this.toString())
                                            .set("unitPrice1", NLS.toUserString(getUnitPrice()))
                                            .set("unitPrice2", NLS.toUserString(pd.getUnitPrice())).handle();

                        }
                    }
                }
                // CRM-7: Wenn der unitPrice im Contract <> unitPrice in der Packagedefinition ist, sollte eine Warnung ausgegeben
                // werden. ACHTUNG Abweichender Preis.
                if(getUnitPrice().isFilled()) {
                    if( ! getUnitPrice().equals(pd.getUnitPrice())) {   // CRM-52 != durch !.equals ersetzt
                        // ToDo Warnmeldung ausgeben.
//                        ApplicationController.addInfoMessage(MessageFormat.format(
//                                "Warnung: Der Preis im Vertrag = {0} EUR entspricht nicht dem Preis im Paket = {1} EUR.",
//                                NLS.toUserString(getUnitPrice()), NLS.toUserString(pd.getUnitPrice())));
                    }
                }
            }
        }
        /**
         * check the contracts of the same product, accountingGroup and accountingProcedure.
         * a contract in the past should have a endDate, the startDate of the following contract should be greater
         * or eqal as the endDate.
         * error: ...... contract 0 ... endDate=null,       1.01.2017 ... contract 1 ...
         * error: ...... contract 0 ... endDate=1.01.2017,  1.11.2016 ... contract 1 ...
         * o.k.:  ...... contract 0 ... endDate=1.11.2016,  1.01.2017 ... contract 1 ...
         */
        // get a list of all contracts withe the same product, accountingGroup and accountingProcedure
        Product product = packageDefinition.getValue().getProduct().getValue();
        List<Contract> contractList = oma.select(Contract.class)
                                         .eq(Contract.COMPANY, company)
                                         .eq(Contract.ACCOUNTINGGROUP, accountingGroup)
                                         .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.PRODUCT), product)
                                         .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.ACCOUNTINGPROCEDURE),
                                             PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL)
                                         .orderAsc(Contract.STARTDATE).queryList();
        if (isNew()) {
            // add a new contract to the list
            contractList.add(this);
        } else {
            // change the saved contract in this list
            HashMap<Integer, Contract> contractMap = new HashMap<Integer, Contract>();
            int listSize = contractList.size();
            for(int i = 0; i<listSize; i++) {
                Contract contract = contractList.get(i);
                if(contract.getId() == this.getId()) {
                  contractMap.put(i, this);
                } else {
                    contractMap.put(i, contract);
                }
            }
            contractList.clear();
            for(int i = 0; i<listSize; i++) {
                Contract contract = contractMap.get(i);
                contractList.add(contract);
            }
        }
        // check the contractList
        checkContractIsSingle(contractList);
    }

    /**
     * check the contracts in the given list
     * a contract in the past should have a endDate, the startDate of the following contract should be greater
     * or eqal as the endDate.
     */
    private void checkContractIsSingle(List<Contract> contractList) {
        if(contractList.size() == 1)     {return;}
        for (int i=1; i<contractList.size(); i++) {
            Contract c1 = contractList.get(i);
            Contract c0 = contractList.get(i-1);
            if(c0.getEndDate() == null) {
                // ToDo auf warning umstellen
                throw Exceptions.createHandled().withNLSKey("Contract.endDateMissing").set("contract0", c0.toString())
                                .set("contract1", c1.toString()).set("abrGrp", c1.getAccountingGroup()).handle();
            }

            if(c1.getStartDate().isBefore(c0.getEndDate())) {
                // ToDo auf warning umstellen
                throw Exceptions.createHandled().withNLSKey("Contract.startDateMissing")
                                .set("contract", c1.toString())
                                .set("oldContract", c0.toString()).set("abrGrp", c1.getAccountingGroup()).handle();
            }
        }
    }

    /**
     * checks the customerNr
     */
    private void checkCustomerNr(Company company) {
        String customerNr = company.getCustomerNumber();
        if (Strings.isEmpty(customerNr)) {
            throw Exceptions.createHandled()
                            .withNLSKey("woody.xrm.Contract.customerNrMissing")
                            .set("value", company.getName())
                            .handle();
        }
        try {
            int i = Integer.parseInt(customerNr);

            if (i <= 0) {
                throw Exceptions.createHandled()
                                .withNLSKey("woody.xrm.Contract.customerNrLessThanZero")
                                .set("value", customerNr)
                                .handle();
            }
        } catch (NumberFormatException e) {
            throw Exceptions.createHandled()
                            .withNLSKey("woody.xrm.Contract.customerError")
                            .set("value", customerNr)
                            .handle();
        }
    }

    @Part
    private static Tenants tenants;

    public List<PackageDefinition>  getAllPackageDefinitionsDirect() {
        List<PackageDefinition> pdList = oma.select(PackageDefinition.class)
            .eq(PackageDefinition.PRODUCT.join(Product.TENANT), tenants.getRequiredTenant())
            .orderAsc(PackageDefinition.PRODUCT.join(Product.NAME))
            .orderAsc(PackageDefinition.NAME).queryList();
        return pdList;
    }

    public String getContractPartnerAsString() {
        if(this.getId() == -1) {
            return "";
        } else {
            return this.getContractPartner().getValue().getPerson().toString();
        }
    }

    public String toContractName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompany().getValue().getName());
        sb.append("_");
        String LEER = "leer";
        String name = LEER;
        if (getPackageDefinition() != null) {
            if (getPackageDefinition().getValue().getProduct() != null) {
                name = getPackageDefinition().getValue().getProduct().getValue().getName();
            }
        }
        sb.append(name);
        sb.append("_");
        String packageName = LEER;
        if (getPackageDefinition() != null) {
            packageName = getPackageDefinition().getValue().getName();
        }
        sb.append(packageName);
        sb.append(" ab ");
        sb.append(NLS.toUserString(getStartDate()));
        return sb.toString();

    }

    public boolean isPosLinePresent() {
        if(Strings.isFilled(getPosLine())) {
            return true;
        }
        return false;
    }


    public List<Person> getAllPersonsForCompany(Company company) {
        List<Person> personList =  oma.select(Person.class).eq(Person.COMPANY, company).queryList();
        return personList;
    }

    /**
     * called by javaScript
     * @param year
     * @return the distance of using the contract in this year, e.g.1.1.-31.12.year
     */
    public String getDistance(int year) {
        LocalDate endDateYear = LocalDate.of(year,12,31);
        String s = "01.01 - ";
        if(this.endDate != null && this.endDate.isBefore(endDateYear)) {
            s = s + NLS.toUserString(this.endDate);
        } else {
            s = s + "31.12." + NLS.toUserString(year);
        }
        return s;
    }

    /**
     * called by javaScript
     * @param year
     * @return  the number of months of using the contract in this year
     */
    public String getMonths(int year) {
        return NLS.toUserString(getMonthsInt(year));
    }

    /**
     * called by javaScript
     * @param year
     * @return yearValue = amount * months * ((unitPrice * (100% - discount) - discountAbsolute)
     */
    public Amount getYearValue(int year) {
        int  months = getMonthsInt(year);
        Amount unitPrice = this.getUnitPrice().fill(Amount.ZERO);
        Amount quantity = Amount.of(this.getQuantity()).fill(Amount.ONE);
        Amount discount = this.getDiscountPercent().fill(Amount.ZERO);
        Amount discountAbsolute = this.getDiscountAbsolute().fill(Amount.ZERO);
        Amount yearValue = unitPrice.decreasePercent(discount);
        yearValue = yearValue.subtract(discountAbsolute);
        yearValue = yearValue.times(quantity);
        yearValue = yearValue.times(Amount.of(months));
        return yearValue;
    }

    /**
     * @param year
     * @return the number of month of usage the contract in this year
     */
    public int getMonthsInt(int year) {
        LocalDate endDateYear = LocalDate.of(year+1,1,1);
        int months = 12;
        if(this.endDate != null) {
            if (this.endDate.isBefore(endDateYear)) {
                months = this.endDate.getMonthValue();
                if(months > 1) {
                    months = months - 1;
                }
            }
        }
        return months;
    }


    public boolean isParameterPresent() {
        return Strings.isFilled(parameter);
    }


    public SQLEntityRef<Company> getCompany() {
        return company;
    }

    public String getAccountingGroup() {
        return accountingGroup;
    }

    public void setAccountingGroup(String accountingGroup) {
        this.accountingGroup = accountingGroup;
    }

    public LocalDate getSigningDate() {
        return signingDate;
    }

    public void setSigningDate(LocalDate signingDate) {
        this.signingDate = signingDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPosLine() {
        return posLine;
    }

    public void setPosLine(String posLine) {
        this.posLine = posLine;
    }

    public ContractSinglePriceType getSinglePriceState() {
        return singlePriceState;
    }

    public void setSinglePriceState(ContractSinglePriceType singlePriceState) {
        this.singlePriceState = singlePriceState;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isNoAccounting() {
        return noAccounting;
    }

    public void setNoAccounting(boolean noAccounting) {
        this.noAccounting = noAccounting;
    }

    public LocalDate getAccountedTo() {
        return accountedTo;
    }

    public void setAccountedTo(LocalDate accountedTo) {
        this.accountedTo = accountedTo;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public AccountingIntervalType getAccountingInterval() {
        return accountingInterval;
    }

    public void setAccountingInterval(AccountingIntervalType accountingInterval) {
        this.accountingInterval = accountingInterval;
    }

    public Amount getSinglePrice() {
        return singlePrice;
    }

    public void setSinglePrice(Amount singlePrice) {
        this.singlePrice = singlePrice;
    }

    public Amount getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Amount unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Amount getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Amount discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Amount getDiscountAbsolute() {
        return discountAbsolute;
    }

    public void setDiscountAbsolute(Amount discountAbsolute) {
        this.discountAbsolute = discountAbsolute;
    }

    public SQLEntityRef<Person> getContractPartner() {
        return contractPartner;
    }

    public SQLEntityRef<PackageDefinition> getPackageDefinition() {
        return packageDefinition;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
