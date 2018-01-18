/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import sirius.biz.tenants.Tenants;
import sirius.db.mixing.OMA;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.ComparableTuple;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

/**
 * Created by aha on 25.01.17.
 */
@Register(classes = RelationHelper.class)
public class RelationHelper {

    public static final String TYPE_NOT_RELATION = "notrelation";
    public static final String TYPE_RELATION = "relation";
    public static final String CSS_NOT_RELATION = "suggestion-notrelation";
    public static final String CSS_RELATION = "suggestion-relation";

    @Part
    private OMA oma;

    @Part
    private Tenants tenants;

    @Part
    private GlobalContext context;

    private static final ComparableTuple<String, String> EMPTY = ComparableTuple.create("-", null);

    private final Cache<String, ComparableTuple<String, String>> targetNameCache =
            CacheManager.createCache("relation-targets");

    public String getTargetName(Relation relation) {
        return getTargetNameAndUri(relation).getFirst();
    }

    public ComparableTuple<String, String> getTargetNameAndUri(Relation relation) {
        return targetNameCache.get(relation.getTarget(), this::computeTargetNameAndUri);
    }

    private ComparableTuple<String, String> computeTargetNameAndUri(String targetName) {
        try {
            if (Strings.isEmpty(targetName)) {
                return EMPTY;
            }

            Tuple<String, String> typeAndName = Strings.split(targetName, "-");
            return context.findPart(typeAndName.getFirst(), RelationProvider.class)
                          .resolveNameAndUri(targetName)
                          .orElse(EMPTY);
        } catch (Exception e) {
            Exceptions.handle(e);
            return EMPTY;
        }
    }
}
