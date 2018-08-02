/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.jdbc.tenants.SQLTenantAware;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.annotations.Unique;

import java.util.List;

/**
 * Created by gerhardhaufler on 09.02.16.
 */
// ToDo framework products klären
//@Framework("products")
public class Product extends SQLTenantAware {

    @Trim
    @Autoloaded
    @Unique(within = "tenant")
    @Length(255)
    private String name;
    public static final Mapping NAME = Mapping.named("name");

    @Trim
    @Autoloaded
    @Length(255)
    private String article;
    public static final Mapping ARTICLE = Mapping.named("article");

    @Autoloaded
    private ProductType productType = ProductType.SERVICE;
    public static final Mapping PRODUCTTYPE = Mapping.named("productType");

    @Autoloaded
    private Boolean collectBugs = false;
    public static final Mapping COLLECTBUGS = Mapping.named("collectBugs");

    @NullAllowed
    @Autoloaded
    @Length(255)
    private String image;
    public static final Mapping IMAGE = Mapping.named("image");

    @NullAllowed
    @Autoloaded
    @Length(1000)
    private String description;
    public static final Mapping DESCRIPTION = Mapping.named("description");

    public static List<Product> getValues() {
        return oma.select(Product.class).orderAsc(Product.NAME).queryList();
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

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
}
