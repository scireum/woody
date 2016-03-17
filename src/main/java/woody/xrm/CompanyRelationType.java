/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm;

import sirius.kernel.nls.NLS;

public enum CompanyRelationType {
	MEMBER,				// Mitglied
	SUBSIDIARY,			// Tochterunternehmen
	SERVICEPROVIDER;	// Dienstleister

	@Override
	public String toString() {
		return NLS.get(CompanyRelationType.class.getName() + "." + name());
	}

}
