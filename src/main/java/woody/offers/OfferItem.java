/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.offers;


import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.DatabaseMetaData;
import sirius.biz.model.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.Database;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.AfterSave;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.NumberFormat;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.sales.CompanyAccountingData;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.sales.ProductType;
import woody.xrm.Company;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Created by gerhardhaufler on 18.04.14.
 */

/**
 * <code>
 * offeritem = Angebots-Position
 *   </code>
 */

public class OfferItem extends BizEntity {

    protected static final String DIENSTLEISTUNGS_PREFIX = "Dienstleistung";

    @Autoloaded
    private final EntityRef<Offer> offer = EntityRef.on(Offer.class, EntityRef.OnDelete.CASCADE);
    public static final Column OFFER = Column.named("offer");

    @Autoloaded
    private Integer position ;
    public static final Column POSITION = Column.named("position");

    @Autoloaded
    private OfferItemType offerItemType = OfferItemType.SERVICE;
    public static final Column OFFERITEMTYPE = Column.named("offerItemType");

    @Autoloaded
    @NullAllowed
    private final EntityRef<Product> baseProduct = EntityRef.on(Product.class, EntityRef.OnDelete.CASCADE);
    public static final Column BASEPRODUCT = Column.named("baseProduct");

    @Autoloaded
    @NullAllowed
    private final EntityRef<PackageDefinition> packageDefinition = EntityRef.on(PackageDefinition.class, EntityRef.OnDelete.CASCADE);
    public static final Column PACKAGEDEFINITION = Column.named("packageDefinition");

    @Autoloaded
    @NullAllowed
    @Length(100)
    private String keyword;
    public static final Column KEYWORD = Column.named("keyword");

    @Autoloaded
    @NullAllowed
    @Length(1000)
    private String text;
    public static final Column TEXT = Column.named("text");

    @Autoloaded
    @NullAllowed
    @Numeric(scale = 2, precision = 15)
    private Amount quantity;
    public static final Column QUANTITY = Column.named("quantity");

    @Autoloaded
    @NullAllowed
    @Length(5)
    private String accountingUnit;
    public static final Column ACCOUNTINGUNIT = Column.named("accountingUnit");


    @Autoloaded
    @NullAllowed
    @Numeric(scale = 2, precision = 15)
    private Amount singlePrice;
    public static final Column SINGLEPRICE = Column.named("singlePrice");


    @NullAllowed
    @Length(20)
    private String priceBase;
    public static final Column PRICEBASE = Column.named("priceBase");


    @NullAllowed
    @Autoloaded
    @Numeric(scale = 2, precision = 15)
    private Amount discount;
    public static final Column DISCOUNT = Column.named("discount");

    @NullAllowed
    @Numeric(scale = 2, precision = 15)
    private Amount price;
    public static final Column PRICE = Column.named("price");

    @NullAllowed
    @Numeric(scale = 2, precision = 15)
    private Amount cyclicPrice;
    public static final Column CYCLICPRICE = Column.named("cyclicPrice");

// ToDo    @Filter(position = 1000, filterEmpty = false)
    @Autoloaded
    private OfferItemState state;
    public static final Column STATE = Column.named("state");

    @NullAllowed
    @Autoloaded
    private LocalDate offerDate;
    public static final Column OFFERDATE = Column.named("offerDate");

    @NullAllowed
    @Autoloaded
    private LocalDate orderDate;
    public static final Column ORDERDATE = Column.named("orderDate");

    @NullAllowed
    @Autoloaded
    private LocalDate salesConfirmationDate = null;
    public static final Column SALESCONFIRMATIONDATE = Column.named("salesConfirmationDate");

    @NullAllowed
    @Autoloaded
    private LocalDate completionDate;
    public static final Column COMPLETIONDATE = Column.named("completionDate");

    @NullAllowed
    @Autoloaded
    private LocalDate acceptanceDate;
    public static final Column ACCEPTANCEDATE = Column.named("acceptanceDate");

    @NullAllowed
    @Autoloaded
    private LocalDate accountingDate;
    public static final Column ACCOUNTINGDATE = Column.named("accountingDate");

    @NullAllowed
    @Length(2000)
    private String history;
    public static final Column HISTORY = Column.named("history");

    @NullAllowed
    private Integer savedPosition;

    private boolean flagOneTime = true;


    @Part
    private static ServiceAccountingService sas;


    public List<PackageDefinition> getAllPackageDefinitionsOrderedByProduct() {
        List<PackageDefinition> pdList = oma.select(PackageDefinition.class)
              .orderAsc(PackageDefinition.PRODUCT.join(Product.NAME))
              .orderAsc(PackageDefinition.NAME).queryList();
        return pdList;
    }

