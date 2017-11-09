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

import sirius.biz.codelists.CodeLists;
import sirius.biz.model.BizEntity;
import sirius.biz.tenants.Tenants;
import sirius.biz.web.Autoloaded;

import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.AfterSave;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Transient;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.NumberFormat;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.sales.CompanyAccountingData;
import woody.sales.Contract;
import woody.sales.PackageDefinition;
import woody.sales.Product;
import woody.sales.ProductType;
import woody.xrm.Company;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
    private final EntityRef<PackageDefinition> packageDefinition = EntityRef.on(PackageDefinition.class, EntityRef.OnDelete.CASCADE);
    public static final Column PACKAGEDEFINITION = Column.named("packageDefinition");

    @Autoloaded
    @Length(100)
    private String keyword;
    public static final Column KEYWORD = Column.named("keyword");

    @Autoloaded
    @Length(1000)
    private String text;
    public static final Column TEXT = Column.named("text");

    @Autoloaded
    @Numeric(scale = 2, precision = 15)
    private Amount quantity = Amount.NOTHING;
    public static final Column QUANTITY = Column.named("quantity");

    /* contains the via codelist 'accountingUnit' translated value */
    @Autoloaded
    @Length(20)
    private String accountingUnitComplete;
    public static final Column ACCOUNTINGUNITCOMPLETE = Column.named("accountingUnitComplete");

    /* singlePrice:
     * licenses = one times singlePrice
     * service  = price / day                */
    @Autoloaded
    @Numeric(scale = 2, precision = 15)
    private Amount singlePrice = Amount.NOTHING;
    public static final Column SINGLEPRICE = Column.named("singlePrice");

    @NullAllowed
    @Length(20)
    private String priceBase;
    public static final Column PRICEBASE = Column.named("priceBase");

    @Autoloaded
    @Numeric(scale = 2, precision = 15)
    private Amount discount = Amount.NOTHING;
    public static final Column DISCOUNT = Column.named("discount");

    /* cyclicPrice:
     * licenses = price e.g. per month
     * service  = not used                */
    @Numeric(scale = 2, precision = 15)
    private Amount cyclicPrice = Amount.NOTHING;
    public static final Column CYCLICPRICE = Column.named("cyclicPrice");

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
    @Autoloaded
    private LocalDate contractStartDate = null;
    public static final Column CONTRACTSTARTDATE = Column.named("contractStartDate");

    @NullAllowed
    @Length(2000)
    private String history;
    public static final Column HISTORY = Column.named("history");


    /* used for offer: singlePriceComplete = (100-discount)/100 * quantity * singlePrice    */
    @Transient
    private Amount singlePriceComplete = Amount.NOTHING;

    /* used for offer: cyclicPriceComplete = 100-discount)/100 * quantity * cyclicPrice */
    @Transient
    private Amount cyclicPriceComplete = Amount.NOTHING;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Tenants  tenants;

    @Part
    private static CodeLists cls;

    public List<PackageDefinition> getAllPackageDefinitionsOrderedByProduct() {
        List<PackageDefinition> pdList = oma.select(PackageDefinition.class)
              .eq(PackageDefinition.PRODUCT.join(Product.TENANT), tenants.getRequiredTenant())
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

        // ToDo alle Exceptions entweder auslagern oder als Warning machen. (CRM-34)

        if (isInfoText()) {
            keyword = "";
            state = OfferItemState.UNUSED;
            priceBase = "nicht benutzt";
            accountingUnitComplete = "";
            if (!quantity.isZeroOrNull()) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoQuantity")
                                .set("angebot", offer.getNumber()).set("pos", position).handle();
            }
            if (!singlePrice.isZeroOrNull()) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoPrice")
                                .set("angebot", offer.getNumber()).set("pos", position).handle();
            }
            if (acceptanceDate != null) {
                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoAcceptanceDate")
                                .set("angebot", offer.getNumber()).set("pos", position).handle();
            }

        } else {
            if(isSum()) {
                keyword = "";
                state = OfferItemState.UNUSED;
                priceBase = "nicht benutzt";
                accountingUnitComplete = "";
                if(Strings.isEmpty(text)) {
                    text = "Zwischensummen:";
                }
            } else {
                //set the accountingUnitComplete
                String accUnit = this.getPackageDefinition().getValue().getAccountingUnit();
                Tuple<String, String> tuple = cls.getValues("accountingUnit", accUnit);
                this.setAccountingUnitComplete(tuple.getFirst());

                // check the accountingUnit
                if (Strings.isEmpty(accUnit)) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.accountingUnitMissing")
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }

               if (isLicense()) {
                    // set keyword and text with default-values from the packageDefinition  (CRM-64)
                    if(this.getKeyword()== null ) {
                        this.setKeyword(this.getPackageDefinition().getValue().getName());
                    }
                    if(this.getText() == null) {
                        if(this.getPackageDefinition().getValue().getDescription() != null) {
                            this.setText(this.getPackageDefinition().getValue().getDescription());
                        }
                    }
                }

                // check the presence of the keyword
                if(Strings.isEmpty(this.getKeyword())) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.keywordMissing")
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }
                // check the presence of the position-text
                if(Strings.isEmpty(this.getText())) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.textMissing")
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }

                // check the presence of the packageDefinition
                if (packageDefinition == null) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.packageDefinitionMissing")
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }

                // check the packageDefinition
                Product product = this.getPackageDefinition().getValue().getProduct().getValue();
                PackageDefinition pd = oma.select(PackageDefinition.class)
                        .eq(PackageDefinition.PRODUCT, product)
                        .eq(PackageDefinition.NAME, packageDefinition.getValue().getName())
                        .queryFirst();
                if (pd == null) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.pdWrong")
                                    .set("prod", packageDefinition.getValue().getProduct().getValue().getName())
                                    .set("pd", packageDefinition.getValue().getName())
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }

                if (packageDefinition.getId() != pd.getId()) {
                    throw Exceptions.createHandled().withNLSKey("OfferItem.idWrong")
                                    .set("pd1", packageDefinition.getValue().getName())
                                    .set("pd2", pd.getName())
                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
                }

                // check the quantity
                if (quantity.isZeroOrNull()) {
                      quantity = Amount.of(1);
                }

                // check the acceptanceDate
                if (OfferItemState.OFFER.equals(state)) {
                    if (acceptanceDate != null) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.noAcceptanceDate")
                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
                    }
                }

                // check the singlePrice
                if (isService()) {
                    if (singlePrice.isZeroOrNull()) {
                        //  is a price for this company present? -> take the company-price
                        CompanyAccountingData companyAccountingData = company.getCompanyAccountingData();
                        if(companyAccountingData != null && companyAccountingData.getPtPrice() != null) {
                                singlePrice = companyAccountingData.getPtPrice();
                                priceBase = "Firma";
                        } else {
                                // is a Package-price present? --> take the package-price
                                singlePrice = packageDefinition.getValue().getUnitPrice();
                                priceBase = "Paket";
                        }
                    }
                    // check the singlePrice of a service (e.g. 800 EUR/day)
                    if (singlePrice.isZeroOrNull()) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.singlePriceMissing")
                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
                    }
                }
                if (isLicense()) {
                    // check the cyclicPrice
                    if(cyclicPrice.equals(Amount.NOTHING) ) {
                        cyclicPrice = packageDefinition.getValue().getUnitPrice();
                    }
                    priceBase = "Paket";
                    if((cyclicPrice.compareTo(packageDefinition.getValue().getUnitPrice()) != 0)) {
                        priceBase = "Angebot";
                    }
                    if (cyclicPrice.equals(Amount.NOTHING)) {
                        throw Exceptions.createHandled().withNLSKey("OfferItem.cyclicPriceMissing")
                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
                    }
                    // check the singlePrice
                    if(singlePrice.equals(Amount.NOTHING)) {
                        if (isPackageDefinitionSinglePricePresent()) {
                            singlePrice = packageDefinition.getValue().getSinglePrice();
                        }
                    }
                }

                // check the values (interval-test)
                sas.checkValue(quantity, true, false, false, false,  null, NLS.get("OfferItem.quantity"));
                sas.checkValue(singlePrice, true, false, false, false,  null, NLS.get("OfferItem.singlePrice"));
                sas.checkValue(discount, true, false, false, true,  Amount.of(100), NLS.get("OfferItem.discount"));
                sas.checkValue(cyclicPrice, true, false, false, false,  null, NLS.get("OfferItem.cyclicPrice"));

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

                // the salesConfirmationDate is set, when the mail with the salesConfirmation is send to the receiver
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

        // generate the position-number
        if (position == null) {
            OfferItem offerItem  = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
                                      .orderDesc(OfferItem.POSITION).queryFirst();
            int pos = 0;
            if(offerItem != null) {
               pos = offerItem.getPosition();
            }
            pos = pos + 10;
            int  divisionsrest = pos % 10;
            position =  pos - divisionsrest;
        }
        if(!(isSum() || isInfoText()) ) {
            // build the history string, organize last come --> first serve
            // offerItems withe the same md5-key are saved only one time in the history
            String md5 =  buildMD5(this);
            if(Strings.isFilled(history)) {
                // adapt the history-format
                history = history.replace("&nbsp;", " ");
                history = history.replace("<br>", "\n");
                // check the md5-key
                int posMd5 = history.indexOf("md5: ") + 5;
                if(posMd5 >= 5) {
                    String md5SubStr = history.substring(posMd5, posMd5 + 32);
                    if (md5SubStr.equals(md5)) {
                        return;    // md5-key is the same --> no change done
                    }
                }
            }
            String stateFix = getStateFix(14);
            String username = userInfo.getUserName();
            String s = NLS.toUserString(LocalDateTime.now()) + "   " + username + ":  " + getStateFix(14) + ", md5: " + buildMD5(this);
            if (history == null) {
                history = "";
            }
            if (Strings.isFilled(s)) {
                s = s + "\n" + history;
                history = s;
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
            s = s+o.getPriceBase() + NLS.toUserString(o.getSinglePrice()) + NLS.toUserString(o.getQuantity()) + o.getAccountingUnitComplete() +
            o.getOffer().getValue().getUniqueName(); // getUnqiueObjectName();
            if(getDiscountPresent()) {
                s = s + NLS.toUserString(discount.toString());
            }
        }
        return sas.buildMd5HexString(s);
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

    public boolean getDiscountPresent() {
        if(discount != null && discount.isPositive()) {
            return true ;
        }  else {
            return false;
        }
    }

    public boolean isService() {
        return isIs(OfferItemType.SERVICE, true);
    }

    public boolean isLicense() {
        return isIs(OfferItemType.LICENSE, false);
    }

    public boolean isInfoText() {
        return isIs(OfferItemType.INFOTEXT, false);
    }

    public boolean isSum() {
        return isIs(OfferItemType.SUM, false);
    }

    private boolean isIs(OfferItemType type, boolean returnValueIfNull) {
        if(type == null) {  // in the older versions the paketDefinition may be == null
            return returnValueIfNull;
        }
        if(type.equals(offerItemType)) {
            return true;
        }
        return false;
    }

    public boolean isContractStartDatePresent() {
        return contractStartDate != null;
    }

    public boolean isPackageDefinitionSinglePricePresent() {
        if(packageDefinition.getValue().getSinglePrice() != null && packageDefinition.getValue().getSinglePrice().isPositive() )  {
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

    /**
     * Replaces new line with <br>
     * tags
     */
    private static String nl2br(String content) {
        if (content == null) {
            return null;
        }
        return content.replace("\n", " <br /> ");
    }

    /**
     * Escapes the given string for use in XML or HTML.
     */
    private static String escapeXML(Object aText) {
        if (Strings.isEmpty(aText)) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText.toString());
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                // the char is not a special one
                // add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    // ToDo allgemeine Lösung als Makro machen
    public static String transformToHtml(String string) {
        string = escapeXML(string);
        string = nl2br(string);
        return string;
    }

    // ToDo allgemeine Lösung als Makro machen
    public static boolean notEmpty(String string) {
        if(string == null) {return false;}
        if(Strings.isFilled(string)) {return true;}
        return false;
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

    public Amount getSinglePrice() {
        return singlePrice;
    }

    public String getAccountingUnitComplete() {
        return accountingUnitComplete;
    }

    public void setAccountingUnitComplete(String accountingUnitComplete) {
        this.accountingUnitComplete = accountingUnitComplete;
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

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Amount getSinglePriceComplete() {
        return singlePriceComplete;
    }

    public void setSinglePriceComplete(Amount singlePriceComplete) {
        this.singlePriceComplete = singlePriceComplete;
    }

    public Amount getCyclicPriceComplete() {
        return cyclicPriceComplete;
    }

    public void setCyclicPriceComplete(Amount cyclicPriceComplete) {
        this.cyclicPriceComplete = cyclicPriceComplete;
    }
}
