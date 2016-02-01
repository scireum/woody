/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.tenants.TenantAware;
import sirius.kernel.commons.Strings;
import sirius.mixing.Column;
import sirius.mixing.annotations.BeforeSave;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;

/**
 * Created by aha on 25.11.15.
 */
public class Tag extends TenantAware {

    @Length(length = 255)
    @Unique(within = {"tenant", "targetType"})
    private String uniqueName;
    public static final Column UNQIUE_NAME = Column.named("uniqueName");

    @Trim
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    @Trim
    @Length(length = 255)
    private String targetType;
    public static final Column TARGET_TYPE = Column.named("targetType");

    @BeforeSave
    protected void clearTagName() {
        uniqueName = getUnqiueName(name);
    }

    public static String getUnqiueName(String tagName) {
        if (tagName != null) {
            String result = tagName.toLowerCase().replaceAll("[^a-z0-9]", "");
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
}
