package org.emau.icmvc.magic.fhir.datatypes;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
 * 							kontakt-ths@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel, c.hampf
 * 							web client
 * 							a.blumentritt, m.bialke, f.m.moser
 * 							fhir-api
 * 							m.bialke
 * 							docker
 * 							r. schuldt
 * 
 * 							The gICS was developed by the University Medicine Greifswald and published
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12967-020-02457-y
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * 							http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import org.junit.jupiter.api.Test;

import static org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus.ACCEPTED;
import static org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus.NOT_CHOSEN;
import static org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus.WITHDRAWN;
import static org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus.valueOfIncludingObsolete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignedConsentModuleTest
{

	@Test
	public void testPatientConsentStatus()
	{
		assertNull(ACCEPTED.getObsolete());
		assertNotNull(ACCEPTED.toString());
		assertEquals(ACCEPTED, valueOfIncludingObsolete(ACCEPTED.toString()));

		assertEquals("REVOKED", WITHDRAWN.getObsolete());
		assertNotEquals(WITHDRAWN.toString(), WITHDRAWN.getObsolete());
		assertEquals(WITHDRAWN, valueOfIncludingObsolete(WITHDRAWN.toString()));
		assertEquals(WITHDRAWN, valueOfIncludingObsolete(WITHDRAWN.getObsolete()));

		assertEquals("NOT_CHOOSEN", NOT_CHOSEN.getObsolete());
		assertNotEquals(NOT_CHOSEN.toString(), NOT_CHOSEN.getObsolete());
		assertEquals(NOT_CHOSEN, valueOfIncludingObsolete(NOT_CHOSEN.toString()));
		assertEquals(NOT_CHOSEN, valueOfIncludingObsolete(NOT_CHOSEN.getObsolete()));

		assertThrows(IllegalArgumentException.class, () ->
				valueOfIncludingObsolete("Kenn ich nicht")
		);
	}
}
