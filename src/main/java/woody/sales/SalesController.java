/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Column;
import sirius.db.mixing.SmartQuery;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.offers.Offer;
import woody.xrm.Company;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by aha on 11.05.15.
 */
@Framework("sales")
@Register(name = "companyContracts", classes = {Controller.class, SalesControllerService.class})
public class SalesController extends BizController implements SalesControllerService {

    private static final String MANAGE_XRM = "permission-manage-xrm";


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/products")
    public void products(WebContext ctx) {
        PageHelper<Product> ph =
                PageHelper.withQuery(oma.select(Product.class).orderAsc(Product.NAME)).forCurrentTenant();
        ph.withContext(ctx);
        ph.withSearchFields(Product.NAME, Product.ID);
        ctx.respondWith().template("templates/sales/products.html.pasta", ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1")
    public void product(WebContext ctx, String productId) {
        Product product = findForTenant(Product.class, productId);
        if (product.isNew()) {
            ctx.respondWith().template("templates/sales/product-details.html.pasta", product);
        } else {
            ctx.respondWith().template("templates/sales/product-overview.html.pasta", product);
        }
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/edit")
    public void editProduct(WebContext ctx, String productId) {

        Product product = findForTenant(Product.class, productId);
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
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("templates/sales/product-details.html.pasta", product);
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
    @Routed("/packageDefinition/:1")
    public void packageDefinition(WebContext ctx, String packageDefinitionId) {
//        PackageDefinition packageDefinition = packageDefinitionHandler(ctx, packageDefinitionId, false);
//        ctx.respondWith().template("view/sales/packageDefinition-details.html", packageDefinition);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/packageDefinitions")
    public void productPackageDefinitions(WebContext ctx, String productId) {
        Product product = findForTenant(Product.class, productId);
        SmartQuery<PackageDefinition> query = oma.select(PackageDefinition.class)
                                        .eq(PackageDefinition.PRODUCT, product)
                                        .orderAsc(PackageDefinition.NAME);
        PageHelper<PackageDefinition> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ph.withSearchFields(PackageDefinition.NAME);
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/sales/product-packageDefinitions.html.pasta", product, ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/packageDefinition/:2")
    public void packageDefinition(WebContext ctx, String productId, String packageDefinitionId) {
        Product product = findForTenant(Product.class, productId);
        PackageDefinition packageDefinition = find(PackageDefinition.class, packageDefinitionId);
        assertNotNew(product);
        setOrVerify(packageDefinition, packageDefinition.getProduct(), product);

        ctx.respondWith().template("templates/sales/packageDefinition-overview.html.pasta", product, packageDefinition);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/product/:1/packageDefinition/:2/edit")
    public void editPackageDefinition(WebContext ctx, String productId, String packageDefinitionId) {
        Product product = findForTenant(Product.class, productId);
        PackageDefinition packageDefinition = find(PackageDefinition.class, packageDefinitionId);
        assertNotNew(product);
        setOrVerify(packageDefinition, packageDefinition.getProduct(), product);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = packageDefinition.isNew();
                load(ctx, packageDefinition);
                oma.update(packageDefinition);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith()
                       .redirectTemporarily("/product/" + product.getId() + "/packageDefinition/" + packageDefinition.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith().template("templates/sales/packageDefinition-details.html.pasta", product, packageDefinition);
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contracts")
    public void companyContracts(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        SmartQuery<Contract> query = oma.select(Contract.class)
                                        .eq(Contract.COMPANY, company)
                                        .orderAsc(Contract.ACCOUNTINGGROUP)
                                        .orderAsc(Contract.STARTDATE);
        PageHelper<Contract> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ph.withSearchFields(Contract.STARTDATE);
        ph.enableAdvancedSearch();
        ctx.respondWith().template("templates/sales/company-contracts.html.pasta", company, ph.asPage());
    }

    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contract/:2")
    public void contract(WebContext ctx, String companyId, String contractId) {
        Company company = findForTenant(Company.class, companyId);
        Contract contract = find(Contract.class, contractId);
        assertNotNew(company);
        setOrVerify(contract, contract.getCompany(), company);

        ctx.respondWith().template("templates/sales/contract-overview.html.pasta", company, contract);
    }


    @LoginRequired
    @Permission(MANAGE_XRM)
    @Routed("/company/:1/contract/:2/edit")
    public void editContract(WebContext ctx, String companyId, String contractId) {
        Company company = findForTenant(Company.class, companyId);
        Contract contract = find(Contract.class, contractId);
        assertNotNew(company);
        setOrVerify(contract, contract.getCompany(), company);

        if (ctx.isPOST()) {
            try {
                boolean wasNew = contract.isNew();
                load(ctx, contract);
                oma.update(contract);
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
        ctx.respondWith().template("templates/sales/contract-details.html.pasta", company, contract);
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

}
