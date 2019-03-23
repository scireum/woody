/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.opportunities;

import sirius.biz.model.BizEntity;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.EntityRef;
import sirius.kernel.nls.NLS;
import woody.core.employees.Employee;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by gerhardhaufler on 10.03.19.
 */
public class OpportunityStateChanges extends BizEntity {

    @Autoloaded
    private final EntityRef<Opportunity> opportunity = EntityRef.on(Opportunity.class, EntityRef.OnDelete.CASCADE);
    public static final String OPPORTUNITY = "opportunity";

    @Autoloaded
    private LocalDateTime datetime = LocalDateTime.now();
    public static final String DATETIME = "datetime";

    @Autoloaded
    private OpportunityState oldState;
    public static final String OLDSTATE = "oldState";

    @Autoloaded
    private OpportunityState newState;
    public static final String NEWSTATE = "newState";

    @Autoloaded
    private final EntityRef<UserAccount> userAccount = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);
    public static final String USERACCOUNT = "userAccount";

    @Override
    public String toString() {
        Employee employee = userAccount.getValue().as(Employee.class);
        StringBuilder sb = new StringBuilder();
        sb.append(employee.getShortName());
        sb.append(":");
        sb.append(opportunity.toString());
        sb.append("@");
        sb.append(NLS.toUserString(datetime));
        sb.append(":");
        sb.append(oldState);
        sb.append("->");
        sb.append(newState);
        return sb.toString();
    }

    public Employee getEmployee() {
        return userAccount.getValue().as(Employee.class);
    }

    public EntityRef<Opportunity> getOpportunity() {
        return opportunity;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public OpportunityState getOldState() {
        return oldState;
    }

    public void setOldState(OpportunityState oldState) {
        this.oldState = oldState;
    }

    public OpportunityState getNewState() {
        return newState;
    }

    public void setNewState(OpportunityState newState) {
        this.newState = newState;
    }

    public EntityRef<UserAccount> getUserAccount() {
        return userAccount;
    }

}
