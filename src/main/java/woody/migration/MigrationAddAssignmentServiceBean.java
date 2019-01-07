/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

import sirius.db.jdbc.Database;
import sirius.db.jdbc.Databases;
import sirius.db.jdbc.Row;
import sirius.db.mixing.Column;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import woody.core.tags.Tag;
import woody.core.tags.TagAssignment;
import woody.xrm.Company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by gerhardhaufler on 04.01.19.
 */
@Register(classes = {MigrationAddAssignmentService.class})
public class MigrationAddAssignmentServiceBean implements MigrationAddAssignmentService {

    @Part
    private static Databases databases;

    @Part
    private static OMA oma;

    @Part
    private static MigrationTableService mts;

    private HashMap<String, String> generalTypeMap = new HashMap();

    @Override
    public void addTags() {

            System.out.println("Start addTags " );
            Database dbCrm = databases.get("crm");
            Database dbWoody = databases.get("mixing");


            // 1. CRM-Tabelle 'industry' in Tags umsetzen
            Long maxId = 0L;
            Long idT = readActualTableId(dbWoody, Tag.class);
            boolean finished = false;
            generalTypeMap.clear();

            do  {
                List<Row> rowList = readTheRowList("industry", dbCrm, maxId);
                Tuple<Long, Long> tuple = null;
                if(rowList.size() > 0) {
                    maxId = storeTypeTags(rowList, maxId, "name");
                } else {
                    finished = true;
                }
            } while ( ! finished);

            for(String key : generalTypeMap.keySet()) {
                idT = createTag(dbWoody, key, idT, "COMPANY");
            }
            System.out.println("   Tags aus der crm-Tabelle 'industry' angelegt, letzte id: " + idT.toString() + ", idT: "+ idT.toString());

            // 2. Anlegen der Tags für itDecider, marketingDecider, salesDecider, management
            idT = createTag(dbWoody, "itDecider", idT, "PERSON");
            idT = createTag(dbWoody, "marketingDecider", idT, "PERSON");
            idT = createTag(dbWoody, "salesDecider", idT, "PERSON");
            idT = createTag(dbWoody, "management", idT, "PERSON");
            System.out.println("   Tags für itDecider usw. angelegt, idT: " +idT.toString());

            // 3. Anlegen der Tags für companyType und businessType
            finished = false;
            maxId = 0L;
            generalTypeMap.clear();

            do  {
                List<Row> rowList = readTheRowList("company", dbCrm, maxId);
                if(rowList.size() > 0) {
                    maxId = storeTypeTags(rowList, maxId, "businessType, companyType");
                } else {
                    finished = true;
                }
            } while ( ! finished);
            for(String key : generalTypeMap.keySet()) {
                idT = createTag(dbWoody, key, idT, "COMPANY");
            }
            System.out.println("   Tags für companyTypes und businessTypes aus der Tabelle crm.company als Tags angelegt, idT: " + idT.toString());
            System.out.println("Ende addTags");
    }

    @Override
    public void addTagAssignemtsFromCompany() {
        String fields = "companyType, businessType";
        String table = "company";
        addAssignmentsFromTable(fields, table, "table", "COMPANY");
    }

    @Override
    public void addTagAssignmentsFromPerson() {
        String fields = "itDecider, marketingDecider, salesDecider, management";
        String table = "person";
        addAssignmentsFromTable(fields, table, "boolean", "PERSON");

    }

