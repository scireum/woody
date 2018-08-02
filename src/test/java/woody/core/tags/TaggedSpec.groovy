/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags

import sirius.db.jdbc.OMA
import sirius.kernel.BaseSpecification
import sirius.kernel.di.std.Part

/**
 * Created by aha on 29.11.15.
 */
class TaggedSpec extends BaseSpecification {

    @Part
    private static OMA oma

    def "ensure addTag adds a tag once"() {
        given:
        TenantsHelper.installTestTenant()
        and:
        TaggedTestEntity entity = new TaggedTestEntity()
        and:
        oma.update(entity)
        when:
        entity.getTagged().addOrCreateTag("Test")
        and:
        entity.getTagged().addOrCreateTag("Test")
        then:
        oma.select(Tag.class).eq(Tag.TARGET_TYPE, entity.getTypeName()).eq(Tag.NAME, "Test").exists()
        and:
        entity.getTagged().getTags().size() == 1
    }

    def "ensure updateTags works without autocreating tags"() {
        given:
        TenantsHelper.installTestTenant()
        and:
        TaggedTestEntity entity = new TaggedTestEntity()
        and:
        oma.update(entity)
        when:
        entity.getTagged().updateTagsToBe(entity.getTagged().parseTagsString("#Test #Test #This,#That"), false)
        then:
        oma.select(Tag.class).eq(Tag.TARGET_TYPE, entity.getTypeName()).eq(Tag.NAME, "Test").exists()
        and:
        entity.getTagged().getTags().size() == 1
    }

    def "ensure updateTags works with autocreating tags"() {
        given:
        TenantsHelper.installTestTenant()
        and:
        TaggedTestEntity entity = new TaggedTestEntity()
        and:
        oma.update(entity)
        when:
        entity.getTagged().updateTagsToBe(entity.getTagged().parseTagsString("#Test #Test #This,#That"), true)
        then:
        oma.select(Tag.class).eq(Tag.TARGET_TYPE, entity.getTypeName()).eq(Tag.NAME, "This").exists()
        and:
        oma.select(Tag.class).eq(Tag.TARGET_TYPE, entity.getTypeName()).eq(Tag.NAME, "That").exists()
        and:
        entity.getTagged().getTagsAsString() == "Test, That, This"
    }

}
