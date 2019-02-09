/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales.opportunities;

import sirius.kernel.nls.NLS;

public enum OpportunityState {
	OPEN, ACCEPTED, REJECTED, CLOSED;

	@Override
	public String toString() {
		return NLS.get(OpportunityState.class.getSimpleName() + "." + name());
	}

}
