/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

/**
 * Created by aha on 10.01.17.
 */
public enum ServerState {
    RED, YELLOW, GREEN, GREY;

    public String getCSSClass() {
        switch (this) {
            case RED:
                return "danger";
            case GREEN:
                return "success";
            case YELLOW:
                return "warning";
        }

        return "default";
    }
}
