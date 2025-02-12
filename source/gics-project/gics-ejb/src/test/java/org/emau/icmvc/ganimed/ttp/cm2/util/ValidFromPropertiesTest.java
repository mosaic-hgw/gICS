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

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ValidFromPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ValidFromPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ValidFromProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidFromPropertiesTest
{
	@Test
	void createConvert() throws ParseException
	{
		Date fromDate = Date.from(LocalDate.of(2024, 1, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
		String propsAsString =
				"%s=P10D;%s=2024.01.10;%s=yyyy.MM.dd;".formatted(ValidFromProperties.VALID_FROM_PERIOD.toString(), ValidFromProperties.VALID_FROM_DATE.toString(),
						ValidFromProperties.VALID_FROM_DATE_FORMAT.toString());

		ValidFromPropertiesObject o = new ValidFromPropertiesObject("%s=2024.01.10;".formatted(ValidFromProperties.VALID_FROM_DATE.toString()));
		Assertions.assertEquals(fromDate, o.getValidFromDate(), "invalid date");
		o = new ValidFromPropertiesObject("%s=2024-01-10;%s=yyyy-MM-dd;".formatted(ValidFromProperties.VALID_FROM_DATE.toString(), ValidFromProperties.VALID_FROM_DATE_FORMAT.toString()));
		Assertions.assertEquals(fromDate, o.getValidFromDate(), "invalid date-format");
		o = new ValidFromPropertiesObject("%s=2024-01-10;%s=yyyy-MM-dd;%s=P10D;".formatted(ValidFromProperties.VALID_FROM_DATE.toString(), ValidFromProperties.VALID_FROM_DATE_FORMAT.toString(),
				ValidFromProperties.VALID_FROM_PERIOD.toString()));
		Assertions.assertEquals(Period.of(0, 0, 10), o.getInvalidPeriod(), "invalid period");
		Assertions.assertEquals(
				propsAsString, o.toPropertiesString(), "invalid convertion to properties");

		ValidFromPropertiesDTO dto = new ValidFromPropertiesDTO(fromDate, Period.of(0, 0, 10));
		o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				propsAsString, o.toPropertiesString(), "invalid convertion to properties");

		Date futureDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());
		dto.setFixedValidFromDate(futureDate);
		ValidFromPropertiesObject mergedProperties = o.createMergedProperties(dto, false);
		Assertions.assertEquals(futureDate, mergedProperties.getValidFromDate());
		o = new ValidFromPropertiesObject(Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant()), null);
		mergedProperties = o.createMergedProperties(dto, true);
		Assertions.assertEquals(futureDate, mergedProperties.getValidFromDate());
		dto.setFixedValidFromDate(fromDate);
		mergedProperties = o.createMergedProperties(dto, true);
		Assertions.assertEquals(o.getValidFromDate(), mergedProperties.getValidFromDate());
	}

	@Test
	void testConsentDate() throws ParseException
	{
		Date fromDate = Date.from(LocalDate.of(2024, 1, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date consentDate = Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		//nothing configured should return given cosnentDate
		ValidFromPropertiesDTO dto = new ValidFromPropertiesDTO(null, null);
		ValidFromPropertiesObject o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				null, o.getValidFromDateForConsentDate(consentDate));

		//configured fixed date should return fixed date
		dto = new ValidFromPropertiesDTO(fromDate, null);
		o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				fromDate, o.getValidFromDateForConsentDate(consentDate));

		//configured period should return consentDate + period
		dto = new ValidFromPropertiesDTO(null, Period.of(0, 0, 10));
		o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				Date.from(LocalDate.ofInstant(consentDate.toInstant(), ZoneId.systemDefault()).plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant()),
				o.getValidFromDateForConsentDate(consentDate));

		//configured date + period (date+1) should return consentDate + period
		dto = new ValidFromPropertiesDTO(fromDate, Period.of(0, 0, 11));
		o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				Date.from(LocalDate.ofInstant(consentDate.toInstant(), ZoneId.systemDefault()).plusDays(11).atStartOfDay(ZoneId.systemDefault()).toInstant()),
				o.getValidFromDateForConsentDate(consentDate));

		//configured date + period (date-1) should return date
		dto = new ValidFromPropertiesDTO(fromDate, Period.of(0, 0, 9));
		o = new ValidFromPropertiesObject(dto);
		Assertions.assertEquals(
				fromDate,
				o.getValidFromDateForConsentDate(consentDate));
	}
}
