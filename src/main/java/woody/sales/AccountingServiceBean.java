/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.db.mixing.Constraint;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.kernel.di.std.Register;

import sirius.db.mixing.OMA;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.DataCollector;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.Formatter;
import sirius.kernel.nls.NLS;

import woody.sales.ContractToDos.Command;
import woody.xrm.Company;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by gerhardhaufler on 18.09.16.
 */

@Register(classes = AccountingService.class)
public class AccountingServiceBean implements AccountingService {

    @Part
    protected OMA oma;

    private Amount prodUmsatz = null;
    private Amount testUmsatz = null;
    private LocalDate accountingDate = null;

    private static final String LINEITEMNAME = "lineitem.csv";

    /**
     * accounts all contracts from all companies.
     * @param dryRun           true = account the contract but no update for the contract
     * @param referenceDate    Stichtag
     * @param givenCompany     null = all companies or one company
     * @param foreignCountry   true = only foreign contries are accounted
     * @return                 all lineitems from the accounting
     */
    @Override
    public DataCollector<Lineitem> accountAllContracts(boolean dryRun,LocalDate referenceDate,Company givenCompany,
    boolean foreignCountry)  {

        if (referenceDate == null) {
            throw Exceptions.createHandled()
                            .withNLSKey("AccountingServiceBean.referenceDateIsNull").handle();
        }
        LocalDateTime clearingDate = LocalDateTime.now();
        String text = MessageFormat.format(
                "Start Lizenz-Abrechnung zum ReferenzDatum {0} im Modus {1}, clearingDate: {2}",
                NLS.toUserString(referenceDate),  dryRun == true ? "Test" : "Produktiv", NLS.toUserString(clearingDate));
        System.err.println(text);

        // create a lineitem-List
        DataCollector<Lineitem> itemCollector = new DataCollector<Lineitem>() {
            @Override
            public void add(Lineitem entity) {
                if (!dryRun) {
                    oma.update(entity);
                }
                super.add(entity);
            }
        };

        prodUmsatz = null;
        testUmsatz = null;

        //create a list wit the companies
        // is a company given?
        List<Company> companyList = new ArrayList<Company>();
        if (givenCompany != null) {
            // add the given company to the companyList
            companyList.add(givenCompany);
        } else {
            // get a List of all companies
            companyList = oma.select(Company.class)
                             .orderAsc(Company.NAME).queryList();
        }

        // create a list with all products
        List<Product> productList = oma.select(Product.class).queryList();
        Long invoiceNr = getMinInvoiceNr();

        // Step1: process the companyList
        for (Company company : companyList) {
//			System.err.println("Firma: " + company.getName());
            String name = company.getName() ;
//            if (company.getName().startsWith("Testfirma14")) {
//			if("10174".equals(company.getCustomerNr())) {
//                name = name +company.getName() ;
//            }
//                if (monitor != null) {
//                    monitor.setTitle("Firma: " + company.getName(), false);
//                }
            if (foreignCountry) {
                String countryCode = company.getAddress().getCountry();
                if(Strings.isEmpty(countryCode) || "de".equals(countryCode.toLowerCase())) {
                    continue;  // throw out the german lineitems
                }
            }
            // Step2: search for all different accountingGroups of this company
            // and store them into the accGroupList
            List<Contract> contractListAll = oma
                    .select(Contract.class)
                    .eq(Contract.COMPANY, company)
                    .orderAsc(Contract.ACCOUNTINGGROUP)
                    .orderAsc(Contract.STARTDATE).queryList();
            if (contractListAll.isEmpty()) {
                continue;
            }
            // select the different accountingGroups - each one time into
            // the accGroupList
            List<String> accGroupList = new ArrayList<String>();
            String accGroup = "";
            for (Contract contract : contractListAll) {
                if (!(accGroup.equals(contract.getAccountingGroup()))) {
                    accGroup = contract.getAccountingGroup();
                    accGroupList.add(accGroup);
                }
            }

            // Step3: Process the accGroupList stepwise by the different
            // products and check rival contracts
            for (String accGrp : accGroupList) {
                Long companyInvoiceNr = 1L;
                for (Product product : productList) {
                    // select a list of all contracts with the given
                    // company, accountingGroup, product, RIVAL-contracts
                    List<Contract> contractList = oma
                            .select(Contract.class)
                            .eq(Contract.COMPANY, company)
                            .eq(Contract.ACCOUNTINGGROUP, accGrp)
                            .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.PRODUCT), product)
                            .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.ACCOUNTINGPROCEDURE),
                                PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL)
                            .orderAsc(Contract.STARTDATE).queryList();

                    // Step4: check the contractList, store possible
                    // accountings in the toDoList
                    if (!contractList.isEmpty()) {
                        List<ContractToDos<Contract, Contract, Contract, Integer>> toDoList = checkContractList(contractList, referenceDate);
                        if (toDoList.size() > 0) {
                            // Step5: process the toDoList
                            // listen! the InvoiceNr is a virtual invoiceNr for collmex.
                            // all invoiceNr are negative and a new invoiceNr is
                            // a new "smaller" negative number!
                            if (companyInvoiceNr != invoiceNr) {
                                invoiceNr--;
                                companyInvoiceNr = invoiceNr;
                            }
                            for (ContractToDos<Contract, Contract, Contract, Integer> c : toDoList) {
                                Command command = c.getCommand();
                                Contract c0 = c.getBefore();
                                Contract c1 = c.getFirst();
                                Contract c2 = c.getSecond();
                                processCommand(itemCollector, dryRun,
                                               referenceDate, command, c0, c1, c2,
                                               invoiceNr, clearingDate);
                            }
                        }
                    }

                    // Step6:look for volume-licences
                    contractList = oma.select(Contract.class)
                            .eq(Contract.COMPANY, company)
                            .eq(Contract.ACCOUNTINGGROUP, accGrp)
                            .eq(Contract.PACKAGEDEFINITION.join(PackageDefinition.PRODUCT), product)
                            .orderAsc(Contract.STARTDATE).queryList();
                    if(contractList.size() > 0) {
                        for (Contract contract : contractList) {
                            String accountingProcedure = contract
                                    .getPackageDefinition().getValue().getAccountingProcedure();
                            if (PackageDefinition.ACCOUNTINGPROCEDURE_VOLUME
                                    .equals(accountingProcedure)) {
                                Command command = analyzeVolumeContract(contract,
                                                                                      referenceDate);
                                if (companyInvoiceNr != invoiceNr) {
                                    invoiceNr--;
                                    companyInvoiceNr = invoiceNr;
                                }
                                processCommand(itemCollector, dryRun,
                                               referenceDate, command, null, contract,
                                               null, invoiceNr, clearingDate);
                            }
                        }
                    }
                }
            }
        }

        // build a activity-news
        Amount summe = Amount.ZERO;
        int counter = 0;
        System.err.println("---------------------------------------------------------") ;
        for ( Lineitem lineitem : itemCollector.getData() ) {
            counter++;
            Amount discount = lineitem.getPositionDiscount();
            if (discount == null) {
                discount = Amount.ZERO;
            }
            Amount price = lineitem.getPrice();
            price = price.decreasePercent(discount);
            Amount quantity = lineitem.getQuantity();
            Amount amount = quantity.times(price);
            summe = summe.add(amount);
            text = lineitem.getCompanyName() + ";" + lineitem.getCustomerNr() + ";" + NLS.toUserString(amount);
            text = text + ";" + lineitem.getPackageName() + ";" +  lineitem.getPositionType();
            System.err.println(text);
        }

        System.err.println("---------------------------------------------------------") ;
        if (dryRun) {
            testUmsatz = summe;
        } else {
            prodUmsatz = summe;
        }

        text = MessageFormat.format(
                "Lizenz-Abrechnung zum ReferenzDatum {0} im Modus {1} erstellt, {2} Rechnungspositionen, Netto-Umsatz: {3} EUR",
                 NLS.toUserString(referenceDate),  dryRun == true ? "Test" : "Produktiv", NLS.toUserString(counter),
                 NLS.toUserString(summe));
        System.err.println(text);
                // ToDo Meldung ausgeben ss.forBackendStream(
