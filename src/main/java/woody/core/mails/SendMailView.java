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
import woody.offers.Offer;
import woody.offers.OfferItem;
import woody.offers.ServiceAccountingService;
import woody.sales.AccountingService;
import woody.sales.Contract;
import woody.xrm.Company;
import woody.xrm.Person;

import javax.activation.DataSource;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gerhardhaufler on 30.01.19.
 */
public class SendMailView {
    private String[] mailTemplateBlackList = {"Angebot_versenden", "AB_", "CD_", "AVV_versenden"};

    @Part
    private ServiceAccountingService sas;

    @Part
    private AccountingService as;

//    @Part
//    private AnswerTemplateService aws;

//    private final class TemplateListener implements ParameterizedActionListener<Mailtemplate> {
//
//        @Override
//        public void action(Mailtemplate value) throws Exception {
//            invokeTemplate(value);
//        }
//    }

//    private final class SendAction implements ActionListener {
//        // send-Button was clicked
//        @Override
//        public void action() throws Exception {
//            String receiver = "";
//            LocalDateTime timestamp = LocalDateTime.now();
//            Mail mailDb = sas.initMailDbElement(subjectBox.getValue(), person, timestamp);
//            String documentIdString = mailDb.getUnqiueObjectName();
//            String mailtext = textAreaView.getValue();
//            String signature = CRM.getCurrent().getSignature();
//            int index = mailtext.indexOf(signature);
//            if(index >= 0 && index < 3) {
//                throw new BusinessException("Die Mail enthält keinen Text sondern nur Freundliche Grüße ...");
//            }
//            mailtext = aws.activateAnswers(mailtext, documentIdString, CRM.getCurrent());
//            // append a footer with the scireum-signature at the end of the mail-texty
//            mailtext = mailtext + ContentGenerator.getInstance().generateString("scireum_signature.vm", context);
//
//            mailtext = mailtext + "\n\nAnhänge:\n" + attachmentArea.getValue();
//
//            mailDb.setText(mailtext);
//            mailDb = OMA.saveEntity(Realm.BACKEND, mailDb);
//
//            if (attachmentName != null) {
//                DataSource datasource = new DataSource() {
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
//                };
//
//
//                // send the mail with attachment
//                receiver = person.getEmail();
//                mail.createEmail()
//                    .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//                    .subject(subjectBox.getValue())
//                    .to(person.getEmail(), person.getName())
//                    .textContent(mailtext)
//                    .addAttachment(datasource)
//                    .send();
//
//                if(OFFER_MAIL.equals(function)) {
//                    List<Person> additionalReceiverList = ( List<Person>)context.get("additionalReceiverList");
//                    if(additionalReceiverList != null) {
//                        for(Person person : additionalReceiverList) {
//                            String text = MessageFormat.format("{0} \n\n die nachfolgende Mail erhalten Sie zu Ihrer Information. \n\n", person.getLetterSalutation() );
//                            mailtext = text + mailtext;
//                            mail.createEmail()
//                                .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//                                .subject(subjectBox.getValue())
//                                .to(person.getEmail(), person.getName())
//                                .textContent(mailtext)
//                                .addAttachment(datasource)
//                                .send();
//                        }
//                    }
//
//                }
//
//                if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function)) {
//                    //send the same mail to the 'buyer'
//                    if (offer.getBuyer() != null && !offer.getBuyer().isOffline()) {
//                        Person buyer = offer.getBuyer();
//                        String emailBuyer = buyer.getEmail();
//                        if (Tools.notEmpty(emailBuyer)) {
//                            receiver = receiver + " und " + emailBuyer;
//                            mail.createEmail()
//                                .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//                                .subject(subjectBox.getValue())
//                                .to(emailBuyer, buyer.getName())
//                                .textContent(mailtext)
//                                .addAttachment(datasource)
//                                .send();
//
//                            mailDb.setCcAdress(emailBuyer);
//                        }
//                    }
//                }
//            } else {
//                // send the mail without a attachment
//                receiver = person.getEmail();
//
//                mailtext = mailtext.replace("\n", "<br>");
//                mailtext = mailtext.replace("\\n", "<br>");
//                mail.createEmail()
//                    .from(Users.getCurrentUser().getEmail(), Users.getCurrentUser().getFullName())
//                    .subject(subjectBox.getValue())
//                    .to(person.getEmail(), person.getName())
//                    .textContent(mailtext)
//                    .htmlContent(mailtext)
//                    .includeHTMLPart(true)
//                    .send();
//            }
//
//            // set the sendDate by the AVV_Mail
//            if (AVV_MAIL.equals(function)) {
//                company.setDataPrivacySendDate(new Date());
//                OMA.saveEntity(Realm.BACKEND, company);
//            }
//
//            // store a mail-attachment as 'attachment' to the mail
//            if (Tools.notEmpty(attachmentName) && !"keine".equals(attachmentName)) {
//                String namedObjectName = mailDb.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(timestamp, namedObjectName, byteIn, attachmentName);
//            }
//
//            String fileMessage = ".";
//            // store th pdf-Document (offer) in a 'offer-attachment'
//            if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function)) {
//                // store the pdf-File as attachment
//                String namedObjectName = offer.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(timestamp, namedObjectName, byteIn, attachmentName);
//                fileMessage = " und bei " + file.getFile().getPath() + " gespeichert.";
//            }
//            // store th pdf-Document (contract) in a 'company-attachment'
//            if (CONTRACT_MAIL.equals(function)) {
//                // store the pdf-File as attachment
//                Company company = contract.getCompany();
//                String namedObjectName = company.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(timestamp, namedObjectName, byteIn, attachmentName);
//                fileMessage = " und bei " + file.getFile().getPath() + " gespeichert.";
//            }
//
//            if (AVV_MAIL.equals(function)) {
//                // store the pdf-File as attachment
//                Company company = person.getCompany();
//                String namedObjectName = company.getUnqiueObjectName();
//                VFile file = sas.storePdfInAttachment(timestamp, namedObjectName, byteIn, attachmentName);
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
//                           + mailtext
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
//                // set the status to 'confirmed' and set the confirmationDate
//                if (oiList != null) {
//                    for (OfferItem oi : oiList) {
//                        oi.setSalesConfirmationDate(Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));
//                        oi.setState(OfferItemState.CONFIRMED);
//                        OMA.saveEntity(Realm.BACKEND, oi);
//                    }
//                }
//            }
//
//            if(OFFER_MAIL.equals(function)) {
//                if(offer.isShop()) {
//                    if (oiList != null) {
//                        for (OfferItem oi : oiList) {
//                            String remark = oi.getRemark();
//                            remark = remark + as.createStringGroup(ServiceAccountingService.OFFER_SEND_DATE, NLS.toUserString(new Date(), true), ";");
//                            oi.setRemark(remark);
//                            OMA.saveEntity(Realm.BACKEND, oi);
//                        }
//                    }
//                }
//            }
//
//            // display a success-message
//            ApplicationController.addSuccessMessage("Mail an " + receiver + " wurde versendet" + fileMessage);
//
//            returnOK();
//        }
//    }

