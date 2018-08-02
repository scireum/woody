/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.quotes;

import sirius.kernel.commons.Amount;
import woody.xrm.Company;

import java.time.LocalDate;

/**
 * Created by gerhardhaufler on 12.10.17.
 */
public class OfferInfo {

    public OfferInfo(Offer offer, OfferItem offerItem, Company company, Amount value, OfferItemState state, LocalDate date, Amount quantity, int nr) {
        this.offer = offer;
        this.offerItem = offerItem;
        this.company = company;
        this.value = value;
        this.state = state;
        this.date = date;
        this.quantity = quantity;
        this.nr = nr;
    }

    private int nr;
    private Offer offer;
    private OfferItem offerItem;
    private Company company;
    private Amount value = Amount.NOTHING;
    private OfferItemState state;
    private LocalDate date;
    private Amount quantity;

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public OfferItem getOfferItem() {
        return offerItem;
    }

    public void setOfferItem(OfferItem offerItem) {
        this.offerItem = offerItem;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Amount getValue() {
        return value;
    }

    public void setValue(Amount value) {
        this.value = value;
    }

    public OfferItemState getState() {
        return state;
    }

    public void setState(OfferItemState state) {
        this.state = state;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Amount getQuantity() {
        return quantity;
    }

    public void setQuantity(Amount quantity) {
        this.quantity = quantity;
    }
}
