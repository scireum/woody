/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import woody.core.employees.Employee;
import woody.xrm.Person;

import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 28.09.17.
 */
public class Mail extends Entity {

    @Length(255)
    private String targetEntity;
    public static final Column TARGET_ENTITY = Column.named("targetEntity");

    @Length(255)
    @NullAllowed
    private String senderAddress;
    public static final Column SENDERADDRESS = Column.named("senderAddress");

    @Length(255)
    @NullAllowed
    private String receiverAddress;
    public static final Column RECEIVERADDRESS = Column.named("receiverAddress");

    @Length(255)
    @NullAllowed
    private String ccAddress;
    public static final Column CCADDRESS = Column.named("ccAddress");

    @Length(255)
    @NullAllowed
    private String bccAddress;
    public static final Column BCCADDRESS = Column.named("bccAddress");

    @Length(255)
    @NullAllowed
    private String messageId;
    public static final Column MESSAGEID = Column.named("messageId");

    @Length(255)
    private String subject;
    public static final Column SUBJECT = Column.named("subject");

    @Lob
    @NullAllowed
    private String text;
    public static final Column TEXT = Column.named("text");

    private LocalDateTime tod;
    public static final Column TOD = Column.named("tod");

    @Autoloaded
    @NullAllowed
    private final EntityRef<Person> personEntity = EntityRef.on(Person.class, EntityRef.OnDelete.REJECT);
    public static final Column PERSON_ENTITY = Column.named("personEntity");

    @Autoloaded
    @NullAllowed
    private final EntityRef<UserAccount> userEntity = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column USER_ENTITY = Column.named("userAccount");

    @Autoloaded
    private final boolean publicVisible = true;
    public static final Column PUBLIC_VISIBLE = Column.named("publicVisible");

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getCcAddress() {
        return ccAddress;
    }

    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }

    public String getBccAddress() {
        return bccAddress;
    }

    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTod() {
        return tod;
    }

    public void setTod(LocalDateTime tod) {
        this.tod = tod;
    }

    public EntityRef<Person> getPersonEntity() {
        return personEntity;
    }

    public EntityRef<UserAccount> getUserEntity() {
        return userEntity;
    }

    public boolean isPublicVisible() {
        return publicVisible;
    }
}