    @BeforeSave
    protected void onSave()  {
        // check te Role of the user
        UserInfo userInfo = UserContext.getCurrentUser();
        userInfo.assertPermission("offers");

        Offer offer = getOffer().getValue();
        Company company = offer.getCompany().getValue();

        if (isInfoText()) {
            offerItemType = OfferItemType.INFOTEXT ;
            keyword = "";
            state = OfferItemState.UNUSED;
            priceBase = "nicht benutzt";
            accountingUnit = "";
            if (quantity!=null) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoQuatity").handle();
            }
            if (singlePrice != null) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoQuatity").handle();
            }
            if (acceptanceDate != null) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoAcceptanceDate").handle();
            }

        } else {
            if(isSum()) {
                offerItemType = OfferItemType.SUM ;
                keyword = "";
                state = OfferItemState.UNUSED;
                priceBase = "nicht benutzt";
                accountingUnit = "";
                if(Strings.isEmpty(text)) {
                    text = "Zwischensummen:";
                }
            } else {
                // check the offerType
                if (isService()) {
                    offerItemType = OfferItemType.SERVICE;
                }
                if(Strings.isEmpty(this.getKeyword())) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.keywordMissing").set("pos", position).handle();
                }
                //check the position-text
                if(Strings.isEmpty(this.getText())) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.textMissing").handle();
                }
                if (packageDefinition == null) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.packageDefinitionMissing").handle();
                }
                // complete the product
                if (baseProduct.getValue() == null) {
                    Product product = this.getPackageDefinition().getValue().getProduct().getValue();
                    long id = product.getId();
                    this.getBaseProduct().setId(id);
                }
                // check the packageDefinition
                PackageDefinition pd = oma.select(PackageDefinition.class)
                        .eq(PackageDefinition.PRODUCT, baseProduct)
                        .eq(PackageDefinition.NAME, packageDefinition.getValue().getName())
                        .queryFirst();
                if (pd == null) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.pdWrong")
                                    .set("prod", packageDefinition.getValue().getProduct().getValue().getName())
                                    .set("pd", packageDefinition.getValue().getName()).handle();
                }

                if (packageDefinition.getId() != pd.getId()) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.idWrong")
                                    .set("pd1", packageDefinition.getValue().getName())
                                    .set("pd2", pd.getName()).handle();
                }
                if (isLicense()) {
                    offerItemType = OfferItemType.LICENSE;
                    accountingUnit = packageDefinition.getValue().getAccountingUnit();
                }

                // check the quantity
                if (isService()) {
                    if (Strings.isEmpty(quantity)) {
                          throw Exceptions.createHandled().withNLSKey("OfferItem.quanityMissing").handle();
                    }
                }

                // check the acceptanceDate
                if (OfferItemState.OFFER.equals(state)) {
                    if (acceptanceDate != null) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.noAcceptanceDate").handle();
                    }
                }
                // check the singlePrice
                if (isService()) {
                    // ToDo Exception bei singlePrice == null :                     if (singlePrice.isZeroOrNull()) {
                    if (singlePrice == null || singlePrice.isZeroOrNull()) {
                        //  is a price for this company present? -> take the company-price
                        CompanyAccountingData companyAccountingData = company.getCompanyAccountingData();
                        if(companyAccountingData != null) {
                            if (companyAccountingData.getPtPrice() != null) {
                                singlePrice = companyAccountingData.getPtPrice();
                                priceBase = "Firma";
                            }
                        }

                        // is a Package-price present? --> take the package-price
                         pd = oma.select(PackageDefinition.class)
                                .eq(PackageDefinition.NAME, packageDefinition.getValue().getName())
                                .eq(PackageDefinition.PRODUCT, baseProduct)
                                .queryFirst();
                        if (pd != null) {
                            singlePrice = pd.getUnitPrice();
                            priceBase = "Paket";
                        } else {
                            pd = oma.select(PackageDefinition.class)
                                    .eq(PackageDefinition.PRODUCT, baseProduct).queryFirst();
                            if (pd != null) {
                                singlePrice = pd.getUnitPrice();
                                priceBase = "Produkt";
                            }
                        }

                    }
                    if (singlePrice == null || singlePrice.isZeroOrNull()) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.singlePriceMissing").handle();
                    }
                }
                if (isLicense()) {
                    cyclicPrice = packageDefinition.getValue().getUnitPrice();
                    priceBase = "Paket";
                    if (cyclicPrice == null) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.cyclicPriceMissing").handle();
                    }
                    if(singlePrice == null) {
                        if (isPackageDefinitionSinglePricePresent()) {
                            singlePrice = packageDefinition.getValue().getSinglePrice();
                        }
                    }
                    if(quantity == null) {
                        quantity = Amount.ONE;
                    }
                }
                // check te accountingUnit
                if (Strings.isEmpty(accountingUnit)) {
                    if (ProductType.LICENSE.equals(packageDefinition.getValue().getProduct().getValue().getProductType())) {
                        accountingUnit = packageDefinition.getValue().getAccountingUnit();
                    }
                    if (ProductType.SERVICE.equals(packageDefinition.getValue().getProduct().getValue().getProductType())) {
                        accountingUnit = "PT";
                    }
                    if (Strings.isEmpty(accountingUnit)) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.quantityUnitMissing").handle();
                    }
                }
                // calculate the price
                Amount discount1 = Amount.ZERO;
                if (discount != null && discount.isPositive() && (discount.compareTo(Amount.ONE_HUNDRED) < 1)) {
                    discount1 = discount;
                }
                if(singlePrice != null) {
                    price = quantity.times(singlePrice);
                    price = price.decreasePercent(discount1);
                }

                // store the dates
                if (OfferItemState.OFFER.equals(state)) {
                    if (offerDate == null) {
                        offerDate = LocalDate.now();
                    }
                }
                if (OfferItemState.ORDERED.equals(state)) {
                    if (orderDate == null) {
                        orderDate = LocalDate.now();
                    }
                }

                // the salesConfirmationDate is set when the mail with the salesConfirmation is send to the receiver
                if (OfferItemState.DEVELOPED.equals(state)) {
                    if (completionDate == null) {
                        completionDate = LocalDate.now();
                    }
                }
                if (OfferItemState.ACCEPTED.equals(state)) {
                    if (acceptanceDate == null) {
                        acceptanceDate = LocalDate.now();
                    }
                }
                if (OfferItemState.ACCOUNTED.equals(state)) {
                    if (accountingDate == null) {
                        accountingDate = LocalDate.now();
                    }
                }
            }
        }

        // the beforeSaveChecks-Method runs on each save two times!
        // the following statements guaranty, that the following statements are executed only one time
        if (flagOneTime) {
            flagOneTime = false;
        // generate the position-number
            if (position == null) {
                OfferItem offerItem  = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
                                          .orderDesc(OfferItem.POSITION).queryFirst();
                int pos = offerItem.getPosition() + 10;
                int  divisionsrest = pos % 10;
                position =  pos - divisionsrest;
                savedPosition = position;
            }
            if(!(isSum() || isInfoText()) ) {
                // build the history string, organize last come --> first serve
                // offerItems withe the same md5-key are saved only one time in the history
                String md5 =  buildMD5(OfferItem.this);
                if(Strings.isFilled(history)) {
                    // adapt the history-format
                    history = history.replace("&nbsp;", " ");
                    history = history.replace("<br>", "\n");
                    // check the md5-key
                    int posMd5 = history.indexOf("md5: ") + 5;
                    String md5SubStr = history.substring(posMd5, posMd5 + 32);
                    if(md5SubStr.equals( md5) ) {return; }   // md5-key is the same --> no change done
                }
                String stateFix = getStateFix(14);
                String username = userInfo.getUserName();
                String s = NLS.toUserString(LocalDateTime.now()) + "   " + username + ":  " + getStateFix(14) + ", md5: " + buildMD5(OfferItem.this);
                if (history == null) {
                    history = "";
                }
                if (Strings.isFilled(s)) {
                    s = s + "\n" + history;
                    history = s;
                }
            }
        } else {
            flagOneTime = true;
            if (position == null) {
                position = savedPosition;
                savedPosition = 0;
            }

        }
    }
    @AfterSave
    protected void afterSave() {
       Offer offer = this.getOffer().getValue();
       sas.updateOfferState(offer, true);
    }

    // returns the state of the offerItem with a fix length
    private String getStateFix( int length1) {
        String s = state.toString();
        String s1 = state.getOfferItemStatePostFix();
        s = s + s1;
        return s;
    }

    // builds a md5-String for the parameters of a offerItem
    private String buildMD5(OfferItem o) {
        String s=o.getPosition().toString()+o.getText();
        if(o.isInfoText() || o.isSum()) {
            // do nothing
        } else {
            s = s+o.getPriceBase() + NLS.toUserString(o.getSinglePrice()) + NLS.toUserString(o.getQuantity()) + o.getAccountingUnit() +
            o.getOffer().getValue().getUniqueName(); // getUnqiueObjectName();
            if(getDiscountPresent()) {
                s = s + NLS.toUserString(discount.toString());
            }
        }

//        String md5 = BaseEncoding.base64().encode(Hashing.md5().hashString(s, Charsets.UTF_8).asBytes());
        byte[] md5HashBytes = Hashing.md5().hashString(s, Charsets.UTF_8).asBytes();
        StringBuilder sb = new StringBuilder(md5HashBytes.length * 2);
        for (int i = 0; i < md5HashBytes.length; i++) {
            sb.append(Character.forDigit((md5HashBytes[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(md5HashBytes[i] & 0x0f, 16));
        }
        String md5 = sb.toString();
        return md5;
    }


    /**
     * Computes the hex-representation of the given byte array.
     */
    private String toHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (int i = 0; i < a.length; i++) {
            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(a[i] & 0x0f, 16));
        }
        return sb.toString();
    }


    // these methods are called by the JSF

    public String getCyclicPriceAsString() {
        if(this.getCyclicPrice() == null) {
            return "";
        } else {
            return this.getCyclicPrice().toString(NumberFormat.TWO_DECIMAL_PLACES).asString();
        }
    }

   public boolean getStateIsVisible() {
        if(state.equals(OfferItemState.OFFER)) {
            return false ;
        }  else {
            return true;
        }
    }

    public Amount checkCyclicPrice() {
        Amount price = getCyclicPrice();
        if(getDiscountPresent()) {
            price = price.decreasePercent(getDiscount());
        }
        return price;
    }


    public boolean getDiscountPresent() {
        if(discount != null && discount.isPositive()) {
            return true ;
        }  else {
            return false;
        }
    }

    public boolean isService() {
        return isIs(ProductType.SERVICE, true);
    }
    public boolean isLicense() {
        return isIs(ProductType.LICENSE, false);
    }

    private boolean isIs(ProductType type, boolean returnValueIfNull) {
        if(packageDefinition == null) {  // in the older versions the paketDefinition may be == null
            return returnValueIfNull;
        }
        if(type.equals(packageDefinition.getValue().getProduct().getValue().getProductType())) {
            return true;
        }
        return false;
    }

    public boolean isPackageDefinitionSinglePricePresent() {
        if(packageDefinition.getValue().getSinglePrice() != null && packageDefinition.getValue().getSinglePrice().isPositive() )  {
            return true;
        }
        return false;
    }

    public boolean isInfoText() {
        if(OfferItemType.INFOTEXT.equals(offerItemType))  {
            return true;
        }
        return false;
    }

    public boolean isSum() {
        if(OfferItemType.SUM.equals(offerItemType))  {
            return true;
        }
        return false;
    }

    public boolean showNextState() {
        if(OfferItemState.UNUSED.equals(this.getState())) {return false;}
        if(OfferItemState.CANCELED.equals(this.getState())) {return false;}
        if(OfferItemState.COPY.equals(this.getState())) {return false;}
        return true;
    }

    public EntityRef<Offer> getOffer() {
        return offer;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public OfferItemType getOfferItemType() {
        return offerItemType;
    }

    public void setOfferItemType(OfferItemType offerItemType) {
        this.offerItemType = offerItemType;
    }

    public EntityRef<Product> getBaseProduct() {
        return baseProduct;
    }

    public EntityRef<PackageDefinition> getPackageDefinition() {

        return packageDefinition;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Amount getQuantity() {
        return quantity;
    }

    public void setQuantity(Amount quantity) {
        this.quantity = quantity;
    }

    public String getAccountingUnit() {
        return accountingUnit;
    }

    public void setAccountingUnit(String accountingUnit) {
        this.accountingUnit = accountingUnit;
    }

    public Amount getSinglePrice() {
        return singlePrice;
    }

    public void setSinglePrice(Amount singlePrice) {
        this.singlePrice = singlePrice;
    }

    public String getPriceBase() {
        return priceBase;
    }

    public void setPriceBase(String priceBase) {
        this.priceBase = priceBase;
    }

    public Amount getDiscount() {
        return discount;
    }

    public void setDiscount(Amount discount) {
        this.discount = discount;
    }

    public Amount getPrice() {
        return price;
    }

    public void setPrice(Amount price) {
        this.price = price;
    }

    public Amount getCyclicPrice() {
        return cyclicPrice;
    }

    public void setCyclicPrice(Amount cyclicPrice) {
        this.cyclicPrice = cyclicPrice;
    }

    public OfferItemState getState() {
        return state;
    }

    public void setState(OfferItemState state) {
        this.state = state;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(LocalDate offerDate) {
        this.offerDate = offerDate;
    }

    public static String getDienstleistungsPrefix() {
        return DIENSTLEISTUNGS_PREFIX;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getSalesConfirmationDate() {
        return salesConfirmationDate;
    }

    public void setSalesConfirmationDate(LocalDate salesConfirmationDate) {
        this.salesConfirmationDate = salesConfirmationDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public LocalDate getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(LocalDate acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public LocalDate getAccountingDate() {
        return accountingDate;
    }

    public void setAccountingDate(LocalDate accountingDate) {
        this.accountingDate = accountingDate;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
