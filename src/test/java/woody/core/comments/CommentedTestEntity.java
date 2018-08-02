/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.biz.jdbc.model.BizEntity;
import sirius.db.mixing.Mapping;

/**
 * Created by aha on 29.11.15.
 */
public class CommentedTestEntity extends BizEntity {

    private final Commented commented = new Commented(this);
    public static final Mapping COMMENTED = Mapping.named("commented");

    public Commented getCommented() {
        return commented;
    }
}
