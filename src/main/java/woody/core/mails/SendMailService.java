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

    public void prepareMail (Context context, String function, DataSource dataSource);

    public void sendMail(String senderAddress, String senderName, String receiverAddress, String receiverName,
                         String subject, String mailText, DataSource attachment, String attachmentName) throws Exception;

    public void sendOfferMail(Context context);

    public List<String> getTemplateList(Offer offer);
}
