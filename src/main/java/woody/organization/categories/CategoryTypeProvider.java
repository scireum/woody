/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.categories;

import sirius.kernel.di.std.Named;

public interface CategoryTypeProvider extends Named {

    String getLabel();

    void beforeDelete(Category category);

    String makeUrl(String technicalName);

    String getOverviewTemplate();

    String getColorType();
}
