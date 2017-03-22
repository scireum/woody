/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.collmex;

import com.scireum.crm.model.Company;
import com.scireum.crm.model.Employee;

import java.util.HashMap;
import java.util.List;

public interface CollmexInformationService {

	/**
	 * get the data of one customer from collmex in a map
	 */
	public HashMap<String, String> getCollmexCustomerData(String customerNumber, List<String> access);

	/**
	 * check the collmex-Data in the map against the CRM-data adressed by the
	 * company
	 */
	public List<String> checkCollmexAgainstCRM(Company companyCRM, HashMap<String, String> collmexMap);

	/**
	 * update the company in the CRM with the given collmex-data in the map
	 */
	public Company updateCompanyWithCollmex(Company companyCRM,
											HashMap<String, String> collmexMap,
											boolean save,
											boolean newCompany);

	/**
	 * generates a collmex-string for the given employee
	 */
	public String generateCollmexStrings(Employee employee);

	/**
	 * checks the access to collmex
	 */
	public List<String> checkAccess();

}
