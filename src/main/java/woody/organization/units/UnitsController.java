/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import com.google.common.base.Strings;
import sirius.biz.sequences.Sequences;
import sirius.biz.web.BizController;
import sirius.biz.web.MagicSearch;
import sirius.biz.web.PageHelper;
import sirius.db.mixing.SmartQuery;
import sirius.db.mixing.constraints.Like;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.controller.AutocompleteHelper;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.Permission;
import sirius.web.security.UserContext;
import sirius.web.services.JSONStructuredOutput;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

import java.util.Optional;

/**
 * Created by aha on 09.05.15.
 */
@Register(classes = Controller.class)
public class UnitsController extends BizController {

    private static final String PERMISSION_MANAGE_UNITS = "permission-manage-units";

    @Routed("/units")
    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    public void units(WebContext ctx) {
        MagicSearch search = MagicSearch.parseSuggestions(ctx);
        SmartQuery<Unit> query = oma.select(Unit.class)
                                    .fields(Unit.ID,
                                            Unit.NAME,
                                            Unit.CODE,
                                            Unit.TYPE.join(UnitType.NAME),
                                            Unit.PARENT.join(Unit.NAME),
                                            Unit.PARENT.join(Unit.TYPE).join(UnitType.NAME))
                                    .orderAsc(Unit.CODE)
                                    .orderAsc(Unit.NAME);
        search.applyQueries(query, Unit.NAME, Unit.CODE, Unit.TYPE.join(UnitType.NAME));
        Tagged.applyTagSuggestions(Unit.class, search, query);
        Relations.applySuggestions(Unit.class, search, query);
        PageHelper<Unit> ph = PageHelper.withQuery(query).forCurrentTenant();
        ph.withContext(ctx);
        ctx.respondWith().template("view/core/units/units.html", ph.asPage(), search.getSuggestionsString());
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed(value = "/units/suggest", jsonCall = true)
    public void unitsSuggest(WebContext ctx, JSONStructuredOutput out) {
        MagicSearch.generateSuggestions(ctx, (q, c) -> {
            Tagged.computeSuggestions(Unit.class, q, c);
            Relations.computeSuggestions(Unit.class, q, c);
        });
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed("/units/autocomplete")
    public void autocomplete(WebContext ctx) {
        AutocompleteHelper.handle(ctx,
                                  (query, result) -> oma.select(Unit.class)
                                                        .eq(Unit.TENANT, currentTenant())
                                                        .where(Like.allWordsInAnyField(query,
                                                                                       Unit.CODE,
                                                                                       Unit.NAME,
                                                                                       Unit.TYPE.join(UnitType.NAME)))
                                                        .orderAsc(Unit.NAME)
                                                        .iterateAll(unit -> result.accept(new AutocompleteHelper.Completion(
                                                                unit.getIdAsString(),
                                                                unit.getName(),
                                                                null))));
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed("/unit/:1/delete")
    public void deleteUnit(WebContext ctx, String id) {
        Optional<Unit> cl = tryFindForTenant(Unit.class, id);
        if (cl.isPresent()) {
            oma.delete(cl.get());
            showDeletedMessage();
        }
        units(ctx);
    }

    @LoginRequired
    @Permission(PERMISSION_MANAGE_UNITS)
    @Routed("/unit/:1")
    public void editUnit(WebContext ctx, String unitId) {
        Unit unit = findForTenant(Unit.class, unitId);
        if (ctx.isPOST()) {
            try {
                boolean wasNew = unit.isNew();
                if (unit.isNew()) {
                    unit.getTenant().setValue(tenants.getRequiredTenant());
                    load(ctx, unit, Unit.TYPE, Unit.PARENT);
                    computeUniquePath(unit);
                }
                load(ctx, unit);

                oma.update(unit);
                unit.getTags().updateTagsToBe(ctx.getParameters("tags"), false);
                showSavedMessage();
                if (wasNew) {
                    ctx.respondWith().redirectTemporarily(WebContext.getContextPrefix() + "/unit/" + unit.getId());
                    return;
                }
            } catch (Throwable e) {
                UserContext.handle(e);
            }
        }
        ctx.respondWith()
           .template("view/core/units/unit.html",
                     unit,
                     oma.select(UnitType.class)
                        .eq(UnitType.TENANT, currentTenant())
                        .orderAsc(UnitType.NAME)
                        .queryList());
    }

    @Part
    private Sequences sequences;

    protected void computeUniquePath(Unit unit) {
        if (unit.getParent().isFilled()) {
            unit.setUniquePath(unit.getParent().getValue().getUniquePath() + encode(sequences.generateId(
                    "units-counter-" + tenants.getRequiredTenant().getIdAsString())));
        } else {
            unit.setUniquePath(encode(unit.getTenant().getId()));
        }
    }

    private String encode(long id) {
        String code = Strings.padStart(Long.toString(id, 36), 3, '0');
        if (code.length() != 3) {
            throw Exceptions.createHandled().withNLSKey("Unit.cannotGenerateCode").handle();
        }

        return code;
    }
}
