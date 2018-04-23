/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.phoneCalls;

import sirius.biz.model.ContactData;
import sirius.biz.model.PersonData;
import sirius.biz.tenants.UserAccount;
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
import sirius.kernel.health.console.Command;
import sirius.kernel.nls.NLS;
import sirius.kernel.timer.EveryMinute;
import woody.core.employees.Employee;
import woody.xrm.Person;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Register(name = "fetchPhoneCalls", classes = {PhoneCallImporter.class, EveryMinute.class, Command.class})
public class PhoneCallImporterBean implements EveryMinute, Command {
    /**
     * <code>
     * <p>
     * Quelle: Starface Wiki 6.2
     * https://knowledge.starface.de/pages/viewpage.action?pageId=3574412
     * <p>
     * <p>
     * Feldname				Typ			Beschreibung
     * ---------------------------------------------------------------------------------------------------------------------------
     * answered				boolean		Ruf angenommen? (Statusfeld)
     * answeredelswhere		String		ID des Benutzers der einen Gruppenruf angenommen hat
     * callbacknumber		String		Nummer, die für Wahlwiederholung genutzt wird
     * callbacknumberextern	boolean		War es ein externer Ruf? (Statusfeld)  Achtung, es wird nur "extern" bewertet,
     * keine Aussage, ob es ein eingehender oder ein ausgehender Anruf war.
     * calledaccountid		int 		(id von Tabelle account, 0 für extern)	Interne Benutzer ID des Rufziels
     * calledcallerid		String		Name und Nummer des Rufziels
     * calleraccountid		int 		(id von Tabelle account, 0 für extern)	Interne Benutzer ID des Anrufenden
     * callercallerid		String		Name und Nummer des Anrufenden
     * callid				long		ID des Rufes
     * callleguuid			String 		(UUID)	ID des Calllegs
     * callresult			String 		(CONNECTED oder NOT_CONNECTED)	Status des Rufes in diesem Callstep
     * callresultcausedby	int			ID der Instanz die den Ruf behandelt hat
     * callresulttime		String 		(format: dd.MM.yyyy HH:mm:ss)	Endzeitpunkt des Rufes
     * callstepid			int			ID einer möglichen Umleitung/Weiterleitung
     * cdraccountid			int 		(id von Tabelle account)	Interne ID des Benutzers dem der Ruflisteneintrag zugeordnet ist
     * deleted				boolean		Aus Ruflisten gelöscht? (Statusfeld)
     * duration				long		Dauer des Rufes
     * hasfax				boolean		Fax generiert (Statusfeld)
     * hasmonitor			boolean		Mitschnitt generiert? (Statusfeld)
     * hasvoicemail			boolean		Voicemail Nachricht generiert? (Statusfeld)
     * id, auch cdrId		long		Fortlaufende Listen-ID
     * incoming				boolean		Wurde der Ruf angenommen? (Statusfeld)
     * lineid				int			ID der verwendeten Providerleitung
     * linename				String		Name der verwendeten Providerleitung
     * linktime				String 		(format: dd.MM.yyyy HH:mm:ss)	Annahmezeitpunkt des Rufes
     * login					String		Login ID des Benutzers zu dem der Datenbankeintrag gehört
     * privatecall			boolean		Privater Anruf (*2)? (Statusfeld)
     * ringingtime			String 		(format: dd.MM.yyyy HH:mm:ss)	Klingeldauer
     * serviceid				int			ID der Ruffunktion (siehe Tabelle unten)
     * starttime				String 		(format: dd.MM.yyyy HH:mm:ss)	Startzeitpunkt des Rufes
     * summarystep			boolean		Zusammengefasster Eintrag? (Statusfeld)
     * <p>
     * Die mit * gekennzeichneten Variablen werden vom PhoneCall-Importer ausgewertet.
     * <p>
     * Wichtig:
     * <p>
     * Das Flag "directionIn" (vom PhoneCall-Importer gebildet ist das einzige zuverlässige Merkmal um zwischen
     * ankommenden und abgehenden Rufen zu unterscheiden
     * </code>
     */

    private static final String PHONE_CALL_IMPORT_LAST_ID = "PhoneCallImportLastId";
    private static final String INITIALIZE_NORMALIZED_PHONE_NUMBERS_IN_PERSON =
            "InitializeNormalizedPhoneNumbersInPerson";

    //  Testflag auf false setzen
    /**
     * testflag = false ---> normal condition
     * testflag = true  ---> all CDR stored in the database
     */
    private final static boolean testflag = false;

    // Constants for the Column 'direction'
    private static final String OUT = "OUT";  // ausgehendes Gespträch, scireum --> extern
    private static final String IN = "IN";  // eingehendes Gespräch, extern --> scireum
    private static final String INTERN = "INTERN";  // internes Gespräch, scireum --> scireum

    // do not touch these variables
    private String lastCaller = "";
    private String lastCalled = "";
    private LocalDateTime lastDate = LocalDateTime.now();
    private long lastCallId = 0;
    private long changeCdr = 190000;
    private String lastPhoneCallString = "";

    @Part
    private static OMA oma;
    @Part
    private static Databases databases;

    // ToDo Überlegen, wie das wieder implementiert wird
//	@Override
//	public void runTimer() throws Exception {
//		fetchPhoneCallsNew();
//		if(testflag)  {
//			fetchPhoneCallsNew();
//		} else {
//			if (Model.isDebugEnvironment()) {
//				CRM.LOG.INFO("Not fetching any phoneCalls in the debug system!");
//				return;
//			}
//			fetchPhoneCallsNew();
//		}
//	}

