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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

class JsonConverterTest
{
	final static String ckAsJson = ("{"
			+ "'consentTemplateKey':{'domainName':'d1','name':'n1','version':'v1'},"
			+ "'signerIds':[{'fhirID':'fid1','idType':'sidt1','id':'sid1','creationDate':'1970-06-23 12:00:00','orderNumber':0}],"
			+ "'consentDate':'1970-06-23 12:00:00'"
			+ "}")
					.replace('\'', '"');

	static final JsonConverter<ConsentKeyDTO> CONSENT_KEY_CONVERTER = new JsonConverter<>(ConsentKeyDTO.class);

	static class ConsentKeySerializer extends JsonConverter.Serializer<ConsentKeyDTO>
	{
		public ConsentKeySerializer()
		{
			super(CONSENT_KEY_CONVERTER);
		}
	}

	static class ConsentKeyDeserializer extends JsonConverter.Deserializer<ConsentKeyDTO>
	{
		public ConsentKeyDeserializer()
		{
			super(CONSENT_KEY_CONVERTER);
		}
	}

	static class TestClass
	{

		@JsonDeserialize(using = ConsentKeyDeserializer.class)
		@JsonSerialize(using = ConsentKeySerializer.class)
		ConsentKeyDTO ck;

		public TestClass()
		{}

		public TestClass(ConsentKeyDTO ck)
		{
			this.ck = ck;
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

			return new EqualsBuilder().append(ck, testClass.ck).isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37).append(ck).toHashCode();
		}
	}

	Set<SignerIdDTO> sids;
	ConsentTemplateKeyDTO ctk;
	ConsentKeyDTO ck;
	JsonConverter<ConsentKeyDTO> converter;
	Date date;

	@BeforeEach
	void setUp() throws ParseException
	{
		date = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).parse("1970-06-23");
		ctk = new ConsentTemplateKeyDTO("d1", "n1", "v1");
		sids = Stream.of(
				new SignerIdDTO("sidt1", "sid1", date, "fid1"),
				new SignerIdDTO("sidt1", "sid1", date, "fid1")).collect(Collectors.toSet());
		ck = new ConsentKeyDTO(ctk, sids, date);
		converter = new JsonConverter<>(ConsentKeyDTO.class);
	}

	@Test
	void toJson() throws JsonProcessingException
	{
		assertEquals(ckAsJson, converter.toJson(ck));
	}

	@Test
	void fromJson() throws IOException
	{
		assertEquals(ck, converter.fromJson(ckAsJson));
	}

	@Test
	void serializer() throws JsonProcessingException
	{
		TestClass testClass = new TestClass(ck);
		String json = CONSENT_KEY_CONVERTER.getMapper().writeValueAsString(testClass);
		assertEquals("{\"ck\":" + ckAsJson + "}", json);
	}

	@Test
	void deserializer() throws JsonProcessingException, IOException
	{
		TestClass testClass = CONSENT_KEY_CONVERTER.getMapper().readValue("{\"ck\":" + ckAsJson + "}", TestClass.class);
		assertEquals(new TestClass(ck), testClass);
	}
}
