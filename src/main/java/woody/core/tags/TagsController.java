/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.OMA;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.Like;
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
import java.util.stream.Collectors;

/**
 * Created by aha on 18.07.16.
 */
@Register(classes = Controller.class)
public class TagsController extends BizController {

    @Part
    private OMA oma;

    private static final String MANAGE_TAGS = "permission-manage-tags";

    private Map<String, String> typeMap;
    private List<String> targetTypes;

    @Part
    private Schema schema;

    public List<String> getTargetTypes() {
        if (targetTypes == null) {
            targetTypes = schema.getDesciptors()
                                .stream()
                                .filter(descriptor -> descriptor.hasComposite(Tagged.class))
                                .map(descriptor -> Schema.getNameForType(descriptor.getType()))
                                .collect(Collectors.toList());
            targetTypes.sort(String::compareTo);
        }
        return targetTypes;
    }

    public String translateType(String targetType) {
        if (typeMap == null) {
            typeMap = schema.getDesciptors()
                            .stream()
                            .filter(descriptor -> descriptor.hasComposite(Tagged.class))
                            .collect(Collectors.toMap(descriptor -> Schema.getNameForType(descriptor.getType()),
                                                      EntityDescriptor::getPluralLabel));
        }
        return typeMap.get(targetType);
    }

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_TAGS)
    @Routed("/tags")
    public void tags(WebContext ctx) {
        PageHelper<Tag> ph = PageHelper.withQuery(oma.select(Tag.class).orderAsc(Tag.TARGET_TYPE).orderAsc(Tag.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(Tag.NAME).forCurrentTenant();
        Facet typeFilter = new Facet(NLS.get("Tag.targetType"),
                                     Tag.TARGET_TYPE.getName(),
                                     ctx.get(Tag.TARGET_TYPE.getName()).asString(null),
                                     null);
        for (String type : getTargetTypes()) {
            typeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(typeFilter);
        ctx.respondWith().template("view/core/tags/tags.html", ph.asPage(), this);
    }

    @LoginRequired
    @Permission(MANAGE_TAGS)
    @Routed("/tag/:1/delete")
    public void deleteTag(WebContext ctx, String id) {
        Optional<Tag> tag = tryFindForTenant(Tag.class, id);
        if (tag.isPresent()) {
            oma.delete(tag.get());
            showDeletedMessage();
        }
        tags(ctx);
    }

    @LoginRequired
    @Permission(MANAGE_TAGS)
    @Routed("/tag/:1")
    public void tag(WebContext ctx, String tagId) {
        Tag tag = findForTenant(Tag.class, tagId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = tag.isNew();
                if (tag.isNew()) {
                    tag.getTenant().setValue(tenants.getRequiredTenant());
                    tag.setTargetType(ctx.get(Tag.TARGET_TYPE.getName()).asString(null));
                }
                tag.setName(ctx.get(Tag.NAME.getName()).asString());
                tag.setViewInList(ctx.get(Tag.VIEW_IN_LIST.getName()).asBoolean(false));
                oma.update(tag);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectToGet("/tag/" + tag.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/core/tags/tag.html", tag, this);
    }

    @LoginRequired
    @Routed("/tags/:1/autocomplete")
    public void tagsAutocomplete(final WebContext ctx, String type) {
        AutocompleteHelper.handle(ctx, (query, result) -> {
            oma.select(Tag.class)
               .eq(Tag.TARGET_TYPE, type)
               .eq(Tag.TENANT, currentTenant())
               .orderAsc(Tag.NAME)
               .where(Like.on(Tag.NAME).ignoreCase().ignoreEmpty().contains(query))
               .iterateAll(t -> {
                   result.accept(new AutocompleteHelper.Completion(t.getName(), t.getName(), t.getName()));
               });
        });
    }

}