//            ss.forBackendStream(
//                    DisplayMarkdownFactory.FACTORY_NAME,
//                    "Lizenz-Abrechnung",  text)
//              .loginRequired(true).setUser(Users.getCurrentUser()).publish();

        accountingDate = LocalDate.now();
        return itemCollector;
    }


        @Override
        public Long exportTest() {
            Long count = oma.select(Lineitem.class).eq(Lineitem.STATUS, Lineitem.LINEITEMSTATUS_NEW).count();
            accountingDate = LocalDate.now();
            return count;

		/* keep this code for testing
		//
		// This breadcrump fetches a list of all contracts and add 4 years to all
		// - startDate
		// - endDate
		// - accountedTo
		// - signingDate
		//
		List<Contract> contractList = OMA.select(Realm.BACKEND,Contract.class).list() ;
		for(Contract contract:contractList) {
			Date date = contract.getStartDate();
			date = addToDate(date);
			if(date != null ) {
				contract.setStartDate(date);
			}
			date = contract.getEndDate();
			date = addToDate(date);
			if(date != null ) {
				contract.setEndDate(date);
			}
			date = contract.getAccountedTo();
			date = addToDate(date);
			if(date != null ) {
				contract.setAccountedTo(date);
			}
			date = contract.getSigningDate();
			date = addToDate(date);
			if(date != null ) {
				contract.setSigningDate(date);
			}

			contract = OMA.saveEntity(Realm.BACKEND, contract) ;
		}
        */

        }

        /**
         * gets the last (the smallest) virtual invoiceNr
         */
        private Long getMinInvoiceNr() {
            Lineitem lineitem = oma.select(Lineitem.class)
                                   .orderAsc(Lineitem.INVOICENR).queryFirst();
            if (lineitem != null) {
                return lineitem.getInvoiceNr();
            } else {
                return 0L;
            }
        }

        /**
         * analyzes a volume-contract and returns the command to account this
         * contract
         */
        private Command analyzeVolumeContract(Contract contract, LocalDate referenceDate) {
            if(contract.getStartDate().isAfter(referenceDate)) {
                // the startDate is after the referenceDate ---> account this contract not at this time
                return Command.NOTHING;
            }
            if(contract.getStartDate().equals(contract.getEndDate())) {
                return Command.FINISHED_CONTRACT;
            }
            if (contract.getAccountedTo() == null) {
                return Command.NEW_CONTRACT;
            } else {
                if (contract.getEndDate() != null) {
                    if (contract.getEndDate().equals(contract.getAccountedTo())) {
                        return Command.FINISHED_CONTRACT;
                    }

                    if (contract.getAccountedTo().isBefore(contract.getEndDate())) {
                        return Command.ACC_IS_BEFORE_TO;
                    } else {
                        return Command.ACC_IS_AFTER_TO;
                    }
                }
                return Command.ACC_IS_PRESSENT_TO_IS_NULL;
            }
        }

        /**
         * accounts the given contract
         *
         * @throws Exception
         */
        private Contract accountContract(DataCollector<Lineitem> itemCollector,
                                         boolean dryRun, Contract contract, LocalDate from, LocalDate to,
                                         int paymentDirection, LocalDate referenceDate, String headline,
                                         Long invoiceNr, LocalDateTime clearingDate) {

            // check the noAccountingFlag of the contract
            if (contract.isNoAccounting()) {
                return contract;
            }
            boolean flagAccountSinglePriceDone = false;

            // check is the single-price-state not OPEN valid
            if (ContractSinglePriceType.OPEN.equals(contract.getSinglePriceState())) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.errorSinglePriceState")
                                .set("contract", contract.toString())
                                .set("state", ContractSinglePriceType.OPEN).handle();            }
            // check the singlePriceState of this contract
            checkContractSinglePriceState(contract);
            // is accTo present? --> the contract was accounted in the past
            // Ziel ist diesen Code zu ersetzen
            if (contract.getAccountedTo() != null) {
                // check the value 'from': from is in a Invoice never .before(accountedTo)
                if ((from.isBefore(contract.getAccountedTo()) && (paymentDirection == AccountingService.INVOICE))) {
                    throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.paramError1")
                                    .set("contract", contract.toString()).handle();

                }
                // check accTo: never account when referenceDate is before accTo
                if ((referenceDate.isBefore(contract.getAccountedTo()) && (paymentDirection == AccountingService.INVOICE))) {
                    throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.paramError2")
                                    .set("contract", contract.toString()).handle();
                }
            } else { // this is a new contract, because it was not accounted in the past
                if (referenceDate.isBefore(contract.getStartDate())) {
                    throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.paramError3")
                                    .set("contract", contract.toString()).handle();
                }
                // check if there is to account a single price
                Amount singlePrice = getSolidSinglePrice(contract);
                if (ContractSinglePriceType.ACCOUNT_NOW.equals(contract
                                                                       .getSinglePriceState())) {
                    accountSinglePrice(itemCollector, contract, referenceDate,
                                       invoiceNr, singlePrice, clearingDate);
                    flagAccountSinglePriceDone = true;
                }
            }
            // account the contract
            int months = calculateMonths(from, to);
            // check if the months to account = zero or negative
            if (months <= 0) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.fromGreaterTo")
                                .set("contract", contract.toString())
                                .set("from", NLS.toUserString(from))
                                .set("to", NLS.toUserString(to)).handle();
            }
            Amount unitPrice = getSolidUnitPrice(contract);
            // the positionPrice is the unitPrice * quantity of licences
            Amount positionPrice = unitPrice;
            // check whether there is a quantity (for more than one licence)
            if (contract.getQuantity() != null) {
                if (contract.getQuantity() > 0) {
                    positionPrice  = unitPrice.times(Amount.of(contract.getQuantity()));
                }
            }
            positionPrice = round(positionPrice,2);

            Amount positionDiscount = getSolidPositionDiscount(contract);
            if(positionDiscount.isPositive()) {
                int gggg = 1;
            }
            Amount discountAmount = getSolidFinalDiscountAmount(contract);
            if(discountAmount.isPositive()) {
                int hhh = 1;
            }
            Amount reducedPrice = positionPrice.decreasePercent(getSolidPositionDiscount(contract));
            Amount discount = positionPrice.subtract(reducedPrice);
            discount = round(discount, 2);
            // build the lineitem-data
            boolean isCredit = false;
            Amount lineitemPrice = positionPrice;
            if (paymentDirection == AccountingService.CREDIT) {
                BigDecimal lp = lineitemPrice.getAmount();
                lp = lp.multiply(Amount.MINUS_ONE.getAmount());
                lineitemPrice = Amount.of(lp);
                isCredit = true;
            }
            // build the description
            String description = getDescriptionForContract(contract, from, to,
                                                           referenceDate, months, isCredit, positionPrice, headline);
            description = checkDiscounts(description, discount, positionDiscount,
                                         discountAmount);

           // write a lineitem
            writeLineitem(itemCollector, contract, referenceDate, months,
                          lineitemPrice, description, "MON", contract
                                  .getPackageDefinition().getValue().toString(), isCredit,
                          positionDiscount, discountAmount, invoiceNr, clearingDate);
            // set the new accountedTo-date
            if (paymentDirection == AccountingService.INVOICE) {
                contract.setAccountedTo(to);
            } else {
                contract.setAccountedTo(from);
            }
            if (flagAccountSinglePriceDone) {
                contract.setSinglePriceState(ContractSinglePriceType.THIS_ACCOUNT);
            }
            return contract;
        }

        /**
         * accounts the single price of the given contract
         */
        private void accountSinglePrice(DataCollector<Lineitem> itemCollector,
                                        Contract contract, LocalDate referenceDate, Long invoiceNr,
                                        Amount singlePrice, LocalDateTime clearingDate) {
            // check: is the single price not null or = 0
            if (singlePrice == null || singlePrice.getAmount().doubleValue() < 1D) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenoValidPrice")
                        .set("abr", contract.getAccountingGroup())
                        .set("contract", contract.toString()).handle();
            }
            if (PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL.equals(contract.getPackageDefinition().getValue()
                                                         .getAccountingProcedure())) {
                accountSinglePriceRival(itemCollector, contract, referenceDate,
                                        invoiceNr, singlePrice, clearingDate);
            }
            if (PackageDefinition.ACCOUNTINGPROCEDURE_VOLUME.equals(contract.getPackageDefinition().getValue()
                                                          .getAccountingProcedure())) {
                accountSinglePriceVolume(itemCollector, contract, referenceDate,
                                         invoiceNr, singlePrice, clearingDate);
            }
        }

        /**
         * accounts the singlePrice for rival contracts
         */
        private void accountSinglePriceRival(DataCollector<Lineitem> itemCollector,
                                             Contract contract, LocalDate referenceDate, Long invoiceNr,
                                             Amount singlePrice, LocalDateTime clearingDate)  {
            // check: is the single price only one time accounted - with the given
            // contract and not with the older contracts
            int count = countOldRivalContracts(contract.getPackageDefinition().getValue()
                                                       .getProduct().getValue(), AccountingService.COUNT_CONTRACTS_ACCOUNT_NOW,
                                               contract);
            if (count > 0) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlepriceAlsoOtherContracts")
                        .set("contract", contract.toString()).handle();
            }
            // check: is the single price already with the older contracts accounted
            count = countOldRivalContracts(contract.getPackageDefinition().getValue()
                                                   .getProduct().getValue(), AccountingService.COUNT_CONTRACTS_THIS_ACCOUNT,
                                           contract);
            if (count > 0) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenNoAccountingDone")
                                .set("abr", contract.getAccountingGroup())
                                .set("contract", contract.toString()).handle();
            }
            // account the single price for a rival contract
            writeLineitem(itemCollector, contract, referenceDate, 1, singlePrice,
                          MessageFormat.format("einmalige Einrichtungskosten für {0}",
                                               contract.getPackageDefinition().getValue().toString()), "PCE",
                          contract.getPackageDefinition().toString(), false, Amount.ZERO, Amount.ZERO,
                          invoiceNr, clearingDate);
        }

        /**
         * accounts the singlePrice for volume contracts or add-on-contracts
         */
        private void accountSinglePriceVolume(
                DataCollector<Lineitem> itemCollector, Contract contract,
                LocalDate referenceDate, Long invoiceNr, Amount singlePrice, LocalDateTime clearingDate)  {
            // account the single price for a volume or add-on contract
            Integer amount = contract.getQuantity();
            if (amount == null) {
                amount = 1;
            }
            String piece = "Paket";
            if (amount > 1) {
                piece = piece + "e";
            }
            writeLineitem(itemCollector, contract, referenceDate, amount,
                          singlePrice, MessageFormat.format(
                            "einmalige Kosten für {0} {1} {2}", amount, piece,
                            contract.getPackageDefinition().getValue().toString()), "PCE",
                          contract.getPackageDefinition().getValue().toString(), false, Amount.ZERO, Amount.ZERO,
                          invoiceNr, clearingDate);
        }

        /**
         * writes a new lineitem
         */
        private void writeLineitem(DataCollector<Lineitem> itemCollector,
                                   Contract contract, LocalDate referenceDate, int months, Amount price,
                                   String description, String measurement, String packageName,
                                   boolean isCredit, Amount positionDiscount, Amount discountAbsolut,
                                   Long invoiceNr, LocalDateTime clearingDate) {
            Lineitem lineitem = new Lineitem();
            Company company = contract.getCompany().getValue();
            String outputLanguage = company.getCompanyAccountingData().getOutputLanguage();
            lineitem.setOutputLanguage(outputLanguage);

            lineitem.setLineitemType(Lineitem.LINEITEMTYPE_LA);
            // check the absolute discount and calculate the accountingPrice
            // listen: collmex can calculate a percent-discount but never a absolute discount,
            // so we do it here - yes we can!
            if (discountAbsolut.getAmount().doubleValue() > 0D && price.getAmount().doubleValue() > 0D) {
                lineitem.setPrice(Amount.of(price.getAmount().subtract(discountAbsolut.getAmount())));
            } else {
                lineitem.setPrice(price);
            }

            lineitem.setFinalDiscountSum(Amount.ZERO); // finalDiscountSum is not used
            lineitem.setInvoiceNr(invoiceNr);
            lineitem.setStatus(Lineitem.LINEITEMSTATUS_NEW);
            if (price.getAmount().doubleValue() == 0D) {
                lineitem.setPositionType(lineitem.getLineitemCollmexTextposition());
            } else {
                lineitem.setPositionType(lineitem.getLineitemCollmexNormalposition());
            }
            lineitem.setLineitemDate(referenceDate);
            lineitem.setCompanyName(contract.getCompany().getValue().getName());
            String customerNr = contract.getCompany().getValue().getCustomerNr();
            if (Strings.isEmpty(customerNr)) {
                // no accounting witout a customerNr!!!
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.customerNrMissing")
                        .set("company", lineitem.getCompanyName()).handle();
            }
            lineitem.setCustomerNr(customerNr);
            lineitem.setMeasurement(measurement);
            lineitem.setPackageName(packageName);
            lineitem.setClearingDate(clearingDate);
            Integer monthsInteger = months;
            lineitem.setQuantity(Amount.of(monthsInteger.doubleValue()));
            lineitem.setDescription(description);
            lineitem.setPosition(contract.getPosition());
            lineitem.setArticle(contract.getPackageDefinition().getValue().getProduct()
                                        .getValue().getArticle());
            lineitem.setCredit(isCredit);
            lineitem.setCollmexCredit(false);   // this field is used at the export to Collmex, now it is = false
            lineitem.setPositionDiscount(positionDiscount);
            lineitem.setFinalDiscountAmount(discountAbsolut);

            itemCollector.add(lineitem);
        }

        private String checkDiscounts(String description, Amount discount,
                                      Amount discountPercent, Amount discountAbsolute) {
            if (discount.isPositive()) {
                description += MessageFormat
                        .format(" Es wird ein Rabatt von {0}% = {1} EUR/Monat berücksichtigt.",
                                NLS.toUserString(discountPercent),
                                NLS.toUserString(discount));
            }
            if (discountAbsolute.isPositive()) {
                description += MessageFormat.format(
                        " Es wird ein Nachlass von {0} EUR/Monat berücksichtigt.",
                        NLS.toUserString(discountAbsolute));
            }
            return description;
        }

        /**
         * Builds the description for a contract
         */
        private String getDescriptionForContract(Contract contract, LocalDate from,
                                                 LocalDate to, LocalDate referenceDate, int months, boolean isCredit,
                                                 Amount unitPrice, String headline) {
            String accToS = "noch nicht.";
            String quantityType = getQuantityType(contract);
            if (contract.getAccountedTo() != null) {
                accToS = "bis zum "
                         + NLS.toUserString(
                        dateMinus1Day(contract.getAccountedTo()))
                         + ".";
            }
            String headlineMessage = getHeadlineMessage(headline, contract);
            String additionalInfos = "";
            if (Strings.isFilled(contract.getPosLine())) {
                additionalInfos = contract.getPosLine() + ". ";
            }
            if (unitPrice.getAmount().doubleValue() == 0D) {
                String stellen = "stellen";
                if (AccountingService.CREDITACCOUNTING.equals(headline)) {
                    stellen = "stellten";
                }
                return Formatter
                        .create("${headline}. Ihr Ansprechpartner: ${contractPartner}. ${additionalInfos}Bisher abgerechnet: ${accountedTo} Gemäß dem Vertrag vom ${signingDate} ${stellen} wir ${quantityType} vom ${from} bis zum ${to} = ${months} Monate kostenlos bei.").set("headline", headlineMessage)
                        .set("contractPartner", contract.getContractPartner().getValue())
                        .setDirect("additionalInfos", additionalInfos, false)
                        .set("accountedTo", accToS)
                        .set("signingDate", contract.getSigningDate())
                        .set("stellen", stellen).set("from", from)
                        .set("to", dateMinus1Day(to)).set("months", months)
                        .set("quantityType", quantityType).format();
            } else {
                return Formatter
                        .create("${headline}. Ihr Ansprechpartner: ${contractPartner}. ${additionalInfos}Bisher abgerechnet: ${accountedTo} Gemäß dem Vertrag vom ${signingDate} ${paymentType} wir ${quantityType} vom ${from} bis zum ${to} = ${months} Monate  je ${unitPrice} EUR." ).set("headline", headlineMessage)
                        .set("contractPartner", contract.getContractPartner().getValue())
                        .setDirect("additionalInfos", additionalInfos, false)
                        .set("accountedTo", accToS)
                        .set("signingDate", contract.getSigningDate())
                        .set("from", from).set("to", dateMinus1Day(to))
                        .set("months", months).set("unitPrice", unitPrice)
                        .set("paymentType", isCredit ? "vergüten" : "berechnen")
                        .set("quantityType", quantityType).format();
            }
        }

        private String getHeadlineMessage(String headline, Contract contract) {
            String headlineText = "";
            if (AccountingService.NEWACCOUNTING.equals(headline)) {
                if (isQuantityPresentAndGreater1(contract)) {
                    headlineText = Formatter
                            .create("Abrechnung der ${quantity} neuen Pakete:").set("quantity", contract.getQuantity())
                            .format();
                } else {
                    headlineText = "Abrechnung des neuen Pakets:";
                }
            }
            if (AccountingService.RUNNINGACCOUNTING.equals(headline)) {
                if (isQuantityPresentAndGreater1(contract)) {
                    headlineText = Formatter
                            .create("Abrechnung der ${quantity} Pakete:")
                            .set("quantity", contract.getQuantity()).format();
                } else {
                    headlineText = "Abrechnung des Pakets:";
                }

            }
            if (AccountingService.CREDITACCOUNTING.equals(headline)) {
                if (isQuantityPresentAndGreater1(contract)) {
                    headlineText = Formatter
                            .create("Gutschrift für die bisher genutzen ${quantity} Pakete:").set("quantity", contract.getQuantity())
                            .format();
                } else {
                    headlineText = "Gutschrift für das bisher genutzte Paket:";
                }

            }
            return headlineText + " " + contract.getPackageDefinition().getValue().toString();
        }

        /**
         * checks whether the quantity of the given contract is present and > 1
         */
        private boolean isQuantityPresentAndGreater1(Contract contract) {
            if (contract.getQuantity() != null && contract.getQuantity() > 1) {
                return true;
            }
            return false;
        }

        /**
         * <code>
         * builds a string like
         * das Paket                        one package
         * die 2 Pakete                     two packages with price = null or 0,00
         * die 2 Pakete je 2,00 EUR         two packages with price = 	 * 2,00 EUR
         * </code>
         */
        private String getQuantityType(Contract contract) {
            if (contract.getQuantity() != null) {
                String s = "";
                if (contract.getQuantity() > 1) {
                    s = Formatter.create("die ${quantity} Pakete")
                                 .set("quantity", contract.getQuantity()).format();
                }
                Amount unitPrice = getSolidUnitPrice(contract);
                if (unitPrice != null && unitPrice.getAmount().doubleValue() > 0D) {
                    s = s
                        + Formatter.create(" je ${unitPrice} EUR")
                                   .set("unitPrice", unitPrice).format();
                }
                return s;
            }
            return "das Paket";
        }

        /**
         * @return a date calculated as dateIn - 1 day
         */
        private LocalDate dateMinus1Day(LocalDate dateIn) {
            LocalDate dateOut = dateIn.minusDays(1);
            return dateOut;
        }

        /**
         * rounds the value to two decimals
         */
        private Amount round(Amount value, int scale) {
           BigDecimal a = value.getAmount();
           a = a.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
           return Amount.of(a);
        }

        /**
         * get the discountPercent
         */
        private Amount getSolidPositionDiscount(Contract contract) {
            Amount discount = contract.getDiscountPercent();
            if (discount == null) {
                discount = Amount.ZERO;
            }
            return discount;
        }

        /**
         * get the discountAbsolute
         */
        private Amount getSolidFinalDiscountAmount(Contract contract) {
            Amount discount = contract.getDiscountAbsolute();
            if (discount == null) {
                discount = Amount.ZERO;
            }
            return discount;
        }

        @Override
        public Amount getSolidUnitPrice(Contract contract) {
            Amount price = null;
            try {
                price = contract.getUnitPrice();
                if (price == null) {
                    Optional packageDefinitionOpt = oma.find(PackageDefinition.class, contract
                                    .getPackageDefinition().getValue().getId());
                    PackageDefinition packageDefinition = (PackageDefinition) packageDefinitionOpt.get();
                    price = packageDefinition.getUnitPrice();
                    contract.setUnitPrice(price);
                }
            } catch (Exception e) {
                throw Exceptions.createHandled().withNLSKey("AccoungServiceBean.solidUnitPrice")
                                .set("contract", contract.toString()).handle();
            }
            return price;
        }

        @Override
        public Amount getSolidSinglePrice(Contract contract) {
            Amount price = null;
            try {
                price = contract.getSinglePrice();
                if (price == null) {
                     Optional packageDefinitionOpt = oma.find(
                            PackageDefinition.class, contract
                                    .getPackageDefinition().getValue().getId());
                    PackageDefinition packageDefinition = (PackageDefinition) packageDefinitionOpt.get();
                    price = packageDefinition.getSinglePrice();
                    contract.setSinglePrice(price);
                }
            } catch (Exception e) {
                throw Exceptions.createHandled().withNLSKey("AccoungServiceBean.solidSinglePrice")
                        .set("contract", contract.toString()).handle();
            }
            return price;
        }

        /**
         * <code><
         * calculates the count of months between the dates 'start' and 'next'
         * start: 1.3.2011, next: 31.3.2011 --> months = 1
         * start: 1.3.2011, next: 02.3.2011 --> months = 1
         * start: 1.3.2011, next: 01.4.2011 --> months = 1
         * start: 1.3.2011, next: 02.4.2011 --> months = 2
         * /code>
         */
    private int calculateMonths(LocalDate start, LocalDate next) {
        int startMonth = start.getMonthValue();
        int startYear = start.getYear();
        int nextMonth = next.getMonthValue();
        int nextYear = next.getYear();
        int nextInt = nextYear * 12 + nextMonth;
        int startInt = startYear * 12 + startMonth;
        int months = nextInt - startInt;
        if(next.getDayOfMonth() > start.getDayOfMonth())  {
            months = months + 1;
        }
        return months;
     }

        /**
         * process the given Command for the given contracts
         */
        private void processCommand(DataCollector<Lineitem> itemCollector,
                                    boolean dryRun, LocalDate referenceDate, Command command, Contract c0,
                                    Contract c1, Contract c2, Long invoiceNr,  LocalDateTime clearingDate) {
            boolean save = false;
            switch (command) {
                case NEW_CONTRACT:    // Case 1, F1
                    LocalDate nextAccTo = getNextAccountedTo(c1, c2, referenceDate);
                    c1 = accountContract(itemCollector, dryRun, c1, c1.getStartDate(),
                                         nextAccTo, INVOICE, referenceDate,
                                         AccountingService.NEWACCOUNTING, invoiceNr, clearingDate);
                    save = true;
                    break;
                case ACC_IS_PRESSENT_TO_IS_NULL:      // Case 1, F2
                    nextAccTo = getNextAccountedTo(c1, c2, referenceDate);
                    LocalDate from = c1.getAccountedTo();       // 17.1.2016: determine the start of accounting
                    if (from == null) {                     // either from accountedTo - or if accountedTo == null from start-Date of the contract
                        from = c1.getStartDate();
                    }
                    // 			if (nextAccTo.after(c1.getAccountedTo())) {
                    if (nextAccTo.isAfter(c1.getAccountedTo()) && referenceDate.isAfter(c1.getAccountedTo())) {     // 17.1.2016: no accounting if accountedTo > referenceDate
                        c1 = accountContract(itemCollector, dryRun, c1,
                                             from, nextAccTo, INVOICE, referenceDate,
                                             AccountingService.RUNNINGACCOUNTING, invoiceNr, clearingDate);
                        save = true;
                    }
                    break;
                case TO_IS_PRESENT_ACC_IS_NULL:   // case 1, F3
                    nextAccTo = getNextAccountedTo(c1, c2, referenceDate);
                    c1 = accountContract(itemCollector, dryRun, c1, c1.getStartDate(),
                                         nextAccTo, INVOICE, referenceDate,
                                         AccountingService.RUNNINGACCOUNTING, invoiceNr, clearingDate);
                    save = true;
                    break;
                case FINISHED_CONTRACT: // case 1, F4, finished contract, do nothing
                    break;
                case ACC_IS_BEFORE_TO:  // case 1, F5
                    if (c1.getAccountedTo().isBefore(referenceDate)) {  // 17.1.2016: no accounting if accountedTo > referenceDate
                        nextAccTo = getNextAccountedTo(c1, c2, referenceDate);
                        c1 = accountContract(itemCollector, dryRun, c1,
                                             c1.getAccountedTo(), nextAccTo, INVOICE,               // 17.2.2016
                                             referenceDate, AccountingService.RUNNINGACCOUNTING,
                                             invoiceNr, clearingDate);
                        save = true;
                    }
                    break;
                case ACC_IS_AFTER_TO: // case 1, F6
                    if(c1.getEndDate() != null) {
                        // 16.2.2016: No accounting if the referenceDate < endDate
                        if(referenceDate.isBefore(c1.getEndDate())) {
                            break;
                        }
                    }
                    c1 = accountContract(itemCollector, dryRun, c1, c1.getEndDate(),
                                         c1.getAccountedTo(), CREDIT, referenceDate,
                                         AccountingService.CREDITACCOUNTING, invoiceNr, clearingDate);
                    save = true;
                    break;
            }

            if (!dryRun && save) {
                c1 = saveContract(c1);
            }
        }


        /**
         * calculate the next accountedTo-Date in relation to the given startDate
         * and the given referenceDate
         */
        private LocalDate getNextAccountedTo(Contract c1, Contract c2, LocalDate referenceDate) {

            // Get the accounting-interval
            AccountingIntervalType accInterval = c1.getAccountingInterval();
            // Calculate the next accounting date
            LocalDate nextAccTo = accInterval.getNextAccountingStartDate(referenceDate);
            if (c1.getEndDate() != null) { // is a endDate present?
                if (c1.getEndDate().isBefore(nextAccTo)) {
                    nextAccTo = c1.getEndDate();
                }
            }
            if (c2 != null) { // is a second contract present
                if (c2.getStartDate().isBefore(nextAccTo)) { // the second contract begins before next accounting date
                    nextAccTo = c2.getStartDate(); // next accounting date is only
                    // the startDate of the second contract
                    c1.setEndDate(nextAccTo); // 17.2.2016 set the endDate to the next accounting Date / c2.startDate
                }
            }
            return nextAccTo;
        }

        private Contract saveContract(Contract givenContract) {
            oma.update(givenContract);
            return givenContract;
        }

        /**
         * <code>
         * check the contracts - given in the contractList - and store the contracts which are to be accounted in the list "toDoList"
         * with the datatype contractToDos.
         * Meaning of the variables:
         * from = startdate of the contract,
         * c1 = contract 1,
         * acc = accountedTo, this contract ist accounted to the given date accountedTo-date).
         * nacc = nextAccountedTo, this is the next accounting-date in relation to the reference-date.
         * to = endDate of the contract, the value "null" is a running contract without a cancellation.
         *
         * from---c1---acc----to
         * CASE 1: only one Contract is present
         * NEW_CONTRACT,               F1: from---c1---null---null: INVOICE--> from---c1---nacc---null "Abrechnung des neuen Pakets"
         * ACC_IS_PRESSENT_TO_IS_NULL, F2: from---c1---acc----null: (if nextAccTo > accTo && referenceDate.after(accountedTo)) INVOICE--> from---c1---nacc----null "Abrechnung des Pakets"
         * TO_IS_PRESENT_ACC_IS_NULL,  F3: from---c1---null---to:   INVOICE--> from---c1---nacc----to, nacc = to "Abrechnung des laufenden Pakets"
         * FINISHED_CONTRACT,          F4: from---c1---acc-to:		acc = to: the contract is accounted to "to" --> do nothing
         * ACC_IS_BEFORE_TO,           F5: from---c1---acc----to:	acc < to: INVOICE   from = acc, to = to --> from---c1---nacc----to, nacc = to "Abrechnung des Pakets"
         * ACC_IS_AFTER_TO,            F6: from---c1---to----acc:	acc > to: CREDIT  from = to, to = acc --> from---c1---nacc----to nacc = to "Gutschrift für das Paket"
         * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         */


        /**
         * Analyze the given contractList in which all contracts with the same paket and accountinggroup are stored
         * @param contractList
         * @return Tupel To Do, in each tupel is the contract and a command stored
         */
        private List<ContractToDos<Contract, Contract, Contract, Integer>> checkContractList(
                List<Contract> contractList, LocalDate referenceDate) {
            List<ContractToDos<Contract, Contract, Contract, Integer>> toDoList = new ArrayList<ContractToDos<Contract, Contract, Contract, Integer>>();

            for (Contract c : contractList) {
                Command command = analyzeContract(c, referenceDate);
                ContractToDos<Contract, Contract, Contract, Integer> toDo =
                        new ContractToDos<Contract, Contract, Contract, Integer>(null, c, null, command);
                toDoList.add(toDo);
            }
            return toDoList;
        }


        /**
         * analyzes the given contract and calculates a command for accounting this contract.
         */
        private Command analyzeContract(Contract c, LocalDate referenceDate) {
            if (c == null) {
                return Command.NOTHING;
            }
            if(c.getStartDate().isAfter(referenceDate)) {
                // the startDate is after the referenceDate ---> account this contract not at this time
                return Command.NOTHING;
            }

            Command command;
            if(c.getStartDate().equals(c.getEndDate())) {
                return Command.FINISHED_CONTRACT;
            }
            if (c.getAccountedTo() != null) {
                if (c.getEndDate() != null) {
                    if (c.getAccountedTo().equals(c.getEndDate())) {
                        return Command.FINISHED_CONTRACT; // finished contract
                    }
                    if(referenceDate.isBefore(c.getAccountedTo()) && referenceDate.isBefore(c.getEndDate())) {
                        // the referenceDate is before the accountedTo and the referenceDate is before the endDate
                        // ---> account this contract not at this time
                        return Command.NOTHING;
                    }
                    if (c.getAccountedTo().isBefore(c.getEndDate())) {
                        command = Command.ACC_IS_BEFORE_TO; // acc < to --> INVOICE
                    } else {
                        command = Command.ACC_IS_AFTER_TO; // acc > to -->CREDIT
                    }
                } else {
                    if(referenceDate.isBefore(c.getAccountedTo()))  {
                        // the referenceDate is before the accountedTo ---> account this contract not at this time
                        return Command.NOTHING;
                    }
                    command = Command.ACC_IS_PRESSENT_TO_IS_NULL; // acc is present, to is null
                }
            } else {
                if (c.getEndDate() == null) {
                    command = Command.NEW_CONTRACT; // new contract
                } else {
                    command = Command.TO_IS_PRESENT_ACC_IS_NULL; // to is present, acc = null
                }
            }
            return command;
        }


        private Amount sum = Amount.ZERO;
        private int counter = 0;

        private void addToSum(Amount value) {
            sum = sum.add(value);
        }

        private void addToCounter(int value) {
            counter = counter + value;
        }

        @Override
        public void exportLicenceLineitems(int maxLineitemsDefault, String filter) {
            if(maxLineitemsDefault <= 0) {
                maxLineitemsDefault = 300;
            }
            if(filter == null) {
                filter = "";
            }
            sum = Amount.ZERO;
            counter = 0;
            // get all lineitems with status "NEW' and Type "LA"
            List<Lineitem> lineitemList = oma
                    .select( Lineitem.class)
                    .eq(Lineitem.STATUS, Lineitem.LINEITEMSTATUS_NEW)
                    .eq(Lineitem.LINEITEMTYPE, Lineitem.LINEITEMTYPE_LA)
                    .orderDesc(Lineitem.INVOICENR)
                    .orderAsc(Lineitem.POSITION).queryList();

            if (lineitemList.size() <= 0) {
                // ToDO Meldung machen   ss.forBackendStream(
//                ss.forBackendStream(
//                        DisplayMarkdownFactory.FACTORY_NAME,
//                        "Lizenz-Abrechnung",MessageFormat.format(
//                                "In der Tabelle 'lineitem' wurden keine lineitems mit dem Typ {0} zum exportieren gefunden",
//                                LineitemType.LA))
//                  .loginRequired(true).setUser(Users.getCurrentUser())
//                  .publish();
                return;
            }
            int counterNew = lineitemList.size();
            // check, modify and save these lineitems where the sum of the invoice = 0,00 EUR; set the CollmexCredit-Flag
            int counterNull = 0;
            counterNull = counterNull + checkLineitems(lineitemList);
            lineitemList.clear();

            // get all lineitems with status "NEW' and Type "LA" after modifying
            lineitemList = oma
                    .select(Lineitem.class)
                    .eq(Lineitem.STATUS, Lineitem.LINEITEMSTATUS_NEW)
                    .eq(Lineitem.LINEITEMTYPE, Lineitem.LINEITEMTYPE_LA)
                    .orderDesc(Lineitem.INVOICENR)
                    .orderAsc(Lineitem.POSITION)
                    .queryList();
            boolean filterFlag = false;
            if(Strings.isFilled(filter)) {
                filter = filter.toUpperCase();
                if (filter.contains("AUSL")) {
                    filterFlag = true;
                    filter = "AUSLAND";
                }
                if (filter.contains("NEIN")) {
                    filterFlag = false;
                } else {
                    filterFlag = true;
                }
            } else {
                filter = "NEIN";
                filterFlag = false;
            }
            // filter the lineitems. Is no filter given --> add all lineitems to the lineitemFilteredList
            List<Lineitem> lineitemFilteredList =new ArrayList<Lineitem>();
            for(Lineitem l:lineitemList) {
                if(filterFlag) { // filter is != nein
                    String customerNr = l.getCustomerNr() ;
                    Company company = oma.select(Company.class).eq(Company.CUSTOMERNR, customerNr).queryFirst() ;
                    String countryCode = company.getAddress().getCountry().toUpperCase() ;
                    if(Strings.isEmpty(countryCode)) {countryCode = "DE";}
                    boolean flagAdd = false;
                    if("AUSLAND".equals(filter))  {  // filter is Ausland
                        if(!("DE".equals(countryCode))) {
                            flagAdd = true;    // add all lineitems with contyCode != DE
                        }
                    }
                    if(!flagAdd) {
                        if(filter.contains(countryCode)) {
                            flagAdd = true;    // add the lineitem if the countryCode matches the filter, eg "AT FR NL"
                        }
                    }
                    if(flagAdd) {
                        lineitemFilteredList.add(l);
                    }
                } else {
                    lineitemFilteredList.add(l);
                }
            }

            // process the lineitemFilteredList --> write the lineitems in groups (size= maxLineDefault) to a outputList
            // Each group is written in a separate file
            List <Lineitem> outputList = new ArrayList<Lineitem>();
            List<File> filenames = new ArrayList<File>();
            long invoiceNr = 0;
            int fileNr = 0;
            File file = null;
            Lineitem l = null;
            int maxLineitems = maxLineitemsDefault;
            for(int k =0;  k < lineitemFilteredList.size(); k++)   {
                maxLineitems--;
                // check if the maxLineitems is in the range of maxLineitems-default-value
                if(maxLineitems <=0 ) {
                    // the value is outside the range  --> look for lineitems with the same invoiceNr and finish the outputList
                    boolean flag = false;
                    l = lineitemFilteredList.get(k);
                    if (invoiceNr == l.getInvoiceNr()) {
                        // the invoiceNr is the same --> add the lineitem to the outputList
                        outputList.add(l);
                        // Look for more lineitems with the same invoiceNr
                        for(int m=0; m<100; m++) {
                            k = k+1;
                            l = lineitemFilteredList.get(k);
                            if (invoiceNr == l.getInvoiceNr()) {
                                //  the invoiceNr is the same --> add the lineitem to the outputList
                                outputList.add(l);
                            } else {
                                // the invoiceNr is different --> the outputList can be finished
                                flag = true;
                                break;
                            }
                        }

                    } else {
                        // the invoiceNr is different --> the outputList can be finished
                        flag = true;
                    }
                    if (flag) {
                        // finish the outputList --> write the lineitems in the outputList to a new file
                        k = k-1;
                        fileNr++;
                        file = createCsvFilename("lineitems", fileNr);
                        filenames.add(file);
                        writeToFile(outputList, file);
                        outputList.clear();
                        maxLineitems = maxLineitemsDefault;
                    }

                } else {
                    // the counter for the maxLineitems is in range of the defaultvalue -->
                    // add the lineitem to the outputList and get the invoiceNr
                    l = lineitemFilteredList.get(k);
                    invoiceNr = l.getInvoiceNr() ;
                    outputList.add(l);
                }
            }
            // look for the last lineitems in the outputList
            if(outputList.size()>0) {
                fileNr++;
                file = createCsvFilename("lineitems", fileNr);
                filenames.add(file);
                writeToFile(outputList, file);
            }

            // build a activity-news
            String message = MessageFormat.format("Es wurde nichts exportiert, Filter: {0}", filter);
            if(filenames.size() > 0) {
                file = filenames.get(0);
                File fileLast = null;
                String fileNameLast = "keine weitere Dateien";
                int last = filenames.size() - 1;
                if (last > 0) {
                    fileLast = filenames.get(last);
                    fileNameLast = "bis " + fileLast.getName();
                }
                message = MessageFormat
                        .format("{0} Rechnungspositionen mit Status NEW, {1} Rechnungspositionen mit Betrag = null, {2} Rechnungspositionen exportiert," +
                                "Anzahl aller Rechnungspositionen {3}, Netto-Umsatz: {4} EUR, Datei: {5}   {6}" +
                                ", Filter: {7}",
                                counterNew, counterNull, counter, counter + counterNull,
                                NLS.toUserString(sum),
                                file.getAbsoluteFile(),
                                fileNameLast, filter);
                System.err.println(message);
            }
            // ToDo meldung machen    ss.forBackendStream(
//            ss.forBackendStream(
//                    DisplayMarkdownFactory.FACTORY_NAME,
//                    "Lizenz-Abrechnung",message)
//              .loginRequired(true).setUser(Users.getCurrentUser())
//              .publish();
        }

        /**
         *  write the lineitems in the outputList to a new file
         */

        private void writeToFile(List<Lineitem> outputList, File file ) {
            PrintWriter pw = null;
            try {
                Amount sum = Amount.ZERO;
                pw = createPrintWriter(file);
                for (Lineitem lineitem : outputList) {
                    Amount value = generateCollmexInvoiceLine(pw, lineitem, null);
                    sum = sum.add(value);
//                    For testing:
//                    String text = lineitem.getCompanyName() + ";" + lineitem.getCustomerNr() + ";" + NLS.toUserString(value);
//                    text = text + ";" + lineitem.getPackageName() + ";" +  lineitem.getPositionType() + ";" + lineitem.getStatus();
//                    System.err.println(text);
                }
                addToSum(sum);
                pw.flush(); // flush the printwriter to get all data to the file

            } catch (Exception e) {
                Exceptions.handle(e) ;
            } finally {
                pw.close();
            }
        }


        private PrintWriter createPrintWriter (File file) throws Exception {
            FileOutputStream output = null;
            output = new FileOutputStream(file);
            Writer fw = new OutputStreamWriter(output, Charset.forName("ISO-8859-1"));
            PrintWriter pw = new PrintWriter(fw);
            return pw;
        }

        /**
         * checks whether lineitems has to be not transfered to collmex
         */
        private int checkLineitems(List<Lineitem> lineitemList) {
            int counterNull = 0;
            List<Lineitem> itemList = new ArrayList<Lineitem>();
            Long invoiceNr = 0L;
            // put all lineitems with one inVoiceNr in the itemList and process then
            // the itemList
            for (Lineitem lineitem : lineitemList) {
                // check if there is a new invoiceNr
                if (!lineitem.getInvoiceNr().equals(invoiceNr)) {
                    // get the new invoiceNr
                    invoiceNr = lineitem.getInvoiceNr();
                    // check the items in the itemList
                    if(!itemList.isEmpty()) {
                        counterNull = counterNull + checkItemList(itemList);
                    }
                    // clear the itemList
                    itemList.clear();
                }
                itemList.add(lineitem);
            }
            // is the itemlist empty or is to do some work?
            if (!itemList.isEmpty()) {
                counterNull = counterNull + checkItemList(itemList);
            }
            return counterNull;
        }

        /**
         * checks all lineitems (with the same invoice-Nr) in the itemList.
         * If the sum of the invoice = 0 --> the status is .IS_ZERO, counterNull is incremented.
         * If the sum of the invoice < 0 --> collmexIsCredit = true.
         * Listen: all lineitems with this invoiceNr get the same status!
         */
        private int checkItemList(List<Lineitem> itemList) {
            int counterNull = 0;
            Amount sum = Amount.ZERO;
            Amount finalDiscountSum = Amount.ZERO;
            for (Lineitem item : itemList) {
                // add the finalDiscount to the finalDiscountSum
                finalDiscountSum = finalDiscountSum.add(item.getFinalDiscountAmount());
                // calculate the price
                Amount price = item.getPrice().decreasePercent(item.getPositionDiscount());

                price = price.times(item.getQuantity());

                // add the price to the sum
                sum = sum.add(price);
            }
            // subtract the (sum of the finalDiscounts) from the sum
            sum = sum.subtract(finalDiscountSum);
            boolean collmexIsCredit = false;
            String status = Lineitem.LINEITEMSTATUS_NEW;
            if (sum.getAmount().doubleValue() == 0D) {
                status = Lineitem.LINEITEMSTATUS_IS_ZERO;
                counterNull ++;
            }
            if (sum.getAmount().doubleValue() < 0D) {
                collmexIsCredit = true;
            }
            for (Lineitem item : itemList) {
                item = checkItem(item, collmexIsCredit, status, finalDiscountSum);
            }
            return counterNull;

        }


        /**
         * checks whether the item has to be updated
         * @param item
         * @param collmexIsCredit
         * @param status
         * @param finalDiscountSum
         * @return
         */
        private Lineitem checkItem(Lineitem item, boolean collmexIsCredit,
                                   String status, Amount finalDiscountSum) {
            boolean flag = false;
//		System.err.println("Alt: "+item.getCompanyName() + "  " + item.getPackageName() + "   " + item.getStatus() + "   " + NLS.toMachineString(item.getFinalDiscountSum()) + "   " + NLS.toUserString(item.isCollmexCredit())) ;

            if (!(item.isCollmexCredit() == collmexIsCredit)) {
                item.setCollmexCredit(collmexIsCredit);
                flag = true;
            }
            if (!(item.getStatus().equals(status))) {
                item.setStatus(status);
                flag = true;
            }
            if (!(item.getFinalDiscountSum().equals( finalDiscountSum))) {
                item.setFinalDiscountSum(finalDiscountSum);
                flag = true;
            }
            if (flag) {
//			System.err.println("Neu: "+item.getCompanyName() + "  " + item.getPackageName() + "   " + item.getStatus() + "   " + NLS.toMachineString(item.getFinalDiscountSum()) + "   " + NLS.toUserString(item.isCollmexCredit())) ;
            if(item.getClearingDate() == null) {
                item.setClearingDate(LocalDateTime.now());
            }
                oma.update(item);
            }
            return item;
        }



        /**
         * creates a filename like YYYYMMDD_hhmmss_<nr>_name
         */
        @Override
        public File createCsvFilename(String name, int nr) {
            String s = dateTimeFilename("_", null);
            if(nr >=0) {
                s = s + NLS.toUserString(nr)+ "_";
            }
            // ToDo neue Lösung oder Rechtsnachfolger für Path
//            return new File(Path.getPath(LINEITEM_PATH).getFile(), s + name + ".csv");
            return new File(LINEITEM_PATH, s + name + ".csv");
        }

        /**
         * creates a string like YYYYMMDD_hhmmss_name "-" is the given space
         */
        @Override
        public String dateTimeFilename(String space, LocalDateTime dateTime) {
            if(dateTime == null) {
                dateTime = LocalDateTime.now();
            }
            if(Strings.isEmpty(space)) {
                space = "_";
            }
            String s = NLS.toUserString(dateTime.getYear());
            s += twoDecimals(NLS.toUserString(dateTime.getMonthValue()));
            s += twoDecimals(NLS.toUserString(dateTime.getDayOfMonth()));
            s += space;
            s += twoDecimals(NLS.toUserString(dateTime.getHour()));
            s += twoDecimals(NLS.toUserString(dateTime.getSecond()));
            s += space;
            return s;
        }


        /**
         * extends a number to two decimals
         */
        private String twoDecimals(String value) {
            if (value.length() < 2) {
                value = "0" + value;
            }
            return value;
        }

        /**
         * generates a invoice-position in Colmex
         */
        private Amount generateCollmexInvoiceLine(PrintWriter pw,
                                                  Lineitem lineitem, LocalDate invoiceDate) {
            Amount sum = Amount.ZERO;

            // generate a csv-line for the export in Collmex-Notation
            final int csvLae = 82; // field #1 - #82
            String[] csv = new String[csvLae + 1]; // csv[0] - csv[82],
            // csv[0] is not used
            csv[1] = "CMXINV"; // Satzart C Festwert CMXINV
            csv[2] = NLS.toUserString(lineitem.getInvoiceNr()); // Rechnungsnummer I8.
            // Die Rechnungsnummer identifiziert die Rechnung eindeutig. Siehe auch Nummernvergabe.
            // 03 = Position I 8 Positionsnummer der Rechnungsposition. Wenn nicht
            // angegeben, wird die Positionsnummer automatisch fortlaufend vergeben.
            if (lineitem.isCollmexCredit()) {// 04 = Rechnungsart I 8
                csv[4] = "1"; // 1 = Gutschrift
            } else {
                csv[4] = "0";// 0 = Rechnung
            } // 2 = Abschlagsrechnung, 3 = Barverkauf
            csv[5] = "1"; // 05 = Firma Nr I 8 Interne Nummer der Firma, wie unter
            // Verwaltung -> Firma anzeigen und ändern angezeigt.
            // 06 = Auftrag Nr I 8 Nummer des Kundenauftrags, auf den sich die
            // Rechnung bezieht.
            csv[7] = NLS.toUserString(lineitem.getCustomerNr()); // 07 = Kunden-Nr I8
            // Der Kunde muss in Collmex existieren. Referenz ausschliesslich über die Kundennummer
            // 08 Anrede C 10 Felder 8 - 27 ist die Kundenadresse. Nur für den
            // Export. Die Felder werden beim Import ignoriert.
            // 09 Titel C 10
            // 10 Vorname C 40
            // 11 Name C 40
            // 12 Firma C 40
            // 13 Abteilung C 40
            // 14 Strasse C 40
            // 15 PLZ C 10
            // 16 Ort C 20
            // 17 Land C 2 ISO Codes
            // 18 Telefon C 20
            // 19 Telefon2 C 20
            // 20 Telefax C 20
            // 21 E-Mail C 50
            // 22 Kontonr C 20
            // 23 Blz C 20
            // 24 Abweichender Kontoinhaber C 30
            // 25 IBAN C 20
            // 26 BIC C 20
            // 27 Bank C 20
            // 28 = USt.IdNr C 20
            csv[29] = "0"; // Privatperson I8, 0 = keine Privatperson , 1 = Privatperson
            if (invoiceDate == null) { // 30 // Rechnungsdatum
                csv[30] = NLS.toUserString(lineitem.getLineitemDate());
            } else {
                csv[30] = NLS.toUserString(invoiceDate);
            }
            // D8 Falls nicht angegeben, wird das aktuelle Datum gesetzt.
            // 31 Preisdatum D 8 Falls nicht angegeben, wird das Rechnungsdatum gesetzt.
            // csv[32] = "0"; // Zahlungsbedingung I 8 Als Zahl codiert, beginnend
            // mit 0 für 30T ohne Abzug, wie im Programm unter "Einstellungen". Falls nicht angegeben, wird die
            // Zahlungsbedingung vom Kunden übernommen.
            if (lineitem.isCollmexCredit()) {
                csv[32] = "10"; // Bei Gutschrift keine Zahlungsbedingungen = 10
            }
            // 33 Währung (ISO-Codes) C 3 Falls nicht angegeben, wird die Währung
            // vom Kunden übernommen.
            // 34 Preisgruppe I 8 Interne Nummer der Preisgruppe, wie sie bei der
            // Pflege der Preisgruppe in der ersten Spalte angezeigt wird. Falls
            // nicht angegeben, wird die Preisgruppe vom Kunden übernommen.
            // 35 Rabattgruppe I 8 Interne Nummer der Rabattgruppe, wie sie bei der
            // Pflege der Rabattgruppe in der ersten Spalte angezeigt wird. Falls
            // nicht angegeben, wird die Rabattgruppe vom Kunden übernommen.
            // 36 Schluss-Rabatt I 8 Schluss-Rabatt in Prozent.
            // 37 Rabattgrund C 255 Grund für den Rabatt.
            // 38 Rechnungskopftext

            // csv[39] = COLLMEX_NULL; // 39 Schlusstext C 1024 Falls (NULL), wird
            // der Text aus den Standard-Textbausteinen ermittelt.

            csv[40] = "Position am " + NLS.toUserString(LocalDateTime.now())
                      + " exportiert"; // 40 Internes Memo C 1024
            csv[41] = "0"; // 41 Gelöscht I 8 0 = nicht gelöscht, 1 = gelöscht.
            csv[42] = lineitem.getOutputLanguage(); // 42 Sprache I 8,  0 = Deutsch, 1 = Englisch.

            // 43 Bearbeiter I 8 Mitarbeiternummer des Bearbeiters. Falls nicht
            // angegeben, wird der Bearbeiter automatisch bestimmt.
            // 44 Vermittler I 8 Mitarbeiter Nummer des Vermittlers für die
            // Provisionsabrechnung.
            // 45 Systemname C 20 Name des externen Systems, das den Auftrag
            // angelegt hat
            // 46 Status I 8 Nur für Export. 0 = neu, 10 = Zu buchen, 20 = offen, 30
            // = gemahnt, 40 = erledigt, 100 = gelöscht
            // csv[47] = NLS.toUserString(lineitem.getFinalDiscountSum()); // kein
            // Schluss-Rabatt 20.2.2012
            // 47 Schluss-Rabatt 2, M 18, Schluss-Rabatt als Betrag.
            // 48 Schluss-Rabatt-2-Grund C 20 Grund für den Rabatt.
            // 49 Versandart I 8 Interne Nummer der Versandart, wie sie bei der
            // Pflege der Versandart angezeigt wird. Falls nicht angegeben, wird die
            // Versandart automatisch bestimmt über die Versandgruppen der im
            // Auftrag enthaltenen Produkte (nur Collmex pro).
            // 50 Versandkosten M 18 Falls nicht angegeben, werden die Versandkosten
            // automatisch über die Versandart bestimmt.
            // 51 Nachnahmegebühr M 18 Falls nicht angegeben, wird die
            // Nachnahmegebühr automatisch über die Versandart bestimmt.
            // 52 Lieferdatum D 8 Optional.
            // 53 Lieferbedingung C 3 International genormte INCOTERMS. Falls nicht
            // angegeben, wird die Lieferbedingung aus der Versandart übernommen.
            // 54 Lieferbedingung Zusatz C 40 Ort der Lieferbedingung
            // 55 Anrede C 10 Felder 46 - 58: Optional. Abweichende Lieferadresse.
            // 56 Titel C 10
            // 57 Vorname C 40
            // 58 Name C 40
            // 59 Firma C 40
            // 60 Abteilung C 40
            // 61 Strasse C 40
            // 62 PLZ C 10
            // 63 Ort C 20
            // 64 Land C 2 ISO Codes
            // 65 Telefon C 20
            // 66 Telefon2 C 20
            // 67 Telefax C 20
            // 68 E-Mail C 50
            csv[69] = NLS.toUserString(lineitem.getPositionType());
            // Positionstyp I 8
            // 0 = Normalposition,
            // 1 = Summenposition,
            // 2 = Textposition,
            // 3 = Kostenlos.
            csv[70] = lineitem.getArticle();// Produktnummer C 20 Falls nicht
            // angegeben, muss die
            // Produktbeschreibung gefüllt sein. Die Produktnummer muss in Collmex
            // existieren.
            String description = lineitem.getDescription();
            csv[71] = replaceCharacter(description, ";");// 71 Produktbeschreibung C
            // 10.000
            // Falls nicht angegeben, wird die
            // Beschreibung aus dem Produkt
            // übernommen.
            String measurement = lineitem.getMeasurement();
            if("PT".equals(measurement)) {
                measurement= "DAY";    // Anpassung PT --> DAY wg. Collmex
            }
            csv[72] = measurement; // 72 Mengeneinheit C 3 ISO Codes.
            // DAY = Personentag
            // PCE = Stück,
            // MON = Monat, Falls nicht
            // angegeben, wird die Mengeneinheit vom in Collmex gespeicherten Produkt
            // übernommen.
            if(Lineitem.LINEITEMTYPE_LA.equals(lineitem.getLineitemType()))  {
                int quantity =lineitem.getQuantity().getAmount().intValue();
                csv[73] = NLS.toUserString(quantity); // 73 Menge N 18
            }  else {
                csv[73] = NLS.toUserString(lineitem.getQuantity()); // 73 Menge N 18
            }
            // Auftragsmenge
            Amount price = lineitem.getPrice();
            // in the lineitems is a credit a negative value.
            // In collmex is a credit a positive value with the flag 'isCredit=true'
            // if the "invoice" is a credit (sum is negative and there is a
            // invoice-linitem)
            // you have also to change the signum of the value
            if (lineitem.isCollmexCredit()) {
                price = Amount.of(price.getAmount().multiply(BigDecimal.valueOf(-1)));
            }
            csv[74] = NLS.toUserString(price);
            // Einzelpreis M 18 Falls nicht angegeben, wird / der Preis über das Produkt bestimmt.
            csv[75] = NLS.toUserString(lineitem.getQuantity()); // Preismenge N 18
            // Falls nicht angegeben, wird die Preismenge über das Produkt bestimmt bzw. auf 1 gesetzt.
            csv[76] = NLS.toUserString(lineitem.getPositionDiscount());// 76 Positionsrabatt M 18
            // Positionsrabatt in Prozent mit zwei Nachkommastellen.
            // 77 Positionswert M 18 Nur für Export. Beim Import wird der
            // Positionswert automatisch aus Preis und Rabatt berechnet.
            csv[78] = "0"; // 78 Produktart I 18 Wird beim Import nur verwendet,
            // wenn kein Produkt gesetzt ist. 0 = Ware, 1 =
            // Dienstleistung, 2 = Mitgliedsbeitrag (nur Collmex
            // Verein), 3 = Baudienstleistung
            csv[79] = "0";// 79 Steuerklassifikation I 8 Wird beim Import nur
            // verwendet, wenn kein Produkt gesetzt ist. Andernfalls
            // wird die Steuerklassifikation aus dem Produkt
            // ermittelt. 0 = voller Steuersatz, 1 = halber
            // Steuersatz, 2 = steuerfrei.
            csv[80] = "0"; // 80 Steuer auch im Ausland I 8 Wird beim Import nur
            // verwendet, wenn kein Produkt gesetzt ist und die
            // Produktart eine Dienstleistung ist. 0 = Sonstige
            // Leistung ist im Inland nicht steuerbar bei
            // Leistungsort im Ausland, 1 = Sonstige Leistung ist im
            // Inland steuerbar auch bei Leistungsort im Ausland.
            csv[81] = "0";// 81 Kundenauftragsposition I 8 Position des
            // Kundenauftrags, auf das sich die Rechnungsposition
            // bezieht.
            csv[82] = "0"; // Erlösart I 8 Nur für Export. 0 = voller Steuersatz

            // Step 3: build the CSV-string from the array 'csv'
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= csvLae; i++) {
                if (csv[i] != null) {
                    sb.append(csv[i]);
                }
                sb.append(";");
            }
            // Step 4: print the CSV-string to the file
            pw.println(sb.toString());

            // Step 5: set status and the clearingDate
            // ToDo wieder einbauen.  lineitem.setStatus(Lineitem.LINEITEMSTATUS_ACCOUNTED);
