/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

/**
 * Created by gerhardhaufler on 09.02.16.
 */

import sirius.kernel.nls.NLS;

/**
     * <pre>
     *
     * singlePriceState for rival contracts:
     * NO_SINGLEPRICE: no singlePrice is present, default for all non-singlePrice-Products
     *                 for all other states: a singlePrice is present and ...
     *                 --> no accounting of the singlePrice
     *
     * ACCOUNT_NOW:    no contracts with state ACCOUNT_NOW is present,
     *                 no contracts with state THIS_ACCOUNT is present,
     *                 no contract with state OLD_ACCOUNT is present
     *                 singlePrice is present, accountedTo = null
     *                 --> the singlePrice is accounted with this job,
     *                     after accounting the status is set to 'THIS_ACCOUNT'
     * THIS_ACCOUNT:   no contracts with state ACCOUNT_NOW present,
     *                 no contracts with state THIS_ACCOUNT present,
     *                 accountedTo != null.
     *                 --> the singelePrice was accounted in the past at this contract.
     * OLD_ACCOUNT:    there have to be other contracts and
     *                 only one of these have the state
     *                 THIS_ACCOUNT or ACCOUNT_NOW or NO_ACCOUNTING.
     *                 --> the singlePrice is not accounted
     * OPEN:           state is open
     *                 --> the singlePrice is not accounted.
     *
     * NO_ACCOUNTING: no Accounting of the singlePrice
     *                this is a special form of a discount
     *                 --> the singlePrice is not accounted.
     * ==========================================================
     *  singlePriceState for volume contracts:
     * NO_SINGLEPRICE: no singlePrice is present, default for all non-singlePrice-Products
     *                 for all other states: a singlePrice is present and ...
     *                 --> no accounting of the singlePrice
     *
     * ACCOUNT_NOW:    singlePrice is present, accountedTo = null
     *                 --> the singlePrice is accounted with this job,
     *                     after accounting the status is set to 'THIS_ACCOUNT'
     * THIS_ACCOUNT:   --> the singelePrice was accounted in the past at this contract.
     * NO_ACCOUNTING: no Accounting of the singlePrice
     *                this is a special form of a discount
     *                 --> the singlePrice is not accounted.
     *
     * </pre>
     */

    public enum ContractSinglePriceType {
        NO_SINGLEPRICE, // kein Einmalpreis ist vorhanden
        OPEN, // offen, noch nichts eingetragen
        ACCOUNT_NOW, // jetzt abrechnen
        THIS_ACCOUNT, // abgerechnet mit diesem Vertrag
        OLD_ACCOUNT, // mit altem (anderen) Vertrag abgerechnet
        NO_ACCOUNTING; // Einmalpreis vorhanden, aber keine Abrechnung des
        // Einmalpreises (spezielle Form eines Rabatts).

        @Override
        public String toString() {
            return NLS.get(ContractSinglePriceType.class.getSimpleName() + "." + name());
        }


    }
