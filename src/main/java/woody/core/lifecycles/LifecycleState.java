/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.lifecycles;

import sirius.biz.tenants.TenantAware;
import sirius.db.mixing.Column;

public class LifecycleState extends TenantAware {

    public Column STAGE = Column.named("stage");
    private Stage stage = Stage.ACTIVE;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