    //Methode for starface-Software 2016
    public int fetchPhoneCalls() throws Exception {

        List<String> list = databases.getDatabases();

        int numFetched = 0;
        lastCaller = "";
        lastCalled = "";
        lastDate = LocalDateTime.now();
        lastCallId = 0;
// ToDo Überlegen, wie das wieder implementiert wird
//		if (ConfigValue.get(INITIALIZE_NORMALIZED_PHONE_NUMBERS_IN_PERSON,
//				"false").asBoolean(false)) {
//			try {
//				System.err.println("PhoneCallImporter: Start Initialisierung normalized phoneNumbers") ;
//				List<Person> pl = OMA.select(Realm.BACKEND, Person.class)
//						.list();
//
//				for (Person p : pl) {
//					OMA.saveEntity(Realm.BACKEND, p);
//					System.err.println(p + "  " + p.getPhoneNormalized());
//				}
//			} finally {
//				// set the value back to false
//				ConfigValue value = new ConfigValue(
//						INITIALIZE_NORMALIZED_PHONE_NUMBERS_IN_PERSON, "false");
//				Configuration.save(value);
//			}
//			System.err.println("PhoneCallImporter: Ende Initialisierung normalized phoneNumbers") ;
//			if (numFetched == -1) {
//				return 0;
//			}
//		}
        numFetched = 0;
        try {
            LocalDateTime startDate = LocalDateTime.now();
            System.err.println(NLS.toUserString(startDate) + "PhoneCallImporter =START======================= ");
            // get the last stored id from the phonecall-table
            PhoneCall p = null;
            int rrr = 1;

            Optional pOpt = oma.select(PhoneCall.class).orderDesc(PhoneCall.CDRID).first();
            if (pOpt.isPresent()) {
                p = (PhoneCall) pOpt.get();
            }
            long cdrId = 0;
            if (p != null) {
                cdrId = p.getCdrId();
            } else {
                cdrId = 191822;   // '191822' = 28.12.2015
            }
            Database db = databases.get("starface");
            // get a list of phone-calls with the same callid
            String sql = "SELECT * FROM cdrdata c where c.id > '" + cdrId + "'" + " order by c.id asc limit 500";
            System.err.println(NLS.toUserString(LocalDateTime.now())
                               + " PhoneCallImporter =OCMDatasource: "
                               + db.toString()
                               + " ======= sql: "
                               + sql);
            List<Row> rowList = db.createQuery(sql).queryList();
            System.err.println(NLS.toUserString(LocalDateTime.now())
                               + " PhoneCallImporter =LIST, size "
                               + rowList.size()
                               + " ======= "
                               + NLS.toUserString(LocalDateTime.now()));
            numFetched = importPhoneCalls(rowList);
            LocalDateTime endDate = LocalDateTime.now();
            long diffTime = ChronoUnit.SECONDS.between(startDate, endDate);

            System.err.println(NLS.toUserString(LocalDateTime.now())
                               + " PhoneCallImporter =FINISH=== "
                               + NLS.toUserString(numFetched)
                               +
                               " RECORDS ===== "
                               + NLS.toUserString(endDate)
                               +
                               " === "
                               + NLS.toUserString(diffTime)
                               + "s, lastPhoneCall: "
                               + lastPhoneCallString);
        } catch (Throwable t) {
            Exceptions.handle(new Exception("Error importing phonecalls", t));
        }
        return numFetched;
    }

    /**
     * Importiert alle CDR's aus der Liste rowList
     * Neues Verfahren, CDR mit gleicher callId werden gemeinsam ausgewertet.
     */
    private int importPhoneCalls(List<Row> rowList) {
        int fetched = 0;
        int start = 0;
        int end = 0;
        int maxI = 0;
        lastPhoneCallString = "";
        // letzte CDR's in der Liste ausblenden, damit immer ein vollständiger Anruf (callId ist einheitlich) bearbeitet wird.
        long callId = rowList.get(rowList.size() - 1).getValue("callid").asLong(0);
        for (int i = rowList.size() - 1; i >= 0; i--) {
            Row row = rowList.get(i);
            if (callId != row.getValue("callid").asLong(0)) {
                maxI = i + 1;
                break;
            }
        }

        // Schleife über alle CDR, ermitteln wieviel CDR zu einer callId gehören
        callId = rowList.get(0).getValue("callid").asLong(0);
        for (int i = 0; i < maxI; i++) {
            Row row = rowList.get(i);
            fetched++;
            if (rowList.get(i).getValue("callid").asLong(0) != callId) {
                // callId hat gewechselt!
                end = i;
                // Diese Gruppe - definiert durch start und end  - importieren
                importGroup(rowList, start, end);
                // Vorbereitung für die nächste Gruppe
                start = i;
                callId = rowList.get(i).getValue("callid").asLong(0);
            }
        }
        return fetched;
    }

