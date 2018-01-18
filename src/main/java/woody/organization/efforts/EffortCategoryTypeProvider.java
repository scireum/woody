/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.efforts;

import sirius.db.mixing.OMA;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.organization.categories.Category;
import woody.organization.categories.CategoryTypeProvider;

import javax.annotation.Nonnull;

@Register
public  class EffortCategoryTypeProvider implements CategoryTypeProvider {

    public static final String TYPE_NAME = "effort";

    @Part
    private OMA oma;

    @Override
    public String getLabel() {
        return NLS.get("Model.effort");
    }

    @Override
    public void beforeDelete(Category category) {
        // All constraints are enforced on the database layer.
    }

    @Override
    public String makeUrl(String technicalName) {
        return "/efforts/" + technicalName;
    }

    @Override
    public String getOverviewTemplate() {
        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_NAME;
    }
}
