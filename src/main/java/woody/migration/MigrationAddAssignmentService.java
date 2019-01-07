/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

/**
 * Created by gerhardhaufler on 04.01.19.
 */
public interface MigrationAddAssignmentService {
    /**
     * add Tags from compay-companyType, company-bussinessType, industry,
     * it-Decider, salesDecider, marketingDecider, management
     */
    public void addTags();

    /**
     * adds the tagAsignments fom the companies (businessType and companyType
     */
    public void addTagAssignemtsFromCompany();

    /**
     * adds the tagAssignments from persons ( t-Decider, salesDecider, marketingDecider, management)
     */
    public void addTagAssignmentsFromPerson();

    /**
     * adds the tagasignment from the given crmTable
     */
    public void transferTagAssignment(String crmTable, String targetType);

    /**
     * updates the given sequencecounter
     * @param function  = "sequencecounter":
     * @param nameCrm   = name of the sequencecounter-value in CRM
     * @param nameWoody = name of the sequencecounter-value in Woody
     *
     * @param function  = "sequencecounter":
     * @param nameCrm   = name of the table in CRM ("company")
     * @param nameWoody = name of the sequencecounter-value in Woody
     */
    public void updateSequenceCounter(String nameCrm, String nameWoody, String function);

    /**
     * special routine!
     * after the transfer of the crm.employees to the woody.useraccount-table the login-data are empty
     * this routine transfers the logindata of gha
     * @param shortname
     */
    public void transferEmployeeInUserAccount(String shortname);
}
