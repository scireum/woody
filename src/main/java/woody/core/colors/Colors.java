/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.db.mixing.OMA;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.security.UserContext;

import java.util.List;

@Register(classes = Colors.class)
public class Colors {

    public static final String FALLBACK_COLOR = "#cccccc";
    private final Cache<String, String> colorCache = CacheManager.createCache("colors");
    private final Cache<Tuple<String, String>, String> colorAssignmentCache =
            CacheManager.createCache("color-assignments");

    @Part
    private OMA oma;

    public void flushColorDefinition(String id) {
        colorCache.remove(id);
    }

    public void flushColorAssignment(String tenantId, String id) {
        colorAssignmentCache.remove(Tuple.create(tenantId, id));
    }

    public List<Tuple<String, String>> getColorTypes() {
        return null;
    }

    public String getLabel(String colorType) {
        return null;
    }

    public String getColorForType(String colorType) {
        String colorDefinitionId =
                colorAssignmentCache.get(Tuple.create(UserContext.getCurrentUser().getTenantId(), colorType),
                                         this::loadColorAssignment);
        if (Strings.isEmpty(colorDefinitionId)) {
            if (Strings.areEqual(colorType, DefaultColorTypeProvider.TYPE)) {
                return FALLBACK_COLOR;
            } else {
                return getColorForType(DefaultColorTypeProvider.TYPE);
            }
        }

        return colorCache.get(colorDefinitionId, this::loadColorDefinition);
    }

    private String loadColorAssignment(Tuple<String, String> providerAndType) {
        TypeColorAssignment assignment = oma.select(TypeColorAssignment.class)
                                            .fields(TypeColorAssignment.COLOR.join(ColorDefinition.ID))
                                            .eq(TypeColorAssignment.TENANT, Long.parseLong(providerAndType.getFirst()))
                                            .eq(TypeColorAssignment.TYPE, providerAndType.getSecond())
                                            .queryFirst();

        if (assignment == null || assignment.getColor().getId() == null) {
            return "";
        }

        return String.valueOf(assignment.getColor().getId());
    }

    public String getColor(ColorDefinition... colorDefinitions) {
        for (ColorDefinition definition : colorDefinitions) {
            if (definition != null) {
               return colorCache.get(definition.getIdAsString(), this::loadColorDefinition);
            }
        }

        return getColorForType(DefaultColorTypeProvider.TYPE);
    }

    private String loadColorDefinition(String idAsString) {
        return oma.find(ColorDefinition.class, idAsString).map(ColorDefinition::getHexCode).orElse(FALLBACK_COLOR);
    }
}
