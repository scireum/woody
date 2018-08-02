/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.tracking.mails;

import sirius.biz.jdbc.model.BizEntity;
import sirius.biz.jdbc.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.NullAllowed;
import woody.xrm.Person;

import java.time.LocalDateTime;

/**
 * Created by gerhardhaufler on 28.09.17.
 */
public class Mail extends BizEntity {

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String targetEntity;
    public static final Mapping TARGET_ENTITY = Mapping.named("targetEntity");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String senderAddress;
    public static final Mapping SENDERADDRESS = Mapping.named("senderAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String receiverAddress;
    public static final Mapping RECEIVERADDRESS = Mapping.named("receiverAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String ccAddress;
    public static final Mapping CCADDRESS = Mapping.named("ccAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String bccAddress;
    public static final Mapping BCCADDRESS = Mapping.named("bccAddress");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String messageId;
    public static final Mapping MESSAGEID = Mapping.named("messageId");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String subject;
    public static final Mapping SUBJECT = Mapping.named("subject");

    @Autoloaded
    @Lob
    @NullAllowed
    private String text;
    public static final Mapping TEXT = Mapping.named("text");

    @NullAllowed
    private LocalDateTime receivingDate;
    public static final Mapping RECEIVINGDATE = Mapping.named("receivingDate");

    @NullAllowed
    private LocalDateTime sendDate;
    public static final Mapping SENDDATE = Mapping.named("sendDate");

    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Person> personEntity = SQLEntityRef.on(Person.class, SQLEntityRef.OnDelete.REJECT);
    public static final Mapping PERSON_ENTITY = Mapping.named("personEntity");

    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<UserAccount> employeeEntity =
            SQLEntityRef.on(UserAccount.class, SQLEntityRef.OnDelete.REJECT);
    public static final Mapping EMPLOYEE_ENTITY = Mapping.named("employeeEntity");

    @Autoloaded
    private final boolean publicVisible = true;
    public static final Mapping PUBLIC_VISIBLE = Mapping.named("publicVisible");

    @Autoloaded
    @Length(255)
    @NullAllowed
    private String attachmentName;
    public static final Mapping ATTACHMENTNAME = Mapping.named("attachmentName");

    @Autoloaded
    @Length(50)
    @NullAllowed
    private String template;
    public static final Mapping TEMPLATE = Mapping.named("template");

    // the usageId contents the uniqueName of the business action, e.g. a offer or a invoice or ...
    // is no business action present then te usageId = null.
    @Autoloaded
    @Length(50)
    @NullAllowed
    private String usageId;
    public static final Mapping USAGEID = Mapping.named("usageId");

    @Length(50)
    @NullAllowed
    private String function;
    public static final Mapping FUNCTION = Mapping.named("function");

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

    public SQLEntityRef<Person> getPersonEntity() {
        return personEntity;
    }

    public SQLEntityRef<UserAccount> getEmployeeEntity() {
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