//            lineitem.setStatus(Lineitem.LINEITEMSTATUS_ACCOUNTED);
            lineitem.setClearingDate(LocalDateTime.now());
            oma.update(lineitem);
            // Step 6: calculate the position-sum
            sum = lineitem.getPrice();
            sum = sum.decreasePercent(lineitem.getPositionDiscount());
            sum = sum.times (lineitem.getQuantity());
            sum = round(sum,2);
            addToCounter(1);
            return sum;
        }

        private String replaceCharacter(String string, String character) {
            string = string.replace(character, " ");
            return string;
        }

//        @Override
    // toDO klären, ob das noch gebraucht wird. Hinweis: eher nicht. im CRM keine Nutzanwendung
//        public List<Lineitem> getLineitems() {
//
//            if (lineitemList == null) {
//                return Collections.emptyList();
//            }
//
//            if (lineitemList.size() == 0) {
//                return Collections.emptyList();
//            }
//            return lineitemList;
//        }

    // TODO: Migrieren? Hinweis: list wird im CRM nur vom ocm.fs.VFile verwendet   public void list(VFile parent, ChildCollector collector)
//        @Override
//        public void list(VFile parent, ChildCollector collector) {
//            collector.add(new DirectoryWrapper(parent, "SYS", Path.getPath(
//                    LINEITEM_PATH).getFile(), "lineitems"));
//
//        }

        @Override
        public void checkContractSinglePriceState(Contract givenContract)  {
            PackageDefinition pd = givenContract.getPackageDefinition().getValue();
            String ap = pd.getAccountingProcedure();
            if (ap.equals(PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL)) {
                checkContractSinglePriceStateRival(givenContract, pd);
            }
            if (ap.equals(PackageDefinition.ACCOUNTINGPROCEDURE_VOLUME)) {
                checkContractSinglePriceStateVolume(givenContract, pd);
            }
        }

        private void checkContractSinglePriceStateVolume(Contract givenContract,
                                                         PackageDefinition pd)  {
            Amount singlePrice = pd.getSinglePrice();

            if (givenContract.getSinglePrice() == null) {     //TASK 7362
                singlePrice = pd.getSinglePrice();
            }
            switch (givenContract.getSinglePriceState()) {
                case NO_ACCOUNTING:
                    break;
                case NO_SINGLEPRICE:
                    break;
                case OPEN:
                    break;
                case ACCOUNT_NOW:
                    if (givenContract.getAccountedTo() != null) {
                        throw Exceptions.createHandled()
                                        .withNLSKey("AccountingServiceBean.singlePriceIsAccounted")
                                        .set("abr", givenContract.getAccountingGroup())
                                        .set("contract", givenContract.toString())
                                        .set("accTo", NLS.toUserString(givenContract.getAccountedTo()))
                                        .handle();
                    }
                    if (singlePrice == null || singlePrice.getAmount().doubleValue() < 1D) {
                        throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenoValidPrice")
                                        .set("abr", givenContract.getAccountingGroup())
                                        .set("contract", givenContract.toString()).handle();
                    }

                    break;
                case THIS_ACCOUNT:
                    if (givenContract.getAccountedTo() == null) {
                        throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenNoAccountingDone")
                                        .set("abr", givenContract.getAccountingGroup())
                                        .set("contract", givenContract.toString()).handle();
                    }

                    break;
                case OLD_ACCOUNT:
                    break;
            }
        }

        /**
         * checks the singlePriceState of the given rival contract
         */
        private void checkContractSinglePriceStateRival(Contract givenContract, PackageDefinition pd) {
            // get the singleprice from the package-definition
            Amount singlePrice = pd.getSinglePrice();
            // if a singleprice in the contract is present, take this one
            if (givenContract.getSinglePrice() == null) {     // TASK 7362
                singlePrice = pd.getSinglePrice();
            }

            Product product = pd.getProduct().getValue();
            switch (givenContract.getSinglePriceState()) {
                case NO_ACCOUNTING:
                    break;
                case NO_SINGLEPRICE:
                    break;
                case OPEN:
                    break;
                case ACCOUNT_NOW: // the single price is accounted with this account
                    // check: no other contracts with state "account now"
                    checkOldRivalContracts(product, givenContract,
                                           AccountingService.COUNT_CONTRACTS_ACCOUNT_NOW,
                                           ContractSinglePriceType.ACCOUNT_NOW, 0);
                    // check: no other contracts with state
                    // "account with this account"
                    checkOldRivalContracts(product, givenContract,
                                           AccountingService.COUNT_CONTRACTS_THIS_ACCOUNT,
                                           ContractSinglePriceType.THIS_ACCOUNT, 0);
                    // TASK 5863: Prüfung herausgenommen
                    // // check: no contract with state OLD_ACCOUNT is present
                    // checkOldRivalContracts(product, givenContract,
                    // AccountingService.COUNT_CONTRACTS_OLD_ACCOUNT,
                    // ContractSinglePriceType.OLD_ACCOUNT, 0);

                    // check: this contract should be not accounted
                    if (givenContract.getAccountedTo() != null) {
                        throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePriceIsAccounted")
                                .set("abr", givenContract.getAccountingGroup())
                                .set("contract", givenContract.toString())
                                .set("accTo", NLS.toUserString(givenContract.getAccountedTo())).handle();
                    }
                    // check: a real singleprice shoud be existent
                    if (singlePrice == null || singlePrice.getAmount().doubleValue() < 1D) {
                        throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenoValidPrice")
                                  .set("abr", givenContract.getAccountingGroup())
                                  .set("contract", givenContract.toString()).handle();
                    }

                    break;
                case THIS_ACCOUNT: // the singleprice was accounted with this account
                    // check: no other contract with this state is existent
                    checkOldRivalContracts(product, givenContract,
                                           AccountingService.COUNT_CONTRACTS_THIS_ACCOUNT,
                                           ContractSinglePriceType.THIS_ACCOUNT, 0);
                    // check: no other contract with the state "account now" is existent
                    checkOldRivalContracts(product, givenContract,
                                           AccountingService.COUNT_CONTRACTS_ACCOUNT_NOW,
                                           ContractSinglePriceType.ACCOUNT_NOW, 0);
                    // check: was there accounting before? if not --> error
                    if (givenContract.getAccountedTo() == null) {
                        Exceptions.createHandled().withNLSKey("AccountingServiceBean.singlePricenNoAccountingDone")
                                  .set("abr", givenContract.getAccountingGroup())
                                  .set("contract", givenContract.toString()).handle();
                    }
                    break;
                case OLD_ACCOUNT: // this is a other old contract
                    // check is a other contract with state "account with this account"
                    // or a other contract with state "account now" existent
                    // or one ore more contracts with state "no account" existent
                    if (countOldRivalContracts(product,
                                               AccountingService.COUNT_CONTRACTS_THIS_ACCOUNT,
                                               givenContract) != 1) {
                        if (countOldRivalContracts(product,
                                                   AccountingService.COUNT_CONTRACTS_ACCOUNT_NOW,
                                                   givenContract) != 1) {
                            if (countOldRivalContracts(product,
                                                       AccountingService.COUNT_CONTRACTS_NO_ACCOUNT,
                                                       givenContract) < 1) {
                                throw Exceptions.createHandled()
                                          .withNLSKey("AccountingServiceBean.singlePricenNoOtherContract")
                                          .set("abr", givenContract.getAccountingGroup())
                                          .set("contract", givenContract.toString())
                                          .set("state1", ContractSinglePriceType.OLD_ACCOUNT.toString())
                                          .set("state2", ContractSinglePriceType.ACCOUNT_NOW.toString())
                                          .set("state3", ContractSinglePriceType.THIS_ACCOUNT.toString())
                                          .handle();
                            }
                        }
                    }
                    break;
            }
        }

        private String addApostroph(String text) {
            return "'" + text + "'";
        }

        private void checkOldRivalContracts(Product product,
                                            Contract givenContract, int mode, ContractSinglePriceType source,
                                            int checkCount)  {
            int count = countOldRivalContracts(product, mode, givenContract);
            if ((count != checkCount)) {
                throw Exceptions.createHandled().withSystemErrorMessage("AccountingServiceBean.singlePrice.manyOldContracts")
                        .set("abr", givenContract.getAccountingGroup())
                        .set("contract", givenContract.toString())
                        .set("count", NLS.toUserString(count) )
                        .set("state", source.toString()).handle();
            }
        }

        @Override
        public int countOldRivalContracts(Product product, int mode,
                                          Contract givenContract) {
            List<Contract> companyContractList = oma
                    .select(Contract.class)
                    .eq(Contract.COMPANY, givenContract.getCompany())
                    .eq(Contract.ACCOUNTINGGROUP, givenContract.getAccountingGroup() ).queryList();
            int count = 0;
            for (Contract contract : companyContractList) {
                if (contract.getId() != givenContract.getId()) {
                    // check only rival contracts (Task #5710)
                    if (PackageDefinition.ACCOUNTINGPROCEDURE_RIVAL.equals(contract
                                     .getPackageDefinition().getValue().getAccountingProcedure())) {
                        if (product.equals(contract.getPackageDefinition().getValue().getProduct().getValue())) {
                            switch (mode) {
                                case AccountingService.COUNT_CONTRACTS_WITHOUT_THIS_CONTRACT:
                                    count++;
                                    break;
                                case AccountingService.COUNT_CONTRACTS_THIS_ACCOUNT:
                                    if (contract.getSinglePriceState().equals(
                                            ContractSinglePriceType.THIS_ACCOUNT)) {
                                        count++;
                                    }
                                    break;
                                case AccountingService.COUNT_CONTRACTS_ACCOUNT_NOW:
                                    if (contract.getSinglePriceState().equals(
                                            ContractSinglePriceType.ACCOUNT_NOW)) {
                                        count++;
                                    }
                                    break;
                                case AccountingService.COUNT_CONTRACTS_OLD_ACCOUNT:
                                    if (contract.getSinglePriceState().equals(
                                            ContractSinglePriceType.OLD_ACCOUNT)) {
                                        count++;
                                    }
                                    break;
                                case AccountingService.COUNT_CONTRACTS_NO_ACCOUNT:
                                    if (contract.getSinglePriceState().equals(
                                            ContractSinglePriceType.NO_ACCOUNTING)) {
                                        count++;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            return count;
        }


        private Amount getProdUmsatz() {
            return prodUmsatz;
        }


        private Amount getTestUmsatz() {
            return testUmsatz;
        }


        private LocalDate getAccountingDate() {
            return accountingDate;
        }

        @Override
        public void generateCollmexKundenCsv()    {
            List<Company> companyList = oma.select(Company.class)
                                          .where(FieldOperator.on(Company.CUSTOMERNR).notEqual(""))
                                           .orderAsc(Company.CUSTOMERNR).queryList();
            if (companyList.size() <= 0) {
                throw Exceptions.createHandled().withNLSKey("AccountingServiceBean.noCompanyToExport").handle();
            }

            for (Company c: companyList ) {
                System.err.println(c.toString() + "/" + c.getCustomerNr() + "/");
            }
            if(companyList.size() > 0) {return;}
            // export them to the file
            File file = createCsvFilename("kunden", -1);
            try {
                FileOutputStream output = new FileOutputStream(file);
                Writer fw = new OutputStreamWriter(output, "ISO-8859-1");
                PrintWriter pw = new PrintWriter(fw);
                try {
                    pw.println("Typkennung;Kunde Nr;Firma;Anrede;Titel;Vorname;Name;Firma;Abteilung;Straße;PLZ;Ort;Bemerkung;"
                               + "Inaktiv;Land;Telefon;Telefax;E-Mail;KontoNr;BLZ;IBAN;BIC;Bankname;Steuernummer;USt-IdNr.;Zahlungsbed.;Rabattgruppe;"
                               + "Lieferbedingung;Zusatz;Ausgabemedium;Inhaber;Adressgruppe;eBay-Mitgliedsname;Preisgruppe;Währung;Vermittler;Kostenstelle;"
                               + "Wiedervorlage;Liefersperre;Baudienstleister;Lief-Nr. bei Kunde;Ausgabe-Sprache;CC;Telefon2");
                    int i = 0;
                    for (Company company : companyList) {
                        i++;
                        printKundenCsvLine(pw, company);
                    }
                    pw.flush(); // flush the printwriter to get all data to the file
                    // build a activity-news
                    // ToDo Meldung machen ss.forBackendStream(
//                    ss.forBackendStream(
//                            DisplayMarkdownFactory.FACTORY_NAME,
//                            "Export für Collmex",
//                            MessageFormat.format(
//                                    "{0} Kundendaten exportiert, Datei: {1}",
//                                    NLS.toUserString(i), file.getAbsoluteFile()))
//                      .loginRequired(true).setUser(Users.getCurrentUser())
//                      .publish();
                } finally {
                    pw.close();
                }
            } catch (Exception e) {
                Exceptions.handle(e);
            }
        }


    private void printKundenCsvLine(PrintWriter pw, Company company) {
            StringBuilder sb = new StringBuilder();
            sb.append("CMXKND;");
            sb.append(company.getCustomerNr());
            sb.append(";1 Gerhard Haufler;;;;;");
            sb.append(company.getName() + ";;");
            sb.append(company.getAddress().getStreet() + ";");
            sb.append(company.getAddress().getZip() + ";");
            sb.append(company.getAddress().getCity() + ";");
            sb.append(";0;");
            sb.append(company.getAddress().getCountry().toUpperCase());
            sb.append(";;;;;;;;;;;0 30 Tage ohne Abzug;0 ;;;0 Druck;;;;0 Standard;EUR;0;;;0;0;;0 Deutsch;;");

            pw.println(sb.toString());

        }



        /**
         * check CRM-Data (as master-data) against collmex-data
         *
         * @param filenameCollmex
         *            = filename of the collmex-data
         * @param pw
         *            = PrintWriter for output-file
         * @throws Exception
         */

	/*
	private void crmVersusCollmex(String filenameCollmex, PrintWriter pw)
			throws Exception {
		message(" ", "", "", false, 0, pw, false, false, null);
		message("Start Vergleich CRM mit  CollmexAdressen", "", "", false, 0,
				pw, false, false, null);
		BufferedReader in = null;
		boolean printDone = false;
		try { // get a buffered reader to the collmex-file
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					filenameCollmex), Tools.ISO_8859_1));
			// get a HashMap to store the collmex-data, key is the customer-Nr
			HashMap<String, String> map = new HashMap<String, String>();
			// read the collmex-data and store them in the HashMap
			String line;
			while ((line = in.readLine()) != null) {
				String[] par = line.split(";");
				map.put(par[1], line);
			}
			in.close();
			// get the crm-data
			List<Company> companyList = OMA
					.select(Realm.BACKEND, Company.class)
					.orderByAsc(Company.NAME).list();
			if (companyList.isEmpty()) {
				print("?????? there are no crm-data !!!!", pw);
				return;
			}
			// check the crm-data against the collmex-data
			for (Company company : companyList) {
				String customerNr = company.getCustomerNr();
				// if a customer-Nr is present ...
				if (!Tools.emptyString(customerNr)) {
					// get the collmex-data from the HashMap, key = customerNr
					line = map.get(customerNr);
					printDone = false;
					if (Tools.emptyString(line)) {
						line = "Firma: " + NLS.toUserString(company);
						printDone = message("###### Für die CRM-Kundennumer "
								+ customerNr
								+ " gibt es keinen Collmex-Datensatz", "", "",
								false, 0, pw, printDone, true, line);
					} else {
						// check the data against the crm-data

						String[] par = line.split(";");
						String name = par[7];
						String street = par[9];
						String zipCode = par[10];
						String city = par[11];
						String country = par[14];
						printDone = check("", customerNr,
								company.getCustomerNr(),
								"customerNr ist falsch", true, pw, printDone,
								line);
						printDone = check("", name, company.getName(),
								"name ist falsch", true, pw, printDone, line);
						printDone = check("", street, company.getStreet(),
								"street ist falsch", true, pw, printDone, line);
						if ("DE".equals(country)) {
							if (zipCode.length() == 4) {
								zipCode = "0" + zipCode;
							}
						}
						printDone = check("", zipCode, company.getZipCode(),
								"PLZ ist falsch", false, pw, printDone, line);

						printDone = check("", city, company.getCity(),
								"city ist falsch", true, pw, printDone, line);
					}
				} else {

					line = "Firma: " + NLS.toUserString(company);
				}
				Company companyFill = OMA.fill(company, Company.CONTRACTS);
				List<Contract> contractList = companyFill.getContracts();
				if (contractList.size() > 0) {
					if (Tools.emptyString(customerNr)) {
						printDone = message(
								MessageFormat
										.format("###### Bei der Firma {0} fehlt die Kundennummer, aber es gibt {1} Verträge mit der Firma.",
												company.getName(),
												contractList.size()),
								"", "", false, 0, pw, printDone, true, line);
					}
					if (!CompanyType.CUSTOMER.equals(company.getCompanyType())) {
						printDone = message(
								MessageFormat.format(
										"###### Bei der Firma {0} gibt es {1} Verträge, aber der CompanyType ist: {2}.",
										company.getName(), contractList.size(),
										company.getCompanyType()), "", "",
								false, 0, pw, printDone, true, line);
					}
				}
			}
			message("Ende Vergleich CRM mit CollmexAdressen", "", "", false, 0,
					pw, false, false, null);
		} catch (Exception e) {
			print(e.getMessage(), pw);
			throw e;
		}
	}

	private void print(String s, PrintWriter pw) {
		if (pw != null) {
			// System.err.println(s);
			pw.println(s);
		}
	}
*/
        /**
         * checks the collmex-data (as master-data) against the crm-data
         *
         * @param filenameCollmex
         *            = name of the collmex-file
         * @param pw
         *            = printwriter for the outputfile
         */
	/*
	private void collmexVersusCrm(String filenameCollmex, PrintWriter pw) {
		message("Start Vergleich CollmexAdressen mit CRM", "", "", false, 0,
				pw, false, false, null);
		BufferedReader in = null;
		try {

			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					filenameCollmex), Tools.ISO_8859_1));

			String line;

			while ((line = in.readLine()) != null) {
				boolean printDone = false;
				checkAdressCollmexVersusCRM(line, pw, printDone);
			}
			in.close();
			message("Ende Vergleich CollmexAdressen mit CRM", "", "", false, 0,
					pw, false, false, null);
		} catch (Exception e) {
			print(e.getMessage(), pw);
		}
	}
	*/

        /**
         * checks all Adress-Parameters between collmex-data and crm-data
         */
	/*
	private void checkAdressCollmexVersusCRM(String line, PrintWriter pw,
			boolean printDone) {
		if (!line.startsWith("CMXKND;"))
			return;
		String[] par = line.split(";");
		String customerNr = par[1];
		if ("9999".equals(customerNr))
			return;
		if ("10000".equals(customerNr))
			return;
		String name = par[7];
		String street = par[9];
		String zipCode = par[10];
		String city = par[11];
		String country = par[14];
		Company company = OMA.select(Realm.BACKEND, Company.class)
				.eq(customerNr, Company.CUSTOMERNR).first();
		String adressString = buildAdressString(customerNr, name, street,
				zipCode, city);

		if (company == null) {
			printDone = message(adressString, customerNr,
					"Diese customerNr gibts in company nicht", true, -1, pw,
					printDone, true, line);
			return;
		}
		printDone = check(adressString, customerNr, company.getCustomerNr(),
				"customerNr ist falsch", true, pw, printDone, line);
		printDone = check(adressString, name, company.getName(),
				"name ist falsch", true, pw, printDone, line);
		printDone = check(adressString, street, company.getStreet(),
				"street ist falsch", true, pw, printDone, line);
		if ("DE".equals(country)) {
			if (zipCode.length() == 4) {
				zipCode = "0" + zipCode;
			}
		}

		printDone = check(adressString, zipCode, company.getZipCode(),
				"PLZ ist falsch", true, pw, printDone, line);
		printDone = check(adressString, city, company.getCity(),
				"city ist falsch", true, pw, printDone, line);
	}
*/
        /**
         * <p>
         * Find the Levenshtein distance between two Strings.
         * </p>
         *
         * <p>
         * This is the number of changes needed to change one String into another,
         * where each change is a single character modification (deletion, insertion
         * or substitution).
         * </p>
         *
         * <p>
         * The previous implementation of the Levenshtein distance algorithm was
         * from <a
         * href="http://www.merriampark.com/ld.htm">http://www.merriampark.com
         * /ld.htm</a>
         * </p>
         *
         * <p>
         * Chas Emerick has written an implementation in Java, which avoids an
         * OutOfMemoryError which can occur when my Java implementation is used with
         * very large strings.<br>
         * This implementation of the Levenshtein distance algorithm is from <a
         * href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/
         * ldjava.htm</a>
         * </p>
         *
         * <pre>
         * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
         * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
         * StringUtils.getLevenshteinDistance("","")               = 0
         * StringUtils.getLevenshteinDistance("","a")              = 1
         * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
         * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
         * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
         * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
         * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
         * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
         * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
         * </pre>
         *
         * @param s
         *            the first String, must not be null
         * @param t
         *            the second String, must not be null
         * @return result distance
         * @throws IllegalArgumentException
         *             if either String input <code>null</code>
         */
	/*
	public static int getLevenshteinDistance(String s, String t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}
*/
		/*
		 * The difference between this impl. and the previous is that, rather
		 * than creating and retaining a matrix of size s.length()+1 by
		 * t.length()+1, we maintain two single-dimensional arrays of length
		 * s.length()+1. The first, d, is the 'current working' distance array
		 * that maintains the newest distance cost counts as we iterate through
		 * the characters of String s. Each time we increment the index of
		 * String t we are comparing, d is copied to p, the second int[]. Doing
		 * so allows us to retain the previous cost counts as required by the
		 * algorithm (taking the minimum of the cost count to the left, up one,
		 * and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing
		 * a System.arraycopy() each time through the outer loop.)
		 *
		 * Effectively, the difference between the two implementations is this
		 * one does not cause an out of memory condition when calculating the LD
		 * over two very large strings.
		 */
/*
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			String tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
						+ cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}

	*/

        /**
         * compare two values
         *
         * @param adressString
         *            = adress of the company
         * @param first
         *            = first value (master)
         * @param second
         *            = second value
         * @param message
         *            = message if the values are not equal
         * @param errorMessage
         *            = flag to write the message
         * @param pw
         *            = PrintWrite for the outputfile
         * @return levenshtein-distance between first and second value
         */

	/*
	private boolean check(String adressString, String first, String second,
			String message, boolean errorMessage, PrintWriter pw,
			boolean printDone, String line) {
		first = checkIfEmpty(first);
		second = checkIfEmpty(second);
		if (first.equals(second)) {
			return printDone;
		}
		int dist = getLevenshteinDistance(first, second);
		if (dist > 0) {
			if (errorMessage) {
				printDone = message("Collmex: " + first, "CRM: " + second,
						message, true, dist, pw, printDone, true, line);
			}
		}
		return printDone;
	}

	private String checkIfEmpty(String string) {
		if (string == null) {
			string = "";
		} else {
			string = string.trim();
		}
		return string;
	}

	private boolean message(String first, String second, String message,
			boolean error, int levDist, PrintWriter pw, boolean printDone,
			boolean printLine, String line) {
		if (!Tools.emptyString(line) && printLine && !printDone) {
			print(" ", pw);
			print(line, pw);
			printDone = true;
		}
		String string = first;
		if (!Tools.emptyString(second)) {
			string = string + ", " + second;
		}
		if (!Tools.emptyString(message)) {
			string = string + ", " + message;
		}
		if (error && levDist > 0) {
			print(">>>>>> " + string + ", Levenshtein-Distanz: " + levDist, pw);
		} else {
			print(string, pw);
		}
		return printDone;
	}

	private String buildAdressString(String customerNr, String name,
			String street, String zipCode, String city) {
		return customerNr + ": " + name + ", " + street + ", " + zipCode + " "
				+ city + ": ";

	}
*/


    }
