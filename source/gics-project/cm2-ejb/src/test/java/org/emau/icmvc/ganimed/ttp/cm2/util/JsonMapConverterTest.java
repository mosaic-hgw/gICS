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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

class JsonMapConverterTest extends JsonConverterTest
{
	final static String psAsJson = ("["
			+ "{'key':{'domainName':'d1','name':'p1','version':'v1'},'value':true},"
			+ "{'key':{'domainName':'d2','name':'p2','version':'v2'},'value':false}"
			+ "]")
					.replace('\'', '"');

	private static final JsonMapConverter<PolicyKeyDTO, Boolean> POLICY_STATUS_CONVERTER = new JsonMapConverter<>(PolicyKeyDTO.class, Boolean.class);

	private static class PolicyStatusSerializer extends JsonMapConverter.Serializer<PolicyKeyDTO, Boolean>
	{
		public PolicyStatusSerializer()
		{
			super(POLICY_STATUS_CONVERTER);
		}
	}

	private static class PolicyStatusDeserializer extends JsonMapConverter.Deserializer<PolicyKeyDTO, Boolean>
	{
		public PolicyStatusDeserializer()
		{
			super(POLICY_STATUS_CONVERTER);
		}
	}

	static class TestClass
	{

		@JsonDeserialize(using = PolicyStatusDeserializer.class)
		@JsonSerialize(using = PolicyStatusSerializer.class)
		Map<PolicyKeyDTO, Boolean> ps;

		public TestClass()
		{}

		public TestClass(Map<PolicyKeyDTO, Boolean> ps)
		{
			this.ps = ps;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}

			if (o == null || getClass() != o.getClass())
			{
				return false;
			}

			TestClass testClass = (TestClass) o;

			return new EqualsBuilder().append(ps, testClass.ps).isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37).append(ps).toHashCode();
		}
	}

	PolicyKeyDTO pk1;
	PolicyKeyDTO pk2;
	Map<PolicyKeyDTO, Boolean> ps;
	JsonMapConverter<PolicyKeyDTO, Boolean> mapConverter;

	@Override
	@BeforeEach
	void setUp() throws ParseException
	{
		super.setUp();
		pk1 = new PolicyKeyDTO("d1", "p1", "v1");
		pk2 = new PolicyKeyDTO("d2", "p2", "v2");
		ps = new HashMap<>();
		ps.put(pk1, true);
		ps.put(pk2, false);
		mapConverter = new JsonMapConverter<>(PolicyKeyDTO.class, Boolean.class);
	}

	@Override
	@Test
	void toJson() throws JsonProcessingException
	{
		assertEquals(psAsJson, mapConverter.toJson(ps));
	}

	@Override
	@Test
	void fromJson() throws IOException
	{
		assertEquals(ps, mapConverter.fromJson(psAsJson));
	}

	@Override
	@Test
	void serializer() throws JsonProcessingException
	{
		TestClass testClass = new TestClass(ps);
		String json = POLICY_STATUS_CONVERTER.getMapper().writeValueAsString(testClass);
		assertEquals("{\"ps\":" + psAsJson + "}", json);
	}

	@Override
	@Test
	void deserializer() throws JsonProcessingException
	{
		TestClass testClass = POLICY_STATUS_CONVERTER.getMapper().readValue("{\"ps\":" + psAsJson + "}", TestClass.class);
		assertEquals(new TestClass(ps), testClass);
	}
}
