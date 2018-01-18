/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;

import javax.annotation.Nonnull;

@Register
public class DefaultColorTypeProvider implements ColorTypeProvider {

    public static final String TYPE = "DEFAULT";

    @Override
    public String getLabel() {
        return NLS.get("DefaultColorTypeProvider.label");
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE;
    }
}
