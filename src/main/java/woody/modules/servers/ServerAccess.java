/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.modules.servers;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;

/**
 * Created by aha on 12.05.15.
 */
public class ServerAccess extends BizEntity {

    public static final Column USER = Column.named("user");
    private final EntityRef<UserAccount> user = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);

    public static final Column SERVER_NAME_EXPRESSION = Column.named("serverNameExpression");
    @Length(150)
    private String serverNameExpression;

    public static final Column ACCESS_SERVER = Column.named("accessServer");
    private boolean accessServer;

    public String getServerNameExpression() {
        return serverNameExpression;
    }

    public void setServerNameExpression(String serverNameExpression) {
        this.serverNameExpression = serverNameExpression;
    }

    public boolean isAccessServer() {
        return accessServer;
    }

    public void setAccessServer(boolean accessServer) {
        this.accessServer = accessServer;
    }

    public EntityRef<UserAccount> getUser() {
        return user;
    }
}
