/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;


import sirius.kernel.commons.Context;

import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;

import sirius.web.mails.Mails;
import sirius.web.templates.Templates;

import woody.offers.Offer;
import woody.offers.OfferItem;

import woody.offers.ServiceAccountingService;
import woody.sales.Contract;
import woody.xrm.Person;

import javax.activation.DataSource;

import java.time.LocalDateTime;

import java.util.List;

/**
 * Created by gerhardhaufler on 26.10.17.
 */
@Register(classes=SendMailService.class)
public class SendMailServiceBean implements SendMailService {

    @Part
    private DataAreas areas;

    @Part
    private Mails mail;

    @Part
    private static Templates templates;

    private Person person;
    private Context context;
//    private ExecutableDropdown<Mailtemplate> mailtemplateDD;
//    private ExecutableDropdown<Mailtemplate> additionDD;
//    private TextBox subjectBox;
//    private TextArea textArea;
//    private TextArea attachmentArea;
    private String function;
    private List<OfferItem> oiList;
    private Offer offer;
//    private byte[] byteIn;
    private String attachmentName;
    private Contract contract;

    public static final String NORMAL_MAIL = "normalMail";
    public static final String OFFER_MAIL = "offerMail";
    public static final String CONTRACT_MAIL = "contractMail";
    public static final String SALESCONFIRMATION_MAIL = "salesConfirmationMail";


//    private void sendSalesMails() {
//
//        if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function)) {
//            //send the same mail to the 'buyer'
//            if (offer.getBuyer() != null) {
//                Person buyer = offer.getBuyer().getValue();
//                String emailBuyer = buyer.getContact().getEmail();
//                if (Strings.isFilled(emailBuyer)) {
//                    receiver = receiver + " und " + emailBuyer;
//                    mail.createEmail()
//                        .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//                        .subject(subjectBox.getValue())
//                        .to(emailBuyer, buyer.getName())
//                        .textContent(textArea.getValue())
//                        .addAttachment(datasource)
//                        .send();
//
//                    mailDb.setCcAdress(emailBuyer);
//                }
//            }
//        }
//    } else {
//        // send the mail without a attachment
//        String eMailAdr = person.getEmail();
//        receiver = eMailAdr;
//        mail.createEmail()
//            .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//            .subject(subjectBox.getValue())
//            .to(eMailAdr, person.getName())
//            .textContent(textArea.getValue())
//            .send();
//    }



    @Override
    public void prepareMail(Context context, String function, DataSource dataSource) {

        switch (function) {
            case ServiceAccountingService.OFFER:
                String attachmentName = (String) context.get("filenamePDF");
                String plainText = templates.generator().useTemplate("templates/mail-template.vm").applyContext(context).generate();
                sendMail((String) context.get("employeeMail"), (String) context.get("employeeName"),
                         (String) context.get("personMail"), (String)   context.get("personName"),
                         (String) context.get("subject"), plainText,  dataSource,
                         attachmentName);
                if(context.get("buyerMail") != null) {
                    sendMail((String) context.get("employeeMail"), (String) context.get("employeeName"),
                             (String) context.get("buyerMail"), (String)   context.get("buyerName"),
                             (String) context.get("subject"), plainText,  dataSource,
                             attachmentName);
                }
            break;
        }

    }

    // send-Button was clicked
        @Override
        public void sendMail(String senderAddress, String senderName, String receiverAddress, String receiverName,
                             String subject, String mailText, DataSource attachment, String attachmentName) {

            Mail mailDb = new Mail();
            LocalDateTime sendTime = LocalDateTime.now();
            if (attachmentName != null) {
                 // send the mail with attachment
                mail.createEmail()
                    .from(senderAddress, senderName)
                    .subject(subject)
                    .to(receiverAddress, receiverName)
                    .textContent(mailText)
                    .addAttachment(attachment)
                    .send();
            }

//            // store the mail in the Mail-Database
//            mailDb.setSubject(subjectBox.getValue());
//            mailDb.setEmployee(CRM.getCurrent());
//            mailDb.setPerson(person);
//            mailDb.setReceiverAdress(person.getEmail());
//            mailDb.setSenderAdress(Users.getCurrentUser().getEmail());
//            mailDb.setReceivingDate(sendTime.getTime());
//            // algorthm see MailImporter / private boolean importMessageInMail(Message message), line 186
//            mailDb.setMessageId(Tools.md5hex(mailDb.getSubject()
//                                             + NLS.toMachineString(mailDb.getReceivingDate())
//                                             + mailDb.getPerson().getId()));
//            mailDb.setText(textArea.getValue() + "\n\nAnhänge:\n" + attachmentArea.getValue());
//            mailDb = OMA.saveEntity(Realm.BACKEND, mailDb);
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
//            if (SALESCONFIRMATION_MAIL.equals(function)) {
//                // handle the offerItems when sending a sales-confirmation
//                // set the status to confirmation and set the confirmationDate
//                if (oiList != null) {
//                    for (OfferItem oi : oiList) {
//                        oi.setSalesConfirmationDate(sendTime.getTime());
//                        oi.setState(OfferItemState.CONFIRMED);
//                        OMA.saveEntity(Realm.BACKEND, oi);
//                    }
//                }
//            }
//
//            ApplicationController.addSuccessMessage("Mail an " + receiver + " wurde versendet" + fileMessage);
//
//            returnOK();
        }

    @Override
    public void sendOfferMail(Context context) {

    }

    @Override
    public List<String> getTemplateList(Offer offer) {
        return null;
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
