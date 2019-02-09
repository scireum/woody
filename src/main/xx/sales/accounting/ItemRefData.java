/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.NullAllowed;

public class ItemRefData extends ItemData {

    public static final Mapping ORIGINAL_ITEM = Mapping.named("originalItem");
    @NullAllowed
    private final SQLEntityRef<Item> originalItem = SQLEntityRef.on(Item.class, SQLEntityRef.OnDelete.SET_NULL);

    public SQLEntityRef<Item> getOriginalItem() {
        return originalItem;
    }
}
