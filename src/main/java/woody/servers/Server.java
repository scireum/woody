/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Ordinal;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;
import woody.xrm.Company;

import java.time.LocalDateTime;

/**
 * Created by aha on 12.05.15.
 */
@Framework(ServerController.FRAMEWORK_SERVERS)
public class Server extends TenantAware {

    public static final Column CUSTOMER = Column.named("customer");
    @NullAllowed
    @Autoloaded
    private final EntityRef<Company> customer = EntityRef.on(Company.class, EntityRef.OnDelete.REJECT);

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Unique(within = "tenant")
    @Trim
    @Autoloaded
    private String name;

    public static final Column TOKEN = Column.named("token");
    @Length(100)
    private String token;

    public static final Column DESCRIPTION = Column.named("description");
    @Length(1024)
    @NullAllowed
    @Autoloaded
    private String description;

    public static final Column URL = Column.named("url");
    @Length(1024)
    @Trim
    @NullAllowed
    @Autoloaded
    private String url;

    public static final Column TAGGED = Column.named("tagged");
    private final Tagged tags = new Tagged(this);

    public static final Column COMMENTS = Column.named("comments");
    private final Commented comments = new Commented(this);

    public static final Column STATE = Column.named("state");
    @Ordinal
    private ServerState state;

    public static final Column IP_ADDRESS = Column.named("ipAddress");
    @Length(50)
    @NullAllowed
    @Autoloaded
    private String ipAddress;

    public static final Column PUBLIC_IP_ADDRESS = Column.named("publicIpAddress");
    @Length(50)
    @NullAllowed
    @Autoloaded
    private String publicIpAddress;

    public static final Column MONITORING_URL = Column.named("monitoringUrl");
    @Length(255)
    @Trim
    @NullAllowed
    @Autoloaded
    private String monitoringUrl;

    public static final Column MONITORING_KEYWORD = Column.named("monitoringKeyword");
    @Length(255)
    @Trim
    @NullAllowed
    @Autoloaded
    private String monitoringKeyword;

    public static final Column MONITORING_STATE = Column.named("monitoringState");
    private ServerState monitoringState = ServerState.GREY;

    public static final Column LAST_MONITORING = Column.named("lastMonitoring");
    @NullAllowed
    private LocalDateTime lastMonitoring;

    public static final Column LAST_HEARTBEAT = Column.named("lastHeartbeat");
    @NullAllowed
    private LocalDateTime lastHeartbeat;

    public static final Column LAST_KEY_UPDATE = Column.named("lastKeyUpdate");
    @NullAllowed
    private LocalDateTime lastKeyUpdate;

    public static final Column LAST_BACKUP = Column.named("lastBackup");
    @NullAllowed
    private LocalDateTime lastBackup;

    public static final Column EXPECTED_HEARTBEAT_INTERVAL = Column.named("expectedHeartbeatInterval");
    @Autoloaded
    private Interval expectedHeartbeatInterval = Interval.OFF;

    public static final Column HEATBEAT_STATE = Column.named("heatbeatState");
    private ServerState heatbeatState = ServerState.GREY;

    public static final Column EXPECTED_KEY_UPDATE_INTERVAL = Column.named("expectedKeyUpdateInterval");
    @Autoloaded
    private Interval expectedKeyUpdateInterval = Interval.OFF;

    public static final Column KEY_UPDATE_STATE = Column.named("keyUpdateState");
    private ServerState keyUpdateState = ServerState.GREY;

    public static final Column EXPECTED_BACKUP_INTERVAL = Column.named("expectedBackupInterval");
    @Autoloaded
    private Interval expectedBackupInterval = Interval.OFF;

    public static final Column BACKUP_STATE = Column.named("backupState");
    private ServerState backupState = ServerState.GREY;

    @BeforeSave
    protected void initializeToken() {
        if (Strings.isEmpty(token)) {
            token = Strings.generateCode(32);
        }
    }

