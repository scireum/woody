/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.collmex;

import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.kernel.di.std.Framework;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.templates.Templates;
import woody.offers.ServiceAccountingService;
import woody.sales.SalesControllerService;
import woody.xrm.Company;

import java.util.HashMap;
import java.util.List;

/**
 * Created by gha on 30.10.2016.
 */
@Framework("collmex")
@Register(classes = Controller.class)
public class CollmexController extends BizController {

    public static final String MANAGE_OFFER = "permission-manage-offers";
    public static final String VIEW_OFFER = "permission-view-offers";

    private static final String MANAGE_XRM = "permission-manage-xrm";

    @Part
    private static SalesControllerService scs;

    @Part
    private static ServiceAccountingService sas;

    @Part
    private static Templates templates;

    @Part
    private static CollmexInformationService cis;

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/generateCollmexString")
    public void generateCollmexString(WebContext ctx) {
        UserAccount uac = findForTenant(UserAccount.class, "4");
        String message = cis.generateCollmexAccessStrings(uac);
        System.err.println(message);
        ctx.respondWith().template("view/main/main.html");
    }

    @LoginRequired
    @Permission(MANAGE_OFFER)
    @Routed("/connectCollmex")
    public void connectCollmex(WebContext ctx) {
 //       UserAccount uac = findForTenant(UserAccount.class, "4");
        List<String> accessData = cis.getCollmexAccessData(null);
        HashMap<String, String> map = cis.getCollmexCustomerData("10004", accessData);
        Company company = findForTenant(Company.class, "3001");

        String csvString = cis.generateCollmexCmxKnd(company);
        System.err.println("controller/generateCsvString:"+csvString);
        cis.sendCmxKnd(accessData, csvString);
        ctx.respondWith().template("view/main/main.html");
    }


}
