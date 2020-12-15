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
import sirius.db.mixing.Constraint;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import woody.offers.Offer;
import woody.opportunities.Opportunity;
import woody.xrm.Company;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
            // read the rowList from the crmTable, starting at maxId
            List<Row> rowList = readTheRowList(crmTable, dbCrm, maxId);
            Tuple<Long, Long> tuple = null;
            if(rowList.size() > 0) {
                // for each row in the rowList
                for(Row row : rowList) {
                    // get the id and calculate the maxId
                    Long id = row.getValue("id").asLong(-1);
                    if(id > maxId) {
                        maxId = id;
                    }
                    // build a HashMap and store the id
                    HashMap<String, Object> map = new HashMap();
                    map.put("id", id.toString());
                    // add more data to the HashMap
                    String function = "full";
                    switch (crmTable) {
                        case "tag":
                            map = buildTagMap(row, map);
                            break;
                        case "company": map = buildCompanyMap(map, row);
                            break;
                        case "campaign": map = buildCampaignMap(map, row);
                            break;
                        case "person": map = buildPersonMap(map, row);
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
                            function = "nothing";
                            map = buildCommentMap(map, row);
                            break;
                        case "industry":
                            map = buildIndustryMap(map, row);
                            break;
                        case "mailtemplate":
                            map = buildMailtemplateMap(map, row);
                            break;
                        case "opportunityStateChanges":
                            function = "nothing";
                            map = buildOpportunityStateChangesMap(map, row);
                            break;
                        default:
                            System.out.println("   Die Verarbeitung der Tabelle '" + crmTable + "' fehlt!");
                            break;
                    }
                    // add the traceData to the HashMap
                    prepareTraceData(map, row, function);
                    // insert the data in the woody-table
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
        rowFetch(map, row, "person", null);
        rowFetch(map, row, "contractValue", null);
        rowFetch(map, row, "nextInteraction", null);
        rowFetch(map, row, "product", null);
        rowFetch(map, row, "source", null);
        rowFetch(map, row, "oldState", null);
        rowFetch(map, row, "newState", null);
        rowFetch(map, row, "sortDate", null);
        rowFetch(map, row, "sortValue", null);
        rowFetch(map, row, "employee", "userAccount");
        return map;
    }

    private HashMap<String,Object> buildOfferItemMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "acceptanceDate", null);
        rowFetch(map, row, "accountingDate", null);
        rowFetch(map, row, "accountingInterval", null);
        rowFetch(map, row, "cyclicPrice", null);
        rowFetch(map, row, "developeDate", "completionDate");
        rowFetch(map, row, "keyword", null);
        rowFetch(map, row, "offer", null);
        rowFetch(map, row, "offerDate", null);
        rowFetch(map, row, "offerItemType", null);
        rowFetch(map, row, "orderDate", null);
        rowFetch(map, row, "packageDefinition", null);
        rowFetch(map, row, "position", null);
        rowFetch(map, row, "priceBase", null);
        rowFetch(map, row, "quantity", null);
        rowFetch(map, row, "salesConfirmationDate", null);
        rowFetch(map, row, "singlePrice", null);
        rowFetch(map, row, "state", null);
        rowFetch(map, row, "text", null);
        rowFetch(map, row, "history", null);
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
        if(map.get("paketType") == null) {
            // set the default
            map.put("paketType", "STANDARD");
        }
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

    private HashMap<String,Object> buildPersonMap(HashMap<String, Object> map, Row row) {
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
        rowFetch(map, row, "offline", "quit");
        rowFetch(map, row, "position", "position");
        if(checkIntegrityOfFields(row, "city, countryCode, street, zipCode")) {
            rowFetch(map, row, "city", "address_city");
            rowFetch(map, row, "countryCode", "address_country");
            rowFetch(map, row, "street", "address_street");
            rowFetch(map, row, "zipCode", "address_zip");
        }
        map.put("login_accountLocked",0);
        map.put("login_generatedPassword", "");
        map.put("login_lastLogin", 0);
        map.put("login_numberOfLogins", 0);
        map.put("login_passwordHash", "");
        map.put("login_salt", "");
        map.put("login_username", "");
        map.put("uniquePath", "");
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
        map.put("tenant", TENANT);
        map.put("login_ucasePasswordHash", "");

        rowFetch(map, row, "shortname", "login_username");
        rowFetch(map, row, "shortname", "Employee_shortname");
        // test-user einrichten
        if("gha".equals(map.get("Employee_shortname"))) {
            map.put("login_passwordHash", "Wv6eWmKjtzeiKwfPAs0yMA==");
            map.put("login_salt", "gurjeii15aq4sm8p71su");
            map.put("permissions_permissionString","administrator,xrm,offers,user-administrator,tasks");
        }

        rowFetch(map, row, "email", "email");
        if(map.get("email") == null) {
            map.put("email", "no_email-address in the crm given");
        }
        rowFetch(map, row, "firstname", "person_firstname");
        rowFetch(map, row, "lastname", "person_lastname");
        rowFetch(map, row, "salutation", "person_salutation");
        rowFetch(map, row, "inaktiv", "Employee_inaktiv");
        rowFetch(map, row, "pbxId", "Employee_phoneExtension");

        String phone = row.getValue("pbxId").getString();
        phone = "+49 7151 90316-" + phone;
        map.put("Employee_phoneNr", phone);

        rowFetch(map, row, "endDate", "Employee_terminationDate");
        rowFetch(map, row, "signature", "Employee_signature");
        map.put("version", 0);
        return map;
    }

    private HashMap<String,Object> buildMailtemplateMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "name", null);
        rowFetch(map, row, "mailcontent", null);
        rowFetch(map, row, "subject", null);
        migrateVelocityToTagliatelle(map, "mailcontent");
        return map;
    }

    private HashMap<String,Object> buildOpportunityStateChangesMap(HashMap<String, Object> map, Row row) {
        rowFetch(map, row, "opportunity", null);
        rowFetch(map, row, "datetime", null);
//        String s = (String)map.get("datetime");
//        String[] fields = s.split(" ");
//        // the dateTime-format is yyyy-mm-ddThh:mm:ss
//        s = fields[0] + "T" + fields[1].substring(0, 8);
//        map.put("datetime", s);
        rowFetch(map, row, "oldState", null);
        rowFetch(map, row, "newState", null);
        rowFetch(map, row, "employee", "userAccount");
        return map;
    }

    /**
     * migrates a Velocity-mailtemplate into a tagliatelle-mailtemplate
     */
    private void migrateVelocityToTagliatelle(HashMap<String, Object> map, String key) {
        String value = (String) map.get(key);
        if (Strings.isEmpty(value)) {
            return;
        }
        // Look for the arguments which start with a $-sign
        List<String> argumentList = new ArrayList();
        int index = 0;
        do {
            int indexStart = value.indexOf("$", index);
            if(indexStart < 0) {
                break;
            }
            index = indexStart + 1;
            // the end of a argument is a space " " or a line feed ("\n"
            int indexEnd1 = value.indexOf(" ", indexStart + 1);
            int indexEnd2 = value.indexOf("\n", indexStart + 1);
            int indexEnd = -1;
            if(indexEnd1 > 0 && indexEnd2 > 0) {
                if(indexEnd1 <= indexEnd2) {
                    indexEnd = indexEnd1;
                } else {
                    indexEnd = indexEnd2;
                }
            }
            if(indexEnd1 > 0 && indexEnd2 < 0) {
                indexEnd = indexEnd1;
            }
            if(indexEnd2 > 0 && indexEnd1 < 0) {
                indexEnd = indexEnd2;
            }
            if(indexEnd < 0) {
                indexEnd = value.length();
            }
            String argument = value.substring(indexStart + 1, indexEnd);
            argumentList.add(argument);
        } while(1==1);

        // replace the $ to @
        value = replaceAll(value, "$", "@");
        value = value.trim();

        value = replaceAll(value, "@person.letterSalutation", "@person.getLetterSalutation()");

        // build the t<i:arg - tag for the tagliatelle-mailtemplate
        String targetString = "";
        for(String variable : argumentList) {
            int indexP = variable.indexOf(".");
            if(indexP > 0) {
                variable = variable.substring(0, indexP);
            }
            String type = "String";
            if(value.startsWith("@person.")) {
                type = "woody.xrm.Person";
            }
            targetString = targetString + "<i:arg type=\"" + type + "\" name=\""+ variable + "\"/>" + "\n";
        }
        targetString = targetString + value;
        map.put(key, targetString);
    }

    /**
     * replaces in the given value all strings <source> with the string <target>
     */
    private String replaceAll(String value, String source, String target) {
        String targetString = "";
        int lastindex = 0;
        do {
           int index = value.indexOf(source, lastindex);
            if(index  < 0) {
                targetString = targetString + value.substring(lastindex, value.length());
                break;
            } else {
                if(index-1 > 0) {
                    targetString = targetString + value.substring(lastindex, index);
                }
                targetString = targetString + target;
                lastindex = index + source.length();
            }
        } while (1==1);
        return targetString;
    }

    /**
     * build a uniqueName -->transfer the name to lowerCase an replace all ä, ö, ü, ß
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

    /**
     * read the data from the given crmTable from id > maxId in a rowList
     */
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
            System.out.println("     Datenbank: " + db.toString() + ", Tablle: " + table + " löschen.");
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

    private HashMap<String, Object> buildCampaignMap(HashMap<String, Object> map, Row row) {
        map.put("tenant", TENANT);
        rowFetch(map, row,"id", null);
        rowFetch(map, row,"name", null);
        rowFetch(map, row,"endDate", null);
        rowFetch(map, row,"startDate", null);
        rowFetch(map, row,"description", null);
        String s =  (String) map.get("description");
        if(!Strings.isEmpty(s)) {
            int count = s.length();
            if (count > 254) {
                int ggg = 3;
            }
        }
        rowFetch(map, row,"employee", "userAccount");
        return map;
    }

    private HashMap<String, Object> buildCompanyMap(HashMap<String, Object> map, Row row) {
        map.put("tenant", TENANT);
        rowFetch(map, row,"id", null);
        rowFetch(map, row, "name", null);
        rowFetch(map, row,"name2", null);
        if(checkIntegrityOfFields(row, "city, countryCode, street, zipCode")) {
            rowFetch(map, row, "city", "address_city");
            rowFetch(map, row, "countryCode", "address_country");
            rowFetch(map, row, "street", "address_street");
            rowFetch(map, row, "zipCode", "address_zip");
        }
        rowFetch(map, row,"customerNr", "customerNumber");
        rowFetch(map, row,"homepage", "website");
        rowFetch(map, row,"image", null);
        rowFetch(map, row,"matchcode", null);
        rowFetch(map, row,"invoiceMedium", "companyAccountingData_invoiceMedium");
        rowFetch(map, row,"invoiceMailAdr", "companyAccountingData_invoiceMailAdr");
        rowFetch(map, row,"ptPrice", "companyAccountingData_ptPrice");
        rowFetch(map, row,"outputLanguage", "companyAccountingData_outputLanguage");
        rowFetch(map, row,"dataPrivacySendDate", "dataPrivacyCompanyData_dataPrivacySendDate");
        rowFetch(map, row,"dataPrivacyReceivingDate", "dataPrivacyCompanyData_dataPrivacyReceivingDate");
        rowFetch(map, row,"dataPrivacyAvvVersionDate", "dataPrivacyCompanyData_dataPrivacyAvvVersionDate");

        // build the postbox-address-elements
        // check whether all postbox-data are present
        if(checkIntegrityOfFields(row, "cityPostbox, zipCodePostbox, postbox, countryCode")) {
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
                String postfachName = NLS.get("PostfachName_" + countryCode);
                if(Strings.isEmpty(postfachName)) {
                    postfachName = "Postbox";
                }
                postbox = postfachName + " " + postbox;
                map.put("postboxAddress_street", postbox);
            } else {
                rowFetch(map, row, "postbox", "postboxAddress_street");
            }
        } else {
            // check whether the city, countryCode and zipCode are present ---> the postbox-number is the zipCode
            // set the postboxAddress_street to "Postfach"
            if(checkIntegrityOfFields(row, "cityPostbox, zipCodePostbox, countryCode")) {
                rowFetch(map, row, "cityPostbox", "postboxAddress_city" );
                rowFetch(map, row, "zipCodePostbox", "postboxAddress_zip" );
                rowFetch(map, row, "countryCode", "postboxAddress_country" );
                String countryCode = row.getValue("countryCode").asString("de").toLowerCase();
                String postfachName = NLS.get("PostfachName_" + countryCode);
                if(Strings.isEmpty(postfachName)) {
                    postfachName = "Postbox";
                }
                map.put("postboxAddress_street", postfachName);
            }
        }
        return map;
    }

    /**
     * check the integrity of the given filelds in the given row
     * @param row: row
     * @param fieldsAsString: field-names, separated by comma, e.g "field1 , field2, field3"
     * @return true if all fields are filled, otherwise false
     */
    private boolean checkIntegrityOfFields(Row row, String fieldsAsString) {
        boolean success = true;
        String[] fields = fieldsAsString.split(",");
        for(int i = 0; i<fields.length; i++) {
            String value = row.getValue(fields[i].trim()).getString();
            if(Strings.isEmpty(value)) {
                success = false;
            }
        }
        if(!success) {
            int sum = 0;
            boolean print = false;
            if(row.hasValue("cityPostbox")) {
                if(row.getValue("cityPostbox").isFilled()) {
                    for(int k = 0; k<fields.length; k++) {
                        if(fields[k].trim().equals("cityPostBox")) {
                            print = true;
                        }
                    }
                }
            } else {
                for (int k = 0; k < fields.length; k++) {
                    if (row.getValue(fields[k].trim()).getString() != null) {
                        sum = sum++;
                    }
                }
                if (sum >= 2) {
                    print = true;
                }
            }
            if(print) {
                for (int k = 0; k < fields.length; k++) {
                    if (k == 0) {
                        String name = "noName";
                        if (row.hasValue("name")) {
                            name = row.getValue("name").getString();
                        } else {
                            if (row.hasValue("lastname")) {
                                name = row.getValue("lastname").getString();
                                if (row.getValue("firstname").isFilled()) {
                                    name = name + ", " + row.getValue("firstname").getString() + ": ";
                                }
                            } else {
                                name = "id: " + row.getValue("id").getString();
                            }
                        }
                        System.out.print("     " + name + ": " + fields[k] + "= " + row.getValue(fields[k].trim()).getString());
                    } else {
                        System.out.print(", " + fields[k] + "= " + row.getValue(fields[k].trim()).getString());
                    }
                    if (k == fields.length - 1) {
                        System.out.println(" ");
                    }
                }
            }
        }
        return success;
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
            case "nothing": {
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

    @Override
    public void saveAllOpportunities() {
        System.out.println("Start saveAllOpportunities");
        List<Opportunity> list = oma.select(Opportunity.class).queryList();
        for(Opportunity opportunity :list) {
            oma.update(opportunity);
        }
        System.out.println("Ende saveAllOpportunities");
    }

    /**
     * translate a LocalDateTime-String (given as yyyy-MM-ddThh:mm:ss) in the seconds of epoch
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

    @Override
    public void addDataprivacyPersons() {
        System.out.println("Start addDataPrivacyPerson");
        Database databaseCrm = databases.get("crm");
        Long maxId = 0L;
        boolean finish = false;
        // read the crm-table company
        do {
            List<Row> rowList = readTheRowList("company", databaseCrm, maxId);
            if(rowList.size() == 0) {
                finish = true;
            } else {
                try {
                    // read each row
                    for(Row row : rowList) {
                        // is a id of the dataPrivacyPerson present?
                        Long dataPrivacyPersonId = row.getValue("dataPrivacyPerson").getLong();
                        if(dataPrivacyPersonId != null) {
                            // get the id of the company
                            Long id = row.getValue("id").getLong();
                            // get the company by id
                            Optional opt = oma.find(Company.class, id);
                            if(opt.isPresent()) {
                                Company company = (Company) opt.get();
                                // set the dataPrivacyPerson and save the company
                                company.getDataPrivacyCompanyData().getDataPrivacyPerson().setId(dataPrivacyPersonId);
                                oma.update(company);
                            } else {
                                // ToDo vernünftig anzeigen
                                throw new Exception("Company mit id = " + id.toString() + " nicht gefunden.");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } while (finish);
        System.out.println("Ende addDataPrivacyPerson");
    }

    @Override
    public void deleteDataPrivacyPersonsInCompanies() {
        System.out.println("Start deleteDataPrivacyPersonsInCompanies");
        List<Company> companyList = oma.select(Company.class).queryList();
        for(Company company : companyList) {
            if(company.getDataPrivacyCompanyData().getDataPrivacyPerson() != null) {
                //set the dataPrivacyPerson to null
                company.getDataPrivacyCompanyData().getDataPrivacyPerson().setId(null);
                // delete all address-data to avoid a exception.
                company.getAddress().setCountry(null);
                company.getAddress().setCity(null);
                company.getAddress().setStreet(null);
                company.getAddress().setZip(null);
                company.getPostboxAddress().setCountry(null);
                company.getPostboxAddress().setCity(null);
                company.getPostboxAddress().setStreet(null);
                company.getPostboxAddress().setZip(null);
                //  System.out.println(company.toString());
                oma.update(company);
            }
        }
        System.out.println("Ende deleteDataPrivacyPersonsInCompanies");
    }


}
