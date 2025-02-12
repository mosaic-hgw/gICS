package org.emau.icmvc.ganimed.ttp.cm2.version;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 * 
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12911-022-02081-4
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

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MajorMinorMaintenanceVersionConverterTest {
	VersionConverter vc;

	@BeforeEach
	void setup()
	{
		vc = new MajorMinorMaintenanceVersionConverter();
	}

	@Test
	void stringToInt() throws InvalidVersionException
	{
		assertEquals(1000000, vc.stringToInt("1.0.0"));
		assertEquals(2000001, vc.stringToInt("2.0.1"));
		assertEquals(2001000, vc.stringToInt("2.1.0"));
		assertEquals(3002001, vc.stringToInt("3.2.1"));
		assertEquals(2023002001, vc.stringToInt("2023.2.1"));
		assertEquals(2146999999, vc.stringToInt("2146.999.999"));
		assertThrows(InvalidVersionException.class, () -> vc.stringToInt("2147.0.0"));
	}

	@Test
	void intToString() throws InvalidVersionException
	{
		assertEquals("1.0.0", vc.intToString(1000000));
		assertEquals("2.0.1", vc.intToString(2000001));
		assertEquals("2.1.0", vc.intToString(2001000));
		assertEquals("3.2.1", vc.intToString(3002001));
		assertEquals("2023.2.1", vc.intToString(2023002001));
		assertEquals("2146.999.999", vc.intToString(2146999999));
		assertThrows(InvalidVersionException.class, () -> vc.intToString(2147000000));
	}

	@Test
	void extractRelevantParts() throws InvalidVersionException
	{
		assertEquals("2.3.4", vc.extractRelevantParts("2.3.4.5"));
		assertEquals("2.3.4", vc.extractRelevantParts("2.3.4"));
		assertThrows(InvalidVersionException.class, () -> vc.extractRelevantParts("2.3"));
	}
}
