/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;
import sirius.mixing.annotations.Length;

/**
 * Created by aha on 12.05.15.
 */
public class ServerAccess extends BizEntity {

    @Length(length = 150)
    private String serverNameExpression;
    public static final Column SERVER_NAME_EXPRESSION = Column.named("serverNameExpression");

    private boolean accessServer;
    public static final Column ACCESS_SERVER = Column.named("accessServer");

    private boolean monitorServer;
    public static final Column MONITOR_SERVER = Column.named("monitorServer");

    private final EntityRef<UserAccount> user = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);
    public static final Column USER = Column.named("user");

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

    public boolean isMonitorServer() {
        return monitorServer;
    }

    public void setMonitorServer(boolean monitorServer) {
        this.monitorServer = monitorServer;
    }

    public EntityRef<UserAccount> getUser() {
        return user;
    }
}
