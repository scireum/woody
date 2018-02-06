/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.colors.ColorTypeProvider;

import javax.annotation.Nonnull;

@Register
public class UnitColorTypeProvider implements ColorTypeProvider {

    public static final String TYPE = "UNIT";

    @Override
    public String getLabel() {
        return NLS.get("Unit.plural");
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE;
    }
}
