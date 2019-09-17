/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.http.MimeHelper;
import sirius.web.mails.Mails;
import woody.core.employees.Employee;
import woody.offers.Offer;
import woody.offers.OfferItem;
import woody.offers.OfferItemState;
import woody.offers.ServiceAccountingService;
import woody.xrm.Person;

import javax.activation.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.CharacterIterator;
import java.text.MessageFormat;
import java.text.StringCharacterIterator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardhaufler on 18.03.19.
 */
@Register(classes=MailService.class)
public class MailServiceBean implements MailService {

    @Part
    private static OMA oma;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Mails mail;

    @Override
    public List<String> getFunctionList() {
        List<String> list = new ArrayList();
        for(int i = 0; i<functions.length; i++) {
            list.add(functions[i]);
        }
        return list;
    }

    @Override
    public Mail createMailToPerson(Person person, UserAccount uac) {
        Mail mail = new Mail();
        mail.getPersonEntity().setValue(person);
        mail.getEmployeeEntity().setValue(uac);
        oma.update(mail);
        return mail;
    }

    @Override
    public String getPersonMailAddress(Person person) {
        if(person == null) {return null;}
        if(person.getContact() == null) {return null;}
        if(Strings.isEmpty(person.getContact().getEmail())) {return null;}
        return person.getContact().getEmail();
    }

    @Override
    public String getUacMailAddress(UserAccount uac) {
        if(uac == null) {return null;}
        if(uac.getEmail() == null) {return null;}
        if(Strings.isEmpty(uac.getEmail())) {return null;}
        return uac.getEmail();
    }

    @Override
    public List<String> prepareAndSendMail(Offer offer, Mail mail) {
        List<String> messageList = new ArrayList<String>();
        String function = mail.getFunction();
        Context context = null;
        LocalDateTime sendTime = null;
        String text = mail.getText();
        UserAccount uac = mail.getEmployeeEntity().getValue();
        Employee employee = uac.as(Employee.class);
        String signature = employee.getSignature();
        text = text + "\n\n" + signature;
        mail.setText(text);
        String plainText = text;
        Person person = mail.getPersonEntity().getValue();
        String subject  = mail.getSubject();
        String senderName = uac.getPerson().getAddressableName();
        String messageTemplate =  "Mail mit Id: {0} an {1} wurde versendet";
        String message = "";
        switch (function) {

            case ServiceAccountingService.OFFER:
            case ServiceAccountingService.SALES_CONFIRMATION:
                context = sas.prepareContext(offer, mail.getFunction()) ;
// ToDo auf pasta umstellen
                File fileAttachment = sas.createPdfFromContext(context, "templates/offer.pdf.pasta");
//                File fileAttachment = sas.createPdfFromContext(context, "templates/offer.pdf.vm");
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

                message = MessageFormat.format(
                        messageTemplate, mail.getId(), mail.getReceiverAddress());
                messageList.add(message);

                if (context.get("buyerMail") != null) {
                    sendMail((String) context.get("employeeMail"), senderName,
                             (String) context.get("buyerMail"), (String) context.get("buyerName"),
                             subject, plainText, dataSource, filenamePDF);
                    mail.setCcAddress((String) context.get("buyerMail"));
                    message = MessageFormat.format(
                            messageTemplate, mail.getId(), (String) context.get("buyerMail"));
                    messageList.add(message);
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
                message = MessageFormat.format(
                        messageTemplate, mail.getId(), mail.getReceiverAddress());
                messageList.add(message);
                break;
        }

        // algorithm see MailImporter / private boolean importMessageInMail(Message message), line 186
        mail.setMessageId(sas.buildMd5HexString(mail.getSubject()
                                                + NLS.toMachineString(mail.getReceivingDate() == null ? mail.getReceivingDate() : mail.getSendDate())
                                                + mail.getPersonEntity().getId()));
        oma.update(mail);

        switch (function) {
            case ServiceAccountingService.SALES_CONFIRMATION :
                // set the salesConfirmationDate = now and the state = CONFIRMED
                LocalDate confirmationDate = LocalDate.now();
                List<OfferItem> confirmationList = (List<OfferItem>)context.get("offerItemList");

                for (OfferItem oi : confirmationList) {
                    if(oi.getSalesConfirmationDate() == null) {
                        oi.setSalesConfirmationDate(confirmationDate);
                        oi.setState(OfferItemState.CONFIRMED);
                        oma.update(oi);
                    }
                    // Ggfs. Verträge aus den Auftragsbestätigungen anlegen
                    messageList.add(sas.createContractFromOfferItem(oi));

                }
                break;


            default:
                break;
        }

        return messageList;

    }


    @Override
    public void sendMail(String senderAddress, String senderName, String receiverAddress, String receiverName,
                         String subject, String mailText, DataSource attachment, String attachmentName) {
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
    }
    /**
     * Escapes the given string for use in XML or HTML.
     */
    private static String escapeXML(Object aText) {
        if (Strings.isEmpty(aText)) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText.toString());
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                // the char is not a special one
                // add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    /**
     * Replaces new line with <br>
     * tags
     */
    private static String nl2br(String content) {
        if (content == null) {
            return null;
        }
        content = content.replace("\n", "<br />");
        content = content.replace("\r", "");
        return content;
    }

    // ToDo allgemeine Lösung als Makro machen
    @Override
    public String transformToHtml(String string) {
        string = escapeXML(string);
        string = nl2br(string);
        return string;
    }

}