    /**
     * Importiert eine Gruppe von CDR mit der gleichen callId, definiert durch start und end.
     * Zunächst wird jeder maßgebliche CDR in die Datenbank eingetragen (Kriterium: save).
     * Zusätzlich wird aus allen CDR ein best mögliches Ergebnis in phoneCall gespeichert und das top-Flag zur Selektion
     * gesetzt.
     */
    private void importGroup(List<Row> rowList, int start, int end) {
        List<PhoneCall> phoneCallList = new ArrayList<PhoneCall>();
        int phoneCallIndex = 0;
        String phoneCallAnsweredElsewhere = "";
        Row lastRow = new Row();
        PhoneCall phoneCall = new PhoneCall();
        boolean filledElsewhere = false;
        boolean transfer = false;

        // prüfen, ob es ein eingehender Ruf ist
        for (int i = start; i < end; i++) {
            Row row = rowList.get(i);
            String s = row.getValue("callresult").asString();
            if ("CONNECTED".equals(s)) {
                s = row.getValue("calledcallerid").asString();
                if (Strings.isFilled(s)) {
                    String s1 = row.getValue("callercallerid").asString();
                    String s2 = trimCallerId(s1);
                    boolean t = isPhoneNumber(s2);
                    if (!transfer) {
                        transfer = t;
                    }
                }
            }
        }
        // bearbeiten der Gruppe
        boolean rowExists = false;
        for (int i = start; i < end; i++) {
            // Step1: get the row
            Row row = rowList.get(i);
            if (i == start) {
                lastRow = row;
            }
            // Prüfung, ob dieser CDR schon in der Datenbank steht
            long cdrId = row.getValue("id").asLong(0);
            if ((oma.select(PhoneCall.class).eq(PhoneCall.CDRID, cdrId).exists())) {
                rowExists = true;
            } else {
                // phInteroms anlegen und die Daten dort speichern
                PhoneCall phInterims = new PhoneCall();
                phInterims.setRow(row.toString());
                phInterims.setDifference(identifyDifference(row, lastRow));
                fillPhoneCall(row, phInterims, false, transfer);
                phInterims.setState("RAW");
                // prüfen, ob phInterins "speicherungswürdig" ist
                boolean save1 = !disablePhoneCall(phInterims, transfer);

                // weitere Daten beim phInterims ergänzen
                boolean save2 = addPersonAndEmployee(phInterims);

                // prüfen, ob der Ruf phInterims doppelt zum Vorgänger ist
                boolean save3 = !checkDuplicateRows(phInterims);

                // Prüfergebnis speichern
                boolean save = save1 && save2 && save3;
                phInterims.setSave(save);

                phoneCallList.add(phoneCallIndex, phInterims);
                phoneCallIndex++;
                if (testflag) {
                    save = true;
                }
                if (save) {
                    if (!(oma.select(PhoneCall.class).eq(PhoneCall.CDRID, phInterims.getCdrId()).exists())) {
                        phInterims.setTransfer(transfer);
                    }
                    oma.update(phInterims);
                }

                // summarize the data from each CDR, method: write over
                fillPhoneCall(row, phoneCall, filledElsewhere, transfer);
                if ("ANSWERED_ELSEWHERE".equals(phoneCall.getCallresult())) {
                    String answeredElsewhere = row.getValue("answeredelsewhere").getString();
                    if (!filledElsewhere && Strings.isFilled(answeredElsewhere)) {
                        phoneCallAnsweredElsewhere = answeredElsewhere;
                        filledElsewhere = true;
                    }
                }
            }
        }
        if (rowExists) {
            return;
        }

        // build a targetString for incoming phoneCalls
        String targetString = "";
        String lastEmployeeName = "";
        List<String> targetList = new ArrayList<String>();
        List<String> resizedList = null;
        if (transfer) {
            for (int k = 0; k < phoneCallIndex; k++) {
                PhoneCall pc = phoneCallList.get(k);
                String employeeName = "";
                if (Strings.isFilled(pc.getAnsweredElsewhere())) {
                    String s = pc.getAnsweredElsewhere();
                    String employeeNumber = right(s, 2);
                    employeeName = lookEmployee(employeeNumber, phoneCall.getStarttime());
                    if (!lastEmployeeName.equals(employeeName)) {
                        targetString = targetString + "->" + employeeName;
                        targetList.add(employeeName);
                        lastEmployeeName = employeeName;
                    }
                } else {
                    if (Strings.isFilled(pc.getEmployeeShortName())) {
                        if (!lastEmployeeName.equals(pc.getEmployeeShortName())) {
                            targetString = targetString + "->" + pc.getEmployeeShortName();
                            targetList.add(pc.getEmployeeShortName());
                            lastEmployeeName = pc.getEmployeeShortName();
                        }
                    } else {
                        String s = pc.getCalledcallerid();
                        if (Strings.isFilled(s) && !isPhoneNumber(s)) {
                            String employeeNumber = right(s, 2);
                            employeeName = lookEmployee(employeeNumber, pc.getStarttime());
                            if (!lastEmployeeName.equals(employeeName)) {
                                targetString = targetString + "->" + employeeName;
                                targetList.add(employeeName);
                                lastEmployeeName = employeeName;
                            }
                        } else {
                            s = pc.getCallercallerid();
                            if (Strings.isFilled(s) && !isPhoneNumber(s)) {
                                String employeeNumber = right(s, 2);
                                employeeName = lookEmployee(employeeNumber, pc.getStarttime());
                                if (!lastEmployeeName.equals(employeeName)) {
                                    targetString = targetString + "->" + employeeName;
                                    targetList.add(employeeName);
                                    lastEmployeeName = employeeName;
                                }
                            }
                        }
                    }
                }
            }
            phoneCall.setTarget(targetString);
            // resize the targetList
            resizedList = resizeList(targetList);
        }

        String resizedTargetString = "";
        if (resizedList != null) {
            for (String s : resizedList) {
                resizedTargetString = resizedTargetString + "->" + s;
            }
        }

        // check the summarized phoneCall
        boolean save = false;
        boolean save1 = !disablePhoneCall(phoneCall, transfer);
        boolean save2 = addPersonAndEmployee(phoneCall);

        if (Strings.isFilled(phoneCallAnsweredElsewhere)) {
            phoneCall.setCalledcallerid(phoneCallAnsweredElsewhere);
            String employeeNumber = right(phoneCall.getCalledcallerid(), 2);
            String employeeName = lookEmployee(employeeNumber, phoneCall.getStarttime());
            phoneCall.setEmployeeShortName(employeeName);
            if (phoneCall.isDirectionIn()) {
                phoneCall.setLastCalled(phoneCallAnsweredElsewhere);
            }
        }

        if (IN.equals(phoneCall.getDirection())) {
            if (resizedTargetString.length() > 5) {
                phoneCall.setCalledcallerid(phoneCall.getCalledcallerid() + " (" + resizedTargetString + ")");
            }
        }
        phoneCall.setTop(true);
        phoneCall.setReason("***");
        phoneCall.setState("O.K.");
        phoneCall.setCallresult("*" + phoneCall.getCallresult());
        phoneCall.setTransfer(transfer);

        save = save1 && save2;
        phoneCall.setSave(save);

        // store the phoneCall
        if (!(oma.select(PhoneCall.class).eq(PhoneCall.CDRID, phoneCall.getCdrId()).eq(PhoneCall.TOP, true).exists())) {
            oma.update(phoneCall);
            // ToDO Rechtsnachfolgen für Syslog   Logger definieren. 20.2.2017
//			Syslog.log("PHONECALL-IMPORT", "Imported: " + phoneCall);
        } else {
//			Syslog.log("ERROR PHONECALL-IMPORT", "NOT Imported: " + phoneCall);
        }

        lastPhoneCallString = phoneCall.toString();
    }

