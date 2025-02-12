package org.emau.icmvc.ganimed.ttp.cm2.internal;

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
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractGicsTest;
import org.emau.icmvc.ganimed.ttp.cm2.util.Dates;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DAOTest extends AbstractGicsTest
{
	private AutoCloseable closeable;

	@Mock
	private EntityManager em;

	@InjectMocks
	private DAO dao;

	private DomainDTO domainDTO;
	private PolicyDTO policyDTO;
	private ModuleDTO moduleDTO;
	private ConsentTemplateDTO consentTemplateDTO;
	private SignerIdTypeDTO signerIdTypeDTO;
	private SignerIdDTO signerIdDTO;
	private ConsentDTO consentDTO;

	Domain domain;
	Module module;
	ConsentTemplate consentTemplate;
	Consent consent;

	Date BEFORE = Dates.toDate(LocalDate.of(2020, 1, 1));
	Date AFTER = Dates.toDate(LocalDate.of(2100, 1, 1));
	Date AFTER_AT_START_OF_DAY = DateUtils.truncate(AFTER, Calendar.DATE);


	@BeforeEach
	protected void setUpDAOTest()
			throws VersionConverterClassException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, InvalidParameterException, InvalidPropertiesException,
			UnknownModuleException, FreeTextConverterStringException, InvalidFreeTextException, MissingRequiredObjectException, UnknownSignerIdTypeException
	{
		closeable = openMocks(this);

		// Domain
		domainDTO = createDomainDto();
		domainDTO.getConfig().getScansConfig().setMandatory(false);
		domain = createDomain(domainDTO);
		initVersionConverters(domain);

		// Policy
		policyDTO = createPolicyDTO();
		Policy policy = createPolicy(domain, policyDTO);

		// Module
		moduleDTO = createModuleDTO(List.of(policyDTO));
		module = createModule(domain, moduleDTO, List.of(policy));

		// Template
		consentTemplateDTO = createConsentTemplateDTO(List.of(moduleDTO));
		consentTemplate = createConsentTemplate(domain, consentTemplateDTO, List.of(module));

		// SignerId
		signerIdTypeDTO = createSignerIdTypeDTO();
		signerIdDTO = createSignerIdDTO();
		SignerId signerId = createSignerId(domain, signerIdDTO, signerIdTypeDTO, dao);

		// Consent
		consentDTO = createConsentDto();
		consent = createConsent(consentTemplate, consentDTO, List.of(module));

		// Mock
		when(em.find(Domain.class, domainDTO.getName())).thenReturn(domain);
		when(em.find(ConsentTemplate.class, consentTemplate.getKey())).thenReturn(consentTemplate);
		when(em.find(Module.class, module.getKey())).thenReturn(module);
		when(em.find(SignerId.class, signerId.getKey())).thenReturn(signerId);
		when(em.find(Consent.class, consent.getKey())).thenReturn(consent);
	}

	@AfterEach
	void teardown() throws Exception
	{
		closeable.close();
	}

	@Nested
	@DisplayName("Validate Consent")
	class ValidateConsentTests
	{
		@Test
		void validateConsentNoScanNoSignaturesV2()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, false, false);
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V2"));
		}

		@Test
		void validateConsentWithScanNoSignatures()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, true, false, false);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, true);
		}

		@Test
		void validateConsentNoScanWithSignatures()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, true);
		}

		@Test
		void validateConsentNoScanWithOnlyOneSignatureV2()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, false);
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V2"));
		}

		@Test
		void validateConsentWithScanWithSignatures()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, true, true, true);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, true);
		}

		@Test
		void validateConsentNoSignatureDatesV3()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true, false, true);
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V3"));

			// Arrange
			ConsentDTO consentDTO2 = createConsentDto(consentTemplateDTO, false, true, true, true, false);
			// Act
			e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO2, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V3"));

			// Arrange
			ConsentDTO consentDTO3 = createConsentDto(consentTemplateDTO, false, true, true, false, false);
			// Act
			e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO3, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V3"));
		}

		@Test
		void validateConsentDateNotBeforeMinDateV4()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO.getKey().setConsentDate(new Date(82850000L));
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V4"));
		}

		@Test
		void validateConsentSignatureDateNotBeforeMinDateV5()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO.setPatientSigningDate(new Date(82850000L));
			// Act
			InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V5"));

			// Arrange
			ConsentDTO consentDTO2 = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO2.setPhysicianSigningDate(new Date(82850000L));
			// Act
			e = assertThrows(InvalidParameterException.class, () -> dao.validateConsentReturningObjects(consentDTO2, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V5"));

			// Arrange
			ConsentDTO consentDTO3 = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO3.setPatientSigningDate(new Date(82850000L));
			consentDTO3.setPhysicianSigningDate(new Date(82850000L));
			// Act
			e = assertThrows(InvalidParameterException.class, () -> dao.validateConsentReturningObjects(consentDTO3, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V5"));
		}

		@Test
		void validateConsentMissingModuleV7()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO.getModuleStates().clear();
			consentDTO.getModuleStates().put(new ModuleKeyDTO("test", "test", "1.0"), new ModuleStateDTO(moduleDTO.getKey(), ConsentStatus.ACCEPTED, List.of(policyDTO.getKey())));
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V7"));
		}

		@Test
		void validateConsentInconsistentTemplateTypesV8()
		{
			// Arrange
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			consentDTO.setTemplateType(ConsentTemplateType.REVOCATION);
			// Act
			InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V8"));
		}

		@Test
		void validateConsentMandatoryScanNoScanV1()
		{
			// Arrange
			domainDTO.getConfig().getScansConfig().setMandatory(true);
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			// Act
			MissingRequiredObjectException e = assertThrows(MissingRequiredObjectException.class, () -> dao.validateConsentReturningObjects(consentDTO, true, true));
			// Assert
			assertTrue(e.getMessage().startsWith("V1"));
		}

		@Test
		void validateConsentMandatoryScanWithScan()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			domainDTO.getConfig().getScansConfig().setMandatory(true);
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, true, false, false);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, true);
		}

		@Test
		void validateConsentMandatoryScanNoScanSkipScanValidation()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			domainDTO.getConfig().getScansConfig().setMandatory(true);
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, false, true, true);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, false);
		}

		@Test
		void validateConsentMandatoryScanWithScanSkipScanValidation()
				throws InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, UnknownDomainException, MissingRequiredObjectException,
				MandatoryFieldsException
		{
			// Arrange
			domainDTO.getConfig().getScansConfig().setMandatory(true);
			ConsentDTO consentDTO = createConsentDto(consentTemplateDTO, true, false, false);
			// Act
			dao.validateConsentReturningObjects(consentDTO, true, false);
		}
	}

	@Nested
	@DisplayName("Calculate LegalConsentDate")
	class LegalConsentDateTests
	{
		@Nested
		@DisplayName("ValidFrom not set")
		class NoValidFrom
		{

			@Test
			@DisplayName("Signature dates at creationDate")
			void signatureDatesAtCreationDate() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentDTO.setPatientSigningDate(NOW_AT_START_OF_DAY);
				consentDTO.setPhysicianSigningDate(NOW_AT_START_OF_DAY);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(NOW_AT_START_OF_DAY.getTime(), consentDateValuesDTO.getLegalConsentDate().getTime());
			}

			@Test
			@DisplayName("Signature dates before creationDate #0")
			void signatureDatesBeforeCreationDate() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentDTO.setPatientSigningDate(DateUtils.truncate(BEFORE, Calendar.DATE));
				consentDTO.setPhysicianSigningDate(DateUtils.truncate(BEFORE, Calendar.DATE));
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(BEFORE, consentDateValuesDTO.getLegalConsentDate());
			}
		}

		@Nested
		@DisplayName("ValidFrom external date")
		class ValidFromDateExternal
		{
			@Test
			@DisplayName("Without signature dates and before creationDate #1")
			void beforeSignatureDatesExternal() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentDTO.setPatientSigningDate(null);
				consentDTO.setPhysicianSigningDate(null);
				consentDTO.setValidFromDate(BEFORE);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(BEFORE, consentDateValuesDTO.getLegalConsentDate());
			}
		}

		@Nested
		@DisplayName("ValidFrom fixed date")
		class ValidFromFixedDate
		{
			@Test
			@DisplayName("Without signature dates and before creationDate #2")
			void withoutSignatureDates() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setFixedValidFromDate(DateUtils.truncate(BEFORE, Calendar.DATE));
				consentDTO.setPatientSigningDate(null);
				consentDTO.setPhysicianSigningDate(null);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(BEFORE, consentDateValuesDTO.getLegalConsentDate());
			}

			@Test
			@DisplayName("Before signature dates #5")
			void beforeSignatureDates() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setFixedValidFromDate(DateUtils.truncate(BEFORE, Calendar.DATE));
				consentDTO.setPatientSigningDate(NOW_AT_START_OF_DAY);
				consentDTO.setPhysicianSigningDate(NOW_AT_START_OF_DAY);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(NOW_AT_START_OF_DAY, consentDateValuesDTO.getLegalConsentDate());
			}

			@Test
			@DisplayName("After signature dates #6")
			void afterSignatureDates() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setFixedValidFromDate(DateUtils.truncate(AFTER, Calendar.DATE));
				consentDTO.setPatientSigningDate(NOW_AT_START_OF_DAY);
				consentDTO.setPhysicianSigningDate(NOW_AT_START_OF_DAY);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(AFTER, consentDateValuesDTO.getLegalConsentDate());
			}
		}

		@Nested
		@DisplayName("ValidFrom period")
		class ValidFromPeriod
		{
			@Test
			@DisplayName("Without signature dates #3")
			void withoutSignatureDates() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setInvalidPeriod(Period.ofDays(10));
				consentDTO.setPatientSigningDate(null);
				consentDTO.setPhysicianSigningDate(null);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				Date in10DaysFromNow = Dates.toDate(Dates.toLocalDate(NOW).plus(consentTemplateDTO.getValidFromProperties().getInvalidPeriod()));
				assertEquals(in10DaysFromNow, consentDateValuesDTO.getLegalConsentDate());
			}
		}

		@Nested
		@DisplayName("ValidFrom fixed date and period")
		class ValidFromFixedDateAndPeriod
		{
			@Test
			@DisplayName("Without signature dates and before creationDate #4a")
			void withoutSignatureDatesBeforeCreationDate() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setFixedValidFromDate(BEFORE);
				consentTemplateDTO.getValidFromProperties().setInvalidPeriod(Period.ofDays(10));
				consentDTO.setPatientSigningDate(null);
				consentDTO.setPhysicianSigningDate(null);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				Date in10DaysFromNow = Dates.toDate(Dates.toLocalDate(NOW).plus(consentTemplateDTO.getValidFromProperties().getInvalidPeriod()));
				assertEquals(in10DaysFromNow, consentDateValuesDTO.getLegalConsentDate());
			}

			@Test
			@DisplayName("Without signature dates and after creationDate #4b")
			void withoutSignatureDatesAfterCreationDate() throws UnknownSignerIdTypeException,
					UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException, InvalidParameterException, InvalidFreeTextException,
					MissingRequiredObjectException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownModuleException, FreeTextConverterStringException
			{
				// Arrange
				consentTemplateDTO.getValidFromProperties().setFixedValidFromDate(AFTER);
				consentTemplateDTO.getValidFromProperties().setInvalidPeriod(Period.ofDays(10));
				consentDTO.setPatientSigningDate(null);
				consentDTO.setPhysicianSigningDate(null);
				reloadMocks();

				// Act
				ConsentDateValuesDTO consentDateValuesDTO = dao.getConsentDates(consentDTO.getKey());

				// Assert
				assertEquals(AFTER, consentDateValuesDTO.getLegalConsentDate());
			}
		}
	}

	@Nested
	@DisplayName("check policies")
	class CheckPoliciesTests
	{
		private ConsentCache.CachedSignedPolicy createCachedSignedPolicy(long gicsConsentDate, long legalConsentDate, long expirationDate, ConsentStatus consentStatus)
				throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			Consent ic = new Consent(consentTemplate, consentDTO, Map.of(), new VirtualPerson(), new DomainConfig());
			return new ConsentCache.CachedSignedPolicy(
					new SignedPolicyKey(ic.getKey(), ic.getConsentTemplate().getModuleConsentTemplates().getFirst().getModule().getModulePolicies().getFirst().getPolicy().getKey()),
					gicsConsentDate, legalConsentDate, consentStatus, expirationDate);
		}

		@Test
		@DisplayName("different legal-consent-dates #1")
		void differentLegalConsentDates() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			//do not create a global var for "new CheckConsentConfig()", otherwise the requestDate in it will be in some cases before the policy-dates which would be wrong
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP), domain));
		}

		@Test
		@DisplayName("legal-consent-dates on same day #2a")
		void legalConsentDatesOnSameDay() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			ConsentCache.CachedSignedPolicy acceptedP2 = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP, acceptedP2), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, acceptedP2, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP2, declinedP, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP2, acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP, acceptedP2), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP2, acceptedP), domain));
		}

		@Test
		@DisplayName("legal-consent-dates on same day and one with time #2b")
		void legalConsentDatesOnSameDayWithOneTime() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP), domain));
			//with time switched
			acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			declinedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP), domain));

		}

		@Test
		@DisplayName("legal-consent-dates on same day and both with time #2c")
		void legalConsentDatesOnSameDayWithBothTime() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP), domain));
		}

		@Test
		@DisplayName("legal-consent-dates on different and same day #3")
		void legalConsentDatesOnDifferentAndSameDay() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			ConsentCache.CachedSignedPolicy acceptedP2 = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(acceptedP, declinedP, acceptedP2), domain));
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(acceptedP, acceptedP2, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(declinedP, acceptedP2, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(declinedP, acceptedP, acceptedP2), domain));
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(acceptedP2, declinedP, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.ACCEPTED, DAO.checkPolicies(config, List.of(acceptedP2, acceptedP, declinedP), domain));
		}

		@Test
		@DisplayName("legal-consent-dates on different and same day with permanent-revoke=true #4")
		void legalConsentDatesOnDifferentAndSameDayWithUnknown() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy declinedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.DECLINED);
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			ConsentCache.CachedSignedPolicy unknown = createCachedSignedPolicy(NOW.getTime(), NOW_AT_START_OF_DAY.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			domain.getConfig().getPoliciesConfig().setPermanentRevoke(true);
			CheckConsentConfig config = new CheckConsentConfig();
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, declinedP, unknown), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(acceptedP, unknown, declinedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, unknown, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(declinedP, acceptedP, unknown), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(unknown, declinedP, acceptedP), domain));
			Assertions.assertEquals(ConsentStatusType.DECLINED, DAO.checkPolicies(config, List.of(unknown, acceptedP, declinedP), domain));
		}

		@Test
		@DisplayName("permanent-revoke=true and unknownConsideredAsDeclined=true #4a")
		void legalConsentDatesOnDifferentDayWithUnknown2() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy unknown = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.UNKNOWN, true, true);
			//switch policies in time
			acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.UNKNOWN, true, true);
			//on same day
			acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.UNKNOWN, true, true);
		}

		@Test
		@DisplayName("permanent-revoke=true and unknownConsideredAsDeclined=false #4b")
		void legalConsentDatesOnDifferentDayWithUnknownPr() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy unknown = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, true, false);
			//switch policies in time
			acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, true, false);
			//on same day
			acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, true, false);
		}

		@Test
		@DisplayName("permanent-revoke=false and unknownConsideredAsDeclined=true #4c")
		void legalConsentDatesOnDifferentDayWithUnknownUCAD() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy unknown = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, false, true);
			//switch policies in time
			acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.UNKNOWN, false, true);
			//on same day
			acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.UNKNOWN, false, true);
		}

		@Test
		@DisplayName("permanent-revoke=false and unknownConsideredAsDeclined=false #4d")
		void legalConsentDatesOnDifferentDayWithUnknownUCAD_PR() throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
		{
			ConsentCache.CachedSignedPolicy unknown = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			ConsentCache.CachedSignedPolicy acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, false, false);
			//switch policies in time
			acceptedP = createCachedSignedPolicy(BEFORE.getTime(), BEFORE.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, false, false);
			//on same day
			acceptedP = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.ACCEPTED);
			unknown = createCachedSignedPolicy(NOW.getTime(), NOW.getTime(), FAR_FUTURE_AT_START_OF_DAY.getTime(), ConsentStatus.UNKNOWN);
			checkPoliciesGeneric(List.of(acceptedP, unknown), ConsentStatusType.ACCEPTED, false, false);
		}

		private void checkPoliciesGeneric(List<ConsentCache.CachedSignedPolicy> policies, ConsentStatusType expectedStatus, boolean permanentRevoke, boolean unknownStateIsConsideredAsDecline)
		{
			domain.getConfig().getPoliciesConfig().setPermanentRevoke(permanentRevoke);
			CheckConsentConfig config = new CheckConsentConfig();
			config.setUnknownStateIsConsideredAsDecline(unknownStateIsConsideredAsDecline);
			Assertions.assertEquals(expectedStatus, DAO.checkPolicies(config, policies, domain));
			Assertions.assertEquals(expectedStatus, DAO.checkPolicies(config, Lists.reverse(policies), domain));
		}

	}

	private void reloadMocks()
			throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException, InvalidPropertiesException, InvalidVersionException, RequirementsNotFullfilledException,
			UnknownModuleException, UnknownDomainException, FreeTextConverterStringException
	{
		consentTemplate = createConsentTemplate(domain, consentTemplateDTO, List.of(module));
		when(em.find(ConsentTemplate.class, consentTemplate.getKey())).thenReturn(consentTemplate);

		consent = createConsent(consentTemplate, consentDTO, List.of(module));
		when(em.find(Consent.class, consent.getKey())).thenReturn(consent);
	}
}
