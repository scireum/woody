/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.categories;

import sirius.biz.tenants.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import woody.core.colors.ColorData;
import woody.core.colors.Colors;
import woody.core.relations.RelationTypeController;
import woody.organization.OrganizationHelper;

/**
 * Created by aha on 11.01.17.
 */
public class Category extends SQLTenantAware {

    public static final Mapping NAME = Mapping.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Mapping TYPE = Mapping.named("type");
    @Length(50)
    private String type;

    public static final Mapping TECHNICAL_NAME = Mapping.named("technicalName");
    @Unique(within = {"tenant", "type"})
    @Length(100)
    @Autoloaded
    private String technicalName;

    public static final Mapping VIEW_ROLE = Mapping.named("viewRole");
    @Length(100)
    @Autoloaded
    @NullAllowed
    private String viewRole;

    public static final Mapping EDIT_ROLE = Mapping.named("editRole");
    @Length(100)
    @Autoloaded
    @NullAllowed
    private String editRole;

    public static final Mapping DESCRIPTION = Mapping.named("description");
    @Length(1024)
    @Autoloaded
    @NullAllowed
    private String description;

    public static final Mapping COLOR = Mapping.named("color");
    private final ColorData color = new ColorData();

    @Part
    private static RelationTypeController relationTypeController;

    @Part
    private static GlobalContext context;

    @Part
    private static Colors colors;

    @BeforeSave
    @BeforeDelete
    protected void onModify() {
        relationTypeController.flushTypeCache();
        OrganizationHelper.flushCategoryCache(getTenant().getId());
    }

    public CategoryTypeProvider getProvider() {
        return context.getPart(getType(), CategoryTypeProvider.class);
    }

    public String getEffectiveColor() {
        return colors.getColor(getColor().getColor()).orElse(colors.getColorForType(getProvider().getColorType()));
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