    private List<String> resizeList(List<String> targetList) {
        if (targetList.size() == 0) {
            return targetList;
        }
        List<String> list = new ArrayList<String>();
        list.addAll(targetList);
        boolean flagK = false;
        boolean flagD = false;
        int i = 0;
        do {
            flagK = resizeListSub(list);
            flagD = resizeDouble(list);
            i++;
            if (i > 5000) {
                System.err.println("Problem bei targetlist: " + targetList.toString());
                break;
            }
        } while (flagK || flagD);

        if (list.size() > 0) {
            if (!(isLowerCase(list.get(list.size() - 1)))) {
                list.remove(list.size() - 1);
            }
        }
        return list;
    }

    /**
     * kill double lowerCase values in a list like "VTB, aha, aha, VTB"	---> "VTB, aha, VTB"
     */
    private boolean resizeDouble(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String s1 = list.get(i);
            int k = i + 1;
            if (k < list.size()) {
                String s2 = list.get(k);
                if (s1.equals(s2)) {
                    list.remove(k);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * kills the second "VTB" in a List  like "VTB, aha, VTB, aha"   --> "VTB, aha, aha"
     */
    private boolean resizeListSub(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String s1 = list.get(i);
            if (isLowerCase(s1)) {
                if (killBetween(i, list)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean killBetween(int i, List<String> list) {
        int k = i + 2;
        if (k < list.size()) {
            String s1 = list.get(i);
            String s2 = list.get(k);
            if (s1.equals(s2)) {
                if (!isLowerCase(list.get(i + 1))) {
                    list.remove(i + 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLowerCase(String s1) {
        String s2 = s1.toLowerCase();
        return s1.equals(s2);
    }

    /**
     * Zeigt die Änderungen zwischen row und lastrow
     */
    // ToDO : was (wer) ist Rechtsnachfolger für row.getFields() behoben, jetzt testen
    private String identifyDifference(Row row, Row lastRow) {
        String text = "";
        for (Tuple<String, Object> nameNadValue : row.getFieldsList()) {
            String key = nameNadValue.getFirst();
            Object value = nameNadValue.getSecond();
            if (key.contains("time")) {
                continue;
            }
            if ("callleguuid".equals(key)) {
                continue;
            }
            if ("id".equals(key)) {
                continue;
            }
            Object other = lastRow.getValue(key).get();

            if (value == null && other == null) {
                continue;
            }
            if (value != null && other != null) {
                if (value.equals(other)) {
                    continue;
                }
            }
            String ot = "null";
            String vt = "null";
            if (value != null) {
                vt = value.toString();
            }
            if (other != null) {
                ot = other.toString();
            }

            String s = "Feld: " + key + " ist verschieden.  lastRow: " + ot +
                       ", row: " + vt + " |";
            text = text + s;
            lastRow = row;
        }
        return text;
    }

    /**
     * liest die Daten aus row in phoneCall ein
     */
    private void fillPhoneCall(Row row, PhoneCall phoneCall, boolean blockCallerCaller, boolean transfer) {
        LocalDateTime date = null;
        phoneCall.setCdrId(row.getValue("id").asLong(0));
        // prepare the starttime
        String timeString = row.getValue("starttime").getString();
        date = calculateDate(timeString);
        phoneCall.setStarttime(date);

        phoneCall.setDirectionIn(transfer);
        phoneCall.setExternCall(row.getValue("callbacknumberextern").asBoolean());
        phoneCall.setCallId(row.getValue("callid").getLong());
        long duration = row.getValue("duration").asLong(0);
        if (duration > phoneCall.getDuration()) {
            phoneCall.setDuration(duration);
        }
        String s = trimCallerId(row.getValue("calledcallerid").getString());
        if (Strings.isFilled(s)) {
            phoneCall.setCalledcallerid(s);
        }
        if (!blockCallerCaller) {
            s = trimCallerId(row.getValue("callercallerid").getString());
            if (Strings.isFilled(s)) {
                phoneCall.setCallercallerid(s);
            }
        }
        if (phoneCall.getCalledcallerid() == null) {
            phoneCall.setCalledcallerid("");
        }
        phoneCall.setAnswered(row.getValue("answered").asBoolean());
        phoneCall.setAnsweredElsewhere(row.getValue("answeredelsewhere").getString());
        phoneCall.setCallresult(row.getValue("callresult").getString());
        phoneCall.setIncoming(row.getValue("incoming").asBoolean());
        if (phoneCall.isDirectionIn()) {
            if (phoneCall.getFirstCalled() == null) {
                phoneCall.setFirstCalled(phoneCall.getCalledcallerid());
            }
            phoneCall.setLastCalled(phoneCall.getCalledcallerid());
        }
        //	adapt the old phonecalls before cdrId < changeAdr (190.000)
        if (phoneCall.getCdrId() <= changeCdr) {
            if (Strings.isEmpty(phoneCall.getCalledcallerid())) {
                phoneCall.setCalledcallerid(trimCallerId(row.getValue("callbacknumber").getString()));
            }
        }
    }

    /**
     * calculates the LocalDateTime from a given Unix-Time-String
     *
     * @param timeString: Unix-Time in milli-seconds since 1.1.1970
     * @return LocalDateTime
     */
    private LocalDateTime calculateDate(String timeString) {
        if (Strings.isEmpty(timeString)) {
            return null;
        }
        if (timeString.length() < 6) {
            return null;
        }
        long millis = 0L;
        try {
            millis = Long.parseLong(timeString);
        } catch (NumberFormatException e) {
            return null;
        }
        Instant instant = Instant.ofEpochMilli(millis);  // Unix-millis --> Instant
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Zurich")); // Instant --> LocalDateTime
        return ldt;
    }

    /**
     * Trims the given callerId
     *
     * @param callerId
     * @return the trimed callerId
     */
    private String trimCallerId(String callerId) {
        callerId = callerId.replaceAll("\"", "");
        callerId = callerId.replaceAll(">", "");
        callerId = callerId.replaceAll("<", "");
        if (callerId.contains("+49")) {
            String[] fields = callerId.split("49");
            String s = fields[1];
            s = s.replaceAll(" ", "");
            s = s.replaceAll("-", "");
            callerId = fields[0] + "0049" + s;
            callerId = callerId.replace("+", "");
        }
        if (callerId.contains("715190316")) {
            String[] fields = callerId.split("715190316");
            callerId = "scireum " + fields[1];
        }
        return callerId;
    }

    /**
     * extracts the phonenumber from the string 'caller', e.g. caller: "Gerhard Haufler 70630 Remshalden 0715174108
     *
     * @return the isolated phonenumber, e.g. 0715174108
     */
    private String getExternPhoneNumber(String caller) {
        String[] fields = caller.split(" ");
        int l = fields.length;
        return fields[l - 1];
    }

    /**
     * @param phoneCall
     * @return true, if the row is duplicate
     */
    private boolean checkDuplicateRows(PhoneCall phoneCall) {
        String state;
        String reason;
        if (phoneCall.getCdrId() > changeCdr) {
            if (lastCaller.equals(phoneCall.getCallercallerid())) {
                if (lastCalled.equals(phoneCall.getCalledcallerid())) {
                    if (Strings.isEmpty(phoneCall.getAnsweredElsewhere())) {
                        // Testen, ob das so geht
                        long diff = ChronoUnit.MILLIS.between(phoneCall.getStarttime(), lastDate);
                        // max difference is < 1,8 sec
                        if (diff < 1800) {
                            state = "no, identical";
                            reason = "diff <1800ms";
                            phoneCall.setState(state);
                            phoneCall.setReason(reason);
                            return true;
                        }
                    } else {
                        phoneCall.setCalledcallerid(phoneCall.getAnsweredElsewhere());
                    }
                }
                lastCalled = phoneCall.getCalledcallerid();
            } else {
                lastCaller = phoneCall.getCallercallerid();
                lastCalled = phoneCall.getCalledcallerid();
            }
        } else {
            long aa = lastCallId;
            long bb = phoneCall.getCallId();
            if (lastCallId == phoneCall.getCallId()) {
                phoneCall.setState("no, same callId");
                phoneCall.setReason("nothing");
                return true;
            } else {
                lastCallId = phoneCall.getCallId();
            }
        }
        return false;
    }

    /**
     * checks if a phoneCall shold be saved
     *
     * @return false --> no save  or  true --> save
     */
    private boolean disablePhoneCall(PhoneCall phoneCall, boolean transfer) {
        if (Strings.isEmpty(phoneCall.getCallercallerid())) {
            phoneCall.setState("callerid == null");
            phoneCall.setReason("nothing");
            phoneCall.setCallercallerid("");
            return true;
        }
        if ("unknown".equals(phoneCall.getCallercallerid().toLowerCase())) {
            phoneCall.setState("unknown caller");
            phoneCall.setReason("");
            return true;
        }

        if (Strings.isFilled(phoneCall.getCalledcallerid()) && "unknown".equals(phoneCall.getCalledcallerid()
                                                                                         .toLowerCase())) {
            phoneCall.setState("called unknown");
            phoneCall.setReason("");
            return true;
        }
        if (phoneCall.getCallercallerid().length() < 7) {
            phoneCall.setState("no-callerId to short");
            phoneCall.setReason("nothing");
            return true;
        }
        if (!transfer) {
            if (Strings.isEmpty(phoneCall.getCalledcallerid())) {
                phoneCall.setState("no-noCalled");
                phoneCall.setReason("");
                return true;
            }
            if (phoneCall.getCdrId() > changeCdr) {
                if (phoneCall.getCalledcallerid().length() < 7) {
                    phoneCall.setState("no-calledId < 7");
                    phoneCall.setReason("nothing");
                    return true;
                }
            } else {
                if (phoneCall.getCalledcallerid().length() < 2) {
                    phoneCall.setState("no-calledId <2");
                    phoneCall.setReason("nothing");
                    return true;
                }
            }
        }
        if (phoneCall.getCdrId() <= changeCdr) {
            if (phoneCall.getCallercallerid().equals(phoneCall.getCalledcallerid())) {
                phoneCall.setState("no-callerId equals calledId");
                phoneCall.setReason("nothing");
                return true;
            }
        }

        // check 'klingel' (scireum-door-bell)
        if (phoneCall.getCallercallerid().contains("Klingel Klingel 88")) {
            phoneCall.setState("no, Klingel");
            phoneCall.setReason("nothing");
            return true;
        }
        return false;
    }

    private boolean isEmployee(String idString, Boolean inaktiv, LocalDateTime startTime) {
        String s = "";
        if ("Vertrieb 10".equals(idString)) {
            return true;
        }
        if ("9031610 : Vertrieb 10".equals(idString)) {
            return true;
        }
        if ("2063720 : 1st Level 20".equals(idString)) {
            return true;
        }
        if (Strings.isFilled(idString) && idString.length() >= 10) {
            s = idString.substring(0, 10);
            if (s.matches("scireum\\s\\d\\d")) {
                String[] field = idString.split(" ");
                if (isPhoneNumber(field[field.length - 1])) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        if ("scireum 19".equals(idString)) {
            return true;
        }
        String[] field = idString.split(" ");
        s = field[field.length - 1];
        if (isPhoneNumber(s)) {
            return false;
        }
        String nr = "";
        if (idString.length() >= 5) {
            s = idString.substring(0, 5);
            if (s.matches("\\d\\d\\s:\\s")) {
                nr = idString.substring(0, 2);
            }
        } else {
            int ggg = 1;
        }
        if (s.startsWith("90316")) {
            if (idString.length() >= 7) {
                nr = idString.substring(5, 7);
            }
        }
        if (Strings.isFilled(nr)) {
            String employeeName = lookEmployee(nr, startTime);
            if (!"???".equals(employeeName)) {
                return true;
            }
            return false;
        } else {
            Employee employee = getEmployeeByName(idString, inaktiv);
            if (employee != null) {
                return true;
            }
            return false;
        }
    }

    private Employee getEmployeeByName(String name, Boolean inaktiv) {
        Employee employee = null;
        UserAccount uac = null;
        String[] fields = name.split(" ");
        if (fields.length > 1) {
            String firstname = fields[0];
            String lastname = fields[1];
            if (inaktiv == null) {
//				employee = oma.select(Employee.class)
//						.eq(firstname, Employee.FIRSTNAME)
//						.eq(lastname, Employee.LASTNAME).first();
                // ToDo Testen ob der Zugriff klappt
                uac = oma.select(UserAccount.class)
                         .eq(UserAccount.PERSON.inner(PersonData.FIRSTNAME), firstname)
                         .eq(UserAccount.PERSON.inner(PersonData.LASTNAME), lastname)
                         .queryFirst();
            } else {
//				employee = oma.select(Employee.class)
//						.eq(firstname, Employee.FIRSTNAME)
//						.eq(lastname, Employee.LASTNAME)
//						.eq(inaktiv, Employee.INAKTIV).first();
//                uac = oma.select(UserAccount.class)
//                         .eq(UserAccount.PERSON.inner(PersonData.FIRSTNAME), firstname)
//                         .eq(UserAccount.PERSON.inner(PersonData.LASTNAME), lastname)
//                         .eq(Employee.INAKTIV, inaktiv)
//                         .queryFirst();
            }
            if (uac != null) {
                employee = uac.as(Employee.class);
            }
        }
        return employee;
    }

    /**
     * Returns true if the string looks like "xxxx dd"
     */
    private boolean isDecimalValue(String string) {
        String a = right(string, 3);
        if (a.startsWith(" ")) {
            a = right(a, 2);
            if (a.matches("\\d+")) {
                return true;
            }
        }
        return false;
    }

    private boolean addPersonAndEmployee(PhoneCall phoneCall) {
        String calledId = phoneCall.getCalledcallerid();
        String callerId = phoneCall.getCallercallerid();
        if ("89 : Klingel Klingel 88".equals(callerId)) {
            return false;  // Haustürklingel
        }
        if (Strings.isFilled(calledId) && calledId.startsWith("Klingel Klingel")) {
            return false;  // Haustürklingel
        }

        boolean calledIdIsExtern = false;
        boolean calledIdIsEmployee = false;
        boolean callerIdIsExtern = false;
        boolean callerIdIsEmployee = false;

        // calledId = anonymus
        if (Strings.isFilled(calledId) && calledId.toLowerCase().contains("anonymus")) {
            if (testflag) {
                System.err.println("calledId = anonymus, " + phoneCall.toString());
            }
            calledId = "unknown";
        }
        //callerId = anonymus
        if (Strings.isFilled(callerId) && callerId.toLowerCase().contains("anonymus")) {
            if (testflag) {
                System.err.println("callerId = anonymus, " + phoneCall.toString());
            }
            callerId = "unknown";
        }
        // calledId = unknown
        if (Strings.isFilled(calledId) && calledId.toLowerCase().contains("unknown")) {
            callerIdIsExtern = isPhoneNumber(callerId);
            callerIdIsEmployee = isEmployee(callerId, null, phoneCall.getStarttime());
            if (callerIdIsExtern && !callerIdIsEmployee) {
                calledId = "";
            } else {
                if (!callerIdIsExtern && callerIdIsEmployee) {
                    calledId = "";
                } else {
                    System.err.println("calledId = unknown, " + phoneCall.toString());
                    return false;
                }
            }
        }
        // callerId = unknown:  unknown --> ....
        if (Strings.isFilled(callerId) && callerId.toLowerCase().contains("unknown")) {
            calledIdIsExtern = isPhoneNumber(calledId);
            calledIdIsEmployee = isEmployee(calledId, null, phoneCall.getStarttime());
            if (calledIdIsExtern && !calledIdIsEmployee) {
                callerId = "";
            } else {
                if (!calledIdIsExtern && calledIdIsEmployee) {
                    callerId = "";
                } else {
                    if (testflag) {
                        System.err.println("callerId = unknown, " + phoneCall.toString());
                    }
                    return false;
                }
            }
            if (Strings.isEmpty(callerId)) {
                if (calledIdIsExtern) {
                    lookPerson(phoneCall, calledId);
                    return true;
                }
                if (calledIdIsEmployee) {
                    addEmployee(phoneCall, calledId);
                    return true;
                }
                return false;
            }
        }

        if (Strings.isEmpty(calledId)) {
            callerIdIsExtern = isPhoneNumber(callerId);
            callerIdIsEmployee = isEmployee(callerId, null, phoneCall.getStarttime());
            if (callerIdIsExtern) {
                lookPerson(phoneCall, callerId);
            }
            if (calledIdIsEmployee) {
                addEmployee(phoneCall, calledId);
            }
            if (callerIdIsExtern || calledIdIsEmployee) {
                return true;
            } else {
                return false;
            }
        } else {
            // Das ist der Normalfall, zuerst Status bilden
            calledIdIsExtern = isPhoneNumber(calledId);
            callerIdIsExtern = isPhoneNumber(callerId);
            calledIdIsEmployee = isEmployee(calledId, null, phoneCall.getStarttime());
            callerIdIsEmployee = isEmployee(callerId, null, phoneCall.getStarttime());
            // calledId und callerId sind beide externe Telefonnummern (Sonderfall)
            if ((calledIdIsExtern == callerIdIsExtern) && callerIdIsExtern) {
                lookPerson(phoneCall, calledId);
                if (calledIdIsEmployee) {
                    addEmployee(phoneCall, calledId);
                    phoneCall.setDirection(IN);
                }
                return true;
            }
            // calledId und callerId sind beide interne Nummern --> internes Gespräch
            if ((calledIdIsEmployee == callerIdIsEmployee) && callerIdIsEmployee) {
                // Internes Gespräch scireum <--> scireum
                addEmployee(phoneCall, calledId);
                phoneCall.setDirection(INTERN);
                return false; // interne Gespräche werden nicht gespeichert.
            }
            // Anruf von extern zu scireum
            if (callerIdIsExtern && calledIdIsEmployee) {
                addNames(phoneCall, calledId, callerId);
                phoneCall.setDirection(IN);
                return true;
            } else {
                // Anruf von scireum nach extern
                if (calledIdIsExtern && callerIdIsEmployee) {
                    addNames(phoneCall, callerId, calledId);
                    phoneCall.setDirection(OUT);
                    return true;
                } else {
                    // nicht plausibel
                    if (testflag) {
                        System.err.println("Anruf nicht detektierbar: " + phoneCall.toString());
                    }
                    return false;
                }
            }
        }
    }

    private void addEmployee(PhoneCall phoneCall, String internId) {
        String nr = "";

        if ("2063720 : 1st Level 20".equals(internId)) {
            nr = "20";
        }
        if ("Vertrieb 10".equals(internId)) {
            nr = "10";
        }

        if ("2063720 : 1st Level 20".equals(internId)) {
            nr = "20";
        }
        if (internId.matches("scireum\\s\\d\\d")) {
            nr = internId.substring(8, 10);
        }

        if (internId.startsWith("90316")) {
            nr = internId.substring(5, 7);
        }
        if (internId.substring(0, 5).matches("\\d\\d\\s:\\s")) {
            nr = internId.substring(0, 2);
        }
        if (Strings.isFilled(nr)) {
            String employeeName = lookEmployee(nr, phoneCall.getStarttime());
            phoneCall.setEmployeeShortName(employeeName);
        } else {
            Employee employee = getEmployeeByName(internId, null);
            if (employee != null) {
                phoneCall.setEmployeeShortName(employee.getShortName());
            }
        }
    }

    private void addNames(PhoneCall phoneCall, String internId, String externId) {
        lookPerson(phoneCall, externId);
        addEmployee(phoneCall, internId);
    }

    /**
     * a extern callerId looks like "Vorname Nachname (Firma) 004912345654321"
     *
     * @param callNumber
     * @return true if the last characters (e.g. "004912345654321") are decimal
     */
    private boolean isPhoneNumber(String callNumber) {
        String[] idFields = callNumber.split(" ");
        String number = idFields[idFields.length - 1];
        if (number.length() > 5) {
            if (number.matches("\\d+")) { // nr is decimal
                //this is a extern number
                return true;
            }
        }
        // no extern number --> change the state
        return false;
    }

    /**
     * get the employee-shortname with the given phonenumber
     *
     * @param phonenumber, e.g. 21
     * @return shortname of the employee, e.g. aha
     */
    private String lookEmployee(String phonenumber, LocalDateTime startDateTime) {
////		List<Employee> employeeList = oma.select(Employee.class)
////										 .icontains(phonenumber, Employee.PBXID).list() ;
//        //ToDo testen ob hier .qu das .icontains ersetzt
//        LocalDate startDate = startDateTime.toLocalDate();
//        List<UserAccount> employeeList =
//                oma.select(UserAccount.class).eq(Column.named("Employee_" + Employee.PBXID), phonenumber).queryList();
////		List<Employee> employeeList = oma.select(Employee.class).eq(Employee.PBXID, phonenumber).queryList();
//        if (employeeList.size() == 0) {
//            return "? " + phonenumber;
//        }
//
//        if (employeeList.size() == 1) {
//            Employee employee = employeeList.get(0).as(Employee.class);
//            return employee.getShortName();
//        }
//
//        long bestDiff = Long.MAX_VALUE;
//        Employee bestEmployee = null;
//        long diff = 0;
//        for (UserAccount ua : employeeList) {
//            Employee employee = ua.as(Employee.class);
//            LocalDate endDate = employee.getTerminationDate();
//            if (endDate == null) {
//                endDate = LocalDate.now();
//            }
//            diff = ChronoUnit.DAYS.between(startDate, endDate);
//            if (diff > 0 && diff < bestDiff) {
//                bestEmployee = employee;
//                bestDiff = diff;
//            }
//        }
//        if (bestEmployee != null) {
//            return bestEmployee.getShortName();
//        } else {
//            return "?? " + phonenumber;
//        }

        return null;
    }

    /**
     * @param value String, e.g. "1234567"
     * @param i     length of the substring, e.g. 3
     * @return substring, e.g. "567"
     */
    private String right(String value, int i) {
        int l = value.length();
        String b = value;
        if (l >= i) {
            b = value.substring(l - i);
        }
        return b;
    }

    /**
     * looks for a person with this phoneNumber and store this person and the company in phoneCall
     */
    private void lookPerson(PhoneCall phoneCall, String phoneNumber) {
        // adapt the phoneNumber to a normalized Nr, e.g. 004971519031621
        if (phoneNumber.startsWith("0049")) {
            // do nothing
        } else {
            if (phoneNumber.startsWith("+49")) {
                phoneNumber = "0049" + phoneNumber.substring(2);
            } else {
                for (int i = 0; i < phoneNumber.length(); i++) {
                    if (phoneNumber.startsWith("0")) {
                        phoneNumber = phoneNumber.substring(1);
                    } else {
                        break;
                    }
                }
            }
        }
        String[] field = phoneNumber.split(" ");
        phoneNumber = field[field.length - 1];
        Person person = null;
        if (phoneNumber.length() > 5) {
            // ToDO testen, ob das mit .eq statt .icontains funktioniert.
            // ToDo mögliche Alternative: WHERE(LIKE.ON(Feldname, Variable)
            Optional opti = oma.select(Person.class).eq(Person.CONTACT.inner(ContactData.PHONE), phoneNumber).first();
            if (opti.isPresent()) {
                person = (Person) opti.get();
            } else {
                opti = oma.select(Person.class).eq(Person.CONTACT.inner(ContactData.MOBILE), phoneNumber).first();
                if (opti.isPresent()) {
                    person = (Person) opti.get();
                }
            }
            if (person != null) {
                phoneCall.getPerson().setId(person.getId());
                phoneCall.getCompany().setId(person.getCompany().getValue().getId());
            }
        }
    }

    @Override
    public void execute(Output output, String... strings) throws Exception {
        // ToDo das wird beim Minutenwechsel nicht aktiviert
    }

    @Override
    public String getDescription() {
        return "Importiert die Telefonanrufe aus Starface";
    }

    @Nonnull
    @Override
    public String getName() {
        return "PhoneCallImporter";
    }

    @Override
    public void runTimer() throws Exception {

        // ToDo wieder aktivieren
        // fetchPhoneCalls();
    }
}
