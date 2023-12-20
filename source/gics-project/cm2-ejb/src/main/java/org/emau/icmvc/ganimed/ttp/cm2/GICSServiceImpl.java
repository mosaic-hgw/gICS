package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FhirIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownAliasException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownFhirIdObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownIDException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;

/**
 * implementation of the gics-service
 *
 * @author geidell
 *
 */
@WebService(name = "gicsService")
@Remote(GICSService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class GICSServiceImpl extends GICSServiceBase implements GICSService
{
	@Override
	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			InvalidParameterException, InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException, RequirementsNotFullfilledException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + consentDTO);
		}
		checkParameter(consentDTO, "consentDTO");
		checkAllowedDomain(consentDTO);
		dad.addConsent(consentDTO);
		if (logger.isInfoEnabled())
		{
			logger.info("added " + consentDTO);
		}
	}

	@Override
	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config) throws UnknownDomainException,
			UnknownPolicyException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(policyKeyDTO, "policyKeyDTO");
		checkAllowedDomain(policyKeyDTO);
		return dad.isConsented(signerIdDTOs, policyKeyDTO, config);
	}

	@Override
	public boolean isConsentedFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		return ConsentStatusType.ACCEPTED.equals(dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, 0, config));
	}

	@Override
	public boolean isConsentedFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		checkAllowedDomain(domainName);
		return ConsentStatusType.ACCEPTED.equals(dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, 0, config));
	}

	@Override
	public boolean isConsentedFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		checkAllowedDomain(domainName);
		return ConsentStatusType.ACCEPTED.equals(dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, -1, config));
	}

	@Override
	public boolean isConsentedFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		checkAllowedDomain(domainName);
		return ConsentStatusType.ACCEPTED.equals(dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, -1, config));
	}

	@Override
	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownPolicyException, InvalidParameterException, InvalidVersionException,
			UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(policyKeyDTO, "policyKeyDTO");
		checkAllowedDomain(policyKeyDTO);
		return dad.getConsentStatusType(signerIdDTOs, policyKeyDTO, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		return dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, 0, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		return dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, 0, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		return dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, -1, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkAllowedDomain(domainName);
		return dad.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, -1, config);
	}

	@Override
	public List<ConsentTemplateDTO> listConsentTemplates(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.listConsentTemplates(domainName, onlyFinal);
	}

	@Override
	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.listCurrentConsentTemplates(domainName);
	}

	@Override
	public List<ModuleDTO> listModules(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.listModules(domainName, onlyFinal);
	}

	@Override
	public ModuleDTO getModule(ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		return dad.getModule(keyDTO);
	}

	@Override
	public List<PolicyDTO> listPolicies(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.listPolicies(domainName, onlyFinal);
	}

	@Override
	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		return dad.getPolicy(keyDTO);
	}

	@Override
	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException
	{
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		return dad.getConsentTemplate(keyDTO);
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(domainName);
		return dad.getPolicyStatesForSignerIds(domainName, signerIdDTOs, useAliases);
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(policyKeyDTO, "policyKeyDTO");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(policyKeyDTO);
		return dad.getPolicyStatesForPolicyAndSignerIds(policyKeyDTO, signerIdDTOs, useAliases);
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(policyName, "policyName");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(domainName);
		return dad.getPolicyStatesForPolicyNameAndSignerIds(domainName, policyName, signerIdDTOs, useAliases);
	}

	@Override
	public List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, CheckConsentConfig config)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		try
		{
			checkParameter(domainName, "domainName");
			checkParameter(signerIdDTOs, "signerIdDTOs");
			checkAllowedDomain(domainName);
			return dad.getCurrentPolicyStatesForSignerIds(domainName, signerIdDTOs, config);
		}
		catch (InvalidVersionException e)
		{
			logger.fatal("unexpected exception while getCurrentPolicyStatesForSignerIds", e);
			return new ArrayList<>();
		}
	}

	@Override
	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		checkParameter(consentTemplateName, "consentTemplateName");
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getCurrentConsentTemplate(consentTemplateName, domainName);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownDomainException,
			InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException
	{
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getAllConsentsForSignerIds(domainName, signerIdDTOs, useAliases);
	}

	@Override
	public ConsentDTO getCurrentConsentForSignerIdsAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber, boolean useAliases)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException
	{
		checkParameter(ctKeyDTO, "ctKeyDTO");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(ctKeyDTO);
		return dad.getCurrentConsentForSignerIdsAndCT(ctKeyDTO, signerIdDTOs, ignoreVersionNumber, useAliases);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException, InconsistentStatusException
	{
		checkParameter(ctKeyDTO, "ctKeyDTO");
		checkAllowedDomain(ctKeyDTO);
		return dad.getAllConsentsForConsentTemplate(ctKeyDTO);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getAllConsentsForDomain(domainName);
	}

	@Override
	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, PaginationConfig config) throws InconsistentStatusException, InvalidParameterException,
			InvalidVersionException, UnknownDomainException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getConsentsForDomainPaginated(domainName, config);
	}

	@Override
	public long countConsentsForDomainWithFilter(String domainName, PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.countConsentsForDomainWithFilter(domainName, config);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, InconsistentStatusException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getAllConsentsForDomainWithoutScan(domainName);
	}

	@Override
	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, InvalidParameterException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		return dad.getConsent(keyDTO);
	}

	@Override
	public ConsentLightDTO getConsentLight(ConsentKeyDTO keyDTO) throws UnknownDomainException, InvalidParameterException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		return dad.getConsentLight(keyDTO);
	}

	@Override
	public DomainDTO getDomain(String domainName) throws InvalidParameterException, UnknownDomainException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.getDomain(domainName);
	}

	@Override
	public List<DomainDTO> listDomains()
	{
		return filterAllowedDomains(dad.listDomains());
	}

	@Override
	public void updateConsentInUse(ConsentKeyDTO consentKeyDTO, String externProperties, String comment, ConsentScanDTO scan)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException,
			UnknownSignerIdTypeException, UnknownConsentException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + consentKeyDTO);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.updateConsentInUse(consentKeyDTO, externProperties, comment, scan);
		if (logger.isInfoEnabled())
		{
			logger.info(consentKeyDTO + " updated");
		}
	}

	@Override
	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType, String fileName)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException,
			InvalidVersionException, UnknownSignerIdTypeException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add scan to " + consentKeyDTO);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.addScanToConsent(consentKeyDTO, scanBase64, fileType, fileName);
		if (logger.isInfoEnabled())
		{
			logger.info("added scan to " + consentKeyDTO);
		}
	}

	@Override
	public void removeScanFromConsent(ConsentKeyDTO consentKeyDTO, String fhirId)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException, InvalidVersionException,
			UnknownSignerIdTypeException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("remove scan from " + consentKeyDTO);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.removeScanFromConsent(consentKeyDTO, fhirId);
		if (logger.isInfoEnabled())
		{
			logger.info("removed scan from " + consentKeyDTO);
		}
	}

	@Override
	public SignerIdTypeDTO getSignerIdType(String domainName, String signerIdTypeName) throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkAllowedDomain(domainName);
		return dad.getSignerIdTypeDTO(domainName, signerIdTypeName);
	}

	@Override
	public List<SignerIdTypeDTO> listSignerIdTypes(String domainName) throws InvalidParameterException, UnknownDomainException
	{
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		return dad.listSignerIdTypes(domainName);
	}

	@Override
	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkAllowedDomain(domainName);
		return dad.getAllIdsForSignerIdType(domainName, signerIdTypeName);
	}

	@Override
	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException,
			InvalidParameterException, InvalidVersionException, InconsistentStatusException
	{
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkParameter(policyKeyDTO, "policyKeyDTO");
		checkAllowedDomain(policyKeyDTO);
		return dad.getAllConsentedIdsFor(signerIdTypeName, policyKeyDTO, config);
	}

	@Override
	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException, InvalidParameterException
	{
		checkParameter(consentDTO, "consentDTO");
		checkAllowedDomain(consentDTO);
		dad.validateConsent(consentDTO, allowRevoke);
	}

	@Override
	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException,
			UnknownDomainException, InternalException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("refuse consent for " + ctKeyDTO + " for signer with ids " + Arrays.toString(signerIdDTOs.toArray()));
		}
		checkParameter(ctKeyDTO, "ctKeyDTO");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(ctKeyDTO);
		dad.refuseConsent(ctKeyDTO, signerIdDTOs);
	}

	/**
	 * add given signerID to given consent
	 *
	 * @throws InvalidParameterException
	 */
	@Override
	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException,
			UnknownConsentTemplateException, UnknownDomainException, InternalException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + signerIdDTO + " to " + consentKeyDTO);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkParameter(signerIdDTO, "signerIdDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.addSignerIdToConsent(consentKeyDTO, signerIdDTO);
		if (logger.isInfoEnabled())
		{
			logger.info("added " + signerIdDTO + " to " + consentKeyDTO);
		}
	}

	@Override
	public ConsentDateValuesDTO getConsentDates(ConsentKeyDTO consentKeyDTO) throws InvalidParameterException, InvalidVersionException,
			UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		return dad.getConsentDates(consentKeyDTO);
	}

	@Override
	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, InternalException, UnknownDomainException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add signedId " + newSignerIdDTO + " to virtual person using signer id " + existentSignerIdDTO);
		}

		checkParameter(domain, "domain");
		checkParameter(existentSignerIdDTO, "existentSignerIdDTO");
		checkParameter(newSignerIdDTO, "newSignerIdDTO");
		checkAllowedDomain(domain);
		dad.addSignerIdToSignerId(domain, existentSignerIdDTO, newSignerIdDTO);

		if (logger.isInfoEnabled())
		{
			logger.info("added signedId " + newSignerIdDTO + " to virtual person using signer id " + existentSignerIdDTO);
		}
	}

	@Override
	public void addAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add alias for signer id " + originalSignerId + " and signer id " + aliasSignerId);
		}

		checkParameter(domainName, "domainName");
		checkParameter(originalSignerId, "originalSignerId");
		checkParameter(aliasSignerId, "aliasSignerId");
		checkAllowedDomain(domainName);
		dad.addAlias(domainName, originalSignerId, aliasSignerId);

		if (logger.isInfoEnabled())
		{
			logger.info("added alias for signer id " + originalSignerId + " and signer id " + aliasSignerId);
		}
	}

	@Override
	public void deactivateAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws InvalidParameterException, UnknownAliasException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("deactivate alias for signer id " + originalSignerId + " and signer id " + aliasSignerId);
		}

		checkParameter(domainName, "domainName");
		checkParameter(originalSignerId, "originalSignerId");
		checkParameter(aliasSignerId, "aliasSignerId");
		checkAllowedDomain(domainName);
		dad.deactivateAlias(domainName, originalSignerId, aliasSignerId);

		if (logger.isInfoEnabled())
		{
			logger.info("deactivated alias for signer id " + originalSignerId + " and signer id " + aliasSignerId);
		}
	}

	@Override
	public List<SignerIdDTO> getAliasesForSignerId(String domainName, SignerIdDTO originalSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(originalSignerId, "originalSignerId");
		checkAllowedDomain(domainName);
		return dad.listAliases(domainName, originalSignerId);
	}

	@Override
	public Map<SignerIdDTO, SignerIdDTO[]> getAliasesForSignerIds(String domainName, List<SignerIdDTO> originalSignerIds)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(originalSignerIds, "originalSignerIds");
		checkAllowedDomain(domainName);
		return dad.mapAliases(domainName, originalSignerIds);
	}

	@Override
	public List<SignerIdDTO> getSignerIdsForAlias(String domainName, SignerIdDTO aliasSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		checkParameter(domainName, "domainName");
		checkParameter(aliasSignerId, "aliasSignerId");
		checkAllowedDomain(domainName);
		return dad.listSignerIdsForAlias(domainName, aliasSignerId);
	}

	@Override
	public void setQCForConsent(ConsentKeyDTO consentKeyDTO, QCDTO qc)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("setting quality control for " + consentKeyDTO);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.setQcForConsent(consentKeyDTO, qc);
		if (logger.isInfoEnabled())
		{
			logger.info("done setting quality control for " + consentKeyDTO);
		}
	}

	@Override
	public List<QCHistoryDTO> getQCHistoryForConsent(ConsentKeyDTO consentKeyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		return dad.getQCHistoryForConsent(consentKeyDTO);
	}

	@Override
	public List<ConsentTemplateKeyDTO> getTemplatesWithPolicies(List<PolicyKeyDTO> policyKeyDTOs)
			throws InvalidParameterException, UnknownDomainException, InvalidVersionException
	{
		checkParameter(policyKeyDTOs, "policyKeyDTOs");
		checkAllowedDomain(policyKeyDTOs);
		return dad.getTemplatesWithPolicies(new HashSet<>(policyKeyDTOs)).stream()
				.sorted(Comparator.comparing(ConsentTemplateKeyDTO::getName)).collect(Collectors.toList());
	}

	@Override
	public List<ConsentTemplateKeyDTO> getMappedTemplatesForSignerId(String domainName, ConsentTemplateType ctType, SignerIdDTO signerId, boolean useAliases)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException, InvalidParameterException
	{
		checkParameter(domainName, "domainName");
		checkParameter(ctType, "ctType");
		checkParameter(signerId, "signerId");
		checkAllowedDomain(domainName);
		return dad.getMappedTemplatesForSignerId(domainName, ctType, signerId, useAliases).stream().sorted(Comparator.comparing(ConsentTemplateKeyDTO::getName)).toList();
	}

	@Override
	public <T extends FhirIdDTO> T getObjectByFhirID(Class<T> clazz, String fhirID) throws UnknownFhirIdObjectException, UnknownIDException
	{
		T objectForFhirID = dad.getObjectByFhirID(clazz, fhirID);
		checkAllowedDomainForFHIRObject(objectForFhirID);
		return objectForFhirID;
	}
}
