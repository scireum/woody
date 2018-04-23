/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.db.mixing.Column;
import sirius.db.mixing.EntityRef;
import sirius.db.mixing.Schema;
import sirius.db.mixing.annotations.AfterDelete;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.NullAllowed;
import woody.core.relations.Relation;
import woody.organization.BasicElement;

/**
 * Created by aha on 13.01.17.
 */
@Index(name = "unique_path_lookup", columns = "uniquePath")
public class Unit extends BasicElement<UnitType> {

    public static final Column PARENT = Column.named("parent");
    @NullAllowed
    private final EntityRef<Unit> parent = EntityRef.on(Unit.class, EntityRef.OnDelete.REJECT);

    public static final Column UNIQUE_PATH = Column.named("uniquePath");
    @Length(150)
    private String uniquePath;

    @Override
    protected EntityRef<UnitType> initializeTypeRef() {
        return EntityRef.on(UnitType.class, EntityRef.OnDelete.REJECT);
    }

    @AfterDelete
    protected void checkChildren() {
        oma.select(Relation.class).eq(Relation.TARGET, getUniquePath()).delete();
    }

    @Override
    public String getTargetString() {
        return Schema.getNameForType(Unit.class) + "-" + getUniquePath()+"*";
    }

    public EntityRef<Unit> getParent() {
        return parent;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
    }
}
