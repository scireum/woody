/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core;

import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.security.UserContext;
import sirius.web.templates.GlobalContextExtender;
import woody.core.colors.Colors;
import woody.organization.OrganizationHelper;

import java.util.function.BiConsumer;

@Register
public class WoodyContextExtender implements GlobalContextExtender {

    @Part
    private Colors colors;

    @Override
    public void collectTemplate(BiConsumer<String, Object> biConsumer) {
        biConsumer.accept("organization", UserContext.getHelper(OrganizationHelper.class));
        biConsumer.accept("colors", colors);
    }

    @Override
    public void collectScripting(BiConsumer<String, Object> biConsumer) {

    }
}
