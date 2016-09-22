/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

import sirius.biz.model.AddressData;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Composite;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.nls.NLS;
import woody.xrm.Company;

/**
 * Created by gerhardhaufler on 22.09.16.
 */
public class CompanyAccountingData extends Composite {

    // TODO mit aha verifizieren

    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    @NullAllowed
    @Autoloaded
    private final AddressData invoiceAddress =
            new AddressData(AddressData.Requirements.NOT_PARTIAL, NLS.get("CompanyAccountingData.invoiceAdress"));
    public static final Column POSTBOXADDRESS = Column.named("invoiceAddress");

    @Trim
    @Autoloaded
    @Length(255)
    private String invoiceMedium = "PRINT";
    public static final Column INVOICEMEDIUM = Column.named("invoiceMedium");

    public static final Column INVOICEMAILADR = Column.named("invoiceMailAdr");
    @Trim
    @NullAllowed
    @Autoloaded
    @Length(150)
    private String invoiceMailAdr;

    @Autoloaded
    @NullAllowed
    @Numeric(scale = 3, precision = 15)
    private Amount ptPrice;
    public static final Column PTPRICE = Column.named("ptPrice");




}
