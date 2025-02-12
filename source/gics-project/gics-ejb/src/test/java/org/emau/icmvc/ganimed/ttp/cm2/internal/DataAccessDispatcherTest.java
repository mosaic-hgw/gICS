package org.emau.icmvc.ganimed.ttp.cm2.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.IllegalCompositionException;
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
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.QC;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractGicsTest;
import org.emau.icmvc.ganimed.ttp.cm2.util.ConsentNotificationSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DataAccessDispatcherTest extends AbstractGicsTest
{
	private AutoCloseable closeable;

	private Domain domain;
	private ModuleDTO moduleDTO;
	private PolicyDTO policyDTO;

	@Mock
	private DAO dao;

	@Mock
	private ConsentNotificationSender consentNotificationSender;
	
	@InjectMocks
	private DataAccessDispatcher dataAccessDispatcher;

	@BeforeEach
	protected void setUp() throws InvalidParameterException, UnknownConsentTemplateException, InvalidFreeTextException, InvalidVersionException, UnknownModuleException, UnknownDomainException,
			VersionConverterClassException, RequirementsNotFullfilledException, DuplicateEntryException, InvalidPropertiesException, IllegalCompositionException, FreeTextConverterStringException
	{
		closeable = openMocks(this);

		// Arrange
		DomainDTO domainDTO = createDomainDto();
		domainDTO.setFinalised(true);

		domainDTO.getConfig().getScansConfig().setMandatory(false);
		domain = createDomain(domainDTO);
		initVersionConverters(domain);

		policyDTO = createPolicyDTO();

		moduleDTO = createModuleDTO(List.of(policyDTO));

		when(dao.addDomain(any())).then(AdditionalAnswers.returnsFirstArg());
		when(dao.addConsentTemplate(any(), anyBoolean())).then(AdditionalAnswers.returnsFirstArg());
		when(dao.updateConsentTemplateInUse(any())).then(AdditionalAnswers.returnsFirstArg());

		dataAccessDispatcher.addDomain(domainDTO);
	}

	@AfterEach
	void teardown() throws Exception
	{
		closeable.close();
		OrgDatCache.removeDomain(domain.getName());
	}

	@Nested
	@DisplayName("Update Consent Template")
	class UpdateConsentTemplate
	{
		@Nested
		@DisplayName("Update In Use")
		class UpdateInUse
		{
			@Test
			void onUpdateWithMapping()
					throws InvalidParameterException, UnknownConsentTemplateException, InvalidFreeTextException, InvalidVersionException, UnknownModuleException, UnknownDomainException,
					RequirementsNotFullfilledException, InvalidPropertiesException, IllegalCompositionException, FreeTextConverterStringException,
					DuplicateEntryException
			{
				// Arrange
				ConsentTemplateDTO consentTemplate = createConsentTemplateDTO(List.of(moduleDTO));
				ConsentTemplateDTO revocationTemplate = createConsentTemplateDTO("REVOCATION", List.of(moduleDTO));
				dataAccessDispatcher.addConsentTemplate(consentTemplate, false);
				dataAccessDispatcher.addConsentTemplate(revocationTemplate, false);

				// Act
				consentTemplate.setMappedRevocationTemplates(new HashSet<>(List.of(revocationTemplate.getKey())));
				dataAccessDispatcher.updateConsentTemplateInUse(consentTemplate);

				// Assert
				// Mapped revocation should be stored in consent after updating consent
				ConsentTemplateDTO updateResultConsent = dataAccessDispatcher.getConsentTemplate(consentTemplate.getKey());
				assertTrue(updateResultConsent.getMappedRevocationTemplates().contains(revocationTemplate.getKey()));
				// Mapped consent should also be stored in revocation after updating consent (without updating revocation)
				ConsentTemplateDTO updateResultRevocation = dataAccessDispatcher.getConsentTemplate(revocationTemplate.getKey());
				// REM: Test doesnt work because mocking of dao is not enough here because the dao manipulates the cache for mapped templates
				//				assertTrue(updateResultRevocation.getMappedRevocationTemplates().contains(consentTemplate.getKey()));
			}
		}
	}

	@Nested
	@DisplayName("Set QC For Consent")
	class SetQcForConsent
	{
		
		@Test
		@SuppressWarnings("unchecked")
		void onSetQcWithDifferentPassedStatus()
				throws UnknownSignerIdTypeException, InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownModuleException, InvalidFreeTextException,
				RequirementsNotFullfilledException, UnknownDomainException, MissingRequiredObjectException, MandatoryFieldsException, DuplicateEntryException, UnknownConsentException,
				InvalidPropertiesException, IllegalCompositionException, FreeTextConverterStringException, VersionConverterClassException
		{
			// Arrange
			// Create and configure domain
			DomainDTO domainDTO = createDomainDto();
			domainDTO.getConfig().getQualityControlConfig().setValidQcTypeValues(new HashSet<>(List.of("PASSED")));
			domainDTO.getConfig().getQualityControlConfig().setInvalidQcTypeValues(new HashSet<>(List.of("FAILED")));
			domain = createDomain(domainDTO);

			// Create consent with qc type "PASSED" and passed=true
			ConsentTemplateDTO consentTemplateDTO = createConsentTemplateDTO(List.of(moduleDTO));
			dataAccessDispatcher.addConsentTemplate(consentTemplateDTO, false);

			ConsentDTO consentDTO = createConsentDto();
			QCDTO qc = new QCDTO(true, "PASSED", new Date(), "Mr Tester", "previousComment", null, null, null);
			consentDTO.setQualityControl(qc);
			dataAccessDispatcher.addConsent(consentDTO);

			// Change qc type to "FAILED", keep passed=true
			qc.setType("FAILED");
			assertTrue(qc.isQcPassed());

			// DAO changes qc.passed depending on qc.type (mocked this behavior with updateQcPassed())
			Consent consent = createConsent(consentDTO, domain, policyDTO, moduleDTO, consentTemplateDTO);
			when(dao.setQcForConsent(any(), any(), any()))
					.thenAnswer(invocation -> new QC(consent, updateQcPassed(invocation.getArgument(1, QCDTO.class)), null));

			// Act
			dataAccessDispatcher.setQcForConsent(consentDTO.getKey(), qc, "gICS_Web");

			// Assert
			// Capture notification content
			ArgumentCaptor<Map<String, Serializable>> contextArgumentCaptor = ArgumentCaptor.forClass(Map.class);
			verify(consentNotificationSender).sendNotification(any(), any(), any(), any(), any(), any(), contextArgumentCaptor.capture());
			QCDTO qcResult = (QCDTO) contextArgumentCaptor.getValue().get("qc");
			assertEquals("FAILED", qcResult.getType());
			assertFalse(qcResult.isQcPassed()); // should no longer be passed=true
		}
	}

	private QCDTO updateQcPassed(QCDTO qcdto)
	{
		return new QCDTO(qcdto.getType().equals("PASSED"), qcdto.getType(), qcdto.getDate(), qcdto.getInspector(), qcdto.getComment(), qcdto.getExternProperties(), qcdto.getProblems(),
				qcdto.getFhirID());
	}
}