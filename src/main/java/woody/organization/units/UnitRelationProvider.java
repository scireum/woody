/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.kernel.di.std.Register;
import woody.core.relations.RelationProvider;
import woody.organization.BasicElement;
import woody.organization.BasicRelationProvider;
import woody.organization.BasicType;

/**
 * Created by aha on 11.01.17.
 */
@Register(classes = RelationProvider.class)
public class UnitRelationProvider extends BasicRelationProvider {

    @Override
    protected Class<? extends BasicElement<?>> getType() {
        return Unit.class;
    }

    @Override
    protected String getURLPrefix() {
        return "/unit";
    }

    @Override
    protected Class<? extends BasicType> getMetaType() {
        return UnitType.class;
    }

    @Override
    protected String getCategoryTypeName() {
        return UnitCategoryTypeProvider.TYPE_NAME;
    }
}
