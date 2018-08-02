/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.kernel.commons.Context;

/**
 * Created by gerhardhaufler on 03.11.16.
 */
//@Register(classes = SystemJobDescription.class)
public class LicenseAccountingJob /* extends SystemJobDescription */ {

    //    @Override
//    public void collectParameters(Consumer<JobParameterDescription> consumer) {
//        int ggg = 1;
//        consumer.accept(new JobParameterDescription());
//
//    }
// ToDO LicenseAccounting Job fertig machen
//    @Override
    public boolean verifyParameters(Context context) {
        return false;
    }
//
//    @Override
//    public void execute(Context params, ManagedTaskContext ctx) {
//        LocalDate referenceDate = params.getValue("referenceData").asLocalDate(LocalDate.now());
//
//        Watch w = Watch.start();
//
//        ctx.addTiming("company", w.elapsedMillis());
//    }
//
//    @Nonnull
//    @Override
//    public String getName() {
//        int gg = 1;
//        return "license-accounting-job";
//    }
//
//    @Override
//    public String getTitle() {
//        int gg = 1;
//        return "Licence-Accounting-Job";}
}
