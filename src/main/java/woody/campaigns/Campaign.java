/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.campaigns;

import org.jetbrains.annotations.NotNull;
import sirius.biz.tenants.TenantAware;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;
import sirius.web.security.UserContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gerhardhaufler on 10.03.19.
 */
public class Campaign extends TenantAware {

    /**
     * Start of the campaign
     */
    @Autoloaded

    private LocalDate startDate;
    public static final String STARTDATE = "startDate";

    /**
     * end of the campaign
     */
    @Autoloaded
    @NullAllowed
    private LocalDate endDate;
    public static final String ENDDATE = "endDate";

    /**
     * employee for this campaign
     */
    @Autoloaded
    private final EntityRef<UserAccount> userAccount = EntityRef.on(UserAccount.class, EntityRef.OnDelete.CASCADE);
    public static final String EMPLOYEE = "employee";

    /**
     * name of the campaign
     */
    @Autoloaded
    @Length(255)
    private String name;
    public static final String NAME = "name";

    /**
     * description of the campaign
     */
    @Autoloaded
    @Length(255)
    @NullAllowed
    private String description;
    public static final String DESCRIPTION = "description";


    @BeforeSave
    protected void onSave() {
        if(startDate == null) {
            startDate = LocalDate.now();
        }
        if(isNew()) {

                List<Campaign> campaignList = oma.select(Campaign.class)
                                                 .eq(Column.named(Campaign.NAME), name)
                                                 .eq(Column.named(Campaign.STARTDATE), startDate).queryList();

                if (!campaignList.isEmpty()) {
                    throw Exceptions.createHandled()
                                    .withNLSKey("Campaign.isPresent")
                                    .set("name", name)
                                    .set("date", NLS.toUserString(startDate))
                                    .handle();
                }



        }

        if(userAccount == null)  {
            this.getUserAccount().setValue(UserContext.getCurrentUser().as(UserAccount.class));
        }


    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("/");
        sb.append(NLS.toUserString(startDate));
        return sb.toString();
    }

    @NotNull
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NotNull LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public EntityRef<UserAccount> getUserAccount() {
        return userAccount;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
