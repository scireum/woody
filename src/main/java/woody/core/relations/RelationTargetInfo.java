/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.db.mixing.Column;
import sirius.db.mixing.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by aha on 12.01.17.
 */
public interface RelationTargetInfo<E extends Entity> {

    @Nonnull
    List<Column> getFetchColumns();

    @Nonnull
    String transformToString(@Nonnull E entity);

    @Nullable
    String transformToUrl(@Nonnull E entity);

}
