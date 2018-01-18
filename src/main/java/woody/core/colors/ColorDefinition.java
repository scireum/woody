/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.tenants.TenantAware;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.Length;

public class ColorDefinition extends TenantAware {

    @Length(50)
    private String name;

    public static final Column HEX_CODE = Column.named("hexCode");
    @Length(7)
    private String hexCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }
}
