/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.tracking.mails;

import sirius.db.mixing.Composite;

/**
 * Created by aha on 25.11.15.
 */
public class Mailed extends Composite {
//
//    @Transient
//    protected final Entity owner;
//
//    public Mailed(Entity owner) {
//        this.owner = owner;
//    }
//
//    @Part
//    private static OMA oma;
//
//    @BeforeDelete
//    protected void onDelete() {
//        if (owner != null && !owner.isNew()) {
//            oma.select(Mail.class).eq(Mail.TARGET_ENTITY, owner.getUniqueName()).delete();
//        }
//    }
//
//    public List<Mail> getAllMails() {
//        return oma.select(Mail.class)
//                  .orderDesc(Mail.BCCADDRESS)
//                  .eq(Mail.TARGET_ENTITY, owner.getUniqueName())
//                  .queryList();
//    }
//
//
//
//    public List<Mail> getPublicMails() {
//        return oma.select(Mail.class)
//                  .orderDesc(Mail.RECEIVINGDATE)
//                  .eq(Mail.TARGET_ENTITY, owner.getUniqueName())
//                  .where(Or.of(FieldOperator.on(Mail.PERSON_ENTITY).eq(UserContext.getCurrentUser().getUserId()),
//                               FieldOperator.on(Mail.PUBLIC_VISIBLE).eq(true)))
//                  .queryList();
//
//    }

    // ToDo prüfen, ob notwendig
/*
    public void addMail(String senderName, String personEntity, String comment, boolean publicVisible) {
        if (Strings.isEmpty(comment)) {
            return;
        }

        if (owner.isNew()) {
            throw new IllegalStateException("owner must not be new");
        }

        Mail commentEntity = new Mail();
        commentEntity.setTargetEntity(owner.getUniqueName());
        commentEntity.setSenderName(senderName);
        commentEntity.setPersonEntity(personEntity);
        commentEntity.setTod(LocalDateTime.now());
        commentEntity.setText(text);
        oma.update(commentEntity);
    }
*/

    // ToDo prüfen, ob notwendig
//    public void addMailAsCurrentUser(String comment) {
//        UserInfo currentUser = UserContext.getCurrentUser();
//        addMail(currentUser.getUserObject(UserAccount.class).getPerson().toString(),
//                   currentUser.getUserId(),
//                   comment,
//                   false);
//    }

    // ToDo prüfen, ob notwendig
//    public void addPublicMailAsCurrentUser(String comment) {
//        UserInfo currentUser = UserContext.getCurrentUser();
//               addMail(currentUser.getUserObject(UserAccount.class).getPerson().toString(),
//                   currentUser.getUserId(),
//                   comment,
//                   true);
//    }
}
