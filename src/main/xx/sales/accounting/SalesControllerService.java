/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

/**
 * 
 */
package woody.sales.accounting;




import sirius.kernel.commons.Amount;
import sirius.kernel.commons.DataCollector;
import sirius.web.http.WebContext;
import woody.xrm.Company;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Here are stored all service-descriptions for the SalesController
 * 
 */
public interface SalesControllerService {

	public void companyContracts(WebContext ctx, String companyId);

}
