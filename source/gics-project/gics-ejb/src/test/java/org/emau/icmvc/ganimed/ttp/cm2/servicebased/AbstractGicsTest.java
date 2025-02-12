package org.emau.icmvc.ganimed.ttp.cm2.servicebased;

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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DAO;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPersonSignerId;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

public abstract class AbstractGicsTest
{
	// Domain
	protected static final String DOMAIN = "TEST_DOMAIN";
	protected static final String SIGNER_ID_TYPE = "TEST_SIGNER_ID_TYPE";
	protected static final String VERSION_CONVERTER = "org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter";

	// Template
	protected static final String TEMPLATE = "TEST_TEMPLATE";
	protected static final String MODULE = "TEST_MODULE";
	protected static final String POLICY = "TEST_POLICY";
	protected static final String VERSION = "1.0";
	protected final Date NOW = new Date();
	protected final Date NOW_AT_START_OF_DAY = DateUtils.truncate(new Date(), Calendar.DATE);
	protected final Date FAR_FUTURE_AT_START_OF_DAY = DateUtils.truncate(new Date((long) Integer.MAX_VALUE * 1000), Calendar.DATE);

	// Consent
	protected static final String SIGNER_ID = "TEST_SIGNER_ID";
	protected static final String SCAN_BASE64 = "scan";
	protected static final String SCAN_FILETYPE = "application/pdf";
	protected static final String SCAN_FILENAME = "test.pdf";
	protected static final String SIGNATURE_BASE64 = "signature";

	// Version Converter
	private VersionConverter TEMPLATE_VERSION_CONVERTER;
	private VersionConverter MODULE_VERSION_CONVERTER;
	private VersionConverter POLICY_VERSION_CONVERTER;

	protected DomainDTO createDomainDto()
	{
		return new DomainDTO(DOMAIN, VERSION_CONVERTER, VERSION_CONVERTER, VERSION_CONVERTER, List.of(SIGNER_ID_TYPE));
	}

	protected SignerIdTypeDTO createSignerIdTypeDTO()
	{
		return new SignerIdTypeDTO(SIGNER_ID_TYPE);
	}

	protected SignerIdDTO createSignerIdDTO()
	{
		return new SignerIdDTO(SIGNER_ID_TYPE, SIGNER_ID);
	}

	protected PolicyDTO createPolicyDTO()
	{
		PolicyKeyDTO policyKeyDTO = new PolicyKeyDTO(DOMAIN, POLICY, VERSION);
		PolicyDTO policyDTO = new PolicyDTO(policyKeyDTO);
		policyDTO.setFinalised(true);
		return policyDTO;
	}

	protected ModuleDTO createModuleDTO()
	{
		ModuleKeyDTO moduleKeyDTO = new ModuleKeyDTO(DOMAIN, MODULE, VERSION);
		ModuleDTO moduleDTO = new ModuleDTO(moduleKeyDTO);
		moduleDTO.setFinalised(true);
		return moduleDTO;
	}

	protected ModuleDTO createModuleDTO(List<PolicyDTO> policyDTOS)
	{
		ModuleDTO moduleDTO = createModuleDTO();
		for (PolicyDTO policyDTO : policyDTOS)
		{
			moduleDTO.getAssignedPolicies().add(new AssignedPolicyDTO(policyDTO));
		}
		return moduleDTO;
	}

	protected ConsentTemplateDTO createConsentTemplateDTO(String name)
	{
		ConsentTemplateKeyDTO consentTemplateKeyDTO = new ConsentTemplateKeyDTO(DOMAIN, name, VERSION);
		return new ConsentTemplateDTO(consentTemplateKeyDTO);
	}

	protected ConsentTemplateDTO createConsentTemplateDTO(List<ModuleDTO> moduleDTOS)
	{
		return createConsentTemplateDTO(TEMPLATE, moduleDTOS);
	}

	protected ConsentTemplateDTO createConsentTemplateDTO(String name, List<ModuleDTO> moduleDTOS)
	{
		ConsentTemplateDTO consentTemplateDTO = createConsentTemplateDTO(name);
		for (ModuleDTO moduleDTO : moduleDTOS)
		{
			consentTemplateDTO.getAssignedModules().add(new AssignedModuleDTO(moduleDTO, null));
		}
		consentTemplateDTO.setFinalised(true);
		return consentTemplateDTO;
	}

