/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

import org.apache.poi.ss.formula.functions.T;
import sirius.db.jdbc.Database;
import sirius.db.jdbc.Databases;
import sirius.db.mixing.OMA;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import woody.opportunities.Opportunity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Created by gerhardhaufler on 01.01.19.
 */
@Register(classes = {MigrationJob.class})
public class MigrationJobBean implements MigrationJob{

    private boolean run = false;

    @Part
    private static MigrationTableService mts;

    @Part
    private static MigrationAddAssignmentService mas;

    @Part
    private static Databases databases;

    @Part
    private static OMA oma;

    @Override
    public void migrateCrmToWdody() {
        if (run) {
            System.out.println("Achtung, 2. Start ist nicht zulässig --> Abbruch.");
            return;
        }

        LocalDateTime startLdt = LocalDateTime.now();
        System.out.println("");
        System.out.println("=======================================================================================");
        System.out.println("Start Migration CRM --> Woody.");

        deleteWoody();

        // todo aha:  Verriegelung gegen Zweitstart: warum notwendig?
        run = true;

        // migrate as first the useraccounts
        mts.migrateCrmDataToWoody("employee", "useraccount");
        mas.transferEmployeeInUserAccount(null);

        // migrate the other tables, do not change the order!
        mts.migrateCrmDataToWoody("mailtemplate", null);
        mts.migrateCrmDataToWoody("industry", null);
        mts.migrateCrmDataToWoody("tag", null);
        mts.migrateCrmDataToWoody("campaign", null);
        mts.migrateCrmDataToWoody("company", null);
        mts.migrateCrmDataToWoody("person", null);
        mts.migrateCrmDataToWoody("product", null);
        mts.migrateCrmDataToWoody("packageDefinition", null);
        mts.migrateCrmDataToWoody("contract", null);
        mts.migrateCrmDataToWoody("offer", null);
        mts.migrateCrmDataToWoody("offerItem", null);
        mts.migrateCrmDataToWoody("opportunity", null);
        mts.saveAllOpportunities();
        mts.migrateCrmDataToWoody("opportunityStateChanges", null);
        mts.migrateCrmDataToWoody("comment", null);

        // add more data to several tables
        mts.addDataprivacyPersons();
        mas.addTags();
        mas.addTagAssignemtsFromCompany();
        mas.addTagAssignmentsFromPerson();

        // transfer assignmentTables into Woody
        mas.transferTagAssignment("tagCompanyAssignment", "COMPANY");
        mas.transferTagAssignment("industryAssignment", "INDUSTRY");
        mas.transferTagAssignment("tagOpportunityAssignment", "OPPORTUNITY");
        mas.transferTagAssignment("tagPersonAssignment", "PERSON");

        // update sequencecounters
        mas.updateSequenceCounter( "OFFER", "OFFERS-1", "sequencecounter");
        mas.updateSequenceCounter( "company", "COMPANIES-1", "table");  // im CRM keine sequence-Number für Company vorhanden

        LocalDateTime endLdt = LocalDateTime.now();
        Long seconds = ChronoUnit.SECONDS.between(startLdt, endLdt);
        System.out.println("Ende Migration CRM --> Woody, Zeitdauer = " + seconds + " sec.");
        System.out.println("=======================================================================================");
        System.out.println("");
        run = false;
    }



    @Override
    public void deleteWoody() {
        if (run) {
            return;
        }

        Database dbWoody = databases.get("mixing");

        System.out.println("Start delete Woody" );
        // do not change the order!
        mts.deleteDataPrivacyPersonsInCompanies();
        mts.deleteContentOfTable(dbWoody, "phonecall");
        mts.deleteContentOfTable(dbWoody, "opportunityStateChanges");
        mts.deleteContentOfTable(dbWoody, "tagassignment");
        mts.deleteContentOfTable(dbWoody, "tag");
        mts.deleteContentOfTable(dbWoody, "notemail");
        mts.deleteContentOfTable(dbWoody, "comment");
        mts.deleteContentOfTable(dbWoody, "mail");
        mts.deleteContentOfTable(dbWoody, "opportunity");
        mts.deleteContentOfTable(dbWoody, "campaign");
        mts.deleteContentOfTable(dbWoody, "offerItem");
        mts.deleteContentOfTable(dbWoody, "offer");
        mts.deleteContentOfTable(dbWoody, "contract");
        mts.deleteContentOfTable(dbWoody, "useraccount");
        mts.deleteContentOfTable(dbWoody, "packageDefinition");
        mts.deleteContentOfTable(dbWoody, "product");
        mts.deleteContentOfTable(dbWoody, "person");
        mts.deleteContentOfTable(dbWoody, "industry");
        mts.deleteContentOfTable(dbWoody, "company");

        mts.deleteContentOfTable(dbWoody, "mailtemplate");

        System.out.println("Ende delete Woody" );

        run = false;
    }
}
