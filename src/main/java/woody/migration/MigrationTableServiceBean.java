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
import woody.offers.Offer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Register(classes = {MigrationTableService.class})
public class MigrationTableServiceBean implements MigrationTableService {
    @Part
    private static Databases databases;

    @Part
    private static OMA oma;

    private static final String TENANT = "1";

    private HashMap<String, String> generalTypeMap = new HashMap();


    @Override
    public void migrateCrmDataToWoody(String crmTable, String woodyTable) {
        if(woodyTable == null) {
            woodyTable = crmTable;
        }
        System.out.println("Start Migration " + crmTable + " --> " + woodyTable );
        Database dbCrm = databases.get("crm");
        Database dbWoody = databases.get("mixing");

        // Delete the content of the woody-table
        deleteContentOfTable(dbWoody, woodyTable);

        // read the data from the crmTable and migrate them to woody
        Long maxId = 0L;
        boolean finished = false;
        do  {
            List<Row> rowList = readTheRowList(crmTable, dbCrm, maxId);
            Tuple<Long, Long> tuple = null;
            if(rowList.size() > 0) {

                for(Row row : rowList) {
                    Long id = row.getValue("id").asLong(-1);
                    if(id > maxId) {
                        maxId = id;
                    }
                    HashMap<String, Object> map = new HashMap();

                    map.put("id", id.toString());
                    String function = "full";
                    switch (crmTable) {
                        case "tag":
                            map = buildTagMap(row, map);
                            break;
                        case "company": map = buildCompanyMap(map, row);
                            break;
                        case "person": map = buildPersonyMap(map, row);
                            break;
                        case "product": map = buildProductMap(map, row);
                            break;
                        case "packageDefinition": map = buildPackageDefinitionMap(map, row);
                            break;
                        case "employee": {
                            map = buildUserAccountMap(map, row);
                            break;
                        }
                        case "contract": map = buildContractMap(map, row);
                            break;
                        case "offer": {
                            map = buildOfferMap(map, row);
                            function = "offer";
                            break;
                        }
                        case "offerItem": {
                            map = buildOfferItemMap(map, row);
                            function = "offerItem";
                            break;
                        }
                        case "opportunity":
                            map = buildOpportunityMap(map, row);
                            break;
                        case "comment":
                            function = "comment";
                            map = buildCommentMap(map, row);
                            break;
                        case "industry":
                            map = buildIndustryMap(map, row);
                            break;
                        default:
                            System.out.println("   Die Verarbeitung der Tabelle '" + crmTable + "' fehlt!");
                            break;
                    }
                    prepareTraceData(map, row, function);
                    insertRow(dbWoody, map, woodyTable);
                }

                System.out.println("   Migration der Tabelle " + crmTable + ", letzte id: " + maxId.toString());
            } else {
                finished = true;
            }
        } while ( ! finished);

        System.out.println("Ende Migration der Tabelle " + crmTable + ", letzte id: " + maxId.toString());
    }

    private HashMap<String,Object> buildIndustryMap(HashMap<String, Object> map, Row row) {
        map.put("tenant", TENANT);
        rowFetch(map, row,"id", null);
        rowFetch(map, row, "name", null);
        return map;
    }

    private HashMap<String,Object> buildCommentMap(HashMap<String, Object> map, Row row) {
        map.put("personEntity", "noPersonEntity");
        map.put("personName", "noPersonName");
        map.put("publicVisible", "1");
        rowFetch(map, row, "comment", "textContent");
        rowFetch(map, row, "targetname", "targetEntity");
        String date = row.getValue("date").getString();
        date = date.replace(" ", "T");
        Long epoch = translateLocalDateTimeToEpoch(date);
        map.put( "tod", epoch.toString());
        return map;
    }

