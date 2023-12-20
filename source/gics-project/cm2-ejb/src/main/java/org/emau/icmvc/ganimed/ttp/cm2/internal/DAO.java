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

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownIDException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache.CachedSignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Alias;
import org.emau.icmvc.ganimed.ttp.cm2.model.AliasKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Alias_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentScan;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentScan_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.FhirDTOExporter;
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextDef;
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextDefKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.MappedConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy_;
import org.emau.icmvc.ganimed.ttp.cm2.model.QC;
import org.emau.icmvc.ganimed.ttp.cm2.model.QCHist;
import org.emau.icmvc.ganimed.ttp.cm2.model.QCHistKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.QCHist_;
import org.emau.icmvc.ganimed.ttp.cm2.model.QC_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Statistic;
import org.emau.icmvc.ganimed.ttp.cm2.model.Statistic_;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPersonSignerId;
import org.emau.icmvc.ganimed.ttp.cm2.util.StatisticKeys;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * single point for db and cache access
 *
 * @author geidell
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class DAO
{
	private static final String PERCENT = "%";
	private static final Logger LOGGER = LogManager.getLogger(DAO.class);
	private static final Date MINDATE_FOR_SIGNATURE = new Date(82860000); // 02.01.1970
	@PersistenceContext(unitName = "gics")
	private EntityManager em;
	private static final ReentrantReadWriteLock EM_RWL = new ReentrantReadWriteLock();

	@PostConstruct
	private void initialiseCaches()
	{
		EM_RWL.writeLock().lock();
		try
		{
			List<Domain> domains = listDomains();

			if (!VersionConverterCache.isInitialised())
			{
				LOGGER.info("initialise version converter cache");
				try
				{
					VersionConverterCache.init(domains);
					LOGGER.info("version converter cache initialised");
				}
				catch (VersionConverterClassException e)
				{
					String message = "exception while initialising version convert class cache " + e.getMessage();
					LOGGER.fatal(message, e);
				}
			}

			if (!OrgDatCache.isInitialised())
			{
				List<ConsentTemplate> cts = listConsentTemplates();
				List<Module> modules = listModules();
				List<Policy> policies = listPolicies();
				List<SignerIdType> sIdTypes = listSignerIdTypes();
				LOGGER.info("initialise orgDat cache");
				OrgDatCache.init(domains, cts, modules, policies, sIdTypes);
				LOGGER.info("orgDat cache initialised");
			}
			em.clear();

			if (!ConsentCache.isInitialised())
			{
				LOGGER.info("initialise consent cache");
				ConsentCache.init(em);
				LOGGER.info("consent cache initialised");
			}
		}
		finally
		{
			try
			{
				em.clear();
			}
			finally
			{
				EM_RWL.writeLock().unlock();
			}
		}
	}

	// ------------------------------------------ private ------------------------------------------

	private List<Domain> listDomains()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Domain> criteriaQuery = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> root = criteriaQuery.from(Domain.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<ConsentTemplate> listConsentTemplates()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
		Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<Module> listModules()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Module> criteriaQuery = criteriaBuilder.createQuery(Module.class);
		Root<Module> root = criteriaQuery.from(Module.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<Policy> listPolicies()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Policy> criteriaQuery = criteriaBuilder.createQuery(Policy.class);
		Root<Policy> root = criteriaQuery.from(Policy.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<SignerIdType> listSignerIdTypes()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SignerIdType> criteriaQuery = criteriaBuilder.createQuery(SignerIdType.class);
		Root<SignerIdType> root = criteriaQuery.from(SignerIdType.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private Domain getDomain(String domainName) throws UnknownDomainException
	{
		Domain domain = em.find(Domain.class, domainName);
		if (domain == null)
		{
			String message = "unknown domain: " + domainName;
			LOGGER.warn(message);
			throw new UnknownDomainException(message);
		}
		return domain;
	}

	private ConsentTemplate getConsentTemplate(ConsentTemplateKey key) throws UnknownConsentTemplateException
	{
		ConsentTemplate result = em.find(ConsentTemplate.class, key);
		if (result == null)
		{
			String message = "unknown consent template: " + key;
			LOGGER.warn(message);
			throw new UnknownConsentTemplateException(message);
		}
		return result;
	}

	private Module getModule(ModuleKey key) throws UnknownModuleException
	{
		Module result = em.find(Module.class, key);
		if (result == null)
		{
			String message = "unknown module: " + key;
			LOGGER.warn(message);
			throw new UnknownModuleException(message);
		}
		return result;
	}

	private Policy getPolicy(PolicyKey key) throws UnknownPolicyException
	{
		Policy result = em.find(Policy.class, key);
		if (result == null)
		{
			String message = "unknown policy: " + key;
			LOGGER.warn(message);
			throw new UnknownPolicyException(message);
		}
		return result;
	}

	private SignerIdType getSignerIdType(Domain domain, String idType) throws UnknownSignerIdTypeException
	{
		for (SignerIdType signerIdType : domain.getSignerIdTypes())
		{
			if (signerIdType.getKey().getName().equals(idType))
			{
				return signerIdType;
			}
		}
		String message = "unknown signer id type: " + idType + " for domain " + domain.getName();
		LOGGER.warn(message);
		throw new UnknownSignerIdTypeException(message);
	}

	private SignedPolicy getSignedPolicy(SignedPolicyKey key)
	{
		return em.find(SignedPolicy.class, key);
	}

	private VirtualPerson getVirtualPerson(long key)
	{
		return em.find(VirtualPerson.class, key);
	}

	private Consent getConsentObject(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException
	{
		Consent result;
		Domain domain = getDomain(keyDTO.getConsentTemplateKey().getDomainName());
		ConsentTemplate ct = getConsentTemplate(
				new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), keyDTO.getConsentTemplateKey()));
		try
		{
			VirtualPerson vp = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, keyDTO.getSignerIds()));
			ConsentKey consentKey = new ConsentKey(ct.getKey(), keyDTO.getConsentDate(), vp.getId());
			result = em.find(Consent.class, consentKey);
		}
		catch (ObjectNotFoundException e)
		{
			String message = "virtual person for " + keyDTO + " not found";
			LOGGER.warn(message);
			throw new UnknownConsentException(message);
		}
		if (result == null)
		{
			String message = keyDTO + " not found";
			LOGGER.warn(message);
			throw new UnknownConsentException(message);
		}
		return result;
	}

	private Map<ModuleKeyDTO, Module> mapModules(ConsentTemplateDTO consentTemplateDTO, Domain domain)
			throws DuplicateEntryException, InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		Map<ModuleKeyDTO, Module> modules = new HashMap<>();
		List<String> policyNames = new ArrayList<>();
		List<String> moduleNames = new ArrayList<>();
		for (AssignedModuleDTO assignedModuleDTO : consentTemplateDTO.getAssignedModules())
		{
			ModuleKeyDTO moduleKeyDTO = assignedModuleDTO.getModule().getKey();
			String moduleName = moduleKeyDTO.getName();
			if (moduleNames.contains(moduleName))
			{
				String message = "multiple occurences of module '" + moduleName + "' within " + consentTemplateDTO;
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			moduleNames.add(moduleName);
			ModuleKey moduleKey = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domain.getName()), moduleKeyDTO);
			Module module = getModule(moduleKey);
			modules.put(moduleKeyDTO, module);
			for (ModulePolicy mp : module.getModulePolicies())
			{
				String policyName = mp.getPolicy().getKey().getName();
				if (policyNames.contains(policyName))
				{
					String message = "multiple occurences of policy '" + policyName + "' within " + consentTemplateDTO;
					LOGGER.warn(message);
					throw new DuplicateEntryException(message);
				}
				policyNames.add(policyName);
			}
		}
		return modules;
	}

	private Map<ConsentTemplateKeyDTO, ConsentTemplate> mapTemplates(ConsentTemplateDTO consentTemplateDTO, Domain domain)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException
	{
		Map<ConsentTemplateKeyDTO, ConsentTemplate> templates = new HashMap<>();

		for (ConsentTemplateKeyDTO ctKeyDTO : consentTemplateDTO.getAllMappedTemplates())
		{
			ConsentTemplateKey ctKey = new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), ctKeyDTO);
			templates.put(ctKeyDTO, getConsentTemplate(ctKey));
		}
		return templates;
	}

	private Set<SignerIdKey> convertToSignerIdKeys(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException
	{
		Set<SignerIdKey> result = new HashSet<>();
		for (SignerIdDTO signerIdDTO : signerIdDTOs)
		{
			SignerIdType sit = getSignerIdType(domain, signerIdDTO.getIdType());
			result.add(new SignerIdKey(sit.getKey(), signerIdDTO.getId()));
		}
		return result;
	}

	private VirtualPerson getOrCreateVirtualPersonFor(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException
	{
		VirtualPerson result;
		try
		{
			// suche nach der virtuellen person, die genau die gegebenen id umfasst
			result = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, signerIdDTOs));
		}
		catch (ObjectNotFoundException e)
		{
			result = createVirtualPerson(domain, signerIdDTOs);
		}
		return result;
	}

	private VirtualPerson findVirtualPersonWithExactTheSignerIds(Domain domain, Set<SignerIdKey> signerIdKeys) throws ObjectNotFoundException
	{
		Set<VirtualPerson> persons = getAllVirtualPersonsWithAtLeastOneSignerId(domain, signerIdKeys, false);
		for (VirtualPerson person : persons)
		{
			Set<VirtualPersonSignerId> virtualPersonSignerIds = new HashSet<>(person.getVirtualPersonSignerIds());
			if (virtualPersonSignerIds.size() != signerIdKeys.size())
			{
				continue;
			}
			boolean found = true;
			for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds)
			{
				if (!signerIdKeys.contains(virtualPersonSignerId.getSignerId().getKey()))
				{
					found = false;
					break;
				}
			}
			if (found)
			{
				return person;
			}
		}
		throw new ObjectNotFoundException();
	}

	/**
	 * liefert alle virtualPersons, welche mindestens einer der gegebenen signerId zugeordnet sind.
	 * aliaseintraege werden beachtet, wenn useAliases = true
	 *
	 * @param domain
	 * @param signerIdKeys
	 * @param useAliases
	 * @return
	 */
	private Set<VirtualPerson> getAllVirtualPersonsWithAtLeastOneSignerId(Domain domain, Set<SignerIdKey> signerIdKeys, boolean useAliases)
	{
		Set<VirtualPerson> result = new HashSet<>();
		Set<SignerIdKey> processedKeys = new HashSet<>();
		for (SignerIdKey signerIdKey : signerIdKeys)
		{
			result.addAll(getVPsForSignerId(signerIdKey, processedKeys, useAliases));
		}
		return result;
	}

	private Set<VirtualPerson> getVPsForSignerId(SignerIdKey sik, Set<SignerIdKey> processedKeys, boolean useAliases)
	{
		Set<VirtualPerson> result = new HashSet<>();
		if (processedKeys.contains(sik))
		{
			return result;
		}
		SignerId signerId = em.find(SignerId.class, sik);
		if (signerId != null)
		{
			for (VirtualPersonSignerId vpsi : signerId.getVirtualPersonSignerIds())
			{
				result.add(vpsi.getVirtualPerson());
			}
			processedKeys.add(sik);
			if (useAliases)
			{
				List<Alias> aliases = getAliases(signerId);
				for (Alias alias : aliases)
				{
					// zirkel vermeiden
					if (!processedKeys.contains(alias.getKey().getAliasSignerIdKey()))
					{
						result.addAll(getVPsForSignerId(alias.getKey().getAliasSignerIdKey(), processedKeys, useAliases));
					}
				}
			}
		}
		return result;
	}

	private Alias getAlias(SignerIdKey origSignerIdKey, SignerIdKey aliasSignerIdKey) throws UnknownAliasException
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Alias> criteriaQuery = criteriaBuilder.createQuery(Alias.class);
		Root<Alias> root = criteriaQuery.from(Alias.class);
		// siehe alias
		// Predicate predicate =
		// criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSignerIdKey),
		// origSignerId.getKey());
		// predicate = criteriaBuilder.and(predicate,
		// criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSignerIdKey),
		// aliasSignerId.getKey()));
		Predicate predicate = criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSITDomainName), origSignerIdKey.getSignerIdTypeKey().getDomainName());
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSITName), origSignerIdKey.getSignerIdTypeKey().getName()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSIValue), origSignerIdKey.getValue()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSITDomainName), aliasSignerIdKey.getSignerIdTypeKey().getDomainName()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSITName), aliasSignerIdKey.getSignerIdTypeKey().getName()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSIValue), aliasSignerIdKey.getValue()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNull(root.get(Alias_.deactivateTimestamp)));
		criteriaQuery.select(root).where(predicate);
		Alias result;
		try
		{
			result = em.createQuery(criteriaQuery).getSingleResult();
		}
		catch (NoResultException maybe)
		{
			throw new UnknownAliasException("no not-deactivated alias found for original " + origSignerIdKey + " and alias " + aliasSignerIdKey);
		}
		return result;
	}

	private List<Alias> getAliases(SignerId origSignerId)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Alias> criteriaQuery = criteriaBuilder.createQuery(Alias.class);
		Root<Alias> root = criteriaQuery.from(Alias.class);
		// siehe alias
		// Predicate predicate =
		// criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSignerIdKey),
		// origSignerId.getKey());
		Predicate predicate = criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSITDomainName), origSignerId.getKey().getSignerIdTypeKey().getDomainName());
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSITName), origSignerId.getKey().getSignerIdTypeKey().getName()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSIValue), origSignerId.getKey().getValue()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNull(root.get(Alias_.deactivateTimestamp)));
		criteriaQuery.select(root).where(predicate);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<Alias> getAliasesForAlias(SignerId aliasSignerId)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Alias> criteriaQuery = criteriaBuilder.createQuery(Alias.class);
		Root<Alias> root = criteriaQuery.from(Alias.class);
		// siehe alias
		// Predicate predicate =
		// criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.origSignerIdKey),
		// origSignerId.getKey());
		Predicate predicate = criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSITDomainName), aliasSignerId.getKey().getSignerIdTypeKey().getDomainName());
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSITName), aliasSignerId.getKey().getSignerIdTypeKey().getName()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Alias_.key).get(AliasKey_.aliasSIValue), aliasSignerId.getKey().getValue()));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNull(root.get(Alias_.deactivateTimestamp)));
		criteriaQuery.select(root).where(predicate);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private VirtualPerson createVirtualPerson(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("create virtual person for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			LOGGER.debug(sb.toString());
		}
		List<SignerId> signerIds = new ArrayList<>();
		VirtualPerson result;
		for (SignerIdDTO signerIdDTO : signerIdDTOs)
		{
			SignerIdType signerIdType = getSignerIdType(domain, signerIdDTO.getIdType());
			SignerId signerId = em.find(SignerId.class, new SignerIdKey(signerIdType.getKey(), signerIdDTO.getId()));
			if (signerId == null)
			{
				signerId = new SignerId(signerIdType, signerIdDTO.getId());
				em.persist(signerId);
			}
			signerIds.add(signerId);
		}
		result = new VirtualPerson();
		em.persist(result);
		// relationen nach persistierung
		for (SignerId signerId : signerIds)
		{
			VirtualPersonSignerId virtualPersonSignerId = new VirtualPersonSignerId(result, signerId);
			signerId.getVirtualPersonSignerIds().add(virtualPersonSignerId);
			result.getVirtualPersonSignerIds().add(virtualPersonSignerId);
		}
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(result + " created within " + domain);
		}
		return result;
	}

	private Map<SignedPolicy, Long> getSignedPoliciesWithExpirationDates(List<SignedPolicy> signedPolicies)
	{
		Map<SignedPolicy, Long> result = new HashMap<>();
		if (signedPolicies.isEmpty())
		{
			return result;
		}
		try
		{
			Consent consent = signedPolicies.get(0).getConsent();
			ConsentDateValues consentDates = consent.getConsentDateValues();
			for (SignedPolicy sp : signedPolicies)
			{
				result.put(sp, consentDates.getTimestampForPolicy(sp.getKey().getPolicyKey()));
			}
		}
		catch (UnknownPolicyException impossible)
		{
			LOGGER.fatal("impossible exception while calculating signed policy expiration dates", impossible);
		}
		return result;
	}

	private void checkPolicyName(String policyName) throws UnknownPolicyException
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Policy> criteriaQuery = criteriaBuilder.createQuery(Policy.class);
		Root<Policy> root = criteriaQuery.from(Policy.class);
		Predicate predicate = criteriaBuilder.equal(root.get(Policy_.key).get(PolicyKey_.name), policyName);
		criteriaQuery.select(root).where(predicate);
		List<Policy> result = em.createQuery(criteriaQuery).getResultList();
		if (result.isEmpty())
		{
			String message = "unknown policy name: " + policyName;
			LOGGER.warn(message);
			throw new UnknownPolicyException(message);
		}
	}

	private Set<SignerIdKey> getSIKsForSignerIds(Domain domain, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownSignerIdTypeException
	{
		Set<SignerIdKey> result = convertToSignerIdKeys(domain, signerIdDTOs);
		if (useAliases)
		{
			Set<SignerIdKey> additionalSIK = new HashSet<>();
			for (SignerIdKey sik : result)
			{
				additionalSIK.addAll(getSIKsForSignerIdWithAliases(sik, result));
			}
			result.addAll(additionalSIK);
		}
		return result;
	}

	private Set<SignerIdKey> getSIKsForSignerIdWithAliases(SignerIdKey sik, Set<SignerIdKey> processedKeys)
	{
		Set<SignerIdKey> result = new HashSet<>();
		if (processedKeys.contains(sik))
		{
			return result;
		}
		SignerId signerId = em.find(SignerId.class, sik);
		if (signerId != null)
		{
			result.add(sik);
			processedKeys.add(sik);
			List<Alias> aliases = getAliases(signerId);
			for (Alias alias : aliases)
			{
				// zirkel vermeiden
				if (!processedKeys.contains(alias.getKey().getAliasSignerIdKey()))
				{
					result.addAll(getSIKsForSignerIdWithAliases(alias.getKey().getAliasSignerIdKey(), processedKeys));
				}
			}
		}
		return result;
	}

	private ConsentStatusType checkPolicies(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain)
	{
		long requestDate = config.getRequestDate() != null ? config.getRequestDate().getTime() : System.currentTimeMillis();
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies)
		{
			// Ignore if expired before request date
			if (signedPolicy.getConsentExpirationDate() < requestDate)
			{
				continue;
			}
			// Ignore if consented after request date
			if (config.isUseHistoricalData() && signedPolicy.getGicsConsentDate() > requestDate || !config.isUseHistoricalData() && signedPolicy.getLegalConsentDate() > requestDate)
			{
				continue;
			}
			ConsentStatusType status = signedPolicy.getConsentStatus().getConsentStatusType();
			// Break if policy is declined and permanent revoked is configured
			if (domain.getPropertiesObject().isPermanentRevoke() && (status.equals(ConsentStatusType.DECLINED)
					|| config.getUnknownStateIsConsideredAsDecline() && status.equals(ConsentStatusType.UNKNOWN)))
			{
				currentSignedPolicy = signedPolicy;
				break;
			}
			// Ignore if unknown
			if (status.equals(ConsentStatusType.UNKNOWN))
			{
				continue;
			}
			// Update currentSignedPolicy
			if (currentSignedPolicy == null
					|| domain.getPropertiesObject().isTakeHighestVersion() && versionIsHigher(signedPolicy, currentSignedPolicy)
					|| !domain.getPropertiesObject().isTakeHighestVersion() && consentDateIsNewer(signedPolicy, currentSignedPolicy))
			{
				currentSignedPolicy = signedPolicy;
			}
		}
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		if (currentSignedPolicy != null)
		{
			result = currentSignedPolicy.getConsentStatus().getConsentStatusType();
		}
		return result;
	}

	/**
	 * Checks if the version of signedPolicy is higher than the version of currentSignedPolicy
	 * Or version is equal and {@link #consentDateIsNewer(CachedSignedPolicy, CachedSignedPolicy)}
	 *
	 * @param signedPolicy
	 *            Possible new signed policy
	 * @param currentSignedPolicy
	 *            current signed policy
	 * @return true if signedPolicy should replace the currentSignedPolicy
	 */
	private boolean versionIsHigher(CachedSignedPolicy signedPolicy, CachedSignedPolicy currentSignedPolicy)
	{
		return signedPolicy.getVersion() > currentSignedPolicy.getVersion()
				|| signedPolicy.getVersion() == currentSignedPolicy.getVersion() && consentDateIsNewer(signedPolicy, currentSignedPolicy);
	}

	/**
	 * Checks if the legalConsentDate of signedPolicy is newer than the legalConsentDate of currentSignedPolicy
	 * Or legalConsentDate is equal and signedPolicy consentStatus is DELCINED
	 *
	 * @param signedPolicy
	 *            Possible new signed policy
	 * @param currentSignedPolicy
	 *            current signed policy
	 * @return true if signedPolicy should replace the currentSignedPolicy
	 */
	private boolean consentDateIsNewer(CachedSignedPolicy signedPolicy, CachedSignedPolicy currentSignedPolicy)
	{
		return signedPolicy.getLegalConsentDate() > currentSignedPolicy.getLegalConsentDate()
				|| signedPolicy.getLegalConsentDate() == currentSignedPolicy.getLegalConsentDate() && signedPolicy.getConsentStatus().getConsentStatusType().equals(ConsentStatusType.DECLINED);
	}

	private List<VirtualPerson> findAllVirtualPersonsWithAtLeastTheSignerIds(Domain domain, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws ObjectNotFoundException, UnknownSignerIdTypeException
	{
		Set<VirtualPerson> persons = getAllVirtualPersonsWithAtLeastOneSignerId(domain, convertToSignerIdKeys(domain, signerIdDTOs), useAliases);
		List<VirtualPerson> result = new ArrayList<>();

		for (VirtualPerson person : persons)
		{
			Set<VirtualPersonSignerId> virtualPersonSignerIds = new HashSet<>(person.getVirtualPersonSignerIds());
			Set<SignerIdDTO> foundSignerIdDTOs = new HashSet<>();
			for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds)
			{
				foundSignerIdDTOs.add(virtualPersonSignerId.getSignerId().toDTO());
			}
			boolean found = false;
			for (SignerIdDTO signerIdDTO : signerIdDTOs)
			{
				for (SignerIdDTO fountSignerIdDTO : foundSignerIdDTOs)
				{
					if (signerIdDTO.equalsId(fountSignerIdDTO))
					{
						found = true;
						break;
					}
				}
				if (found)
				{
					break;
				}
			}
			if (found)
			{
				result.add(person);
			}
		}
		if (result.isEmpty())
		{
			throw new ObjectNotFoundException();
		}
		return result;
	}

	private List<Consent> getConsentsForSignerIds(Domain domain, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownSignerIdTypeException
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		Set<VirtualPerson> signers = getAllVirtualPersonsWithAtLeastOneSignerId(domain, convertToSignerIdKeys(domain, signerIdDTOs), useAliases);
		List<Consent> result = new ArrayList<>();
		for (VirtualPerson signer : signers)
		{
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), signer.getId()),
					criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName()));
			criteriaQuery.select(root).where(predicate);
			result.addAll(em.createQuery(criteriaQuery).getResultList());
		}
		return result;
	}

	private CachedSignedPolicy getCurrentPolicy(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain) throws ObjectNotFoundException
	{
		long now = System.currentTimeMillis();
		long requestDate = config.getRequestDate() != null ? config.getRequestDate().getTime() : now;
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies)
		{
			// Ignore if expired before request date
			if (signedPolicy.getConsentExpirationDate() < requestDate)
			{
				continue;
			}
			// Ignore if consented after request date
			if (config.isUseHistoricalData() && signedPolicy.getGicsConsentDate() > requestDate || !config.isUseHistoricalData() && signedPolicy.getLegalConsentDate() > requestDate)
			{
				continue;
			}
			ConsentStatusType status = signedPolicy.getConsentStatus().getConsentStatusType();
			// Break if policy is declined and permanent revoked is configured
			if (domain.getPropertiesObject().isPermanentRevoke() && (status.equals(ConsentStatusType.DECLINED)
					|| config.getUnknownStateIsConsideredAsDecline() && status.equals(ConsentStatusType.UNKNOWN)))
			{
				currentSignedPolicy = signedPolicy;
				break;
			}
			// Ignore if unknown
			if (status.equals(ConsentStatusType.UNKNOWN))
			{
				continue;
			}
			// Update currentSignedPolicy
			if (currentSignedPolicy == null
					|| domain.getPropertiesObject().isTakeHighestVersion() && versionIsHigher(signedPolicy, currentSignedPolicy)
					|| !domain.getPropertiesObject().isTakeHighestVersion() && consentDateIsNewer(signedPolicy, currentSignedPolicy))
			{
				currentSignedPolicy = signedPolicy;
			}
		}
		if (currentSignedPolicy != null)
		{
			return currentSignedPolicy;
		}
		throw new ObjectNotFoundException();
	}

	private Predicate generateWhereForConsent(CriteriaBuilder cb, Root<Consent> root, Domain domain, PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, ObjectNotFoundException, UnknownDomainException
	{
		Predicate predicate = cb.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName());
		Predicate orPredicate = null;
		if (!config.getFilter().isEmpty())
		{
			boolean asConjunction = config.isFilterFieldsAreTreatedAsConjunction();
			boolean caseSensitive = config.isFilterIsCaseSensitive();

			for (Entry<ConsentField, String> entry : config.getFilter().entrySet())
			{
				String value = entry.getValue();

				switch (entry.getKey())
				{
					case CT_NAME:
					{
						Predicate p = generateLikePredicate(cb, root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name),
								value, asConjunction ? predicate : orPredicate, caseSensitive, asConjunction);
						if (asConjunction)
						{
							predicate = p;
						}
						else
						{
							orPredicate = p;
						}
					}
						break;
					case CT_LABEL:
					{
						Predicate p = generateLikePredicate(cb, root.get(Consent_.consentTemplate).get(ConsentTemplate_.label),
								value, asConjunction ? predicate : orPredicate, caseSensitive, asConjunction);
						if (asConjunction)
						{
							predicate = p;
						}
						else
						{
							orPredicate = p;
						}
					}
						break;
					case CT_VERSION:
						VersionConverter vc = VersionConverterCache.getCTVersionConverter(domain.getName());
						int versionToFilter = vc.stringToInt(value);
						if (asConjunction)
						{
							predicate = cb.and(predicate, cb.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter));
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = cb.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter);
							}
							else
							{
								orPredicate = cb.or(orPredicate, cb
										.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter));
							}
						}
						break;
					case SIGNER_ID:
						Predicate signerIdPredicate = getWhereForSignerId(cb, root, domain, value, config.isUseAliases());
						if (asConjunction)
						{
							predicate = cb.and(predicate, signerIdPredicate);
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = signerIdPredicate;
							}
							else
							{
								orPredicate = cb.or(orPredicate, signerIdPredicate);
							}
						}
						break;
					case QC_TYPE:
						String[] qcTypes = value.split(",");
						for (String qcType : qcTypes)
						{
							if (!domain.getPropertiesObject().getValidQcTypes().contains(qcType) && !domain.getPropertiesObject().getInvalidQcTypes().contains(qcType))
							{
								String message = "unknown qc status '" + qcType + "' for filter-clause within generateWhereForConsent()";
								LOGGER.fatal(message);
								throw new InvalidParameterException(message);
							}
						}
						Expression<String> inExpression = root.get(Consent_.qc).get(QC_.type);
						Predicate qcPredicate = inExpression.in(Arrays.asList(qcTypes));
						if (asConjunction)
						{
							predicate = cb.and(predicate, qcPredicate);
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = qcPredicate;
							}
							else
							{
								orPredicate = cb.or(orPredicate, qcPredicate);
							}
						}
						break;
					case DATE:
						LOGGER.warn("to filter the date of consents use startDate and endDate; ignored value: " + value);
						break;
					case NONE:
						break;
					default:
						String message = "unimplemented ConsentField '" + entry.getKey().name() + "' for filter-clause within generateWhereForConsent()";
						LOGGER.fatal(message);
						throw new InvalidParameterException(message);
				}
			}
		}
		Predicate datePredicate = getWhereForDate(cb, root, config);
		if (datePredicate != null)
		{
			if (config.isFilterFieldsAreTreatedAsConjunction())
			{
				predicate = cb.and(predicate, datePredicate);
			}
			else
			{
				if (orPredicate == null)
				{
					orPredicate = datePredicate;
				}
				else
				{
					orPredicate = cb.or(orPredicate, datePredicate);
				}
			}
		}
		Predicate typePredicate = getWhereForType(cb, root, domain.getName(), config);
		if (typePredicate != null)
		{
			predicate = cb.and(predicate, typePredicate);
		}
		if (orPredicate != null)
		{
			predicate = cb.and(predicate, orPredicate);
		}
		return predicate;
	}

	private Predicate generateLikePredicate(CriteriaBuilder cb, Path<String> path, String value, Predicate predicate, boolean caseSensitive, boolean asConjunction)
	{
		if (predicate == null)
		{
			predicate = asConjunction ? cb.conjunction() : cb.disjunction();
		}

		String pattern = PERCENT + value + PERCENT;

		if (caseSensitive)
		{
			return link(cb, asConjunction, predicate,
					cb.like(path, pattern));
		}
		else
		{
			return link(cb, asConjunction, predicate,
					cb.like(cb.lower(path), cb.lower(cb.literal(pattern))));
		}
	}

	private static Predicate link(CriteriaBuilder cb, boolean asConjunction, Predicate p1, Predicate p2)
	{
		return asConjunction ? cb.and(p1, p2) : cb.or(p1, p2);
	}

	private static Predicate getWhereForDate(CriteriaBuilder criteriaBuilder, Root<Consent> root, PaginationConfig config)
	{
		Predicate result = null;
		if (config.getStartDate() != null)
		{
			result = criteriaBuilder
					.greaterThanOrEqualTo(root.get(Consent_.key).get(ConsentKey_.consentDate), config.getStartDate());
		}
		if (config.getEndDate() != null)
		{
			if (result != null)
			{
				result = criteriaBuilder.and(result, criteriaBuilder.lessThanOrEqualTo(root.get(Consent_.key).get(ConsentKey_.consentDate), config.getEndDate()));
			}
			else
			{
				result = criteriaBuilder.lessThanOrEqualTo(root.get(Consent_.key).get(ConsentKey_.consentDate), config.getEndDate());
			}
		}
		return result;
	}

	private Predicate getWhereForType(CriteriaBuilder criteriaBuilder, Root<Consent> root, String domainName, PaginationConfig config) throws ObjectNotFoundException
	{
		Predicate result = null;
		if (config.getTemplateType() != null)
		{
			try
			{
				List<ConsentTemplateDTO> templateDTOs = OrgDatCache.listConsentTemplatesFIUO(domainName, true, config.getTemplateType());
				if (templateDTOs.isEmpty())
				{
					throw new ObjectNotFoundException();
				}
				else
				{
					for (ConsentTemplateDTO templateDTO : templateDTOs)
					{
						if (result == null)
						{
							result = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), templateDTO.getKey().getName());
						}
						else
						{
							result = criteriaBuilder.or(result, criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), templateDTO.getKey().getName()));
						}
					}
				}
			}
			catch (UnknownDomainException impossible)
			{
				LOGGER.fatal("impossible exception", impossible);
			}
		}
		return result;
	}

	private Predicate getWhereForSignerId(CriteriaBuilder cb, Root<Consent> root, Domain domain, String signerId, boolean useAliases) throws ObjectNotFoundException
	{
		Predicate result = null;
		Set<SignerIdDTO> signerIdDTOs = new HashSet<>();
		for (SignerIdType idType : domain.getSignerIdTypes())
		{
			signerIdDTOs.add(new SignerIdDTO(idType.getKey().getName(), signerId, idType.getOrderNumber(), null, null));
		}
		Set<VirtualPerson> signers;
		try
		{
			signers = getAllVirtualPersonsWithAtLeastOneSignerId(domain, convertToSignerIdKeys(domain, signerIdDTOs), useAliases);
			if (signers.isEmpty())
			{
				return cb.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), -1);
			}
			Set<Long> personIds = new HashSet<>();
			for (VirtualPerson signer : signers)
			{
				personIds.add(signer.getId());
			}
			for (Long personId : personIds)
			{
				if (result == null)
				{
					result = cb.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), personId);
				}
				else
				{
					result = cb.or(result, cb.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), personId));
				}
			}
		}
		catch (UnknownSignerIdTypeException e)
		{
			// impossible
			LOGGER.fatal(e);
			result = cb.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), -1);
		}
		return result;
	}

	private static Path<?> generateSortExpressionForConsent(ConsentField sortField, Root<Consent> root)
	{
		Path<?> order = null;
		if (sortField != null)
		{
			switch (sortField)
			{
				case DATE:
					order = root.get(Consent_.key).get(ConsentKey_.consentDate);
					break;
				case CT_VERSION:
					order = root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version);
					break;
				case CT_NAME:
					order = root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name);
					break;
				case CT_LABEL:
					order = root.get(Consent_.consentTemplate).get(ConsentTemplate_.label);
					break;
				case SIGNER_ID:
					LOGGER.warn("sorting is not available for signer id");
					break;
				case QC_TYPE:
					order = root.get(Consent_.qc).get(QC_.type);
					break;
				case NONE:
					break;
				default:
					LOGGER.fatal("unimplemented ConsentField '" + sortField.name() + "' for order-by-clause within generateSortExpressionForConsent()");
					break;
			}
		}
		return order;
	}

	private void addSignerIdToDBConsent(Consent consent, SignerIdDTO signerIdDTO)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException, InvalidParameterException
	{
		Domain domain = getDomain(consent.getConsentTemplate().getKey().getDomainName());
		SignerIdType signerIdType = getSignerIdType(domain, signerIdDTO.getIdType());
		try
		{
			addSignerId(signerIdType, signerIdDTO.getId());

			VirtualPerson oldVirtualPerson = consent.getVirtualPerson();
			Set<SignerIdDTO> toBeTestedSignerIDs = new HashSet<>();
			toBeTestedSignerIDs.add(signerIdDTO);

			// check if a virtual person using this very combination of signerIds exists
			for (VirtualPersonSignerId personSignerIds : oldVirtualPerson.getVirtualPersonSignerIds())
			{
				toBeTestedSignerIDs.add(personSignerIds.getSignerId().toDTO());
			}
			VirtualPerson usableVP = getOrCreateVirtualPersonFor(domain, toBeTestedSignerIDs);
			if (usableVP == oldVirtualPerson)
			{
				return;
			}
			ConsentKey possibleKey = new ConsentKey(consent.getConsentTemplate().getKey(), consent.getKey().getConsentDate(), usableVP.getId());
			if (em.find(Consent.class, possibleKey) != null)
			{
				return;
			}

			ConsentDTO consentDTO = consent.toDTO();
			Map<ModuleKeyDTO, Module> modules = validateConsentReturningObjects(consentDTO, true);
			Consent newConsent = new Consent(consent.getConsentTemplate(), consentDTO, modules, usableVP, domain.getPropertiesObject());
			em.persist(newConsent);
			// den alten nicht loeschen! bisherige anfragen muessen valid bleiben

			ConsentCache.addConsent(usableVP.getId(), convertToSignerIdKeys(domain, Collections.singleton(signerIdDTO)), getSignedPoliciesWithExpirationDates(consent.getSignedPolicies()),
					consent);
		}
		catch (InvalidVersionException | InconsistentStatusException | MissingRequiredObjectException | MandatoryFieldsException | UnknownModuleException | InvalidFreeTextException e)
		{
			String message = "unexpected exception while adding signer id to consent: " + e.getMessage();
			LOGGER.error(message);
			throw new InternalException(message);
		}
	}

	private void addSignerId(SignerIdType signerIdType, String signerId)
	{
		try
		{
			// check if signer id exists
			getSignerId(signerIdType, signerId);
		}
		catch (UnknownSignerIdException e)
		{
			SignerId newSignerId = new SignerId(signerIdType, signerId);
			em.persist(newSignerId);
		}
	}

	private SignerId getSignerId(SignerIdType signerIdType, String signerId) throws UnknownSignerIdException
	{
		return getSignerId(new SignerIdKey(signerIdType.getKey(), signerId));
	}

	private SignerId getSignerId(SignerIdKey signerIdKey) throws UnknownSignerIdException
	{
		SignerId signerIdFromDB = em.find(SignerId.class, signerIdKey);
		if (signerIdFromDB == null)
		{
			String message = "unknown signer id : " + signerIdKey.getValue() + " of type " + signerIdKey.getSignerIdTypeKey();
			LOGGER.warn(message);
			throw new UnknownSignerIdException(message);
		}
		return signerIdFromDB;
	}

	private List<Consent> getConsentsForPerson(Domain domain, VirtualPerson person)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		List<Consent> result = new ArrayList<>();
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), person.getId()),
				criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName()));
		criteriaQuery.select(root).where(predicate);
		result.addAll(em.createQuery(criteriaQuery).getResultList());
		return result;
	}

	private long countConsentWithScansForDomain(String domainName)
	{
		// weder per criteria api noch per jpl umsetzbar ...
		Query query = em.createNativeQuery(
				"SELECT COUNT(1) FROM(SELECT '' FROM consent_scan cs WHERE cs.CT_DOMAIN_NAME = ?1 GROUP BY cs.CONSENT_DATE, cs.VIRTUAL_PERSON_ID, cs.CT_VERSION, cs.CT_NAME, cs.CT_DOMAIN_NAME) x");
		query.setParameter(1, domainName);
		return (Long) query.getSingleResult();
	}

	// ------------------------------------------ public ------------------------------------------

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T persist(T entity)
	{
		EM_RWL.writeLock().lock();
		try
		{
			em.persist(entity);
			return entity;
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> void remove(Class<T> entityClass, Object key)
	{
		EM_RWL.writeLock().lock();
		try
		{
			T entity = em.find(entityClass, key);
			em.remove(entity);
			em.flush();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public DomainDTO addDomain(DomainDTO domainDTO) throws VersionConverterClassException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = new Domain(domainDTO);
			// needs to be executed before dao.persist to check the version converters
			VersionConverterCache.addDomain(domain);
			persist(domain);
			return domain.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public DomainDTO finaliseDomain(String domainName) throws UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			domain.finalise();
			return domain.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public DomainDTO updateDomain(DomainDTO dto) throws ObjectInUseException, UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(dto.getName());
			List<SignerIdType> oldSignerIdTypes = domain.getSignerIdTypes();
			for (SignerIdType oldSignerIdType : oldSignerIdTypes)
			{
				em.remove(oldSignerIdType);
			}
			em.flush();
			domain.update(dto);
			return domain.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public DomainDTO updateDomainInUse(String domainName, String label, String logo, String externProperties, ExpirationPropertiesDTO expiration, String comment)
			throws UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			domain.updateInUse(label, comment, externProperties, expiration, logo);
			return domain.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentTemplateDTO addConsentTemplate(ConsentTemplateDTO consentTemplateDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidParameterException, InvalidPropertiesException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(consentTemplateDTO.getKey().getDomainName());
			Map<ModuleKeyDTO, Module> modules = mapModules(consentTemplateDTO, domain);
			Map<ConsentTemplateKeyDTO, ConsentTemplate> templates = mapTemplates(consentTemplateDTO, domain);
			try
			{
				ConsentTemplate consentTemplate = new ConsentTemplate(domain, consentTemplateDTO, modules, templates, finaliseRelatedEntities);
				em.persist(consentTemplate);
				return consentTemplate.toDTO();
			}
			catch (InvalidPropertiesException e)
			{
				String message = "invalid properties within " + consentTemplateDTO;
				LOGGER.warn(message);
				throw new InvalidPropertiesException(message);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(createConsentTemplateKey(keyDTO));
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey), consentTemplate.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0)
			{
				String message = consentTemplate + " is in use and therefore can't be deleted";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(consentTemplate);
			em.flush();
			for (ModuleConsentTemplate moduleConsentTemplate : consentTemplate.getModuleConsentTemplates())
			{
				moduleConsentTemplate.getModule().getModuleConsentTemplates().remove(moduleConsentTemplate);
			}
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum
			// ende der transaction ein neues objekt mit gleichem key im cache
			ConsentTemplate duplicateTest = em.find(ConsentTemplate.class, consentTemplate.getKey());
			if (duplicateTest != null)
			{
				em.clear();
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentTemplateDTO finaliseTemplate(ConsentTemplateKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(createConsentTemplateKey(keyDTO));
			try
			{
				consentTemplate.finalise(finaliseRelatedEntities);
				return consentTemplate.toDTO();
			}
			catch (RequirementsNotFullfilledException e)
			{
				LOGGER.error(e.getLocalizedMessage());
				throw e;
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentTemplateDTO updateConsentTemplate(ConsentTemplateDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException,
			FreeTextConverterStringException, InvalidFreeTextException, InvalidPropertiesException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(createConsentTemplateKey(dto.getKey()));
			Domain domain = getDomain(dto.getKey().getDomainName());
			consentTemplate.update(dto, finaliseRelatedEntities);

			// ========= free text =========

			Map<FreeTextDefKey, FreeTextDef> oldFreeTextDefs = new HashMap<>();
			for (FreeTextDef oldFreeTextDef : consentTemplate.getFreeTextDefs())
			{
				oldFreeTextDefs.put(oldFreeTextDef.getKey(), oldFreeTextDef);
			}
			List<FreeTextDef> newFreeTextDefs = new ArrayList<>();
			for (FreeTextDefDTO freeTextDTO : dto.getFreeTextDefs())
			{
				FreeTextDefKey key = new FreeTextDefKey(consentTemplate.getKey(), freeTextDTO.getName());
				if (oldFreeTextDefs.containsKey(key))
				{
					FreeTextDef oldFreeTextDef = oldFreeTextDefs.get(key);
					oldFreeTextDef.update(freeTextDTO);
					newFreeTextDefs.add(oldFreeTextDef);
					oldFreeTextDefs.remove(key);
				}
				else
				{
					newFreeTextDefs.add(new FreeTextDef(consentTemplate, freeTextDTO));
				}
			}
			consentTemplate.getFreeTextDefs().clear();
			newFreeTextDefs.sort(new FreeTextDef.FreeTextDefComparator());
			consentTemplate.getFreeTextDefs().addAll(newFreeTextDefs);
			for (FreeTextDef unusedFreeTextDef : oldFreeTextDefs.values())
			{
				em.remove(unusedFreeTextDef);
			}

			// ========= module templates =========

			Map<ModuleKeyDTO, Module> modules = mapModules(dto, domain);
			Map<Module, ModuleConsentTemplate> oldMCTs = new HashMap<>();
			for (ModuleConsentTemplate oldMCT : consentTemplate.getModuleConsentTemplates())
			{
				oldMCTs.put(oldMCT.getModule(), oldMCT);
			}
			List<ModuleConsentTemplate> newMCTs = new ArrayList<>();
			for (AssignedModuleDTO assignedModuleDTO : dto.getAssignedModules())
			{
				Module module = modules.get(assignedModuleDTO.getModule().getKey());
				Module parent = null;
				if (assignedModuleDTO.getParent() != null)
				{
					parent = modules.get(assignedModuleDTO.getParent());
					if (parent == null)
					{
						throw new UnknownModuleException(assignedModuleDTO.getParent() + ", which is set as parent of " + module + " is not part of "
								+ consentTemplate.getKey());
					}
				}
				if (oldMCTs.containsKey(module))
				{
					ModuleConsentTemplate oldMCT = oldMCTs.get(module);
					oldMCT.update(assignedModuleDTO, parent);
					newMCTs.add(oldMCT);
					oldMCTs.remove(module);
				}
				else
				{
					newMCTs.add(new ModuleConsentTemplate(consentTemplate, module, assignedModuleDTO, parent));
				}
			}

			for (ModuleConsentTemplate mct : consentTemplate.getModuleConsentTemplates())
			{
				mct.getModule().getModuleConsentTemplates().remove(mct);
			}
			consentTemplate.getModuleConsentTemplates().clear();
			consentTemplate.getModuleConsentTemplates().addAll(newMCTs);
			for (ModuleConsentTemplate mct : newMCTs)
			{
				mct.getModule().getModuleConsentTemplates().add(mct);
			}
			for (ModuleConsentTemplate unusedMCT : oldMCTs.values())
			{
				unusedMCT.getModule().getModuleConsentTemplates().remove(unusedMCT);
				em.remove(unusedMCT);
			}

			// ========= mapped templates =========

			updateMappedTemplates(dto, consentTemplate);

			return consentTemplate.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentTemplateDTO updateConsentTemplateInUse(ConsentTemplateDTO dto) throws InvalidFreeTextException, InvalidVersionException,
			UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(createConsentTemplateKey(dto.getKey()));
			consentTemplate.updateInUse(dto, true);
			updateMappedTemplates(dto, consentTemplate);
			return consentTemplate.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	private void updateMappedTemplates(ConsentTemplateDTO dto, ConsentTemplate consentTemplate)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException
	{
		// first validate, that all mappings relate to the same domain
		Set<String> domains = dto.getAllMappedTemplates().stream().map(ConsentTemplateKeyDTO::getDomainName).collect(Collectors.toSet());
		domains.add(dto.getDomainName());

		if (domains.size() > 1)
		{
			throw new InvalidParameterException("Mapped templates must not relate to different domains: " + domains);
		}

		// convert consent template key DTOs to consent template DB models
		Map<ConsentTemplateKeyDTO, ConsentTemplate> templates = mapTemplates(dto, consentTemplate.getDomain());
		Set<ConsentTemplate> newMappedCTs = new HashSet<>(templates.values());

		// remove existing mappings from the set of new mapped CTs and add omitted mappings to the set of mappings to remove
		Set<MappedConsentTemplate> mappingsToRemove = consentTemplate.getMappedConsentTemplates().stream()
				.filter(m -> !newMappedCTs.remove(m.getConsentTemplateTo())).collect(Collectors.toSet());

		// remove omitted mappings including their reverse versions from relations and DB
		for (MappedConsentTemplate mapping : mappingsToRemove)
		{
			consentTemplate.getMappedConsentTemplates().remove(mapping);

			// find managed reverse mapping
			MappedConsentTemplate inverseMapping = findInverseMappedTemplate(mapping);

			if (inverseMapping != null)
			{
				mapping.getConsentTemplateTo().getMappedConsentTemplates().remove(inverseMapping);
				em.remove(inverseMapping);
			}
			em.remove(mapping);
		}

		// create added mappings including their reverse versions as relations and DB objects
		for (ConsentTemplate mappedCT : newMappedCTs)
		{
			MappedConsentTemplate mapping = new MappedConsentTemplate(consentTemplate, mappedCT);
			consentTemplate.getMappedConsentTemplates().add(mapping);

			MappedConsentTemplate reverseMapping = new MappedConsentTemplate(mappedCT, consentTemplate);
			mappedCT.getMappedConsentTemplates().add(reverseMapping);
		}
		em.flush();
		for (MappedConsentTemplate mapping : mappingsToRemove)
		{
			ConsentTemplateKeyDTO ctKeyToRemove = mapping.getConsentTemplateTo().getKey().toDTO(VersionConverterCache.getCTVersionConverter(dto.getDomainName()));
			OrgDatCache.removeMappedTemplateFromConsentTemplate(dto.getKey(), ctKeyToRemove);
			// inverse
			OrgDatCache.removeMappedTemplateFromConsentTemplate(ctKeyToRemove, dto.getKey());
		}
		for (ConsentTemplate mappedCT : newMappedCTs)
		{
			ConsentTemplateKeyDTO ctKeyToAdd = mappedCT.getKey().toDTO(VersionConverterCache.getCTVersionConverter(dto.getDomainName()));
			OrgDatCache.addMappedTemplateToConsentTemplate(dto.getKey(), ctKeyToAdd, mappedCT.getType());
			// inverse
			OrgDatCache.addMappedTemplateToConsentTemplate(ctKeyToAdd, dto.getKey(), dto.getType());
		}
	}

	private MappedConsentTemplate findInverseMappedTemplate(MappedConsentTemplate mapping)
	{
		return mapping.getConsentTemplateTo().getMappedConsentTemplates().stream()
				.filter(m -> m.getConsentTemplateTo().getKey().equals(mapping.getConsentTemplateFrom().getKey()))
				.findFirst().orElse(null);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ModuleDTO addModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(moduleDTO.getKey().getDomainName());
			if (!domain.getFinalised())
			{
				String message = moduleDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			ModuleKey key = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domain.getName()), moduleDTO.getKey());
			Module duplicateTest = em.find(Module.class, key);
			if (duplicateTest != null)
			{
				String message = key + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			Module module = new Module(domain, moduleDTO, finaliseRelatedEntities);
			List<String> policyNames = new ArrayList<>();
			Map<Policy, ModulePolicy> policies = new HashMap<>();
			for (AssignedPolicyDTO assignedPolicyDTO : moduleDTO.getAssignedPolicies())
			{
				String policyName = assignedPolicyDTO.getPolicy().getKey().getName();
				if (policyNames.contains(policyName))
				{
					String message = "multiple occurences of policy '" + policyName + "' within " + moduleDTO;
					LOGGER.warn(message);
					throw new DuplicateEntryException(message);
				}
				PolicyKey policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), assignedPolicyDTO.getPolicy().getKey());
				Policy policy = getPolicy(policyKey);
				policyNames.add(policyName);
				ModulePolicy mp = new ModulePolicy(module, policy, assignedPolicyDTO);
				module.getModulePolicies().add(mp);
				policies.put(policy, mp);
			}
			em.persist(module);
			for (Entry<Policy, ModulePolicy> entry : policies.entrySet())
			{
				entry.getKey().getModulePolicies().add(entry.getValue());
			}
			return module.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteModule(ModuleKeyDTO keyDTO) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownModuleException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			if (!module.getModuleConsentTemplates().isEmpty())
			{
				String message = module + " is in use and therefore can't be deleted";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(module);
			// relationen werden in der @PreRemove in der entity aufgeraeumt
			em.flush();
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum
			// ende der transaction ein neues objekt mit gleichem key im cache
			Module duplicateTest = em.find(Module.class, module.getKey());
			if (duplicateTest != null)
			{
				em.clear();
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ModuleDTO finaliseModule(ModuleKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			try
			{
				module.finalise(finaliseRelatedEntities);
				return module.toDTO();
			}
			catch (RequirementsNotFullfilledException e)
			{
				LOGGER.error(e.getLocalizedMessage());
				throw e;
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ModuleDTO updateModule(ModuleDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidParameterException, InvalidVersionException,
			ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(dto.getKey().getDomainName());
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(dto.getKey().getDomainName()), dto.getKey()));
			List<String> policyNames = new ArrayList<>();
			Map<Policy, ModulePolicy> policies = new HashMap<>();
			for (AssignedPolicyDTO assignedPolicyDTO : dto.getAssignedPolicies())
			{
				String policyName = assignedPolicyDTO.getPolicy().getKey().getName();
				if (policyNames.contains(policyName))
				{
					String message = "multiple occurences of policy '" + policyName + "' within " + dto;
					LOGGER.warn(message);
					throw new DuplicateEntryException(message);
				}
				PolicyKey policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), assignedPolicyDTO.getPolicy().getKey());
				Policy policy = getPolicy(policyKey);
				policyNames.add(policyName);
				ModulePolicy mp = new ModulePolicy(module, policy, assignedPolicyDTO);
				policies.put(policy, mp);
			}
			module.update(dto, finaliseRelatedEntities);
			for (ModulePolicy mp : module.getModulePolicies())
			{
				mp.getPolicy().getModulePolicies().remove(mp);
				em.remove(mp);
			}
			module.getModulePolicies().clear();
			em.flush();
			for (Entry<Policy, ModulePolicy> entry : policies.entrySet())
			{
				em.persist(entry.getValue());
				module.getModulePolicies().add(entry.getValue());
				entry.getKey().getModulePolicies().add(entry.getValue());
			}
			return module.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ModuleDTO updateModuleInUse(ModuleKeyDTO keyDTO, String label, String shortText, String externProperties, String comment, Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			module.updateInUse(label, shortText, comment, externProperties, assignedPolicyDTOs);
			return module.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PolicyDTO addPolicy(PolicyDTO policyDTO) throws DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(policyDTO.getKey().getDomainName());
			if (!domain.getFinalised())
			{
				String message = policyDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			PolicyKey key = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), policyDTO.getKey());
			Policy duplicateTest = em.find(Policy.class, key);
			if (duplicateTest != null)
			{
				String message = key + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			Policy policy = new Policy(domain, policyDTO);
			em.persist(policy);
			return policy.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deletePolicy(PolicyKeyDTO keyDTO) throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<SignedPolicy> root = criteriaQuery.from(SignedPolicy.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignedPolicy_.key).get(SignedPolicyKey_.policyKey), policy.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0 || !policy.getModulePolicies().isEmpty())
			{
				String message = policy + " is in use and therefore can't be deleted";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(policy);
			// relationen werden in der @PreRemove in der entity aufgeraeumt
			em.flush();
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum
			// ende der transaction ein neues objekt mit gleichem key im cache
			Policy duplicateTest = em.find(Policy.class, policy.getKey());
			if (duplicateTest != null)
			{
				em.clear();
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PolicyDTO finalisePolicy(PolicyKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			policy.finalise();
			return policy.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PolicyDTO updatePolicy(PolicyDTO dto) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(dto.getKey().getDomainName()), dto.getKey()));
			policy.update(dto);
			return policy.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PolicyDTO updatePolicyInUse(PolicyKeyDTO keyDTO, String label, String externProperties, String comment)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			policy.updateInUse(label, comment, externProperties);
			return policy.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public SignerIdTypeDTO addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerIdType signerIdType = new SignerIdType(domain, signerIdTypeName);
			if (domain.getSignerIdTypes().contains(signerIdType))
			{
				String message = "signer id type '" + signerIdTypeName + "' already exists within " + domain;
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			em.persist(signerIdType);
			// relationen nach persistierung
			domain.getSignerIdTypes().add(signerIdType);
			return signerIdType.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<SignerId> root = criteriaQuery.from(SignerId.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignerId_.key).get(SignerIdKey_.signerIdTypeKey), signerIdType.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0)
			{
				String message = "signer id type '" + signerIdTypeName + "' for " + domain + " is in use and therefore can't be deleted";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(signerIdType);
			// relationen koennen nicht in der @PreRemove in der entity aufgeraeumt werden -
			// das macht sonst die cascade-delete funktionalitaet domain<->signerIdTypes
			// kaputt
			domain.getSignerIdTypes().remove(signerIdType);
			em.flush();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public SignerIdTypeDTO updateSignerIdType(String domainName, String signerIdTypeName, String label, String comment) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			signerIdType.update(label, comment);
			return signerIdType.toDTO();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ service ------------------------------------------

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException, InvalidParameterException, RequirementsNotFullfilledException
	{
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		EM_RWL.writeLock().lock();
		try
		{
			String domainName = consentKeyDTO.getConsentTemplateKey().getDomainName();
			Domain domain = getDomain(domainName);
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			if (!consentTemplate.getFinalised())
			{
				String message = consentKeyDTO + " can't be added because the consent template isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			Map<ModuleKeyDTO, Module> modules = validateConsentReturningObjects(consentDTO, true);
			VirtualPerson virtualPerson = getOrCreateVirtualPersonFor(domain, consentKeyDTO.getSignerIds());
			ConsentKey consentKey = new ConsentKey(consentTemplate.getKey(), consentKeyDTO.getConsentDate(), virtualPerson.getId());
			if (em.find(Consent.class, consentKey) != null)
			{
				String message = consentKeyDTO + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}

			Consent newConsent = new Consent(consentTemplate, consentDTO, modules, virtualPerson, domain.getPropertiesObject());
			em.persist(newConsent);
			ConsentCache.addConsent(virtualPerson.getId(), convertToSignerIdKeys(domain, consentKeyDTO.getSignerIds()),
					getSignedPoliciesWithExpirationDates(newConsent.getSignedPolicies()), newConsent);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.readLock().lock();
		try
		{
			PolicyKey policyKey;
			if (!config.getIgnoreVersionNumber())
			{
				OrgDatCache.getPolicyFIUO(policyKeyDTO);
				policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(policyKeyDTO.getDomainName()), policyKeyDTO);
			}
			else
			{
				checkPolicyName(policyKeyDTO.getName());
				policyKey = new PolicyKey(policyKeyDTO.getDomainName(), policyKeyDTO.getName(), 0);
			}
			return getConsentStatusTypeFromTo(signerIdDTOs, policyKeyDTO.getDomainName(), policyKeyDTO.getName(), policyKey.getVersion(), policyKey.getVersion(), config);
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	/**
	 * from <= version <= to
	 *
	 * @param signerIdDTOs
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 * @return
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownDomainException
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, int versionFrom,
			int versionTo, CheckConsentConfig config) throws UnknownSignerIdTypeException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			ConsentStatusType result = ConsentStatusType.UNKNOWN;
			List<CachedSignedPolicy> cachedSignedPolicies;
			ConsentStatusType consentStatusTypeForSigner;
			Domain domain = getDomain(domainName);
			switch (config.getIdMatchingType())
			{
				case AT_LEAST_ONE:
					Set<SignerIdKey> siks = getSIKsForSignerIds(domain, signerIdDTOs, config.isUseAliases());
					for (SignerIdKey sik : siks)
					{
						if (config.getIgnoreVersionNumber())
						{
							cachedSignedPolicies = ConsentCache.getCachedPoliciesForSigner(domainName, sik, policyName);
						}
						else
						{
							cachedSignedPolicies = ConsentCache.getCachedPoliciesForSigner(domainName, sik, policyName, versionFrom,
									versionTo);
						}
						consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
						if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED))
						{
							result = consentStatusTypeForSigner;
						}
						else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED))
						{
							result = consentStatusTypeForSigner;
							break;
						}
					}
					break;
				case AT_LEAST_ALL:
					List<VirtualPerson> persons;
					try
					{
						persons = findAllVirtualPersonsWithAtLeastTheSignerIds(domain, signerIdDTOs, config.isUseAliases());
					}
					catch (ObjectNotFoundException e)
					{
						// TODO hier lieber ne UnknownSignerIdException werfen?
						LOGGER.warn("could not find virtualPerson for signerIds " + signerIdDTOs);
						return config.getUnknownStateIsConsideredAsDecline() ? ConsentStatusType.DECLINED : ConsentStatusType.UNKNOWN;
					}
					cachedSignedPolicies = new ArrayList<>();
					for (VirtualPerson person : persons)
					{
						if (config.getIgnoreVersionNumber())
						{
							cachedSignedPolicies.addAll(ConsentCache.getCachedPoliciesForVP(domainName, person, policyName));
						}
						else
						{
							cachedSignedPolicies
									.addAll(ConsentCache.getCachedPoliciesForVP(domainName, person, policyName, versionFrom, versionTo));
						}
					}
					consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
					if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED) || consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED))
					{
						result = consentStatusTypeForSigner;
					}
					break;
				case EXACT:
					VirtualPerson person;
					try
					{
						person = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, signerIdDTOs));
					}
					catch (ObjectNotFoundException e)
					{
						// TODO hier lieber ne UnknownSignerIdException werfen?
						LOGGER.warn("could not find virtualPerson for signerIds " + signerIdDTOs);
						return config.getUnknownStateIsConsideredAsDecline() ? ConsentStatusType.DECLINED : ConsentStatusType.UNKNOWN;
					}
					if (config.getIgnoreVersionNumber())
					{
						cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domainName, person, policyName);
					}
					else
					{
						cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domainName, person, policyName, versionFrom, versionTo);
					}
					consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
					if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED) || consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED))
					{
						result = consentStatusTypeForSigner;
					}
					break;
				default:
					LOGGER.fatal("unimplemented IdMatching '" + config.getIdMatchingType() + "' for matching config within getConsentStatusTypeFromTo()");
					break;
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName) throws InvalidVersionException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplate consentTemplate : queryConsentTemplates(domainName))
			{
				result.add(consentTemplate.toDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<ConsentTemplate> queryConsentTemplates(String domainName)
	{
		// mehrere moegliche sql-queries fuer die aufgabe:
		// 1. select * from (select * from consent_template order by version desc) as x
		// group by name;
		// 2. select t1.* from consent_template as ct1 left join consent_template as ct2
		// on (ct1.NAME = ct2.NAME and ct1.VERSION <
		// ct2.VERSION) where ct2.VERSION is null;
		// 3. select * from consent_template ct1 where ct1.version = (select
		// max(version) from consent_template ct2 where ct1.NAME = ct2.NAME);
		// nur die 3. laesst sich momentan mit der criteria-api umsetzen
		// bei 1. haengt's daran, dass subqueries nicht nach from stehen duerfen
		// fuer 2. muesste man extra eine selbst-reference im consent_template-db-modell
		// einbauen - nur ueber diese sind joins moeglich
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
		Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
		Subquery<Integer> subQuery = criteriaQuery.subquery(Integer.class);
		Root<ConsentTemplate> subRoot = subQuery.from(ConsentTemplate.class);
		Predicate subPredicate = criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name),
				subRoot.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name));
		subPredicate = criteriaBuilder.and(subPredicate, criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName),
				subRoot.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName)));
		subPredicate = criteriaBuilder.and(subPredicate, criteriaBuilder.isTrue(subRoot.get(ConsentTemplate_.finalised)));
		subQuery.select(criteriaBuilder.max(subRoot.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version))).where(subPredicate);
		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName),
				criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version), subQuery));
		criteriaQuery.select(root).where(predicate);
		return em.createQuery(criteriaQuery).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignedPolicyDTO> getPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			List<Consent> consents = getConsentsForSignerIds(domain, signerIdDTOs, useAliases);
			for (Consent consent : consents)
			{
				if (consent.getQc().isQcPassed())
				{
					ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
							consent.getVirtualPerson());
					for (SignedPolicy signedPolicy : consent.getSignedPolicies())
					{
						PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey()
								.toDTO(VersionConverterCache.getPolicyVersionConverter(domainName));
						SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), policyKeyDTO, consentKeyDTO, signedPolicy.getFhirID());
						result.add(signedPolicyDTO);
					}
				}
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(policyKeyDTO.getDomainName());
			List<Consent> consents = getConsentsForSignerIds(domain, signerIdDTOs, useAliases);
			for (Consent consent : consents)
			{
				if (consent.getQc().isQcPassed())
				{
					ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(VersionConverterCache.getCTVersionConverter(domain.getName()),
							consent.getVirtualPerson());
					for (SignedPolicy signedPolicy : consent.getSignedPolicies())
					{
						PolicyKeyDTO currentPolicyKeyDTO = signedPolicy.getKey().getPolicyKey()
								.toDTO(VersionConverterCache.getPolicyVersionConverter(domain.getName()));
						if (currentPolicyKeyDTO.equals(policyKeyDTO))
						{
							SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), currentPolicyKeyDTO, consentKeyDTO, signedPolicy.getFhirID());
							result.add(signedPolicyDTO);
						}
					}
				}
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			List<Consent> consents = getConsentsForSignerIds(domain, signerIdDTOs, useAliases);
			for (Consent consent : consents)
			{
				if (consent.getQc().isQcPassed())
				{
					ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
							consent.getVirtualPerson());
					for (SignedPolicy signedPolicy : consent.getSignedPolicies())
					{
						if (signedPolicy.getKey().getPolicyKey().getName().equals(policyName))
						{
							PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey()
									.toDTO(VersionConverterCache.getPolicyVersionConverter(domainName));
							SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), policyKeyDTO, consentKeyDTO, signedPolicy.getFhirID());
							result.add(signedPolicyDTO);
						}
					}
				}
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdException, InvalidVersionException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			List<PolicyDTO> policies = OrgDatCache.listPoliciesFIUO(domainName, true);
			List<CachedSignedPolicy> cachedSignedPolicies;
			CachedSignedPolicy currentCachedSignedPolicy;
			switch (config.getIdMatchingType())
			{
				case AT_LEAST_ONE:
					Set<SignerIdKey> siks = getSIKsForSignerIds(domain, signerIdDTOs, config.isUseAliases());
					for (SignerIdKey sik : siks)
					{
						for (PolicyDTO policy : policies)
						{
							cachedSignedPolicies = ConsentCache.getCachedPoliciesForSigner(domain.getName(), sik, policy.getKey().getName());
							try
							{
								currentCachedSignedPolicy = getCurrentPolicy(config, cachedSignedPolicies, domain);
								SignedPolicy sp = getSignedPolicy(currentCachedSignedPolicy.getSPKey());
								result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
										currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
										currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
												getVirtualPerson(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
										sp.getFhirID()));
							}
							catch (ObjectNotFoundException ignore)
							{
								// could occur
							}
						}
					}
					break;
				case AT_LEAST_ALL:
					List<VirtualPerson> persons;
					try
					{
						persons = findAllVirtualPersonsWithAtLeastTheSignerIds(domain, signerIdDTOs, config.isUseAliases());
					}
					catch (ObjectNotFoundException e)
					{
						String message = "could not find virtualPerson for signerIds " + signerIdDTOs;
						LOGGER.warn(message);
						throw new UnknownSignerIdException(message);
					}
					for (VirtualPerson person : persons)
					{
						for (PolicyDTO policy : policies)
						{
							try
							{
								cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policy.getKey().getName());
								currentCachedSignedPolicy = getCurrentPolicy(config, cachedSignedPolicies, domain);
								SignedPolicy sp = getSignedPolicy(currentCachedSignedPolicy.getSPKey());
								result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
										currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
										currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
												getVirtualPerson(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
										sp.getFhirID()));
							}
							catch (ObjectNotFoundException ignore)
							{
								// could occur
							}
						}
					}
					break;
				case EXACT:
					VirtualPerson person;
					try
					{
						person = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, signerIdDTOs));
					}
					catch (ObjectNotFoundException e)
					{
						String message = "could not find virtualPerson for signerIds " + signerIdDTOs;
						LOGGER.warn(message);
						throw new UnknownSignerIdException(message);
					}
					for (PolicyDTO policy : policies)
					{
						try
						{
							cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policy.getKey().getName());
							currentCachedSignedPolicy = getCurrentPolicy(config, cachedSignedPolicies, domain);
							SignedPolicy sp = getSignedPolicy(currentCachedSignedPolicy.getSPKey());
							result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
									currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
									currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
											getVirtualPerson(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
									sp.getFhirID()));
						}
						catch (ObjectNotFoundException ignore)
						{
							// could occur
						}
					}
					break;
				default:
					LOGGER.fatal("unimplemented IdMatching '" + config.getIdMatchingType() + "' for matching config within getConsentStatusTypeFromTo()");
					break;
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
			Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name), consentTemplateName),
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName));
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.isTrue(root.get(ConsentTemplate_.finalised)));
			criteriaQuery.select(root).where(predicate)
					.orderBy(criteriaBuilder.desc(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version)));
			List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
			if (ctList.isEmpty())
			{
				String message = "unknown consent template with name '" + consentTemplateName + "' for domain '" + domainName + "'";
				LOGGER.warn(message);
				throw new UnknownConsentTemplateException(message);
			}
			return OrgDatCache.getConsentTemplateCFEU(ctList.get(0).getKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName)));
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentLightDTO> getAllConsentsForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownDomainException,
			UnknownSignerIdTypeException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			List<Consent> consents = getConsentsForSignerIds(domain, signerIdDTOs, useAliases);
			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentDTO getCurrentConsentForSignerIdsAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException, UnknownSignerIdTypeException
	{
		ConsentDTO result = null;
		long now = System.currentTimeMillis();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(ctKeyDTO.getDomainName());
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Set<VirtualPerson> signers = getAllVirtualPersonsWithAtLeastOneSignerId(domain, convertToSignerIdKeys(domain, signerIdDTOs), useAliases);
			VersionConverter ctVC = VersionConverterCache.getCTVersionConverter(domain.getName());
			// TODO max date schon in die abfrage mit reinnehmen - die max-funktion der
			// criteria-api kann aber nur number
			// Subquery<Date> subQuery = criteriaQuery.subquery(Date.class);
			// Root<Consent> subRoot = subQuery.from(Consent.class);
			// Predicate subPredicate =
			// criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name),
			// subRoot.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name));
			// // +domain +version
			// subQuery.select(criteriaBuilder.max(subRoot.get(Consent_.key).get(ConsentKey_.consentDate))).where(subPredicate);
			Consent temp = null;
			for (VirtualPerson signer : signers)
			{
				Predicate predicate = criteriaBuilder.and(
						criteriaBuilder.and(criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), signer.getId()),
								criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName),
										ctKeyDTO.getDomainName())),
						criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), ctKeyDTO.getName()));
				if (!ignoreVersionNumber)
				{
					predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
							root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), ctVC.stringToInt(ctKeyDTO.getVersion())));
				}
				criteriaQuery.select(root).where(predicate);
				List<Consent> consentList = em.createQuery(criteriaQuery).getResultList();
				for (Consent consent : consentList)
				{
					if (consent.getQc().isQcPassed())
					{
						ConsentDateValues consentDates = consent.getConsentDateValues();
						if (consentDates.getConsentExpirationTimestamp() > now
								&& (temp == null || consentDates.getLegalConsentTimestamp() > temp.getConsentDateValues().getLegalConsentTimestamp() ||
										consentDates.getLegalConsentTimestamp() == temp.getConsentDateValues().getLegalConsentTimestamp() &&
												consent.getCreateTimestamp().after(temp.getCreateTimestamp())))
						{
							temp = consent;
						}
					}
				}
			}
			if (temp != null)
			{
				result = temp.toDTO();
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey),
					createConsentTemplateKey(ctKeyDTO));
			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();
			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName) throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName),
					domainName);
			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName) throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domainName),
					criteriaBuilder.isEmpty(root.get(Consent_.scans)));

			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, PaginationConfig config)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			try
			{
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Consent> cq = cb.createQuery(Consent.class);
				Root<Consent> root = cq.from(Consent.class);
				cq.select(root).where(generateWhereForConsent(cb, root, domain, config));

				Expression<?> order = generateSortExpressionForConsent(config.getSortField(), root);
				if (order != null)
				{
					cq.orderBy(config.isSortIsAscending() ? cb.asc(order) : cb.desc(order));
				}

				List<Consent> consents = em.createQuery(cq).setFirstResult(config.getFirstEntry()).setMaxResults(config.getPageSize()).getResultList();

				for (Consent consent : consents)
				{
					result.add(consent.toLightDTO());
				}
			}
			catch (ObjectNotFoundException e)
			{
				// nothing to do here
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public long countConsentsForDomainWithFilter(String domainName, PaginationConfig config) throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		long result;
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			try
			{
				CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
				CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
				Root<Consent> root = criteriaQuery.from(Consent.class);
				criteriaQuery.select(criteriaBuilder.count(root)).where(generateWhereForConsent(criteriaBuilder, root, domain, config));
				result = em.createQuery(criteriaQuery).getSingleResult();
			}
			catch (ObjectNotFoundException e)
			{
				result = 0;
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException
	{
		EM_RWL.readLock().lock();
		try
		{
			Consent result = getConsentObject(keyDTO);
			return result.toDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentLightDTO getConsentLight(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException
	{
		EM_RWL.readLock().lock();
		try
		{
			Consent result = getConsentObject(keyDTO);
			return result.toLightDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateConsentInUse(ConsentKeyDTO consentKeyDTO, String externProperties, String comment, ConsentScanDTO scan, String notificationClientID)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException,
			UnknownConsentException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			getConsentObject(consentKeyDTO).updateInUse(comment, externProperties, scan);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType, String fileName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, DuplicateEntryException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Consent consent = getConsentObject(consentKeyDTO);
			ConsentScan consentScan = new ConsentScan(consent, scanBase64, fileType, fileName);
			consent.addScan(consentScan);
			em.persist(consentScan);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeScanFromConsent(ConsentKeyDTO consentKeyDTO, String fhirId)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException, InvalidVersionException,
			UnknownSignerIdTypeException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentScan> criteriaQuery = criteriaBuilder.createQuery(ConsentScan.class);
			Root<ConsentScan> root = criteriaQuery.from(ConsentScan.class);
			Predicate predicate = criteriaBuilder.equal(root.get(ConsentScan_.fhirID), fhirId);

			ConsentScan sc = em.createQuery(criteriaQuery.select(root).where(predicate)).getSingleResult();
			if (sc != null)
			{
				Consent consent = getConsentObject(consentKeyDTO);
				consent.removeScan(fhirId);

				em.remove(sc);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		List<String> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<SignerId> criteriaQuery = criteriaBuilder.createQuery(SignerId.class);
			Root<SignerId> root = criteriaQuery.from(SignerId.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignerId_.key).get(SignerIdKey_.signerIdTypeKey), signerIdType.getKey());
			criteriaQuery.select(root).where(predicate);
			List<SignerId> signerIds = em.createQuery(criteriaQuery).getResultList();
			for (SignerId signerId : signerIds)
			{
				result.add(signerId.getKey().getValue());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, InvalidVersionException
	{
		List<String> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(policyKeyDTO.getDomainName());
			List<String> signerIds = getAllIdsForSignerIdType(domain.getName(), signerIdTypeName);
			for (String signerId : signerIds)
			{
				SignerIdDTO signerIdDTO = new SignerIdDTO(signerIdTypeName, signerId, null, null);
				Set<SignerIdDTO> dummySet = new HashSet<>();
				dummySet.add(signerIdDTO);
				if (ConsentStatusType.ACCEPTED.equals(getConsentStatusType(dummySet, policyKeyDTO, config)))
				{
					result.add(signerId);
				}
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<ModuleKeyDTO, Module> validateConsentReturningObjects(ConsentDTO consentDTO, boolean allowRevoke)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, InvalidParameterException
	{
		EM_RWL.readLock().lock();
		try
		{
			ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
			// module-map fuer relationen
			Map<ModuleKeyDTO, Module> result = new HashMap<>();
			Domain domain = getDomain(consentKeyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			List<ModuleConsentTemplate> moduleConsentTemplates = consentTemplate.getModuleConsentTemplates();
			List<ModuleKeyDTO> validModulesForTemplate = new ArrayList<>();
			// test, ob fuer alle modules ein consent status gesetzt wurde
			// gleichzeitig auf mandatory-felder-logik pruefen
			// wenn widerruf nicht zugelassen werden soll, einfach so tun, als waere einem
			// mandatory-feld zugestimmt
			boolean mandatoryFieldsTrue = !allowRevoke;
			boolean mandatoryFieldsFalse = false;
			boolean optionalFieldsTrue = false;
			for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates)
			{
				ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getKey().getModuleKey()
						.toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName()));
				validModulesForTemplate.add(moduleKeyDTO);
				ModuleStateDTO moduleState = consentDTO.getModuleStates().get(moduleKeyDTO);
				if (moduleState == null)
				{
					String message = "missing module: " + moduleKeyDTO;
					LOGGER.warn(message);
					throw new MissingRequiredObjectException(message);
				}
				// TODO die abfrage ist wegen der nutzung von modulen ohne policy als "infomodule" -
				// bei addSignerIdToConsent gibt's dann natuerlich keinen modulstatus ...
				if (!moduleConsentTemplate.getModule().getModulePolicies().isEmpty())
				{
					ConsentStatus consentStatus = moduleState.getConsentState();
					if (consentStatus == null)
					{
						String message = "there's no consent status set for: " + moduleKeyDTO;
						LOGGER.warn(message);
						throw new MissingRequiredObjectException(message);
					}
					ConsentStatusType csType = consentStatus.getConsentStatusType();
					// test fuer mandatory/optional regeln
					if (moduleConsentTemplate.getMandatory())
					{
						if (csType.equals(ConsentStatusType.ACCEPTED))
						{
							mandatoryFieldsTrue = true;
						}
						else if (csType.equals(ConsentStatusType.DECLINED))
						{
							mandatoryFieldsFalse = true;
						}
						else
						{
							String message = "all mandatory fields must have a consent status of either "
									+ ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED) + " or "
									+ ConsentStatus.getStringForType(ConsentStatusType.DECLINED);
							LOGGER.warn(message);
							throw new MandatoryFieldsException(message);
						}
					}
					else if (csType.equals(ConsentStatusType.ACCEPTED))
					{
						optionalFieldsTrue = true;
					}
					// test fuer parent regeln
					if (moduleConsentTemplate.getParent() != null && csType.equals(ConsentStatusType.ACCEPTED))
					{
						ModuleStateDTO parentModuleState = consentDTO.getModuleStates()
								.get(moduleConsentTemplate.getParent().getKey().toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName())));
						ConsentStatus parentConsentStatus = parentModuleState == null ? null : parentModuleState.getConsentState();
						if (parentConsentStatus == null || !ConsentStatusType.ACCEPTED.equals(parentConsentStatus.getConsentStatusType()))
						{
							String message = "the consent status of the parent module (" + moduleConsentTemplate.getParent().getKey()
									+ ") also need to be one of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
									+ " if the consent status of the child module (" + moduleKeyDTO + ") is one of them";
							LOGGER.warn(message);
							throw new MandatoryFieldsException(message);
						}
					}
				}
			}

			if (mandatoryFieldsFalse && mandatoryFieldsTrue)
			{
				String message = "mandatory modules cannot have a consent status of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
						+ " and one of " + ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + " within one consent";
				LOGGER.warn(message);
				throw new MandatoryFieldsException(message);
			}
			else if (mandatoryFieldsFalse && optionalFieldsTrue)
			{
				String message = "if at least one mandatory module have a consent status of "
						+ ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + ", all optional modules may not have a consent status of "
						+ ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED);
				LOGGER.warn(message);
				throw new MandatoryFieldsException(message);
			}

			if (optionalFieldsTrue || mandatoryFieldsTrue)
			{
				if (!domain.getPropertiesObject().isNoMandatoryScans()
						&& consentDTO.getScans().isEmpty()
						&& (consentDTO.getPatientSignatureBase64() == null || consentDTO.getPhysicianSignatureBase64() == null
								|| consentDTO.getPatientSignatureBase64().trim().isEmpty()
								|| consentDTO.getPhysicianSignatureBase64().trim().isEmpty()))
				{
					String message = "signatures of both the patient and a physician are mandatory as well as the scan of the consent if at least one module is consented. "
							+ consentDTO.getKey();
					LOGGER.warn(message);
					throw new MissingRequiredObjectException(message);
				}
				else if (consentDTO.getPatientSigningDate() == null || consentDTO.getPhysicianSigningDate() == null)
				{
					String message = "Need signature dates if at least one module is consented."
							+ consentDTO.getKey();
					LOGGER.warn(message);
					throw new MissingRequiredObjectException(message);
				}
				else if (consentDTO.getKey().getConsentDate().before(MINDATE_FOR_SIGNATURE))
				{
					String message = "consent date need to be after " + MINDATE_FOR_SIGNATURE + " if at least one module is consented. "
							+ consentDTO.getKey();
					LOGGER.warn(message);
					throw new MissingRequiredObjectException(message);
				}
			}

			// in any case assert that with a given signature date that it is not before MINDATE_FOR_SIGNATURE
			if (consentDTO.getPatientSigningDate() != null && consentDTO.getPatientSigningDate().before(MINDATE_FOR_SIGNATURE)
					|| consentDTO.getPhysicianSigningDate() != null && consentDTO.getPhysicianSigningDate().before(MINDATE_FOR_SIGNATURE))
			{
				String message = "a given signature date need to be after " + MINDATE_FOR_SIGNATURE + ". "
						+ consentDTO.getKey();
				LOGGER.warn(message);
				throw new InvalidParameterException(message);
			}

			for (ModuleKeyDTO moduleKeyDTO : consentDTO.getModuleStates().keySet())
			{
				if (!validModulesForTemplate.contains(moduleKeyDTO))
				{
					String message = "the given " + moduleKeyDTO + " is not part of the given " + consentKeyDTO.getConsentTemplateKey();
					LOGGER.warn(message);
					throw new UnknownModuleException(message);
				}
				ModuleKey moduleKey = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domain.getName()), moduleKeyDTO);
				result.put(moduleKeyDTO, getModule(moduleKey));
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentDTO refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, String notificationClientID)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, InvalidParameterException, UnknownSignerIdTypeException, InternalException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(ctKeyDTO.getDomainName());
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), ctKeyDTO));
			VirtualPerson virtualPerson = getOrCreateVirtualPersonFor(domain, signerIdDTOs);
			ConsentKey consentKey = new ConsentKey(consentTemplate.getKey(), new Date(), virtualPerson.getId());
			if (em.find(Consent.class, consentKey) != null)
			{
				String message = consentKey + " already exists - should not have happened! - consent was generated";
				LOGGER.error(message);
				throw new InternalException(message);
			}

			ConsentKeyDTO consentKeyDTO = consentKey.toDTO(VersionConverterCache.getCTVersionConverter(domain.getName()), virtualPerson);

			Map<ModuleKeyDTO, Module> modules = new HashMap<>();
			List<ModuleConsentTemplate> mcts = consentTemplate.getModuleConsentTemplates();
			ConsentDTO tempConsentDTO = new ConsentDTO(consentKeyDTO);
			for (ModuleConsentTemplate mct : mcts)
			{
				ModuleKeyDTO moduleKeyDTO = mct.getKey().getModuleKey().toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName()));
				modules.put(moduleKeyDTO, mct.getModule());
				// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
				tempConsentDTO.getModuleStates().put(moduleKeyDTO,
						new ModuleStateDTO(moduleKeyDTO, ConsentStatus.REFUSED, new ArrayList<>()));
			}
			for (FreeTextDef freeTextDef : consentTemplate.getFreeTextDefs())
			{
				if (freeTextDef.getRequired())
				{
					tempConsentDTO.getFreeTextVals().add(new FreeTextValDTO(freeTextDef.getKey().getName(), "dummy for refused consent", "no fhir-id"));
				}
			}
			try
			{
				Consent newConsent = new Consent(consentTemplate, tempConsentDTO, modules, virtualPerson, domain.getPropertiesObject());
				em.persist(newConsent);
				ConsentCache.addConsent(virtualPerson.getId(), convertToSignerIdKeys(domain, signerIdDTOs), getSignedPoliciesWithExpirationDates(newConsent.getSignedPolicies()), newConsent);
				return newConsent.toDTO();
			}
			catch (MissingRequiredObjectException | InvalidFreeTextException | InconsistentStatusException impossible)
			{
				String message = "exception should not have happened! - consent was generated";
				LOGGER.error(message, impossible);
				throw new InternalException(message, impossible);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException, InternalException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Consent consent = getConsentObject(consentKeyDTO);
			addSignerIdToDBConsent(consent, signerIdDTO);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ConsentDateValuesDTO getConsentDates(ConsentKeyDTO consentKeyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException
	{
		ConsentDateValuesDTO result;
		EM_RWL.readLock().lock();
		try
		{
			result = getConsentObject(consentKeyDTO).getConsentDatesDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdTypeException, InternalException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomain(domain);
			Set<VirtualPerson> virtualPersons = getAllVirtualPersonsWithAtLeastOneSignerId(domainObject, convertToSignerIdKeys(domainObject, Collections.singleton(existentSignerIdDTO)), false);
			try
			{
				for (VirtualPerson virtualPerson : virtualPersons)
				{
					List<Consent> consents = getConsentsForPerson(domainObject, virtualPerson);
					for (Consent consent : consents)
					{
						addSignerIdToDBConsent(consent, newSignerIdDTO);
					}
				}
			}
			catch (UnknownSignerIdTypeException | InvalidVersionException | UnknownConsentTemplateException e)
			{
				String message = "unexpected exception: " + e.getMessage();
				LOGGER.error(message);
				throw new InternalException(message);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId) throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerId signerId1 = getSignerId(getSignerIdType(domain, originalSignerId.getIdType()), originalSignerId.getId());
			SignerId signerId2;
			try
			{
				signerId2 = getSignerId(getSignerIdType(domain, aliasSignerId.getIdType()), aliasSignerId.getId());
			}
			catch (UnknownSignerIdException expected)
			{
				signerId2 = new SignerId(getSignerIdType(domain, aliasSignerId.getIdType()), aliasSignerId.getId());
				em.persist(signerId2);
				em.flush();
			}
			Alias alias;
			try
			{
				alias = getAlias(signerId1.getKey(), signerId2.getKey());
			}
			catch (UnknownAliasException expected)
			{
				alias = new Alias(signerId1, signerId2);
				em.persist(alias);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deactivateAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws UnknownAliasException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerIdKey signerIdKey1 = new SignerIdKey(getSignerIdType(domain, originalSignerId.getIdType()).getKey(), originalSignerId.getId());
			SignerIdKey signerIdKey2 = new SignerIdKey(getSignerIdType(domain, aliasSignerId.getIdType()).getKey(), aliasSignerId.getId());
			Alias alias = getAlias(signerIdKey1, signerIdKey2);
			alias.setDeactivateTimestamp(new Timestamp(System.currentTimeMillis()));
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignerIdDTO> listAliases(String domainName, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		List<SignerIdDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			result.addAll(listAliases(domain, signerIdDTO));
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<SignerIdDTO, SignerIdDTO[]> mapAliases(String domainName, List<SignerIdDTO> originalSignerIds)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			Map<SignerIdDTO, SignerIdDTO[]> result = new HashMap<>();
			for (SignerIdDTO sidDTO : originalSignerIds)
			{
				List<SignerIdDTO> aliases = listAliases(domain, sidDTO);
				result.put(sidDTO, aliases.toArray(new SignerIdDTO[aliases.size()]));
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<SignerIdDTO> listAliases(Domain domain, SignerIdDTO signerIdDTO) throws UnknownSignerIdException, UnknownSignerIdTypeException
	{
		List<SignerIdDTO> result = new ArrayList<>();
		SignerId signerId = getSignerId(getSignerIdType(domain, signerIdDTO.getIdType()), signerIdDTO.getId());
		List<Alias> aliases = getAliases(signerId);
		for (Alias alias : aliases)
		{
			SignerId alisId = getSignerId(alias.getKey().getAliasSignerIdKey());
			result.add(alisId.toDTO());
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void setQcForConsent(ConsentKeyDTO consentKeyDTO, QCDTO qcDTO, String notificationClientID)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Consent consent = getConsentObject(consentKeyDTO);
			QC oldQC = consent.getQc();
			boolean oldQcPassed = oldQC.isQcPassed();
			String domainName = consentKeyDTO.getConsentTemplateKey().getDomainName();
			Domain domainObject = getDomain(domainName);
			consent.updateQC(qcDTO, domainObject.getPropertiesObject());
			if (oldQcPassed != consent.getQc().isQcPassed())
			{
				if (consent.getQc().isQcPassed())
				{
					ConsentCache.addConsent(consent.getVirtualPerson().getId(), convertToSignerIdKeys(domainObject, consentKeyDTO.getSignerIds()),
							getSignedPoliciesWithExpirationDates(consent.getSignedPolicies()), consent);
				}
				else
				{
					ConsentCache.removeConsent(consent, convertToSignerIdKeys(domainObject, consentKeyDTO.getSignerIds()),
							getSignedPoliciesWithExpirationDates(consent.getSignedPolicies()), consentKeyDTO.getConsentTemplateKey().getDomainName());
				}
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<QCHistoryDTO> getQCHistoryForConsent(ConsentKeyDTO consentKeyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentException
	{
		List<QCHistoryDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(consentKeyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate ct = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			VirtualPerson vp = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, consentKeyDTO.getSignerIds()));
			ConsentKey consentKey = new ConsentKey(ct.getKey(), consentKeyDTO.getConsentDate(), vp.getId());
			CriteriaBuilder criteriaBuilderTmp = em.getCriteriaBuilder();
			CriteriaQuery<QCHist> query = criteriaBuilderTmp.createQuery(QCHist.class);
			Root<QCHist> root = query.from(QCHist.class);

			Predicate predicateKey = criteriaBuilderTmp.equal(root.get(QCHist_.key).get(QCHistKey_.consent), consentKey);

			query.select(root).where(predicateKey);
			List<QCHist> qcHists = em.createQuery(query).getResultList();
			for (QCHist qcHist : qcHists)
			{
				result.add(qcHist.toDTO());
			}
		}
		catch (ObjectNotFoundException e)
		{
			String message = consentKeyDTO + " not found";
			LOGGER.warn(message);
			throw new UnknownConsentException(message);
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SignerIdDTO> listSignerIdsForAlias(String domainName, SignerIdDTO aliasSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		List<SignerIdDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			SignerId aliasSignerId = getSignerId(getSignerIdType(domain, aliasSignerIdDTO.getIdType()), aliasSignerIdDTO.getId());
			List<Alias> aliases = getAliasesForAlias(aliasSignerId);
			for (Alias alias : aliases)
			{
				SignerId signerId = getSignerId(alias.getKey().getOrigSignerIdKey());
				result.add(signerId.toDTO());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	private ConsentTemplateKey createConsentTemplateKey(ConsentTemplateKeyDTO ctKeyDTO)
			throws InvalidVersionException, UnknownDomainException
	{
		return new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(ctKeyDTO.getDomainName()), ctKeyDTO);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Set<ConsentTemplateKeyDTO> getTemplatesWithPolicies(Set<PolicyKeyDTO> policyKeyDTOs)
			throws InvalidParameterException, UnknownDomainException, InvalidVersionException
	{
		if (policyKeyDTOs.isEmpty())
		{
			return Set.of();
		}

		Set<String> domainNames = policyKeyDTOs.stream().map(PolicyKeyDTO::getDomainName).collect(Collectors.toSet());

		if (domainNames.size() > 1)
		{
			throw new InvalidParameterException("Consent policies must not relate to different domains: " + domainNames);
		}

		String domainName = domainNames.iterator().next();

		EM_RWL.readLock().lock();
		try
		{
			HashSet<ConsentTemplateKeyDTO> ctDTOsWithPolicies = new HashSet<>();
			List<ConsentTemplateDTO> domainTemplates = OrgDatCache.listConsentTemplatesFIUO(domainName, false);

			for (ConsentTemplateDTO ctDTO : domainTemplates)
			{
				boolean found = false;

				for (AssignedModuleDTO amDTO : ctDTO.getAssignedModules())
				{
					for (AssignedPolicyDTO apDTO : amDTO.getModule().getAssignedPolicies())
					{
						if (policyKeyDTOs.contains(apDTO.getPolicy().getKey()))
						{
							ctDTOsWithPolicies.add(ctDTO.getKey());
							found = true;
							break;
						}
					}
					if (found)
					{
						break;
					}
				}
			}

			return ctDTOsWithPolicies;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	// --------------------------------------- mapped templates ----------------------------------------

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Set<ConsentTemplateKeyDTO> getMappedTemplatesForSignerId(String domainName, ConsentTemplateType ctType, SignerIdDTO signerId, boolean useAliases)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.readLock().lock();
		try
		{
			Set<ConsentTemplateKeyDTO> mappedCtKeyDTOs = new HashSet<>();
			Set<ConsentTemplateKeyDTO> ctKeyDTOs = getAllConsentsForSignerIds(domainName, Set.of(signerId), useAliases).stream()
					.map(c -> c.getKey().getConsentTemplateKey())
					.filter(ct -> domainName.equals(ct.getDomainName())).collect(Collectors.toSet());

			for (ConsentTemplateKeyDTO ctKeyDTO : ctKeyDTOs)
			{
				mappedCtKeyDTOs.addAll(OrgDatCache.getConsentTemplateFIUO(ctKeyDTO).getMappedTemplates(ctType));
			}

			return mappedCtKeyDTOs;
		}
		catch (InconsistentStatusException | UnknownConsentTemplateException | InvalidVersionException e)
		{
			throw new UnknownSignerIdException(e);
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	// --------------------------------------- fhir ----------------------------------------

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T extends FhirIdDTO, U extends FhirDTOExporter<T>> U getDTOByFhirID(Class<U> clazz, String fhirID) throws UnknownIDException
	{
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(clazz);
			Root<U> root = criteriaQuery.from(clazz);
			ParameterExpression<String> p = criteriaBuilder.parameter(String.class);
			criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("fhirID"), p));
			try
			{
				return em.createQuery(criteriaQuery).setParameter(p, fhirID).getSingleResult();
			}
			catch (NoResultException maybe)
			{
				throw new UnknownIDException("no " + clazz.getSimpleName() + " found for fhirID " + fhirID);
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	// --------------------------------------- stats ----------------------------------------

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public StatisticDTO getLatestStats()
	{
		EM_RWL.readLock().lock();
		StatisticDTO result;
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Statistic> criteriaQuery = criteriaBuilder.createQuery(Statistic.class);
			Root<Statistic> root = criteriaQuery.from(Statistic.class);
			criteriaQuery.select(root).orderBy(criteriaBuilder.desc(root.get(Statistic_.stat_entry_id)));
			Statistic stat = em.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
			result = stat.toDTO();
		}
		catch (NoResultException maybe)
		{
			result = new StatisticDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<StatisticDTO> getAllStats()
	{
		EM_RWL.readLock().lock();
		try
		{
			List<StatisticDTO> result = new ArrayList<>();
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Statistic> criteriaQuery = criteriaBuilder.createQuery(Statistic.class);
			Root<Statistic> root = criteriaQuery.from(Statistic.class);
			criteriaQuery.select(root);
			List<Statistic> queryResult = em.createQuery(criteriaQuery).getResultList();

			for (Statistic stats : queryResult)
			{
				result.add(stats.toDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public StatisticDTO updateStats() throws StatisticException
	{
		Instant start = Instant.now();
		Date now = Date.from(start);

		StatisticDTO result = new StatisticDTO();
		Map<String, Long> stat = result.getMappedStatValue();

		EM_RWL.readLock().lock();
		try
		{
			// TODO Remove entries that are not required in stat history

			// Count domains
			List<DomainDTO> domains = OrgDatCache.listDomainsFIUO(false);
			stat.put(StatisticKeys.DOMAINS, (long) domains.size());

			// Pepare count all modules without version
			List<ModuleDTO> allModules = new ArrayList<>();

			// Check consent config
			CheckConsentConfig checkConsentConfig = new CheckConsentConfig(CheckConsentConfig.IdMatchingType.AT_LEAST_ONE, false, true);

			// Count for each domain
			for (DomainDTO domainDTO : domains)
			{
				// Pagination config
				PaginationConfig paginationConfig = new PaginationConfig();
				Map<ConsentField, String> filter = new EnumMap<>(ConsentField.class);

				// Consents, Revocations, Refusals
				paginationConfig.setTemplateType(ConsentTemplateType.CONSENT);
				long domainConsents = countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig);
				paginationConfig.setTemplateType(ConsentTemplateType.REVOCATION);
				long domainRevocations = countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig);
				paginationConfig.setTemplateType(ConsentTemplateType.REFUSAL);
				long domainRefusals = countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig);

				stat.put(StatisticKeys.CONSENTS + StatisticKeys.PER_DOMAIN + domainDTO.getName(), domainConsents);
				stat.put(StatisticKeys.REVOCATIONS + StatisticKeys.PER_DOMAIN + domainDTO.getName(), domainRevocations);
				stat.put(StatisticKeys.REFUSALS + StatisticKeys.PER_DOMAIN + domainDTO.getName(), domainRefusals);
				stat.put(StatisticKeys.CONSENTS, stat.getOrDefault(StatisticKeys.CONSENTS, 0L) + domainConsents);
				stat.put(StatisticKeys.REVOCATIONS, stat.getOrDefault(StatisticKeys.REVOCATIONS, 0L) + domainRevocations);
				stat.put(StatisticKeys.REFUSALS, stat.getOrDefault(StatisticKeys.REFUSALS, 0L) + domainRefusals);

				// Quality Control
				paginationConfig.setTemplateType(null);
				long qcValidDocuments = 0L;
				long qcInvalidDocuments = 0L;
				for (String type : domainDTO.getValidQcTypes())
				{
					filter.put(ConsentField.QC_TYPE, type);
					paginationConfig.setFilter(filter);
					long qcTypeDocuments = countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig);
					stat.put(new StatisticKeys(StatisticKeys.QC).perDomain(domainDTO.getName()).perQcType(type).build(), qcTypeDocuments);
					qcValidDocuments += qcTypeDocuments;
				}
				for (String type : domainDTO.getInvalidQcTypes())
				{
					filter.put(ConsentField.QC_TYPE, type);
					paginationConfig.setFilter(filter);
					long qcTypeDocuments = countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig);
					stat.put(new StatisticKeys(StatisticKeys.QC).perDomain(domainDTO.getName()).perQcType(type).build(), qcTypeDocuments);
					qcInvalidDocuments += qcTypeDocuments;
				}
				stat.put(new StatisticKeys(StatisticKeys.QC_VALID).perDomain(domainDTO.getName()).build(), qcValidDocuments);
				stat.put(new StatisticKeys(StatisticKeys.QC_INVALID).perDomain(domainDTO.getName()).build(), qcInvalidDocuments);

				// Templates, Modules, Policies
				List<ConsentTemplateDTO> domainTemplates = OrgDatCache.listConsentTemplatesFIUO(domainDTO.getName(), false);
				List<ModuleDTO> domainModules = OrgDatCache.listModulesFIUO(domainDTO.getName(), false);
				allModules.addAll(domainModules);
				List<PolicyDTO> domainPolicies = OrgDatCache.listPoliciesFIUO(domainDTO.getName(), false);

				stat.put(StatisticKeys.TEMPLATES + StatisticKeys.PER_DOMAIN + domainDTO.getName(), (long) domainTemplates.size());
				stat.put(StatisticKeys.MODULES + StatisticKeys.PER_DOMAIN + domainDTO.getName(), (long) domainModules.size());
				stat.put(StatisticKeys.POLICIES + StatisticKeys.PER_DOMAIN + domainDTO.getName(), (long) domainPolicies.size());
				stat.put(StatisticKeys.TEMPLATES, stat.getOrDefault(StatisticKeys.TEMPLATES, 0L) + domainTemplates.size());
				stat.put(StatisticKeys.MODULES, stat.getOrDefault(StatisticKeys.MODULES, 0L) + domainModules.size());
				stat.put(StatisticKeys.POLICIES, stat.getOrDefault(StatisticKeys.POLICIES, 0L) + domainPolicies.size());

				// Documents by template
				filter.clear();
				for (ConsentTemplateDTO consentTemplateDTO : domainTemplates)
				{
					if (consentTemplateDTO.getFinalised())
					{
						filter.put(ConsentField.CT_NAME, consentTemplateDTO.getKey().getName());
						filter.put(ConsentField.CT_VERSION, consentTemplateDTO.getKey().getVersion());
						paginationConfig.setFilter(filter);
						stat.put(new StatisticKeys(StatisticKeys.DOCUMENTS).perDomain(domainDTO.getName()).perTemplate(consentTemplateDTO.getKey()).build(),
								countConsentsForDomainWithFilter(domainDTO.getName(), paginationConfig));
					}
				}

				// Signer Id Types
				for (String signerIdType : domainDTO.getSignerIdTypes())
				{
					List<String> signerIds = getAllIdsForSignerIdType(domainDTO.getName(), signerIdType);
					stat.put(StatisticKeys.SIGNER_IDS + StatisticKeys.PER_DOMAIN + domainDTO.getName() + StatisticKeys.PER_IDTYPE + signerIdType, (long) signerIds.size());

					if (Boolean.parseBoolean(domainDTO.getProperty(DomainProperties.STATISTIC_POLICY_DETAILS.name())))
					{
						// Prepare map with all policies and count of ConsentStatusType
						Map<PolicyKeyDTO, Map<ConsentStatusType, Long>> policies = new HashMap<>();
						for (PolicyDTO policy : OrgDatCache.listPoliciesFIUO(domainDTO.getName(), true))
						{
							Map<ConsentStatusType, Long> types = new EnumMap<>(ConsentStatusType.class);
							for (ConsentStatusType type : ConsentStatusType.values())
							{
								types.put(type, 0L);
							}
							policies.put(policy.getKey(), types);
						}

						// Signer Ids
						for (String signerId : signerIds)
						{
							Set<SignerIdDTO> signerIdSet = new HashSet<>();
							signerIdSet.add(new SignerIdDTO(signerIdType, signerId, now, null));

							// Policies
							for (Entry<PolicyKeyDTO, Map<ConsentStatusType, Long>> policy : policies.entrySet())
							{
								ConsentStatusType type = getConsentStatusType(signerIdSet, policy.getKey(), checkConsentConfig);
								policy.getValue().put(type, policy.getValue().get(type) + 1);
							}
						}

						// Save result
						for (Entry<PolicyKeyDTO, Map<ConsentStatusType, Long>> policy : policies.entrySet())
						{
							for (Entry<ConsentStatusType, Long> type : policy.getValue().entrySet())
							{
								stat.put(new StatisticKeys(StatisticKeys.SIGNER_IDS)
										.perDomain(domainDTO.getName())
										.perIdType(signerIdType)
										.perPolicy(policy.getKey())
										.perStatus(type.getKey())
										.build(), type.getValue());
							}
						}
					}
				}

				// Document details
				long documentsWithDigitalSignature = 0L;
				long documentsExpired = 0L;
				if (Boolean.parseBoolean(domainDTO.getProperty(DomainProperties.STATISTIC_DOCUMENT_DETAILS.name())))
				{
					for (ConsentLightDTO consentLightDTO : getAllConsentsForDomain(domainDTO.getName()))
					{
						// is expired
						if (consentLightDTO.getConsentDates().getConsentExpirationDate().before(now))
						{
							documentsExpired++;
						}

						// has digital signature
						if (consentLightDTO.getHasPatientSignature())
						{
							documentsWithDigitalSignature++;
						}
					}
					stat.put(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE).perDomain(domainDTO.getName()).build(), documentsWithDigitalSignature);
					stat.put(new StatisticKeys(StatisticKeys.DOCUMENTS_EXPIRED_FULL).perDomain(domainDTO.getName()).build(), documentsExpired);
				}
				stat.put(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_SCANS).perDomain(domainDTO.getName()).build(), countConsentWithScansForDomain(domainDTO.getName()));
			}

			// All modules ignoring version
			stat.put(StatisticKeys.MODULES_WITHOUT_VERSIONS, allModules.stream().map(m -> m.getKey().getDomainName() + m.getKey().getName()).distinct().count());

			// All documents with scans
			stat.put(StatisticKeys.DOCUMENTS_WITH_SCANS,
					domains.stream().mapToLong(d -> stat.getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_SCANS).perDomain(d.getName()).build(), 0L)).sum());

			// All documents with digital patient signature
			stat.put(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE,
					domains.stream().mapToLong(d -> stat.getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE).perDomain(d.getName()).build(), 0L)).sum());
		}
		catch (InvalidParameterException | UnknownDomainException | InvalidVersionException | UnknownSignerIdTypeException | UnknownPolicyException | InconsistentStatusException e)
		{
			throw new StatisticException(e);
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}

		Instant finish = Instant.now();
		result.getMappedStatValue().put(StatisticKeys.CALCULATION_TIME, Duration.between(start, finish).toMillis());

		addStat(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addStat(StatisticDTO statisticDTO)
	{
		EM_RWL.writeLock().lock();
		try
		{
			Statistic stat = new Statistic(statisticDTO);
			em.persist(stat);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}
}