	protected ConsentDTO createConsentDto()
	{
		return createConsentDto(createConsentTemplateDTO(TEMPLATE));
	}

	protected ConsentDTO createConsentDto(ConsentTemplateDTO consentTemplateDTO)
	{
		return createConsentDto(consentTemplateDTO, false, true, true);
	}

	protected ConsentDTO createConsentDto(ConsentTemplateDTO consentTemplateDTO, boolean withScan, boolean withPatientSignature, boolean withPhysicianSignature)
	{
		return createConsentDto(consentTemplateDTO, withScan, withPatientSignature, withPhysicianSignature, true, true);
	}

	protected ConsentDTO createConsentDto(ConsentTemplateDTO consentTemplateDTO, boolean withScan, boolean withPatientSignature, boolean withPhysicianSignature, boolean withPatientSignatureDate,
			boolean withPhysicianSignatureDate)
	{
		SignerIdDTO signerIdDTO = new SignerIdDTO(SIGNER_ID_TYPE, SIGNER_ID);
		ConsentKeyDTO consentKeyDTO = new ConsentKeyDTO(consentTemplateDTO.getKey(), new HashSet<>(List.of(signerIdDTO)), NOW);
		ConsentDTO consentDTO = new ConsentDTO(consentKeyDTO);
		Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<>();
		for (AssignedModuleDTO assignedModuleDTO : consentTemplateDTO.getAssignedModules())
		{
			moduleStates.put(assignedModuleDTO.getModule().getKey(), new ModuleStateDTO(assignedModuleDTO.getModule().getKey(), ConsentStatus.ACCEPTED,
					assignedModuleDTO.getModule().getAssignedPolicies().stream().map(ap -> ap.getPolicy().getKey()).collect(
							Collectors.toList())));
		}
		consentDTO.setModuleStates(moduleStates);
		if (withPatientSignatureDate)
			consentDTO.setPhysicianSigningDate(NOW);
		if (withPhysicianSignatureDate)
			consentDTO.setPatientSigningDate(NOW);

		if (withScan)
		{
			ConsentScanDTO consentScanDTO = new ConsentScanDTO(consentKeyDTO, SCAN_BASE64, SCAN_FILETYPE, SCAN_FILENAME, NOW);
			consentDTO.setScans(List.of(consentScanDTO));
		}

		if (withPatientSignature)
		{
			consentDTO.setPatientSignatureBase64(SIGNATURE_BASE64);
		}

		if (withPhysicianSignature)
		{
			consentDTO.setPhysicianSignatureBase64(SIGNATURE_BASE64);
		}
		return consentDTO;
	}

	protected Domain createDomain(DomainDTO domainDTO) throws VersionConverterClassException
	{
		return new Domain(domainDTO);
	}

	protected SignerId createSignerId(Domain domain, SignerIdDTO signerIdDTO, SignerIdTypeDTO signerIdTypeDTO, DAO dao) throws UnknownSignerIdTypeException
	{
		SignerId signerId = new SignerId(createSignerIdType(domain, signerIdTypeDTO), signerIdDTO.getId());
		VirtualPersonSignerId virtualPersonSignerId = new VirtualPersonSignerId(dao.createVirtualPerson(domain, Set.of(signerIdDTO)), signerId);
		signerId.setVirtualPersonSignerIds(List.of(virtualPersonSignerId));
		return signerId;
	}

	protected SignerIdType createSignerIdType(Domain domain, SignerIdTypeDTO signerIdTypeDTO)
	{
		return new SignerIdType(domain, signerIdTypeDTO.getName());
	}

	protected Policy createPolicy(Domain domain, PolicyDTO policyDTO) throws InvalidVersionException, UnknownDomainException
	{
		return new Policy(domain, policyDTO);
	}

	protected Module createModule(Domain domain, ModuleDTO moduleDTO, List<Policy> policies) throws InvalidVersionException, UnknownDomainException, RequirementsNotFullfilledException
	{
		Module module = new Module(domain, moduleDTO, true);
		for (Policy policy : policies)
		{
			module.getModulePolicies().add(new ModulePolicy(module, policy, Objects.requireNonNull(moduleDTO.getAssignedPolicies().stream().filter(ap -> {
				try
				{
					return policy.toDTO().getKey().equals(ap.getPolicy().getKey());
				}
				catch (InvalidVersionException | UnknownDomainException e)
				{
					throw new RuntimeException(e);
				}
			}).findAny().orElse(null))));
		}
		//		Map<ModuleKeyDTO, Module> emModules = new HashMap<>();
		//		emModules.put(moduleDTO.getKey(), module);
		return module;
	}

