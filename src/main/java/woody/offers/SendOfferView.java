/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;

import sirius.db.mixing.OMA;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.http.MimeHelper;
import sirius.web.mails.Mails;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;

import woody.core.employees.Employee;
import woody.sales.AccountingService;
import woody.xrm.Person;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardhaufler on 01.05.14.
 */



// ToDO prüfen ob SendOfferView überhaupt noch gebraucht wird
public class SendOfferView /* extends BasicView*/ {

    private List<OfferItem> offerItemList = new ArrayList<OfferItem>();
    public void setSendOfferViewOfferItemList (List<OfferItem> oiList) {
        offerItemList = oiList;
    }

//    @Part
//    private DataAreas areas;

    @Part
    private OMA oma;

    @Part
    private Mails mails;

//    @Part
//    private NamedObjectService nos;

    @Part
    private AccountingService as;

    private final class SendAction /*implements ActionListener */{
        // send-Button was clicked
//        @Override
        public void action() throws Exception {

            Offer offer = (Offer)context.get("offer");

            // send the offer via mail to the receiver
            checkMailReceiver(person);

            // is a Buyer with a valid Mail-Adress available?
            if(offer.getBuyer().isFilled()) {
                    checkMailReceiver(offer.getBuyer().getValue());
            }

            // get the date and the time of sending the mail
            LocalDate sendDate = LocalDate.now();

            // ToDo store the pdf-File as attachment
//            String namedObjectName = offer.getUnqiueObjectName();
//            NamedObject namedObject = nos.getNamedObject(namedObjectName);
//            DataArea area = areas.getAttachmentsArea(namedObject);
//            InputStream in = new ByteArrayInputStream(byteIn);
//            String filename = as.dateTimeFilename("_", sendDate);
//            filename = filename + attachmentName;
//            VFile  file = area.addFile(null, filename, in);

            // send the mail to the person ("Ansprechpartner")
//            sendMailToPerson(person.getContact().getEmail(), sendDate, namedObjectName, file, person);
            // send the mail to the buyer ("kfm. Ansprechpartner")
            if(offer.getBuyer().isFilled()) {
//                sendMailToPerson(offer.getBuyer().getValue().getContact().getEmail(), sendDate, namedObjectName, file, offer.getBuyer().getValue());
            }

            if(ServiceAccountingService.SALES_CONFIRMATION.equals(function)) {
                // set the status to confirmation and set the confirmationDate
                if (oiList != null) {
                    for (OfferItem oi : oiList) {
                        oi.setSalesConfirmationDate(sendDate);
                        oi.setState(OfferItemState.CONFIRMED);
                        oma.update(oi);
                    }
                }
            }

            // Build a success-message
            // Todo erfolgsmeldung ausgeben
//            ApplicationController.addSuccessMessage(MessageFormat.format("Mail wurde versendet und bei {0} gespeichert.",  file.getFile().getPath()));
//            returnOK();
        }

    }

    private void checkMailReceiver(Person person) {
        if(Strings.isEmpty(person.getContact().getEmail())) {
            throw Exceptions.createHandled()
                            .withNLSKey("checkMailReceiver.isNull")
                            .set("value", person.getPerson().getAddressableName())
                            .handle();
        }
        if(person.isOffline()) {
            throw Exceptions.createHandled()
                            .withNLSKey("checkMailReceiver.personOffline")
                            .set("value", person.getPerson().getAddressableName())
                            .handle();
        }
    }
// ToDO neue Mailschnittstelle machen
//    void mailTest() {
//        mails.createEmail()
//            .to(eMailAdr, person.getName()).useMailTemplate()
//    }

