/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.organization.units;

import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.Mixing;
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

    public static final Mapping PARENT = Mapping.named("parent");
    @NullAllowed
    private final SQLEntityRef<Unit> parent = SQLEntityRef.on(Unit.class, SQLEntityRef.OnDelete.REJECT);

    public static final Mapping UNIQUE_PATH = Mapping.named("uniquePath");
    @Length(150)
    private String uniquePath;

    @Override
    protected SQLEntityRef<UnitType> initializeTypeRef() {
        return SQLEntityRef.on(UnitType.class, SQLEntityRef.OnDelete.REJECT);
    }

    @AfterDelete
    protected void checkChildren() {
        oma.select(Relation.class).eq(Relation.TARGET, getUniquePath()).delete();
    }

    @Override
    public String getTargetString() {
        return Mixing.getNameForType(Unit.class) + "-" + getUniquePath() + "*";
    }

    public SQLEntityRef<Unit> getParent() {
        return parent;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
    }
}
