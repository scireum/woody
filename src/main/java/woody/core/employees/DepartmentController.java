/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.employees;

import sirius.biz.model.PersonData;
import sirius.biz.tenants.UserAccount;
import sirius.biz.tenants.UserAccountController;
import sirius.biz.web.BizController;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.Column;
import sirius.db.mixing.SmartQuery;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import woody.xrm.Person;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class DepartmentController extends BizController {

    @Routed("/departments")
    @LoginRequired
    @Permission(UserAccountController.PERMISSION_MANAGE_USER_ACCOUNTS)
    public void departments(WebContext ctx) {
        SmartQuery<Department> query = oma.select(Department.class)
                                          .fields(Department.ID,
                                                  Department.NAME,
                                                  Department.CODE,
                                                  Department.PARENT.join(Department.NAME),
                                                  Department.PARENT.join(Department.CODE),
                                                  Department.SUPERVISOR.join(Person.PERSON.inner(PersonData.TITLE)),
                                                  Department.SUPERVISOR.join(Person.PERSON.inner(PersonData.FIRSTNAME)),
                                                  Department.SUPERVISOR.join(Person.PERSON.inner(PersonData.LASTNAME)),
                                                  Department.SUPERVISOR.join(Person.PERSON.inner(PersonData.SALUTATION)))
                                          .eq(Department.TENANT, tenants.getRequiredTenant())
                                          .orderAsc(Department.CODE);
        PageHelper<Department> ph = PageHelper.withQuery(query);
        ph.withContext(ctx);
        ph.withSearchFields(Department.NAME,
                            Department.CODE,
                            Department.PARENT.join(Department.NAME),
                            Department.PARENT.join(Department.CODE));
        ctx.respondWith().template("view/core/employee/departments.html", ph.asPage());
    }

    @LoginRequired
    @Permission(UserAccountController.PERMISSION_MANAGE_USER_ACCOUNTS)
    @Routed("/department/:1")
    public void department(WebContext ctx, String departmentId) {
        Department department = findForTenant(Department.class, departmentId);
        boolean isNew = department.isNew();
        if (isNew) {
            department.getTenant().setValue(currentTenant());
        }
        save(ctx, department);
        if (!department.isNew() && isNew) {
            ctx.respondWith().redirectTemporarily("/department/" + department.getId());
            return;
        }

        ctx.respondWith()
           .template("view/core/employee/department.html",
                     department,
                     oma.select(Department.class)
                        .eq(Department.TENANT, currentTenant())
                        .where(FieldOperator.on(Department.ID).notEqual(department.getId()))
                        .orderAsc(Department.CODE)
                        .queryList(),
                     oma.select(UserAccount.class)
                        .eq(UserAccount.TENANT, currentTenant())
                        .orderAsc(Column.mixin(Employee.class).inner(Employee.EMPLOYEE_NUMBER))
                        .queryList());
    }
}
