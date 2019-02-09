/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.jdbc.model.BizEntity;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.Unique;

/**
 * Created by gerhardhaufler on 15.10.17.
 */
public class Mailtemplate extends BizEntity {

    @Unique
    @Length(255)
    private String name;
    public static final Mapping NAME = Mapping.named("name");

    @Length(255)
    private String subject;
    public static final Mapping SUBJECT = Mapping.named("subject");

    @Lob
    private String mailcontent;
    public static final Mapping MAILCONTENT = Mapping.named("mailcontent");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailcontent() {
        return mailcontent;
    }

    public void setMailcontent(String mailcontent) {
        this.mailcontent = mailcontent;
    }
}
