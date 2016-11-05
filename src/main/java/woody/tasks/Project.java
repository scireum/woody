/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.tasks;

import sirius.biz.tenants.TenantAware;
import sirius.biz.tenants.Tenants;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.di.std.Part;
import woody.core.comments.Commented;
import woody.core.tags.Tagged;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DUMMY - PROJECT nur angelegt, dass es compiliert
 */
public class Project extends TenantAware {

    @Length(1024)
    @Autoloaded
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
