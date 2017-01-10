/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.kernel.nls.NLS;

import java.time.LocalDateTime;

/**
 * Created by aha on 10.01.17.
 */
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

    public ServerState check(LocalDateTime lastUpdate, ServerState failState) {
        if (this == OFF) {
            return ServerState.GREY;
        }
        if (lastUpdate == null || lastUpdate.isBefore(getLimit())) {
            return failState;
        } else {
            return ServerState.GREEN;
        }
    }

    @Override
    public String toString() {
        return NLS.get(Interval.class.getSimpleName() + "." + name());
    }
}
