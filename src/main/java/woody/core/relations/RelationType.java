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
import sirius.kernel.commons.Strings;

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
    @Autoloaded
    private String sourceType;

    public static final Column TARGET_TYPE = Column.named("targetType");
    @Length(100)
    @NullAllowed
    @Trim
    @Autoloaded
    private String targetType;

    public static final Column MULTIPLE = Column.named("multiple");
    @Autoloaded
    private boolean multiple;

    public static final Column VIEW_IN_LIST = Column.named("viewInList");
    private boolean viewInList;

    public static final Column SHOW_REVERSE = Column.named("showReverse");
    @Autoloaded
    private boolean showReverse;

    public static final Column REVERSE_NAME = Column.named("reverseName");
    @Autoloaded
    @Length(100)
    private String reverseName;

    public String getEffectiveReverseName() {
        return Strings.isFilled(reverseName) ? reverseName : name;
    }

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

    public boolean isShowReverse() {
        return showReverse;
    }

    public void setShowReverse(boolean showReverse) {
        this.showReverse = showReverse;
    }

    public String getReverseName() {
        return reverseName;
    }

    public void setReverseName(String reverseName) {
        this.reverseName = reverseName;
    }
}
