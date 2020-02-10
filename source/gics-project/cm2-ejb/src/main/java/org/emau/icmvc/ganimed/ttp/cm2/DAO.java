package org.emau.icmvc.ganimed.ttp.cm2;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jws.WebParam;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache.CachedSignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextDef;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdTypeKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Text_;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPersonSignerId;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * single point for db and cache access
 *
 * @author geidell
 *
 */
@Singleton
@Startup
public class DAO {

	private static final Logger logger = Logger.getLogger(DAO.class);
	private static final Date minDateForSignature = new Date(82860000); // 02.01.1970

	@PersistenceContext
	private EntityManager em;
	private static final ReentrantReadWriteLock emRWL = new ReentrantReadWriteLock();
	private static final ConsentCache consentCache = new ConsentCache();
	private static final VersionConverterCache vcc = new VersionConverterCache();

	public DAO() {
	}

	@PostConstruct
	private void checkConsentCache() {
		emRWL.readLock().lock();
		try {
			if (!consentCache.isInitialised()) {
				logger.info("initialise consent cache");
				consentCache.init(em);
				logger.info("consent cache initialised");
			}
			if (!vcc.isInitialised()) {
				logger.info("initialise version converter cache");
				try {
					vcc.init(listDomainObjects());
					logger.info("version converter cache initialised");
				} catch (VersionConverterClassException e) {
					String message = "exception while initialising version convert class cache " + e.getMessage();
					logger.fatal(message, e);
				}
			}
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException {
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(vcc.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			Map<ModuleKeyDTO, Module> modules = validateConsentReturningObjects(consentDTO, true);
			VirtualPerson virtualPerson = getOrCreateVirtualPersonFor(domain, consentDTO.getKey().getSignerIds());
			ConsentKey consentKey = new ConsentKey(consentTemplate, consentDTO.getKey().getConsentDate(), virtualPerson.getId());
			if (em.find(Consent.class, consentKey) != null) {
				String message = consentDTO.getKey() + " already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			Consent newConsent = new Consent(consentTemplate, consentDTO, modules, virtualPerson);
			em.persist(newConsent);
			// relationen zu bereits vorhanden objekten nach der persistierung setzen
			virtualPerson.getConsents().add(newConsent);
			consentCache.addConsent(virtualPerson.getId(), consentDTO.getKey().getSignerIds(), newConsent.getSignedPolicies());
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config) throws UnknownDomainException,
			UnknownPolicyException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusType(signerIdDTOs, policyKeyDTO, config));
	}

	public boolean isConsentedFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED
				.equals(getConsentStatusTypeFromIncludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config));
	}

