/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.biz.tenants.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeDelete;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;

import java.util.regex.Pattern;

public class ColorDefinition extends SQLTenantAware {

    public static final Mapping NAME = Mapping.named("name");
    @Length(50)
    @Autoloaded
    private String name;

    public static final Mapping PALETTE = Mapping.named("palette");
    @Length(50)
    @Autoloaded
    @NullAllowed
    private String palette;

    public static final Mapping HEX_CODE = Mapping.named("hexCode");
    @Length(7)
    @Autoloaded
    private String hexCode;

    private static final Pattern VALID_HEX_CODE = Pattern.compile("#[0-9a-f]{6}", Pattern.CASE_INSENSITIVE);

    @Part
    private static Colors colors;

    @BeforeSave
    protected void validateHexCode() {
        if (Strings.isFilled(hexCode)) {
            if (!VALID_HEX_CODE.matcher(hexCode).matches()) {
                throw Exceptions.createHandled().withNLSKey("ColorDefinition.invalidHexCode").handle();
            }
        }
    }

    @BeforeSave
    @BeforeDelete
    protected void onModify() {
        colors.flushColorDefinition(getIdAsString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPalette() {
        return palette;
    }

    public void setPalette(String palette) {
        this.palette = palette;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }
}
