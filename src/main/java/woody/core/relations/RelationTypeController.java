/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Schema;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Facet;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import woody.core.colors.ColorData;
import woody.core.colors.ColorDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by aha on 11.01.17.
 */
@Register(classes = {Controller.class, RelationTypeController.class})
public class RelationTypeController extends BizController {

    private static final String PERMISSION_MANAGE_RELATION_TYPES = "permission-manage-relation-types";

    @Part
    private Schema schema;

    @Part
    private GlobalContext context;

    private Map<String, String> typeMap;
    private List<String> sourceTypes;
    private List<String> targetTypes;

    public void flushTypeCache() {
        sourceTypes = null;
        targetTypes = null;
        typeMap = null;
    }

    public List<String> getSourceTypes() {
        if (sourceTypes == null) {
            initializeTypes();
        }

        return Collections.unmodifiableList(sourceTypes);
    }

    public List<String> getTargetTypes() {
        if (targetTypes == null) {
            initializeTypes();
        }

        return Collections.unmodifiableList(targetTypes);
    }

    public String translateType(String type) {
        if (type == null) {
            return "";
        }

        if (typeMap == null) {
            initializeTypes();
        }

        return typeMap.get(type);
    }

    protected synchronized void initializeTypes() {
        typeMap = Maps.newHashMap();
        sourceTypes = Lists.newArrayList();
        targetTypes = Lists.newArrayList();

        for (RelationProvider provider : context.getParts(RelationProvider.class)) {
            for (Tuple<String, String> subType : provider.getSourceTypes()) {
                sourceTypes.add(subType.getFirst());
                typeMap.put(subType.getFirst(), subType.getSecond());
            }
            for (Tuple<String, String> subType : provider.getTargetTypes()) {
                targetTypes.add(subType.getFirst());
                typeMap.put(subType.getFirst(), subType.getSecond());
            }
        }

        sourceTypes.sort(String::compareTo);
        targetTypes.sort(String::compareTo);
    }

    @DefaultRoute
    @LoginRequired
    @Permission(PERMISSION_MANAGE_RELATION_TYPES)
    @Routed("/relations/types")
    public void relationTypes(WebContext ctx) {
        PageHelper<RelationType> ph = PageHelper.withQuery(oma.select(RelationType.class)
                                                              .fields(RelationType.ID,
                                                                      RelationType.NAME,
                                                                      RelationType.SOURCE_TYPE,
                                                                      RelationType.TARGET_TYPE,
                                                                      RelationType.VIEW_IN_LIST,
                                                                      RelationType.MULTIPLE,
                                                                      RelationType.LIST_REVERSE,
                                                                      RelationType.COLOR.inner(ColorData.COLOR)
                                                                                        .join(ColorDefinition.HEX_CODE))
                                                              .orderAsc(RelationType.SOURCE_TYPE)
                                                              .orderAsc(RelationType.TARGET_TYPE)
                                                              .orderAsc(RelationType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(RelationType.NAME).forCurrentTenant();
        Facet sourceTypeFilter = new Facet(NLS.get("RelationType.sourceType"),
                                           RelationType.SOURCE_TYPE.getName(),
                                           ctx.get(RelationType.SOURCE_TYPE.getName()).asString(),
                                           null);
        for (String type : getSourceTypes()) {
            sourceTypeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(sourceTypeFilter);
        Facet targetTypeFilter = new Facet(NLS.get("RelationType.targetType"),
                                           RelationType.TARGET_TYPE.getName(),
                                           ctx.get(RelationType.TARGET_TYPE.getName()).asString(),
                                           null);
        for (String type : getTargetTypes()) {
            targetTypeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(targetTypeFilter);
        ctx.respondWith().template("/templates/core/relations/types.html.pasta", ph.asPage(), this);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_RELATION_TYPES)
    @Routed("/relations/type/:1/delete")
    public void deleteRelationType(WebContext ctx, String id) {
        Optional<RelationType> relationType = tryFindForTenant(RelationType.class, id);
        if (relationType.isPresent()) {
            oma.delete(relationType.get());
            showDeletedMessage();
        }

        ctx.respondWith().redirectToGet("/relations/types");
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_RELATION_TYPES)
    @Routed("/relations/type/:1")
    public void relationType(WebContext ctx, String typeId) {
        RelationType type = findForTenant(RelationType.class, typeId);

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(wasNew -> {
            if (wasNew) {
                type.setSourceType(ctx.get(RelationType.SOURCE_TYPE.getName()).asString());
                type.setTargetType(ctx.get(RelationType.TARGET_TYPE.getName()).asString());
            }
        }).withAfterSaveURI("/relations/types").saveEntity(type);

        if (!requestHandled) {
            validate(type);
            ctx.respondWith().template("/templates/core/relations/type.html.pasta", type, this);
        }
    }
}
