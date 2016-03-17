/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.TenantAware;
import sirius.mixing.Column;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;

/**
 * Created by gerhardhaufler on 05.02.16.
 */
public class Industry extends TenantAware {

    @Trim
    @Unique(within = "tenant")
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");


    public String getName() {
            return name;
        }

    public void setName(String name) {
            this.name = name;
        }

}
