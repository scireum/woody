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

    public Object buildUniqueName(String name);

    public void prepareTraceData(HashMap<String, Object> map, Row row, String function);

    public void insertRow(Database db, HashMap<String, Object> map, String table);

    public String getTENANT();

}
