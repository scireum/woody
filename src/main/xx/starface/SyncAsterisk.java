/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.modules.starface;

import sirius.kernel.di.std.Register;
import sirius.kernel.timer.EveryDay;

@Register(classes = {EveryDay.class})
public class SyncAsterisk /* implements EveryDay */ {
//
//    // test mit localhost:9000/system/console
//    //          timer everyDay
//
//
//    public static final Log LOG = Log.get("asterisk");
//
//    private static final long BASE_ID = 9000000;
//    private static final int PHONEFIELDNR = 2;        // Feld-Nummer in persondata für die Telefonnummer
//    private static final int FAXFIELDNR = 3;          // Feld-Nummer in persondata für die FAX-Nummer
//    private static final int COMPANYFIELDNR = 14;     // Feld-Nummer in persondata für den Firmennamen
//    private static final int MOBILEFIELDNR = 22;      // Feld-Nummer in persondata für die Handy-Nummer
//    private static final int URLFIELDNR = 24;         // Feld-Nummer in persondata für die URL, in dieses Feld wird die Id eingetragen
//
//    // ToDo dryrun auf false setzen
//    private static final boolean dryrun = true; // --> no updates in the starface-database
////    private static final boolean dryrun = false; // --> updates in the starface-database
//
//    boolean testSystem = true;
//
//    @Part
//    private static Databases databases;
//
//    @Part
//    private static OMA oma;
//
//    @Override
//    public void runTimer() throws Exception {
////        // ToDo Rechtsnachfolger für Model.isDebugEnvironment()
//////        if (Model.isDebugEnvironment()) {
//////            CRM.LOG.INFO("Not fetching any phoneCalls in the debug system!");
//////            return;
//////        }
////
////        if(testSystem) {
////            LOG.INFO("SyncAsterisk: no synchronizing with starface");
////            return;
////        }
////
////        LOG.INFO(NLS.toUserString("Start Synchronisation Telefonanlage Asterisk, dryrun: " + NLS.toUserString(dryrun)));
////
////        // Datenbank der Telefonanlage
////        Database db =   databases.get("starface");
////        // Abfrage der CRM-Datenbank, zuerst die Personen, die offline sind, damit bei gleicher Telefon-Nr
////        // die aktuelle Telefonnummer zuletzt gespeichert wird.
////        // Beispiel: Alois Klemmerle kündigt, der Nachfolger ist Josef Häberle
////        // Relation Person:                                       Telefonanlage Asterisk:
////        // Klemmerle, Alois offline=true  0711/4532-2346          Klemmerle, Alois      XXX
////        // Häberle, Josef   offline=false 0711/4532-2346          Häberle, Josef        0711/4532-2346
////
////        int nr = 0;
////        List<Person> personList = oma.select(Person.class)/*.orderDesc(Person.OFFLINE)*/.queryList();
////        LOG.INFO(NLS.toUserString(personList.size()) + " Personen  gefunden, dryrun: " + NLS.toUserString(dryrun));
////        for (Person person : personList) {
////            nr++;
////            LOG.INFO(NLS.toUserString(nr) + ",   " + person.getPerson().toString()) ;
////            if(person.getContact().getPhone() == null && person.getContact().getFax() == null &&
////                    person.getContact().getMobile() == null) {
////                LOG.INFO("Die Person: " + person.getPerson().toString() + " hat weder Telefon-, noch Fax- noch Mobile-Nummer --> kein upDate");
////                continue;
////            }
////            Long idAsterisk = null;       // id der Asterisk-Datenbank
////            boolean fieldUpdate = false;
////            try {
////
////                long idPersonal = person.getId() + BASE_ID;   // id aus Person abgeleitet
////                // Zugriff auf die Telefondatenbank
////                List<Row> rowList = db
////                        .createQuery("SELECT * FROM person p WHERE EXISTS(SELECT * FROM persondata pd WHERE pd.personid = p.id AND pd.datadefaultid = " + URLFIELDNR + " AND pd.value = ${id})")
////                        .set("id", String.valueOf(idPersonal))
////                        .queryList();
////                int size = rowList.size();
////                switch (size) {
////                    case 1:    // Genau ein Ergebnis
////                        Row personRow = rowList.get(0);
////                        idAsterisk = Long.parseLong(personRow.getValue("id").asString());
////                        String firstname = personRow.getValue("firstname").asString();
////                        String familyname = personRow.getValue("familyname").asString();
////                        // check the lastname
////                        if (!familyname.equals(person.getPerson().getLastname().trim())) {
////                            LOG.WARN("Nachname der Person " + NLS.toUserString(person) + " sind verschieden, Starface: "
////                                               + familyname);
////                        }
////                        // check the firstname
////                        if (Strings.isFilled(person.getPerson().getFirstname())) {
////                            if (!firstname.equals(person.getPerson().getFirstname().trim())) {
////                                LOG.WARN("Vorname der Person " + NLS.toUserString(person)
////                                                   + " sind verschieden, Starface: " + firstname);
////                            }
////                        }
////                        // update the table person in the starface-DB
////                        LOG.INFO("update starface.person für "+ person.getPerson().toString() + ", person.Id = " + person.getId());
////                        if(!dryrun) {
////                            db.createQuery("UPDATE person SET firstname=${first}, familyname=${family} WHERE id= ${id}")
////                              .set("id", idAsterisk)
////                              .set("first",
////                                   Strings.isEmpty(person.getPerson().getFirstname()) ? " " : person.getPerson().getFirstname())
////                              .set("family", person.getPerson().getLastname())
////                              .executeUpdate();
////                        }
////                        fieldUpdate = true;
////                    break;
////
////                    case 0:    // kein Eintrag mit dieser id vorhanden
////                     //   boolean offline = person.isOffline();
//////                        LOG.INFO(person.getPerson().toString() + ", person.Id: " + person.getId()
//////                                 + ", offline: "+ NLS.toUserString(offline));
//////                        if (!(person.isOffline())) {    // nur INSERT für Personen die nicht offline sind
//////                            LOG.INFO("Für die Person: " + person.getPerson().toString()
//////                                     + ", person.Id = " + person.getId() + " ist ein INSERT erforderlich");
//////                            if(!dryrun) {
//////                                Row rowInsert = db.createQuery(
//////                                        "INSERT INTO person (firstname, familyname) VALUES(${first}, ${family})")
//////                                                  .set("first", Strings.isEmpty(person.getPerson().getFirstname()) ?
//////                                                       " " : person.getPerson().getFirstname())
//////                                                  .set("family", person.getPerson().getLastname())
//////                                                  .executeUpdateReturnKeys();
//////                                if (rowInsert != null) {
//////                                    String IdString = rowInsert.getValue("id").asString();
//////                                    idAsterisk = Long.parseLong(IdString);
//////                                }
//////                            }
////                            fieldUpdate = true;
////                        }
////                    break;
////
////                    default:  // Die Liste enthält für Name und Vorname mehrere Einträge ---> Fehler
////                        LOG.SEVERE("Für die Person: "
////                                       + person.getPerson().toString() + ", person.Id: " + person.getId()
////                                       + " wurde in der Telefonanlage kein eindeutiger Teilnehmer gefunden, mögliche Teilnehmer-Anzahl = "
////                                       + size);
////                    break;
////                }
////
////                if (fieldUpdate) {
////                    // update der Telefondatenbank, Relation persondata
////                    setField(db, idAsterisk, PHONEFIELDNR, normalizePhonenumberForStarfaceAddressbook(person.getContact().getPhone(), true), person);
////                    setField(db, idAsterisk, FAXFIELDNR, normalizePhonenumberForStarfaceAddressbook(person.getContact().getFax(), true), person);
////                    setField(db, idAsterisk, MOBILEFIELDNR, normalizePhonenumberForStarfaceAddressbook(person.getContact().getMobile(), true), person);
////                    setField(db, idAsterisk, COMPANYFIELDNR, person.getCompany().getValue().getName(), person);
////                    String idString = NLS.toUserString(idPersonal);
////                    setField(db, idAsterisk, URLFIELDNR, idString, person);
////                 } else {
////                    LOG.INFO("Für die Person: " + person.getPerson().toString() + ", person.Id: " + NLS.toUserString(person.getId()) +" erfolgt kein fieldUpdate!");
////                }
////            } catch (Exception e) {
////                Exceptions.handle(e);
////            }
////        }
////
////        LOG.INFO(NLS.toUserString("Ende Synchronisation Telefonanlage Asterisk"));
//
//    }
//
//    /**
//     * A given phonenumber is normalized for using in starface to detect calls by the phoneNumber
//     * given phonenumber e.g. 0049(0)7151 / 90316-21 --> +4971519031621
//     * @param phoneNumber: phonenumber
//     * @return normalized phoneNumber
//     */
//   public static String normalizePhonenumberForStarfaceAddressbook(String phoneNumber, boolean withPlus) {
//        if (Strings.isEmpty(phoneNumber)) {
//            return null;
//        }
//        phoneNumber = phoneNumber.replace(" ", "");
//        // (0) am Anfang durch 0 ersetzen
//        if (phoneNumber.startsWith("(0)")) {
//            phoneNumber = "0" + phoneNumber.substring(3);
//        }
//        // (0) nach +49 o.ä. weglöschen
//        phoneNumber = phoneNumber.replace("(0)", "");
//        // Aus +49 0049 machen
//        phoneNumber = phoneNumber.replace("+", "00");
//        // Alles außer Ziffern kommt raus.
//        phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
//        // Ohne Ländervorwahl ist es Deutschland
//        if (phoneNumber.startsWith("0") && !phoneNumber.startsWith("00")) {
//            phoneNumber = "0049" + phoneNumber.substring(1);
//        }
//        // set "000" to "00"
//        if(phoneNumber.startsWith("000")) {
//            phoneNumber = phoneNumber.replaceFirst("000", "00");
//        }
//       if(withPlus) {
//           // Änderung am 23.01.2017:  aus 0049 bzw. 0043 wird nun +49 bzw. +43. Hintergrund: update Starface im Jan. 2017
//           String newNumber = phoneNumber;
//           if (newNumber == null) {
//               newNumber = "";
//           } else {
//               newNumber = newNumber.replaceFirst("00", "+");
//           }
//           return newNumber;
//       } else {
//           return phoneNumber;
//       }
//    }
//
//    // Schreibt einen Wert in die Telefon-Datenbank, Tabelle: persondata
//    private int setField(Database db, Long id, int field, String value, Person person) {
////        if (id == null || id < 1) {
////            LOG.SEVERE("field: " + NLS.toUserString(field) + ", Person.ID = " + person.getId() +
////                               ", asteriskId (id) == null or <1, " + person.getPerson().toString() +
////                               ",  " + person.getCompany().getValue().getName()
////                               + ", Nummer: " + value + " kann nicht eingetragen werden, da id <= 0");
////            return 1;
////        }
////        if (Strings.isFilled(value)) {
////            if (!(field == COMPANYFIELDNR || field == URLFIELDNR)) {  //Wert-Anpassung nicht bei Company  oder URL
////                if (person.isOffline()) { // if persons are offline --> set the values to "XXX"
////                    value = "XXX";
////                    LOG.INFO("field: " + NLS.toUserString(field) + ", Person is offline, Person.ID = " + person.getId() +
////                                       ", id = " + id + ", " + person.getPerson().toString() + ",  " + person.getCompany().getValue().getName()
////                                              + ", Nummer: " + value + " --> XXX");
////                }
////            }
////            try {
////                if (db.createQuery(
////                        "SELECT * FROM persondata where personid = ${id} AND datadefaultid=${field}")
////                        .set("id", id).set("field", field).queryFirst() != null) {
////                    String s = MessageFormat.format("UPDATE persondata set value= {0} WHERE personid= {1} AND datadefaultid= {2}", value, id, field);
////                    LOG.INFO(s);
////                    if(!dryrun) {
////                        db.createQuery(
////                                "UPDATE persondata set value=${value} WHERE personid=${id} AND datadefaultid=${field}")
////                          .set("id", id)
////                          .set("field", field)
////                          .set("value", value)
////                          .executeUpdate();
////                    }
////                } else {
////                    String s = MessageFormat.format("INSERT INTO persondata (personid, datadefaultid, value) VALUES({0}, {1}, {2})",id, field, value);
////                    LOG.INFO(s);
////                    if(!dryrun) {
////                        db.createQuery(
////                                "INSERT INTO persondata (personid, datadefaultid, value) VALUES(${id}, ${field}, ${value})")
////                          .set("id", id)
////                          .set("field", field)
////                          .set("value", value)
////                          .executeUpdate();
////                    }
////                }
////            } catch (SQLException e) {
////                Exceptions.handle(e);
////            }
////            return 0;
////        }
////        return 1;
//
//        return 0;
//    }
//
//
//    @Override
//    public String getConfigKeyName() {
//        return "SyncAsterisk";
//    }
}
