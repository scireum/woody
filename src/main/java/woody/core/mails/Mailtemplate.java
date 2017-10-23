/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.model.BizEntity;
import sirius.db.mixing.Column;
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
    public static final Column NAME = Column.named("name");

    @Length(255)
    private String subject;
    public static final Column SUBJECT = Column.named("subject");

    @Lob
    private String mailcontent;
    public static final Column MAILCONTENT = Column.named("mailcontent");

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
