/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Entity;
import sirius.db.mixing.Mixable;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Or;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Context;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.web.controller.AutocompleteHelper;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Facet;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by aha on 11.01.17.
 */
@Register(classes = {Controller.class, RelationsController.class})
public class RelationsController extends BizController {

    private static final String PERMISSION_MANAGE_RELATION_TYPES = "permission-manage-relation-types";

    @Part
    private Schema schema;

    @Context
    private GlobalContext context;

    private Map<String, String> typeMap;
    private List<String> sourceTypes;
    private List<String> targetTypes;

    private static String relationsSecret;

    public static String computeAuthHash(String objectId) {
        if (relationsSecret == null) {
            relationsSecret = Strings.generateCode(32);
        }

        long unixTimeInDays = TimeUnit.DAYS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        return Hashing.md5()
                      .hashString(objectId + relationsSecret + String.valueOf(unixTimeInDays), Charsets.UTF_8)
                      .toString();
    }

    public void flushTypeCache() {
        sourceTypes = null;
        targetTypes = null;
        typeMap = null;
    }

    public List<String> getSourceTypes() {
        if (sourceTypes == null) {
            initializeTypes();
        }

        return sourceTypes;
    }

    public List<String> getTargetTypes() {
        if (targetTypes == null) {
            initializeTypes();
        }

        return targetTypes;
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
                                                              .orderAsc(RelationType.SOURCE_TYPE)
                                                              .orderAsc(RelationType.TARGET_TYPE)
                                                              .orderAsc(RelationType.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(RelationType.NAME, RelationType.REVERSE_NAME).forCurrentTenant();
        Facet sourceTypeFilter = new Facet(NLS.get("RelationType.sourceType"),
                                           RelationType.SOURCE_TYPE.getName(),
                                           ctx.get(RelationType.SOURCE_TYPE.getName()).asString(null),
                                           null);
        for (String type : getSourceTypes()) {
            sourceTypeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(sourceTypeFilter);
        Facet targetTypeFilter = new Facet(NLS.get("RelationType.targetType"),
                                           RelationType.TARGET_TYPE.getName(),
                                           ctx.get(RelationType.TARGET_TYPE.getName()).asString(null),
                                           null);
        for (String type : getTargetTypes()) {
            targetTypeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(targetTypeFilter);
        ctx.respondWith().template("view/core/relations/types.html", ph.asPage(), this);
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
        relationTypes(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_RELATION_TYPES)
    @Routed(value = "/relations/type/:1")
    public void relationType(WebContext ctx, String typeId) {
        RelationType type = findForTenant(RelationType.class, typeId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = type.isNew();
                if (type.isNew()) {
                    type.getTenant().setValue(tenants.getRequiredTenant());
                    type.setSourceType(ctx.get(RelationType.SOURCE_TYPE.getName()).asString(null));
                    type.setTargetType(ctx.get(RelationType.TARGET_TYPE.getName()).asString(null));
                }
                type.setName(ctx.get(RelationType.NAME.getName()).asString());
                type.setReverseName(ctx.get(RelationType.REVERSE_NAME.getName()).asString());
                type.setShowReverse(ctx.get(RelationType.SHOW_REVERSE.getName()).asBoolean(false));
                type.setViewInList(ctx.get(RelationType.VIEW_IN_LIST.getName()).asBoolean(false));
                type.setViewInList(ctx.get(RelationType.VIEW_IN_LIST.getName()).asBoolean(false));

                oma.update(type);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectToGet("/relations/type/" + type.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/core/relations/type.html", type, this);
    }

    @LoginRequired
    @Routed("/relations/autocomplete/:1")
    public void relationsAutocomplete(final WebContext ctx, String type) {
        String baseType = computeBaseType(type);
        AutocompleteHelper.handle(ctx, (query, result) -> {
            oma.select(RelationType.class)
               .eq(RelationType.TENANT, tenants.getRequiredTenant())
               .where(Or.of(FieldOperator.on(RelationType.SOURCE_TYPE).eq(type),
                            FieldOperator.on(RelationType.SOURCE_TYPE).eq(baseType)))
               .orderAsc(RelationType.SOURCE_TYPE)
               .orderAsc(RelationType.TARGET_TYPE)
               .iterateAll(relationType -> {
                   if (relationType.getTargetType() == null) {
                       for (RelationProvider provider : context.getParts(RelationProvider.class)) {
                           provider.computeSuggestions(null, query, false, suggestion -> {
                               result.accept(new AutocompleteHelper.Completion(relationType.getIdAsString()
                                                                               + ":"
                                                                               + suggestion.getFirst(),
                                                                               relationType.getName()
                                                                               + ": "
                                                                               + suggestion.getSecond(),
                                                                               relationType.getName()
                                                                               + ": "
                                                                               + suggestion.getSecond()));
                           });
                       }
                   } else {
                       Tuple<String, String> mainAndSubType = Strings.split(relationType.getTargetType(), "-");
                       RelationProvider provider = context.findPart(mainAndSubType.getFirst(), RelationProvider.class);
                       provider.computeSuggestions(mainAndSubType.getSecond(), query, false, suggestion -> {
                           result.accept(new AutocompleteHelper.Completion(relationType.getIdAsString()
                                                                           + ":"
                                                                           + suggestion.getFirst(),
                                                                           relationType.getName()
                                                                           + ": "
                                                                           + suggestion.getSecond(),
                                                                           relationType.getName()
                                                                           + ": "
                                                                           + suggestion.getSecond()));
                       });
                   }
               });
        });
    }

    protected String computeBaseType(String type) {
        String baseType = type;
        if (baseType.contains("-")) {
            baseType = Strings.split(baseType, "-").getFirst();
        }
        return baseType;
    }

    @Routed("/relations/add/:1")
    public void addRelation(final WebContext ctx, String objectId) {
        Entity target = oma.resolveOrFail(objectId);
        if (Strings.areEqual(ctx.get("authHash").asString(), computeAuthHash(objectId))) {
            Relations relations = getRelations(target);
            if (relations != null) {
                relations.addFromUniversalId(ctx.get("relationId").asString());
            }
        }
        ctx.respondWith().redirectToGet(ctx.get("redirectUrl").asString("/"));
    }

    private Relations getRelations(Entity entity) {
        if (entity instanceof HasRelations) {
            return ((HasRelations) entity).getRelations();
        }

        for (Class<? extends Mixable> mixin : schema.getDescriptor(entity.getClass()).getMixins()) {
            if (HasRelations.class.isAssignableFrom(mixin)) {
                return ((HasRelations) entity.as(mixin)).getRelations();
            }
        }

        return null;
    }

    @Routed("/relations/delete/:1/:2")
    public void deleteRelation(final WebContext ctx, String relationId, String authHash) {
        Relation relation = oma.find(Relation.class, relationId).orElse(null);

        if (Strings.areEqual(authHash, computeAuthHash(relation.getOwnerType() + "-" + relation.getOwnerId()))) {
            oma.delete(relation);
        }
        ctx.respondWith().redirectToGet(ctx.get("redirectUrl").asString("/"));
    }
}
