/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization;

import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.core.colors.ColorData;
import woody.core.relations.RelationTypeController;
import woody.organization.categories.Category;

public abstract class BasicType extends SQLTenantAware {

    public static final Mapping NAME = Mapping.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Mapping CATEGORY = Mapping.named("category");
    private final SQLEntityRef<Category> category = SQLEntityRef.on(Category.class, SQLEntityRef.OnDelete.REJECT);

    public static final Mapping DESCRIPTION = Mapping.named("description");
    @Length(1024)
    @Autoloaded
    @NullAllowed
    private String description;

    public static final Mapping CODE_PREFIX = Mapping.named("codePrefix");
    @Length(20)
    @Autoloaded
    @NullAllowed
    private String codePrefix;

    public static final Mapping COLOR = Mapping.named("color");
    private final ColorData color = new ColorData();

    @Part
    private static RelationTypeController relationTypeController;

    @BeforeSave
    protected void onSave() {
        if (Strings.isFilled(codePrefix)) {
            codePrefix = codePrefix.trim().toUpperCase();
            if (codePrefix.endsWith("-")) {
                codePrefix = codePrefix.substring(0, codePrefix.length() - 1);
            }
        }
    }

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

    public SQLEntityRef<Category> getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ColorData getColor() {
        return color;
    }

    public String getCodePrefix() {
        return codePrefix;
    }

    public void setCodePrefix(String codePrefix) {
        this.codePrefix = codePrefix;
    }
}