	public boolean isConsentedFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED
				.equals(getConsentStatusTypeFromExcludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config));
	}

	public boolean isConsentedFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED
				.equals(getConsentStatusTypeFromIncludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config));
	}

	public boolean isConsentedFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED
				.equals(getConsentStatusTypeFromExcludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config));
	}

	public ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, 0, config);
	}

	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, 0, config);
	}

	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 1, versionTo, -1, config);
	}

	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, versionFrom, 0, versionTo, -1, config);
	}

	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws InvalidVersionException, VersionConverterClassException, UnknownDomainException, UnknownPolicyException,
			UnknownSignerIdTypeException {
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
			PolicyKey policyKey = new PolicyKey(vcc.getPolicyVersionConverter(domain.getName()), policyKeyDTO);
			getPolicy(policyKey);
			ConsentStatusType result = getConsentStatusTypeFromTo(signerIdDTOs, domain, policyKeyDTO.getName(), policyKey.getVersion(),
					policyKey.getVersion(), config);
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("get consent status type for ");
				sb.append(policyKeyDTO);
				sb.append(" signed by:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("using ");
				sb.append(config);
				sb.append(" result: ");
				sb.append(result);
				logger.info(sb.toString());
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listConsentTemplates(String domainName)
			throws VersionConverterClassException, InvalidVersionException, UnknownDomainException {
		emRWL.readLock().lock();
		try {
			Domain domainObject = getDomainObject(domainName);
			List<ConsentTemplate> consentTemplates = domainObject.getConsentTemplates();
			List<ConsentTemplateDTO> result = new ArrayList<ConsentTemplateDTO>();
			for (ConsentTemplate consentTemplate : consentTemplates) {
				result.add(consentTemplate.toDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " consent templates");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName)
			throws VersionConverterClassException, InvalidVersionException, UnknownDomainException {
		emRWL.readLock().lock();
		try {
			getDomainObject(domainName);
			// mehrere moegliche sql-queries fuer die aufgabe:
			// 1. select * from (select * from consent_template order by version desc) as x group by name;
			// 2. select t1.* from consent_template as ct1 left join consent_template as ct2 on (ct1.NAME = ct2.NAME and ct1.VERSION <
			// ct2.VERSION) where ct2.VERSION is null;
			// 3. select * from consent_template ct1 where ct1.version = (select max(version) from consent_template ct2 where ct1.NAME = ct2.NAME);
			// nur die 3. laesst sich momentan mit der criteria-api umsetzen
			// bei 1. haengt's daran, dass subqueries nicht nach from stehen duerfen
			// fuer 2. muesste man extra eine selbst-reference im consent_template-db-modell einbauen - nur ueber diese sind joins moeglich
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
			Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
			Subquery<Integer> subQuery = criteriaQuery.subquery(Integer.class);
			Root<ConsentTemplate> subRoot = subQuery.from(ConsentTemplate.class);
			Predicate subPredicate = criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name),
					subRoot.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name));
			subQuery.select(criteriaBuilder.max(subRoot.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version))).where(subPredicate);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName),
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version), subQuery));
			criteriaQuery.select(root).where(predicate);
			List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).getResultList();
			List<ConsentTemplateDTO> result = new ArrayList<ConsentTemplateDTO>();
			for (ConsentTemplate consentTemplate : ctList) {
				result.add(consentTemplate.toDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " consent templates");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO) throws InvalidVersionException, VersionConverterClassException,
			DuplicateEntryException, FreeTextConverterStringException, UnknownModuleException, InvalidPropertiesException, UnknownDomainException {
		emRWL.writeLock().lock();
		try {
			Domain domainObject = getDomainObject(consentTemplateDTO.getKey().getDomainName());
			ConsentTemplateKey key = new ConsentTemplateKey(vcc.getCTVersionConverter(domainObject.getName()), consentTemplateDTO.getKey());
			ConsentTemplate duplicateTest = em.find(ConsentTemplate.class, key);
			if (duplicateTest != null) {
				String message = key + " already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			Map<ModuleKeyDTO, Module> modules = new HashMap<ModuleKeyDTO, Module>();
			List<String> policyNames = new ArrayList<String>();
			List<String> moduleNames = new ArrayList<String>();
			for (AssignedModuleDTO assignedModuleDTO : consentTemplateDTO.getAssignedModules()) {
				ModuleKeyDTO moduleKeyDTO = assignedModuleDTO.getModule().getKey();
				String moduleName = moduleKeyDTO.getName();
				if (moduleNames.contains(moduleName)) {
					String message = "multiple occurences of module '" + moduleName + "' within " + consentTemplateDTO;
					logger.warn(message);
					throw new DuplicateEntryException(message);
				}
				moduleNames.add(moduleName);
				ModuleKey moduleKey = new ModuleKey(vcc.getModuleVersionConverter(domainObject.getName()), moduleKeyDTO);
				Module module = getModule(moduleKey);
				modules.put(moduleKeyDTO, module);
				for (Policy policy : module.getPolicies()) {
					String policyName = policy.getKey().getName();
					if (policyNames.contains(policyName)) {
						String message = "multiple occurences of policy '" + policyName + "' within " + consentTemplateDTO;
						logger.warn(message);
						throw new DuplicateEntryException(message);
					}
					policyNames.add(policyName);
				}
			}
			try {
				ConsentTemplate consentTemplate = new ConsentTemplate(domainObject, consentTemplateDTO, modules, vcc);
				em.persist(consentTemplate);
				// relationen nach persist
				domainObject.getConsentTemplates().add(consentTemplate);
			} catch (InvalidPropertiesException e) {
				String message = "invalid properties within " + consentTemplateDTO;
				logger.warn(message);
				throw new InvalidPropertiesException(message);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException, ObjectInUseException {
		emRWL.writeLock().lock();
		try {
			ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(vcc.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey), consentTemplate.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0) {
				String message = consentTemplate + " is in use and therefore can't be deleted";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(consentTemplate);
			// relationen werden in der @PreRemove in der entity aufgeraeumt
			em.flush();
			for (ModuleConsentTemplate moduleConsentTemplate : consentTemplate.getModuleConsentTemplates()) {
				moduleConsentTemplate.getModule().getModuleConsentTemplates().remove(moduleConsentTemplate);
			}
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum ende der transaction ein neues objekt mit gleichem key im cache
			ConsentTemplate duplicateTest = em.find(ConsentTemplate.class, consentTemplate.getKey());
			if (duplicateTest != null) {
				em.clear();
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public List<ModuleDTO> listModules(String domainName) throws VersionConverterClassException, InvalidVersionException, UnknownDomainException {
		emRWL.readLock().lock();
		try {
			Domain domainObject = getDomainObject(domainName);
			List<Module> modules = domainObject.getModules();
			List<ModuleDTO> result = new ArrayList<ModuleDTO>();
			for (Module module : modules) {
				result.add(module.toDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " modules");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void addModule(ModuleDTO moduleDTO)
			throws InvalidVersionException, VersionConverterClassException, DuplicateEntryException, UnknownDomainException, UnknownPolicyException {
		emRWL.writeLock().lock();
		try {
			Domain domainObject = getDomainObject(moduleDTO.getKey().getDomainName());
			ModuleKey key = new ModuleKey(vcc.getModuleVersionConverter(domainObject.getName()), moduleDTO.getKey());
			Module duplicateTest = em.find(Module.class, key);
			if (duplicateTest != null) {
				String message = key + " already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			List<String> policyNames = new ArrayList<String>();
			List<Policy> policies = new ArrayList<Policy>();
			for (PolicyDTO policyDTO : moduleDTO.getPolicies()) {
				String policyName = policyDTO.getKey().getName();
				if (policyNames.contains(policyName)) {
					String message = "multiple occurences of policy '" + policyName + "' within " + moduleDTO;
					logger.warn(message);
					throw new DuplicateEntryException(message);
				}
				PolicyKey policyKey = new PolicyKey(vcc.getPolicyVersionConverter(domainObject.getName()), policyDTO.getKey());
				Policy policy = getPolicy(policyKey);
				policyNames.add(policyName);
				policies.add(policy);
			}
			Module module = new Module(domainObject, moduleDTO, vcc);
			em.persist(module);
			// relationen nach persist
			domainObject.getModules().add(module);
			module.getPolicies().addAll(policies);
			for (Policy policy : policies) {
				policy.getModules().add(module);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void deleteModule(ModuleKeyDTO keyDTO)
			throws InvalidVersionException, VersionConverterClassException, ObjectInUseException, UnknownDomainException, UnknownModuleException {
		emRWL.writeLock().lock();
		try {
			Module module = getModule(new ModuleKey(vcc.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			if (module.getModuleConsentTemplates().size() > 0) {
				String message = module + " is in use and therefore can't be deleted";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(module);
			// relationen werden in der @PreRemove in der entity aufgeraeumt
			em.flush();
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum ende der transaction ein neues objekt mit gleichem key im cache
			Module duplicateTest = em.find(Module.class, module.getKey());
			if (duplicateTest != null) {
				em.clear();
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, VersionConverterClassException, InvalidVersionException {
		emRWL.readLock().lock();
		try {
			ModuleKey moduleKey = new ModuleKey(vcc.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO);
			return getModule(moduleKey).toDTO(vcc);
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<PolicyDTO> listPolicies(String domainName) throws VersionConverterClassException, InvalidVersionException, UnknownDomainException {
		emRWL.readLock().lock();
		try {
			Domain domainObject = getDomainObject(domainName);
			List<Policy> policies = domainObject.getPolicies();
			List<PolicyDTO> result = new ArrayList<PolicyDTO>();
			for (Policy policy : policies) {
				result.add(policy.toDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " policies");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void addPolicy(PolicyDTO policyDTO)
			throws InvalidVersionException, VersionConverterClassException, DuplicateEntryException, UnknownDomainException {
		emRWL.writeLock().lock();
		try {
			Domain domainObject = getDomainObject(policyDTO.getKey().getDomainName());
			PolicyKey key = new PolicyKey(vcc.getPolicyVersionConverter(domainObject.getName()), policyDTO.getKey());
			Policy duplicateTest = em.find(Policy.class, key);
			if (duplicateTest != null) {
				String message = key + " already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			Policy policy = new Policy(domainObject, policyDTO, vcc);
			em.persist(policy);
			domainObject.getPolicies().add(policy);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void deletePolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException, ObjectInUseException {
		emRWL.writeLock().lock();
		try {
			Policy policy = getPolicy(new PolicyKey(vcc.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<SignedPolicy> root = criteriaQuery.from(SignedPolicy.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignedPolicy_.key).get(SignedPolicyKey_.policyKey), policy.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0 || policy.getModules().size() > 0) {
				String message = policy + " is in use and therefore can't be deleted";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(policy);
			// relationen werden in der @PreRemove in der entity aufgeraeumt
			em.flush();
			// wahrscheinlich ein bug in eclipselink, manchmal ist nach dem remove bis zum ende der transaction ein neues objekt mit gleichem key im cache
			Policy duplicateTest = em.find(Policy.class, policy.getKey());
			if (duplicateTest != null) {
				em.clear();
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException {
		emRWL.readLock().lock();
		try {
			Policy result = getPolicy(new PolicyKey(vcc.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			return result.toDTO(vcc);
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException {
		emRWL.readLock().lock();
		try {
			ConsentTemplate result = getConsentTemplate(new ConsentTemplateKey(vcc.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
			return result.toDTO(vcc);
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<SignedPolicyDTO> getPolicyStatesForSigner(String domainName, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, VersionConverterClassException, UnknownDomainException, UnknownSignerIdTypeException {
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
			for (Consent consent : consents) {
				ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(vcc.getCTVersionConverter(domainName), consent.getVirtualPerson());
				for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
					PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(vcc.getPolicyVersionConverter(domainName));
					SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), policyKeyDTO, consentKeyDTO);
					result.add(signedPolicyDTO);
				}
			}
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				logger.debug(sb.toString());
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSigner(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, VersionConverterClassException, UnknownDomainException, UnknownSignerIdTypeException {
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
			List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
			for (Consent consent : consents) {
				ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(vcc.getCTVersionConverter(domain.getName()), consent.getVirtualPerson());
				for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
					PolicyKeyDTO currentPolicyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(vcc.getPolicyVersionConverter(domain.getName()));
					if (currentPolicyKeyDTO.equals(policyKeyDTO)) {
						SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), currentPolicyKeyDTO, consentKeyDTO);
						result.add(signedPolicyDTO);
					}
				}
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSigner(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, VersionConverterClassException, UnknownDomainException, UnknownSignerIdTypeException {
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
			for (Consent consent : consents) {
				ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(vcc.getCTVersionConverter(domainName), consent.getVirtualPerson());
				for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
					if (signedPolicy.getKey().getPolicyKey().getName().equals(policyName)) {
						PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(vcc.getPolicyVersionConverter(domainName));
						SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), policyKeyDTO, consentKeyDTO);
						result.add(signedPolicyDTO);
					}
				}
			}
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("for policy name '" + policyName + "' within " + domain);
				logger.info(sb.toString());
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException {
		emRWL.readLock().lock();
		try {
			getDomainObject(domainName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
			Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name), consentTemplateName),
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName));
			criteriaQuery.select(root).where(predicate)
					.orderBy(criteriaBuilder.desc(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version)));
			List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
			if (ctList.isEmpty()) {
				String message = "unknown consent template with name '" + consentTemplateName + "' for domain '" + domainName + "'";
				logger.error(message);
				throw new UnknownConsentTemplateException(message);
			}
			ConsentTemplateDTO result = ctList.get(0).toDTO(vcc);
			if (logger.isInfoEnabled()) {
				logger.info("newest consent template found: " + result);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForPerson(String domainName, Set<SignerIdDTO> signerIdDTOs) throws UnknownDomainException,
			UnknownSignerIdTypeException, InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		List<ConsentLightDTO> result = new ArrayList<ConsentLightDTO>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
			for (Consent consent : consents) {
				result.add(consent.toLightDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("found " + result.size() + " consents for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				logger.info(sb.toString());
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public ConsentDTO getCurrentConsentForPersonAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException,
			UnknownSignerIdTypeException {
		ConsentDTO result = null;
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(ctKeyDTO.getDomainName());
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Set<VirtualPerson> signers = getVirtualPersons(domain, signerIdDTOs);
			VersionConverter ctVC = vcc.getCTVersionConverter(domain.getName());
			// TODO max date schon in die abfrage mit reinnehmen - die max-funktion der criteria-api kann aber nur number
			// Subquery<Date> subQuery = criteriaQuery.subquery(Date.class);
			// Root<Consent> subRoot = subQuery.from(Consent.class);
			// Predicate subPredicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name),
			// subRoot.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name));
			// // +domain +version
			// subQuery.select(criteriaBuilder.max(subRoot.get(Consent_.key).get(ConsentKey_.consentDate))).where(subPredicate);
			Consent temp = null;
			for (VirtualPerson signer : signers) {
				Predicate predicate = criteriaBuilder.and(
						criteriaBuilder.and(criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), signer.getId()),
								criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName),
										ctKeyDTO.getDomainName())),
						criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), ctKeyDTO.getName()));
				if (!ignoreVersionNumber) {
					predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
							root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), ctVC.stringToInt(ctKeyDTO.getVersion())));
				}
				criteriaQuery.select(root).where(predicate);
				List<Consent> consentList = em.createQuery(criteriaQuery).getResultList();
				for (Consent consent : consentList) {
					if (temp == null || consent.getKey().getConsentDate().after(temp.getKey().getConsentDate())) {
						temp = consent;
					}
				}
			}
			if (temp != null) {
				result = temp.toDTO(vcc);
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		List<ConsentLightDTO> result = new ArrayList<ConsentLightDTO>();
		emRWL.readLock().lock();
		try {
			List<Consent> consents = getConsentsForConsentTemplate(
					new ConsentTemplateKey(vcc.getCTVersionConverter(ctKeyDTO.getDomainName()), ctKeyDTO));
			for (Consent consent : consents) {
				result.add(consent.toLightDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " consents for consent template " + ctKeyDTO);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName)
			throws UnknownDomainException, InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		List<ConsentLightDTO> result = new ArrayList<ConsentLightDTO>();
		emRWL.readLock().lock();
		try {
			getDomainObject(domainName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName),
					domainName);
			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

			for (Consent consent : consents) {
				result.add(consent.toLightDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " consents for domain '" + domainName + "'");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		List<ConsentLightDTO> result = new ArrayList<ConsentLightDTO>();
		emRWL.readLock().lock();
		try {
			getDomainObject(domainName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domainName),
					criteriaBuilder.or(criteriaBuilder.isNull(root.get(Consent_.scanBase64).get(Text_.text)),
							criteriaBuilder.equal(root.get(Consent_.scanBase64).get(Text_.text), "")));
			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

			for (Consent consent : consents) {
				result.add(consent.toLightDTO(vcc));
			}
			if (logger.isInfoEnabled()) {
				logger.info("found " + result.size() + " consents for domain '" + domainName + "' which doesn't have a scan attached");
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, int firstEntry, int pageSize, ConsentField sortField,
			boolean sortIsAscending)
			throws UnknownDomainException, InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		List<ConsentLightDTO> result = new ArrayList<ConsentLightDTO>();
		emRWL.readLock().lock();
		try {
			getDomainObject(domainName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName),
					domainName);
			criteriaQuery.select(root).where(predicate);
			Expression<?> order = null;
			switch (sortField) {
				case DATE:
					order = root.get(Consent_.key).get(ConsentKey_.consentDate);
				break;
				case VERSION:
					order = root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version);
				break;
				case CT_NAME:
					order = root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name);
				break;
				case NONE:
				break;
				default:
					logger.warn("unimplemented ConsentField '" + sortField.name() + "' for order-by-clause within getConsentsForDomainPaginated()");
				break;
			}
			if (order != null) {
				if (sortIsAscending) {
					criteriaQuery.orderBy(criteriaBuilder.asc(order));
				} else {
					criteriaQuery.orderBy(criteriaBuilder.desc(order));
				}
			}
			List<Consent> consents = em.createQuery(criteriaQuery).setFirstResult(firstEntry).setMaxResults(pageSize).getResultList();

			for (Consent consent : consents) {
				result.add(consent.toLightDTO(vcc));
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException {
		return getConsentObject(keyDTO).toDTO(vcc);
	}

	private Consent getConsentObject(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException, UnknownConsentException {
		if (logger.isDebugEnabled()) {
			logger.debug("get " + keyDTO);
		}
		Consent result;
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(keyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate ct = getConsentTemplate(
					new ConsentTemplateKey(vcc.getCTVersionConverter(domain.getName()), keyDTO.getConsentTemplateKey()));
			try {
				VirtualPerson vp = findVirtualPerson(domain, keyDTO.getSignerIds());
				ConsentKey consentKey = new ConsentKey(ct, keyDTO.getConsentDate(), vp.getId());
				if (logger.isDebugEnabled()) {
					logger.debug("search " + consentKey);
				}
				result = em.find(Consent.class, consentKey);
			} catch (ObjectNotFoundException e) {
				String message = keyDTO + " not found";
				logger.warn(message);
				throw new UnknownConsentException(message);
			}
			if (result == null) {
				String message = keyDTO + " not found";
				logger.warn(message);
				throw new UnknownConsentException(message);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException {
		emRWL.writeLock().lock();
		try {
			Domain duplicateTest = em.find(Domain.class, domainDTO.getName());
			if (duplicateTest != null) {
				String message = "domain with name '" + domainDTO.getName() + "' already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			if (domainDTO.getLabel() == null || domainDTO.getLabel().isEmpty()) {
				if (logger.isInfoEnabled()) {
					logger.info("no label is set for new domain '" + domainDTO.getName() + "' - set it to the name of the domain");
				}
				domainDTO.setLabel(domainDTO.getName());
			}
			Domain domain = new Domain(domainDTO);
			vcc.addDomain(domain);
			em.persist(domain);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void deleteDomain(String domainName) throws UnknownDomainException, ObjectInUseException {
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			if (domain.getPolicies().size() > 0 || domain.getModules().size() > 0 || domain.getConsentTemplates().size() > 0) {
				String message = "domain with name '" + domainName + "' can't be deleted - it there are " + domain.getPolicies().size()
						+ " policies, " + domain.getModules().size() + " modules and " + domain.getConsentTemplates().size()
						+ " consent templates connected to it";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(domain);
			em.flush();
			vcc.removeDomain(domainName);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public DomainDTO getDomain(String domainName) throws UnknownDomainException {
		DomainDTO result = getDomainObject(domainName).toDTO();
		if (logger.isInfoEnabled()) {
			logger.info("found domain " + result);
		}
		return result;
	}

	public List<DomainDTO> listDomains() {
		List<DomainDTO> result = new ArrayList<DomainDTO>();
		emRWL.readLock().lock();
		try {
			List<Domain> domains = listDomainObjects();
			for (Domain domain : domains) {
				result.add(domain.toDTO());
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	private List<Domain> listDomainObjects() {
		emRWL.readLock().lock();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Domain> criteriaQuery = criteriaBuilder.createQuery(Domain.class);
			Root<Domain> root = criteriaQuery.from(Domain.class);
			criteriaQuery.select(root);
			List<Domain> result = em.createQuery(criteriaQuery).getResultList();
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public void updateDomain(String domainName, String label, String logo, String externProperties, String comment) throws UnknownDomainException {
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			domain.setLabel(label);
			domain.setLogo(logo);
			domain.setExternProperties(externProperties);
			domain.setComment(comment);
			em.persist(domain);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void updateConsentTemplate(ConsentTemplateKeyDTO keyDTO, String externProperties, String comment, String scanBase64, String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException {
		emRWL.writeLock().lock();
		try {
			ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(vcc.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
			consentTemplate.setComment(comment);
			consentTemplate.setExternProperties(externProperties);
			// TODO alten nicht ueberschreiben - mehrere scans zulassen?
			consentTemplate.setScanBase64(scanBase64, scanFileType);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	/**
	 * update policy
	 * 
	 * @author bialkem
	 * @param keyDTO
	 * @param externProperties
	 * @param comment
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 */
	public void updatePolicy(PolicyKeyDTO keyDTO, String externProperties, String comment)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, VersionConverterClassException {
		emRWL.writeLock().lock();
		try {
			Policy policy = getPolicy(new PolicyKey(vcc.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			policy.setComment(comment);
			policy.setExternProperties(externProperties);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void updateModule(ModuleKeyDTO keyDTO, String title, String externProperties, String comment)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException, VersionConverterClassException {
		emRWL.writeLock().lock();
		try {
			Module module = getModule(new ModuleKey(vcc.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			module.setTitle(title);
			module.setComment(comment);
			module.setExternProperties(externProperties);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void updateConsent(ConsentKeyDTO keyDTO, String externProperties, String comment, String scanBase64, String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			UnknownSignerIdTypeException, UnknownConsentException {
		emRWL.writeLock().lock();
		try {
			Consent consent = getConsentObject(keyDTO);
			consent.setComment(comment);
			consent.setExternProperties(externProperties);
			// TODO alten nicht ueberschreiben - mehrere scans zulassen?
			consent.setScanBase64(scanBase64, scanFileType);
			em.persist(consent);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			UnknownSignerIdTypeException, UnknownConsentException, DuplicateEntryException {
		emRWL.writeLock().lock();
		try {
			Consent consent = getConsentObject(consentKeyDTO);
			if (consent.getScanBase64() != null && consent.getScanBase64().getText() != null) {
				String message = "there's already a scan attached to " + consentKeyDTO;
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			consent.setScanBase64(scanBase64, fileType);
			if (logger.isInfoEnabled()) {
				logger.info("scan added to " + consentKeyDTO);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException {
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			SignerIdType signerIdType = new SignerIdType(domain, signerIdTypeName);
			if (domain.getSignerIdTypes().contains(signerIdType)) {
				String message = "signer id type '" + signerIdTypeName + "' already exists within " + domain;
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			em.persist(signerIdType);
			// relationen nach persistierung
			domain.getSignerIdTypes().add(signerIdType);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException {
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<SignerId> root = criteriaQuery.from(SignerId.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignerId_.key).get(SignerIdKey_.signerIdTypeKey), signerIdType.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0) {
				String message = "signer id type '" + signerIdTypeName + "' for " + domain + " is in use and therefore can't be deleted";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(signerIdType);
			// relationen koennen nicht in der @PreRemove in der entity aufgeraeumt werden - das macht sonst die cascade-delete funktionalitaet domain<->signerIdTypes kaputt
			domain.getSignerIdTypes().remove(signerIdType);
			em.flush();
			if (logger.isInfoEnabled()) {
				logger.info("signer id type '" + signerIdTypeName + "' successfully removed from domain '" + domainName);
			}
			return;
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException {
		List<String> result = new ArrayList<String>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<SignerId> criteriaQuery = criteriaBuilder.createQuery(SignerId.class);
			Root<SignerId> root = criteriaQuery.from(SignerId.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignerId_.key).get(SignerIdKey_.signerIdTypeKey), signerIdType.getKey());
			criteriaQuery.select(root).where(predicate);
			List<SignerId> signerIds = em.createQuery(criteriaQuery).getResultList();
			for (SignerId signerId : signerIds) {
				result.add(signerId.getKey().getValue());
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, VersionConverterClassException,
			InvalidVersionException {
		List<String> result = new ArrayList<String>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
			List<String> signerIds = getAllIdsForSignerIdType(domain.getName(), signerIdTypeName);
			for (String signerId : signerIds) {
				SignerIdDTO signerIdDTO = new SignerIdDTO(signerIdTypeName, signerId);
				Set<SignerIdDTO> dummySet = new HashSet<SignerIdDTO>();
				dummySet.add(signerIdDTO);
				if (isConsented(dummySet, policyKeyDTO, config)) {
					result.add(signerId);
				}
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, VersionConverterClassException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException {
		validateConsentReturningObjects(consentDTO, allowRevoke);
	}

	private Map<ModuleKeyDTO, Module> validateConsentReturningObjects(ConsentDTO consentDTO, boolean allowRevoke)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException {
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		// module-map fuer relationen
		Map<ModuleKeyDTO, Module> result = new HashMap<ModuleKeyDTO, Module>();
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(vcc.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			List<ModuleConsentTemplate> moduleConsentTemplates = consentTemplate.getModuleConsentTemplates();
			List<ModuleKeyDTO> validModulesForTemplate = new ArrayList<ModuleKeyDTO>();
			// test, ob fuer alle modules ein consent status gesetzt wurde
			// gleichzeitig auf mandatory-felder-logik pruefen
			// wenn widerruf nicht zugelassen werden soll, einfach so tun, als waere einem mandatory-feld zugestimmt
			boolean mandatoryFieldsTrue = !allowRevoke;
			boolean mandatoryFieldsFalse = false;
			boolean optionalFieldsTrue = false;
			for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates) {
				ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getKey().getModuleKey().toDTO(vcc.getModuleVersionConverter(domain.getName()));
				validModulesForTemplate.add(moduleKeyDTO);
				ModuleStateDTO moduleState = consentDTO.getModuleStates().get(moduleKeyDTO);
				if (moduleState == null) {
					String message = "missing module: " + moduleKeyDTO;
					logger.warn(message);
					throw new MissingRequiredObjectException(message);
				}
				ConsentStatus consentStatus = moduleState.getConsentState();
				if (consentStatus == null) {
					String message = "there's no consent status set for: " + moduleKeyDTO;
					logger.warn(message);
					throw new MissingRequiredObjectException(message);
				}
				ConsentStatusType csType = consentStatus.getConsentStatusType();
				// test fuer mandatory/optional regeln
				if (moduleConsentTemplate.getMandatory()) {
					if (csType.equals(ConsentStatusType.ACCEPTED)) {
						mandatoryFieldsTrue = true;
					} else if (csType.equals(ConsentStatusType.DECLINED)) {
						mandatoryFieldsFalse = true;
					} else {
						String message = "all mandatory fields must have a consent status of either "
								+ ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED) + " or "
								+ ConsentStatus.getStringForType(ConsentStatusType.DECLINED);
						logger.warn(message);
						throw new MandatoryFieldsException(message);
					}
				} else if (csType.equals(ConsentStatusType.ACCEPTED)) {
					optionalFieldsTrue = true;
				}
				// test fuer parent regeln
				if (moduleConsentTemplate.getParent() != null && csType.equals(ConsentStatusType.ACCEPTED)) {
					ModuleStateDTO parentModuleState = consentDTO.getModuleStates()
							.get(moduleConsentTemplate.getParent().getKey().toDTO(vcc.getModuleVersionConverter(domain.getName())));
					ConsentStatus parentConsentStatus = parentModuleState == null ? null : parentModuleState.getConsentState();
					if (parentConsentStatus == null || !ConsentStatusType.ACCEPTED.equals(parentConsentStatus.getConsentStatusType())) {
						String message = "the consent status of the parent module (" + moduleConsentTemplate.getParent().getKey()
								+ ") also need to be one of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
								+ " if the consent status of the child module (" + moduleKeyDTO + ") is one of them";
						logger.warn(message);
						throw new MandatoryFieldsException(message);
					}
				}
			}
			if (mandatoryFieldsFalse && mandatoryFieldsTrue) {
				String message = "mandatory modules cannot have a consent status of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
						+ " and one of " + ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + " within one consent";
				logger.warn(message);
				throw new MandatoryFieldsException(message);
			} else if (mandatoryFieldsFalse && optionalFieldsTrue) {
				String message = "if at least one mandatory module have a consent status of "
						+ ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + ", all optional modules may not have a consent status of "
						+ ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED);
				logger.warn(message);
				throw new MandatoryFieldsException(message);
			}

			if (optionalFieldsTrue || mandatoryFieldsTrue) {
				if (!domain.getPropertiesObject().isNoMandatoryScans()
						&& (consentDTO.getScanBase64() == null || consentDTO.getScanBase64().trim().isEmpty())
						&& (consentDTO.getPatientSignatureBase64() == null || consentDTO.getPhysicanSignatureBase64() == null
								|| consentDTO.getPatientSignatureBase64().trim().isEmpty()
								|| consentDTO.getPhysicanSignatureBase64().trim().isEmpty())) {
					String message = "signatures of both the patient and a physican are mandatory as well as the scan of the consent if at least one module is consented. "
							+ consentDTO.getKey();
					logger.warn(message);
					throw new MissingRequiredObjectException(message);
				} else if (consentDTO.getPatientSigningDate() == null || consentDTO.getPatientSigningDate().before(minDateForSignature)
						|| consentDTO.getPhysicanSigningDate() == null || consentDTO.getPhysicanSigningDate().before(minDateForSignature)) {
					String message = "signature date need to be after " + minDateForSignature + " if at least one module is consented. "
							+ consentDTO.getKey();
					logger.warn(message);
					throw new MissingRequiredObjectException(message);
				} else if (consentDTO.getKey().getConsentDate().before(minDateForSignature)) {
					String message = "consent date need to be after " + minDateForSignature + " if at least one module is consented. "
							+ consentDTO.getKey();
					logger.warn(message);
					throw new MissingRequiredObjectException(message);
				}
			}

			for (ModuleKeyDTO moduleKeyDTO : consentDTO.getModuleStates().keySet()) {
				if (!validModulesForTemplate.contains(moduleKeyDTO)) {
					String message = "the given " + moduleKeyDTO + " is not part of the given " + consentKeyDTO.getConsentTemplateKey();
					logger.warn(message);
					throw new UnknownModuleException(message);
				}
				ModuleKey moduleKey = new ModuleKey(vcc.getModuleVersionConverter(domain.getName()), moduleKeyDTO);
				result.put(moduleKeyDTO, getModule(moduleKey));
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public void invalidateConsent(ConsentKeyDTO consentKeyDTO, ConsentStatus invalidationState, String comment, String scanBase64)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			UnknownSignerIdTypeException, UnknownConsentException, InternalException {
		emRWL.writeLock().lock();
		try {
			Consent consent = getConsentObject(consentKeyDTO);
			ConsentDTO newConsentDTO = new ConsentDTO(consent.toDTO(vcc), new Date(System.currentTimeMillis()));
			for (ModuleStateDTO moduleState : newConsentDTO.getModuleStates().values()) {
				moduleState.setConsentState(invalidationState);
			}
			newConsentDTO.setPatientSignatureBase64(null);
			newConsentDTO.setPatientSigningDate(null);
			newConsentDTO.setPhysicanSignatureBase64(null);
			newConsentDTO.setPhysicanSigningDate(null);
			newConsentDTO.setComment(comment);
			newConsentDTO.setScanBase64(scanBase64);
			addConsent(newConsentDTO);
			if (logger.isInfoEnabled()) {
				logger.info(consentKeyDTO + " invalidated; new consent stored (" + newConsentDTO.getKey() + ")");
			}
		} catch (InconsistentStatusException | UnknownModuleException | MissingRequiredObjectException | InvalidFreeTextException
				| MandatoryFieldsException | DuplicateEntryException e) {
			String message = "unexpected exception while invalidating consent: " + e.getMessage();
			logger.error(message);
			throw new InternalException(message);
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			UnknownSignerIdTypeException, InternalException {
		emRWL.writeLock().lock();
		try {
			Domain domain = getDomainObject(ctKeyDTO.getDomainName());
			ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(vcc.getCTVersionConverter(domain.getName()), ctKeyDTO));
			VirtualPerson virtualPerson = getOrCreateVirtualPersonFor(domain, signerIdDTOs);
			ConsentKey consentKey = new ConsentKey(consentTemplate, new Date(), virtualPerson.getId());
			if (em.find(Consent.class, consentKey) != null) {
				String message = consentKey + " already exists - should not have happened! - consent was generated";
				logger.error(message);
				throw new InternalException(message);
			}
			Map<ModuleKeyDTO, Module> modules = new HashMap<ModuleKeyDTO, Module>();
			List<ModuleConsentTemplate> mcts = consentTemplate.getModuleConsentTemplates();
			ConsentDTO tempConsentDTO = new ConsentDTO(consentKey.toDTO(vcc.getCTVersionConverter(domain.getName()), virtualPerson));
			for (ModuleConsentTemplate mct : mcts) {
				ModuleKeyDTO moduleKeyDTO = mct.getKey().getModuleKey().toDTO(vcc.getModuleVersionConverter(domain.getName()));
				modules.put(moduleKeyDTO, mct.getModule());
				// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
				tempConsentDTO.getModuleStates().put(moduleKeyDTO,
						new ModuleStateDTO(moduleKeyDTO, ConsentStatus.REFUSED, new ArrayList<PolicyKeyDTO>()));
			}
			for (FreeTextDef freeTextDef : consentTemplate.getFreeTextDefs()) {
				if (freeTextDef.getRequired()) {
					tempConsentDTO.getFreeTextVals().put(freeTextDef.getKey().getName(), "dummy for refused consent");
				}
			}
			try {
				Consent newConsent = new Consent(consentTemplate, tempConsentDTO, modules, virtualPerson);
				em.persist(newConsent);
				// relationen zu bereits vorhanden objekten nach der persistierung setzen
				virtualPerson.getConsents().add(newConsent);
				consentCache.addConsent(virtualPerson.getId(), signerIdDTOs, newConsent.getSignedPolicies());
			} catch (MissingRequiredObjectException e) {
				String message = "MissingRequiredObjectException - should not have happened! - consent was generated";
				logger.error(message, e);
				throw new InternalException(message);
			} catch (InvalidFreeTextException e) {
				String message = "InvalidFreeTextException - should not have happened! - consent was generated";
				logger.error(message, e);
				throw new InternalException(message);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException,
			UnknownSignerIdTypeException, UnknownConsentException, InternalException {
		emRWL.writeLock().lock();
		try {
			Consent consent = getConsentObject(consentKeyDTO);
			addSignerId(consent, signerIdDTO);

			if (logger.isInfoEnabled()) {
				logger.info(signerIdDTO + " added to " + consentKeyDTO);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	private void addSignerId(Consent consent, SignerIdDTO signerIdDTO)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException {
		emRWL.writeLock().lock();
		try {
			Domain domainObject = getDomainObject(consent.getConsentTemplate().getKey().getDomainName());
			SignerIdType signerIdType = getSignerIdType(domainObject, signerIdDTO.getIdType());
			try {
				SignerId signerId;
				try {
					// check if signer id exists
					signerId = getSignerId(signerIdType, signerIdDTO.getId());
				} catch (UnknownSignerIdException e) {
					// save new signer id to database
					signerId = new SignerId(signerIdType, signerIdDTO.getId());
					em.persist(signerId);
				}

				VirtualPerson oldVirtualPerson = consent.getVirtualPerson();
				Set<SignerIdDTO> toBeTestedSignerIDs = new HashSet<SignerIdDTO>();
				toBeTestedSignerIDs.add(signerIdDTO);

				// check if a virtualperson using this very combination of signerids exists
				for (VirtualPersonSignerId personSignerIds : oldVirtualPerson.getVirtualPersonSignerIds()) {
					toBeTestedSignerIDs.add(personSignerIds.getSignerId().toDTO());
				}
				VirtualPerson usableVP = getOrCreateVirtualPersonFor(domainObject, toBeTestedSignerIDs);
				if (usableVP == oldVirtualPerson) {
					logger.info("consent already belonging to the given signerId");
					return;
				}

				ConsentDTO consentDTO = consent.toDTO(vcc);
				Map<ModuleKeyDTO, Module> modules = validateConsentReturningObjects(consentDTO, true);
				Consent newConsent = new Consent(consent.getConsentTemplate(), consentDTO, modules, usableVP);
				em.persist(newConsent);
				usableVP.getConsents().add(newConsent);
				// den alten nicht loeschen! bisherige anfragen muessen valid bleiben

				consentCache.addConsent(usableVP.getId(), Collections.singleton(signerIdDTO), consent.getSignedPolicies());
			} catch (VersionConverterClassException | InvalidVersionException | InconsistentStatusException | MissingRequiredObjectException
					| MandatoryFieldsException | UnknownModuleException | InvalidFreeTextException e) {
				String message = "unexpected exception while invalidating consent: " + e.getMessage();
				logger.error(message);
				throw new InternalException(message);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
		if (logger.isInfoEnabled()) {
			logger.info(signerIdDTO + " added to " + consent);
		}
	}

	public Date getExpirationDate(ConsentKeyDTO consentKeyDTO, Date result) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, VersionConverterClassException, UnknownSignerIdTypeException, UnknownConsentException, InternalException {
		emRWL.readLock().lock();
		try {
			Consent consent = getConsentObject(consentKeyDTO);
			result = consent.getExpirationDate();
		} catch (InternalException e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdTypeException, InternalException {
		emRWL.writeLock().lock();
		try {
			Domain domainObject = getDomainObject(domain);
			Set<VirtualPerson> virtualPersons = getVirtualPersons(domainObject, Collections.singleton(existentSignerIdDTO));
			if (logger.isInfoEnabled()) {
				logger.info("found " + virtualPersons.size() + " persons for given signerId");
			}
			try {
				for (VirtualPerson virtualPerson : virtualPersons) {
					List<Consent> consents = getConsentsForPerson(domainObject, virtualPerson);
					if (logger.isInfoEnabled()) {
						logger.info("found " + consents.size() + " consents for virtual person " + virtualPerson);
					}
					for (Consent consent : consents) {
						addSignerId(consent, newSignerIdDTO);
					}
				}
			} catch (UnknownSignerIdTypeException | InvalidVersionException | UnknownConsentTemplateException e) {
				String message = "unexpected exception while ...: " + e.getMessage();
				logger.error(message);
				throw new InternalException(message);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
	}

	private ConsentStatusType checkPolicies(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("properties for checkPolicies: ");
			sb.append(domain.getPropertiesObject().toString());
			logger.debug(sb.toString());
		}
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies) {
			ConsentStatusType status = signedPolicy.getConsentStatus().getConsentStatusType();
			if (domain.getPropertiesObject().isPermanentRevoke()) {
				if (status.equals(ConsentStatusType.DECLINED)
						|| (config.getUnknownStateIsConsideredAsDecline() && status.equals(ConsentStatusType.UNKNOWN))) {
					currentSignedPolicy = signedPolicy;
					break;
				}
			}
			if (status.equals(ConsentStatusType.UNKNOWN)) {
				continue;
			}
			if (domain.getPropertiesObject().isTakeHighestVersion()
					&& (currentSignedPolicy == null || signedPolicy.getVersion() > currentSignedPolicy.getVersion())) {
				currentSignedPolicy = signedPolicy;
			} else if (currentSignedPolicy == null || signedPolicy.getConsentDate() > currentSignedPolicy.getConsentDate()) {
				currentSignedPolicy = signedPolicy;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("found the following current signed policy: " + currentSignedPolicy);
		}
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		if (currentSignedPolicy != null) {
			result = currentSignedPolicy.getConsentStatus().getConsentStatusType();
		}
		return result;
	}

	private ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFromString, int versionFromOffset, String versionToString, int versionToOffset, CheckConsentConfig config)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException, VersionConverterClassException {
		emRWL.readLock().lock();
		try {
			Domain domain = getDomainObject(domainName);
			int versionFrom = vcc.getPolicyVersionConverter(domainName).stringToInt(versionFromString) + versionFromOffset;
			int versionTo = vcc.getPolicyVersionConverter(domainName).stringToInt(versionToString) + versionToOffset;
			return getConsentStatusTypeFromTo(signerIdDTOs, domain, policyName, versionFrom, versionTo, config);
		} finally {
			emRWL.readLock().unlock();
		}
	}

	/**
	 * from <= version <= to
	 *
	 * @param id
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 * @return
	 * @throws UnknownSignerIdTypeException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws ObjectNotFoundException
	 */
	private ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, Domain domain, String policyName, int versionFrom,
			int versionTo, CheckConsentConfig config) throws InvalidVersionException, UnknownSignerIdTypeException, VersionConverterClassException {
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		if (logger.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer("get consent status for domain '");
			sb.append(domain.getName());
			sb.append("', policiy '");
			sb.append(policyName);
			sb.append("', version range from ");
			sb.append(versionFrom);
			sb.append(" to ");
			sb.append(versionTo);
			sb.append(" idMatchingType ");
			sb.append(config.getIdMatchingType());
			sb.append(" and ");
			for (SignerIdDTO signerIdDTO : signerIdDTOs) {
				sb.append(signerIdDTO);
			}
			logger.debug(sb.toString());
		}

		List<CachedSignedPolicy> cachedSignedPolicies;
		ConsentStatusType consentStatusTypeForSigner;
		switch (config.getIdMatchingType()) {
			case AT_LEAST_ONE:
				for (SignerIdDTO signerIdDTO : signerIdDTOs) {
					getSignerIdType(domain, signerIdDTO.getIdType());
					if (config.getIgnoreVersionNumber()) {
						cachedSignedPolicies = consentCache.getCachedPoliciesForSigner(domain.getName(), signerIdDTO, policyName);
					} else {
						cachedSignedPolicies = consentCache.getCachedPoliciesForSigner(domain.getName(), signerIdDTO, policyName, versionFrom,
								versionTo);
					}
					consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
					if (logger.isDebugEnabled()) {
						StringBuilder sb = new StringBuilder("found ");
						sb.append(cachedSignedPolicies.size());
						sb.append(" signed policies for domain '");
						sb.append(domain.getName());
						sb.append("', policy '");
						sb.append(policyName);
						sb.append("', version range from ");
						sb.append(versionFrom);
						sb.append(" to ");
						sb.append(versionTo);
						sb.append(" signed by ");
						sb.append(signerIdDTO);
						sb.append(". checkPolicies result: ");
						sb.append(consentStatusTypeForSigner);
						logger.debug(sb.toString());
					}
					if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED)) {
						result = consentStatusTypeForSigner;
					} else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED)) {
						result = consentStatusTypeForSigner;
						break;
					}
				}
			break;
			case AT_LEAST_ALL:
				List<VirtualPerson> persons;
				try {
					persons = findVirtualPersons(domain, signerIdDTOs);
				} catch (ObjectNotFoundException e) {
					// TODO hier lieber ne UnknownSignerIdException werfen?
					logger.warn("could not find virtualPerson for signerIds " + signerIdDTOs);
					return config.getUnknownStateIsConsideredAsDecline() ? ConsentStatusType.DECLINED : ConsentStatusType.UNKNOWN;
				}
				cachedSignedPolicies = new ArrayList<>();
				for (VirtualPerson person : persons) {
					if (config.getIgnoreVersionNumber()) {
						cachedSignedPolicies.addAll(consentCache.getCachedPoliciesForVP(domain.getName(), person, policyName));
					} else {
						cachedSignedPolicies
								.addAll(consentCache.getCachedPoliciesForVP(domain.getName(), person, policyName, versionFrom, versionTo));
					}
				}
				consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder("found ");
					sb.append(cachedSignedPolicies.size());
					sb.append(" signed policies for domain '");
					sb.append(domain.getName());
					sb.append("', policy '");
					sb.append(policyName);
					sb.append("', version range from ");
					sb.append(versionFrom);
					sb.append(" to ");
					sb.append(versionTo);
					sb.append(" signed by one of ");
					sb.append(persons.size());
					sb.append(" persons. checkPolicies result: ");
					sb.append(consentStatusTypeForSigner);
					logger.debug(sb.toString());
				}
				if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED)) {
					result = consentStatusTypeForSigner;
				} else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED)) {
					result = consentStatusTypeForSigner;
				}
			break;
			case EXACT:
				VirtualPerson person;
				try {
					person = findVirtualPerson(domain, signerIdDTOs);
				} catch (ObjectNotFoundException e) {
					// TODO hier lieber ne UnknownSignerIdException werfen?
					logger.warn("could not find virtualPerson for signerIds " + signerIdDTOs);
					return config.getUnknownStateIsConsideredAsDecline() ? ConsentStatusType.DECLINED : ConsentStatusType.UNKNOWN;
				}
				if (config.getIgnoreVersionNumber()) {
					cachedSignedPolicies = consentCache.getCachedPoliciesForVP(domain.getName(), person, policyName);
				} else {
					cachedSignedPolicies = consentCache.getCachedPoliciesForVP(domain.getName(), person, policyName, versionFrom, versionTo);
				}
				consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder("found ");
					sb.append(cachedSignedPolicies.size());
					sb.append(" signed policies for domain '");
					sb.append(domain.getName());
					sb.append("', policy '");
					sb.append(policyName);
					sb.append("', version range from ");
					sb.append(versionFrom);
					sb.append(" to ");
					sb.append(versionTo);
					sb.append(" signed by ");
					sb.append(person);
					sb.append(". checkPolicies result: ");
					sb.append(consentStatusTypeForSigner);
					logger.debug(sb.toString());
				}
				if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED)) {
					result = consentStatusTypeForSigner;
				} else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED)) {
					result = consentStatusTypeForSigner;
				}
			break;
		}
		return result;
	}

	private List<Consent> getConsentsForPerson(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("get consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.debug(sb.toString());
		}
		emRWL.readLock().lock();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Set<VirtualPerson> signers = getVirtualPersons(domain, signerIdDTOs);
			List<Consent> result = new ArrayList<Consent>();
			for (VirtualPerson signer : signers) {
				Predicate predicate = criteriaBuilder.and(
						criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), signer.getId()),
						criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName()));
				criteriaQuery.select(root).where(predicate);
				result.addAll(em.createQuery(criteriaQuery).getResultList());
			}
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("found " + result.size() + " consents for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				logger.debug(sb.toString());
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private List<Consent> getConsentsForPerson(Domain domain, VirtualPerson person) throws UnknownSignerIdTypeException {
		emRWL.readLock().lock();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			List<Consent> result = new ArrayList<Consent>();
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), person.getId()),
					criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName()));
			criteriaQuery.select(root).where(predicate);
			result.addAll(em.createQuery(criteriaQuery).getResultList());
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private List<Consent> getConsentsForConsentTemplate(ConsentTemplateKey ctKey) throws UnknownDomainException, UnknownConsentTemplateException {
		if (logger.isDebugEnabled()) {
			logger.debug("get consents for consent template " + ctKey);
		}
		emRWL.readLock().lock();
		try {
			getConsentTemplate(ctKey);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey), ctKey);
			criteriaQuery.select(root).where(predicate);
			List<Consent> result = em.createQuery(criteriaQuery).getResultList();
			if (logger.isDebugEnabled()) {
				logger.debug("found " + result.size() + " consents for consent template " + ctKey);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private Domain getDomainObject(String domainName) throws UnknownDomainException {
		if (logger.isDebugEnabled()) {
			logger.debug("get domain for " + domainName);
		}
		emRWL.readLock().lock();
		try {
			Domain domainObject = em.find(Domain.class, domainName);
			if (domainObject == null) {
				String message = "unknown domain: " + domainName;
				logger.error(message);
				throw new UnknownDomainException(message);
			}
			return domainObject;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private Policy getPolicy(PolicyKey key) throws UnknownPolicyException {
		if (logger.isDebugEnabled()) {
			logger.debug("get policy for " + key);
		}
		emRWL.readLock().lock();
		try {
			Policy result = em.find(Policy.class, key);
			if (result == null) {
				String message = "unknown policy: " + key;
				logger.error(message);
				throw new UnknownPolicyException(message);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private Module getModule(ModuleKey key) throws UnknownModuleException {
		if (logger.isDebugEnabled()) {
			logger.debug("get module for " + key);
		}
		emRWL.readLock().lock();
		try {
			Module result = em.find(Module.class, key);
			if (result == null) {
				String message = "unknown module: " + key;
				logger.error(message);
				throw new UnknownModuleException(message);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private ConsentTemplate getConsentTemplate(ConsentTemplateKey key) throws UnknownConsentTemplateException {
		if (logger.isDebugEnabled()) {
			logger.debug("get consent template for " + key);
		}
		emRWL.readLock().lock();
		try {
			ConsentTemplate result = em.find(ConsentTemplate.class, key);
			if (result == null) {
				String message = "unknown consent template: " + key;
				logger.error(message);
				throw new UnknownConsentTemplateException(message);
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	private VirtualPerson getOrCreateVirtualPersonFor(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("get or create virtual person for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.debug(sb.toString());
		}
		VirtualPerson result;
		emRWL.readLock().lock();
		try {
			// suche nach der virtuellen person, die genau die gegebenen id umfasst
			result = findVirtualPerson(domain, signerIdDTOs);
		} catch (ObjectNotFoundException e) {
			emRWL.writeLock().lock();
			try {
				result = createVirtualPerson(domain, signerIdDTOs);
			} finally {
				emRWL.writeLock().unlock();
			}
		} finally {
			emRWL.readLock().unlock();
		}
		return result;
	}

	private void testSignerIdTypes(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException {
		Set<String> idTypesForTest = new HashSet<String>();
		for (SignerIdDTO signerId : signerIdDTOs) {
			idTypesForTest.add(signerId.getIdType());
		}
		for (SignerIdType signerIdType : domain.getSignerIdTypes()) {
			if (idTypesForTest.contains(signerIdType.getKey().getName())) {
				idTypesForTest.remove(signerIdType.getKey().getName());
			}
		}
		if (!idTypesForTest.isEmpty()) {
			StringBuilder sb = new StringBuilder("unknown signer id types:");
			for (String idType : idTypesForTest) {
				sb.append(" '" + idType + "'");
			}
			logger.error(sb.toString());
			throw new UnknownSignerIdTypeException(sb.toString());
		}
	}

	private VirtualPerson findVirtualPerson(Domain domain, Set<SignerIdDTO> signerIdDTOs)
			throws ObjectNotFoundException, UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("search virtual person for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.debug(sb.toString());
		}
		testSignerIdTypes(domain, signerIdDTOs);
		Set<VirtualPerson> persons = getVirtualPersons(domain, signerIdDTOs);
		for (VirtualPerson person : persons) {
			Set<VirtualPersonSignerId> virtualPersonSignerIds = new HashSet<VirtualPersonSignerId>(person.getVirtualPersonSignerIds());
			if (virtualPersonSignerIds.size() != signerIdDTOs.size()) {
				continue;
			}
			boolean found = true;
			for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds) {
				if (!signerIdDTOs.contains(virtualPersonSignerId.getSignerId().toDTO())) {
					found = false;
					break;
				}
			}
			if (found) {
				if (logger.isDebugEnabled()) {
					logger.debug(person.toString() + " found within " + domain);
				}
				return person;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("virtual person not found within " + domain);
		}
		throw new ObjectNotFoundException();
	}

	private List<VirtualPerson> findVirtualPersons(Domain domain, Set<SignerIdDTO> signerIdDTOs)
			throws ObjectNotFoundException, UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("search virtual persons for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.debug(sb.toString());
		}
		testSignerIdTypes(domain, signerIdDTOs);
		Set<VirtualPerson> persons = getVirtualPersons(domain, signerIdDTOs);
		List<VirtualPerson> result = new ArrayList<>();
		for (VirtualPerson person : persons) {
			Set<VirtualPersonSignerId> virtualPersonSignerIds = new HashSet<VirtualPersonSignerId>(person.getVirtualPersonSignerIds());
			Set<SignerIdDTO> virtualPersonSignerIdDTOs = new HashSet<>();
			for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds) {
				virtualPersonSignerIdDTOs.add(virtualPersonSignerId.getSignerId().toDTO());
			}
			boolean found = true;
			for (SignerIdDTO signerIdDTO : signerIdDTOs) {
				if (!virtualPersonSignerIdDTOs.contains(signerIdDTO)) {
					found = false;
					break;
				}
			}
			if (found) {
				if (logger.isDebugEnabled()) {
					logger.debug(person.toString() + " found within " + domain);
				}
				result.add(person);
			}
		}
		if (result.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("no virtual person found within " + domain);
			}
			throw new ObjectNotFoundException();
		}
		return result;
	}

	private VirtualPerson createVirtualPerson(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("create virtual person for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.debug(sb.toString());
		}
		List<SignerId> signerIds = new ArrayList<SignerId>();
		VirtualPerson result = null;
		emRWL.writeLock().lock();
		try {
			for (SignerIdDTO signerIdDTO : signerIdDTOs) {
				SignerIdType signerIdType = getSignerIdType(domain, signerIdDTO.getIdType());
				SignerId signerId = em.find(SignerId.class, new SignerIdKey(signerIdType.getKey(), signerIdDTO.getId()));
				if (signerId == null) {
					signerId = new SignerId(signerIdType, signerIdDTO.getId());
					em.persist(signerId);
				}
				signerIds.add(signerId);
			}
			result = new VirtualPerson();
			em.persist(result);
			// relationen nach persistierung
			for (SignerId signerId : signerIds) {
				VirtualPersonSignerId virtualPersonSignerId = new VirtualPersonSignerId(result, signerId);
				signerId.getVirtualPersonSignerIds().add(virtualPersonSignerId);
				result.getVirtualPersonSignerIds().add(virtualPersonSignerId);
			}
		} finally {
			emRWL.writeLock().unlock();
		}
		if (logger.isDebugEnabled()) {
			logger.debug(result.toString() + " created within " + domain);
		}
		return result;
	}

	/**
	 * liefert alle virtualPersons, welche mindestens einer der gegebenen signerId zugeordnet sind
	 *
	 * @param domain
	 * @param signerIdDTOs
	 * @return
	 * @throws UnknownSignerIdTypeException
	 */
	private Set<VirtualPerson> getVirtualPersons(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("get virtual persons for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within ");
			sb.append(domain);
			logger.debug(sb.toString());
		}
		Set<VirtualPerson> result = new HashSet<VirtualPerson>();
		emRWL.readLock().lock();
		try {
			for (SignerIdDTO signerIdDTO : signerIdDTOs) {
				getSignerIdType(domain, signerIdDTO.getIdType());
				SignerIdTypeKey signerIdTypeKey = new SignerIdTypeKey(domain.getName(), signerIdDTO.getIdType());
				SignerId signerId = em.find(SignerId.class, new SignerIdKey(signerIdTypeKey, signerIdDTO.getId()));
				if (signerId != null) {
					for (VirtualPersonSignerId vpsi : signerId.getVirtualPersonSignerIds()) {
						result.add(vpsi.getVirtualPerson());
					}
				}
			}
		} finally {
			emRWL.readLock().unlock();
		}
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("found ");
			sb.append(result.size());
			sb.append(" virtual persons for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within ");
			sb.append(domain);
			logger.debug(sb.toString());
		}
		return result;
	}

	private SignerIdType getSignerIdType(Domain domain, String idType) throws UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			logger.debug("get signer id type for '" + idType + "'");
		}

		for (SignerIdType signerIdType : domain.getSignerIdTypes()) {
			logger.debug("comparing: " + signerIdType.getKey().getName() + " and " + idType);
			if (signerIdType.getKey().getName().equals(idType)) {
				return signerIdType;
			}
		}
		String message = "unknown signer id type: " + idType + " for domain " + domain.getName();
		logger.error(message);
		throw new UnknownSignerIdTypeException(message);
	}

	/**
	 * get signer id of specified type if exists
	 *
	 * @param signerIdType
	 *            signerIdType to be found
	 * @param signerId
	 *            signer id to be found
	 * @return specified signer id if existing
	 * @throws UnknownSignerIdException
	 *             in case the signer id and type is not known
	 */
	private SignerId getSignerId(SignerIdType signerIdType, String signerId) throws UnknownSignerIdException {
		if (logger.isDebugEnabled()) {
			logger.debug("get signer id   '" + signerId + " of type " + signerIdType);
		}

		SignerIdKey key = new SignerIdKey(signerIdType.getKey(), signerId);
		emRWL.readLock().lock();
		try {
			SignerId signerIdFromDB = em.find(SignerId.class, key);
			if (signerIdFromDB == null) {
				String message = "unknown signer id : " + signerId + " of type " + signerIdType;
				logger.error(message);
				throw new UnknownSignerIdException(message);
			}
			return signerIdFromDB;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public int getNextModuleVersionBetween(String domainName, String moduleName, int minVersion, int maxVersion) {
		if (logger.isDebugEnabled()) {
			logger.debug("get next version for module '" + moduleName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		emRWL.readLock().lock();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Module> criteriaQuery = criteriaBuilder.createQuery(Module.class);
			Root<Module> root = criteriaQuery.from(Module.class);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(Module_.key).get(ModuleKey_.name), moduleName),
					criteriaBuilder.equal(root.get(Module_.key).get(ModuleKey_.domainName), domainName));
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.between(root.get(Module_.key).get(ModuleKey_.version), minVersion, maxVersion));
			criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.desc(root.get(Module_.key).get(ModuleKey_.version)));
			List<Module> modules = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
			int result;
			if (modules.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("no module found - take minversion as result: " + minVersion);
				}
				result = minVersion;
			} else {
				result = modules.get(0).getKey().getVersion() + 1;
				if (logger.isDebugEnabled()) {
					logger.debug("next version found: " + result);
				}
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}

	public int getNextCTVersionBetween(String domainName, String ctName, int minVersion, int maxVersion) {
		if (logger.isDebugEnabled()) {
			logger.debug("get next version for consent template '" + ctName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		emRWL.readLock().lock();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
			Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name), ctName),
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName));
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.between(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version), minVersion, maxVersion));
			criteriaQuery.select(root).where(predicate)
					.orderBy(criteriaBuilder.desc(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version)));
			List<ConsentTemplate> cts = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
			int result;
			if (cts.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("no consent template found - take minversion as result: " + minVersion);
				}
				result = minVersion;
			} else {
				result = cts.get(0).getKey().getVersion() + 1;
				if (logger.isDebugEnabled()) {
					logger.debug("next version found: " + result);
				}
			}
			return result;
		} finally {
			emRWL.readLock().unlock();
		}
	}
}
