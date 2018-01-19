/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.categories;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import woody.core.colors.ColorData;
import woody.core.relations.RelationTypeController;
import woody.organization.OrganizationHelper;

/**
 * Created by aha on 11.01.17.
 */
public class Category extends TenantAware {

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Column TYPE = Column.named("type");
    @Length(50)
    private String type;

    public static final Column TECHNICAL_NAME = Column.named("technicalName");
    @Unique(within = {"tenant", "type"})
    @Length(100)
    @Autoloaded
    private String technicalName;

    public static final Column VIEW_ROLE = Column.named("viewRole");
    @Length(100)
    @Autoloaded
    @NullAllowed
    private String viewRole;

    public static final Column EDIT_ROLE = Column.named("editRole");
    @Length(100)
    @Autoloaded
    @NullAllowed
    private String editRole;

    public static final Column DESCRIPTION = Column.named("description");
    @Length(1024)
    @Autoloaded
    @NullAllowed
    private String description;

    public static final Column COLOR = Column.named("color");
    private final ColorData color = new ColorData();

    @Part
    private static RelationTypeController relationTypeController;

    @Part
    private static GlobalContext context;

    @BeforeSave
    @BeforeDelete
    protected void onModify() {
        relationTypeController.flushTypeCache();
        OrganizationHelper.flushCategoryCache(getTenant().getId());
    }

    public CategoryTypeProvider getProvider() {
        return context.getPart(getType(), CategoryTypeProvider.class);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public String getViewRole() {
        return viewRole;
    }

    public void setViewRole(String viewRole) {
        this.viewRole = viewRole;
    }

    public String getEditRole() {
        return editRole;
    }

    public void setEditRole(String editRole) {
        this.editRole = editRole;
    }

    public ColorData getColor() {
        return color;
    }
}
