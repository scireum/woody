/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.tenants.UserAccount;
import sirius.db.jdbc.OMA;
import sirius.kernel.commons.Context;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.http.MimeHelper;
import sirius.web.mails.Mails;
import sirius.web.templates.Templates;
import woody.sales.accounting.ServiceAccountingService;
import woody.sales.quotes.Offer;
import woody.sales.quotes.OfferItem;
import woody.xrm.Person;
import woody.xrm.tracking.mails.Mail;

import javax.activation.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardhaufler on 26.10.17.
 */
@Register(classes = SendMailService.class)
public class SendMailServiceBean implements SendMailService {

    @Part
    private DataAreas areas;

    @Part
    private Mails mail;

    @Part
    private static Templates templates;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static OMA oma;

    @Override
    public List<String> prepareAndSendMail(Offer offer, Mail mail) {
        List<String> messageList = new ArrayList<String>();
        String function = mail.getFunction();
        Context context = null;
        LocalDateTime sendTime = null;
        String plainText = mail.getText();
        Person person = mail.getPersonEntity().getValue();
        String subject = mail.getSubject();
        UserAccount uac = mail.getEmployeeEntity().getValue();
        String senderName = uac.getPerson().getAddressableName();
        String text0 = "Mail mit Id: {0} an {1} wurde versendet";
        String text = "";
        switch (function) {

            case ServiceAccountingService.OFFER:
            case ServiceAccountingService.SALES_CONFIRMATION:
                context = sas.prepareContext(offer, mail.getFunction());
// ToDo auf pasta umstellen
//        File fileAttachment = sas.createPdfFromContext(context, "templates/offer.pdf.pasta");
                File fileAttachment = sas.createPdfFromContext(context, "templates/offer.pdf.vm");
                context.set("fileAttachment", fileAttachment);

                String filenamePDF = (String) context.get("filenamePDF");
                DataSource dataSource = new DataSource() {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(fileAttachment);
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
                        return filenamePDF;
                    }
                };

                sendMail(mail.getSenderAddress(),
                         senderName,
                         mail.getReceiverAddress(),
                         person.getPerson().getAddressableName(),
                         subject,
                         plainText,
                         dataSource,
                         mail.getAttachmentName());

                sendTime = LocalDateTime.now();
                mail.setSendDate(sendTime);

                text = MessageFormat.format(text0, mail.getId(), mail.getReceiverAddress());
                messageList.add(text);

                if (context.get("buyerMail") != null) {
                    sendMail((String) context.get("employeeMail"),
                             senderName,
                             (String) context.get("buyerMail"),
                             (String) context.get("buyerName"),
                             subject,
                             plainText,
                             dataSource,
                             filenamePDF);
                    mail.setCcAddress((String) context.get("buyerMail"));
                    text = MessageFormat.format(text0, mail.getId(), (String) context.get("buyerMail"));
                    messageList.add(text);
                }

                break;
            case ServiceAccountingService.NORMAL_MAIL:
                sendMail(mail.getSenderAddress(),
                         senderName,
                         mail.getReceiverAddress(),
                         person.getPerson().getAddressableName(),
                         subject,
                         plainText,
                         null,
                         null);
                sendTime = LocalDateTime.now();
                mail.setSendDate(sendTime);
                text = MessageFormat.format(text0, mail.getId(), mail.getSenderAddress());
                messageList.add(text);

                break;
        }

        // algorithm see MailImporter / private boolean importMessageInMail(Message message), line 186
        mail.setMessageId(sas.buildMd5HexString(mail.getSubject()
                                                + NLS.toMachineString(mail.getReceivingDate()
                                                                      == null ?
                                                                      mail.getReceivingDate() :
                                                                      mail.getSendDate())
                                                + mail.getPersonEntity().getId()));
        oma.update(mail);

