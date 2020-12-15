/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

import sirius.db.jdbc.Database;
import sirius.db.jdbc.Row;

import java.util.HashMap;

/**
 * Created by gerhardhaufler on 04.01.19.
 */
public interface MigrationTableService {
    /**
     * migrate the data in the crmTable to the given woodyTable
     *
     * @param crmTable: name of the crmTable
     * @param woodyTable: name of the woodyTable. If woodyTable == null --> woodyTable = crmTable
     */
    public void migrateCrmDataToWoody(String crmTable, String woodyTable);


    /**
     * deletes the content of the given table
     * @param db
     * @param table
     */
    public void deleteContentOfTable(Database db, String table);

    /**
     * builds a uniqueName --> transfer the given name to lowerCase and replace ä, ö, ü ß
     */
    public Object buildUniqueName(String name);

    /**
     * prepare the traceData given in the row and add them to the given HashMap
     */
    public void prepareTraceData(HashMap<String, Object> map, Row row, String function);

    /**
     * insert the in a HashMap given data into the given table in the given databas
     */
    public void insertRow(Database db, HashMap<String, Object> map, String table);

    /**
     * read all compaanies from the crm and add the dataPrivacyPersons into the woody-company-table
     */
    public void addDataprivacyPersons();

    /**
     * set all dataPrivacyPersons in the woody-company-table to null
     */
    public void deleteDataPrivacyPersonsInCompanies();

    /**
     * get the tenant
     */
    public String getTENANT();

    /**
     * saves all opportunities
     */
    public void saveAllOpportunities();

}
