/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.db.mixing.OMA;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import woody.organization.categories.Category;
import woody.organization.categories.CategoryTypeProvider;

import javax.annotation.Nonnull;

@Register
public class UnitCategoryTypeProvider implements CategoryTypeProvider {

    public static final String TYPE_NAME = "unit";

    @Part
    private OMA oma;

    @Override
    public String getLabel() {
        return NLS.get("Unit.plural");
    }

    @Override
    public void beforeDelete(Category category) {
        // All constraints are enforced on the database layer.
    }

    @Override
    public String makeUrl(String technicalName) {
        return "/units/" + technicalName;
    }

    @Override
    public String getOverviewTemplate() {
        return "templates/organization/units/relateable-units-overview.html.pasta";
    }

    @Nonnull
    @Override
    public String getName() {
        return TYPE_NAME;
    }
}