	protected ConsentTemplate createConsentTemplate(Domain domain, ConsentTemplateDTO consentTemplateDTO, List<Module> modules)
			throws InvalidParameterException, InvalidPropertiesException, InvalidVersionException, RequirementsNotFullfilledException, UnknownModuleException, UnknownDomainException,
			FreeTextConverterStringException
	{
		//		Map<ConsentTemplateKeyDTO, ConsentTemplate> emTemplates = new HashMap<>();
		Map<ModuleKeyDTO, Module> modulesMap = modules.stream().collect(Collectors.toMap(k -> {
			try
			{
				return k.toDTO().getKey();
			}
			catch (InvalidVersionException | UnknownDomainException e)
			{
				throw new RuntimeException(e);
			}
		}, k -> k));
		return new ConsentTemplate(domain, consentTemplateDTO, modulesMap, new HashMap<>(), true);
		//		emTemplates.put(consentTemplateDTO.getKey(), emTemplate);
	}

	protected Consent createConsent(ConsentDTO consentDTO, Domain domain, PolicyDTO policyDTO, ModuleDTO moduleDTO, ConsentTemplateDTO consentTemplateDTO)
			throws InvalidVersionException, UnknownDomainException, RequirementsNotFullfilledException, InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException,
			InvalidPropertiesException, UnknownModuleException, FreeTextConverterStringException
	{
		Policy policy = createPolicy(domain, policyDTO);
		Module module = createModule(domain, moduleDTO, List.of(policy));
		ConsentTemplate consentTemplate = createConsentTemplate(domain, consentTemplateDTO, List.of(module));
		return createConsent(consentTemplate, consentDTO, List.of(module));
	}

	protected Consent createConsent(ConsentTemplate consentTemplate, ConsentDTO consentDTO, List<Module> domainModules)
			throws InvalidParameterException, InvalidFreeTextException, MissingRequiredObjectException
	{
		Map<ModuleKeyDTO, Module> moduleMap = new HashMap<>();
		for (ModuleKeyDTO moduleKeyDTO : consentDTO.getModuleStates().keySet())
		{
			moduleMap.put(moduleKeyDTO, domainModules.stream().filter(m -> {
				try
				{
					return m.toDTO().getKey().equals(moduleKeyDTO);
				}
				catch (InvalidVersionException | UnknownDomainException e)
				{
					throw new RuntimeException(e);
				}
			}).findAny().orElse(null));
		}

		return new Consent(consentTemplate, consentDTO, moduleMap, new VirtualPerson(), null);
	}

	protected void initVersionConverters(Domain domain) throws VersionConverterClassException, UnknownDomainException
	{
		VersionConverterCache.init(List.of(domain));
		TEMPLATE_VERSION_CONVERTER = VersionConverterCache.getCTVersionConverter(domain.getName());
		MODULE_VERSION_CONVERTER = VersionConverterCache.getModuleVersionConverter(domain.getName());
		POLICY_VERSION_CONVERTER = VersionConverterCache.getPolicyVersionConverter(domain.getName());
	}

	protected ConsentCache.CachedSignedPolicy createCachedSignedPolicy(long gicsConsentDate, long legalConsentDate, long expirationDate, ConsentStatus consentStatus, ConsentTemplateKey tKey,
			Long vpId, PolicyKey pKey)
	{
		return new ConsentCache.CachedSignedPolicy(
				new SignedPolicyKey(new ConsentKey(tKey, new Date(), vpId), pKey),
				gicsConsentDate, legalConsentDate, consentStatus, expirationDate);
	}

	protected ConsentCache.CachedSignedPolicy createCachedSignedPolicy(long gicsConsentDate, long legalConsentDate, long expirationDate, ConsentStatus consentStatus, String domain, String tName, String pName, Long vpId)
	{
		return new ConsentCache.CachedSignedPolicy(
				new SignedPolicyKey(new ConsentKey(new ConsentTemplateKey(domain, tName, 1000), new Date(), vpId), new PolicyKey(domain, pName, 1000)),
				gicsConsentDate, legalConsentDate, consentStatus, expirationDate);
	}
}
