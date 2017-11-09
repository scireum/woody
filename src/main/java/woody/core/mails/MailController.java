/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.model.AddressData;
import sirius.biz.model.ContactData;
import sirius.biz.model.PersonData;
import sirius.biz.sequences.Sequences;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.DataCollector;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import sirius.web.services.JSONStructuredOutput;
import sirius.web.templates.Templates;
import sirius.web.templates.velocity.VelocityContentHandler;
import woody.core.employees.Employee;
import woody.core.tags.Tagged;
import woody.offers.Offer;
import woody.offers.OffersController;
import woody.offers.ServiceAccountingService;
import woody.phoneCalls.Starface;
import woody.sales.AccountingService;
import woody.sales.Lineitem;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("mails")
@Register(classes = Controller.class)
public class MailController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @Part
    private AccountingService asb;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Templates templates;

    @Part
    private static SendMailService sms;


    // Taste Speichern bei view 'mail-details.html' wurde gedrückt
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/mail/:1/offer/:2")
    public void saveMail(WebContext ctx, String mailId, String offerId) {
        Offer offer = find(Offer.class, offerId);
        Company company = offer.getCompany().getValue();
        Mail mail = find(Mail.class, mailId);
        // Mail-Template in Liste vorbelegen
        List<String> templateList = new ArrayList<String>();
        if (mail.getTemplate() != null) {
            templateList.add(mail.getTemplate());
        }
        // Daten des Editors in mail speichern
        if (ctx.isPOST()) {
            try {
                boolean wasNew = mail.isNew();
                load(ctx, mail);
                oma.update(mail);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/mail/" + mail.getId() + "/offer/" + offer.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }

        // Wenn noch kein subject da ist, dieses und den Mail-Text über das template erzeugen
        if(Strings.isEmpty(mail.getSubject())) {
            if(Strings.isFilled(mail.getTemplate())) {
                templateList.add(mail.getTemplate());
                Mailtemplate mailtemplate = oma.select(Mailtemplate.class).eq(Mailtemplate.NAME, mail.getTemplate()).queryFirst();
                if (mailtemplate != null) {
                    String subjectTemplate = mailtemplate.getSubject();
                    String textTemplate = mailtemplate.getMailcontent();
                    Context context = sas.prepareContext(offer, ServiceAccountingService.OFFER);
                    String subject = templates.generator()
                                              .direct(subjectTemplate, VelocityContentHandler.VM)
                                              .applyContext(context)
                                              .generate();
                    String mailText = templates.generator()
                                               .direct(textTemplate, VelocityContentHandler.VM)
                                               .applyContext(context)
                                               .generate();
                    mail.setSubject(subject);
                    mail.setText(mailText);
                    mail.setAttachmentName((String) context.get("filenamePDF"));
                    oma.update(mail);
                }
            }
        }

        ctx.respondWith().template("view/mails/mail-details.html", company, offer, templateList, mail,
                                   OffersController.SENDOFFER);
    }

}
