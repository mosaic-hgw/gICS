package org.emau.icmvc.ganimed.ttp.cm2.util;

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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleQRCodecTest
{
	private static final String M_02 = "m1";
	private static final String M_04 = "modu";
	private static final String M_06 = "modul1";
	private static final String M_09 = "modul1234";
	private static final String M_11 = "modul11-123";
	private static final String M_12 = "modul12-1234";
	private static final String M_16 = "modul16-12345678";
	private static final String M_17 = "modul17-123456789";
	private static final String M_15 = "modul1234567890";
	private static final String M_25 = "modul12345678901234567890";
	private static final String M_30 = "modul1234567890123456789012345";

	private static final Set<String> KNOWN_MODULES =
			Set.of(M_04, M_02, M_06, M_09, M_11, M_12, M_15, M_16, M_17, M_25, M_30);

	// Define the expected QR code for each module name
	private static final String Q_02 = "m1_###_ca0df2c95";
	private static final String Q_04 = "modu_###_4de5202";
	private static final String Q_06 = "modul1_###_d82cb";
	private static final String Q_09 = "modul1234_###_7f";
	private static final String Q_11 = "modul11-123_###_";
	private static final String Q_12 = "modul12-1234    ";
	private static final String Q_15 = "modul1234567890 ";
	private static final String Q_16 = M_16;
	private static final String Q_17 = "modul17-12345...";
	private static final String Q_25 = "modul12345678...";
	private static final String Q_30 = Q_25;

	// Define a module name that is not in the set
	private static final String UNKNOWN_MODULE_NAME = "modul2";

	// Define a QR code that is not in the map
	private static final String UNKNOWN_QR_CODE = "modul2_###_18652";

	// Define a QR code that is ambiguous
	private static final String AMBIGUOUS_QR_CODE = Q_25;

	// Define the QR code length
	private static final int QR_CODE_LENGTH = 16;

	// Create an instance of the codec
	private static final ModuleQRCodec CODEC = new ModuleQRCodec(QR_CODE_LENGTH);

	@Test
	public void testFromQRCodeToModuleName()
	{
		// Test the conversion from QR code to module name
		assertEquals(M_02, CODEC.fromQRCodeToModuleName(Q_02, KNOWN_MODULES));
		assertEquals(M_04, CODEC.fromQRCodeToModuleName(Q_04, KNOWN_MODULES));
		assertEquals(M_06, CODEC.fromQRCodeToModuleName(Q_06, KNOWN_MODULES));
		assertEquals(M_09, CODEC.fromQRCodeToModuleName(Q_09, KNOWN_MODULES));
		assertEquals(M_11, CODEC.fromQRCodeToModuleName(Q_11, KNOWN_MODULES));
		assertEquals(M_12, CODEC.fromQRCodeToModuleName(Q_12, KNOWN_MODULES));
		assertEquals(M_15, CODEC.fromQRCodeToModuleName(Q_15, KNOWN_MODULES));
		assertEquals(M_16, CODEC.fromQRCodeToModuleName(Q_16, KNOWN_MODULES));
		assertEquals(M_17, CODEC.fromQRCodeToModuleName(Q_17, KNOWN_MODULES));
	}

	@Test
	public void testUnknownFromQRCodeToModuleName()
	{
		// Test the conversion for an unknown QR code
		assertNull(CODEC.fromQRCodeToModuleName(UNKNOWN_QR_CODE, KNOWN_MODULES));

		HashSet<String> modules = new HashSet<>(KNOWN_MODULES);
		modules.add(UNKNOWN_MODULE_NAME);
		assertEquals(UNKNOWN_MODULE_NAME, CODEC.fromQRCodeToModuleName(UNKNOWN_QR_CODE, modules));
	}

	@Test
	public void testAmbiguousFromQRCodeToModuleName()
	{
		assertNotEquals(M_25, M_30);
		assertEquals(Q_25, Q_30);

		HashSet<String> modulesWithUniqQRCodes = new HashSet<>(KNOWN_MODULES);
		modulesWithUniqQRCodes.remove(M_30);

		assertTrue(modulesWithUniqQRCodes.contains(M_25));
		assertEquals(M_25, CODEC.fromQRCodeToModuleName(Q_25, modulesWithUniqQRCodes));

		assertTrue(KNOWN_MODULES.contains(M_25));
		assertTrue(KNOWN_MODULES.contains(M_30));

		// Test the conversion for an ambiguous QR code returns null
		assertNull(CODEC.fromQRCodeToModuleName(Q_25, KNOWN_MODULES));
	}

	@Test
	public void testToQRCodeFromModuleName()
	{
		// Test the conversion from module name to QR code
		assertEquals(Q_02, CODEC.toQRCodeFromModuleName(M_02));
		assertEquals(Q_04, CODEC.toQRCodeFromModuleName(M_04));
		assertEquals(Q_06, CODEC.toQRCodeFromModuleName(M_06));
		assertEquals(Q_09, CODEC.toQRCodeFromModuleName(M_09));
		assertEquals(Q_11, CODEC.toQRCodeFromModuleName(M_11));
		assertEquals(Q_12, CODEC.toQRCodeFromModuleName(M_12));
		assertEquals(Q_15, CODEC.toQRCodeFromModuleName(M_15));
		assertEquals(Q_16, CODEC.toQRCodeFromModuleName(M_16));
		assertEquals(Q_17, CODEC.toQRCodeFromModuleName(M_17));
		assertEquals(Q_25, CODEC.toQRCodeFromModuleName(M_25));
		assertEquals(Q_30, CODEC.toQRCodeFromModuleName(M_30));
		assertEquals(UNKNOWN_QR_CODE, CODEC.toQRCodeFromModuleName(UNKNOWN_MODULE_NAME));
	}

	@Test
	public void testAmbiguousToQRCodeFromModuleName()
	{
		assertNotEquals(M_25, M_30);
		assertEquals(CODEC.toQRCodeFromModuleName(M_25), CODEC.toQRCodeFromModuleName(M_30));
	}
}