        switch (function) {
            case ServiceAccountingService.SALES_CONFIRMATION:
                // set the salesConfirmationDate = now and the state = CONFIRMED
                LocalDate confirmationDate = LocalDate.now();
                List<OfferItem> confirmationList = (List<OfferItem>) context.get("offerItemList");

                for (OfferItem oi : confirmationList) {
//                    if (oi.getSalesConfirmationDate() == null) {
//                        oi.setSalesConfirmationDate(confirmationDate);
//                        oi.setState(OfferItemState.CONFIRMED);
//                        oma.update(oi);
//                    }
                    // Ggfs. Verträge aus den Auftragsbestätigungen anlegen
                    messageList.add(sas.createContractFromOfferItem(oi));
                }
                break;

            default:
                break;
        }

        return messageList;
    }

    // send-Button was clicked
    @Override
    public void sendMail(String senderAddress,
                         String senderName,
                         String receiverAddress,
                         String receiverName,
                         String subject,
                         String mailText,
                         DataSource attachment,
                         String attachmentName) {

        if (attachmentName != null) {
            List<DataSource> listDataSource = new ArrayList<DataSource>();
            listDataSource.add(attachment);
            // send the mail with attachment
            mail.createEmail()
                .from(senderAddress, senderName)
                .subject(subject)
                .to(receiverAddress, receiverName)
                .textContent(mailText)
                .addAttachment(attachment)
                .send();
        } else {
            mail.createEmail()
                .from(senderAddress, senderName)
                .subject(subject)
                .to(receiverAddress, receiverName)
                .textContent(mailText)
                .send();
        }

//
//            // store a mail-attachment as 'attachment' to the mail
//            if(Tools.notEmpty(attachmentName) && !"keine".equals(attachmentName)) {
//                String namedObjectName = mailDb.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(sendTime, namedObjectName, byteIn, attachmentName);
//
//            }
//
//            String fileMessage = ".";
//            // store th pdf-Document (offer) in a 'offer-attachment'
//            if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function)) {
//                // store the pdf-File as attachment
//                String namedObjectName = offer.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(sendTime, namedObjectName, byteIn, attachmentName);
//                fileMessage = " und bei " + file.getFile().getPath() + " gespeichert.";
//            }
//            // store th pdf-Document (contract) in a 'company-attachment'
//            if (CONTRACT_MAIL.equals(function)) {
//                // store the pdf-File as attachment
//                Company company = contract.getCompany();
//                String namedObjectName = company.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(sendTime, namedObjectName, byteIn, attachmentName);
//                fileMessage = " und bei " + file.getFile().getPath() + " gespeichert.";
//            }
//
//            // store the mail in a notice of the person
//            Note note = new Note();
//            note.setCompany(person.getCompany());
//            note.setDate(new Date());
//            note.setEmployee(CRM.getCurrent());
//            note.setNotice(subjectBox.getValue()
//                           + "\n\n"
//                           + textArea.getValue()
//                           + "\n\nAnlagen: \n"
//                           + attachmentArea.getValue());
//            note.setNoticeType(NoticeType.EMAIL_OUT);
//            note.setPerson(person);
//            note.setSubject(subjectBox.getValue());
//            note = OMA.saveEntity(Realm.BACKEND, note);
//
//            Person personFill = OMA.fill(person, Person.NOTES);
//            List<Note> noteList = personFill.getNotes();
//            noteList.add(note);
//
//            personFill.setNotes(noteList);
//            person = OMA.saveEntity(Realm.BACKEND, personFill);
//

    }

