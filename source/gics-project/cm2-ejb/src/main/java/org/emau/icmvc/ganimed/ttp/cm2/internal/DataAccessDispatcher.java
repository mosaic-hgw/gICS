package org.emau.icmvc.ganimed.ttp.cm2.internal;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FhirIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextValDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.StatisticException;
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
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentScan;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.FhirDTOExporter;
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextDef;
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextVal;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.QC;
import org.emau.icmvc.ganimed.ttp.cm2.model.QCHist;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.util.ConsentNotificationSender;

/**
 * access point for db and caches
 *
 * @author geidell
 */
@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class DataAccessDispatcher
{
	private final Logger LOGGER = LogManager.getLogger(DataAccessDispatcher.class);

	@EJB
	private ConsentNotificationSender notificationSender;
	@EJB
	private DAO dao;
	private static final ReentrantReadWriteLock ACCESS_RWL = new ReentrantReadWriteLock();

	private static final Map<String, Class<? extends FhirDTOExporter<? extends FhirIdDTO>>> fhirIdObjects = new HashMap<>();
	static
	{
		fhirIdObjects.put(DomainDTO.class.getName(), Domain.class);
		fhirIdObjects.put(ConsentTemplateDTO.class.getName(), ConsentTemplate.class);
		fhirIdObjects.put(FreeTextDefDTO.class.getName(), FreeTextDef.class);
		fhirIdObjects.put(FreeTextValDTO.class.getName(), FreeTextVal.class);
		fhirIdObjects.put(ModuleDTO.class.getName(), Module.class);
		fhirIdObjects.put(PolicyDTO.class.getName(), Policy.class);
		fhirIdObjects.put(AssignedModuleDTO.class.getName(), ModuleConsentTemplate.class);
		fhirIdObjects.put(AssignedPolicyDTO.class.getName(), ModulePolicy.class);
		fhirIdObjects.put(ConsentDTO.class.getName(), Consent.class);
		fhirIdObjects.put(ConsentLightDTO.class.getName(), Consent.class);
		fhirIdObjects.put(QCDTO.class.getName(), QC.class);
		fhirIdObjects.put(QCHistoryDTO.class.getName(), QCHist.class);
		fhirIdObjects.put(SignerIdDTO.class.getName(), SignerId.class);
		fhirIdObjects.put(SignerIdTypeDTO.class.getName(), SignerIdType.class);
		fhirIdObjects.put(SignedPolicyDTO.class.getName(), SignedPolicy.class);
		fhirIdObjects.put(ConsentScanDTO.class.getName(), ConsentScan.class);
	}

	@PostConstruct
	private void init()
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			while (!(VersionConverterCache.isInitialised() && OrgDatCache.isInitialised() && ConsentCache.isInitialised()))
			{
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("DataAccessDispatcher is ready");
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	/**
	 * Returns a map with the policy keys of all assigned policies of all assigned modules of the consent template for the given consent key
	 * as keys in the map and their consented state as returned by {@link #isConsented(Set, PolicyKeyDTO, CheckConsentConfig)} with the
	 * * signer IDs of the given consent key w.r.t. the given {@link CheckConsentConfig}.
	 *
	 * @param consentKeyDTO
	 *            the consent key to che the policy state for
	 * @param config
	 *            the configuration how to check the consent
	 * @return a map with the policy states for the given consent key
	 *
	 * @throws UnknownSignerIdTypeException
	 *             for an unknown signer ID type
	 * @throws InvalidVersionException
	 *             for an invalid version
	 * @throws UnknownDomainException
	 *             for an unknown domain
	 */
	private Map<PolicyKeyDTO, Boolean> getPolicyStates(ConsentKeyDTO consentKeyDTO, CheckConsentConfig config)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException
	{
		if (config == null)
		{
			config = new CheckConsentConfig();
		}

		Map<PolicyKeyDTO, Boolean> states = new HashMap<>();
		Set<SignerIdDTO> signerIds = consentKeyDTO.getSignerIds();
		PolicyKeyDTO policyKey = null;
		try
		{
			ConsentTemplateDTO consentTemplate = OrgDatCache.getConsentTemplateFIUO(consentKeyDTO.getConsentTemplateKey());

			for (AssignedModuleDTO assignedModule : consentTemplate.getAssignedModules())
			{
				for (AssignedPolicyDTO assignedPolicy : assignedModule.getModule().getAssignedPolicies())
				{
					policyKey = new PolicyKeyDTO(assignedPolicy.getPolicy().getKey());
					states.put(policyKey, isConsented(signerIds, policyKey, config));
				}
			}
			return states;
		}
		catch (UnknownPolicyException shouldNeverHappen)
		{
			LOGGER.fatal("Failed to get the current policy status for policy key " + policyKey + " and signer IDs " + signerIds, shouldNeverHappen);
		}
		catch (UnknownConsentTemplateException e)
		{
			LOGGER.fatal("Failed to get the current policy states for the unknown consent template of " + consentKeyDTO, e);
		}
		return null;
	}

	// TODO
	// ------------------------------------------ managementService ------------------------------------------

	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				StringBuilder sb = new StringBuilder("addDomain ");
				sb.append(domainDTO);
				LOGGER.debug(sb.toString());
			}
			try
			{
				OrgDatCache.getDomainFIUO(domainDTO.getName());
				String message = "domain with name '" + domainDTO.getName() + "' already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			catch (UnknownDomainException expected)
			{}
			if (domainDTO.getLabel() == null || domainDTO.getLabel().isEmpty())
			{
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("no label is set for new domain '" + domainDTO.getName() + "' - set it to the name of the domain");
				}
				domainDTO.setLabel(domainDTO.getName());
			}
			domainDTO = dao.addDomain(domainDTO);
			// VersionConverterCache wird schon im dao gefuellt, muss so sein wegen test der converter ...
			OrgDatCache.addDomain(domainDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deleteDomain(String domainName) throws UnknownDomainException, ObjectInUseException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			OrgDatCache.getDomainFIUO(domainName);
			if (!OrgDatCache.listPoliciesFIUO(domainName, false).isEmpty() || !OrgDatCache.listModulesFIUO(domainName, false).isEmpty()
					|| !OrgDatCache.listConsentTemplatesFIUO(domainName, false).isEmpty())
			{
				String message = "domain with name '" + domainName + "' can't be deleted - there are " + OrgDatCache.listPoliciesFIUO(domainName, false).size() + " policies, "
						+ OrgDatCache.listModulesFIUO(domainName, false).size() + " modules and " + OrgDatCache.listConsentTemplatesFIUO(domainName, false).size()
						+ " consent templates connected to it";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			dao.remove(Domain.class, domainName);
			VersionConverterCache.removeDomain(domainName);
			OrgDatCache.removeDomain(domainName);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void finaliseDomain(String domainName) throws UnknownDomainException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			DomainDTO domainDTO = dao.finaliseDomain(domainName);
			OrgDatCache.updateDomain(domainDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateDomain(DomainDTO domainDTO) throws ObjectInUseException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("updateDomain " + domainDTO.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			DomainDTO result = dao.updateDomain(domainDTO);
			OrgDatCache.updateDomain(result);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateDomainInUse(String domainName, String label, String logo, String externProperties, String comment)
			throws UnknownDomainException
	{
		updateDomainInUse(domainName, label, logo, externProperties, new ExpirationPropertiesDTO(), comment);
	}

	public void updateDomainInUse(String domainName, String label, String logo, String externProperties, ExpirationPropertiesDTO expiration, String comment)
			throws UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("updateDomainInUse with name '");
			sb.append(domainName);
			sb.append("', extern properties '");
			sb.append(externProperties);
			if (expiration != null)
			{
				sb.append("', expiration properties '");
				sb.append(expiration);
			}
			sb.append("', label '");
			sb.append(label);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append(logo != null && !logo.isEmpty() ? "' and a logo" : "' and no logo");
			LOGGER.debug(sb.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			DomainDTO result = dao.updateDomainInUse(domainName, label, logo, externProperties, expiration, comment);
			OrgDatCache.updateDomain(result);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidParameterException, InvalidPropertiesException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				StringBuilder sb = new StringBuilder("addConsentTemplate ");
				sb.append(consentTemplateDTO);
				LOGGER.debug(sb.toString());
			}
			String domainName = consentTemplateDTO.getKey().getDomainName();
			DomainDTO domainDTO = OrgDatCache.getDomainFIUO(domainName);
			if (!domainDTO.getFinalised())
			{
				String message = consentTemplateDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			try
			{
				OrgDatCache.getConsentTemplateFIUO(consentTemplateDTO.getKey());
				String message = consentTemplateDTO.getKey() + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			catch (UnknownConsentTemplateException expected)
			{}
			consentTemplateDTO = dao.addConsentTemplate(consentTemplateDTO, finaliseRelatedEntities);
			OrgDatCache.addConsentTemplate(consentTemplateDTO);
			if (finaliseRelatedEntities && consentTemplateDTO.getFinalised())
			{
				finaliseAssignedModules(consentTemplateDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, ObjectInUseException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.deleteConsentTemplate(keyDTO);
			OrgDatCache.removeConsentTemplate(keyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void finaliseTemplate(ConsentTemplateKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			ConsentTemplateDTO ctDTO = dao.finaliseTemplate(keyDTO, finaliseRelatedEntities);
			OrgDatCache.updateConsentTemplate(ctDTO);
			if (finaliseRelatedEntities)
			{
				finaliseAssignedModules(ctDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateConsentTemplate(ConsentTemplateDTO ctDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException,
			FreeTextConverterStringException, InvalidFreeTextException, InvalidPropertiesException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + ctDTO.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			ctDTO = dao.updateConsentTemplate(ctDTO, finaliseRelatedEntities);
			OrgDatCache.updateConsentTemplate(ctDTO);
			if (finaliseRelatedEntities && ctDTO.getFinalised())
			{
				finaliseAssignedModules(ctDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateConsentTemplateInUse(ConsentTemplateDTO dto) throws InvalidFreeTextException, InvalidVersionException,
			UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString() + " (updateInUse - only non-critical fields are updated)");
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			ConsentTemplateDTO ctDTO = dao.updateConsentTemplateInUse(dto);
			OrgDatCache.updateConsentTemplate(ctDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownPolicyException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			moduleDTO = dao.addModule(moduleDTO, finaliseRelatedEntities);
			OrgDatCache.addModule(moduleDTO);
			if (finaliseRelatedEntities && moduleDTO.getFinalised())
			{
				finaliseAssignedPolicies(moduleDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deleteModule(ModuleKeyDTO keyDTO) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownModuleException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.deleteModule(keyDTO);
			OrgDatCache.removeModule(keyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void finaliseModule(ModuleKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			ModuleDTO moduleDTO = dao.finaliseModule(keyDTO, finaliseRelatedEntities);
			OrgDatCache.updateModule(moduleDTO);
			if (finaliseRelatedEntities)
			{
				finaliseAssignedPolicies(moduleDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidParameterException, InvalidVersionException,
			ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + moduleDTO.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			moduleDTO = dao.updateModule(moduleDTO, finaliseRelatedEntities);
			OrgDatCache.updateModule(moduleDTO);
			if (finaliseRelatedEntities && moduleDTO.getFinalised())
			{
				finaliseAssignedPolicies(moduleDTO);
			}
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateModuleInUse(ModuleKeyDTO keyDTO, String label, String shortText, String externProperties, String comment, Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		if (LOGGER.isDebugEnabled())
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
			LOGGER.debug(sb.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			ModuleDTO moduleDTO = dao.updateModuleInUse(keyDTO, label, shortText, externProperties, comment, assignedPolicyDTOs);
			OrgDatCache.updateModule(moduleDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addPolicy(PolicyDTO policyDTO) throws DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			policyDTO = dao.addPolicy(policyDTO);
			OrgDatCache.addPolicy(policyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deletePolicy(PolicyKeyDTO keyDTO) throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, ObjectInUseException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.deletePolicy(keyDTO);
			OrgDatCache.removePolicy(keyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void finalisePolicy(PolicyKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			PolicyDTO policyDTO = dao.finalisePolicy(keyDTO);
			OrgDatCache.updatePolicy(policyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updatePolicy(PolicyDTO policyDTO) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + policyDTO.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			policyDTO = dao.updatePolicy(policyDTO);
			OrgDatCache.updatePolicy(policyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updatePolicyInUse(PolicyKeyDTO keyDTO, String label, String externProperties, String comment)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("update ");
			sb.append(keyDTO);
			sb.append(" with extern properties '");
			sb.append(externProperties);
			sb.append("', label '");
			sb.append(label);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("'");
			LOGGER.debug(sb.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			PolicyDTO policyDTO = dao.updatePolicyInUse(keyDTO, label, externProperties, comment);
			OrgDatCache.updatePolicy(policyDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	private void finaliseAssignedModules(ConsentTemplateDTO ctDTO)
	{
		for (AssignedModuleDTO assignedModuleDTO : ctDTO.getAssignedModules())
		{
			try
			{
				OrgDatCache.updateModule(assignedModuleDTO.getModule());
			}
			catch (InvalidVersionException | UnknownDomainException | UnknownModuleException e)
			{
				LOGGER.error("impossible exception while finalising consent template", e);
			}
			finaliseAssignedPolicies(assignedModuleDTO.getModule());
		}
	}

	private void finaliseAssignedPolicies(ModuleDTO moduleDTO)
	{
		for (AssignedPolicyDTO assignedPolicyDTO : moduleDTO.getAssignedPolicies())
		{
			try
			{
				OrgDatCache.updatePolicy(assignedPolicyDTO.getPolicy());
			}
			catch (InvalidVersionException | UnknownDomainException | UnknownPolicyException e)
			{
				LOGGER.error("impossible exception while finalising module", e);
			}
		}
	}

	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			SignerIdTypeDTO signerIdTypeDTO = dao.addSignerIdType(domainName, signerIdTypeName);
			OrgDatCache.addSignerIdType(domainName, signerIdTypeDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.deleteSignerIdType(domainName, signerIdTypeName);
			OrgDatCache.removeSignerIdType(domainName, signerIdTypeName);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void updateSignerIdType(String domainName, String signerIdTypeName, String label, String comment) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("update signerIdType with name ");
			sb.append(signerIdTypeName);
			sb.append(" within domain '");
			sb.append(domainName);
			sb.append("', with label '");
			sb.append(label);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("'");
			LOGGER.debug(sb.toString());
		}
		ACCESS_RWL.writeLock().lock();
		try
		{
			SignerIdTypeDTO signerIdTypeDTO = dao.updateSignerIdType(domainName, signerIdTypeName, label, comment);
			OrgDatCache.updateSignerIdType(domainName, signerIdTypeDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ service ------------------------------------------

	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException, InvalidParameterException, RequirementsNotFullfilledException
	{
		addConsent(consentDTO, null);
	}

	public void addConsent(ConsentDTO consentDTO, String notificationClientID) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException, InvalidParameterException, RequirementsNotFullfilledException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getPolicyStates(consentKeyDTO, null) : null;

			dao.addConsent(consentDTO);

			Map<PolicyKeyDTO, Boolean> currentPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getPolicyStates(consentKeyDTO, null) : null;
			notificationSender.sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, currentPolicyStates, "AddConsent", null);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusType(signerIdDTOs, policyKeyDTO, config));
	}

	public ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFromString, int versionFromOffset, String versionToString, int versionToOffset, CheckConsentConfig config)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			int versionFrom = 0;
			int versionTo = 0;
			if (!config.getIgnoreVersionNumber())
			{
				versionFrom = VersionConverterCache.getPolicyVersionConverter(domainName).stringToInt(versionFromString) + versionFromOffset;
				versionTo = VersionConverterCache.getPolicyVersionConverter(domainName).stringToInt(versionToString) + versionToOffset;
			}
			return dao.getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException,
			UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getConsentStatusType(signerIdDTOs, policyKeyDTO, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listConsentTemplates(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.listConsentTemplatesCFEU(domainName, onlyFinal);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName) throws InvalidVersionException, UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			OrgDatCache.getDomainFIUO(domainName);
			return dao.listCurrentConsentTemplates(domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ModuleDTO> listModules(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.listModulesCFEU(domainName, onlyFinal);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ModuleDTO getModule(ModuleKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.getModuleCFEU(keyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<PolicyDTO> listPolicies(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.listPoliciesCFEU(domainName, onlyFinal);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.getPolicyCFEU(keyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.getConsentTemplateCFEU(keyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignedPolicyDTO> getPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getPolicyStatesForSignerIds(domainName, signerIdDTOs, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getPolicyStatesForPolicyAndSignerIds(policyKeyDTO, signerIdDTOs, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getPolicyStatesForPolicyNameAndSignerIds(domainName, policyName, signerIdDTOs, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdException, InvalidVersionException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getCurrentPolicyStatesForSignerIds(domainName, signerIdDTOs, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			OrgDatCache.getDomainFIUO(domainName);
			return dao.getCurrentConsentTemplate(consentTemplateName, domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownDomainException,
			UnknownSignerIdTypeException, InvalidVersionException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getAllConsentsForSignerIds(domainName, signerIdDTOs, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentDTO getCurrentConsentForSignerIdsAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getCurrentConsentForSignerIdsAndCT(ctKeyDTO, signerIdDTOs, ignoreVersionNumber, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidVersionException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			OrgDatCache.getConsentTemplateFIUO(ctKeyDTO);
			return dao.getAllConsentsForConsentTemplate(ctKeyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName) throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			OrgDatCache.getDomainFIUO(domainName);
			return dao.getAllConsentsForDomain(domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, PaginationConfig config)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getConsentsForDomainPaginated(domainName, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public long countConsentsForDomainWithFilter(String domainName, PaginationConfig config) throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.countConsentsForDomainWithFilter(domainName, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			OrgDatCache.getDomainFIUO(domainName);
			return dao.getAllConsentsForDomainWithoutScan(domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getConsent(keyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public ConsentLightDTO getConsentLight(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getConsentLight(keyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public DomainDTO getDomain(String domainName) throws UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.getDomainCFEU(domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<DomainDTO> listDomains()
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.listDomainsCFEU(false);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void updateConsentInUse(ConsentKeyDTO keyDTO, String externProperties, String comment, ConsentScanDTO scan)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException,
			UnknownConsentException, InvalidParameterException
	{
		updateConsentInUse(keyDTO, externProperties, comment, scan, null);
	}

	public void updateConsentInUse(ConsentKeyDTO consentKeyDTO, String externProperties, String comment, ConsentScanDTO scan, String notificationClientID)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException,
			UnknownConsentException, InvalidParameterException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getPolicyStates(consentKeyDTO, null) : null;
			dao.updateConsentInUse(consentKeyDTO, externProperties, comment, scan, notificationClientID);
			notificationSender.sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, getPolicyStates(consentKeyDTO, null), "UpdateConsentInUse", null);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType, String fileName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, DuplicateEntryException, InvalidParameterException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.addScanToConsent(consentKeyDTO, scanBase64, fileType, fileName);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void removeScanFromConsent(ConsentKeyDTO consentKeyDTO, String fhirId)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException, InvalidVersionException,
			UnknownSignerIdTypeException, InvalidParameterException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.removeScanFromConsent(consentKeyDTO, fhirId);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public SignerIdTypeDTO getSignerIdTypeDTO(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.getSignerIdTypeCFEU(domainName, signerIdTypeName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignerIdTypeDTO> listSignerIdTypes(String domainName) throws UnknownDomainException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return OrgDatCache.listSignerIdTypesCFEU(domainName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getAllIdsForSignerIdType(domainName, signerIdTypeName);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, InvalidVersionException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getAllConsentedIdsFor(signerIdTypeName, policyKeyDTO, config);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException, InvalidParameterException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			dao.validateConsentReturningObjects(consentDTO, allowRevoke);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, InvalidParameterException,
			UnknownSignerIdTypeException, InternalException
	{
		refuseConsent(ctKeyDTO, signerIdDTOs, null);
	}

	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, String notificationClientID)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, InvalidParameterException,
			UnknownSignerIdTypeException, InternalException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			// TODO woher das consentKeyDTO nehmen?
			// Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getPolicyStates(consentKeyDTO, null) : null;
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = null;

			ConsentDTO consentDTO = dao.refuseConsent(ctKeyDTO, signerIdDTOs, notificationClientID);
			notificationSender.sendNotification(notificationClientID, consentDTO.getKey(), previousPolicyStates, getPolicyStates(consentDTO.getKey(), null), "RefuseConsent", null);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException, InternalException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.addSignerIdToConsent(consentKeyDTO, signerIdDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public ConsentDateValuesDTO getConsentDates(ConsentKeyDTO consentKeyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getConsentDates(consentKeyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdTypeException, InternalException, InvalidParameterException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.addSignerIdToSignerId(domain, existentSignerIdDTO, newSignerIdDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void addAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId) throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.addAlias(domainName, originalSignerId, aliasSignerId);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public void deactivateAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws UnknownAliasException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.deactivateAlias(domainName, originalSignerId, aliasSignerId);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public List<SignerIdDTO> listAliases(String domainName, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.listAliases(domainName, signerIdDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public Map<SignerIdDTO, SignerIdDTO[]> mapAliases(String domainName, List<SignerIdDTO> originalSignerIds)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.mapAliases(domainName, originalSignerIds);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<SignerIdDTO> listSignerIdsForAlias(String domainName, SignerIdDTO aliasSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.listSignerIdsForAlias(domainName, aliasSignerIdDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void setQcForConsent(ConsentKeyDTO consentKeyDTO, QCDTO qcDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		setQcForConsent(consentKeyDTO, qcDTO, null);
	}

	public void setQcForConsent(ConsentKeyDTO consentKeyDTO, QCDTO qcDTO, String notificationClientID)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getPolicyStates(consentKeyDTO, null) : null;
			dao.setQcForConsent(consentKeyDTO, qcDTO, notificationClientID);
			// TODO woher statusVergleich nehmen? boolean return vom dao?
			// if (oldQcPassed != consent.getQc().isQcPassed())
			// {
			notificationSender.sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, getPolicyStates(consentKeyDTO, null), "SetQcForConsent", null,
					Collections.singletonMap("qc", qcDTO));
			// }
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}

	public List<QCHistoryDTO> getQCHistoryForConsent(ConsentKeyDTO consentKeyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getQCHistoryForConsent(consentKeyDTO);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public Set<ConsentTemplateKeyDTO> getTemplatesWithPolicies(Set<PolicyKeyDTO> policyKeyDTOs)
			throws InvalidParameterException, UnknownDomainException, InvalidVersionException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getTemplatesWithPolicies(policyKeyDTOs);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	// --------------------------------------- mapped templates ----------------------------------------

	public Set<ConsentTemplateKeyDTO> getMappedTemplatesForSignerId(String domainName, ConsentTemplateType ctType, SignerIdDTO signerId, boolean useAliases)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getMappedTemplatesForSignerId(domainName, ctType, signerId, useAliases);
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	// ------------------------------------------ fhirService ------------------------------------------

	@SuppressWarnings("unchecked")
	public <T extends FhirIdDTO> T getObjectByFhirID(Class<T> clazz, String fhirID) throws UnknownFhirIdObjectException, UnknownIDException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			Class<? extends FhirDTOExporter<T>> dbClazz = (Class<? extends FhirDTOExporter<T>>) fhirIdObjects.get(clazz.getName());
			if (dbClazz != null)
			{
				FhirDTOExporter<T> dbObject = dao.getDTOByFhirID(dbClazz, fhirID);
				T result = null;
				try
				{
					if (ConsentLightDTO.class.equals(clazz))
					{
						result = (T) ((Consent) dbObject).toLightDTO();
					}
					else
					{
						result = dbObject.toDTO();
					}
				}
				catch (InconsistentStatusException | InvalidVersionException | UnknownDomainException e)
				{
					// impossible
					LOGGER.fatal("impossible exception, db manipulated?", e);
				}
				return result;
			}
			else
			{
				throw new UnknownFhirIdObjectException("unknown object type: " + clazz.getName());
			}
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	// ------------------------------------------ statistics ------------------------------------------

	public StatisticDTO getLatestStats()
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getLatestStats();
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public List<StatisticDTO> getAllStats()
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.getAllStats();
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public StatisticDTO updateStats() throws StatisticException
	{
		ACCESS_RWL.readLock().lock();
		try
		{
			return dao.updateStats();
		}
		finally
		{
			ACCESS_RWL.readLock().unlock();
		}
	}

	public void addStat(StatisticDTO statisticDTO)
	{
		ACCESS_RWL.writeLock().lock();
		try
		{
			dao.addStat(statisticDTO);
		}
		finally
		{
			ACCESS_RWL.writeLock().unlock();
		}
	}
}
