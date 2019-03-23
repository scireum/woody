/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.tenants.UserAccount;
import sirius.kernel.commons.Tuple;
import woody.core.employees.Employee;
import woody.offers.Offer;
import woody.xrm.Person;

import javax.activation.DataSource;
import javax.lang.model.element.AnnotationValueVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardhaufler on 18.03.19.
 */
public interface MailService {

    public static final String NORMAL_MAIL = "normalMail";
    public static final String OFFER_MAIL = "offerMail";
    public static final String CONTRACT_MAIL = "contractMail";
    public static final String SALESCONFIRMATION_MAIL = "salesConfirmationMail";
    public static final String AVV_SEND = "avv_send";
    public static final String AVV_REPLACE = "avv_replace";

    public static final String[] functions = {
            NORMAL_MAIL, OFFER_MAIL, CONTRACT_MAIL, SALESCONFIRMATION_MAIL,
            AVV_SEND, AVV_REPLACE

    };

    static final Tuple<String, String> offerTuple = new Tuple("Angebot_versenden",OFFER_MAIL);
    static final Tuple<String, String> confiTuple = new Tuple("AB_Universal",SALESCONFIRMATION_MAIL);
    static final Tuple<String, String> contractTuple = new Tuple("CD_Universal",CONTRACT_MAIL);
    static final Tuple<String, String> AVV_versenden = new Tuple("AVV_versenden",AVV_SEND);
    static final Tuple<String, String> AVV_ersetzen = new Tuple("AVV_ersetzen",AVV_REPLACE);
    public static final Tuple<String,String>[] mailFunctionTuples = new Tuple[]{offerTuple, confiTuple, contractTuple, AVV_versenden, AVV_ersetzen};



    public List<String> getFunctionList();

    public Mail createMailToPerson(Person person, UserAccount uac);

    public String getPersonMailAddress(Person person);

    public String getUacMailAddress(UserAccount uac);

    /**
     * prepares and send a mail from the given context
     * the given mail is updated and stored in the Mail-class.
     * The datasource is a link to the attachment.
     * function:
     *  - NORMAL_MAIL:
     *  - OFFER_MAIL: The offer (given in the vontext) is send as a mail
     *  - SALESCONFIRMATION_MAIL: The sales-confirmation for the offer (given in the context)
     *    is generated and send as a mail
     *  - CONTRACT_MAIL
     */
    public List<String> prepareAndSendMail (Offer offer, Mail mail);

    /**
     * the given mail (see parameters) is send and stored in the Mail-Class.
     * @param senderAddress
     * @param senderName
     * @param receiverAddress
     * @param receiverName
     * @param subject
     * @param mailText
     * @param attachment
     * @param attachmentName
     * @throws Exception
     */
    public void sendMail(String senderAddress, String senderName, String receiverAddress, String receiverName,
                         String subject, String mailText, DataSource attachment, String attachmentName) throws Exception;

    public String transformToHtml(String string);
}