    @Part
    private DataAreas areas;
    @Part
    private SendMailService mail;

    private Person person;
    private Company company;
    private Context context;
//    private ExecutableDropdown<Mailtemplate> mailtemplateDD;
//    private ExecutableDropdown<Mailtemplate> additionDD;
    //    private TextBox toBox;
//    private TextBox ccBox;
//    private TextBox subjectBox;
//    private TextArea textArea;
//    private TextArea textAreaView;
//    private TextArea attachmentArea;
    private String function;
    private List<OfferItem> oiList;
    private Offer offer;
    private byte[] byteIn;
    private String attachmentName;
    private Contract contract;
    private boolean mailTemplateUsed;

    public static final String NORMAL_MAIL = "normalMail";
    public static final String OFFER_MAIL = "offerMail";
    public static final String CONTRACT_MAIL = "contractMail";
    public static final String SALESCONFIRMATION_MAIL = "salesConfirmationMail";
    public static final String AVV_MAIL = "avvMail";

//    public SendMailView(Person person, Context context) {
//        super();
//        this.person = person;
//        this.context = context;
//        if (this.context == null) {
//            this.context = Context.create();
//        }
//        this.context.put("person", person);
//        this.context.put("user", CRM.getCurrent());
//        this.context.put("subject", "");
//        this.function = NORMAL_MAIL;
//    }
//
//    public SendMailView(Context context, String function, byte[] byteIn) {
//        super();
//        this.context = context;
//        if (this.context == null) {
//            this.context = Context.create();
//        }
//        if (CONTRACT_MAIL.equals(function)) {
//            contract = (Contract) context.get("contract");
//            person = contract.getActualContractPartner();
//        } else {
//            if (AVV_MAIL.equals(function)) {
//                this.company = (Company) context.get("company");
//                this.person = (Person) context.get("person");
//            } else {
//                this.offer = (Offer) context.get("offer");
//                this.person = offer.getPerson();
//            }
//        }
//        this.context.put("user", CRM.getCurrent());
//        this.function = function;
//        this.oiList = (List<OfferItem>)context.get("offerItemList");
//        this.byteIn = byteIn;
//    }

//    @Override
//    protected void setupUI() {
//        mailTemplateUsed = false;
//        // build the UserInterface of the SendMailView
//        // step1: container and checks
//        if (person == null) {
//            throw new BusinessException("Es ist kein Empfänger angegeben --> Abbruch!");
//        }
//        Person buyer = (Person) context.get("buyer");
//        String text = MessageFormat.format("E-Mail an: {0} {1}.", person.toString(), person.getEmail());
//        if (person.isOffline()) {
//            throw new BusinessException(MessageFormat.format("{0} ist offline --> keine Mail möglich.",
//                                                             person.toString()));
//        }
//        if (Tools.emptyString(person.getEmail())) {
//            throw new BusinessException(MessageFormat.format("Die Person: {0} hat keine Mail-Adresse --> Abbruch!",
//                                                             person.toString()));
//        }
//        if (buyer != null && !buyer.isOffline()) {
//            text = text + MessageFormat.format("    und an: {0} {1}", buyer.toString(), buyer.getEmail());
//        }
//        if (buyer != null && buyer.isOffline()) {
//            text = text + MessageFormat.format(" {0} ist offline --> keine Mail an diesen Empfänger möglich.", buyer.toString());
//        }
//        List<Person> additionalReceiverList = ( List<Person>)context.get("additionalReceiverList");
//        if(additionalReceiverList != null) {
//            text = text + " und zur Information an: ";
//            for(Person person : additionalReceiverList) {
//                text = text + MessageFormat.format("  {0} {1}   ", person.toString(), person.getEmail());
//            }
//        }
//        Section container = addTab(text).add(Container.class).setAddContainer(true).add(Section.class);

//        // step2: toBox
//        toBox = container.add(TextBox.class);
//        toBox.setLabel("To");
//        toBox.setTwoCols(true);
//        toBox.setClearLeft(true);
//        toBox.setValue(person.getEmail());

//        // step3: ccBox
//        ccBox = container.add(TextBox.class);
//        ccBox.setLabel("CC");
//        ccBox.setTwoCols(true);
//        ccBox.setClearLeft(true);
//        if(buyer != null && !buyer.isOffline()) {
//            ccBox.setValue(buyer.getEmail());
//        }

//        // step4: drop-down-menue for the mail-template
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
//        if(NORMAL_MAIL.equals(function)) {
//            flagSetItems = true;
//            List<Mailtemplate> list =
//                    OMA.select(Realm.BACKEND, Mailtemplate.class).list();
//            List<Mailtemplate> list1 = new ArrayList();
//            for(Mailtemplate mailtemplate : list) {
//                boolean flag = true;
//                for(int i = 0; i < mailTemplateBlackList.length; i++) {
//                    if(mailtemplate.getName().startsWith(mailTemplateBlackList[i])) {
//                        flag = false;
//                        break;
//                    }
//                }
//                if(flag) {
//                    list1.add(mailtemplate);
//                }
//            }
//            mailtemplateDD.setItems(list1);
//        }
//        if (AVV_MAIL.equals(function)) {
//            flagSetItems = true;
//            List<Mailtemplate> list =
//                    OMA.select(Realm.BACKEND, Mailtemplate.class).eq("AVV_versenden", Mailtemplate.NAME).list();
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
//
//        if(mailtemplateDD.getItems().size() != 1) {
//            mailtemplateDD.setActionListener(new TemplateListener());
//            container.add(mailtemplateDD);
//        }
//
//        // step5: subjectbox
//        subjectBox = container.add(TextBox.class);
//        subjectBox.setLabel("Betreff");
//        subjectBox.setTwoCols(true);
//        subjectBox.setClearLeft(true);
//
//        // step6: textarea for mail-text
//        // there are two textAreas:
//        // one textArea for the full text with the complete http-link to send it with the mail and
//        // a second textAreaView, only for text to view it on the display
//        textAreaView = container.add(TextArea.class);
//        textAreaView.setMaximized(true);
//        textAreaView.setLabel("Text");
//        textAreaView.setClearLeft(true);
//        textAreaView.setTwoCols(true);
//        String defaultText = "\n\n" + CRM.getCurrent().getSignature();
//        textAreaView.setValue(defaultText);
//
//        textArea = new TextArea();
//        textArea.setValue(defaultText);
//
//        // step7: textarea for attachments
//        attachmentArea = container.add(TextArea.class);
//        attachmentArea.setMaximized(false);
//        attachmentArea.setClearLeft(true);
//        attachmentArea.setLabel("Anlagen");
//        attachmentArea.setValue("keine");
//        attachmentArea.setTwoCols(true);

/* -------------------- zurzeit nicht realisiert -------------------------
        // step6: addition-box
        additionDD = new ExecutableDropdown<Mailtemplate>("Ergänzung");
        additionDD.setNullable(true);
        additionDD.setTwoCols(true);
        List<Mailtemplate> list = OMA.select(Realm.BACKEND, Mailtemplate.class)
                .ilike("ADD_", Mailtemplate.NAME)
                .orderByAsc(Mailtemplate.NAME)
                .list();
        additionDD.setItems(list);
        additionDD.setActionListener(new TemplateListener());
        container.add(additionDD);
------------------------------------------------------------------------ */

//        // step8: send-button
//        ButtonBar buttonBar = container.add(ButtonBar.class);
//        buttonBar.addButton("Mail senden", new SendAction());
//
//        // if there is only one mailtemplate valid invoke this template
//        if(mailtemplateDD.getItems().size() == 1) {
//            Mailtemplate mailtemplate = mailtemplateDD.getItems().get(0);
//            invokeTemplate(mailtemplate);
//        }
//
//    }

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
//                // Step5: analyze the text and insert answers
//                // there are two textAreas:
//                // one textArea for the full text with the complete http-link to send it with the mail and
//                // a second textAreaView, only for text to view it on the display
//                String textView = text;
//                textView = aws.activateAnswers(textView, null, null);
//                textAreaView.setValue(textView);
//                //the sended mailtext is activatet in the send-action
////                text = aws.activateAnswers(text, true);
//                textArea.setValue(text);
//
//            } else {
//                // normal procedure for preparing the mail-window
//                // prepare the text with the parametrs from the content
//                String text =  ContentGenerator.getInstance()
//                                               .generateDirectString(mt.getName(), mt.getMailcontent(), context)
//                               + "\n\n"
//                               + CRM.getCurrent().getSignature();
//                // there are two textAreas:
//                // one textArea for the full text with the complete http-link to send it with the mail and
//                // a second textAreaView, only for text to view it on the display
//                String textView = text;
//                textAreaView.setValue(aws.activateAnswers(textView, null, null));
//                // the sended mailtext is activatet in the send-action
////                text = aws.activateAnswers(text,true);
//                textArea.setValue(text);
//                subjectBox.setValue(ContentGenerator.getInstance()
//                                                    .generateDirectString(mt.getName(), mt.getSubject(), context));
//                attachmentName = null;
//                // set the PDF-File into the AttachmentArea and store the file in the dataArea
//                if (OFFER_MAIL.equals(function) || SALESCONFIRMATION_MAIL.equals(function) || CONTRACT_MAIL.equals(
//                        function) || AVV_MAIL.equals(function)) {
//                    String filenamePDF = (String) context.get("filenamePDF");
//                    attachmentArea.setValue(filenamePDF);
//                    attachmentName = filenamePDF;
//                } else {
//                    attachmentArea.setValue("keine");
//                }
//            }
//        } catch (Exception e) {
//            ApplicationController.handle(e);
//        }
//    }

}
