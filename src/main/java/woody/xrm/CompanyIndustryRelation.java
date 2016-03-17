/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.TenantAware;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;

/**
 * Created by gerhardhaufler on 07.02.16.
 */
public class CompanyIndustryRelation extends TenantAware {
    private final EntityRef<Company> company = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column COMPANY = Column.named("company");

    private final EntityRef<Industry> industry = EntityRef.on(Industry.class, EntityRef.OnDelete.CASCADE);
    public static final Column INDUSTRY = Column.named("industry");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(company);
        sb.append(" Branche ");
        sb.append(industry);
        return sb.toString() ;
    }

}
