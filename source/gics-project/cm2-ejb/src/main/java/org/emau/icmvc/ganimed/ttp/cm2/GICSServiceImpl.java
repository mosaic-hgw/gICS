package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

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
	protected static final Logger logger = LogManager.getLogger(GICSServiceImpl.class);

	@Override
	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException, InternalException, RequirementsNotFullfilledException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + consentDTO);
		}
		dao.addConsent(consentDTO);
		if (logger.isInfoEnabled())
		{
			logger.info("added " + consentDTO);
		}
	}

	@Override
	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config) throws UnknownDomainException,
			UnknownPolicyException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return dao.isConsented(signerIdDTOs, policyKeyDTO, config);
	}

	@Override
	public boolean isConsentedFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return ConsentStatusType.ACCEPTED.equals(dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, 0, config));
	}

	@Override
	public boolean isConsentedFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return ConsentStatusType.ACCEPTED.equals(dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, 0, config));
	}

	@Override
	public boolean isConsentedFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return ConsentStatusType.ACCEPTED.equals(dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, -1, config));
	}

	@Override
	public boolean isConsentedFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return ConsentStatusType.ACCEPTED.equals(dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, -1, config));
	}

	@Override
	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException,
			UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get consent status type for ");
			sb.append(policyKeyDTO);
			sb.append(" signed by:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("using ");
			sb.append(config);
			logger.info(sb.toString());
		}
		return dao.getConsentStatusType(signerIdDTOs, policyKeyDTO, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, 0, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, 0, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, -1, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		return dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, -1, config);
	}

	@Override
	public List<ConsentTemplateDTO> listConsentTemplates(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list " + (onlyFinal ? "final" : "all") + " consent templates for domain " + domainName);
		}
		return dao.listConsentTemplates(domainName, onlyFinal);
	}

	@Override
	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName)
			throws UnknownDomainException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list current consent templates for domain " + domainName);
		}
		return dao.listCurrentConsentTemplates(domainName);
	}

	@Override
	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidVersionException, InvalidPropertiesException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + consentTemplateDTO);
		}
		dao.addConsentTemplate(consentTemplateDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(consentTemplateDTO + " added");
		}
	}

	@Override
	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			ObjectInUseException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		dao.deleteConsentTemplate(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public List<ModuleDTO> listModules(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list " + (onlyFinal ? "final" : "all") + " modules for domain " + domainName);
		}
		return dao.listModules(domainName, onlyFinal);
	}

	@Override
	public void addModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + moduleDTO);
		}
		dao.addModule(moduleDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(moduleDTO + " added");
		}
	}

	@Override
	public void deleteModule(ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		dao.deleteModule(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get module dto for " + keyDTO);
		}
		return dao.getModule(keyDTO);
	}

	@Override
	public List<PolicyDTO> listPolicies(String domainName, boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list " + (onlyFinal ? "final" : "all") + " policies for domain " + domainName);
		}
		return dao.listPolicies(domainName, onlyFinal);
	}

	@Override
	public void addPolicy(PolicyDTO policyDTO) throws DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + policyDTO);
		}
		dao.addPolicy(policyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(policyDTO + " added");
		}
	}

	@Override
	public void deletePolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		dao.deletePolicy(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get policy dto for " + keyDTO);
		}
		return dao.getPolicy(keyDTO);
	}

	@Override
	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get consent template dto for " + keyDTO);
		}
		return dao.getConsentTemplate(keyDTO);
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = dao.getPolicyStatesForSignerIds(domainName, signerIdDTOs, useAliases);
		return result;
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("for " + policyKeyDTO);
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = dao.getPolicyStatesForPolicyAndSignerIds(policyKeyDTO, signerIdDTOs, useAliases);
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("for " + policyKeyDTO);
			logger.info(sb.toString());
		}
		return result;
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("for policy name '" + policyName + "' within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = dao.getPolicyStatesForPolicyNameAndSignerIds(domainName, policyName, signerIdDTOs, useAliases);
		return result;
	}

	@Override
	public List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("with config '" + config + "' within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = new ArrayList<>();
		try
		{
			result = dao.getCurrentPolicyStatesForSignerIds(domainName, signerIdDTOs, config);
		}
		catch (InvalidVersionException e)
		{
			logger.fatal("unexpected exception while getCurrentPolicyStatesForSignerIds", e);
		}
		return result;
	}

	@Override
	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get newest consent template of '" + consentTemplateName + "' for domain '" + domainName + "'");
		}
		return dao.getCurrentConsentTemplate(consentTemplateName, domainName);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownDomainException,
			InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get all consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<ConsentLightDTO> result = dao.getAllConsentsForSignerIds(domainName, signerIdDTOs, useAliases);
		return result;
	}

	@Override
	public ConsentDTO getCurrentConsentForSignerIdsAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber, boolean useAliases)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, InconsistentStatusException
	{
		ConsentDTO result = null;
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("get current consent for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("and " + ctKeyDTO);
			logger.info(sb.toString());
		}
		result = dao.getCurrentConsentForSignerIdsAndCT(ctKeyDTO, signerIdDTOs, ignoreVersionNumber, useAliases);
		if (logger.isInfoEnabled())
		{
			if (result != null)
			{
				StringBuilder sb = new StringBuilder("found " + result.getKey() + " for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("and " + ctKeyDTO);
				logger.info(sb.toString());
			}
			else
			{
				StringBuilder sb = new StringBuilder("no consent found for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("for " + ctKeyDTO);
				logger.info(sb.toString());
			}
		}
		return result;
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidVersionException, InconsistentStatusException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get all consents for consent template '" + ctKeyDTO);
		}
		return dao.getAllConsentsForConsentTemplate(ctKeyDTO);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get all consents for domain '" + domainName + "'");
		}
		return dao.getAllConsentsForDomain(domainName);
	}

	@Override
	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, PaginationConfig config) throws InconsistentStatusException, InvalidParameterException,
			InvalidVersionException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get consents paginated for domain '" + domainName + "', first entry " + config.getFirstEntry() + ", page size "
					+ config.getPageSize());
		}
		List<ConsentLightDTO> result = dao.getConsentsForDomainPaginated(domainName, config);
		if (logger.isInfoEnabled())
		{
			logger.info("found " + result.size() + " consents for the requested page for domain " + domainName);
		}
		return result;
	}

	@Override
	public long countConsentsForDomainWithFilter(String domainName, PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("count consents for domain '" + domainName + "', with " + config.getFilter().size() + " filter criterions");
		}
		long result = dao.countConsentsForDomainWithFilter(domainName, config);
		if (logger.isInfoEnabled())
		{
			logger.info("found " + result + " consents for domain " + domainName);
		}
		return result;
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get all consents for domain '" + domainName + "' which doesn't have a scan attached");
		}
		return dao.getAllConsentsForDomainWithoutScan(domainName);
	}

	@Override
	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get consent template dto for " + keyDTO);
		}
		return dao.getConsent(keyDTO);
	}

	@Override
	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("create " + domainDTO);
		}
		dao.addDomain(domainDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(domainDTO + " created");
		}
	}

	@Override
	public void deleteDomain(String domainName) throws ObjectInUseException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete domain with name " + domainName);
		}
		dao.deleteDomain(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("domain with name '" + domainName + "' deleted");
		}
	}

	@Override
	public DomainDTO getDomain(String domainName) throws UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get domain with name " + domainName);
		}
		DomainDTO result = dao.getDomain(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("domain with name " + domainName + " found");
		}
		return result;
	}

	@Override
	public List<DomainDTO> listDomains()
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list all domains");
		}
		List<DomainDTO> result = dao.listDomains();
		if (logger.isInfoEnabled())
		{
			logger.info("found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public void updateDomain(DomainDTO dto) throws ObjectInUseException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update domain with name " + dto.getName());
		}
		dao.updateDomain(dto);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + dto.getName() + "' updated");
		}
	}

	@Override
	public void updateDomainInUse(String domainName, String label, String logo, String externProperties, String comment)
			throws UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update domain with name " + domainName);
		}
		dao.updateDomainInUse(domainName, label, logo, externProperties, comment);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + domainName + "' updated");
		}
	}

	@Override
	public void updatePolicy(PolicyDTO dto) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		dao.updatePolicy(dto);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updatePolicyInUse(PolicyKeyDTO keyDTO, String label, String externProperties, String comment)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + keyDTO);
		}
		dao.updatePolicyInUse(keyDTO, label, externProperties, comment);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void updateModule(ModuleDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidParameterException, InvalidVersionException,
			ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		dao.updateModule(dto, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updateModuleInUse(ModuleKeyDTO keyDTO, String label, String shortText, String externProperties, String comment, Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("update ");
			sb.append(keyDTO);
			sb.append(" with label '");
			sb.append(label);
			sb.append("' short text '");
			sb.append(shortText);
			sb.append("' extern properties '");
			sb.append(externProperties);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("'");
			logger.info(sb.toString());
		}
		dao.updateModuleInUse(keyDTO, label, shortText, externProperties, comment, assignedPolicyDTOs);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void updateConsentTemplate(ConsentTemplateDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException,
			FreeTextConverterStringException, InvalidFreeTextException, InvalidPropertiesException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		dao.updateConsentTemplate(dto, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updateConsentTemplateInUse(ConsentTemplateDTO dto) throws InvalidFreeTextException, InvalidVersionException,
			UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		dao.updateConsentTemplateInUse(dto);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updateConsentInUse(ConsentKeyDTO keyDTO, String externProperties, String comment, ConsentScanDTO scan)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + createLogMessageForUpdateConsent(keyDTO, externProperties, comment, scan));
		}
		dao.updateConsentInUse(keyDTO, externProperties, comment, scan);
		if (logger.isInfoEnabled())
		{
			logger.info("updated " + createLogMessageForUpdateConsent(keyDTO, externProperties, comment, scan));
		}
	}

	@Override
	public void finaliseDomain(String domainName) throws UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise domain with name '" + domainName);
		}
		dao.finaliseDomain(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + domainName + " finalised");
		}
	}

	@Override
	public void finalisePolicy(PolicyKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO);
		}
		dao.finalisePolicy(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " finalised");
		}
	}

	@Override
	public void finaliseModule(ModuleKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO + (finaliseRelatedEntities ? " and related entities" : ""));
		}
		dao.finaliseModule(keyDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + (finaliseRelatedEntities ? " and related entities " : "") + " finalised");
		}
	}

	@Override
	public void finaliseTemplate(ConsentTemplateKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO + (finaliseRelatedEntities ? " and related entities" : ""));
		}
		dao.finaliseTemplate(keyDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + (finaliseRelatedEntities ? " and related entities " : "") + " finalised");
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
		dao.addScanToConsent(consentKeyDTO, scanBase64, fileType, fileName);
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

		dao.removeScanFromConsent(consentKeyDTO, fhirId);
	}

	@Override
	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add signer id type " + signerIdTypeName + " to domain " + domainName);
		}
		dao.addSignerIdType(domainName, signerIdTypeName);
		if (logger.isInfoEnabled())
		{
			logger.info("signer id type " + signerIdTypeName + " successfully added to domain " + domainName);
		}
	}

	@Override
	public SignerIdTypeDTO getSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("get signerIdType with name " + signerIdTypeName + " within domain " + domainName);
		}
		SignerIdTypeDTO result = dao.getSignerIdTypeDTO(domainName, signerIdTypeName);
		if (logger.isInfoEnabled())
		{
			logger.info("signerIdType with name " + signerIdTypeName + " within domain " + domainName + " found");
		}
		return result;
	}

	@Override
	public void updateSignerIdType(String domainName, String signerIdTypeName, String label, String comment) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update signerIdType with name " + signerIdTypeName + " within domain " + domainName);
		}
		dao.updateSignerIdType(domainName, signerIdTypeName, label, comment);
		if (logger.isInfoEnabled())
		{
			logger.info("signerIdType " + signerIdTypeName + " within domain " + domainName + " updated");
		}
	}

	@Override
	public List<SignerIdTypeDTO> listSignerIdTypes(String domainName) throws UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list all signerIdTypes for domain " + domainName);
		}
		List<SignerIdTypeDTO> result = dao.listSignerIdTypes(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("found " + result.size() + " signerIdTypes for domain " + domainName);
		}
		return result;
	}

	@Override
	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("remove signer id type " + signerIdTypeName + " from domain " + domainName);
		}
		dao.deleteSignerIdType(domainName, signerIdTypeName);
		if (logger.isInfoEnabled())
		{
			logger.info("signer id type " + signerIdTypeName + " removed from domain " + domainName);
		}
	}

	@Override
	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException
	{
		List<String> result = dao.getAllIdsForSignerIdType(domainName, signerIdTypeName);
		return result;
	}

	@Override
	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException,
			InvalidVersionException, InconsistentStatusException
	{
		List<String> result = dao.getAllConsentedIdsFor(signerIdTypeName, policyKeyDTO, config);
		return result;
	}

	@Override
	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("validate " + consentDTO);
		}
		dao.validateConsent(consentDTO, allowRevoke);
		if (logger.isInfoEnabled())
		{
			logger.info(consentDTO + " is valide");
		}
	}

	@Override
	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException,
			UnknownDomainException, InternalException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("refuse " + createLogMessageForRefuseConsent(ctKeyDTO, signerIdDTOs));
		}
		dao.refuseConsent(ctKeyDTO, signerIdDTOs);
		if (logger.isInfoEnabled())
		{
			logger.info("refused " + createLogMessageForRefuseConsent(ctKeyDTO, signerIdDTOs));
		}
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

		if (consentKeyDTO == null || signerIdDTO == null)
		{
			String msg = "unable to addSignerId. The given parameter consentKey or signerId is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.addSignerIdToConsent(consentKeyDTO, signerIdDTO);
	}

	@Override
	public ConsentDateValuesDTO getConsentDates(ConsentKeyDTO consentKeyDTO) throws InvalidVersionException,
			UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("getting expiration dates for " + consentKeyDTO);
		}
		ConsentDateValuesDTO result = dao.getConsentDates(consentKeyDTO);
		if (logger.isDebugEnabled())
		{
			logger.debug("found expiration dates for " + consentKeyDTO);
		}
		return result;
	}

	@Override
	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, InternalException, UnknownDomainException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add signedId " + newSignerIdDTO + " to virtual person using signer id " + existentSignerIdDTO);
		}

		if (existentSignerIdDTO == null || newSignerIdDTO == null)
		{
			String msg = "unable to add signer si. The given parameter newSignerIdDTO or existentSignerIdDTO is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.addSignerIdToSignerId(domain, existentSignerIdDTO, newSignerIdDTO);

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

		if (originalSignerId == null || aliasSignerId == null)
		{
			String msg = "unable to add alias. The given parameter originalSignerId or aliasSignerId is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.addAlias(domainName, originalSignerId, aliasSignerId);

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

		if (originalSignerId == null || aliasSignerId == null)
		{
			String msg = "unable to deactivate alias. The given parameter originalSignerId or aliasSignerId is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.deactivateAlias(domainName, originalSignerId, aliasSignerId);

		if (logger.isInfoEnabled())
		{
			logger.info("deactivated alias for signer id " + originalSignerId + " and signer id " + aliasSignerId);
		}
	}

	@Override
	public List<SignerIdDTO> getAliasesForSignerId(String domainName, SignerIdDTO originalSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("list aliases for signer id " + originalSignerId);
		}

		if (originalSignerId == null)
		{
			String msg = "unable to list aliases. The given parameter originalSignerId is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		List<SignerIdDTO> result = dao.listAliases(domainName, originalSignerId);

		if (logger.isInfoEnabled())
		{
			logger.info("list aliases for signer id " + originalSignerId + " returns " + result.size() + " aliases");
		}
		return result;
	}

	@Override
	public void setQCForConsent(ConsentKeyDTO consentKeyDTO, QCDTO qc)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException,
			UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("setting quality control for " + consentKeyDTO);
		}
		dao.setQcForConsent(consentKeyDTO, qc);
		if (logger.isDebugEnabled())
		{
			logger.debug("done setting quality control for " + consentKeyDTO);
		}
	}

	@Override
	public List<QCHistoryDTO> getQCHistoryForConsent(ConsentKeyDTO consentKeyDTO)
			throws InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("getQCHistoryForConsent " + consentKeyDTO);
		}
		List<QCHistoryDTO> result = dao.getQCHistoryForConsent(consentKeyDTO);
		if (logger.isDebugEnabled())
		{
			logger.debug("getQCHistoryForConsent " + consentKeyDTO + " returned " + result.size() + " entries");
		}
		return result;
	}

	@Override
	public <T extends FhirIdDTO> T getObjectByFhirID(Class<T> clazz, String fhirID) throws UnknownFhirIdObjectException, UnknownIDException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("getObjectByFhirID, class=" + clazz.getName() + ", fhirID=" + fhirID);
		}
		T result = dao.getObjectByFhirID(clazz, fhirID);
		if (logger.isDebugEnabled())
		{
			logger.debug("getObjectByFhirID, class=" + clazz.getName() + ", fhirID=" + fhirID + " returned " + result);
		}
		return result;
	}
}
