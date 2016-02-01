/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.core.comments;

import sirius.biz.model.BizEntity;
import sirius.mixing.Column;

/**
 * Created by aha on 29.11.15.
 */
public class CommentedTestEntity extends BizEntity {

    private final Commented commented = new Commented(this);
    public static final Column COMMENTED = Column.named("commented");

    public Commented getCommented() {
        return commented;
    }
}
