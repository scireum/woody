/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.items;

import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import woody.xrm.Company;

import java.util.Optional;

@Register(classes = Controller.class)
public class DiscountController extends BizController {

    @Routed("/company/:1/discounts")
    public void discounts(WebContext ctx, String companyId) {
        Company company = findForTenant(Company.class, companyId);
        SmartQuery<Discount> query = oma.select(Discount.class)
                                        .fields(Discount.ID,
                                                Discount.DISCOUNT_GROUP,
                                                Discount.DISCOUNT_IN_PERCENT,
                                                Discount.MONTHLY_CHARGE_DISCOUNT_IN_PERCENT)
                                        .eq(Discount.COMPANY, company)
                                        .orderAsc(Discount.DISCOUNT_GROUP);
        SQLPageHelper<Discount> ph = SQLPageHelper.withQuery(query);
        ph.withSearchFields(QueryField.contains(Discount.DISCOUNT_GROUP));
        ph.withContext(ctx);
        ctx.respondWith().template("/templates/sales/items/company-discounts.html.pasta", company, ph.asPage());
    }

    @Routed("/company/:1/discount/:2")
    public void discount(WebContext ctx, String companyId, String discountId) {
        Company company = findForTenant(Company.class, companyId);
        Discount discount = find(Discount.class, discountId);
        assertNotNew(company);
        setOrVerify(discount, discount.getCompany(), company);

        boolean requestHandled =
                prepareSave(ctx).withAfterSaveURI("/company/" + company.getId() + "/discounts").saveEntity(discount);

        if (!requestHandled) {
            validate(discount);
            ctx.respondWith().template("/templates/sales/items/company-discount.html.pasta", company, discount);
        }
    }

    @Routed("/company/:1/discount/:2/delete")
    public void deleteDiscount(WebContext ctx, String companyId, String discountId) {
        Optional<Discount> discount = tryFind(Discount.class, discountId);
        discount.ifPresent(value -> {
            assertTenant(value.getCompany().getValue());
            oma.delete(value);
            showDeletedMessage();
        });

        ctx.respondWith().redirectToGet("/company/" + companyId + "/discounts");
    }
}
