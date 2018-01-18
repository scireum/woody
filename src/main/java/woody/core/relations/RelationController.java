/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.relations;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import sirius.biz.web.BizController;
import sirius.db.mixing.Constraint;
import sirius.db.mixing.Entity;
import sirius.db.mixing.Schema;
import sirius.db.mixing.constraints.FieldOperator;
import sirius.db.mixing.constraints.Or;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.web.controller.AutocompleteHelper;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.services.JSONStructuredOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by aha on 11.01.17.
 */
@Register(classes = {Controller.class, RelationController.class})
public class RelationController extends BizController {

    @Part
    private Schema schema;

    @Part
    private GlobalContext context;

    private static String relationsSecret;

    public static String computeAuthHash(String objectId) {
        if (relationsSecret == null) {
            relationsSecret = Strings.generateCode(32);
        }

        long unixTimeInDays = TimeUnit.DAYS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        return Hashing.md5()
                      .hashString(objectId + relationsSecret + String.valueOf(unixTimeInDays), Charsets.UTF_8)
                      .toString();
    }



    @LoginRequired
    @Routed("/relations/autocomplete/:1")
    public void relationsAutocomplete(final WebContext ctx, String type) {
        AutocompleteHelper.handle(ctx, (query, result) -> {
            if (Strings.isEmpty(query)) {
                return;
            }
            oma.select(RelationType.class)
               .eq(RelationType.TENANT, tenants.getRequiredTenant())
               .where(Or.of(parseSourceTypeConstraints(type)))
               .orderAsc(RelationType.SOURCE_TYPE)
               .orderAsc(RelationType.TARGET_TYPE)
               .iterateAll(relationType -> {
                   if (relationType.getTargetType() == null) {
                       for (RelationProvider provider : context.getParts(RelationProvider.class)) {
                           provider.computeTargetSuggestions(null, query, suggestion -> {
                               result.accept(new AutocompleteHelper.Completion(relationType.getIdAsString()
                                                                               + ":"
                                                                               + suggestion.getFirst(),
                                                                               relationType.getName()
                                                                               + ": "
                                                                               + suggestion.getSecond(),
                                                                               relationType.getName()
                                                                               + ": "
                                                                               + suggestion.getSecond()));
                           });
                       }
                   } else {
                       Tuple<String, String> mainAndSubType = Strings.split(relationType.getTargetType(), "-");
                       RelationProvider provider = context.findPart(mainAndSubType.getFirst(), RelationProvider.class);
                       provider.computeTargetSuggestions(mainAndSubType.getSecond(), query, suggestion -> {
                           result.accept(new AutocompleteHelper.Completion(relationType.getIdAsString()
                                                                           + ":"
                                                                           + suggestion.getFirst(),
                                                                           relationType.getName()
                                                                           + ": "
                                                                           + suggestion.getSecond(),
                                                                           relationType.getName()
                                                                           + ": "
                                                                           + suggestion.getSecond()));
                       });
                   }
               });
        });
    }

    private List<Constraint> parseSourceTypeConstraints(String typeExpression) {
        List<Constraint> result = new ArrayList<>(3);
        result.add(FieldOperator.on(RelationType.SOURCE_TYPE).eq(null));
        for (String type : typeExpression.split(",")) {
            type = type.trim();
            result.add(FieldOperator.on(RelationType.SOURCE_TYPE).eq(type));
        }

        return result;
    }

    @Routed(value = "/relations/add/:1", jsonCall = true)
    public void addRelation(final WebContext ctx, JSONStructuredOutput out, String objectId) {
        Entity owner = oma.resolveOrFail(objectId);
        if (!Strings.areEqual(ctx.get("authHash").asString(), computeAuthHash(objectId))) {
            throw Exceptions.createHandled().withSystemErrorMessage("Security hash does not match!").handle();
        }
        if (!(owner instanceof HasRelations)) {
            throw Exceptions.handle().withSystemErrorMessage("%s cannot own relations!", owner).handle();
        }

        String relationDescription = ctx.get("relationId").asString();

        Tuple<String, String> typeAndId = Strings.split(relationDescription, ":");
        RelationType type = oma.findOrFail(RelationType.class, typeAndId.getFirst());
        assertTenant(type);

        Relation relation = new Relation();
        relation.setOwnerId(owner.getId());
        relation.setOwnerType(owner.getTypeName());
        relation.getType().setValue(type);
        relation.setTarget(typeAndId.getSecond());
        oma.update(relation);

        if (!type.isMultiple()) {
            oma.select(Relation.class)
               .eq(Relation.OWNER_ID, owner.getId())
               .eq(Relation.OWNER_TYPE, owner.getTypeName())
               .eq(Relation.TYPE, type)
               .where(FieldOperator.on(Relation.ID).notEqual(relation.getId()))
               .delete();
        }

        out.property("refresh", true);
    }

    @Routed(value = "/relations/delete/:1", jsonCall = true)
    public void deleteRelation(final WebContext ctx, JSONStructuredOutput out, String relationId) {
        Relation relation = oma.find(Relation.class, relationId).orElse(null);

        if (relation == null) {
            return;
        }

        if (Strings.areEqual(ctx.get("authHash").asString(),
                             computeAuthHash(relation.getOwnerType() + "-" + relation.getOwnerId()))) {
            oma.delete(relation);
            out.property("refresh", true);
        }
    }
}
