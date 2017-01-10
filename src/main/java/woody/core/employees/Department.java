/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import com.google.common.collect.Sets;
import sirius.biz.tenants.TenantAware;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;

import java.util.Set;

/**
 * Created by aha on 11.05.15.
 */
public class Department extends TenantAware {

    public static final Column SUPERVISOR = Column.named("supervisor");
    @NullAllowed
    @Autoloaded
    private final EntityRef<UserAccount> supervisor = EntityRef.on(UserAccount.class, EntityRef.OnDelete.SET_NULL);

    public static final Column PARENT = Column.named("parent");
    @NullAllowed
    @Autoloaded
    private final EntityRef<Department> parent = EntityRef.on(Department.class, EntityRef.OnDelete.SET_NULL);

    public static final Column CODE = Column.named("code");
    @Length(100)
    @Unique(within = "tenant")
    @Trim
    @Autoloaded
    private String code;

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Unique(within = "tenant")
    @Trim
    @Autoloaded
    private String name;

    public static final Column DESCRIPTION = Column.named("description");
    @Length(1024)
    @NullAllowed
    @Autoloaded
    private String description;

    @BeforeSave
    protected void checkIntegrity() {
        assertSameTenant(() -> NLS.get("Department.parent"), getParent().getValue());
        assertSameTenant(() -> NLS.get("Department.supervisor"), getSupervisor().getValue());

        Set<Long> seenIds = Sets.newTreeSet();
        seenIds.add(getId());
        Department parentToCheck = getParent().getValue();
        while (parentToCheck != null) {
            if (seenIds.contains(parentToCheck.getId())) {
                throw Exceptions.createHandled().withNLSKey("Department.loopDetected").handle();
            }
            seenIds.add(parentToCheck.getId());
            parentToCheck = parentToCheck.getParent().getValue();
        }
    }

    public EntityRef<UserAccount> getSupervisor() {
        return supervisor;
    }

    public EntityRef<Department> getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
