/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.opportunities;

import sirius.kernel.nls.NLS;

public enum OpportunityState {
//	OPEN, ACCEPTED, REJECTED, CLOSED;
	COLD,          // neuer Status kalt
	WARM,          // neuer Status warm
	QUALIFIED,     // neuer Status qualifiziert
	CONTACTED,     // neuer Status kontaktiert
	APPOINTMENT,   // neuer Status Termin vereinbart
	ADVICE,        // neuer Status Beratung
	NEGOTIATION,   // neuer Status Vertrag
	CLOSE,         // neuer Status geschlossen
	FAIL;          // neuer Status verloren

	@Override
	public String toString() {
		return NLS.get(OpportunityState.class.getSimpleName() + "." + name());
	}

	private static final OpportunityState[] statesColdToNegotiation =
			{	COLD,          // neuer Status kalt
				WARM,          // neuer Status warm
				QUALIFIED,     // neuer Status qualifiziert
				CONTACTED,     // neuer Status kontaktiert
				APPOINTMENT,   // neuer Status Termin vereinbart
				ADVICE,        // neuer Status Beratung
				NEGOTIATION};  // neuer Status Vertrag

	public static final OpportunityState[] getStatesColdToNegotiation() {
		return statesColdToNegotiation;
	}
}
