/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.mails;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Transient;
import sirius.kernel.commons.Strings;
import woody.core.employees.Employee;
import woody.xrm.Person;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 28.09.17.
 */
public class Mail extends BizEntity {

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String targetEntity;
    public static final Column TARGET_ENTITY = Column.named("targetEntity");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String senderAddress;
    public static final Column SENDERADDRESS = Column.named("senderAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String receiverAddress;
    public static final Column RECEIVERADDRESS = Column.named("receiverAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String ccAddress;
    public static final Column CCADDRESS = Column.named("ccAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String bccAddress;
    public static final Column BCCADDRESS = Column.named("bccAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String messageId;
    public static final Column MESSAGEID = Column.named("messageId");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String subject;
    public static final Column SUBJECT = Column.named("subject");

    @Autoloaded
    @Lob
    @NullAllowed
    private String text;
    public static final Column TEXT = Column.named("text");

    @NullAllowed
    private LocalDateTime receivingDate;
    public static final Column RECEIVINGDATE = Column.named("receivingDate");

    @NullAllowed
    private LocalDateTime sendDate;
    public static final Column SENDDATE = Column.named("sendDate");


    @Autoloaded
    @NullAllowed
    private final EntityRef<Person> personEntity = EntityRef.on(Person.class, EntityRef.OnDelete.REJECT);
    public static final Column PERSON_ENTITY = Column.named("personEntity");

    @Autoloaded
    @NullAllowed
    private final EntityRef<UserAccount> employeeEntity = EntityRef.on(UserAccount.class, EntityRef.OnDelete.REJECT);
    public static final Column EMPLOYEE_ENTITY = Column.named("employeeEntity");

    @Autoloaded
    private final boolean publicVisible = true;
    public static final Column PUBLIC_VISIBLE = Column.named("publicVisible");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String attachmentName;
    public static final Column ATTACHMENTNAME = Column.named("attachmentName");

    @Autoloaded
    @Length(50)
    @NullAllowed
    private String template;
    public static final Column TEMPLATE = Column.named("template");

    // the usageId contents the uniqueName of the business action, e.g. a offer or a invoice or ...
    // is no business action present then te usageId = null.
    @Autoloaded
    @Length(50)
    @NullAllowed
    private String usageId;
    public static final Column USAGEID = Column.named("usageId");

    @Length(50)
    @NullAllowed
    private String function;
    public static final Column FUNCTION = Column.named("function");

    /**
     * Replaces new line with <br>
     * tags
     */
    private static String nl2br(String content) {
        if (content == null) {
            return null;
        }
        return content.replace("\n", " <br /> ");
    }


    public String getUsageId() {
        return usageId;
    }

    public void setUsageId(String usageId) {
        this.usageId = usageId;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

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

    public LocalDateTime getReceivingDate() {
        return receivingDate;
    }

    public void setReceivingDate(LocalDateTime receivingDate) {
        this.receivingDate = receivingDate;
    }

    public EntityRef<Person> getPersonEntity() {
        return personEntity;
    }

    public EntityRef<UserAccount> getEmployeeEntity() {
        return employeeEntity;
    }

    public boolean isPublicVisible() {
        return publicVisible;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}
