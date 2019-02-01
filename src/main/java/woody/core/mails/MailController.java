/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Message;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import sirius.web.templates.TagliatelleContentHandler;
import sirius.web.templates.Templates;
import woody.core.employees.Employee;
import woody.offers.Offer;
import woody.offers.ServiceAccountingService;
import woody.sales.AccountingService;
import woody.xrm.Company;
import woody.xrm.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by aha on 11.05.15.
 */
@Framework("mails")
@Register(classes = Controller.class)
public class MailController extends BizController {
//
    private static final String MANAGE_XRM = "permission-manage-xrm";
    public static final String MANAGE_OFFER = "permission-manage-offers";

    @Part
    private AccountingService asb;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Templates templates;

    @Part
    private static SendMailService sms;

    // Taste 'Mail senden' wurde gedrückt
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/mail/:1/sendMail")
    public void sendMail(WebContext ctx, String mailId) {

        Mail mail = find(Mail.class, mailId);
        Person person = mail.getPersonEntity().getValue();
        Company company = person.getCompany().getValue();
        Offer offer = null;
        List<Mailtemplate> templateList = new ArrayList();
        if(mail.getTemplate() != null) {
            templateList.add(oma.select(Mailtemplate.class).eq(Mailtemplate.NAME,mail.getTemplate()).queryFirst());
        }
        if(mail == null) {
            ctx.respondWith().template("templates/mails/mail-details.html", templateList, mail, mail.getFunction());
            return;
        }
        if(Strings.isEmpty(mail.getSubject())) {
            UserContext.message(Message.info("Bei der Mail ist der 'Betreff' leer, deshalb kann keine Mail gesendet werden."));
            ctx.respondWith().template("templates/mails/mail-details.html.pasta", mail, templateList, mail.getFunction(), company, person);
            return;
        }
        switch (mail.getFunction()) {
            case ServiceAccountingService.OFFER:
            case ServiceAccountingService.SALES_CONFIRMATION:
                if (mail.getUsageId() != null) {
                    offer = buildOfferFromMail(mail);
                    break;
                }
            case ServiceAccountingService.NORMAL_MAIL:
                break;
        }
        List<String> messageList = sms.prepareAndSendMail(offer, mail);
        for(String s: messageList) {
            UserContext.message(Message.info(s));
        }

        ctx.respondWith().template("templates/mails/mail-details.html.pasta", mail, templateList, mail.getFunction(), company, person);
    }

    private Offer buildOfferFromMail(Mail mail) {
        Offer offer = null;
        if(mail.getUsageId() != null && mail.getUsageId().contains("-")) {
            String[] fields = mail.getUsageId().split("-");
            String classname = fields[0];
            if("OFFER".equals(classname.toUpperCase())) {
                String id = fields[1];
                offer = find(Offer.class, id);
            }
        }
        return offer;
    }


//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/mail/:1")
//    public void mail(WebContext ctx, String mailId) {
//        Mail mail = findForTenant(Mail.class, mailId);
//        if (mail.isNew()) {
//            ctx.respondWith().template("templates/mails/mail-details.html.pasta", mail);
//        } else {
//            ctx.respondWith().template("templates/mails/mail-overview.html.pasta", mail);
//        }
//    }


    // send a normal mail to a person
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/person/:1/sendNormalMailToPerson")
    // Funktion Normale Mail an Person senden, z. B. durch Anklicken der Mail-Adresse
    public void sendNormalMailToPerson(WebContext ctx, String personId) {
        String mailTemplateName =  ServiceAccountingService.NORMAL_MAIL;
        String function = ServiceAccountingService.NORMAL_MAIL;

        Person person = find(Person.class, personId);
        Mail mail = new Mail();
        String receiver = person.getContact().getEmail();
        UserInfo ui = UserContext.getCurrentUser();
        UserAccount uac = ui.as(UserAccount.class);
        String sender = uac.getEmail();
        mail.getEmployeeEntity().setValue(uac);
        mail.getPersonEntity().setValue(person);
        mail.setReceiverAddress(receiver);
        mail.setSenderAddress(sender);
        mail.setFunction(function);
        mail.setTemplate(mailTemplateName);
        mail.setAttachmentName("keine");
        Mailtemplate mailtemplate = oma.select(Mailtemplate.class).eq(Mailtemplate.NAME, mailTemplateName).queryFirst();

        Context context = new Context();
        context.set("salutation",sas.getLetterHeadline(person));
        String mailText = "";
        if(mail.getText() == null) {
             mailText = templates.generator().direct(mailtemplate.getMailcontent(), TagliatelleContentHandler.PASTA)
                                       .applyContext(context).generate();
            mail.setText(mailText);
            int gg = 3;
        }
        oma.update(mail);
        // Mail-Template in Liste vorbelegen
        List<Mailtemplate> templateList = new ArrayList();
        if (mail.getTemplate() != null) {
            Mailtemplate template = oma.select(Mailtemplate.class).eq(Mailtemplate.NAME, mail.getTemplate()).queryFirst();
            templateList.add(template);
        }
        Company company = person.getCompany().getValue();
        ctx.respondWith().template("templates/mails/mail-details.html.pasta", mail, templateList, mail.getFunction(), company, person);
    }



    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/mail/:1/edit")
    public void editMail(WebContext ctx, String mailId) {
        Mail mail = findForTenant(Mail.class, mailId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = mail.isNew();
                load(ctx, mail);
                oma.update(mail);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily("/mail/" + mail.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        Person person = mail.getPersonEntity().getValue();
        Company company = person.getCompany().getValue();
        List<Mailtemplate> templateList = new ArrayList();
        if(!mail.getTemplate().isEmpty()) {
            templateList.add(oma.select(Mailtemplate.class).eq(Mailtemplate.NAME,mail.getTemplate()).queryFirst());
        }
        ctx.respondWith().template("templates/mails/mail-details.html.pasta", mail, templateList, mail.getFunction(), company, person);
    }


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
