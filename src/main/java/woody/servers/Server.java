/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.TenantAware;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.di.std.Framework;

import java.time.LocalDateTime;

/**
 * Created by aha on 12.05.15.
 */
@Framework(Servers.FRAMEWORK_SERVERS)
public class Server extends TenantAware {

    public enum Interval {
        OFF, HOURLY, DAILY, WEEKLY, MONTHLY;

        public LocalDateTime getLimit() {
            switch (this) {
                case HOURLY:
                    return LocalDateTime.now().minusHours(2);
                case DAILY:
                    return LocalDateTime.now().minusHours(36);
                case WEEKLY:
                    return LocalDateTime.now().minusDays(10);
                case MONTHLY:
                    return LocalDateTime.now().minusDays(45);
            }
            throw new IllegalArgumentException("No limit for: " + this);
        }

        public ServerState check(LocalDateTime lastUpdate) {
            if (this == OFF) {
                return ServerState.GREEN;
            }
            if (lastUpdate == null || lastUpdate.isBefore(getLimit())) {
                return ServerState.RED;
            } else {
                return ServerState.GREEN;
            }
        }
    }

    public enum ServerState {
        RED, YELLOW, GREEN;
    }

    @Length( 50)
    private String category;
    public static final Column CATEGORY = Column.named("category");

    @Length(100)
    private String name;
    public static final Column NAME = Column.named("name");

    private ServerState state;
    public static final Column STATE = Column.named("state");

    @Length(50)
    @NullAllowed
    private String ipAddress;
    public static final Column IP_ADDRESS = Column.named("ipAddress");

    @Length(50)
    @NullAllowed
    private String publicIpAddress;
    public static final Column PUBLIC_IP_ADDRESS = Column.named("publicIpAddress");

    @Length(255)
    @NullAllowed
    private String primaryMonitoringUrl;
    public static final Column PRIMARY_MONITORING_URL = Column.named("primaryMonitoringUrl");

    private ServerState primaryUrlState;
    public static final Column PRIMARY_URL_STATE = Column.named("primaryUrlState");

    @Length(255)
    @NullAllowed
    private String secondaryMonitoringUrl;
    public static final Column SECONDARY_MONITORING_URL = Column.named("secondaryMonitoringUrl");

    private ServerState secondaryUrlState;
    public static final Column SECONDARY_URL_STATE = Column.named("secondaryUrlState");

    private LocalDateTime lastMonitoring;
    public static final Column LAST_MONITORING = Column.named("lastMonitoring");

    private LocalDateTime lastHeartbeat;
    public static final Column LAST_HEARTBEAT = Column.named("lastHeartbeat");

    private Interval expectedHeartbeatInterval = Interval.OFF;
    public static final Column EXPECTED_HEARTBEAT_INTERVAL = Column.named("expectedHeartbeatInterval");

    private ServerState heatbeatState;
    public static final Column HEATBEAT_STATE = Column.named("heatbeatState");

    private LocalDateTime lastKeyUpdate;
    public static final Column LAST_KEY_UPDATE = Column.named("lastKeyUpdate");

    private Interval expectedKeyUpdateInterval = Interval.OFF;
    public static final Column EXPECTED_KEY_UPDATE_INTERVAL = Column.named("expectedKeyUpdateInterval");

    private ServerState keyUpdateState;
    public static final Column KEY_UPDATE_STATE = Column.named("keyUpdateState");

    private LocalDateTime lastBackup;
    public static final Column LAST_BACKUP = Column.named("lastBackup");

    private Interval expectedBackupInterval = Interval.OFF;
    public static final Column EXPECTED_BACKUP_INTERVAL = Column.named("expectedBackupInterval");

    private ServerState backupState;
    public static final Column BACKUP_STATE = Column.named("backupState");

    @BeforeSave
    protected void updateStates() {
        heatbeatState = expectedHeartbeatInterval.check(lastHeartbeat);
        keyUpdateState = expectedKeyUpdateInterval.check(lastKeyUpdate);
        backupState = expectedBackupInterval.check(lastBackup);
        state = ServerState.GREEN;
        if (heatbeatState.ordinal() < state.ordinal()) {
            state = heatbeatState;
        }
        if (keyUpdateState.ordinal() < state.ordinal()) {
            state = keyUpdateState;
        }
        if (backupState.ordinal() < state.ordinal()) {
            state = backupState;
        }
        if (primaryUrlState.ordinal() < state.ordinal()) {
            state = primaryUrlState;
        }
        if (secondaryUrlState.ordinal() < state.ordinal()) {
            state = secondaryUrlState;
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

    public ServerState getSecondaryUrlState() {
        return secondaryUrlState;
    }

    public void setSecondaryUrlState(ServerState secondaryUrlState) {
        this.secondaryUrlState = secondaryUrlState;
    }

    public String getSecondaryMonitoringUrl() {
        return secondaryMonitoringUrl;
    }

    public void setSecondaryMonitoringUrl(String secondaryMonitoringUrl) {
        this.secondaryMonitoringUrl = secondaryMonitoringUrl;
    }

    public ServerState getPrimaryUrlState() {
        return primaryUrlState;
    }

    public void setPrimaryUrlState(ServerState primaryUrlState) {
        this.primaryUrlState = primaryUrlState;
    }

    public String getPrimaryMonitoringUrl() {
        return primaryMonitoringUrl;
    }

    public void setPrimaryMonitoringUrl(String primaryMonitoringUrl) {
        this.primaryMonitoringUrl = primaryMonitoringUrl;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
