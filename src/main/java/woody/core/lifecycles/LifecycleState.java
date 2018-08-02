/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.lifecycles;

import sirius.biz.jdbc.tenants.SQLTenantAware;
import sirius.db.mixing.Mapping;

public class LifecycleState extends SQLTenantAware {

    public Mapping STAGE = Mapping.named("stage");
    private Stage stage = Stage.ACTIVE;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
