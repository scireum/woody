/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.core.colors.ColorTypeProvider;

import javax.annotation.Nonnull;

@Register
public class TagQueryTagColorTypeProvider implements ColorTypeProvider {

    public static final String TYPE = "TAG-QUERY-TAG";

    @Override
    public String getLabel() {
        return NLS.get("TagQueryTagColorTypeProvider.label");
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE;
    }
}
