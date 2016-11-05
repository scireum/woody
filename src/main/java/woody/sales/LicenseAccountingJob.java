/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.biz.jobs.JobParameterDescription;
import sirius.biz.jobs.system.SystemJobDescription;
import sirius.kernel.commons.Context;
import sirius.kernel.commons.Watch;
import sirius.kernel.di.std.Register;
import sirius.web.tasks.ManagedTaskContext;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Created by gerhardhaufler on 03.11.16.
 */
@Register(classes = SystemJobDescription.class)
public class LicenseAccountingJob extends SystemJobDescription {

    @Override
    public void collectParameters(Consumer<JobParameterDescription> consumer) {
//                     consumer.accept(new JobParameterDescription());
    }

    @Override
    public boolean verifyParameters(Context context) {
        return false;
    }

    @Override
    public void execute(Context params, ManagedTaskContext ctx) {
        LocalDate referenceDate = params.getValue("referenceData").asLocalDate(LocalDate.now());

        Watch w = Watch.start();

        ctx.addTiming("company", w.elapsedMillis());
    }

    @Nonnull
    @Override
    public String getName() {
        return "license-accounting-job";
    }
}
