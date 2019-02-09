/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.biz.jdbc.tenants.SQLTenantAware;
import sirius.db.mixing.Mapping;
import woody.core.comments.Commented;
import woody.core.comments.HasComments;
import woody.core.relations.HasRelations;
import woody.core.relations.Relations;
import woody.core.tags.Tagged;

public class Item extends SQLTenantAware implements HasComments, HasRelations {

    public static final Mapping ITEM = Mapping.named("item");
    private final ItemData item = new ItemData();

    private final Tagged tags = new Tagged(this);
    public static final Mapping TAGS = Mapping.named("tags");

    private final Commented comments = new Commented(this);
    public static final Mapping COMMENTS = Mapping.named("comments");

    private final Relations relations = new Relations(this);
    public static final Mapping RELATIONS = Mapping.named("relations");

    public Tagged getTags() {
        return tags;
    }

    @Override
    public Commented getComments() {
        return comments;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }
}
