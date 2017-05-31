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
 * Provides the handlers used to create, edit or delete a comment.
 * <p>
 * To perform autorisation (we don't know who may comment on what), a shared secret is computed and provided here.
 * This is used to generate an autorisation hash which is checked here. Clients of the comments framework can user
 * {@link Commented#getAuthHash()} to embed commenting functionalities into their UI. The <tt>comments.html</tt> part
 * takes care of that for most cases.
 */
@Register(classes = Controller.class)
public class CommentsController extends BizController {

    private static String commentsSecret;

    /**
     * Used to compute an autorisation hash for a given object based un its unique object name.
     * <p>
     * Clients of the comments framework can use this to compute hashes to permit access to the APIs provided below.
     *
     * @param objectId the unique object name of the object to comment on
     * @return an autorization hash which is valid for the given object and one day.
     */
    public static String computeAuthHash(String objectId) {
        if (commentsSecret == null) {
            commentsSecret = Strings.generateCode(32);
        }

        long unixTimeInDays = TimeUnit.DAYS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        return Hashing.md5()
                      .hashString(objectId + commentsSecret + String.valueOf(unixTimeInDays), Charsets.UTF_8)
                      .toString();
    }

    /**
     * Handles a new comment for an object.
     *
     * @param ctx    the REST request
     * @param object the unique object name of the object to comment on
     */
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

    /**
     * Handles an edit request for a comment.
     *
     * @param ctx       the REST request
     * @param commentId the id of the comment to modify
     */
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

    /**
     * Handles a delete request for a comment.
     *
     * @param ctx       the REST request
     * @param commentId the id of the comment to delete (or mark as deleted).
     */
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
