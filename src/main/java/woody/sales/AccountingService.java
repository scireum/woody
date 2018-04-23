/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

/**
 * 
 */
package woody.sales;




import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.DataCollector;
import woody.xrm.Company;

import javax.swing.text.View;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Here are stored all service-descriptions for the CRM and the licence-accounting
 * 
 */
public interface AccountingService {
//
//	/**
//	 * do not touch these constants !!!
//	 */
//	public static final int INVOICE = 1;
//	public static final int CREDIT = -1;
//	public static final String NEWACCOUNTING = "newAccounting";
//	public static final String RUNNINGACCOUNTING = "runningAccounting";
//	public static final String CREDITACCOUNTING = "creditAccounting";
//
//	public static final String COLLMEX_NULL = "(NULL)";
//	public static final String LINEITEM_PATH = "lineitemPath";
//
//
//	/**
//	 * account all contracts
//	 *
//	 * @param referenceDate
//	 *            = reference Date
//	 * @param givenCompany
//	 *            = company or null for all companies
//	 */
//	public DataCollector<Lineitem> accountAllContracts(boolean dryRun,
//				LocalDate referenceDate, Company givenCompany,
//				//			  TaskMonitor monitor,
//				boolean foreignCountry) ;
//
////	/**
////	 * creates csv-files and exports the lineitems
////	 * @param maxLineitems = max lineitems in one File
////	 * @param filter = only these lineitems are exported
////	 *               nein = no filter
////	 *               Ausland = all countries exclude DE
////	 *               Countrycodes like DE or CH or AT and so on
////	 *
////	 */
////	public void exportLicenceLineitems(int maxLineitems, String filter);
//
//	/**
//	 * creates csv-files and exports the lineitems
//	 * @param type "LA" = licence, "OA" = offeritems
//	 * @param maxLineitems  max lineitems in one File
//	 * @param filter = only these lineitems are exported
//	 *               nein  or "" or null = no filter
//	 *               Ausland = all countries exclude DE
//	 *               Countrycodes like DE or CH or AT and so on
//     */
//	public void exportLineitems(String type, int maxLineitems, String filter);
//
//	/**
//	 * test wether lineitems with status "new" are present
//	 * @return  count of lineitems with status "new"
//	 *
//     */
//	public Long exportTest();
//
//	/**
//	 * checks the cmx-relation from collmex against the customers
//	 */
//	// Am 19.2.2016 stillgelegt, Ersatz durch cis
//	// public void collmexKundenCheck();
//
//	/**
//	 * checks the singlepriceState of the given contract
//	 *
//	 * @see ContractSinglePriceType
//	 */
//	public void checkContractSinglePriceState(Contract givenContract) ;
//
//	public File createCsvFilename(String filename, int nr, LocalDateTime timestamp);
//
//	/**
//	 * counts old rival contracts
//	 *
//	 * @param product
//	 *            product of the given contract
//	 * @param mode
//	 *            : mode of counting, see below
//	 * @param givenContract
//	 * @return: number of found contracts
//	 */
//
//
//	public int countOldRivalContracts(Product product, int mode, Contract givenContract) ;
//
//	public static final int COUNT_CONTRACTS_WITHOUT_THIS_CONTRACT = 1;
//	public static final int COUNT_CONTRACTS_THIS_ACCOUNT = 2;
//	public static final int COUNT_CONTRACTS_ACCOUNT_NOW = 3;
//	public static final int COUNT_CONTRACTS_OLD_ACCOUNT = 4;
//	public static final int COUNT_CONTRACTS_NO_ACCOUNT = 5;
//
//	/**
//	 * get the UnitPrice of the given contract. if the unitPrice == null get the
//	 * unitPrice from the packageDefinition
//	 *
//	 * @throws Exception
//	 */
//	public Amount getSolidUnitPrice(Contract contract) ;
//
//	/**
//	 * get the SinglePrice if the singlePrice in the contract is null, get the
//	 * singlePrice from the packageDefinition
//	 */
//	public Amount getSolidSinglePrice(Contract contract) ;
//
//	/**
//	 * generates a csv-file with all companies from the company-relation
//	 * this file can be used to import the customers in collmex.
//	 */
//	public void generateCollmexKundenCsv() ;
//
//	/**
//	 * creates a string like YYYYMMDD_hhmmss_name "_" is the given space
//	 * if cal == null the Calende-Value cal is created
//	 */
//	public String dateTimeFilename(String space, LocalDateTime timestamp);
//
//	/**
//	 * gets the last (the smallest) virtual invoiceNr
//	 */
//	public Long getMinInvoiceNr();
//
//	/**
//	 * checks the syntax of a given parameter, like "aaa=bbb ccc=unendlich"
//	 * @param parameter
//	 */
//	public void checkParameterSyntax(String parameter);
//
//	/**
//	 * creates a PDF-Information for the given company
//	 * @param company
//	 */
//	public void createYearInformationForCompany(Company company, int year, LocalDateTime timestamp);
//
//	/**
//	 * returns the errorList.
//     */
//	public List<String> getErrorList();

}
