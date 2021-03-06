/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import woody.xrm.Company;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by aha on 11.05.15.
 */
@Framework("sales")
@Register(name = "companyContracts", classes = {Controller.class, SalesControllerService.class})
public class SalesController extends BizController implements SalesControllerService {

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @DefaultRoute
    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/products")
    public void products(WebContext ctx) {
        SQLPageHelper<Product> ph =
                SQLPageHelper.withQuery(tenants.forCurrentTenant(oma.select(Product.class).orderAsc(Product.NAME)));
        ph.withContext(ctx);
        ph.withSearchFields(QueryField.contains(Product.NAME), QueryField.contains(Product.ID));
        ctx.respondWith().template("view/sales/products.html", ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1")
    public void product(WebContext ctx, String productId) {
        Product product = productHandler(ctx, productId, false);
        ctx.respondWith().template("view/sales/product-details.html", product);
    }

//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/packageDefinition/:1")
//    public void packageDefinition(WebContext ctx, String packageDefinitionId) {
//        PackageDefinition packageDefinition = packageDefinitionHandler(ctx, packageDefinitionId, false);
//        ctx.respondWith().template("view/sales/packageDefinition-details.html", packageDefinition);
//    }
//
//    @LoginRequired
//    @Permission(MANAGE_XRM)
//    @Routed("/product/:1/packageDefinitions")
//    public void productPackageDefinitions(WebContext ctx, String productId) {
//        PageHelper<PackageDefinition> ph = PageHelper.withQuery(oma.select(PackageDefinition.class)
//                                                                   .eq(PackageDefinition.PRODUCT, productId)
//                                                                   .orderAsc(Mapping.named(Product.NAME
//                                                                                           + ","
//                                                                                           + PackageDefinition.NAME)));
//        ph.withContext(ctx);
//        ph.withSearchFields(PackageDefinition.NAME, Product.NAME);
//        Optional oproduct = oma.find(Product.class, productId);
//        Product product = (Product) oproduct.get();
//        currentProduct = product;
//        ctx.respondWith().template("view/sales/product-packageDefinitions.html", ph.asPage());
//    }

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
                    ctx.respondWith().redirectTemporarily("/product/" + product.getId());
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

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contract/:2")
    public void contract(WebContext ctx, String companyId, String contractId) {
        Company company = findForTenant(Company.class, companyId);
        Contract contract = find(Contract.class, contractId);
        assertNotNew(company);
        setOrVerify(contract, contract.getCompany(), company);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = contract.isNew();
                load(ctx, contract);
                oma.update(contract);
//                contract.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily("/company/" + company.getId() + "/contract/" + contract.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("view/sales/contract-details.html", company, contract);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contract/:2/delete")
    public void deleteContract(WebContext ctx, String companyId, String contractId) {
        Optional<Contract> contract = tryFind(Contract.class, contractId);
        if (contract.isPresent()) {
            assertTenant(contract.get().getCompany().getValue());
            oma.delete(contract.get());
            showDeletedMessage();
        }
        companyContracts(ctx, companyId);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contracts")
    public void companyContracts(WebContext ctx, String companyId) {
//        Company company = findForTenant(Company.class, companyId);
//        MagicSearch search = MagicSearch.parseSuggestions(ctx);
//        SmartQuery<Contract> query = oma.select(Contract.class)
//                                        .eq(Contract.COMPANY, company)
//                                        .orderAsc(Contract.ACCOUNTINGGROUP)
//                                        .orderAsc(Contract.STARTDATE);
//
//        Tagged.applyTagSuggestions(Contract.class, search, query);
//        PageHelper<Contract> ph = PageHelper.withQuery(query);
//        ph.withContext(ctx);
//        ctx.respondWith()
//           .template("view/sales/company-contracts.html", company, ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/contracts")
    // ToDo: Wofür ist dieser Code? - Analog zur Methode persons
    public void contracts(WebContext ctx) {
//        MagicSearch search = MagicSearch.parseSuggestions(ctx);
//        SmartQuery<Contract> query = oma.select(Contract.class)
//                                        .fields(Contract.COMPANY.join(Company.ID))
//                                        .eq(Contract.COMPANY.join(Company.TENANT), tenants.getRequiredTenant())
//                                        .orderAsc(Contract.STARTDATE)
//                                        .orderAsc(Contract.ENDDATE);
//
//        Tagged.applyTagSuggestions(Contract.class, search, query);
//        PageHelper<Contract> ph = PageHelper.withQuery(query);
//        ph.withContext(ctx);
//        ctx.respondWith().template("view/sales/company-contracts.html", ph.asPage(), search.getSuggestionsString());
    }

    private PackageDefinition packageDefinitionHandler(WebContext ctx,
                                                       String packageDefinitionId,
                                                       boolean forceDetails) {

        PackageDefinition packageDefinition = findForTenant(PackageDefinition.class, packageDefinitionId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = packageDefinition.isNew();
                if (packageDefinition.isNew()) {
                    // do nothing
                }
                Collection<String> list = ctx.getParameterNames();
                load(ctx, packageDefinition);
                if (packageDefinition.getProduct().getValue() == null) {
                    packageDefinition.getProduct().setValue(currentProduct);
                }
                oma.update(packageDefinition);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily("/packageDefinition/" + packageDefinition.getId());
                    return packageDefinition;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        return packageDefinition;
    }
}
