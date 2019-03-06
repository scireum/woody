/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.items;

import sirius.biz.tenants.jdbc.SQLTenantAware;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

public class Item extends SQLTenantAware implements HasComments, HasRelations {

    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Length(255)
    private String name;

    public static final Mapping ITEM_NUMBER = Mapping.named("itemNumber");
    @Trim
    @Length(255)
    private String itemNumber;

    public static final Mapping MATCH_CODE = Mapping.named("matchCode");
    @Trim
    @Length(255)
    @NullAllowed
    private String matchCode;

    public static final Mapping ONE_OFF_PRICE = Mapping.named("oneOffPrice");
    @Numeric(scale = 3, precision = 15)
    private Amount oneOffPrice = Amount.NOTHING;

    public static final Mapping MONTHLY_CHARGE = Mapping.named("monthlyCharge");
    @Numeric(scale = 3, precision = 15)
    private Amount monthlyCharge = Amount.NOTHING;

    public static final Mapping PRICE_QUANTITY = Mapping.named("priceQuantity");
    @Numeric(scale = 3, precision = 15)
    private Amount priceQuantity = Amount.ONE;

    public static final Mapping DISCOUNT_GROUP = Mapping.named("discountGroup");
    @Trim
    @Length(10)
    @NullAllowed
    private String discountGroup;

    public static final Mapping VAT_RATE = Mapping.named("vatRate");
    @Trim
    @Length(10)
    private String vatRate;

    public static final Mapping QUANTITY_UNIT = Mapping.named("quantityUnit");
    @Trim
    @Length(10)
    private String quantityUnit;

    public static final Mapping MIN_QUANTITY = Mapping.named("minQuantity");
    @Numeric(scale = 3, precision = 15)
    private Amount minQuantity = Amount.ONE;

    public static final Mapping QUANTITY_STEP = Mapping.named("quantityStep");
    @Numeric(scale = 3, precision = 15)
    private Amount quantityStep = Amount.ONE;

    private final Tagged tags = new Tagged(this);
    public static final Mapping TAGS = Mapping.named("tags");

    private final Commented comments = new Commented(this);
    public static final Mapping COMMENTS = Mapping.named("comments");

    private final Relations relations = new Relations(this);
    public static final Mapping RELATIONS = Mapping.named("relations");

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode;
    }

    public String getDiscountGroup() {
        return discountGroup;
    }

    public void setDiscountGroup(String discountGroup) {
        this.discountGroup = discountGroup;
    }

    public String getVatRate() {
        return vatRate;
    }

    public void setVatRate(String vatRate) {
        this.vatRate = vatRate;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
}
