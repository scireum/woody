/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.jdbc.tenants.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import woody.core.colors.ColorData;

/**
 * Created by aha on 11.01.17.
 */
public class RelationType extends SQLTenantAware {

    public static final Mapping NAME = Mapping.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Mapping SOURCE_TYPE = Mapping.named("sourceType");
    @Length(100)
    @Trim
    private String sourceType;

    public static final Mapping TARGET_TYPE = Mapping.named("targetType");
    @Length(100)
    @NullAllowed
    @Trim
    private String targetType;

    public static final Mapping MULTIPLE = Mapping.named("multiple");
    @Autoloaded
    private boolean multiple;

    public static final Mapping VIEW_IN_LIST = Mapping.named("viewInList");
    @Autoloaded
    private boolean viewInList;

    public static final Mapping LIST_REVERSE = Mapping.named("listReverse");
    @Autoloaded
    private boolean listReverse;

    public static final Mapping COLOR = Mapping.named("color");
    private final ColorData color = new ColorData();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isViewInList() {
        return viewInList;
    }

    public void setViewInList(boolean viewInList) {
        this.viewInList = viewInList;
    }

    public boolean isListReverse() {
        return listReverse;
    }

    public void setListReverse(boolean listReverse) {
        this.listReverse = listReverse;
    }

    public ColorData getColor() {
        return color;
    }
}
