/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import sirius.db.mixing.OMA;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.kernel.async.Tasks;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.timer.EveryMinute;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;

/**
 * Created by aha on 10.01.17.
 */
@Register(framework = ServerController.FRAMEWORK_SERVERS)
public class ServerMonitoring implements EveryMinute {

    private static final String EXECUTOR_SERVERS_MONITORING = "servers-monitoring";

    @Part
    private Tasks tasks;

    @Part
    private OMA oma;

    @Override
    public void runTimer() throws Exception {
        tasks.executor(EXECUTOR_SERVERS_MONITORING)
             .dropOnOverload(() -> ServerController.LOG.WARN("Thread pool is exhausted! Not performing any monitoring."))
             .fork(this::executeInMonitoringThread);
    }

    private void executeInMonitoringThread() {
        oma.select(Server.class)
           .where(FieldOperator.on(Server.MONITORING_URL).notEqual(null))
           .iterateAll(server -> tasks.executor(EXECUTOR_SERVERS_MONITORING)
                                      .fork(() -> this.executeMonitoring(server)));
    }

    private void executeMonitoring(Server server) {
        ServerController.LOG.FINE("Monitoring: %s", server.getName());
        boolean healthy = checkHealth(server);
        ServerState newMonitoringState = healthy ? ServerState.GREEN : ServerState.RED;
        if (server.getMonitoringState() != newMonitoringState) {
            server.setMonitoringState(newMonitoringState);
            //TODO notify
        }
        ServerController.LOG.FINE("%s is now %s", server.getName(), newMonitoringState.name());
        server.setLastMonitoring(LocalDateTime.now());
        server.getTrace().setSilent(true);
        oma.update(server);
    }

    private boolean checkHealth(Server server) {
        try {
            URLConnection connection = new URL(server.getMonitoringUrl()).openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            String response = CharStreams.toString(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
            ServerController.LOG.FINE("Received %s for %s (%s)", response, server.getName(), server.getUrl());

            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                ServerController.LOG.FINE("Received status %s for %s (%s)",
                                          responseCode,
                                          server.getName(),
                                          server.getUrl());
                return false;
            }

            return Strings.isEmpty(server.getMonitoringKeyword()) || response.contains(server.getMonitoringKeyword());
        } catch (Throwable e) {
            ServerController.LOG.FINE("Received %s (%s) for %s (%s)",
                                      e.getMessage(),
                                      e.getClass().getName(),
                                      server.getName(),
                                      server.getUrl());
            return false;
        }
    }
}