    @Override
    public void transferTagAssignment(String table, String targetType) {
        System.out.println("Start Transfer TagAssignments von: " + table);
        Database dbCrm = databases.get("crm");
        Database dbWoody = databases.get("mixing");

        Long maxId = -1L;
        Long idT = readActualTableId(dbWoody, TagAssignment.class);
        boolean finished = false;
        HashMap<String, Object> map = new HashMap();

        List<Row> rowList = readTheRowList(table, dbCrm, maxId);
        Tuple<Long, Long> tuple = null;
        if(rowList.size() > 0) {
            for (Row row : rowList) {
                map.clear();
                if (!(maxId == -1L)) {
                    Long id = row.getValue(("id")).getLong();
                    if (id > maxId) {
                        maxId = id;
                    }
                }
                Long targetEntity = null;
                Long tagId = null;
                try {
                    switch (table) {
                        case "tagCompanyAssignment": {
                            targetEntity = row.getValue("company").getLong();
                            tagId = row.getValue("tag").getLong();
                            break;
                        }
                        case "tagOpportunityAssignment": {
                            targetEntity = row.getValue("person").getLong();
                            tagId = row.getValue("tag").getLong();
                            break;
                        }
                        case "tagPersonAssignment": {
                            targetEntity = row.getValue("person").getLong();
                            tagId = row.getValue("tag").getLong();
                            break;
                        }
                        case "industryAssignment": {
                            targetEntity = row.getValue("company").getLong();
                            Long industryId = row.getValue("industry").getLong();
                            Optional opt = oma.find(Industry.class, industryId);
                            if (opt.isPresent()) {
                                Industry industry = (Industry) opt.get();
                                String industryName = industry.getName();
                                industryName = mts.buildUniqueName(industryName).toString();
                                opt = oma.select(Tag.class).eq(Tag.UNQIUE_NAME, industryName).first();
                                if (opt.isPresent()) {
                                    Tag tag = (Tag) opt.get();
                                    tagId = tag.getId();
                                } else {
                                    throw new Exception("tag für uniqueName: " + industryName + " wurde nicht gefunden.");
                                }
                            } else {
                                throw new Exception("industry für id: " + industryId.toString() + " wurde nicht gefunden.");
                            }
                            break;
                        }
                        default: {
                            throw new Exception("Keine Verarbeitung für tabelle: " + table);
                        }
                    }

                    idT = idT + 1;
                    insertTagAssignment(dbWoody, idT, targetEntity, tagId, targetType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("   Lesen der Assignments aus Tabelle: "
                                   + table
                                   + ", letzte Id: "
                                   + maxId.toString()
                                   + ", idT: "
                                   + idT.toString());

        } else {
            finished = true;
            System.out.println("   TagAssignments aus der Tabelle '" + table + "' gelesen, letzte id: " +  maxId.toString() + ", idT: " + idT.toString());
        }
        System.out.println("Ende transferTagassignment, TagAssignments aus der Tabelle '" + table + "' gelesen, letzte id: " +  maxId.toString() + ", idT: " + idT.toString());
    }

    private void addAssignmentsFromTable(String fieldsStr, String table, String function, String target) {
        System.out.println("Start addAssignments von '" + table +"', Felder: " + fieldsStr + ";   function: " + function + ", target: " + target );
        Database dbCrm = databases.get("crm");
        Database dbWoody = databases.get("mixing");
        String[] fields = fieldsStr.split(",");
        Long maxId = 0L;
        Long idT = readActualTableId(dbWoody, TagAssignment.class);
        boolean finished = false;

        do  {
            List<Row> rowList = readTheRowList(table, dbCrm, maxId);
            Tuple<Long, Long> tuple = null;
            if(rowList.size() > 0) {
                for (Row row : rowList)  {
                    Long id = row.getValue(("id")).getLong();
                    if(id > maxId) {
                        maxId = id;
                    }
                    for(int i=0; i<fields.length; i++) {
                        idT = prepareTagAssignment(dbWoody, idT, row, id, fields[i].trim(), function, target);
                    }
                }
                System.out.println("   Lesen der Assignments aus Tabelle: " + table + ", letzte Id: " +maxId.toString() + ", idT: " + idT.toString());
            } else {
                finished = true;
                System.out.println("   TagAssignments aus der Tabelle '" + table + "' gelesen, letzte id: " +  maxId.toString() + ", idT: " + idT.toString());
            }
        } while ( ! finished);
        System.out.println("Ende addTagassignment, TagAssignments aus der Tabelle '" + table + "' gelesen, letzte id: " +  maxId.toString() + ", idT: " + idT.toString());
    }

    /**
     * prepares the given tagassignment
     * @param dbWoody
     * @param idT      id of the tagassignment
     * @param row      row with data of the crm
     * @param id       id of the targetEntity
     * @param name     name of the tag
     * @return         idT
     */
    private Long prepareTagAssignment(Database dbWoody, Long idT, Row row, Long id, String name, String function, String target) {
        String tagName = null;
        Long tagId = null;
        if("table".equals(function)) {
            // only if the value is filled
            if (row.getValue(name).isFilled()) {
                tagName = row.getValue(name).getString();
            }
        }
        if("boolean".equals(function)) {
            tagName = name;
        }
        if(Strings.isFilled(tagName)) {
            Optional optional = oma.select(Tag.class).eq(Tag.NAME, tagName).first();
            if (optional.isPresent()) {
                Tag tag = (Tag) optional.get();
                tagId = tag.getId();
            } else {
                // ToDo exception anzeigen
                throw Exceptions.createHandled().withNLSKey("MigrationsServiceBean.noTagFound").set("tag", tagName).handle();
            }
            if ("table".equals(function)) {
                // only if the value is filled
                if (row.getValue(name).isFilled()) {
                    idT = idT + 1;
                    insertTagAssignment(dbWoody, idT, id, tagId, "COMPANY");
                }
            }
            if ("boolean".equals(function)) {
                String value = row.getValue(name).getString().toLowerCase();
                if ("true".equals(value)) {
                    idT = idT + 1;
                    insertTagAssignment(dbWoody, idT, id, tagId, "COMPANY");
                }
            }
        }
        return idT;

    }

    /**
     * inserts the given tagassignment
     */
    private void insertTagAssignment(Database db, Long idT, Long targetEntity, Long tagId, String targetType) {

        HashMap<String, Object> map = new HashMap();
        map.put("id", idT);
        map.put("tag", tagId);
        map.put("targetEntity", targetEntity);
        map.put("targetType", targetType);
        mts.insertRow(db, map, "tagassignment");
    }

    @Override
    public void transferEmployeeInUserAccount(String shortname) {
        Connection con = null;
        Database dbWoody = databases.get("mixing");
        try {
            con = dbWoody.getConnection();
            String sql =         "SELECT  `useraccountSave`.`login_numberOfLogins`, `useraccountSave`.`login_passwordHash`, " +
            "`useraccountSave`.`login_salt`,`useraccountSave`.`permissions_permissionString`," +
            "`useraccountSave`.`login_ucasePasswordHash` FROM `woody`.`useraccountSave` Where Employee_shortname = 'gha';";
            PreparedStatement select = con.prepareStatement(sql);

            ResultSet rs = select.executeQuery();
            Boolean error = null;
            while(rs.next()) {
                int login_numberOfLogins = rs.getInt("login_numberOfLogins");
                String login_passwordHash = rs.getString("login_passwordHash");
                String login_salt = rs.getString("login_salt");
                String permissions_permissionString = rs.getString("permissions_permissionString");
                String login_ucasePasswordHash = rs.getString("login_ucasePasswordHash");

                sql = "update woody.useraccount set login_numberOfLogins = " + login_numberOfLogins +
                                           ", login_passwordHash = '" + login_passwordHash +"', login_salt = '" + login_salt +
                                           "', permissions_permissionString = '" + permissions_permissionString +"', " +
                                           "login_ucasePasswordHash = '" + login_ucasePasswordHash + "' where Employee_shortName = 'gha';";

                PreparedStatement upDate = con.prepareStatement(sql);
                error = upDate.execute();
            }
            con.close();
            System.out.println("Fehler beim Update UserAccount für gha: " + error.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateSequenceCounter(String nameCrm,  String nameWoody, String function) {

        if (nameWoody == null) {
            nameWoody = nameCrm;
        }

        Database dbCrm = databases.get("crm");
        Database dbWoody = databases.get("mixing");
        String nextId = null;
        Connection conCrm = null;
        Connection conWoody = null;
        try {
            conCrm = dbCrm.getConnection();
            conWoody = dbCrm.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        switch (function) {
            case "sequencecounter": {
                // read the nextId from the sequencecounter
                try {

                    String sql = "SELECT `serialnumbercounter`.`nextId` FROM `crm`.`serialnumbercounter` " +
                                 "WHERE  `serialnumbercounter`.`idKey`= '" + nameCrm + "';";
                    PreparedStatement statement = conCrm.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        nextId = rs.getString("nextId");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "table": {
                if ("company".equals(nameCrm)) {
                    // calculate the nextId from the max. customerNumber als max. customerNumber + 1
                    // read all companies in a list ordered desc by customerNumber
                    List<Company> companyList = oma.select(Company.class).orderDesc(Company.CUSTOMER_NUMBER).queryList();
                    // throw out the company Haufler_Test
                    List<Company> companyListSelected = new ArrayList();
                    for (Company company : companyList) {
                        if (!"Haufler_Test".equals(company.getName())) {
                            companyListSelected.add(company);
                        }
                    }
                    // get the real max. customerNumber
                    String lastCustomerNumber = companyListSelected.get(0).getCustomerNumber();
                    Long value = Long.parseLong(lastCustomerNumber);
                    // add 1 --> this is the nextId
                    value = value + 1;
                    nextId = value.toString();
                }
                break;
            }
        }
        try {
            // check: is a nextId present?
            if (nextId == null) {
                throw new Exception("Der counter bei nameCrm = " + nameCrm + " ist null.");
            }
            // update the sequencecounter
            String sql = "update `woody`.`sequencecounter` SET `sequencecounter`.`nextValue` = " + nextId +
                         "  WHERE `sequencecounter`.`name` = '" + nameWoody + "';";
            PreparedStatement statement = conWoody.prepareStatement(sql);
            boolean error = statement.execute();
            System.out.println("Update sequenceCounter, name = '"+ nameWoody + "' , value = "
                               + nextId + ", Fehler = " + error);
            // close the connections
            conCrm.close();
            conWoody.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * creates a HashMap with all datas of a tag and insert the HashMap into the table 'tag'
     */
    private Long createTag(Database dbWoody, String name, Long idT, String targetType) {
        HashMap<String, Object> map = new HashMap();
        idT = idT + 1;
        map.put("id", idT.toString());
        map.put("targetType", targetType);
        map.put("name", name);
        map.put("uniqueName", mts.buildUniqueName(name));
        map.put("tenant", mts.getTENANT());
        mts.prepareTraceData(map, null, null);
        mts.insertRow(dbWoody, map, "tag");
        return idT;
    }

    /**
     * reads the actual Id from the given clazz
     */
    private Long readActualTableId(Database dbWoody, Class clazz) {
        try {
            Optional opt = oma.select(clazz).orderDesc(Column.named("id")).first();
            if(opt.isPresent()) {
                  if(Tag.class.equals(clazz)) {
                      Tag tag = (Tag) opt.get();
                      return tag.getId();
                  }
                if(TagAssignment.class.equals(clazz)) {
                    TagAssignment tagAssignment = (TagAssignment) opt.get();
                    return tagAssignment.getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * read the crmData from the given crmTable where the id in the table is > maxId
     */
    private List<Row> readTheRowList(String crmTable, Database dbCrm, Long maxId) {
        String sql = "SELECT * FROM " + crmTable + " c ";
        if(maxId  >= 0) {
            sql = sql + " WHERE c.id > " + NLS.toUserString(maxId) + " order by c.id limit 1000";
        } else {
            sql = sql + "";
        }
        List<Row>rowList = null;
        try {
            rowList = dbCrm.createQuery(sql).queryList();
        } catch (SQLException e) {
            e.printStackTrace();
            // ToDo exception anzeigen
            throw Exceptions.createHandled()
                            .withNLSKey("MigrationsServiceBean.errorReadFromCompany").set("maxId", maxId)
                            .handle();
        }
        return rowList;
    }

    /**
     * reads all given types (by name) from each row and store them in a HashMap 'generalTypeMap'
     */
    private Long storeTypeTags(List<Row> rowList, Long maxId, String names) {
        String[] fields = names.split(",");
        for(Row row : rowList) {
            Long id = row.getValue("id").getLong();
            if(id > maxId) {
                maxId = id;
            }
            for(int i = 0; i < fields.length; i++) {
                String name = fields[i].trim();
                String type = row.getValue(name).getString();
                if (Strings.isFilled(type)) {
                    generalTypeMap.put(type, type);
                }
            }
        }
        return maxId;
    }

}
