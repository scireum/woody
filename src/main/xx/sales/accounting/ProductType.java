/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.accounting;

import sirius.kernel.nls.NLS;

public enum ProductType {
	
	LICENSE, // Lizenz
	SERVICE; // Dienstleistung

	@Override
	public String toString() {
		return NLS.get(ProductType.class.getSimpleName() + "." + name());
	}

}
