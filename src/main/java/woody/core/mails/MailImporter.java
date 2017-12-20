/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import com.google.common.collect.Lists;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPMessage;
import sirius.biz.model.ContactData;
import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Strings;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import woody.core.employees.Employee;
import woody.offers.ServiceAccountingService;
import woody.xrm.Person;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.DateTerm;
import javax.mail.search.ReceivedDateTerm;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by gerhardhaufler on 16.10.17.
 */
public class MailImporter {

    @sirius.kernel.di.std.Part
    protected OMA oma;

    @sirius.kernel.di.std.Part
    private static ServiceAccountingService sas;

// ToDo wieder reinmachen
//    @Override
//    public void runTimer() throws Exception {
//        if (Model.isDebugEnvironment()) {
//            CRM.LOG.INFO("Not fetching any mails in the debug system!");
//            return;
//        }

//        LocalDate limit = LocalDate.now();
//        limit.minusDays(1);
//        fetchMails(limit);
//
//    }

    protected int fetchMails(LocalDate limit) throws NoSuchProviderException, MessagingException {
        int numFetched = 0;


        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "gimap");
        props.setProperty("mail.gimap.socketFactory.class",
                          "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.gimap.socketFactory.fallback", "false");
        props.setProperty("mail.gimap.socketFactory.port", "993");
        List<UserAccount> userList = oma.select(UserAccount.class).queryList() ;
        for (UserAccount user : userList) {
            Employee emp = user.as(Employee.class);
            if(Strings.isEmpty(emp.getEmailPassword())) {continue;}
            if(emp.isInaktiv()) {continue;}


            try {
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore("gimap");
                store.connect("imap.googlemail.com", user.getEmail(),
                              emp.getEmailPassword());
// ToDo Meldung ausgeben
//                Syslog.log("MAIL-IMPORT",
//                           "Importing Mails from: " + user.getEmail());
                try {
                    Folder inbox = store.getFolder("[Gmail]").getFolder(
                            "Alle Nachrichten");
                    inbox.open(Folder.READ_ONLY);
                    Date limitDate = Date.from(limit.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Message messages[] = inbox.search(new ReceivedDateTerm(
                            DateTerm.GT, limitDate));
                    for (Message message : messages) {
                        if (importMessageInMail(message)) {
                            numFetched++;
                        }
                    }
                } finally {
                    if (store.isConnected()) {
                        store.close();
                    }
                }
            } catch (Throwable t) {
                Exceptions.handle(new Exception("Error importing mails from: "
                                               + emp.getShortName() + ": " + t.getMessage(), t));
            }
        }
          return numFetched;
    }


