/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;


import sirius.biz.tenants.TenantAware;
import sirius.mixing.Column;
import sirius.mixing.EntityRef;

/**
 * Created by gerhardhaufler on 07.02.16.
 */
public class CompanyRelation extends TenantAware {
    /**<code>
     * Here are stored all relations between the companies.
     * CompanyRelation = Beziehung zwischen den beiden Firmen.
     * whole-company   übergeordnet
     *
     *       |
     *       |
     *       V
     * part-company    untergeordnet
     *
     * </code>
     */

    private final EntityRef<Company> part = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column PART = Column.named("part");

    private CompanyRelationType companyRelationType;
    public static final Column COMPANYRELATIONTYPE = Column.named("companyrelationtype");

    private final EntityRef<Company> whole = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column WHOLE = Column.named("whole");

    @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(part);
            sb.append(" ist ");
            sb.append(companyRelationType.toString());
            sb.append(" von ");
            sb.append(whole);
            return sb.toString() ;
    }
}
