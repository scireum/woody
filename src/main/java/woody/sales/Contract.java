/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import sirius.biz.model.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.OMA;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gerhardhaufler on 10.02.16.
 */
public class Contract extends BizEntity {

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    @Trim
    @Length(50)
    @Autoloaded
    private String accountingGroup;
    public static final Column ACCOUNTINGGROUP = Column.named("accountingGroup");

    private final EntityRef<Person> contractPartner = EntityRef.on(Person.class, EntityRef.OnDelete.CASCADE);
    public static final Column CONTRACTPARTNER = Column.named("contractPartner");

    @Autoloaded
    private LocalDate signingDate;
    public static final Column SIGNINGDATE = Column.named("signingDate");

    private final EntityRef<PackageDefinition> packageDefinition =
            EntityRef.on(PackageDefinition.class, EntityRef.OnDelete.CASCADE);
    public static final Column PACKAGEDEFINITION = Column.named("packageDefinition");

    @NullAllowed
    @Autoloaded
    private Integer quantity = null;
    public static final Column QUANTITY = Column.named("quantity");

    @Autoloaded
    private LocalDate startDate;
    public static final Column STARTDATE = Column.named("startDate");

    @NullAllowed
    @Autoloaded
    private LocalDate endDate;
    public static final Column ENDDATE = Column.named("endDate");

    @NullAllowed
    @Length(255)
    @Autoloaded
    private String posLine;
    public static final Column POS_LINE = Column.named("posLine");

    /* this is the contractSinglePrice */
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount singlePrice = null;
    public static final Column SINGLEPRICE = Column.named("singlePrice");

    //    @Filter(position = 10)
    @Autoloaded
    private ContractSinglePriceType singlePriceState = ContractSinglePriceType.NO_SINGLEPRICE;
    public static final Column SINGLEPRICESTATE = Column.named("singlePriceState");

    /* this is the contractUnitPrice */
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    @Autoloaded
    private Amount unitPrice = null;
    public static final Column UNITPRICE = Column.named("unitPrice");

    /*
     * with the position-value you can control the order of the lineitems in the
     * invoice
     */
    @Autoloaded
    private int position = 0;
    public static final Column POSITION = Column.named("position");

    @Autoloaded
    @NullAllowed
    @Length(1500)
    private String parameter;
    public static final Column PARAMETER = Column.named("parameter");

    @Autoloaded
    @NullAllowed
    @Length(1500)
    private String comments;
    public static final Column COMMENTS = Column.named("comments");

    private boolean noAccounting = false;
    public static final Column NOACCOUNTING = Column.named("noAccounting");
    /**
     * the accountingInterval is a enumeration
     *
     * @see AccountingIntervalType
     */
    @Autoloaded
    private AccountingIntervalType accountingInterval;
    public static final Column ACCOUNTINGINTERVAL = Column.named("accountingInterval");

    @Autoloaded
    @NullAllowed
    private LocalDate accountedTo;
    public static final Column ACCOUNTEDTO = Column.named("accountedTo");

    // the discount is written as percent-value: 7,5% --> 7.5
    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount discountPercent;
    public static final Column DISCOUNTPERCENT = Column.named("discountPercent");

    // this is a absoluteDiscount, eg. price = 100, absoluteDiscount = 15
    // end-price = 100 - 15 = 85
    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount discountAbsolute;
    public static final Column DISCOUNTABSOLUTE = Column.named("discountAbsolute");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompany().getValue().getName());
        sb.append(":");
        String LEER = "leer";
        String name = LEER;
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

    @BeforeSave
    protected void onSave() {
 //ToDo: wieder raus
        String s = "Wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww1111wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww";
        String a = BaseEncoding.base64().encode(Hashing.md5().hashString(s, Charsets.UTF_8).asBytes());
        byte[] b = Hashing.md5().hashString(s, Charsets.UTF_8).asBytes();

        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(Character.forDigit((b[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(b[i] & 0x0f, 16));
        }
        String c1 = sb.toString();


        //completeParameter(this);
        checkParameterSyntax(this.getParameter());
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
        if (getUnitPrice() == null) {
            PackageDefinition packageDefinition = oma.select(PackageDefinition.class)
                 .eq(PackageDefinition.ID, this.getPackageDefinition().getValue().getId()).queryFirst();
            if (packageDefinition != null) {
                if (packageDefinition.getUnitPrice().isFilled()) {
                    this.setUnitPrice(packageDefinition.getUnitPrice());
                }
            }
        }

        // check the singlePrice. If the singlePrice is null, fetch the singlePrice from the packetDefinition
        if (getSinglePrice() == null) {
            PackageDefinition packageDefinition = oma.select(PackageDefinition.class)
                       .eq(PackageDefinition.ID, this.getPackageDefinition().getValue().getId()).queryFirst();
            if (packageDefinition != null) {
                if (packageDefinition.getSinglePrice() != null)  {
                    this.setSinglePrice(packageDefinition.getSinglePrice());
                }
            }
        }

        //check the singlePriceState
        asb.checkContractSinglePriceState(this);

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
        // ToDo:Testen
        if(isNew()) {
            Product product = packageDefinition.getValue().getProduct().getValue();
            List<Contract> contractList = oma.select(Contract.class)
                                             .eq(Contract.COMPANY, company)
                                             .eq(Contract.ACCOUNTINGGROUP, accountingGroup)
                                             .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.PRODUCT), product)
                                             .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.ACCOUNTINGPROCEDURE),
                                                 PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL)
                                             .orderAsc(Contract.STARTDATE)
                                             .queryList();

            if (isNew()) {
                contractList.add(this);
            } else {
                List<Contract> contractList2 = new ArrayList<Contract>();
                for (Contract c : contractList) {
                    contractList2.add(c);
                }
                for (Contract c : contractList2) {
                    if (c.getId() != this.getId()) {
                        contractList.add(c);
                    } else {
                        contractList.add(this);
                    }
                }
            }

            checkContractIsSingle(contractList);

        }
    }


    private void checkContractIsSingle(List<Contract> contractList) {
        if(contractList.size() == 1)     {return;}
        for (int i=1; i<contractList.size(); i++) {
            Contract c1 = contractList.get(i);
            Contract c0 = contractList.get(i-1);
            if(c0.getEndDate() == null) {
                throw Exceptions.createHandled().withNLSKey("Contract.endDateMissing").set("contract", c0.toString()).handle();
            }
            if(c1.getStartDate().isBefore(c0.getEndDate())) {
                throw Exceptions.createHandled().withNLSKey("Contract.startDateMissing")
                                .set("contract", c1.toString())
                                .set("oldContract", c0.toString()).handle();
            }
        }
    }

    /**
     * checks the syntax of the given parameters
     */
    private void checkParameterSyntax(String text) {
        if (Strings.isFilled(text)) {
            text = text.replaceAll("\\n", "");
            if (!text.endsWith(" ")) {
                text = text + " ";
            }
            String regex = "((\\w)*=(\\d)*\\s)*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (!matcher.matches()) {
                throw Exceptions.createHandled()
                                .withNLSKey("woody.xrm.Contract.ParameterSyntaxError")
                                .set("text", text)
                                .handle();
            }
        }
        return;
    }

    /**
     * checks the customerNr
     */
    private void checkCustomerNr(Company company) {
        String customerNr = company.getCustomerNr();
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

    public EntityRef<Company> getCompany() {
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

    public EntityRef<Person> getContractPartner() {
        return contractPartner;
    }

    public EntityRef<PackageDefinition> getPackageDefinition() {
        return packageDefinition;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
