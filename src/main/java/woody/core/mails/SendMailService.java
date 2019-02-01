/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.kernel.commons.Context;
import woody.offers.Offer;

import javax.activation.DataSource;
import java.util.List;

/**
 * Created by gerhardhaufler on 26.10.17.
 */



public interface SendMailService {

//    public static final String NORMAL_MAIL = "normalMail";
//    public static final String OFFER_MAIL = "offerMail";
//    public static final String CONTRACT_MAIL = "contractMail";
//    public static final String SALESCONFIRMATION_MAIL = "salesConfirmationMail";

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
