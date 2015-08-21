/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;

/**
 * Created by aha on 18.08.15.
 */
public class TaskList extends BizEntity {

    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    private final EntityRef<UserAccount> owner = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);
    public static final Column OWNER = Column.named("owner");

}
