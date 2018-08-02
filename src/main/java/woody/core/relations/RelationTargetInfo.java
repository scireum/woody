/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.db.jdbc.SQLEntity;
import sirius.db.mixing.Mapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by aha on 12.01.17.
 */
public interface RelationTargetInfo<E extends SQLEntity> {

    @Nonnull
    List<Mapping> getFetchColumns();

    @Nonnull
    String transformToString(@Nonnull E entity);

    @Nullable
    String transformToUrl(@Nonnull E entity);
}
