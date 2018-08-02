/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.colors;

import sirius.db.jdbc.OMA;
import sirius.db.jdbc.SQLEntityRef;
import sirius.kernel.cache.Cache;
import sirius.kernel.cache.CacheManager;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Parts;
import sirius.kernel.di.std.Register;
import sirius.web.security.UserContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Register(classes = Colors.class)
public class Colors {

    public static final String FALLBACK_COLOR = "#cccccc";
    private final Cache<String, String> colorCache = CacheManager.createCache("colors");
    private final Cache<Tuple<String, String>, String> colorAssignmentCache =
            CacheManager.createCache("color-assignments");

    @Part
    private OMA oma;

    @Parts(ColorTypeProvider.class)
    private Collection<ColorTypeProvider> colorTypeProviders;

    @Part
    private GlobalContext context;

    public void flushColorDefinition(String id) {
        colorCache.remove(id);
    }

    public void flushColorAssignment(String tenantId, String type) {
        colorAssignmentCache.remove(Tuple.create(tenantId, type));
    }

    public List<Tuple<String, String>> getColorTypes() {
        return colorTypeProviders.stream()
                                 .sorted(Comparator.comparing(ColorTypeProvider::getLabel))
                                 .map(provider -> Tuple.create(provider.getName(), provider.getLabel()))
                                 .collect(Collectors.toList());
    }

    public String getLabel(String colorType) {
        return context.findPart(colorType, ColorTypeProvider.class).getLabel();
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

    public Optional<String> getColor(SQLEntityRef<ColorDefinition> definition) {
        if (definition.isFilled()) {
            return Optional.of(colorCache.get(String.valueOf(definition.getId()), this::loadColorDefinition));
        }

        return Optional.empty();
    }

    private String loadColorDefinition(String idAsString) {
        return oma.find(ColorDefinition.class, idAsString).map(ColorDefinition::getHexCode).orElse(FALLBACK_COLOR);
    }

    public List<ColorDefinition> getColorDefinitions() {
        return oma.select(ColorDefinition.class)
                  .eq(ColorDefinition.TENANT, Long.parseLong(UserContext.getCurrentUser().getTenantId()))
                  .orderAsc(ColorDefinition.NAME)
                  .queryList();
    }
}