    private void sendMailToPerson(String eMailAdr, LocalDate sendDate, String namedObjectName, File file, Person person) {
        // prepare subject and text for HTML and XML
        // ToDo subject und text
        String subject = ""; // Tools.nl2br(Tools.escapeXML(subjectBox.getValue()));
        String text = ""; // Tools.nl2br(Tools.escapeXML(textArea.getValue()));
        mails.createEmail()
                .from("support@scireum.net", "scireum-Support")  // ToDO currentUser
                .subject(subject)
                .to(eMailAdr, person.getPerson().getLastname())
                .textContent(text)
                .addAttachment(new DataSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(byteIn);
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        return null;
                    }

                    @Override
                    public String getContentType() {
                        return MimeHelper.APPLICATION_PDF;
                    }

                    @Override
                    public String getName() {
                        return attachmentName;
                    }
                })
                .send();

    }

    //TODO mailschnittstelle mit PDF file
//    private void sendMailToPerson(String eMailAdr, LocalDate sendDate, String namedObjectName, VFile file, Person person) {
//             // prepare subject and text for HTML and XML
//      mail.createEmail()
//                .from("support@scireum.net", "scireum-Support")
//                .subject(subjectBox.getValue())
//                .to(eMailAdr, person.getName())
//                .textContent(textArea.getValue())
//                .addAttachment(new DataSource() {
//                    @Override
//                    public InputStream getInputStream() throws IOException {
//                        return new ByteArrayInputStream(byteIn);
//                    }
//
//                    @Override
//                    public OutputStream getOutputStream() throws IOException {
//                        return null;
//                    }
//
//                    @Override
//                    public String getContentType() {
//                        return MimeHelper.APPLICATION_PDF;
//                    }
//
//                    @Override
//                    public String getName() {
//                        return attachmentName;
//                    }
//                })
//                .send();

//    // generate a pdf-document from the mail-text
//    String subject =  Tools.nl2br(Tools.escapeXML(subjectBox.getValue()));
//    String text =  Tools.nl2br(Tools.escapeXML(textArea.getValue()));

//    Context context = new Context();
//    context.set("dateString", NLS.toUserString(cal.getTime(), true));
//    context.set("eMailAdr", eMailAdr);
//    context.set("subject", subject);
//    context.set("textArea", text);
//    context.set("objectName", namedObjectName);
//    context.set("file", file.getFile().getPath());
//    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//    try {
//        ContentGenerator.getInstance().generate("/plugins/scireum/crm/mailoffer.pdf", byteOut, context);
//    } catch (IOException e) {
//        ApplicationController.handle(e);
//    }
//
//    // store the subject and the mail-text (as pdf-document) in the attachment-area
//    NamedObject namedObject = nos.getNamedObject(namedObjectName);
//    DataArea area = areas.getAttachmentsArea(namedObject);
//    byte[] mailtext = byteOut.toByteArray();
//    InputStream in = new ByteArrayInputStream(mailtext);
//    String timeStamp = as.dateTimeFilename("_", cal);
//    String filename = MessageFormat.format("{0}Mail_an_{1}{2}_{3}", timeStamp, person.getLastname(),
//                                           person.getFirstname() == null ? "" : person.getFirstname(), attachmentName);
//    try {
//        file = area.addFile(null, filename, in);
//    } catch (IOException e) {
//        ApplicationController.handle(e);
//    }



    // store the mail in a notice of the person
        //ToDO: andere Lösung um notes zu speichern
//        Note note = new Note();
//        note.setCompany(person.getCompany());
//        note.setDate(cal.getTime());
//        note.setEmployee(CRM.getCurrent());
//        text = MessageFormat.format("{0} \n\n{1}\n\nAnlagen: \n{2}\n\nuniqueObjectName: {3}, filename: {4}",
//                subjectBox.getValue(), textArea.getValue(), attachmentArea.getValue(), namedObjectName,  file.getFile().getPath());
//        note.setNotice(text);
//        note.setNoticeType(NoticeType.EMAIL_OUT);
//        note.setPerson(person);
//        note.setSubject(subjectBox.getValue());
//        note = OMA.saveEntity(Realm.BACKEND, note);
//
//        Person personFill = OMA.fill(person, Person.NOTES);
//        List<Note> noteList = personFill.getNotes();
//        noteList.add(note);
//        personFill.setNotes(noteList);
//        person = OMA.saveEntity(Realm.BACKEND, personFill);
//    }



    private Person person;
    private Context context;
    private List<OfferItem>  oiList = new ArrayList<OfferItem>();
