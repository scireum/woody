/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.categories;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Parts;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Facet;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.Permission;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Register(classes = {Controller.class, CategoryController.class})
public class CategoryController extends BizController {

    private static final String PERMISSION_MANAGE_CATEGORIES = "permission-manage-categories";

    @Parts(CategoryTypeProvider.class)
    private Collection<CategoryTypeProvider> typeProviders;

    @ConfigValue("security.roles")
    private List<String> roles;

    @DefaultRoute
    @Permission(PERMISSION_MANAGE_CATEGORIES)
    @Routed("/categories")
    public void categories(WebContext ctx) {
        PageHelper<Category> ph = PageHelper.withQuery(oma.select(Category.class).orderAsc(Category.NAME));
        ph.withContext(ctx);
        ph.withSearchFields(Category.NAME).forCurrentTenant();
        Facet typeFacet = new Facet(NLS.get("Category.type"),
                                    Category.TYPE.getName(),
                                    ctx.get(Category.TYPE.getName()).asString(),
                                    null);
        typeProviders.forEach(provider -> typeFacet.addItem(provider.getName(), provider.getLabel(), -1));
        ph.addFilterFacet(typeFacet);

        ctx.respondWith().template("/templates/organization/categories/categories.html.pasta", ph.asPage(), this);
    }

    @Permission(PERMISSION_MANAGE_CATEGORIES)
    @Routed("/category/:1")
    public void category(WebContext ctx, String id) {
        Category category = findForTenant(Category.class, id);
        if (category.isNew()) {
            category.setType(ctx.get(Category.TYPE.getName()).asString());
        }

        boolean requestHandled = prepareSave(ctx).withPreSaveHandler(wasNew -> {
            if (!isValidType(category.getType())) {
                throw Exceptions.handle().withSystemErrorMessage("Unknown type: %s", category.getType()).handle();
            }
        }).withAfterSaveURI("/categories").saveEntity(category);

        if (!requestHandled) {
            validate(category);
            ctx.respondWith().template("/templates/organization/categories/category.html.pasta", category, this);
        }
    }

    public List<Tuple<String, String>> getTypes() {
        return typeProviders.stream()
                            .map(provider -> Tuple.create(provider.getName(), provider.getLabel()))
                            .collect(Collectors.toList());
    }

    public String translateType(Category category) {
        return findProvider(category.getType()).map(CategoryTypeProvider::getLabel).orElse("");
    }

    private boolean isValidType(String type) {
        return findProvider(type).isPresent();
    }

    private Optional<CategoryTypeProvider> findProvider(String type) {
        return typeProviders.stream().filter(provider -> Strings.areEqual(type, provider.getName())).findFirst();
    }

    /**
     * Lists all roles which can be granted to a user.
     *
     * @return all roles which can be granted to a user
     */
    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    /**
     * Returns the translated name of a role.
     *
     * @param role the role to translate
     * @return a translated name for the given role
     */
    public String translateRole(String role) {
        return NLS.get("Role." + role);
    }

    @Permission(PERMISSION_MANAGE_CATEGORIES)
    @Routed("/category/:1/delete")
    public void deleteCategory(WebContext ctx, String id) {
        Optional<Category> category = tryFindForTenant(Category.class, id);
        category.ifPresent(cat -> {
            findProvider(cat.getType()).ifPresent(provider -> provider.beforeDelete(cat));
            oma.delete(cat);
            showDeletedMessage();
        });

        ctx.respondWith().redirectTemporarily("/categories");
    }
}