    private HashMap<String,Object> buildOpportunityMap(HashMap<String,Object> map, Row row) {
        rowFetch(map, row, "company", null);
        rowFetch(map, row, "contractValue", null);
        rowFetch(map, row, "nextInteraction", null);
        rowFetch(map, row, "nextInteraction", null);
        rowFetch(map, row, "product", null);
        rowFetch(map, row, "source", null);
        String state = row.getValue("state").getString();
        String stateText = null;
        switch (state) {
            case "0":
                stateText = "OPEN";
                break;
            case "1":
                stateText = "ACCEPTED";
                break;
            case "2":
                stateText = "REJECTED";
                break;
            case "3":
                stateText = "CLOSED";
                break;
            default:
                stateText = "Fehler";
                break;
        }
        map.put( "state", stateText);
        rowFetch(map, row, "employee", "userAccount");
        return map;
    }

    private HashMap<String,Object> buildOfferItemMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "acceptanceDate", null);
        rowFetch(map, row, "accountingDate", null);
        rowFetch(map, row, "accountingInterval", null);
        rowFetch(map, row, "baseProduct", null);
        rowFetch(map, row, "cyclicPrice", null);
        rowFetch(map, row, "developeDate", null);
//        rowFetch(map, row, "flagOneTimeHistory", null);
        rowFetch(map, row, "keyword", null);
        rowFetch(map, row, "offer", null);
        rowFetch(map, row, "offerDate", null);
        rowFetch(map, row, "offerItemType", null);
        rowFetch(map, row, "orderDate", null);
        rowFetch(map, row, "packageDefinition", null);
        rowFetch(map, row, "position", null);
        rowFetch(map, row, "price", null);
        rowFetch(map, row, "priceBase", null);
        rowFetch(map, row, "quantity", null);
        rowFetch(map, row, "quantityUnit", null);
        if("Monat".equals(map.get("quantityUnit"))) {
            map.put("quantityUnit", "MONTH");
        }
        rowFetch(map, row, "salesConfirmationDate", null);
        rowFetch(map, row, "singlePrice", null);

        rowFetch(map, row, "state", null);
        rowFetch(map, row, "text", null);
        return map;
    }

    private HashMap<String,Object> buildOfferMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "buyer", null);
        rowFetch(map, row, "company", null);
        rowFetch(map, row, "accountingInterval", null);
        rowFetch(map, row, "date", null);
        rowFetch(map, row, "keyword", null);
        rowFetch(map, row, "number", null);
        rowFetch(map, row, "person", null);
        rowFetch(map, row, "reference", null);
        rowFetch(map, row, "state", null);
        rowFetch(map, row, "employee", null);
        return map;
    }

    private HashMap<String,Object> buildContractMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "accountedTo", null);
        rowFetch(map, row, "accountingGroup", null);
        rowFetch(map, row, "accountingInterval", null);
        rowFetch(map, row, "quantity", "amount");
        rowFetch(map, row, "company", null);
        rowFetch(map, row, "contractPartner", null);
        rowFetch(map, row, "discountAbsolute", null);
        rowFetch(map, row, "discountPercent", null);
        rowFetch(map, row, "endDate", null);
        rowFetch(map, row, "noAccounting", null);
        rowFetch(map, row, "packageDefinition", null);
        rowFetch(map, row, "position", null);
        rowFetch(map, row, "signingDate", null);
        rowFetch(map, row, "singlePrice", null);
        rowFetch(map, row, "startDate", null);
        rowFetch(map, row, "singlePriceState", null);
        rowFetch(map, row, "unitPrice", null);
        rowFetch(map, row, "comments", null);
        return map;
    }

    private HashMap<String,Object> buildPackageDefinitionMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "accountingProcedure", null);
        rowFetch(map, row, "accountingUnit", null);
        rowFetch(map, row, "defaultPosition", null);
        rowFetch(map, row, "name", null);
        rowFetch(map, row, "packetType", "paketType");
        rowFetch(map, row, "parameter", null);
        rowFetch(map, row, "singlePrice", null);
        rowFetch(map, row, "product", null);
        rowFetch(map, row, "unitPrice", null);
        return map;
    }

    private HashMap<String,Object> buildProductMap(HashMap<String, Object> map, Row row) {
        map.put("tenant", TENANT);
        rowFetch(map, row, "article", null);
        rowFetch(map, row, "collectBugs", null);
        rowFetch(map, row, "description", null);
        rowFetch(map, row, "image", null);
        rowFetch(map, row, "name", null);
        rowFetch(map, row, "productType", null);
        return map;
    }

    private HashMap<String,Object> buildPersonyMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row,"id", null);
        rowFetch(map, row, "company", "company");
        rowFetch(map, row, "firstname", "person_firstname");
        rowFetch(map, row, "lastname", "person_lastname");
        rowFetch(map, row, "salutation", "person_salutation");
        rowFetch(map, row, "title", "person_title");
        rowFetch(map, row, "email", "contact_email");
        rowFetch(map, row, "faxNormalized", "contact_fax");
        rowFetch(map, row, "mobileNormalized", "contact_mobile");
        rowFetch(map, row, "phoneNormalized", "contact_phone");
        rowFetch(map, row, "birthday", null);
        rowFetch(map, row, "offline", null);
        rowFetch(map, row, "position", "position");
        rowFetch(map, row, "city", "address_city");
        rowFetch(map, row, "countryCode", "address_country");
        rowFetch(map, row, "street", "address_street");
        rowFetch(map, row, "zipCode", "address_zip");
        map.put("login_accountLocked",0);
        map.put("login_generatedPassword", "");
        map.put("login_lastLogin", 0);
        map.put("login_numberOfLogins", 0);
        map.put("login_passwordHash", "");
        map.put("login_salt", "");
        map.put("login_username", "");
        return map;
    }

    private  HashMap<String, Object> buildTagMap(Row row, HashMap<String, Object> map) {
        rowFetch(map, row, "application", "targetType");
        rowFetch(map, row, "name", null);
        map.put("uniqueName", buildUniqueName(map.get("name").toString()));
        map.put("tenant", TENANT);
        return map;
    }

    private  HashMap<String, Object> buildUserAccountMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "emailPassword", "Employee_emailPassword");
        map.put("login_accountLocked", 0);
        map.put("login_numberOfLogins", 0);
        map.put("login_passwordHash", "");
        map.put("login_salt", "");
        rowFetch(map, row, "shortname", "login_username");
        map.put("tenant", TENANT);
        map.put("login_ucasePasswordHash", "");
        rowFetch(map, row, "shortname", "Employee_shortname");
        rowFetch(map, row, "email", null);
        if(map.get("email") == null) {
            map.put("email", "no_email-address in the crm given");
        }
        rowFetch(map, row, "firstname", "person_firstname");
        rowFetch(map, row, "lastname", "person_lastname");
        rowFetch(map, row, "salutation", "person_salutation");
        rowFetch(map, row, "inaktiv", "Employee_inaktiv");
        rowFetch(map, row, "pbxId", "Employee_pbxId");
        String phone = row.getValue("pbxId").getString();
        phone = "+49 7151 90316-" + phone;
        map.put("Employee_phoneNr", phone);
        rowFetch(map, row, "endDate", "Employee_terminationDate");
        rowFetch(map, row, "signature", "Employee_signature");
        map.put("version", 0);
        return map;
    }

    /**
     * build a uniqueName -->transfer to lowerCase an replace all ä, ö, ü, ß
     */
    @Override
    public Object buildUniqueName(String name) {
        name = name.toLowerCase();
        name = name.replace("ä", "ae");
        name = name.replace("ü", "ue");
        name = name.replace("ö", "oe");
        name = name.replace("ß", "ss");
        return name;
    }


    private List<Row> readTheRowList(String crmTable, Database dbCrm, Long maxId) {
        String sql = "SELECT * FROM " + crmTable + " c WHERE c.id > " + NLS.toUserString(maxId) + " order by c.id limit 1000";
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



    @Override
    public void deleteContentOfTable(Database db, String table) {
        Connection con = null;
        try {
            con = db.getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM " + table);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            // ToDo Exception anzeigen
            throw Exceptions.createHandled()
                            .withNLSKey("MigrationsServiceBean.errorDeleteFromTable").set("table", table)
                            .handle();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // ToDo Exception anzeigen
                throw Exceptions.createHandled()
                                .withNLSKey("MigrationsServiceBean.errorCloseConnection").set("con", con.toString())
                                .handle();
            }
        }
    }

    /**
     * reads each company-data from the rowList an transfer them to woody/company
     */
    private HashMap<String, Object> buildCompanyMap(HashMap<String, Object> map, Row row) {

        // read the crm-data from the row and store them in a HashMap

        map.put("tenant", TENANT);
        rowFetch(map, row,"id", null);
        rowFetch(map, row, "name", "name");
        rowFetch(map, row,"name2", null);
        rowFetch(map, row, "city", "address_city");
        rowFetch(map, row, "countryCode", "address_country");
        rowFetch(map, row,"street", "address_street");
        rowFetch(map, row,"zipCode", "address_zip");
        rowFetch(map, row,"customerNr", null);
        rowFetch(map, row,"homepage", null);
        rowFetch(map, row,"image", null);
        rowFetch(map, row,"matchcode", null);
        rowFetch(map, row,"invoiceMedium", "companyAccountingData_invoiceMedium");
        rowFetch(map, row,"invoiceMailAdr", "companyAccountingData_invoiceMailAdr");
        rowFetch(map, row,"ptPrice", "companyAccountingData_ptPrice");
        rowFetch(map, row,"outputLanguage", "companyAccountingData_outputLanguage");

        // dataPrivacyPerson und dataPrivacySendDate werden im 2. Lauf der company-Migration bearbeitet.
//            rowFetch(map, row,"dataPrivacyPerson", null);
        rowFetch(map, row,"dataPrivacySendDate", null);

        // build the postbox-address-element - or not
        int sum = 0;
        sum = sum + checkIsValuePresent(row,"cityPostbox");
        sum = sum + checkIsValuePresent(row,"zipCodePostbox");
        sum = sum + checkIsValuePresent(row,"postbox");
        if(sum >0 && sum <3) {
            // some valueus for the postbox-address are missing
//                System.out.println("Fehler bei Firma: " + row.getValue("name").getString() + ", id = " + id.toString());
        }
        if (sum == 3) {
            // all values are present for the postbox-address
            rowFetch(map, row, "cityPostbox", "postboxAddress_city" );
            rowFetch(map, row, "zipCodePostbox", "postboxAddress_zip" );
            rowFetch(map, row, "countryCode", "postboxAddress_country" );

            // if only a number of a postbox is given, add the name 'postbox'
            String postbox = row.getValue("postbox").asString("");
            String postfachNamen = NLS.get("PostfachNamen");
            String[] postfach = postfachNamen.split(";");
            boolean addPostboxName = true;
            for(int i = 0; i < postfach.length; i++) {
                if(postbox.toLowerCase().contains(postfach[i])) {
                    addPostboxName = false;
                    break;
                }
            }
            if(addPostboxName) {
                String countryCode = row.getValue("countryCode").asString("de").toLowerCase();
                String postfachName = NLS.get("PostfachName_"+countryCode);
                if(Strings.isEmpty(postfachName)) {
                    postfachName = "Postbox";
                }
                postbox = postfachName + " " + postbox;
                map.put("postboxAddress_street", postbox);
//                    System.out.println(postbox);
            }
        }

//            // insert the row in the company-table
//            insertRow(dbWoody, id, map, woodyTable);
//
//            // add the tag-assignments
//            idT = prepareTagAssignment(dbWoody, idT, row, id, "companyType");
//            idT = prepareTagAssignment(dbWoody, idT, row, id, "businessType");
//        }
        return map;
    }

    /**
     * migrate the data for the TraceData from the crm-values, given in row
     */

    @Override
    public void prepareTraceData(HashMap<String, Object> map, Row row, String function) {
        if(row == null) {
            map.put("trace_createdIn", "Migration");
            map.put("trace_createdBy", "Migration");
            Long date =  new Date().getTime();
            String dateString = date.toString().concat("000");
            map.put("trace_createdAt", dateString);
            return;
        }
        switch (function) {
            case "full": {
                String date = row.getValue("createdDateLong").getString().concat("000");
                map.put("trace_createdAt", date);
                if (row.getValue("createdInSystem").isFilled()) {
                    rowFetch(map, row, "createdInSystem", "trace_createdIn");
                } else {
                    map.put("trace_createdIn", "???");
                }
                rowFetch(map, row, "createdByUser", "trace_createdBy");
                rowFetch(map, row, "lastChangedByUser", "trace_changedBy");
                rowFetch(map, row, "lastChangedInSystem", "trace_changedIn");

                // these crm-dates are given as seconds from the epoch (1.1.1970)
                // in woody, the LocalDateTime needs millisec.
                date = row.getValue("lastChangedDateLong").getString().concat("000");
                map.put("trace_changedAt", date);
                break;
            }

            case "offer": {
                String date = row.getValue("date").getString();  //YYYY-MM-DD
                date = date + "T12:00:00";
                Long epoch = translateLocalDateTimeToEpoch(date);
//                LocalDateTime xxx = LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
                map.put("trace_createdAt", epoch.toString());
                map.put("trace_createdIn", "Migration");
                map.put("trace_createdBy", "Migration");
                break;
            }

            case "offerItem": {
                String date = row.getValue("offerDate").getString();  //YYYY-MM-DD
                if(date == null) {
                    Long idOffer = row.getValue("offer").getLong();
                    Optional optional = oma.find((Offer.class), idOffer);
                    Offer offer = (Offer)optional.get();
                    LocalDate ldt = offer.getDate();
                    date = ldt.toString();
                }
                date = date + "T12:00:00";
                Long epoch = translateLocalDateTimeToEpoch(date);
                map.put("trace_createdAt", epoch.toString());
                map.put("trace_createdIn", "Migration");
                map.put("trace_createdBy", "Migration");
                break;
            }
            case "comment": {
                return;
            }

        }

    }

    /**
     * inserts a row in the given table
     * @param dbWoody  database
     * @param map      data of the row in a HashMap
     * @param table    nmae of the table
     */
    @Override
    public void insertRow(Database dbWoody, HashMap<String, Object> map, String table) {
        try {
            Row rowWoody = dbWoody.insertRow(table, map);
        } catch (SQLException e) {
            e.printStackTrace();
            // ToDo Exception anzeigen
            String text = "nicht vorhanden";
            Long id = (Long)map.get("id");
            if(id != null) {
                text = id.toString();
            }
            throw Exceptions.createHandled()
                            .withNLSKey("MigrationsServiceBean.errorInsertRow").set("table", table)
                            .set("id", text).handle();
        }
    }

    @Override
    public String getTENANT() {
        return TENANT;
    }


    /**
     * translate a LocalDateTime-String (given as yyy-MM-dddThh:mm:ss) in the seconds of epoch
     */
    private Long translateLocalDateTimeToEpoch(String date) {
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE_TIME;  // yyyy-MM-ddThh:mm:ss
        LocalDateTime ldt = LocalDateTime.parse(date, df);
        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of(ZoneId.systemDefault().toString());
        ZonedDateTime zdt = now.atZone(zoneId);
        ZoneOffset offset = zdt.getOffset();
        return ldt.toEpochSecond(offset);
    }


    /**
     * @return 0 if the value is not present, returns 1 if the value is present
     */
    private int checkIsValuePresent(Row row, String name) {
        int count = 0;
        if(row.hasValue(name)) {
            if(row.getValue(name).isFilled()) {
                count = 1;
            }
        }
        return count;
    }

    /**
     * reads the given value (nameCrm) from the row and store the value (String) in the HashMap with the key 'nameWoody'
     */
    private void rowFetch(HashMap<String, Object> map, Row row, String nameCrm, String nameWoody) {
        String data = null;
        if(row.hasValue(nameCrm)) {
            if(nameWoody == null) {
                nameWoody = nameCrm;
            }
            if(row.getValue(nameCrm).isFilled()) {
                data = row.getValue(nameCrm).asString();
                // adaption from true / false to 1 / 0
                if("true".equals(data)) {
                    data = "1";
                }
                if("false".equals(data)) {
                    data = "0";
                }
                map.put(nameWoody, data);
            }
        }
    }

}