    /**
     * import a mail-message in a Mail
     */
    private boolean importMessageInMail(Message message) {
        try {
            List<String> labels = Lists.newArrayList(((GmailMessage) message).getLabels());
            if (labels.contains("\\Draft") || labels.contains("\\Spam")) {
                return false;
            }
            Mail mail = new Mail();
            mail.setMessageId(((IMAPMessage) message).getMessageID());
            if (message.getReceivedDate() != null) {
                Date date = message.getReceivedDate();
                LocalDateTime localDateTime = transformToLocalDateTime(date);
                mail.setReceivingDate(localDateTime);
            } else if (message.getSentDate() != null) {
                Date date = message.getSentDate();
                mail.setReceivingDate(transformToLocalDateTime(date));
            }
            mail.setSubject(message.getSubject());
            StringBuilder sb = new StringBuilder();
            StringBuilder adrSb = new StringBuilder();
            for (Address addr : message.getFrom()) {
                sb.append("von: " + ((InternetAddress) addr).getAddress()
                          + "\n ");
                resolve(addr, mail);
                adrSb.append(((InternetAddress) addr).getAddress()).toString();
                adrSb.append(" ");
            }
            String s = adrSb.toString();
            if (Strings.isFilled(s)) {
                mail.setSenderAddress(s.trim());
            }
            if (message.getRecipients(Message.RecipientType.TO) != null) {
                adrSb = new StringBuilder();
                for (Address addr : message.getRecipients(Message.RecipientType.TO)) {
                    sb.append("an: " + ((InternetAddress) addr).getAddress()
                              + "\n ");
                    resolve(addr, mail);
                    adrSb.append(((InternetAddress) addr).getAddress())
                         .toString();
                    adrSb.append(" ");
                }
                s = adrSb.toString();
                if (Strings.isFilled(s)) {
                    mail.setReceiverAddress(s.trim());
                }
            }
            if (message.getRecipients(Message.RecipientType.CC) != null) {
                adrSb = new StringBuilder();
                for (Address addr : message.getRecipients(Message.RecipientType.CC)) {
                    sb.append("cc: " + ((InternetAddress) addr).getAddress()
                              + "\n ");
                    resolve(addr, mail);
                    adrSb.append(((InternetAddress) addr).getAddress())
                         .toString();
                    adrSb.append(" ");
                }
                s = adrSb.toString();
                if (Strings.isFilled(s)) {
                    mail.setCcAddress(adrSb.toString().trim());
                }
            }
            if (message.getRecipients(Message.RecipientType.BCC) != null) {
                adrSb = new StringBuilder();
                for (Address addr : message.getRecipients(Message.RecipientType.BCC)) {
                    sb.append("bcc: " + ((InternetAddress) addr).getAddress()
                              + "\n ");
                    resolve(addr, mail);
                    adrSb.append(((InternetAddress) addr).getAddress())
                         .toString();
                    adrSb.append(" ");
                }
                s = adrSb.toString();
                if (Strings.isFilled(s)) {
                    mail.setBccAddress(adrSb.toString().trim());
                }
            }
            // ToDo noch migrieren
            if (mail.getEmployeeEntity() == null || mail.getPersonEntity() == null) {
                return false;
            }
            if (Strings.isEmpty(mail.getMessageId())) {
                mail.setMessageId(sas.buildMd5HexString(mail.getSubject()
                                               + NLS.toMachineString(mail.getReceivingDate())
                                               + mail.getPersonEntity().getValue().getId()));
            }
            if (!oma.select(Mail.class)
                    .eq( Mail.MESSAGEID, mail.getMessageId()).exists()) {
                mail.setText(sb.toString() + "\n\n" + readContent(message));
                oma.update(mail);
                // ToDo implementieren
//               Syslog.log("MAIL-IMPORT", "Imported: " + mail.getSubject());

                addAttachments(message, NamedObject.createNamedObject( mail));
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            try {
                Exceptions.handle(new Exception("Error Importing: "
                                               + message.getSubject() + " (from: "
                                               + message.getFrom()[0] + ")", e));
            } catch (MessagingException e1) {
                // IGNORE
            }
        }
        return false;
    }

    private LocalDateTime transformToLocalDateTime(Date date) throws MessagingException {
        Instant instant = date.toInstant() ;
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String readContent(Message message) {
        try {
            if (message.getContent() == null) {
                return null;
            } else if (message.getContent() instanceof String) {
                return (String) message.getContent();
            } else if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mm = (MimeMultipart) message.getContent();
                return readMultipart(mm);
            }
        } catch (Throwable e) {
            Exceptions.handle(e);
        }
        return null;
    }

    protected static String readMultipart(MimeMultipart mm)
            throws MessagingException, IOException {
        String result = "";
        for (int i = 0; i < mm.getCount(); i++) {
            BodyPart mbp = mm.getBodyPart(i);
            if (mbp.isMimeType("text/plain")) {
                result += mbp.getContent().toString();
            }
            if (mbp.isMimeType("multipart/alternative")) {
                result += readMultipart((MimeMultipart) mbp.getContent());
            }
            if (mbp.isMimeType("multipart/related")) {
                result += readMultipart((MimeMultipart) mbp.getContent());
            }
        }
        if (Strings.isFilled(result)) {
            return result;
        }
        for (int i = 0; i < mm.getCount(); i++) {
            BodyPart mbp = mm.getBodyPart(i);
            if (mbp.isMimeType("text/html")) {
                return mbp.getContent().toString();
            }
        }

        return null;
    }

    @sirius.kernel.di.std.Part
    private static DataAreas areas;

    public static void addAttachments(Message message, NamedObject no) {
        try {
            if (message.getContent() == null
                || !(message.getContent() instanceof MimeMultipart)) {
                return;
            }
            MimeMultipart mm = (MimeMultipart) message.getContent();
            processMultipart(no, mm);
        } catch (Throwable e) {
            Exceptions.handle(e);
        }
    }

    protected static void processMultipart(NamedObject no, MimeMultipart mm)
            throws MessagingException, UnsupportedEncodingException,
                   IOException {
        for (int i = 0; i < mm.getCount(); i++) {
            BodyPart mbp = mm.getBodyPart(i);
            if (mbp.isMimeType("multipart/ALTERNATIVE")) {
                processMultipart(no, (MimeMultipart) mbp.getContent());
            } else if (mbp.getFileName() != null
                       && mbp.getDisposition() != null
                       && mbp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)
                       && !mbp.isMimeType("APPLICATION/MS-TNEF")) {
                String filename = MimeUtility.decodeText(mbp.getFileName());
                areas.get().getAttachmentsArea(no)
                     .addFile(null, filename, mbp.getInputStream());
            } else if (mbp.getDisposition() != null
                       && mbp.getDisposition().equalsIgnoreCase(Part.INLINE)
                       && mbp.isMimeType("image/png")) {
                String filename = "inline-" + i + ".png";
                areas.get().getAttachmentsArea(no)
                     .addFile(null, filename, mbp.getInputStream());
            } else if (mbp.getDisposition() != null
                       && mbp.getDisposition().equalsIgnoreCase(Part.INLINE)
                       && mbp.isMimeType("image/jpeg")) {
                String filename = "inline-" + i + ".jpg";
                areas.get().getAttachmentsArea(no)
                     .addFile(null, filename, mbp.getInputStream());
            } else if (mbp.isMimeType("text/html")) {
                // save all non text/plain parts as attachments
                String filename = "html-" + i + ".html";
                areas.get().getAttachmentsArea(no)
                     .addFile(null, filename, mbp.getInputStream());
            }
        }
    }

