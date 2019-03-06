/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import woody.core.colors.ColorData;
import woody.core.colors.Colors;

/**
 * Created by aha on 25.11.15.
 */
public class Tag extends SQLTenantAware {

    public static final Mapping UNQIUE_NAME = Mapping.named("uniqueName");
    @Length(255)
    @Unique(within = {"tenant", "targetType"})
    private String uniqueName;

    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Length(255)
    @Autoloaded
    private String name;

    public static final Mapping TARGET_TYPE = Mapping.named("targetType");
    @Trim
    @Length(255)
    private String targetType;

    public static final Mapping VIEW_IN_LIST = Mapping.named("viewInList");
    @Autoloaded
    private boolean viewInList;

    public static final Mapping COLOR = Mapping.named("color");
    private final ColorData color = new ColorData();

    @Part
    private static Colors colors;

    public String getEffectiveColor() {
        return colors.getColor(getColor().getColor())
                     .orElseGet(() -> colors.getColorForType(TagQueryTagColorTypeProvider.TYPE));
    }

    @BeforeSave
    protected void clearTagName() {
        uniqueName = getUnqiueName(name);
    }

    public static String getUnqiueName(String tagName) {
        if (tagName != null) {
            String result = tagName.toLowerCase().replaceAll("[^a-z0-9_]", "");
            if (Strings.isEmpty(result)) {
                return null;
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public boolean isViewInList() {
        return viewInList;
    }

    public void setViewInList(boolean viewInList) {
        this.viewInList = viewInList;
    }

    public ColorData getColor() {
        return color;
    }
}