//    public SendMailView(Person person, Context context) {
//        super();
//        this.person = person;
//        this.context = context;
//        if (this.context == null) {
//            this.context = Context.create();
//        }
//        this.context.put("person", person);
//        this.context.put("user", CRM.getCurrent());
//        this.function = NORMAL_MAIL;
//    }
//
//    public SendMailView(Context context, String function, byte[] byteIn, List<OfferItem> oiList) {
//        super();
//        this.context = context;
//        if (this.context == null) {
//            this.context = Context.create();
//        }
//        if (CONTRACT_MAIL.equals(function)) {
//            contract = (Contract) context.get("contract");
//            person = contract.getContractPartner();
//        } else {
//            this.offer = (Offer) context.get("offer");
//            this.person = offer.getPerson();
//        }
//        this.context.put("user", CRM.getCurrent());
//        this.function = function;
//        this.oiList = oiList;
//        this.byteIn = byteIn;
//    }
//
//    @Override
//    protected void setupUI() {
//        // build the UserInterface of the SendMailView
//        // Step1: Container
//        Section container = addTab("E-Mail an: " + person + " (" + person.getEmail() + ")").add(Container.class)
//                                                                                           .setAddContainer(true)
//                                                                                           .add(Section.class);
//
//        mailtemplateDD = new ExecutableDropdown<Mailtemplate>("Vorlage");
//        mailtemplateDD.setNullable(true);
//        mailtemplateDD.setTwoCols(true);
//        boolean flagSetItems = false;
//        if (SALESCONFIRMATION_MAIL.equals(function)) {
//            flagSetItems = true;
//            String templatePattern = generateTemplatePattern(oiList, null);
//            List<Mailtemplate> list = OMA.select(Realm.BACKEND, Mailtemplate.class)
//                                         .ilike(templatePattern, Mailtemplate.NAME)
//                                         .orderByAsc(Mailtemplate.NAME)
//                                         .list();
//            if (list.isEmpty()) {
//                list = OMA.select(Realm.BACKEND, Mailtemplate.class)
//                          .ilike("AB_", Mailtemplate.NAME)
//                          .orderByAsc(Mailtemplate.NAME)
//                          .list();
//            }
//            mailtemplateDD.setItems(list);
//        }
//        if (OFFER_MAIL.equals(function)) {
//            flagSetItems = true;
//            List<Mailtemplate> list =
//                    OMA.select(Realm.BACKEND, Mailtemplate.class).eq("Angebot_versenden", Mailtemplate.NAME).list();
//            mailtemplateDD.setItems(list);
//        }
//        if (CONTRACT_MAIL.equals(function)) {
//            flagSetItems = true;
//            String templatePattern = generateTemplatePattern(null, contract);
//            List<Mailtemplate> list = OMA.select(Realm.BACKEND, Mailtemplate.class)
//                                         .ilike("CD_", Mailtemplate.NAME)
//                                         .orderByAsc(Mailtemplate.NAME)
//                                         .list();
//            mailtemplateDD.setItems(list);
//        }
//        if (!flagSetItems) {
//            mailtemplateDD.setItems(OMA.select(Realm.BACKEND, Mailtemplate.class).orderByAsc(Mailtemplate.NAME).list());
//        }
//        mailtemplateDD.setTwoCols(true);
//        container.add(mailtemplateDD);
//        // Step1: SubjectBox
//        subjectBox = container.add(TextBox.class);
//        subjectBox.setLabel("Betreff");
//        subjectBox.setTwoCols(true);
//        subjectBox.setClearLeft(true);
//        // Step2: Textarea for mail-text
//        textArea = container.add(TextArea.class);
//        textArea.setMaximized(true);
//        textArea.setLabel("Text");
//        textArea.setClearLeft(true);
//        textArea.setTwoCols(true);
//        textArea.setValue("\n\n" + CRM.getCurrent().getSignature());
//
//        // Step3: Textarea for attachments
//        attachmentArea = container.add(TextArea.class);
//        attachmentArea.setMaximized(false);
//        attachmentArea.setClearLeft(true);
//        attachmentArea.setLabel("Anlagen");
//        attachmentArea.setValue("keine");
//        attachmentArea.setTwoCols(true);
//
//        // Step4: Addition-Box
//        additionDD = new ExecutableDropdown<Mailtemplate>("Ergänzung");
//        additionDD.setNullable(true);
//        additionDD.setTwoCols(true);
//        List<Mailtemplate> list = OMA.select(Realm.BACKEND, Mailtemplate.class)
//                                     .ilike("ADD_", Mailtemplate.NAME)
//                                     .orderByAsc(Mailtemplate.NAME)
//                                     .list();
//        additionDD.setItems(list);
//        container.add(additionDD);
//
////		// Step5: Send-Button
//        ButtonBar buttonBar = container.add(ButtonBar.class);
//        buttonBar.addButton("Mail senden", new SendAction());
//        additionDD.setActionListener(new TemplateListener());
//        mailtemplateDD.setActionListener(new TemplateListener());
//    }
//
//    private String generateTemplatePattern(List<OfferItem> list, Contract contract) {
//        String patternPackageDefinition = "";
//        if (contract != null) {
//            patternPackageDefinition = contract.getPackageDefinition().getName();
//            return "AB_" + patternPackageDefinition;
//        }
//        boolean flagService = false;
//        boolean flagLicence = false;
//        int anzService = 0;
//        int anzLicense = 0;
//
//        for (OfferItem oi : list) {
//            if (OfferItemType.SERVICE.equals(oi.getOfferItemType())) {
//                flagService = true;
//                anzService++;
//            }
//            if (OfferItemType.LICENSE.equals(oi.getOfferItemType())) {
//                flagLicence = true;
//                anzLicense++;
//                patternPackageDefinition = oi.getPackageDefinition().getName();
//            }
//        }
//        if (flagLicence) {
//            if (anzService == 0 && anzLicense == 1) {
//                return "AB_" + patternPackageDefinition;
//            }
//            return "AB_";
//        } else {
//            return "AB_Universal";
//        }
//    }
//
//    private void invokeTemplate(Mailtemplate mt) {
//        try {
//            if (mt == null) {
//                return;
//            }
//            if (mt.getName().startsWith("ADD_")) {
//                // add the addition to the textArea
//                // Step1: get the template
//                String name = mt.getName();
//                Mailtemplate template =
//                        OMA.select(Realm.BACKEND, Mailtemplate.class).eq(name, Mailtemplate.NAME).first();
//                // Step2: cut out the signature in the textArea
//                String text = textArea.getValue();
//                String signature = CRM.getCurrent().getSignature();
//                int pos = text.indexOf(signature);
//                int l = text.length();
//                String textNew = text.substring(0, pos - 1);
//                signature = text.substring(pos, l);
//                // Step3: add the template
//                text = textNew + ("\n\n");
//                text = text + template.getSubject();
//                text = text + ("\n\n");
//                text = text + template.getMailcontent();
//                text = text + ("\n\n");
//                // Step4: add the signature
//                text = text + signature;
//                textArea.setValue(text);
//            } else {
//                // normal procedure for preparing the mail-window
//                textArea.setValue(ContentGenerator.getInstance()
//                                                  .generateDirectString(mt.getName(), mt.getMailcontent(), context)
//                                  + "\n\n"
//                                  + CRM.getCurrent().getSignature());
//                subjectBox.setValue(ContentGenerator.getInstance()
//                                                    .generateDirectString(mt.getName(), mt.getSubject(), context));
//                attachmentName = null;
//                // set the PDF-File into the AttachmentArea and store the file in the dataArea
//                if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function) || CONTRACT_MAIL.equals(
//                        function)) {
//                    String filenamePDF = (String) context.get("filenamePDF");
//                    attachmentArea.setValue(filenamePDF);
//                    attachmentName = filenamePDF;
////                  am 5.10.2017 stillgelegt, da keine DataArea verwendet wird.
////					// get the dataArea of the mailtemplate
////					DataArea dataArea = areas.getAttachmentsArea(mailtemplateDD
////							.getValue());
////					InputStream inputStream = new ByteArrayInputStream(byteIn);
////					VFile vFile = dataArea.addFile(null, filenamePDF, inputStream);
//                } else {
////					am 5.10.2017 stillgelegt, da keine DataArea verwendet wird.
////					// get the dataArea of the mailtemplate
////					DataArea dataArea = areas.getAttachmentsArea(mailtemplateDD
////							.getValue());
////					// search for the attached files
////					List<VFile> vFileList = dataArea.list(null, null);
////					StringBuilder sb = new StringBuilder();
////					for (VFile vFile : vFileList) {
////						sb.append(vFile.getName() + "   ("
////								+ Tools.convertFileSize(vFile.getSize()) + ")\n");
////
////					}
////					attachmentArea.setValue(vFileList.isEmpty() ? "keine" : sb
////							.toString());
//                    attachmentArea.setValue("keine");
//                }
//            }
//        } catch (Exception e) {
//            ApplicationController.handle(e);
//        }
//    }
}