//    private ExecutableDropdown<Mailtemplate> mailtemplateDD;
//    private TextBox subjectBox;
//    private TextArea textArea;
//    private TextArea attachmentArea;
    private byte[] byteIn;
    private String attachmentName;
    private String function;

    public SendOfferView(Context context, byte[] byteIn, String filename, String function, List<OfferItem> oiList) {
        super();
        this.context = context;
        this.byteIn = byteIn;
        this.attachmentName  = filename;
        this.function = function;
        this.oiList = oiList;
    }

    protected Context getSendOfferViewContext() {
        return context;
    }

//    @Override
    protected void setupUI() {
        // build the UserInterface of the SendMailView
        // Step1: Container
        person = (Person)context.get("person");
//        Section container = addTab("E-Mail an: " + person + ", " + person.getEmail()).add(Container.class)
//                .setAddContainer(true).add(Section.class);
//
//
//        // Step2: SubjectBox
//        subjectBox = container.add(TextBox.class);
//        subjectBox.setLabel("Betreff");
//        subjectBox.setTwoCols(true);
//        subjectBox.setClearLeft(true);
//        // Step3: Textarea for mail-text
//        textArea = container.add(TextArea.class);
//        textArea.setMaximized(true);
//        textArea.setLabel("Text");
//        textArea.setClearLeft(true);
//        textArea.setTwoCols(true);
//
//        // Step3: Textarea for attachments
//        attachmentArea = container.add(TextArea.class);
//        attachmentArea.setMaximized(false);
//        attachmentArea.setClearLeft(true);
//        attachmentArea.setLabel("Anlagen");
//        attachmentArea.setTwoCols(true);
//        // Step4: Send-Button
//        ButtonBar buttonBar = container.add(ButtonBar.class);
//        buttonBar.addButton("Mail senden", new SendAction());
//        invokeTemplate(context);

    }

    private void invokeTemplate(Context ctx) {
        try {
            Offer offer = (Offer) ctx.get("offer");
            Person person = (Person) ctx.get("person");
            StringBuilder sb = new StringBuilder();
            sb.append(person.getLetterSalutation());
            sb.append("\n\n");

            switch(function) {
                case ServiceAccountingService .OFFER:
//                    subjectBox.setValue("Angebot " + offer.getNumber() + " vom " + NLS.toUserString(offer.getDate()) + ", " + offer.getKeyword());
                    sb.append("anbei erhalten Sie das gewünschte Angebot.") ;
                    break;
                case ServiceAccountingService.SALES_CONFIRMATION:
//                    subjectBox.setValue("Auftragsbestätigung "+offer.getNumber()+ " Positionen: "+ ctx.get("positions")
//                            +", "+offer.getKeyword());
                    sb.append("anbei erhalten Sie die Auftragsbestätigung zu Ihrer Bestellung.") ;
                    break;
            }

            sb.append("\n\n");

            if(Strings.isFilled(offer.getBuyer())) {
                sb.append(offer.getBuyer().getValue().getPerson().getAddressableName() + " erhält diese Mail ebenfalls zur Information.") ;
                sb.append("\n\n");
            }

            sb.append("Für Erläuterungen und Rückfragen stehen wir gerne zur Verfügung.") ;
            sb.append("\n\n");
            UserInfo userInfo = UserContext.getCurrentUser();
            Employee employee = userInfo.as(Employee.class);

            sb.append(employee.getSignature()) ;        // neue Lösung gemäß CRM-48
            sb.append("\n\n\n");
            sb.append("Anlage: Datei: "+ctx.get("filenamePDF")) ;
// ToDo textArea und attachmentArea
//           textArea.setValue(sb.toString());
//           attachmentArea.setValue(attachmentName);

        } catch (Exception e) {
            Exceptions.handle(e);
        }
    }
}



