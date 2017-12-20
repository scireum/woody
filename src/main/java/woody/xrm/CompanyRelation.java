/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.biz.tenants.TenantAware;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;

/**
 * Created by gerhardhaufler on 07.02.16.
 */
public class CompanyRelation extends TenantAware {
    /**
     * <code>
     * Here are stored all relations between the companies.
     * CompanyRelation = Beziehung zwischen den beiden Firmen.
     * whole-company   übergeordnet
     * <p>
     * |
     * |
     * V
     * part-company    untergeordnet
     * <p>
     * </code>
     */

    public static final String COMPANYRELATION_CODELIST = "companyRelation";

    private final EntityRef<Company> part = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column PART = Column.named("part");

    @Length(20)
    @NullAllowed
    private String companyRelation;
    public static final Column COMPANYRELATIONTYPE = Column.named("companyRelationType");

    private final EntityRef<Company> whole = EntityRef.on(Company.class, EntityRef.OnDelete.CASCADE);
    public static final Column WHOLE = Column.named("whole");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(part.toString());
        sb.append(" ist ");
        sb.append(companyRelation);
        sb.append(" von ");
        sb.append(whole.toString());
        return sb.toString();
    }
}
