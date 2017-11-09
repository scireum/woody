/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments

import sirius.db.mixing.OMA
import sirius.kernel.BaseSpecification
import sirius.kernel.di.std.Part

/**
 * Created by aha on 29.11.15.
 */
class CommentedSpec extends BaseSpecification {

    @Part
    private static OMA oma;

    def "add a public and private comments work"() {
        given:
        TenantsHelper.installTestTenant();
        and:
        CommentedTestEntity entity = new CommentedTestEntity();
        and:
        oma.update(entity);
        when:
        entity.getCommented().addComment("Test", "Test", "This is a test", false);
        and:
        entity.getCommented().addCommentAsCurrentUser("This is another test")
        and:
        entity.getCommented().addPublicCommentAsCurrentUser("This is the 3rd test")
        and:
        TenantsHelper.clearCurrentUser();
        then:
        oma.select(Comment.class).eq(Comment.TARGET_ENTITY, entity.getUniqueName()).count() == 3;
        and:
        entity.getCommented().getAllComments().size() == 3
        and:
        entity.getCommented().getPublicComments().size() == 1
    }

    def "user can edit own comments"() {
        given:
        TenantsHelper.installTestTenant();
        and:
        CommentedTestEntity entity = new CommentedTestEntity();
        and:
        oma.update(entity);
        when:
        entity.getCommented().addCommentAsCurrentUser("This is a test");
        then: "A comment created by the current user is considered public even if it isn't publicVisible"
        entity.getCommented().getAllComments().size() == entity.getCommented().getPublicComments().size()
        and:
        entity.getCommented().getAllComments().get(0).canBeEditedByCurrentUser()
    }

}
