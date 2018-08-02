/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.quotes;

import sirius.biz.jdbc.model.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import woody.sales.accounting.ItemRefData;

public class OfferItem extends BizEntity {

    public static final Mapping OFFER = Mapping.named("offer");
    private final SQLEntityRef<Offer> offer = SQLEntityRef.on(Offer.class, SQLEntityRef.OnDelete.CASCADE);

    public static final Mapping POSITION = Mapping.named("position");
    @Autoloaded
    private int position;

    public static final Mapping OFFERITEMTYPE = Mapping.named("offerItemType");
    @Autoloaded
    private OfferItemType offerItemType = OfferItemType.SERVICE;

//    @Autoloaded
//    @NullAllowed
//    private final EntityRef<PackageDefinition> packageDefinition = EntityRef.on(PackageDefinition.class, EntityRef.OnDelete.CASCADE);
//    public static final Column PACKAGEDEFINITION = Column.named("packageDefinition");

    public static final Mapping SUMMARY = Mapping.named("summary");
    @Autoloaded
    @Length(255)
    private String summary;

    public static final Mapping DESCRIPTION = Mapping.named("description");
    @Autoloaded
    @Length(1000)
    private String description;

    public static final Mapping ITEM = Mapping.named("item");
    private final ItemRefData item = new ItemRefData();

    @Autoloaded
    private OfferItemState state;
    public static final Mapping STATE = Mapping.named("state");

