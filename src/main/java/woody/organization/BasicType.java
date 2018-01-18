/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.di.std.Part;
import woody.core.relations.RelationTypeController;
import woody.organization.categories.Category;

public abstract class BasicType extends TenantAware {

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Column CATEGORY = Column.named("category");
    private final EntityRef<Category> category = EntityRef.on(Category.class, EntityRef.OnDelete.REJECT);

    public static final Column DESCRIPTION = Column.named("description");
    @Length(1024)
    @Autoloaded
    @NullAllowed
    private String description;

    @Part
    private static RelationTypeController relationTypeController;

    @BeforeSave
    @BeforeDelete
    protected void onModify() {
        relationTypeController.flushTypeCache();
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityRef<Category> getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
