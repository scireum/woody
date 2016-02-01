/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.tenants.Tenants;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.mixing.Composite;
import sirius.mixing.Entity;
import sirius.mixing.OMA;
import sirius.mixing.annotations.BeforeDelete;
import sirius.mixing.annotations.Transient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by aha on 25.11.15.
 */
public class Tagged extends Composite {

    @Transient
    protected final Entity owner;

    public Tagged(Entity owner) {
        this.owner = owner;
    }

    @Part
    private static OMA oma;

    @Part
    private static Tenants tenants;

    @BeforeDelete
    protected void onDelete() {
        if (owner != null && !owner.isNew()) {
            oma.select(TagAssignment.class).eq(TagAssignment.TARGET_ENTITY, owner.getUniqueName()).delete();
        }
    }

    public List<Tag> getTags() {
        return getAssignedTags().stream().map(ta -> ta.getTag().getValue()).collect(Collectors.toList());
    }

    protected List<TagAssignment> getAssignedTags() {
        return oma.select(TagAssignment.class)
                  .orderAsc(TagAssignment.TAG.join(Tag.NAME))
                  .eq(TagAssignment.TARGET_ENTITY, owner.getUniqueName())
                  .queryList();
    }

    public String getTagsAsString() {
        return getTags().stream().map(Tag::getName).collect(Collectors.joining(", "));
    }

    protected Tag resolveTag(String tagName, String targetType, boolean autoCreate) {
        Tag tag = oma.select(Tag.class)
                     .eq(Tag.TENANT, tenants.getRequiredTenant())
                     .eq(Tag.UNQIUE_NAME, Tag.getUnqiueName(tagName))
                     .eq(Tag.TARGET_TYPE, targetType)
                     .queryFirst();
        if (tag == null && autoCreate) {
            tag = new Tag();
            tag.setName(tagName);
            tag.setTargetType(targetType);
            tag.getTenant().setValue(tenants.getRequiredTenant());
            oma.update(tag);
        }

        return tag;
    }

    public void updateTagsToBe(Set<Tag> tags) {
        if (owner.isNew()) {
            throw new IllegalStateException("owner must not be new");
        }
        Set<Long> uniqueTags = tags.stream().filter(Objects::nonNull).map(Tag::getId).collect(Collectors.toSet());
        List<TagAssignment> currentTags = getAssignedTags();
        for (TagAssignment tagAssignment : currentTags) {
            if (!uniqueTags.contains(tagAssignment.getTag().getId())) {
                oma.forceDelete(tagAssignment);
            } else {
                uniqueTags.remove(tagAssignment.getTag().getId());
            }
        }
        for (Long tag : uniqueTags) {
            TagAssignment ta = new TagAssignment();
            ta.getTag().setId(tag);
            ta.setTargetEntity(owner.getUniqueName());
            oma.update(ta);
        }
    }

    public void updateTagsToBe(Collection<String> tags, boolean autocreate) {
        Set<Tag> resolvedTags = tags.stream()
                                    .filter(Strings::isFilled)
                                    .map(name -> resolveTag(name, owner.getTypeName(), autocreate))
                                    .collect(Collectors.toSet());

        updateTagsToBe(resolvedTags);
    }

    public List<String> parseTagsString(String tagString) {
        return Arrays.asList(tagString.split("[\n\\.,;\\s]"))
                     .stream()
                     .filter(Strings::isFilled)
                     .filter(s -> s.startsWith("#"))
                     .map(s -> s.substring(1))
                     .collect(Collectors.toList());
    }

    public void addTagExisting(String tag) {
        Tag resolvedTag = resolveTag(tag, owner.getTypeName(), false);
        addTag(resolvedTag);
    }

    public void addOrCreateTag(String tag) {
        Tag resolvedTag = resolveTag(tag, owner.getTypeName(), true);
        addTag(resolvedTag);
    }

    public void addTag(Tag tag) {
        if (tag != null) {
            if (owner.isNew()) {
                throw new IllegalStateException("owner must not be new");
            }
            if (!oma.select(TagAssignment.class)
                    .eq(TagAssignment.TARGET_ENTITY, owner.getUniqueName())
                    .eq(TagAssignment.TAG, tag)
                    .exists()) {
                TagAssignment ta = new TagAssignment();
                ta.setTargetEntity(owner.getUniqueName());
                ta.getTag().setValue(tag);
                oma.update(ta);
            }
        }
    }

    public void removeTag(String tag) {
        Tag resolvedTag = resolveTag(tag, owner.getTypeName(), false);
        removeTag(resolvedTag);
    }

    public void removeTag(Tag tag) {
        if (tag != null && !owner.isNew()) {
            oma.select(TagAssignment.class)
               .eq(TagAssignment.TARGET_ENTITY, owner.getUniqueName())
               .eq(TagAssignment.TAG, tag)
               .delete();
        }
    }
}
