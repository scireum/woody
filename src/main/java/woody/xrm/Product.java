/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.tenants.TenantAware;
import sirius.biz.web.Autoloaded;
import sirius.kernel.di.std.Framework;
import sirius.mixing.Column;
import sirius.mixing.annotations.Length;
import sirius.mixing.annotations.NullAllowed;
import sirius.mixing.annotations.Trim;
import sirius.mixing.annotations.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
//@Framework("products")
public class Product extends TenantAware{

    @Trim
    @Autoloaded
    @Unique(within = "tenant")
    @Length(length = 255)
    private String name;
    public static final Column NAME = Column.named("name");

    @Trim
    @Autoloaded
    @Length(length = 255)    private String article;
    public static final Column ARTICLE = Column.named("article");

    @Autoloaded
    private Boolean collectBugs = false;
    public static final Column COLLECTBUGS = Column.named("collectBugs");

   /*  @FormField(position = 16)
    @Params({
            @Param(name = EntityObjectAdapter.PARAM_IMAGE_FIELD, value = "true"),
            @Param(name = EntityObjectAdapter.PARAM_IMAGE_PATH, value = "/images/Produkte"),
            @Param(name = ParamsFieldConstants.PARAM_CLEAR_LEFT, value = "true") })
    @Image
    @Column(name = IMAGE, nullable = true, length = 255)       */
    @NullAllowed
    @Autoloaded
    @Length(length = 255)
    private String image;
    public static final Column IMAGE = Column.named("image");

 /*   @FormField(position = 19, section = "description")
    @Params({
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_NO_LABEL, value = "true"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_MAXIMIZED, value = "false"),
            @Param(name = ParamsFieldConstants.PARAM_TEXT_AREA_SYNTAX, value = TextArea.MARKDOWN) })
    @Lob
    @Column(name = DESCRIPTION, nullable = true)   */
    @NullAllowed
    @Autoloaded
    @Length(length = 1000)
    private String description;
    public static final Column DESCRIPTION = Column.named("description");

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

    public Boolean getCollectBugs() {
        return collectBugs;
    }

    public void setCollectBugs(Boolean collectBugs) {
        this.collectBugs = collectBugs;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

}
