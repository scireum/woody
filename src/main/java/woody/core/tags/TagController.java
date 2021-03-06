/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.query.QueryField;
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
import woody.core.colors.ColorData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by aha on 18.07.16.
 */
@Register(classes = Controller.class)
public class TagController extends BizController {

    private static final String MANAGE_TAGS = "permission-manage-tags";

    private Map<String, String> typeMap;
    private List<String> targetTypes;

    public List<String> getTargetTypes() {
        if (targetTypes == null) {
            targetTypes = mixing.getDesciptors()
                                .stream()
                                .filter(descriptor -> descriptor.hasComposite(Tagged.class))
                                .map(descriptor -> mixing.getNameForType(descriptor.getType()))
                                .collect(Collectors.toList());
            targetTypes.sort(String::compareTo);
        }
        return targetTypes;
    }

    public String translateType(String targetType) {
        if (typeMap == null) {
            typeMap = mixing.getDesciptors()
                            .stream()
                            .filter(descriptor -> descriptor.hasComposite(Tagged.class))
                            .collect(Collectors.toMap(descriptor -> mixing.getNameForType(descriptor.getType()),
                                                      EntityDescriptor::getPluralLabel));
        }
        return typeMap.get(targetType);
    }

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_TAGS)
    @Routed("/tags")
    public void tags(WebContext ctx) {
        SmartQuery<Tag> query = oma.select(Tag.class)
                                   .fields(Tag.ID,
                                           Tag.NAME,
                                           Tag.TARGET_TYPE,
                                           Tag.COLOR.inner(ColorData.COLOR),
                                           Tag.VIEW_IN_LIST)
                                   .orderAsc(Tag.TARGET_TYPE)
                                   .orderAsc(Tag.NAME);
        SQLPageHelper<Tag> ph = SQLPageHelper.withQuery(tenants.forCurrentTenant(query));
        ph.withContext(ctx);
        ph.withSearchFields(QueryField.contains(Tag.NAME));
        Facet typeFilter = new Facet(NLS.get("Tag.targetType"),
                                     Tag.TARGET_TYPE.getName(),
                                     ctx.get(Tag.TARGET_TYPE.getName()).asString(),
                                     null);
        for (String type : getTargetTypes()) {
            typeFilter.addItem(type, translateType(type), -1);
        }
        ph.addFilterFacet(typeFilter);
        ctx.respondWith().template("/templates/core/tags/tags.html.pasta", ph.asPage(), this);
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

        ctx.respondWith().redirectToGet("/tags");
    }

    @LoginRequired
    @Permission(MANAGE_TAGS)
    @Routed("/tag/:1")
    public void tag(WebContext ctx, String tagId) {
        Tag tag = findForTenant(Tag.class, tagId);

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(isNew -> {
            if (isNew) {
                tag.setTargetType(ctx.get(Tag.TARGET_TYPE.getName()).asString());
            }
        }).withAfterSaveURI("/tags").saveEntity(tag);

        if (!requestHandled) {
            validate(tag);
            ctx.respondWith().template("/templates/core/tags/tag.html.pasta", tag, this);
        }
    }

    @LoginRequired
    @Routed("/tags/:1/autocomplete")
    public void tagsAutocomplete(final WebContext ctx, String type) {
        AutocompleteHelper.handle(ctx,
                                  (query, result) -> oma.select(Tag.class)
                                                        .eq(Tag.TARGET_TYPE, type)
                                                        .eq(Tag.TENANT, tenants.getRequiredTenant())
                                                        .orderAsc(Tag.NAME)
                                                        .queryString(query, QueryField.contains(Tag.NAME))
                                                        .iterateAll(t -> result.accept(new AutocompleteHelper.Completion(
                                                                t.getName(),
                                                                t.getName(),
                                                                t.getName()))));
    }
}
