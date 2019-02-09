/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.tracking.mails;

import sirius.biz.web.BizController;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;

/**
 * Created by aha on 11.05.15.
 */
@Framework("mails")
@Register(classes = Controller.class)
public class MailController extends BizController {
//
//    private static final String MANAGE_XRM = "permission-manage-xrm";
//    public static final String MANAGE_OFFER = "permission-manage-offers";
//
//    @Part
//    private AccountingService asb;
//
//    @Part
//    private static ServiceAccountingService sas;
//
//    @Part
//    private static Templates templates;
//
//    @Part
//    private static SendMailService sms;
//
//    // Taste 'Mail senden' wurde gedrückt
//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/mail/:1/sendOffer")
//    public void sendMail(WebContext ctx, String mailId) {
//
//        Mail mail = find(Mail.class, mailId);
//        Offer offer = null;
//        List<String> templateList = new ArrayList<String>();
//        if(mail.getTemplate() != null) {
//            templateList.add(mail.getTemplate());
//        }
//        if(mail == null) {
//            ctx.respondWith().template("view/mails/mail-details.html", templateList, mail, mail.getFunction());
//            return;
//        }
//        if(Strings.isEmpty(mail.getSubject())) {
//            UserContext.message(Message.info("Bei der Mail ist der 'Betreff' leer, deshalb kann keine Mail gesendet werden."));
//            ctx.respondWith().template("view/mails/mail-details.html", templateList, mail, mail.getFunction());
//            return;
//        }
//        switch (mail.getFunction()) {
//            case ServiceAccountingService.OFFER:
//            case ServiceAccountingService.SALES_CONFIRMATION:
//                if (mail.getUsageId() != null) {
//                    offer = buildOfferFromMail(mail);
//                    break;
//                }
//            case ServiceAccountingService.NORMAL_MAIL:
//                break;
//        }
//        List<String> messageList = sms.prepareAndSendMail(offer, mail);
//        for(String s: messageList) {
//            UserContext.message(Message.info(s));
//        }
//
//        ctx.respondWith().template("view/mails/mail-details.html", templateList, mail, mail.getFunction());
//    }
//
//    private Offer buildOfferFromMail(Mail mail) {
//        Offer offer = null;
//        if(mail.getUsageId() != null && mail.getUsageId().contains("-")) {
//            String[] fields = mail.getUsageId().split("-");
//            String classname = fields[0];
//            if("OFFER".equals(classname.toUpperCase())) {
//                String id = fields[1];
//                offer = find(Offer.class, id);
//            }
//        }
//        return offer;
//    }
//
//    // send a normal mail to a person
//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/company/:1/person/:2/sendMailToPerson")
//    // Funktion Normale Mail an Person senden, z. B. durch Anklicken der Mail-Adresse
//    public void sendNormalMailToPerson(WebContext ctx, String companyId, String personId) {
//        String mailTemplateName =  ServiceAccountingService.NORMAL_MAIL;
//        String function = ServiceAccountingService.NORMAL_MAIL;
//        Company company = find(Company.class, companyId);
//        Person person = find(Person.class, personId);
//        Mail mail = new Mail();
//        String receiver = person.getContact().getEmail();
//        UserInfo ui = UserContext.getCurrentUser();
//        UserAccount uac = ui.as(UserAccount.class);
//        String sender = uac.getEmail();
//        mail.getEmployeeEntity().setValue(uac);
//        mail.getPersonEntity().setValue(person);
//        mail.setReceiverAddress(receiver);
//        mail.setSenderAddress(sender);
//        mail.setFunction(function);
//        mail.setTemplate(mailTemplateName);
//        mail.setAttachmentName("keine");
//        Mailtemplate mailtemplate = oma.select(Mailtemplate.class).eq(Mailtemplate.NAME, mailTemplateName).queryFirst();
//        Employee employee = uac.as(Employee.class);
//        Context context = new Context();
//        context.set("employeeSignature", employee.getSignature());
//        context.set("salutation",person.getLetterSalutation());
//
//        if(mail.getText() == null) {
//// ToDo von Velocity auf tagliatelle umstellen
//
//            String mailText = templates.generator()
//                                       .direct(mailtemplate.getMailcontent(), VelocityContentHandler.VM)
//                                       .applyContext(context)
//                                       .generate();
//            mail.setText(mailText);
//        }
//        oma.update(mail);
//        // Mail-Template in Liste vorbelegen
//        List<String> templateList = new ArrayList<String>();
//        if (mail.getTemplate() != null) {
//            templateList.add(mail.getTemplate());
//        }
//        ctx.respondWith().template("view/mails/mail-details.html", templateList, mail, mail.getFunction());
//    }
//
//    // Taste Speichern bei view 'mail-details.html' wurde gedrückt
//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/mail/:1")
//    public void saveMail(WebContext ctx, String mailId) {
//        Mail mail = find(Mail.class, mailId);
//        // Mail-Template in Liste vorbelegen
//        List<String> templateList = new ArrayList<String>();
//        if (mail.getTemplate() != null) {
//            templateList.add(mail.getTemplate());
//        }
//        // Daten des Editors in mail speichern
//        if (ctx.isPOST()) {
//            try {
//                boolean wasNew = mail.isNew();
//                load(ctx, mail);
//                oma.update(mail);
//                showSavedMessage();
//                if (wasNew) {
//                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/mail/" + mail.getId());
//                    return;
//                }
//            } catch (Throwable e) {
//                UserContext.handle(e);
//            }
//        }
//        if(!ServiceAccountingService.NORMAL_MAIL.equals(mail.getFunction())) {
//            // Wenn noch kein subject da ist, dieses und den Mail-Text über das template erzeugen
//            if (Strings.isEmpty(mail.getSubject())) {
//                if (Strings.isFilled(mail.getTemplate())) {
//                    Offer offer = buildOfferFromMail(mail);
//                    templateList.add(mail.getTemplate());
//                    Mailtemplate mailtemplate =
//                            oma.select(Mailtemplate.class).eq(Mailtemplate.NAME, mail.getTemplate()).queryFirst();
//                    if (mailtemplate != null) {
//                        Context context = sas.prepareContext(offer, mail.getFunction());
//                        String subjectTemplate = mailtemplate.getSubject();
//                        if(!subjectTemplate.isEmpty()) {
//                            String subject = templates.generator()
//                                                      .direct(subjectTemplate, VelocityContentHandler.VM)
//                                                      .applyContext(context)
//                                                      .generate();
//                            mail.setSubject(subject);
//                        }
//                        String textTemplate = mailtemplate.getMailcontent();
//                        if(!textTemplate.isEmpty()) {
//
//// ToDo von Velocity auf tagliatelle umstellen
//
//                            String mailText = templates.generator()
//                                                       .direct(textTemplate, VelocityContentHandler.VM)
//                                                       .applyContext(context)
//                                                       .generate();
//
//                            mail.setText(mailText);
//                        }
//                        mail.setAttachmentName((String) context.get("filenamePDF"));
//                        oma.update(mail);
//                    }
//                }
//            }
//        }
//
//        ctx.respondWith().template("view/mails/mail-details.html", templateList, mail,
//                                   mail.getFunction());
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_OFFER)
//    @Routed("/mail/:1/sendNotOffer")
//    // Taste Nicht senden wurde gedrückt
//    public void sendNotMail(WebContext ctx, String companyId, String offerId, String mailId) {
//        List<String>  templateList =  new ArrayList<String>();
//        Mail mail = find(Mail.class, mailId);
//
//        if(mail != null && Strings.isFilled(mail.getTemplate())) {
//            templateList.add(mail.getTemplate());
//        }
//        UserContext.message(Message.info("Diese Mail wurde nicht versendet"));
//        ctx.respondWith().template("view/mails/mail-details.html", templateList, mail,
//                                   mail.getFunction());
//    }

}
