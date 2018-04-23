/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import woody.core.colors.ColorData;

/**
 * Created by aha on 11.01.17.
 */
public class RelationType extends TenantAware {

    public static final Column NAME = Column.named("name");
    @Length(100)
    @Autoloaded
    private String name;

    public static final Column SOURCE_TYPE = Column.named("sourceType");
    @Length(100)
    @Trim
    private String sourceType;

    public static final Column TARGET_TYPE = Column.named("targetType");
    @Length(100)
    @NullAllowed
    @Trim
    private String targetType;

    public static final Column MULTIPLE = Column.named("multiple");
    @Autoloaded
    private boolean multiple;

    public static final Column VIEW_IN_LIST = Column.named("viewInList");
    @Autoloaded
    private boolean viewInList;

    public static final Column LIST_REVERSE = Column.named("listReverse");
    @Autoloaded
    private boolean listReverse;

    public static final Column COLOR = Column.named("color");
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
