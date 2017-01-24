/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import sirius.biz.tenants.UserAccount;
import sirius.biz.web.BizController;
import sirius.db.mixing.Entity;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Controller;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;

import java.util.concurrent.TimeUnit;

/**
 * Created by aha on 15.01.17.
 */
@Register(classes = Controller.class)
public class CommentsController extends BizController {

    private static String commentsSecret;

    public static String computeAuthHash(String objectId) {
        if (commentsSecret == null) {
            commentsSecret = Strings.generateCode(32);
        }

        long unixTimeInDays = TimeUnit.DAYS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        return Hashing.md5()
                      .hashString(objectId + commentsSecret + String.valueOf(unixTimeInDays), Charsets.UTF_8)
                      .toString();
    }

    @Routed("/comments/add/:1")
    public void addComment(final WebContext ctx, String object) {
        if (Strings.areEqual(ctx.get("authHash").asString(), computeAuthHash(object))) {
            Entity target = oma.resolveOrFail(object);
            if (target instanceof HasComments) {
                ((HasComments) target).getComments()
                                      .addComment(getUser().getUserObject(UserAccount.class).getPerson().toString(),
                                                  getUser().getUserId(),
                                                  ctx.get("comment").asString(),
                                                  ctx.get("publicVisible").asBoolean());
            }
        }
        ctx.respondWith().redirectToGet(ctx.get("redirectUrl").asString("/"));
    }

    @Routed("/comments/edit/:1")
    public void editComment(final WebContext ctx, String commentId) {
        Comment comment = oma.find(Comment.class, commentId).orElse(null);
        if (comment != null && comment.canBeEditedByCurrentUser()) {
            comment.setTextContent(ctx.get("comment").asString());
            comment.setPublicVisible(ctx.get("publicVisible").asBoolean());
            comment.setEdited(true);
            oma.update(comment);
        }

        ctx.respondWith().redirectToGet(ctx.get("redirectUrl").asString("/"));
    }

    @Routed("/comments/delete/:1")
    public void deleteComment(final WebContext ctx, String commentId) {
        Comment comment = oma.find(Comment.class, commentId).orElse(null);
        if (comment != null && comment.canBeEditedByCurrentUser()) {
            comment.setTextContent("");
            comment.setDeleted(true);
            oma.update(comment);
        }

        ctx.respondWith().redirectToGet(ctx.get("redirectUrl").asString("/"));
    }
}
