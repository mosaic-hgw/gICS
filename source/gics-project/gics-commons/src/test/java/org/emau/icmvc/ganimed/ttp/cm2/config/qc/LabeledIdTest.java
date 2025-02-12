package org.emau.icmvc.ganimed.ttp.cm2.config.qc;

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

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabeledIdTest
{
	Map<String, String> map;
	LabeledId ml;

	@BeforeEach
	void setup()
	{
		map = Map.of("1", "a", "2", "b", "3", "c");
		ml = new LabeledId();
		ml.setLabels(map);
	}

	@Test
	void getLabel()
	{
		assertEquals("a", ml.getLabel("1"));
		assertEquals("a", ml.getLabels().get("1"));
		assertEquals("b", ml.getLabel("2"));
		assertEquals("b", ml.getLabels().get("2"));
	}

	@Test
	void setLabel()
	{
		ml.setLabel("1", "aa");
		ml.getLabels().put("2", "bb");
		assertEquals("aa", ml.getLabels().get("1"));
		assertEquals("aa", ml.getLabel("1"));
		assertEquals("bb", ml.getLabels().get("2"));
		assertEquals("bb", ml.getLabel("2"));
	}

	@Test
	void getLabels()
	{
		assertEquals(map, ml.getLabels());
		ml.getLabels().putAll(map);
		assertEquals(map, ml.getLabels());
		ml.getLabels().putAll(map);
		assertEquals(map, ml.getLabels());
		ml.clear();
		ml.getLabels().putAll(map);
		assertEquals(map, ml.getLabels());
	}

	@Test
	void setLabels()
	{
		LabeledId ml2 = new LabeledId();
		ml2.setLabels(map);
		assertEquals(ml, ml2);
	}

	@Test
	void entrySet()
	{
		assertEquals(new LabeledId("id", ml.getLabels()).getLabels().entrySet(), ml.getLabels().entrySet());
		ml.getLabels().entrySet().clear();
		assertTrue(ml.getLabels().isEmpty());
	}

	@Test
	void put()
	{
		ml.getLabels().put("4", "d");
		assertEquals("d", ml.getLabel("4"));
	}

	@Test
	void remove()
	{
		assertEquals("c", ml.removeLabel("3"));
		assertNull(ml.removeLabel("3"));
		assertNull(ml.getLabels().get("3"));
	}

	@Test
	public void testValidIds()
	{
		assertTrue(new LabeledId("auto_generated").hasValidId());
		assertTrue(new LabeledId("auto.generated").hasValidId());
		assertFalse(new LabeledId("auto generated").hasValidId());
		assertFalse(new LabeledId("###_auto_generated_###").hasValidId());
		assertFalse(new LabeledId("1auto_generated").hasValidId());
	}

	@Test
	public void testInvalidCharsInIds()
	{
		assertEquals("", new LabeledId("").findInvalidCharsInID());
		assertEquals("", new LabeledId("auto_generated").findInvalidCharsInID());
		assertEquals("", new LabeledId("auto.generated").findInvalidCharsInID());
		assertEquals(" ", new LabeledId("auto generated").findInvalidCharsInID());
		assertEquals("#", new LabeledId("###_auto_generated_###").findInvalidCharsInID());
		assertEquals("1", new LabeledId("1auto_generated").findInvalidCharsInID());
		assertEquals("# ", new LabeledId("###_auto generated_###").findInvalidCharsInID());
		assertEquals("1 #", new LabeledId("1_auto generated_###").findInvalidCharsInID());
	}
}
