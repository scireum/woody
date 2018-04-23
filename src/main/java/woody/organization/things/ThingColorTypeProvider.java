/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.colors.ColorTypeProvider;

import javax.annotation.Nonnull;

@Register
public class ThingColorTypeProvider implements ColorTypeProvider {

    public static final String TYPE = "THING";

    @Override
    public String getLabel() {
        return NLS.get("Thing.plural");
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE;
    }
}
