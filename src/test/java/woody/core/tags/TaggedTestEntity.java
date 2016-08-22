/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.tags;

import sirius.biz.model.BizEntity;
import sirius.db.mixing.Column;

/**
 * Created by aha on 29.11.15.
 */
public class TaggedTestEntity extends BizEntity {

    private final Tagged tagged = new Tagged(this);
    public static final Column TAGGED = Column.named("tagged");

    public Tagged getTagged() {
        return tagged;
    }
}
