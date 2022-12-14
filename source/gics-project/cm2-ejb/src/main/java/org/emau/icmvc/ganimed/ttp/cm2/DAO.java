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

import java.io.Serializable;
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

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebParam;
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
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownFhirIdObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownIDException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache.CachedSignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentDateValues;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
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
import org.emau.icmvc.ganimed.ttp.cm2.model.FreeTextVal;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module_;
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
import org.emau.icmvc.ganimed.ttp.cm2.util.ExternPropertyFilterByOIDs;
import org.emau.icmvc.ganimed.ttp.cm2.util.NotificationMessage;
import org.emau.icmvc.ganimed.ttp.cm2.util.Operator;
import org.emau.icmvc.ganimed.ttp.cm2.util.StatisticKeys;
import org.emau.icmvc.ganimed.ttp.cm2.util.StringPair;
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
	public static final String GICS_TOOL_PREFIX = "GICS";
	private static final String PERCENT = "%";
	private static final Logger LOGGER = LogManager.getLogger(DAO.class);
	private static final Date MINDATE_FOR_SIGNATURE = new Date(82860000); // 02.01.1970
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

	@EJB
	private NotificationSender notificationSender;

	@PersistenceContext(unitName = "gics")
	private EntityManager em;
	private static final ReentrantReadWriteLock EM_RWL = new ReentrantReadWriteLock();

	@PostConstruct
	private void checkConsentCache()
	{
		EM_RWL.readLock().lock();
		try
		{
			if (!ConsentCache.isInitialised())
			{
				LOGGER.info("initialise consent cache");
				ConsentCache.init(em);
				LOGGER.info("consent cache initialised");
			}
			if (!VersionConverterCache.isInitialised())
			{
				LOGGER.info("initialise version converter cache");
				try
				{
					VersionConverterCache.init(listDomainObjects());
					LOGGER.info("version converter cache initialised");
				}
				catch (VersionConverterClassException e)
				{
					String message = "exception while initialising version convert class cache " + e.getMessage();
					LOGGER.fatal(message, e);
				}
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException, InternalException, InvalidParameterException, RequirementsNotFullfilledException
	{
		addConsent(consentDTO, null);
	}

	public void addConsent(ConsentDTO consentDTO, String notificationClientID) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, DuplicateEntryException,
			InvalidFreeTextException, UnknownSignerIdTypeException, InternalException, InvalidParameterException, RequirementsNotFullfilledException
	{
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		EM_RWL.writeLock().lock();
		try
		{
			String domainName = consentKeyDTO.getConsentTemplateKey().getDomainName();
			Domain domain = getDomainObject(domainName);
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), consentKeyDTO.getConsentTemplateKey()));
			if (!consentTemplate.getFinalised())
			{
				String message = consentKeyDTO + " can't be added because the consent template isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			// query previous policy status for notification
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getCurrentPolicyStates(consentKeyDTO) : null;

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
			// relationen zu bereits vorhanden objekten nach der persistierung setzen
			virtualPerson.getConsents().add(newConsent);
			ConsentCache.addConsent(virtualPerson.getId(), convertToSignerIdKeys(domain, consentKeyDTO.getSignerIds()),
					getSignedPoliciesWithExpirationDates(newConsent.getSignedPolicies()), newConsent);

			sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, "AddConsent", null);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	/**
	 * Returns a map with the policy keys of all assigned policies of all assigned modules of the consent template for the given consent key
	 * as keys in the map and their consented state as returned by {@link #isConsented(Set, PolicyKeyDTO, CheckConsentConfig)} with the
	 * signer IDs of the given consent key w.r.t. the default {@link CheckConsentConfig}.
	 *
	 * @param consentKeyDTO
	 * 		the consent key to che the policy state for
	 * @return a map with the policy states for the given consent key
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 */
	protected Map<PolicyKeyDTO, Boolean> getCurrentPolicyStates(ConsentKeyDTO consentKeyDTO)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException
	{
		return getCurrentPolicyStates(consentKeyDTO, null);
	}

	/**
	 * Returns a map with the policy keys of all assigned policies of all assigned modules of the consent template for the given consent key
	 * as keys in the map and their consented state as returned by {@link #isConsented(Set, PolicyKeyDTO, CheckConsentConfig)} with the
	 * * signer IDs of the given consent key w.r.t. the given {@link CheckConsentConfig}.
	 *
	 * @param consentKeyDTO
	 * 		the consent key to che the policy state for
	 * @param config
	 * 		the configuration how to check the consent
	 * @return a map with the policy states for the given consent key
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 */
	protected Map<PolicyKeyDTO, Boolean> getCurrentPolicyStates(ConsentKeyDTO consentKeyDTO, CheckConsentConfig config)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException
	{
		if (config == null)
		{
			config = new CheckConsentConfig();
		}

		Map<PolicyKeyDTO, Boolean> states = new HashMap<>();
		Set<SignerIdDTO> signerIds = consentKeyDTO.getSignerIds();
		PolicyKeyDTO p = null;

		try
		{
			ConsentTemplateDTO consentTemplate = getConsentTemplate(consentKeyDTO.getConsentTemplateKey());

			for (AssignedModuleDTO assignedModule : consentTemplate.getAssignedModules())
			{
				for (AssignedPolicyDTO assignedPolicy : assignedModule.getModule().getAssignedPolicies())
				{
					p = assignedPolicy.getPolicy().getKey();
					states.put(p, isConsented(signerIds, p, config));
				}
			}
			return states;
		}
		catch (UnknownPolicyException shouldNeverHappen)
		{
			LOGGER.warn("Failed to get the current policy status for policy key " + p + " and signer IDs " + signerIds, shouldNeverHappen);
		}
		catch (UnknownConsentTemplateException e)
		{
			LOGGER.warn("Failed to get the current policy states for the unknown consent template of " + consentKeyDTO, e);
		}
		return null;
	}

	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusType(signerIdDTOs, policyKeyDTO, config));
	}

	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException,
			UnknownSignerIdTypeException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
			PolicyKey policyKey;
			if (!config.getIgnoreVersionNumber())
			{
				policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), policyKeyDTO);
				getPolicy(policyKey);
			}
			else
			{
				policyKey = new PolicyKey(policyKeyDTO.getDomainName(), policyKeyDTO.getName(), 0);
				checkPolicyName(policyKey.getName());
			}
			ConsentStatusType result = getConsentStatusTypeFromTo(signerIdDTOs, domain, policyKeyDTO.getName(), policyKey.getVersion(),
					policyKey.getVersion(), config);
			if (LOGGER.isInfoEnabled())
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
				sb.append(" result: ");
				sb.append(result);
				LOGGER.info(sb.toString());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listConsentTemplates(String domainName, boolean onlyFinal)
			throws InvalidVersionException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domainObject = getDomainObject(domainName);
			List<ConsentTemplate> consentTemplates = domainObject.getConsentTemplates();
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplate consentTemplate : consentTemplates)
			{
				if (!onlyFinal || consentTemplate.getFinalised())
				{
					result.add(consentTemplate.toDTO());
				}
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " consent templates");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<ConsentTemplate> listConsentTemplatesByType(String domainName, ConsentTemplateType ctType)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listConsentTemplatesByType for domain " + domainName + " and type " + ctType);
		}
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
			Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName),
					criteriaBuilder.equal(root.get(ConsentTemplate_.type), ctType));
			criteriaQuery.select(root).where(predicate);
			List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).getResultList();
			List<ConsentTemplate> result = new ArrayList<>(ctList);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + result.size() + " consent templates");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName)
			throws InvalidVersionException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			getDomainObject(domainName);
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
			List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).getResultList();
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplate consentTemplate : ctList)
			{
				result.add(consentTemplate.toDTO());
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " consent templates");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidParameterException, InvalidPropertiesException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomainObject(consentTemplateDTO.getKey().getDomainName());
			if (!domainObject.getFinalised())
			{
				String message = consentTemplateDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			ConsentTemplateKey key = new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domainObject.getName()),
					consentTemplateDTO.getKey());
			ConsentTemplate duplicateTest = em.find(ConsentTemplate.class, key);
			if (duplicateTest != null)
			{
				String message = key + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			Map<ModuleKeyDTO, Module> modules = mapModules(consentTemplateDTO, domainObject);
			try
			{
				ConsentTemplate consentTemplate = new ConsentTemplate(domainObject, consentTemplateDTO, modules, finaliseRelatedEntities);
				em.persist(consentTemplate);
				// relationen nach persist
				domainObject.getConsentTemplates().add(consentTemplate);
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

	private Map<ModuleKeyDTO, Module> mapModules(ConsentTemplateDTO consentTemplateDTO, Domain domainObject)
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
			ModuleKey moduleKey = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domainObject.getName()), moduleKeyDTO);
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

	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			InvalidVersionException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
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
			// relationen werden in der @PreRemove in der entity aufgeraeumt
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

	public List<ModuleDTO> listModules(String domainName, boolean onlyFinal) throws InvalidVersionException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domainObject = getDomainObject(domainName);
			List<Module> modules = domainObject.getModules();
			List<ModuleDTO> result = new ArrayList<>();
			for (Module module : modules)
			{
				if (!onlyFinal || module.getFinalised())
				{
					result.add(module.toDTO());
				}
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " modules");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void addModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidVersionException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomainObject(moduleDTO.getKey().getDomainName());
			if (!domainObject.getFinalised())
			{
				String message = moduleDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			ModuleKey key = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domainObject.getName()), moduleDTO.getKey());
			Module duplicateTest = em.find(Module.class, key);
			if (duplicateTest != null)
			{
				String message = key + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			Module module = new Module(domainObject, moduleDTO, finaliseRelatedEntities);
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
				PolicyKey policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domainObject.getName()), assignedPolicyDTO.getPolicy().getKey());
				Policy policy = getPolicy(policyKey);
				policyNames.add(policyName);
				ModulePolicy mp = new ModulePolicy(module, policy, assignedPolicyDTO);
				module.getModulePolicies().add(mp);
				policies.put(policy, mp);
			}
			em.persist(module);
			// relationen nach persist
			domainObject.getModules().add(module);
			for (Entry<Policy, ModulePolicy> entry : policies.entrySet())
			{
				entry.getKey().getModulePolicies().add(entry.getValue());
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void deleteModule(ModuleKeyDTO keyDTO)
			throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownModuleException
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

	private ModuleDTO getModuleIntern(ModuleKeyDTO keyDTO) throws UnknownDomainException, UnknownModuleException, InvalidVersionException
	{
		EM_RWL.readLock().lock();
		try
		{
			ModuleKey moduleKey = new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO);
			return getModule(moduleKey).toDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException
	{
		return getModuleIntern(keyDTO);
	}

	public List<PolicyDTO> listPolicies(String domainName, boolean onlyFinal)
			throws InvalidVersionException, UnknownDomainException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domainObject = getDomainObject(domainName);
			List<Policy> policies = domainObject.getPolicies();
			List<PolicyDTO> result = new ArrayList<>();
			for (Policy policy : policies)
			{
				if (!onlyFinal || policy.getFinalised())
				{
					result.add(policy.toDTO());
				}
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " policies");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void addPolicy(PolicyDTO policyDTO) throws DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomainObject(policyDTO.getKey().getDomainName());
			if (!domainObject.getFinalised())
			{
				String message = policyDTO.getKey() + " can't be added because the domain isn't finalised";
				LOGGER.error(message);
				throw new RequirementsNotFullfilledException(message);
			}
			PolicyKey key = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domainObject.getName()), policyDTO.getKey());
			Policy duplicateTest = em.find(Policy.class, key);
			if (duplicateTest != null)
			{
				String message = key + " already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			Policy policy = new Policy(domainObject, policyDTO);
			em.persist(policy);
			domainObject.getPolicies().add(policy);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void deletePolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, ObjectInUseException
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

	private PolicyDTO getPolicyIntern(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException
	{
		EM_RWL.readLock().lock();
		try
		{
			Policy result = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			return result.toDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException
	{
		return getPolicyIntern(keyDTO);
	}

	private ConsentTemplateDTO getConsentTemplateIntern(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		EM_RWL.readLock().lock();
		try
		{
			ConsentTemplate result = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
			return result.toDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		return getConsentTemplateIntern(keyDTO);
	}

	public List<SignedPolicyDTO> getPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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
			if (LOGGER.isInfoEnabled())
			{
				StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				LOGGER.debug(sb.toString());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
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

	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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
			if (LOGGER.isInfoEnabled())
			{
				StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("for policy name '" + policyName + "' within " + domain);
				LOGGER.info(sb.toString());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	public List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdException, InvalidVersionException, UnknownSignerIdTypeException
	{
		List<SignedPolicyDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			List<PolicyDTO> policies = listPolicies(domainName, true);
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
								SignedPolicy sp = findSPByKey(currentCachedSignedPolicy.getSPKey());
								result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
										currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
										currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
												findVPByKey(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
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
					cachedSignedPolicies = new ArrayList<>();
					for (VirtualPerson person : persons)
					{
						for (PolicyDTO policy : policies)
						{
							try
							{
								cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policy.getKey().getName());
								currentCachedSignedPolicy = getCurrentPolicy(config, cachedSignedPolicies, domain);
								SignedPolicy sp = findSPByKey(currentCachedSignedPolicy.getSPKey());
								result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
										currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
										currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
												findVPByKey(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
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
							SignedPolicy sp = findSPByKey(currentCachedSignedPolicy.getSPKey());
							result.add(new SignedPolicyDTO(currentCachedSignedPolicy.getConsentStatus(),
									currentCachedSignedPolicy.getSPKey().getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(domainName)),
									currentCachedSignedPolicy.getSPKey().getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(domainName),
											findVPByKey(currentCachedSignedPolicy.getSPKey().getConsentKey().getVirtualPersonId())),
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

	private CachedSignedPolicy getCurrentPolicy(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain) throws ObjectNotFoundException
	{
		Long now = System.currentTimeMillis();
		Long requestDate = config.getRequestDate() != null ? config.getRequestDate().getTime() : now;
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies)
		{
			// Ignore if expired before request date
			if (signedPolicy.getConsentExpirationDate() < requestDate)
			{
				LOGGER.debug("signedPolicy.getConsentExpirationDate() < requestDate (" + signedPolicy.getConsentExpirationDate() + " < " + requestDate + ")");
				continue;
			}
			// Ignore if consented after request date
			if (signedPolicy.getGicsConsentDate() > requestDate)
			{
				LOGGER.debug("signedPolicy.getGicsConsentDate() > requestDate (" + signedPolicy.getGicsConsentDate() + " > " + requestDate + ")");
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
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found the following current signed policy: " + currentSignedPolicy);
		}
		if (currentSignedPolicy != null)
		{
			return currentSignedPolicy;
		}
		throw new ObjectNotFoundException();
	}

	/**
	 * Checks if the version of signedPolicy is higher than the version of currentSignedPolicy
	 * Or version is equal and {@link #consentDateIsNewer(CachedSignedPolicy, CachedSignedPolicy)}
	 *
	 * @param signedPolicy
	 * 		Possible new signed policy
	 * @param currentSignedPolicy
	 * 		current signed policy
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
	 * 		Possible new signed policy
	 * @param currentSignedPolicy
	 * 		current signed policy
	 * @return true if signedPolicy should replace the currentSignedPolicy
	 */
	private boolean consentDateIsNewer(CachedSignedPolicy signedPolicy, CachedSignedPolicy currentSignedPolicy)
	{
		return signedPolicy.getLegalConsentDate() > currentSignedPolicy.getLegalConsentDate()
				|| signedPolicy.getLegalConsentDate() == currentSignedPolicy.getLegalConsentDate() && signedPolicy.getConsentStatus().getConsentStatusType().equals(ConsentStatusType.DECLINED);
	}

	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException
	{
		EM_RWL.readLock().lock();
		try
		{
			getDomainObject(domainName);
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
			ConsentTemplateDTO result = ctList.get(0).toDTO();
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("newest consent template found: " + result);
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForSignerIds(String domainName, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownDomainException,
			UnknownSignerIdTypeException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			List<Consent> consents = getConsentsForSignerIds(domain, signerIdDTOs, useAliases);
			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
			if (LOGGER.isInfoEnabled())
			{
				StringBuilder sb = new StringBuilder("found " + result.size() + " consents for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				LOGGER.info(sb.toString());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	public ConsentDTO getCurrentConsentForSignerIdsAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber, boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException, UnknownSignerIdTypeException
	{
		ConsentDTO result = null;
		Long now = System.currentTimeMillis();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(ctKeyDTO.getDomainName());
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
								&& (temp == null || consentDates.getLegalConsentTimestamp() > temp.getConsentDateValues().getLegalConsentTimestamp()))
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

	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			List<Consent> consents = getConsentsForConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(ctKeyDTO.getDomainName()), ctKeyDTO));
			for (Consent consent : consents)
			{
				result.add(consent.toLightDTO());
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " consents for consent template " + ctKeyDTO);
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			getDomainObject(domainName);
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
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + result.size() + " consents for domain '" + domainName + "'");
			}

			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			getDomainObject(domainName);
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
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + result.size() + " consents for domain '" + domainName + "' which doesn't have a scan attached");
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, PaginationConfig config) throws InconsistentStatusException,
			InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get consents paginated for domain '" + domainName + "', with " + config);
		}
		List<ConsentLightDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			try
			{
				CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
				CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
				Root<Consent> root = criteriaQuery.from(Consent.class);
				criteriaQuery.select(root).where(generateWhereForConsent(criteriaBuilder, root, domain, config));
				Expression<?> order = generateSortExpressionForConsent(config.getSortField(), root);
				if (order != null)
				{
					if (config.isSortIsAscending())
					{
						criteriaQuery.orderBy(criteriaBuilder.asc(order));
					}
					else
					{
						criteriaQuery.orderBy(criteriaBuilder.desc(order));
					}
				}
				List<Consent> consents = em.createQuery(criteriaQuery).setFirstResult(config.getFirstEntry()).setMaxResults(config.getPageSize())
						.getResultList();

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

	public long countConsentsForDomainWithFilter(String domainName, PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("count consents for domain '" + domainName + "', with " + config.getFilter().size()
					+ " filter criterions, conjunction of filter fields = " + config.isFilterFieldsAreTreatedAsConjunction()
					+ ", filter is case sensitive = " + config.isFilterIsCaseSensitive());
		}
		long result;
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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

	private Predicate generateWhereForConsent(CriteriaBuilder criteriaBuilder, Root<Consent> root, Domain domain, PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, ObjectNotFoundException, UnknownDomainException
	{
		Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domain.getName());
		Predicate orPredicate = null;
		if (!config.getFilter().isEmpty())
		{
			for (Entry<ConsentField, String> entry : config.getFilter().entrySet())
			{
				switch (entry.getKey())
				{
					case CT_NAME:
						if (config.isFilterIsCaseSensitive())
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(
										root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), PERCENT + entry.getValue() + PERCENT));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(
											root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), PERCENT + entry.getValue() + PERCENT);
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder.like(
											root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), PERCENT + entry.getValue() + PERCENT));
								}
							}
						}
						else
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder
												.lower(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name)),
										criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name)),
											criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT)));
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate,
											criteriaBuilder.like(criteriaBuilder.lower(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name)),
													criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
								}
							}
						}
						break;
					case CT_VERSION:
						VersionConverter vc = VersionConverterCache.getCTVersionConverter(domain.getName());
						int versionToFilter = vc.stringToInt(entry.getValue());
						if (config.isFilterFieldsAreTreatedAsConjunction())
						{
							predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter));
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter);
							}
							else
							{
								orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder
										.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.version), versionToFilter));
							}
						}
						break;
					case SIGNER_ID:
						Predicate signerIdPredicate = getWhereForSignerId(criteriaBuilder, root, domain, entry.getValue(), config.isUseAliases());
						if (config.isFilterFieldsAreTreatedAsConjunction())
						{
							predicate = criteriaBuilder.and(predicate, signerIdPredicate);
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = signerIdPredicate;
							}
							else
							{
								orPredicate = criteriaBuilder.or(orPredicate, signerIdPredicate);
							}
						}
						break;
					case QC_TYPE:
						String[] qcTypes = entry.getValue().split(",");
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
						if (config.isFilterFieldsAreTreatedAsConjunction())
						{
							predicate = criteriaBuilder.and(predicate, qcPredicate);
						}
						else
						{
							if (orPredicate == null)
							{
								orPredicate = qcPredicate;
							}
							else
							{
								orPredicate = criteriaBuilder.or(orPredicate, qcPredicate);
							}
						}
						break;
					case DATE:
						LOGGER.warn("to filter the date of consents use startDate and endDate; ignored value: " + entry.getValue());
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
		Predicate datePredicate = getWhereForDate(criteriaBuilder, root, config);
		if (datePredicate != null)
		{
			if (config.isFilterFieldsAreTreatedAsConjunction())
			{
				predicate = criteriaBuilder.and(predicate, datePredicate);
			}
			else
			{
				if (orPredicate == null)
				{
					orPredicate = datePredicate;
				}
				else
				{
					orPredicate = criteriaBuilder.or(orPredicate, datePredicate);
				}
			}
		}
		Predicate typePredicate = getWhereForType(criteriaBuilder, root, domain.getName(), config);
		if (typePredicate != null)
		{
			predicate = criteriaBuilder.and(predicate, typePredicate);
		}
		if (orPredicate != null)
		{
			predicate = criteriaBuilder.and(predicate, orPredicate);
		}
		return predicate;
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
			List<ConsentTemplate> templates = listConsentTemplatesByType(domainName, config.getTemplateType());
			if (templates.isEmpty())
			{
				throw new ObjectNotFoundException();
			}
			else
			{
				Set<String> templateNames = new HashSet<>();
				for (ConsentTemplate template : templates)
				{
					templateNames.add(template.getKey().getName());
				}
				for (String templateName : templateNames)
				{
					if (result == null)
					{
						result = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), templateName);
					}
					else
					{
						result = criteriaBuilder.or(result, criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.name), templateName));
					}
				}
			}
		}
		return result;
	}

	private Predicate getWhereForSignerId(CriteriaBuilder criteriaBuilder, Root<Consent> root, Domain domain, String signerId, boolean useAliases) throws ObjectNotFoundException
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
				return criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), -1);
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
					result = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), personId);
				}
				else
				{
					result = criteriaBuilder.or(result, criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), personId));
				}
			}
		}
		catch (UnknownSignerIdTypeException e)
		{
			// impossible
			LOGGER.fatal(e);
			result = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), -1);
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

	public List<Consent> getConsentsByExternProperty(String domainName, List<StringPair> keyValueList, String operator)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException
	{
		List<Consent> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			LOGGER.info("getConsentsByExternProperty for domain: " + domainName);

			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domainName);
			criteriaQuery.select(root).where(predicate);
			List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

			Domain domainObject = getDomainObject(domainName);
			List<Module> modules = domainObject.getModules();

			Map<Module, List<SignedPolicy>> modulePolicies = new HashMap<>();
			for (Module m : modules)
			{
				List<ModulePolicy> mpList = m.getModulePolicies();

				if (!mpList.isEmpty())
				{
					Policy p = mpList.get(0).getPolicy();

					CriteriaBuilder criteriaBuilderTmp = em.getCriteriaBuilder();
					CriteriaQuery<SignedPolicy> querySp = criteriaBuilderTmp.createQuery(SignedPolicy.class);
					Root<SignedPolicy> rootSp = querySp.from(SignedPolicy.class);

					Predicate predicateKey = criteriaBuilderTmp.equal(rootSp.get(SignedPolicy_.policy).get(Policy_.key), p.getKey());

					querySp.select(rootSp).where(predicateKey);
					List<SignedPolicy> signedPolicies = em.createQuery(querySp).getResultList();
					modulePolicies.put(m, signedPolicies);
				}
			}

			ExternPropertyFilterByOIDs epf = new ExternPropertyFilterByOIDs(consents, modulePolicies, keyValueList, Operator.valueOf(operator));
			result = epf.getFilteredConsents();

			LOGGER.info("found " + result.size() + " consents");
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}

		return result;
	}

	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InconsistentStatusException
	{
		Consent result = getConsentObject(keyDTO);
		return result.toDTO();
	}

	private Consent getConsentObject(ConsentKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get " + keyDTO);
		}
		Consent result;
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(keyDTO.getConsentTemplateKey().getDomainName());
			ConsentTemplate ct = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), keyDTO.getConsentTemplateKey()));
			try
			{
				VirtualPerson vp = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, keyDTO.getSignerIds()));
				ConsentKey consentKey = new ConsentKey(ct.getKey(), keyDTO.getConsentDate(), vp.getId());
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("search " + consentKey);
				}
				result = em.find(Consent.class, consentKey);
			}
			catch (ObjectNotFoundException e)
			{
				String message = keyDTO + " not found";
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain duplicateTest = em.find(Domain.class, domainDTO.getName());
			if (duplicateTest != null)
			{
				String message = "domain with name '" + domainDTO.getName() + "' already exists";
				LOGGER.warn(message);
				throw new DuplicateEntryException(message);
			}
			if (domainDTO.getLabel() == null || domainDTO.getLabel().isEmpty())
			{
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("no label is set for new domain '" + domainDTO.getName() + "' - set it to the name of the domain");
				}
				domainDTO.setLabel(domainDTO.getName());
			}
			Domain domain = new Domain(domainDTO);
			VersionConverterCache.addDomain(domain);
			em.persist(domain);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void deleteDomain(String domainName) throws UnknownDomainException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			if (!domain.getPolicies().isEmpty() || !domain.getModules().isEmpty() || !domain.getConsentTemplates().isEmpty())
			{
				String message = "domain with name '" + domainName + "' can't be deleted - there are " + domain.getPolicies().size() + " policies, "
						+ domain.getModules().size() + " modules and " + domain.getConsentTemplates().size() + " consent templates connected to it";
				LOGGER.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(domain);
			em.flush();
			VersionConverterCache.removeDomain(domainName);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	private DomainDTO getDomainIntern(String domainName) throws UnknownDomainException
	{
		EM_RWL.readLock().lock();
		DomainDTO result = null;
		try
		{
			result = getDomainObject(domainName).toDTO();
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found domain " + result);
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	public DomainDTO getDomain(String domainName) throws UnknownDomainException
	{
		return getDomainIntern(domainName);
	}

	public List<DomainDTO> listDomains()
	{
		List<DomainDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			List<Domain> domains = listDomainObjects();
			for (Domain domain : domains)
			{
				result.add(domain.toDTO());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	private List<Domain> listDomainObjects()
	{
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Domain> criteriaQuery = criteriaBuilder.createQuery(Domain.class);
			Root<Domain> root = criteriaQuery.from(Domain.class);
			criteriaQuery.select(root);
			List<Domain> result = em.createQuery(criteriaQuery).getResultList();
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void updateDomain(DomainDTO dto) throws ObjectInUseException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString());
		}
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(dto.getName());
			List<SignerIdType> oldSignerIdTypes = domain.getSignerIdTypes();
			for (SignerIdType oldSignerIdType : oldSignerIdTypes)
			{
				em.remove(oldSignerIdType);
			}
			em.flush();
			domain.update(dto);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void updateDomainInUse(String domainName, String label, String logo, String externProperties, String comment)
			throws UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("update domain '");
			sb.append(domainName);
			sb.append("' with extern properties '");
			sb.append(externProperties);
			sb.append("', label '");
			sb.append(label);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append(logo != null && !logo.isEmpty() ? "' and a logo" : "' and no logo");
			LOGGER.debug(sb.toString());
		}
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			domain.updateInUse(label, comment, externProperties, logo);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void updatePolicy(PolicyDTO dto) throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString());
		}
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(dto.getKey().getDomainName()), dto.getKey()));
			policy.update(dto);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
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
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			policy.updateInUse(label, comment, externProperties);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void updateModule(ModuleDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException, InvalidParameterException, InvalidVersionException,
			ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString());
		}
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomainObject(dto.getKey().getDomainName());
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
				PolicyKey policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domainObject.getName()), assignedPolicyDTO.getPolicy().getKey());
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
		}
		finally
		{
			EM_RWL.writeLock().unlock();
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
		EM_RWL.writeLock().lock();
		try
		{
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			module.updateInUse(label, shortText, comment, externProperties, assignedPolicyDTOs);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void updateConsentTemplate(ConsentTemplateDTO dto, boolean finaliseRelatedEntities) throws DuplicateEntryException,
			FreeTextConverterStringException, InvalidFreeTextException, InvalidPropertiesException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString());
		}
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(dto.getKey().getDomainName()), dto.getKey()));
			Domain domainObject = getDomainObject(dto.getKey().getDomainName());
			Map<ModuleKeyDTO, Module> modules = mapModules(dto, domainObject);
			consentTemplate.update(dto, finaliseRelatedEntities);

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

			consentTemplate.getFreeTextDefs().clear();
			newFreeTextDefs.sort(new FreeTextDef.FreeTextDefComparator());
			consentTemplate.getFreeTextDefs().addAll(newFreeTextDefs);
			for (FreeTextDef unusedFreeTextDef : oldFreeTextDefs.values())
			{
				em.remove(unusedFreeTextDef);
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
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void updateConsentTemplateInUse(ConsentTemplateDTO dto) throws InvalidFreeTextException, InvalidVersionException,
			UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("update " + dto.toString() + " (updateInUse - only non-critical fields are updated)");
		}
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(dto.getKey().getDomainName()), dto.getKey()));
			consentTemplate.updateInUse(dto, true);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
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
		EM_RWL.writeLock().lock();
		try
		{
			// query previous policy status for notification
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getCurrentPolicyStates(consentKeyDTO) : null;

			getConsentObject(consentKeyDTO).updateInUse(comment, externProperties, scan);
			sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, "UpdateConsentInUse", null);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
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
		EM_RWL.writeLock().lock();
		try
		{
			Consent consent = getConsentObject(consentKeyDTO);
			// query previous policy status for notification
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getCurrentPolicyStates(consentKeyDTO) : null;
			QC oldQC = consent.getQc();
			boolean oldQcPassed = oldQC.isQcPassed();
			String domainName = consentKeyDTO.getConsentTemplateKey().getDomainName();
			Domain domainObject = getDomainObject(domainName);
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

				sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, "SetQcForConsent", null,
						Collections.singletonMap("qc", qcDTO));
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public List<QCHistoryDTO> getQCHistoryForConsent(ConsentKeyDTO consentKeyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentException
	{
		List<QCHistoryDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
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

	public void finaliseDomain(String domainName) throws UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			domain.finalise();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void finalisePolicy(PolicyKeyDTO keyDTO) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Policy policy = getPolicy(new PolicyKey(VersionConverterCache.getPolicyVersionConverter(keyDTO.getDomainName()), keyDTO));
			policy.finalise();
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void finaliseModule(ModuleKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Module module = getModule(new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO));
			try
			{
				module.finalise(finaliseRelatedEntities);
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

	public void finaliseTemplate(ConsentTemplateKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException
	{
		EM_RWL.writeLock().lock();
		try
		{
			ConsentTemplate consentTemplate = getConsentTemplate(
					new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(keyDTO.getDomainName()), keyDTO));
			try
			{
				consentTemplate.finalise(finaliseRelatedEntities);
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
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("scan added to " + consentKeyDTO);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

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

			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("scan removed from " + consentKeyDTO);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public SignerIdTypeDTO getSignerIdTypeDTO(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			return signerIdType.toDTO();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void updateSignerIdType(String domainName, String signerIdTypeName, String label, String comment) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
			signerIdType.update(label, comment);
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public List<SignerIdTypeDTO> listSignerIdTypes(String domainName) throws UnknownDomainException
	{
		List<SignerIdTypeDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			for (SignerIdType signerIdType : domain.getSignerIdTypes())
			{
				result.add(signerIdType.toDTO());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("signer id type '" + signerIdTypeName + "' successfully removed from domain '" + domainName);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException
	{
		List<String> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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

	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException,
			InvalidVersionException
	{
		List<String> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(policyKeyDTO.getDomainName());
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

	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException
	{
		validateConsentReturningObjects(consentDTO, allowRevoke);
	}

	private Map<ModuleKeyDTO, Module> validateConsentReturningObjects(ConsentDTO consentDTO, boolean allowRevoke)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException
	{
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		// module-map fuer relationen
		Map<ModuleKeyDTO, Module> result = new HashMap<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
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
				else if (consentDTO.getPatientSigningDate() == null || consentDTO.getPatientSigningDate().before(MINDATE_FOR_SIGNATURE)
						|| consentDTO.getPhysicianSigningDate() == null || consentDTO.getPhysicianSigningDate().before(MINDATE_FOR_SIGNATURE))
				{
					String message = "signature date need to be after " + MINDATE_FOR_SIGNATURE + " if at least one module is consented. "
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
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
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
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(ctKeyDTO.getDomainName());
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

			// query previous policy status for notification
			Map<PolicyKeyDTO, Boolean> previousPolicyStates = StringUtils.isNotBlank(notificationClientID) ? getCurrentPolicyStates(consentKeyDTO) : null;

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
				// relationen zu bereits vorhanden objekten nach der persistierung setzen
				virtualPerson.getConsents().add(newConsent);
				ConsentCache.addConsent(virtualPerson.getId(), convertToSignerIdKeys(domain, signerIdDTOs), getSignedPoliciesWithExpirationDates(newConsent.getSignedPolicies()), newConsent);
				sendNotification(notificationClientID, consentKeyDTO, previousPolicyStates, "RefuseConsent", null);
			}
			catch (MissingRequiredObjectException e)
			{
				String message = "MissingRequiredObjectException - should not have happened! - consent was generated";
				LOGGER.error(message, e);
				throw new InternalException(message);
			}
			catch (InvalidFreeTextException e)
			{
				String message = "InvalidFreeTextException - should not have happened! - consent was generated";
				LOGGER.error(message, e);
				throw new InternalException(message);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException, InternalException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Consent consent = getConsentObject(consentKeyDTO);
			addSignerIdToDBConsent(consent, signerIdDTO);

			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info(signerIdDTO + " added to " + consentKeyDTO);
			}
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
	}

	private void addSignerIdToDBConsent(Consent consent, SignerIdDTO signerIdDTO)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(consent.getConsentTemplate().getKey().getDomainName());
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
					LOGGER.info("consent already belongs to the given signerId");
					return;
				}
				ConsentKey possibleKey = new ConsentKey(consent.getConsentTemplate().getKey(), consent.getKey().getConsentDate(), usableVP.getId());
				if (em.find(Consent.class, possibleKey) != null)
				{
					LOGGER.info("consent already belongs to the given signerId");
					return;
				}

				ConsentDTO consentDTO = consent.toDTO();
				Map<ModuleKeyDTO, Module> modules = validateConsentReturningObjects(consentDTO, true);
				Consent newConsent = new Consent(consent.getConsentTemplate(), consentDTO, modules, usableVP, domain.getPropertiesObject());
				em.persist(newConsent);
				usableVP.getConsents().add(newConsent);
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
		finally
		{
			EM_RWL.writeLock().unlock();
		}
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info(signerIdDTO + " added to " + consent);
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

	private Map<SignedPolicy, Long> getSignedPoliciesWithExpirationDates(List<SignedPolicy> signedPolicies)
	{
		Map<SignedPolicy, Long> result = new HashMap<>();
		if (signedPolicies.isEmpty())
		{
			return result;
		}
		EM_RWL.readLock().lock();
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownDomainException, UnknownSignerIdTypeException, InternalException, InvalidParameterException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domainObject = getDomainObject(domain);
			Set<VirtualPerson> virtualPersons = getAllVirtualPersonsWithAtLeastOneSignerId(domainObject, convertToSignerIdKeys(domainObject, Collections.singleton(existentSignerIdDTO)), false);
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("found " + virtualPersons.size() + " persons for given signerId");
			}
			try
			{
				for (VirtualPerson virtualPerson : virtualPersons)
				{
					List<Consent> consents = getConsentsForPerson(domainObject, virtualPerson);
					if (LOGGER.isInfoEnabled())
					{
						LOGGER.info("found " + consents.size() + " consents for virtual person " + virtualPerson);
					}
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

	@SuppressWarnings("unchecked")
	public <T extends FhirIdDTO> T getObjectByFhirID(Class<T> clazz, String fhirID) throws UnknownFhirIdObjectException, UnknownIDException
	{
		EM_RWL.readLock().lock();
		try
		{
			Class<? extends FhirDTOExporter<T>> dbClazz = (Class<? extends FhirDTOExporter<T>>) fhirIdObjects.get(clazz.getName());
			if (dbClazz != null)
			{
				FhirDTOExporter<T> dbObject = getDTOByFhirID(dbClazz, fhirID);
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
			EM_RWL.readLock().unlock();
		}
	}

	private <T extends FhirIdDTO, U extends FhirDTOExporter<T>> U getDTOByFhirID(Class<U> clazz, String fhirID) throws UnknownIDException
	{
		U result;
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(clazz);
		Root<U> root = criteriaQuery.from(clazz);
		ParameterExpression<String> p = criteriaBuilder.parameter(String.class);
		criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("fhirID"), p));
		try
		{
			result = em.createQuery(criteriaQuery).setParameter(p, fhirID).getSingleResult();
		}
		catch (NoResultException maybe)
		{
			throw new UnknownIDException("no " + clazz.getSimpleName() + " found for fhirID " + fhirID);
		}
		return result;
	}

	public void addAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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

	public void deactivateAlias(String domainName, SignerIdDTO originalSignerId, SignerIdDTO aliasSignerId)
			throws UnknownAliasException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		EM_RWL.writeLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
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

	public List<SignerIdDTO> listAliases(String domainName, SignerIdDTO signerIdDTO)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		List<SignerIdDTO> result = new ArrayList<>();
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			SignerId signerId = getSignerId(getSignerIdType(domain, signerIdDTO.getIdType()), signerIdDTO.getId());
			List<Alias> aliases = getAliases(signerId);
			for (Alias alias : aliases)
			{
				SignerId alisId = getSignerId(alias.getKey().getAliasSignerIdKey());
				result.add(alisId.toDTO());
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	private Alias getAlias(SignerIdKey origSignerIdKey, SignerIdKey aliasSignerIdKey) throws UnknownAliasException
	{
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<Alias> getAliases(SignerId origSignerId)
	{
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private ConsentStatusType checkPolicies(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain)
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("properties for checkPolicies: ");
			sb.append(domain.getPropertiesObject().toString());
			LOGGER.debug(sb.toString());
		}
		Long now = System.currentTimeMillis();
		Long requestDate = config.getRequestDate() != null ? config.getRequestDate().getTime() : now;
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies)
		{
			// Ignore if expired before request date
			if (signedPolicy.getConsentExpirationDate() < requestDate)
			{
				LOGGER.debug("signedPolicy.getConsentExpirationDate() < requestDate (" + signedPolicy.getConsentExpirationDate() + " < " + requestDate + ")");
				continue;
			}
			// Ignore if consented after request date
			if (signedPolicy.getGicsConsentDate() > requestDate)
			{
				LOGGER.debug("signedPolicy.getGicsConsentDate() > requestDate (" + signedPolicy.getGicsConsentDate() + " > " + requestDate + ")");
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
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found the following current signed policy: " + currentSignedPolicy);
		}
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		if (currentSignedPolicy != null)
		{
			result = currentSignedPolicy.getConsentStatus().getConsentStatusType();
		}
		return result;
	}

	public ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFromString, int versionFromOffset, String versionToString, int versionToOffset, CheckConsentConfig config)
			throws InvalidVersionException, UnknownDomainException, UnknownSignerIdTypeException
	{
		EM_RWL.readLock().lock();
		try
		{
			Domain domain = getDomainObject(domainName);
			int versionFrom = 0;
			int versionTo = 0;
			if (!config.getIgnoreVersionNumber())
			{
				versionFrom = VersionConverterCache.getPolicyVersionConverter(domainName).stringToInt(versionFromString) + versionFromOffset;
				versionTo = VersionConverterCache.getPolicyVersionConverter(domainName).stringToInt(versionToString) + versionToOffset;
			}
			return getConsentStatusTypeFromTo(signerIdDTOs, domain, policyName, versionFrom, versionTo, config);
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
	 * @param domain
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 * @return
	 * @throws UnknownSignerIdTypeException
	 */
	private ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, Domain domain, String policyName, int versionFrom,
			int versionTo, CheckConsentConfig config) throws UnknownSignerIdTypeException
	{
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("get consent status for domain '");
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
			for (SignerIdDTO signerIdDTO : signerIdDTOs)
			{
				sb.append(signerIdDTO);
			}
			LOGGER.debug(sb.toString());
		}

		List<CachedSignedPolicy> cachedSignedPolicies;
		ConsentStatusType consentStatusTypeForSigner;
		switch (config.getIdMatchingType())
		{
			case AT_LEAST_ONE:
				Set<SignerIdKey> siks = getSIKsForSignerIds(domain, signerIdDTOs, config.isUseAliases());
				for (SignerIdKey sik : siks)
				{
					if (config.getIgnoreVersionNumber())
					{
						cachedSignedPolicies = ConsentCache.getCachedPoliciesForSigner(domain.getName(), sik, policyName);
					}
					else
					{
						cachedSignedPolicies = ConsentCache.getCachedPoliciesForSigner(domain.getName(), sik, policyName, versionFrom,
								versionTo);
					}
					consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
					if (LOGGER.isDebugEnabled())
					{
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
						sb.append(sik);
						sb.append(". checkPolicies result: ");
						sb.append(consentStatusTypeForSigner);
						LOGGER.debug(sb.toString());
					}
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
						cachedSignedPolicies.addAll(ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policyName));
					}
					else
					{
						cachedSignedPolicies
								.addAll(ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policyName, versionFrom, versionTo));
					}
				}
				consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
				if (LOGGER.isDebugEnabled())
				{
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
					LOGGER.debug(sb.toString());
				}
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
					cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policyName);
				}
				else
				{
					cachedSignedPolicies = ConsentCache.getCachedPoliciesForVP(domain.getName(), person, policyName, versionFrom, versionTo);
				}
				consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
				if (LOGGER.isDebugEnabled())
				{
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
					LOGGER.debug(sb.toString());
				}
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

	private List<Consent> getConsentsForSignerIds(Domain domain, Set<SignerIdDTO> signerIdDTOs, boolean useAliases) throws UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("get consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			LOGGER.debug(sb.toString());
		}
		EM_RWL.readLock().lock();
		try
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
			if (LOGGER.isDebugEnabled())
			{
				StringBuilder sb = new StringBuilder("found " + result.size() + " consents for:\n");
				for (SignerIdDTO signerId : signerIdDTOs)
				{
					sb.append(signerId + "\n");
				}
				sb.append("within " + domain);
				LOGGER.debug(sb.toString());
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<Consent> getConsentsForPerson(Domain domain, VirtualPerson person)
	{
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private List<Consent> getConsentsForConsentTemplate(ConsentTemplateKey ctKey) throws UnknownConsentTemplateException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get consents for consent template " + ctKey);
		}
		EM_RWL.readLock().lock();
		try
		{
			getConsentTemplate(ctKey);
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
			Root<Consent> root = criteriaQuery.from(Consent.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey), ctKey);
			criteriaQuery.select(root).where(predicate);
			List<Consent> result = em.createQuery(criteriaQuery).getResultList();
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + result.size() + " consents for consent template " + ctKey);
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private Domain getDomainObject(String domainName) throws UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get domain for " + domainName);
		}
		EM_RWL.readLock().lock();
		try
		{
			Domain domainObject = em.find(Domain.class, domainName);
			if (domainObject == null)
			{
				String message = "unknown domain: " + domainName;
				LOGGER.warn(message);
				throw new UnknownDomainException(message);
			}
			return domainObject;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private Policy getPolicy(PolicyKey key) throws UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get policy for " + key);
		}
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private void checkPolicyName(String policyName) throws UnknownPolicyException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("checkPolicyName for " + policyName);
		}
		EM_RWL.readLock().lock();
		try
		{
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Policy> criteriaQuery = criteriaBuilder.createQuery(Policy.class);
			Root<Policy> root = criteriaQuery.from(Policy.class);
			Predicate predicate = criteriaBuilder.equal(root.get(Policy_.key).get(PolicyKey_.name), policyName);
			criteriaQuery.select(root).where(predicate);
			List<Policy> result = new ArrayList<>();
			result = em.createQuery(criteriaQuery).getResultList();
			if (result.isEmpty())
			{
				String message = "unknown policy name: " + policyName;
				LOGGER.warn(message);
				throw new UnknownPolicyException(message);
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private Module getModule(ModuleKey key) throws UnknownModuleException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get module for " + key);
		}
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private ConsentTemplate getConsentTemplate(ConsentTemplateKey key) throws UnknownConsentTemplateException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get consent template for " + key);
		}
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private VirtualPerson getOrCreateVirtualPersonFor(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("get or create virtual person for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			LOGGER.debug(sb.toString());
		}
		VirtualPerson result;
		EM_RWL.readLock().lock();
		try
		{
			// suche nach der virtuellen person, die genau die gegebenen id umfasst
			result = findVirtualPersonWithExactTheSignerIds(domain, convertToSignerIdKeys(domain, signerIdDTOs));
		}
		catch (ObjectNotFoundException e)
		{
			EM_RWL.writeLock().lock();
			try
			{
				result = createVirtualPerson(domain, signerIdDTOs);
			}
			finally
			{
				EM_RWL.writeLock().unlock();
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
		return result;
	}

	private VirtualPerson findVirtualPersonWithExactTheSignerIds(Domain domain, Set<SignerIdKey> signerIdKeys) throws ObjectNotFoundException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("search virtual person for:\n");
			for (SignerIdKey signerId : signerIdKeys)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			LOGGER.debug(sb.toString());
		}
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
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(person + " found within " + domain);
				}
				return person;
			}
		}
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("virtual person not found within " + domain);
		}
		throw new ObjectNotFoundException();
	}

	private List<VirtualPerson> findAllVirtualPersonsWithAtLeastTheSignerIds(Domain domain, Set<SignerIdDTO> signerIdDTOs, boolean useAliases)
			throws ObjectNotFoundException, UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("search virtual persons for:\n");
			for (SignerIdDTO signerId : signerIdDTOs)
			{
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			LOGGER.debug(sb.toString());
		}
		Set<VirtualPerson> persons = getAllVirtualPersonsWithAtLeastOneSignerId(domain, convertToSignerIdKeys(domain, signerIdDTOs), useAliases);
		List<VirtualPerson> result = new ArrayList<>();
		Set<SignerId> signerIds = new HashSet<>();
		for (SignerIdDTO signerIdDTO : signerIdDTOs)
		{
			SignerIdType sit = getSignerIdType(domain, signerIdDTO.getIdType());
			signerIds.add(new SignerId(sit, signerIdDTO.getId()));
		}

		for (VirtualPerson person : persons)
		{
			Set<VirtualPersonSignerId> virtualPersonSignerIds = new HashSet<>(person.getVirtualPersonSignerIds());
			Set<SignerId> foundSignerIds = new HashSet<>();
			for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds)
			{
				foundSignerIds.add(virtualPersonSignerId.getSignerId());
			}
			boolean found = true;
			for (SignerId signerId : signerIds)
			{
				if (!foundSignerIds.contains(signerId))
				{
					found = false;
					break;
				}
			}
			if (found)
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(person + " found within " + domain);
				}
				result.add(person);
			}
		}
		if (result.isEmpty())
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("no virtual person found within " + domain);
			}
			throw new ObjectNotFoundException();
		}
		return result;
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
		VirtualPerson result = null;
		EM_RWL.writeLock().lock();
		try
		{
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
		}
		finally
		{
			EM_RWL.writeLock().unlock();
		}
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(result + " created within " + domain);
		}
		return result;
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
		EM_RWL.readLock().lock();
		try
		{
			for (SignerIdKey signerIdKey : signerIdKeys)
			{
				result.addAll(getVPsForSignerId(signerIdKey, processedKeys, useAliases));
			}
		}
		finally
		{
			EM_RWL.readLock().unlock();
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

	private SignerIdType getSignerIdType(Domain domain, String idType) throws UnknownSignerIdTypeException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get signer id type for '" + idType + "'");
		}

		for (SignerIdType signerIdType : domain.getSignerIdTypes())
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("comparing: " + signerIdType.getKey().getName() + " and " + idType);
			}
			if (signerIdType.getKey().getName().equals(idType))
			{
				return signerIdType;
			}
		}
		String message = "unknown signer id type: " + idType + " for domain " + domain.getName();
		LOGGER.warn(message);
		throw new UnknownSignerIdTypeException(message);
	}

	private SignerId getSignerId(SignerIdType signerIdType, String signerId) throws UnknownSignerIdException
	{
		return getSignerId(new SignerIdKey(signerIdType.getKey(), signerId));
	}

	private SignerId getSignerId(SignerIdKey signerIdKey) throws UnknownSignerIdException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get signer id '" + signerIdKey.getValue() + " of type " + signerIdKey.getSignerIdTypeKey());
		}
		EM_RWL.readLock().lock();
		try
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
		finally
		{
			EM_RWL.readLock().unlock();
		}
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

	public int getNextModuleVersionBetween(String domainName, String moduleName, int minVersion, int maxVersion)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get next version for module '" + moduleName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		EM_RWL.readLock().lock();
		try
		{
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
			if (modules.isEmpty())
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("no module found - take minversion as result: " + minVersion);
				}
				result = minVersion;
			}
			else
			{
				result = modules.get(0).getKey().getVersion() + 1;
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("next version found: " + result);
				}
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	public int getNextCTVersionBetween(String domainName, String ctName, int minVersion, int maxVersion)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("get next version for consent template '" + ctName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		EM_RWL.readLock().lock();
		try
		{
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
			if (cts.isEmpty())
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("no consent template found - take minversion as result: " + minVersion);
				}
				result = minVersion;
			}
			else
			{
				result = cts.get(0).getKey().getVersion() + 1;
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("next version found: " + result);
				}
			}
			return result;
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
	}

	private VirtualPerson findVPByKey(long key)
	{
		return em.find(VirtualPerson.class, key);
	}

	private SignedPolicy findSPByKey(SignedPolicyKey key)
	{
		return em.find(SignedPolicy.class, key);
	}

	// --------------------- statistic ---------------------

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
			List<DomainDTO> domains = listDomains();
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
				List<ConsentTemplateDTO> domainTemplates = listConsentTemplates(domainDTO.getName(), false);
				List<ModuleDTO> domainModules = listModules(domainDTO.getName(), false);
				allModules.addAll(domainModules);
				List<PolicyDTO> domainPolicies = listPolicies(domainDTO.getName(), false);

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
						for (PolicyDTO policy : listPolicies(domainDTO.getName(), true))
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
	public long countConsentWithScansForDomain(String domainName)
	{
		EM_RWL.readLock().lock();
		try
		{
			// weder per criteria api noch per jpl umsetzbar ...
			Query query = em.createNativeQuery(
					"SELECT COUNT(1) FROM(SELECT '' FROM consent_scan cs WHERE cs.CT_DOMAIN_NAME = ?1 GROUP BY cs.CONSENT_DATE, cs.VIRTUAL_PERSON_ID, cs.CT_VERSION, cs.CT_NAME, cs.CT_DOMAIN_NAME) x");
			query.setParameter(1, domainName);
			return (Long) query.getSingleResult();
		}
		finally
		{
			EM_RWL.readLock().unlock();
		}
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

	private void sendNotification(String clientID, ConsentKeyDTO consentKeyDTO, Map<PolicyKeyDTO, Boolean> previousPolicyStates, String event, String comment)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException
	{
		sendNotification(clientID, consentKeyDTO, previousPolicyStates, event, comment, null);
	}

	private void sendNotification(String clientID, ConsentKeyDTO consentKeyDTO, Map<PolicyKeyDTO, Boolean> previousPolicyStates, String event, String comment, Map<String, Serializable> context)
			throws UnknownSignerIdTypeException, InvalidVersionException, UnknownDomainException
	{
		if (StringUtils.isNotBlank(clientID))
		{
			notificationSender.sendNotification(new NotificationMessage(consentKeyDTO, previousPolicyStates, getCurrentPolicyStates(consentKeyDTO),
					GICS_TOOL_PREFIX + "." + event, clientID, comment, context));
		}
		else
		{
			LOGGER.debug("can't send notification because clientID is blank");
		}
	}
}
