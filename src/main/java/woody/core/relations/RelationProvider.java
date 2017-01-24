/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Named;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by aha on 11.01.17.
 */
public interface RelationProvider extends Named {

    void computeSuggestions(String subType, String query, boolean forSearch, Consumer<Tuple<String, String>> consumer);

    Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName);

    List<Tuple<String, String>> getSourceTypes();

    List<Tuple<String, String>> getTargetTypes();
}