    @BeforeSave
    protected void onSave() {
//        // check te Role of the user
//        UserInfo userInfo = UserContext.getCurrentUser();
//        userInfo.assertPermission("offers");
//
//        Offer offer = getOffer().getValue();
//        Company company = offer.getCompany().getValue();
//
//        // ToDo alle Exceptions entweder auslagern oder als Warning machen. (CRM-34)
//
//        if (isInfoText()) {
//            keyword = "";
//            state = OfferItemState.UNUSED;
//            priceBase = "nicht benutzt";
//            accountingUnitComplete = "";
//            if (!quantity.isZeroOrNull()) {
//                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoQuantity")
//                                .set("angebot", offer.getNumber()).set("pos", position).handle();
//            }
//            if (!singlePrice.isZeroOrNull()) {
//                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoPrice")
//                                .set("angebot", offer.getNumber()).set("pos", position).handle();
//            }
//            if (acceptanceDate != null) {
//                throw Exceptions.createHandled().withNLSKey("OfferItem.infoTextNoAcceptanceDate")
//                                .set("angebot", offer.getNumber()).set("pos", position).handle();
//            }
//
//        } else {
//            if(isSum()) {
//                keyword = "";
//                state = OfferItemState.UNUSED;
//                priceBase = "nicht benutzt";
//                accountingUnitComplete = "";
//                if(Strings.isEmpty(text)) {
//                    text = "Zwischensummen:";
//                }
//            } else {
//                // check the presence of the packageDefinition
//                if (packageDefinition == null) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.packageDefinitionMissing")
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//
//                // check the packageDefinition
//                Product product = this.getPackageDefinition().getValue().getProduct().getValue();
//                PackageDefinition pd = oma.select(PackageDefinition.class)
//                                          .eq(PackageDefinition.PRODUCT, product)
//                                          .eq(PackageDefinition.NAME, packageDefinition.getValue().getName())
//                                          .queryFirst();
//                if (pd == null) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.pdWrong")
//                                    .set("prod", packageDefinition.getValue().getProduct().getValue().getName())
//                                    .set("pd", packageDefinition.getValue().getName())
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//
//                if (packageDefinition.getId() != pd.getId()) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.idWrong")
//                                    .set("pd1", packageDefinition.getValue().getName())
//                                    .set("pd2", pd.getName())
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//
//                // check the offerItemType
//                if(OfferItemType.SERVICE.equals(this.getOfferItemType()) &&
//                   ProductType.LICENSE.equals(this.getPackageDefinition().getValue().getProduct().getValue().getProductType()) ||
//                   OfferItemType.LICENSE.equals(this.getOfferItemType()) &&
//                   ProductType.SERVICE.equals(this.getPackageDefinition().getValue().getProduct().getValue().getProductType())) {
//                   throw Exceptions.createHandled().withNLSKey("OfferItem.typesWrong")
//                            .set("oiType", this.getOfferItemType().toString())
//                            .set("productType", this.getPackageDefinition().getValue().getProduct().getValue().toString())
//                            .handle();
//                }
//
//                //set the accountingUnitComplete
//                String accUnit = this.getPackageDefinition().getValue().getAccountingUnit();
//                Tuple<String, String> tuple = cls.getValues("accountingUnit", accUnit);
//                this.setAccountingUnitComplete(tuple.getFirst());
//
//                // check the accountingUnit
//                if (Strings.isEmpty(accUnit)) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.accountingUnitMissing")
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//
//               if (isLicense()) {
//                    // set keyword and text with default-values from the packageDefinition  (CRM-64)
//                    if(this.getKeyword()== null ) {
//                        this.setKeyword(this.getPackageDefinition().getValue().getName());
//                    }
//                    if(this.getText() == null) {
//                        if(this.getPackageDefinition().getValue().getDescription() != null) {
//                            this.setText(this.getPackageDefinition().getValue().getDescription());
//                        }
//                    }
//                }
//
//                // check the presence of the keyword
//                if(Strings.isEmpty(this.getKeyword())) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.keywordMissing")
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//                // check the presence of the position-text
//                if(Strings.isEmpty(this.getText())) {
//                    throw Exceptions.createHandled().withNLSKey("OfferItem.textMissing")
//                                    .set("angebot", offer.getNumber()).set("pos", position).handle();
//                }
//
//                // check the quantity
//                if (quantity.isZeroOrNull()) {
//                      quantity = Amount.of(1);
//                }
//
//                // check the acceptanceDate
//                if (OfferItemState.OFFER.equals(state)) {
//                    if (acceptanceDate != null) {
//                        throw Exceptions.createHandled().withNLSKey("OfferItem.noAcceptanceDate")
//                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
//                    }
//                }
//
//                // check the singlePrice
//                if (isService()) {
//                    if (singlePrice.isZeroOrNull()) {
//                        //  is a price for this company present? -> take the company-price
////                        CompanyAccountingData companyAccountingData = company.getCompanyAccountingData();
////                        if(companyAccountingData != null && companyAccountingData.getPtPrice() != null) {
////                                singlePrice = companyAccountingData.getPtPrice();
////                                priceBase = "Firma";
////                        } else {
////                                // is a Package-price present? --> take the package-price
////                                singlePrice = packageDefinition.getValue().getUnitPrice();
////                                priceBase = "Paket";
////                        }
//                    }
//                    // check the singlePrice of a service (e.g. 800 EUR/day)
//                    if (singlePrice.isZeroOrNull()) {
//                        throw Exceptions.createHandled().withNLSKey("OfferItem.singlePriceMissing")
//                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
//                    }
//                }
//                if (isLicense()) {
//                    // check the cyclicPrice
//                    if(cyclicPrice.equals(Amount.NOTHING) ) {
//                        cyclicPrice = packageDefinition.getValue().getUnitPrice();
//                    }
//                    priceBase = "Paket";
//                    if((cyclicPrice.compareTo(packageDefinition.getValue().getUnitPrice()) != 0)) {
//                        priceBase = "Angebot";
//                    }
//                    if (cyclicPrice.equals(Amount.NOTHING)) {
//                        throw Exceptions.createHandled().withNLSKey("OfferItem.cyclicPriceMissing")
//                                        .set("angebot", offer.getNumber()).set("pos", position).handle();
//                    }
//                    // check the singlePrice
//                    if(singlePrice.equals(Amount.NOTHING)) {
//                        if (isPackageDefinitionSinglePricePresent()) {
//                            singlePrice = packageDefinition.getValue().getSinglePrice();
//                        }
//                    }
//                }
//
//                // check the values (interval-test)
//                sas.checkValue(quantity, true, false, false, false,  null, NLS.get("OfferItem.quantity"));
//                sas.checkValue(singlePrice, true, false, false, false,  null, NLS.get("OfferItem.singlePrice"));
//                sas.checkValue(discount, true, false, false, true,  Amount.of(100), NLS.get("OfferItem.discount"));
//                sas.checkValue(cyclicPrice, true, false, false, false,  null, NLS.get("OfferItem.cyclicPrice"));
//
//                // store the dates
//                if (OfferItemState.OFFER.equals(state)) {
//                    if (offerDate == null) {
//                        offerDate = LocalDate.now();
//                    }
//                }
//                if (OfferItemState.ORDERED.equals(state)) {
//                    if (orderDate == null) {
//                        orderDate = LocalDate.now();
//                    }
//
//                    // check the contractStartDate for licenses.
//                    // If the date == null create the contractStartDate as the first day of the next month as default
//
//                    if(this.isLicense()) {
//                        if(this.getContractStartDate() == null) {
//
//                            int year = orderDate.getYear();
//                            int month = orderDate.getMonthValue() + 1;
//                            if(month > 12) {
//                                month = 1;
//                                year = year + 1;
//                            }
//                            LocalDate contractStartDate = LocalDate.of(year, month, 1);
//                            this.setContractStartDate(contractStartDate);
//                        }
//                    }
//                }
//
//                // the salesConfirmationDate is set, when the mail with the salesConfirmation is send to the receiver
//
//                if (OfferItemState.DEVELOPED.equals(state)) {
//                    if (completionDate == null) {
//                        completionDate = LocalDate.now();
//                    }
//                }
//                if (OfferItemState.ACCEPTED.equals(state)) {
//                    if (acceptanceDate == null) {
//                        acceptanceDate = LocalDate.now();
//                    }
//                }
//                if (OfferItemState.ACCOUNTED.equals(state)) {
//                    if (accountingDate == null) {
//                        accountingDate = LocalDate.now();
//                    }
//                }
//            }
//        }
//
//        // generate the position-number
//        if (position == null) {
//            OfferItem offerItem  = oma.select(OfferItem.class).eq(OfferItem.OFFER, offer)
//                                      .orderDesc(OfferItem.POSITION).queryFirst();
//            int pos = 0;
//            if(offerItem != null) {
//               pos = offerItem.getPosition();
//            }
//            pos = pos + 10;
//            int  divisionsrest = pos % 10;
//            position =  pos - divisionsrest;
//        }
//
//        if(!(isSum() || isInfoText()) ) {
//            // build the history string, organize last come --> first serve
//            // offerItems withe the same md5-key are saved only one time in the history
//            String md5 =  buildMD5(this);
//            if(Strings.isFilled(history)) {
//                // adapt the history-format
//                history = history.replace("&nbsp;", " ");
//                history = history.replace("<br>", "\n");
//                // check the md5-key
//                int posMd5 = history.indexOf("md5: ") + 5;
//                if(posMd5 >= 5) {
//                    String md5SubStr = history.substring(posMd5, posMd5 + 32);
//                    if (md5SubStr.equals(md5)) {
//                        return;    // md5-key is the same --> no change done
//                    }
//                }
//            }
//            String stateFix = getStateFix(14);
//            String username = userInfo.getUserName();
//            String s = NLS.toUserString(LocalDateTime.now()) + "   " + username + ":  " + getStateFix(14) + ", md5: " + buildMD5(this);
//            if (history == null) {
//                history = "";
//            }
//            if (Strings.isFilled(s)) {
//                s = s + "\n" + history;
//                history = s;
//            }
//        }
    }
//
//    public SQLEntityRef<Offer> getOffer() {
//        return offer;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public OfferItemType getOfferItemType() {
//        return offerItemType;
//    }
//
//    public void setOfferItemType(OfferItemType offerItemType) {
//        this.offerItemType = offerItemType;
//    }
//
//    public String getSummary() {
//        return summary;
//    }
//
//    public void setSummary(String summary) {
//        this.summary = summary;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Amount getQuantity() {
//        return quantity;
//    }
//
//    public void setQuantity(Amount quantity) {
//        this.quantity = quantity;
//    }
//
//    public Amount getOneOffPrice() {
//        return oneOffPrice;
//    }
//
//    public void setOneOffPrice(Amount oneOffPrice) {
//        this.oneOffPrice = oneOffPrice;
//    }
//
//    public Amount getAbsoluteDiscount() {
//        return absoluteDiscount;
//    }
//
//    public void setAbsoluteDiscount(Amount absoluteDiscount) {
//        this.absoluteDiscount = absoluteDiscount;
//    }
//
//    public Amount getPercentageDiscount() {
//        return percentageDiscount;
//    }
//
//    public void setPercentageDiscount(Amount percentageDiscount) {
//        this.percentageDiscount = percentageDiscount;
//    }
//
//    public Amount getReoccuringPrice() {
//        return reoccuringPrice;
//    }
//
//    public void setReoccuringPrice(Amount reoccuringPrice) {
//        this.reoccuringPrice = reoccuringPrice;
//    }
//
//    public Amount getReoccuringAbsoluteDiscount() {
//        return reoccuringAbsoluteDiscount;
//    }
//
//    public void setReoccuringAbsoluteDiscount(Amount reoccuringAbsoluteDiscount) {
//        this.reoccuringAbsoluteDiscount = reoccuringAbsoluteDiscount;
//    }
//
//    public Amount getReoccuringPercentageDiscount() {
//        return reoccuringPercentageDiscount;
//    }
//
//    public void setReoccuringPercentageDiscount(Amount reoccuringPercentageDiscount) {
//        this.reoccuringPercentageDiscount = reoccuringPercentageDiscount;
//    }
//
//    public OfferItemState getState() {
//        return state;
//    }
//
//    public void setState(OfferItemState state) {
//        this.state = state;
//    }
}