    @BeforeSave
    protected void updateStates() {
        if (expectedHeartbeatInterval == null) {
            expectedHeartbeatInterval = Interval.OFF;
        }
        if (expectedKeyUpdateInterval == null) {
            expectedKeyUpdateInterval = Interval.OFF;
        }
        if (expectedBackupInterval == null) {
            expectedBackupInterval = Interval.OFF;
        }

        state = ServerState.GREY;

        heatbeatState = expectedHeartbeatInterval.check(lastHeartbeat, ServerState.RED);
        if (heatbeatState.ordinal() < state.ordinal()) {
            state = heatbeatState;
        }

        keyUpdateState = expectedKeyUpdateInterval.check(lastKeyUpdate, ServerState.YELLOW);
        if (keyUpdateState.ordinal() < state.ordinal()) {
            state = keyUpdateState;
        }

        backupState = expectedBackupInterval.check(lastBackup, ServerState.YELLOW);
        if (backupState.ordinal() < state.ordinal()) {
            state = backupState;
        }

        if (monitoringState.ordinal() < state.ordinal()) {
            state = monitoringState;
        }
    }

    public ServerState getBackupState() {
        return backupState;
    }

    public void setBackupState(ServerState backupState) {
        this.backupState = backupState;
    }

    public Interval getExpectedBackupInterval() {
        return expectedBackupInterval;
    }

    public void setExpectedBackupInterval(Interval expectedBackupInterval) {
        this.expectedBackupInterval = expectedBackupInterval;
    }

    public LocalDateTime getLastBackup() {
        return lastBackup;
    }

    public void setLastBackup(LocalDateTime lastBackup) {
        this.lastBackup = lastBackup;
    }

    public ServerState getKeyUpdateState() {
        return keyUpdateState;
    }

    public void setKeyUpdateState(ServerState keyUpdateState) {
        this.keyUpdateState = keyUpdateState;
    }

    public Interval getExpectedKeyUpdateInterval() {
        return expectedKeyUpdateInterval;
    }

    public void setExpectedKeyUpdateInterval(Interval expectedKeyUpdateInterval) {
        this.expectedKeyUpdateInterval = expectedKeyUpdateInterval;
    }

    public LocalDateTime getLastKeyUpdate() {
        return lastKeyUpdate;
    }

    public void setLastKeyUpdate(LocalDateTime lastKeyUpdate) {
        this.lastKeyUpdate = lastKeyUpdate;
    }

    public ServerState getHeatbeatState() {
        return heatbeatState;
    }

    public void setHeatbeatState(ServerState heatbeatState) {
        this.heatbeatState = heatbeatState;
    }

    public Interval getExpectedHeartbeatInterval() {
        return expectedHeartbeatInterval;
    }

    public void setExpectedHeartbeatInterval(Interval expectedHeartbeatInterval) {
        this.expectedHeartbeatInterval = expectedHeartbeatInterval;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public LocalDateTime getLastMonitoring() {
        return lastMonitoring;
    }

    public void setLastMonitoring(LocalDateTime lastMonitoring) {
        this.lastMonitoring = lastMonitoring;
    }

    public EntityRef<Company> getCustomer() {
        return customer;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMonitoringUrl() {
        return monitoringUrl;
    }

    public void setMonitoringUrl(String monitoringUrl) {
        this.monitoringUrl = monitoringUrl;
    }

    public String getMonitoringKeyword() {
        return monitoringKeyword;
    }

    public void setMonitoringKeyword(String monitoringKeyword) {
        this.monitoringKeyword = monitoringKeyword;
    }

    public ServerState getMonitoringState() {
        return monitoringState;
    }

    public void setMonitoringState(ServerState monitoringState) {
        this.monitoringState = monitoringState;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tagged getTags() {
        return tags;
    }

    public Commented getComments() {
        return comments;
    }
}