    /**
     * resolves the given internet-adress of the mail into the Mail-Parameters
     */
    private void resolve(Address addr, Mail mail) {
        InternetAddress iaddr = (InternetAddress) addr;
        // ToDo statt eq eqIgnoreCase realisieren
        UserAccount ua = oma.select(UserAccount.class).eq(UserAccount.EMAIL, iaddr.getAddress()).queryFirst();

        if (ua != null) {
            Employee e = ua.as(Employee.class);
            if (e != null) {
                if (mail.getEmployeeEntity() == null) {
                    mail.getEmployeeEntity().setValue(ua);
                }
            }
        } else {
            if (mail.getPersonEntity() == null) {
                Person person = oma.select(Person.class).eq(Person.CONTACT.inner(ContactData.EMAIL), iaddr.getAddress())
                                   // ToDo noch reinbauen                                  .eq(true, Person.SCAN_EMAIL)
                                   .queryFirst();
                if (person != null) {
                    mail.getPersonEntity().setValue(person);
                }
            }
        }
    }
            // ToDo wieder aktivieren
//
//    @Override
//    public void execute(PrintWriter output, String... params) throws Exception {
// // ToDo auf LocalDate umstellen
// Calendar limit = Calendar.getInstance();
//        limit.add(Calendar.DAY_OF_MONTH, -3);
//
//        if (params.length > 0) {
//            limit = NLS.parseUserString(Calendar.class, params[0]);
//        }
//        output.println(fetchMails(limit));
//    }
//
//    @Override
//    public String getName() {
//        return "fetchMails";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Importiert Mails";
//    }

}
