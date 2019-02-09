/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.modules.collmex;

import sirius.biz.tenants.UserAccount;
import woody.xrm.Company;

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
     * generates a collmex-access-string for the given employee
     */
    public String generateCollmexAccessStrings(UserAccount uac);

    /**
     * get the access-data to collmex for the given useraccount
     *
     * @param uac: userAccount, or null --> the current user
     * @return a list with the access-data.
     */
    public List<String> getCollmexAccessData(UserAccount uac);

    /**
     * generates a csv-string for Collmex with the datas from the given company
     *
     * @param company
     * @return
     */

    public String generateCollmexCmxKnd(Company company);

    /**
     * ends the given csv-String to Collmex
     *
     * @param csvString
     */
    public void sendCmxKnd(List<String> collmexAccessData, String csvString);
}
