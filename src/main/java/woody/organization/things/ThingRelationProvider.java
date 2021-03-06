/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.things;

import sirius.kernel.di.std.Register;
import woody.core.relations.RelationProvider;
import woody.organization.BasicElement;
import woody.organization.BasicRelationProvider;
import woody.organization.BasicType;

/**
 * Created by aha on 11.01.17.
 */
@Register(classes = RelationProvider.class)
public class ThingRelationProvider extends BasicRelationProvider {

    @Override
    protected Class<? extends BasicElement<?>> getType() {
        return Thing.class;
    }

    @Override
    protected String getURLPrefix() {
        return "/thing";
    }

    @Override
    protected Class<? extends BasicType> getMetaType() {
        return ThingType.class;
    }

    @Override
    protected String getCategoryTypeName() {
        return ThingCategoryTypeProvider.TYPE_NAME;
    }
}
