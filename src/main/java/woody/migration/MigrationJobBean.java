/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.migration;

import sirius.db.jdbc.Database;
import sirius.db.jdbc.Databases;
import sirius.db.mixing.OMA;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    @Override
    public void migrateCrmToWdody() {
        if (run) {
            System.out.println("Achtung, 2. Start ist nicht zulässig --> Abbruch.");
            return;
        }

        LocalDateTime startLdt = LocalDateTime.now();
        System.out.println("=======================================================================================");
        System.out.println("Start Migration CRM --> Woody.");

//        deleteWoody();

        run = true;
//        mts.migrateCrmDataToWoody("industry", null);
//        mts.migrateCrmDataToWoody("tag", null);
//        mts.migrateCrmDataToWoody("company", null);
//        mts.migrateCrmDataToWoody("person", null);
//        mts.migrateCrmDataToWoody("product", null);
//        mts.migrateCrmDataToWoody("packageDefinition", null);
//        mts.migrateCrmDataToWoody("employee", "useraccount");
//        mas.transferEmployeeInUserAccount(null);
//        mts.migrateCrmDataToWoody("contract", null);
//        mts.migrateCrmDataToWoody("offer", null);
//        mts.migrateCrmDataToWoody("offerItem", null);
//        mts.migrateCrmDataToWoody("opportunity", null);
//        mts.migrateCrmDataToWoody("comment", null);
//
//        mas.addTags();
//        mas.addTagAssignemtsFromCompany();
//        mas.addTagAssignmentsFromPerson();
//        mas.transferTagAssignment("tagCompanyAssignment", "COMPANY");
//        mas.transferTagAssignment("industryAssignment", "INDUSTRY");
//        mas.transferTagAssignment("tagOpportunityAssignment", "OPPORTUNITY");
//        mas.transferTagAssignment("tagPersonAssignment", "PERSON");
// ToDo testen
        mas.updateSequenceCounter( "OFFER", "OFFERS-1", "sequencecounter");
        mas.updateSequenceCounter( "company", "COMPANIES-1", "table");  // im CRM keine sequence-Number für Company
        LocalDateTime endLdt = LocalDateTime.now();
        Long seconds = ChronoUnit.SECONDS.between(startLdt, endLdt);
        System.out.println("Ende Migration CRM --> Woody, Zeitdauer = " + seconds + " sec.");
        System.out.println("=======================================================================================");
        run = false;
    }



    @Override
    public void deleteWoody() {
        if (run) {
            return;
        }

        Database dbWoody = databases.get("mixing");

        System.out.println("Start delete Woody" );
        mts.deleteContentOfTable(dbWoody, "phonecall");
        mts.deleteContentOfTable(dbWoody, "tagassignment");
        mts.deleteContentOfTable(dbWoody, "tag");
        mts.deleteContentOfTable(dbWoody, "notemail");
        mts.deleteContentOfTable(dbWoody, "comment");
        mts.deleteContentOfTable(dbWoody, "mail");
        mts.deleteContentOfTable(dbWoody, "opportunity");
        mts.deleteContentOfTable(dbWoody, "offerItem");
        mts.deleteContentOfTable(dbWoody, "offer");
        mts.deleteContentOfTable(dbWoody, "contract");
        mts.deleteContentOfTable(dbWoody, "useraccount");
        mts.deleteContentOfTable(dbWoody, "packageDefinition");
        mts.deleteContentOfTable(dbWoody, "product");
        mts.deleteContentOfTable(dbWoody, "person");
        mts.deleteContentOfTable(dbWoody, "industry");

        System.out.println("Ende delete Woody" );

        run = false;
    }



}
