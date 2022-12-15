package org.emau.icmvc.ganimed.ttp.cm2.util;

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

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class NotificationMessageTest extends JsonMapConverterTest
{
	final static String nmAsJson =
				("{'consentKey':" + ckAsJson + ","
				+ "'previousPolicyStates':" + psAsJson + ","
				+ "'type':'t',"
				+ "'clientId':'cid',"
				+ "'comment':'c'}")
			.replace('\'', '"');
	final static String nm2AsJson =
			("{'consentKey':" + ckAsJson + ","
					+ "'previousPolicyStates':" + psAsJson + ","
					+ "'currentPolicyStates':[{'key':{'domainName':'d1','name':'p3','version':'v1'},'value':true}],"
					+ "'type':'t',"
					+ "'clientId':'cid',"
					+ "'comment':'c'}")
					.replace('\'', '"');

	NotificationMessage nm;
	PolicyKeyDTO p3;

	@BeforeEach
	void setUp() throws ParseException
	{
		super.setUp();
		nm = new NotificationMessage(ck, ps, "t", "cid", "c");
		p3 = new PolicyKeyDTO("d1", "p3", "v1");
	}

	@Test
	void toJson() throws JsonProcessingException
	{
		assertEquals(nmAsJson, nm.toJson());
		nm.getContext(); // creates an empty context map which should not change serialization
		assertEquals(nmAsJson, nm.toJson());
		this.nm.setCurrentPolicyStates(new HashMap<>());
		this.nm.getCurrentPolicyStates().put(p3, Boolean.TRUE);
		assertEquals(nm2AsJson, nm.toJson());
	}

	@Test
	void fromJson() throws IOException
	{
		NotificationMessage nm = new NotificationMessage();
		nm.fromJson(nmAsJson);
		assertEquals(this.nm, nm);
		this.nm.setCurrentPolicyStates(new HashMap<>());
		this.nm.getCurrentPolicyStates().put(p3, Boolean.TRUE);
		nm.fromJson(nm2AsJson);
		assertEquals(this.nm, nm);
	}

	@Test
	void capture()
	{
		NotificationMessage nm = new NotificationMessage();
		nm.capture(this.nm);
		assertEquals(this.nm, nm);
		this.nm.setCurrentPolicyStates(new HashMap<>());
		this.nm.getCurrentPolicyStates().put(p3, Boolean.TRUE);
		this.nm.getContext().put("key", "value");
		assertNotEquals(this.nm, nm);
		nm.capture(this.nm);
		assertEquals(this.nm, nm);
		assertNotSame(this.nm.getPreviousPolicyStates(), nm.getPreviousPolicyStates());
		assertNotSame(this.nm.getCurrentPolicyStates(), nm.getCurrentPolicyStates());
		assertNotSame(this.nm.getContext(), nm.getContext());
	}

	@Test
	void roundtrip() throws IOException
	{
		this.nm.getContext().put("key", "value");
		NotificationMessage nm = new NotificationMessage(this.nm.toJson());
		assertEquals(this.nm, nm);
		assertEquals("value", nm.getContext().get("key"));
	}
}
