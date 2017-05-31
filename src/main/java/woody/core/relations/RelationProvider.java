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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides relation targets and resolves <tt>unique object names</tt> into real names.
 * <p>
 * For each type of source- and target object used by a {@link Relation} a matching RelationProvider has to be
 * registered. As many types have subtypes, types may contain a "-" to separate the effective name used to lookup
 * the RelationProvider from subtype passed to the methods below.
 */
public interface RelationProvider extends Named {

    /**
     * Computes suggestions to generate possible target unique object names as a filter in a search.
     *
     * @param subType  if sub types are supported, this might contain a possible sub type.
     * @param query    the query string used to filter possible objects
     * @param consumer used to collect suggestions containing a name and unique object name. If the name ends with a
     *                 "*", it is expanded into a like constraint which can be used for hierarchical searches
     */
    void computeSearchSuggestions(@Nullable String subType,
                                  @Nonnull String query,
                                  @Nonnull Consumer<Tuple<String, String>> consumer);

    /**
     * Computes suggestions to generate possible targets for a new relation.
     *
     * @param subType  the sub type of targets to filter on
     * @param query    the query to filter possible objects
     * @param consumer used to collect suggestions containing a name and unique object name
     */
    void computeTargetSuggestions(@Nullable String subType,
                                  @Nonnull String query,
                                  @Nonnull Consumer<Tuple<String, String>> consumer);

    /**
     * Resolves a unique object name into a visible name and a uri to link to.
     *
     * @param uniqueObjectName the <tt>unique name</tt> to resolve
     * @return a tuple containing a name and an uri (which may be null)
     */
    Optional<ComparableTuple<String, String>> resolveNameAndUri(String uniqueObjectName);

    /**
     * Lists source types supported by this provider.
     *
     * @return a list of tuples containing a type and name
     */
    List<Tuple<String, String>> getSourceTypes();

    /**
     * Lists target types supported by this provider.
     *
     * @return a list of tuples containing a type and name
     */
    List<Tuple<String, String>> getTargetTypes();
}