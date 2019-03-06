/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core;

import sirius.kernel.commons.NumberFormat;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.security.UserContext;
import sirius.web.templates.GlobalContextExtender;
import woody.core.colors.Colors;
import woody.organization.OrganizationHelper;
import woody.xrm.XRMHelper;

import java.math.RoundingMode;
import java.util.function.BiConsumer;

@Register
public class WoodyContextExtender implements GlobalContextExtender {

    @Part
    private Colors colors;

    @ConfigValue("sales.currency.decimalPlaces")
    private int decimalPlaces;

    @ConfigValue("sales.currency.symbol")
    private String symbol;

    @ConfigValue("sales.currency.roundingMode")
    private RoundingMode roundingMode;

    private NumberFormat currency;
    private NumberFormat currencyWithoutSymbol;

    @Override
    public void collectTemplate(BiConsumer<String, Object> biConsumer) {
        biConsumer.accept("organization", UserContext.getHelper(OrganizationHelper.class));
        biConsumer.accept("xrm", UserContext.getHelper(XRMHelper.class));
        biConsumer.accept("colors", colors);
        biConsumer.accept("FORMAT_PERCENT", NumberFormat.PERCENT);
        biConsumer.accept("FORMAT_TWO_DECIMAL_PLACES", NumberFormat.TWO_DECIMAL_PLACES);
        biConsumer.accept("FORMAT_CURRENCY", getCurrencyFormat());
        biConsumer.accept("CURRENCY_WITHOUT_SYMBOL", getCurrencyFormatWithoutSymbol());
    }

    private NumberFormat getCurrencyFormatWithoutSymbol() {
        if (currencyWithoutSymbol == null) {
            currencyWithoutSymbol = new NumberFormat(decimalPlaces, roundingMode, null, true, null);
        }
        return currencyWithoutSymbol;
    }

    private NumberFormat getCurrencyFormat() {
        if (currency == null) {
            currency = new NumberFormat(decimalPlaces, roundingMode, null, true, symbol);
        }
        return currency;
    }

    @Override
    public void collectScripting(BiConsumer<String, Object> biConsumer) {

    }
}
