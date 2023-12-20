package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyValueHelperTest
{
	private static final String UNIQUE_KEYS = "A=1;B=2;C=3;D=4";
	private static final String AMBIGUOUS_KEYS = "A=1;A=2;A=3;B=4";
	private static final String AMBIGUOUS_VALUES = "A=1;B=1;C=1;D=1";
	private static final String EMPTY_KEY_VALUE_STRING = "";
	private static final String NULL_KEY_VALUE_STRING = null;
	private static final String SINGLE_KEY_VALUE_STRING = "A";
	private static final String OTHER_ASS_SEP_VALUE_STRING = "A:1,B:2";

	private static final KeyValueHelper UNIQUE_KV = new KeyValueHelper(UNIQUE_KEYS);
	private static final KeyValueHelper AMBIGUOUS_K = new KeyValueHelper(AMBIGUOUS_KEYS);
	private static final KeyValueHelper AMBIGUOUS_V = new KeyValueHelper(AMBIGUOUS_VALUES);

	@Test
	public void testConstructorNull()
	{
		assertThrows(IllegalArgumentException.class, () ->
				new KeyValueHelper(NULL_KEY_VALUE_STRING));
	}

	@Test
	public void testEmptyKeyValuePairs()
	{
		assertEquals(0, (new KeyValueHelper(SINGLE_KEY_VALUE_STRING)).size());
		assertEquals(0, (new KeyValueHelper(EMPTY_KEY_VALUE_STRING)).size());
		assertEquals(0, (new KeyValueHelper(OTHER_ASS_SEP_VALUE_STRING)).size());
	}

	@Test
	public void testKeyValuePairsWithAssSep()
	{
		assertEquals(2, (new KeyValueHelper(OTHER_ASS_SEP_VALUE_STRING, ":", ",")).size());
	}

	@Test
	public void testSizes()
	{
		assertEquals(4, (new KeyValueHelper(UNIQUE_KEYS)).size());
		assertEquals(2, (new KeyValueHelper(AMBIGUOUS_KEYS)).size());
		assertEquals(4, (new KeyValueHelper(AMBIGUOUS_VALUES)).size());
		assertEquals(0, (new KeyValueHelper(EMPTY_KEY_VALUE_STRING)).size());
		assertEquals(0, (new KeyValueHelper(SINGLE_KEY_VALUE_STRING)).size());
		assertEquals(0, (new KeyValueHelper(OTHER_ASS_SEP_VALUE_STRING)).size());
		assertEquals(2, (new KeyValueHelper(OTHER_ASS_SEP_VALUE_STRING, ":", ",")).size());
	}

	@Test
	public void testGetUniqueKeyValuePairsIsOK()
	{
		assertEquals("1", UNIQUE_KV.getValueByKey("A"));
		assertEquals("2", UNIQUE_KV.getValueByKey("B"));
		assertEquals("3", UNIQUE_KV.getValueByKey("C"));
		assertEquals("4", UNIQUE_KV.getValueByKey("D"));

		assertEquals("A", UNIQUE_KV.getKeyByValue("1"));
		assertEquals("B", UNIQUE_KV.getKeyByValue("2"));
		assertEquals("C", UNIQUE_KV.getKeyByValue("3"));
		assertEquals("D", UNIQUE_KV.getKeyByValue("4"));

		assertNull(UNIQUE_KV.getValueByKey("1234"));
		assertNull(UNIQUE_KV.getKeyByValue("1234"));

		assertNull(AMBIGUOUS_K.getPairByKey("E"));
	}

	@Test
	public void testGetUniquePairsByKeyIsOK()
	{
		assertEquals("A", UNIQUE_KV.getPairByKey("A").getKey());
		assertEquals("B", UNIQUE_KV.getPairByKey("B").getKey());
		assertEquals("C", UNIQUE_KV.getPairByKey("C").getKey());
		assertEquals("D", UNIQUE_KV.getPairByKey("D").getKey());

		assertEquals("1", UNIQUE_KV.getPairByKey("A").getValue());
		assertEquals("2", UNIQUE_KV.getPairByKey("B").getValue());
		assertEquals("3", UNIQUE_KV.getPairByKey("C").getValue());
		assertEquals("4", UNIQUE_KV.getPairByKey("D").getValue());
	}

	@Test
	public void testGetUniquePairsByValueIsOK()
	{
		assertEquals("A", UNIQUE_KV.getPairByValue("1").getKey());
		assertEquals("B", UNIQUE_KV.getPairByValue("2").getKey());
		assertEquals("C", UNIQUE_KV.getPairByValue("3").getKey());
		assertEquals("D", UNIQUE_KV.getPairByValue("4").getKey());

		assertEquals("1", UNIQUE_KV.getPairByValue("1").getValue());
		assertEquals("2", UNIQUE_KV.getPairByValue("2").getValue());
		assertEquals("3", UNIQUE_KV.getPairByValue("3").getValue());
		assertEquals("4", UNIQUE_KV.getPairByValue("4").getValue());
	}

	@Test
	public void testContainsUniqueKeyIsOK()
	{
		assertTrue(UNIQUE_KV.containsKey("A"));
		assertTrue(UNIQUE_KV.containsKey("B"));
		assertTrue(UNIQUE_KV.containsKey("C"));
		assertTrue(UNIQUE_KV.containsKey("D"));

		assertTrue(UNIQUE_KV.containsValue("1"));
		assertTrue(UNIQUE_KV.containsValue("2"));
		assertTrue(UNIQUE_KV.containsValue("3"));
		assertTrue(UNIQUE_KV.containsValue("4"));

		assertFalse(UNIQUE_KV.containsKey("a"));
		assertFalse(UNIQUE_KV.containsKey("b"));
		assertFalse(UNIQUE_KV.containsKey("E"));
		assertFalse(UNIQUE_KV.containsKey("F"));
	}

	@Test
	public void testGetAmbiguousKPairsIsOK()
	{
		assertEquals("3", AMBIGUOUS_K.getValueByKey("A"));
		assertEquals("4", AMBIGUOUS_K.getValueByKey("B"));
		assertNull(AMBIGUOUS_K.getValueByKey("C"));

		assertEquals("A", AMBIGUOUS_K.getKeyByValue("3"));
		assertEquals("B", AMBIGUOUS_K.getKeyByValue("4"));

		assertNull(AMBIGUOUS_K.getValueByKey("1234"));
		assertNull(AMBIGUOUS_K.getKeyByValue("1234"));

		assertNull(AMBIGUOUS_K.getPairByKey("E"));
	}

	@Test
	public void testGetAmbiguousKPairsByKeyIsOK()
	{
		assertEquals("A", AMBIGUOUS_K.getPairByKey("A").getKey());
		assertEquals("B", AMBIGUOUS_K.getPairByKey("B").getKey());

		assertEquals("3", AMBIGUOUS_K.getPairByKey("A").getValue());
		assertEquals("4", AMBIGUOUS_K.getPairByKey("B").getValue());
	}

	@Test
	public void testGetAmbiguousKPairsByValueIsOK()
	{
		assertEquals("A", AMBIGUOUS_K.getPairByValue("3").getKey());
		assertEquals("B", AMBIGUOUS_K.getPairByValue("4").getKey());

		assertEquals("3", AMBIGUOUS_K.getPairByValue("3").getValue());
		assertEquals("4", AMBIGUOUS_K.getPairByValue("4").getValue());
	}

	@Test
	public void testContainsAmbiguousKKeyIsOK()
	{
		assertTrue(AMBIGUOUS_K.containsKey("A"));
		assertTrue(AMBIGUOUS_K.containsKey("B"));
		assertFalse(AMBIGUOUS_K.containsKey("C"));

		assertTrue(AMBIGUOUS_K.containsValue("3"));
		assertTrue(AMBIGUOUS_K.containsValue("4"));

		assertFalse(AMBIGUOUS_K.containsKey("a"));
		assertFalse(AMBIGUOUS_K.containsKey("b"));
		assertFalse(AMBIGUOUS_K.containsKey("E"));
		assertFalse(AMBIGUOUS_K.containsKey("F"));
	}

	@Test
	public void testGetAmbiguousVValuePairsIsOK()
	{
		assertEquals("1", AMBIGUOUS_V.getValueByKey("A"));
		assertEquals("1", AMBIGUOUS_V.getValueByKey("B"));
		assertEquals("1", AMBIGUOUS_V.getValueByKey("C"));
		assertEquals("1", AMBIGUOUS_V.getValueByKey("D"));

		assertEquals("A", AMBIGUOUS_V.getKeyByValue("1"));

		assertNull(AMBIGUOUS_V.getValueByKey("1234"));
		assertNull(AMBIGUOUS_V.getKeyByValue("1234"));

		assertNull(AMBIGUOUS_V.getPairByKey("E"));
	}

	@Test
	public void testGetAmbiguousVPairsByKeyIsOK()
	{
		assertEquals("A", AMBIGUOUS_V.getPairByKey("A").getKey());
		assertEquals("B", AMBIGUOUS_V.getPairByKey("B").getKey());
		assertEquals("C", AMBIGUOUS_V.getPairByKey("C").getKey());
		assertEquals("D", AMBIGUOUS_V.getPairByKey("D").getKey());

		assertEquals("1", AMBIGUOUS_V.getPairByKey("A").getValue());
		assertEquals("1", AMBIGUOUS_V.getPairByKey("B").getValue());
		assertEquals("1", AMBIGUOUS_V.getPairByKey("C").getValue());
		assertEquals("1", AMBIGUOUS_V.getPairByKey("D").getValue());
	}

	@Test
	public void testGetAmbiguousVPairsByValueIsOK()
	{
		assertEquals("A", AMBIGUOUS_V.getPairByValue("1").getKey());

		assertEquals("1", AMBIGUOUS_V.getPairByValue("1").getValue());
	}

	@Test
	public void testContainsAmbiguousVKeyIsOK()
	{
		assertTrue(AMBIGUOUS_V.containsKey("A"));
		assertTrue(AMBIGUOUS_V.containsKey("B"));
		assertTrue(AMBIGUOUS_V.containsKey("C"));
		assertTrue(AMBIGUOUS_V.containsKey("D"));

		assertTrue(AMBIGUOUS_V.containsValue("1"));
		assertFalse(AMBIGUOUS_V.containsValue("2"));

		assertFalse(AMBIGUOUS_V.containsKey("a"));
		assertFalse(AMBIGUOUS_V.containsKey("b"));
		assertFalse(AMBIGUOUS_V.containsKey("E"));
		assertFalse(AMBIGUOUS_V.containsKey("F"));
	}
}
