package org.emau.icmvc.ganimed.ttp.cm2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

class ConsentNotificationMessageTest
{
	final static String DATE = "1970-06-23";
	final static String CONSENT_KEY_JSON = ("{"
			+ "'consentTemplateKey':{'domainName':'d1','name':'n1','version':'v1'},"
			+ "'signerIds':[{'fhirID':'fid1','idType':'sidt1','id':'sid1','creationDate':'1970-06-23 12:00:00','orderNumber':0}],"
			+ "'consentDate':'1970-06-23 12:00:00'"
			+ "}").replace('\'', '"');
	final static String POLICY_STATE_MAP_JSON = ("["
			+ "{'key':{'domainName':'d1','name':'p1','version':'v1'},'value':true},"
			+ "{'key':{'domainName':'d2','name':'p2','version':'v2'},'value':false}"
			+ "]").replace('\'', '"');
	final static String CONSENT_NOTIFICATION_MSG1_JSON = ("{"
			+ "'type':'t',"
			+ "'clientId':'cid',"
			+ "'comment':'c',"
			+ "'consentKey':" + CONSENT_KEY_JSON + ","
			+ "'previousPolicyStates':" + POLICY_STATE_MAP_JSON
			+ "}").replace('\'', '"');
	final static String CONSENT_NOTIFICATION_MSG2_JSON = ("{"
			+ "'type':'t',"
			+ "'clientId':'cid',"
			+ "'comment':'c',"
			+ "'consentKey':" + CONSENT_KEY_JSON + ","
			+ "'previousPolicyStates':" + POLICY_STATE_MAP_JSON + ","
			+ "'currentPolicyStates':[{'key':{'domainName':'d1','name':'p3','version':'v1'},'value':true}]"
			+ "}").replace('\'', '"');

	ConsentNotificationMessage cnmsg;
	PolicyKeyDTO pk3;
	Map<PolicyKeyDTO, Boolean> policyStateMap;

	@BeforeEach
	void setUp() throws ParseException
	{
		Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).parse(DATE);
		ConsentTemplateKeyDTO ctk = new ConsentTemplateKeyDTO("d1", "n1", "v1");
		Set<SignerIdDTO> sids = Stream.of(
				new SignerIdDTO("sidt1", "sid1", date, "fid1"),
				new SignerIdDTO("sidt1", "sid1", date, "fid1")).collect(Collectors.toSet());
		ConsentKeyDTO ck = new ConsentKeyDTO(ctk, sids, date);
		policyStateMap = new HashMap<>();
		policyStateMap.put(new PolicyKeyDTO("d1", "p1", "v1"), true);
		policyStateMap.put(new PolicyKeyDTO("d2", "p2", "v2"), false);
		cnmsg = new ConsentNotificationMessage(ck, policyStateMap, "t", "cid", "c");
		pk3 = new PolicyKeyDTO("d1", "p3", "v1");
	}

	@Test
	void toJson() throws JsonProcessingException
	{
		assertEquals(CONSENT_NOTIFICATION_MSG1_JSON, cnmsg.toJson());
		cnmsg.getContext(); // creates an empty context map which should not change serialization
		assertEquals(CONSENT_NOTIFICATION_MSG1_JSON, cnmsg.toJson());
		this.cnmsg.setCurrentPolicyStates(new HashMap<>());
		this.cnmsg.getCurrentPolicyStates().put(pk3, Boolean.TRUE);
		assertEquals(CONSENT_NOTIFICATION_MSG2_JSON, cnmsg.toJson());
	}

	@Test
	void fromJson() throws IOException
	{
		ConsentNotificationMessage nm = new ConsentNotificationMessage();
		nm.fromJson(CONSENT_NOTIFICATION_MSG1_JSON);
		assertEquals(this.cnmsg, nm);
		this.cnmsg.setCurrentPolicyStates(new HashMap<>());
		this.cnmsg.getCurrentPolicyStates().put(pk3, Boolean.TRUE);
		nm.fromJson(CONSENT_NOTIFICATION_MSG2_JSON);
		assertEquals(this.cnmsg, nm);
	}

	@Test
	void capture()
	{
		ConsentNotificationMessage nm = new ConsentNotificationMessage();
		nm.capture(this.cnmsg);
		assertEquals(this.cnmsg, nm);
		this.cnmsg.setCurrentPolicyStates(new HashMap<>());
		this.cnmsg.getCurrentPolicyStates().put(pk3, Boolean.TRUE);
		this.cnmsg.getContext().put("key", "value");
		assertNotEquals(this.cnmsg, nm);
		nm.capture(this.cnmsg);
		assertEquals(this.cnmsg, nm);
		assertNotSame(this.cnmsg.getPreviousPolicyStates(), nm.getPreviousPolicyStates());
		assertNotSame(this.cnmsg.getCurrentPolicyStates(), nm.getCurrentPolicyStates());
		assertNotSame(this.cnmsg.getContext(), nm.getContext());
	}

	@Test
	void roundtrip() throws IOException
	{
		this.cnmsg.getContext().put("key", "value");
		ConsentNotificationMessage nm = new ConsentNotificationMessage(this.cnmsg.toJson());
		assertEquals(this.cnmsg, nm);
		assertEquals("value", nm.getContext().get("key"));
	}
}
