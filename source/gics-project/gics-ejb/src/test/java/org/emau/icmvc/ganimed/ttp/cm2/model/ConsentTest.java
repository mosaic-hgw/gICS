package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import java.time.LocalDate;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentDateValues;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractGicsTest;
import org.emau.icmvc.ganimed.ttp.cm2.util.Dates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsentTest extends AbstractGicsTest
{
	private DomainDTO domainDTO;
	private PolicyDTO policyDTO;
	private ModuleDTO moduleDTO;
	Module module;
	private ConsentTemplateDTO consentTemplateDTO;
	ConsentTemplate consentTemplate;

	@BeforeEach
	protected void setUpConsentTest()
			throws VersionConverterClassException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, InvalidParameterException, InvalidPropertiesException,
			UnknownModuleException, FreeTextConverterStringException
	{
		// Domain
		domainDTO = createDomainDto();
		domainDTO.getConfig().getScansConfig().setMandatory(false);
		Domain domain = createDomain(domainDTO);
		VersionConverterCache.init(List.of(domain));

		// Policy
		policyDTO = createPolicyDTO();
		Policy policy = createPolicy(domain, policyDTO);

		// Module
		moduleDTO = createModuleDTO(List.of(policyDTO));
		module = createModule(domain, moduleDTO, List.of(policy));

		// Template
		consentTemplateDTO = createConsentTemplateDTO(List.of(moduleDTO));
		consentTemplate = createConsentTemplate(domain, consentTemplateDTO, List.of(module));
	}

	@Nested
	@DisplayName("calculateConsentDateValues")
	class CalculateConsentDateValues
	{
		@Test
		void withSignatureNoValidFrom() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException, VersionConverterClassException
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO);
			consentDTO.setPatientSigningDate(Dates.toDate(LocalDate.of(2024, 1, 1)));
			consentDTO.setPhysicianSigningDate(Dates.toDate(LocalDate.of(2024, 1, 1)));
			Consent consent = createConsent(consentTemplate, consentDTO, List.of(module));

			// Act
			ConsentDateValues consentDateValues = consent.getConsentDateValues();

			// Assert
			assertEquals(consentDTO.getPatientSigningDate().getTime(), consentDateValues.getLegalConsentTimestamp());
		}
	}
}
