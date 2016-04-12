/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.PersonData;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.NLS;
import sirius.mixing.Column;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;

import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("products")
@Register(classes = Controller.class)
public class ProductController extends BizController {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/products")
    public void products(WebContext ctx) {
        PageHelper<Product> ph =
                PageHelper.withQuery(oma.select(Product.class).orderAsc(Product.NAME)).forCurrentTenant();
        ph.withContext(ctx);
        ph.withSearchFields(Product.NAME, Product.ID);
        ctx.respondWith().template("view/xrm/products.html", ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1" )
    public void product(WebContext ctx, String productId) {
        Product product = productHandler(ctx, productId, false);
        ctx.respondWith().template("view/xrm/product-details.html", product);

    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/packageDefinition/:1")
    public void packageDefinition(WebContext ctx, String packageDefinitionId) {
        PackageDefinition packageDefinition = packageDefinitionHandler(ctx, packageDefinitionId, false);
        ctx.respondWith().template("view/xrm/packageDefinition-details.html", packageDefinition);
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/packageDefinitions")
    public void productPackageDefinitions(WebContext ctx, String productId) {
        PageHelper<PackageDefinition> ph =
                PageHelper.withQuery(oma.select(PackageDefinition.class).eq(PackageDefinition.PRODUCT, productId)
                                        .orderAsc(Column.named(Product.NAME + ","+ PackageDefinition.NAME)));
        ph.withContext(ctx);
        ph.withSearchFields(PackageDefinition.NAME, Product.NAME);
        Optional oproduct = oma.find(Product.class, productId);
        Product product = (Product) oproduct.get();
        currentProduct = product;
        ctx.respondWith().template("view/xrm/product-packageDefinitions.html", ph.asPage());
    }

    private Product currentProduct;

    private Product productHandler(WebContext ctx, String productId, boolean forceDetails) {
        Product product = findForTenant(Product.class, productId);
        currentProduct = product;
        if (ctx.isPOST()) {
            try {
                boolean wasNew = product.isNew();
                if (product.isNew()) {
                    product.getTenant().setValue(tenants.getRequiredTenant());
                }
                load(ctx, product);
                oma.update(product);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/product/" + product.getId());
                    return product;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        return product;

    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/delete")
    public void deleteProduct(WebContext ctx, String id) {
        Optional<Product> product = tryFindForTenant(Product.class, id);
        if (product.isPresent()) {
            oma.delete(product.get());
            showDeletedMessage();
        }
        products(ctx);
    }

    private PackageDefinition packageDefinitionHandler(WebContext ctx, String packageDefinitionId, boolean forceDetails) {
        PackageDefinition packageDefinition = findForTenant(PackageDefinition.class, packageDefinitionId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = packageDefinition.isNew();
                if (packageDefinition.isNew()) {
                    // do nothing
                }
                load(ctx, packageDefinition);
                if(packageDefinition.getProduct().getValue() == null) {
                    packageDefinition.getProduct().setValue(currentProduct);
                }
                oma.update(packageDefinition);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/packageDefinition/" + packageDefinition.getId());
                    return packageDefinition;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        return packageDefinition;

    }

}
