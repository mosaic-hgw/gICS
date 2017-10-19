package org.emau.icmvc.ganimed.ttp.cm2;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MalformedCDAException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnexpectedCDAException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache.CachedSignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey_;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent_;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
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
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;

@WebService(name = "gicsService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(CM2Manager.class)
@PersistenceContext(name = "gics")
/**
 * implementation of the psn-service
 * 
 * @author geidell
 *
 */
public class CM2ManagerBean implements CM2Manager {

	// TODO klasse aufteilen, min. den cda-teil rausnehmen
	private static final Logger logger = Logger.getLogger(CM2ManagerBean.class);
	private static final String GENERATED_FROM_CDA = "generated from cda";
	// die folgenden werte muessen mit der smooks-configuration (smooks-config.xml) uebereinstimmen!
	private static final String SMOOKS_VALUES = "values";
	private static final String SMOOKS_PERSONS = "persons";
	private static final String SMOOKS_CONSENTS = "consents";
	private static final String SMOOKS_FREE_TEXTS = "free_texts";
	private static final String SMOOKS_CASE_ID = "case_id";
	private static final String SMOOKS_DOC_ID = "doc_id";
	private static final String SMOOKS_MPI = "mpi";
	private static final String SMOOKS_CONSENT_TYPE = "consent_type";
	private static final String SMOOKS_NAME = "name";
	private static final String SMOOKS_TITLE = "title";
	private static final String SMOOKS_TEXT = "text";
	private static final String SMOOKS_CONSENTED = "consented";
	private static final String SMOOKS_VERSION = "version";
	private static final String SMOOKS_DATE = "date";
	private static final String SMOOKS_SIGNATURE = "signature";
	private static final String SMOOKS_PATIENT = "patient";
	private static final String SMOOKS_PHYSICAN = "physican";
	private static final String SMOOKS_GUARDIAN = "guardian";
	private static final ConsentStatus defaultConsentStatusForCDA = ConsentStatus.UNKNOWN;
	private static final List<ConsentStatus> displayCheckboxesForCDA = Arrays.asList(ConsentStatus.ACCEPTED, ConsentStatus.DECLINED,
			ConsentStatus.UNKNOWN);
	private static final String PRAEFIX_FOR_CDA_MODULE_NAME = "dummy module for ";
	private static final Date minDateForSignature = new Date(82860000); // 02.01.1970
	// achtung, diese id-types muessen in der domain so angelegt sein
	private static final String SIGNER_ID_TYPE_CASE_ID = "case_id";
	private static final String SIGNER_ID_TYPE_MPI_ID = "mpi_id";

	@PersistenceContext
	private EntityManager em;
	private static final Object emSynchronizerDummy = new Object();
	private final Smooks smooks;
	private static final ConsentCache consentCache = new ConsentCache();

	public CM2ManagerBean() {
		Smooks temp = null;
		logger.info("constructor");
		try {
			temp = new Smooks("smooks-config.xml");
		} catch (Exception e) {
			logger.fatal("exception while configuring smooks", e);
		}
		smooks = temp;
	}

	@Override
	public void addConsentCDA(String cda, String domainName) throws MalformedCDAException, MissingRequiredObjectException, UnknownDomainException,
			InvalidVersionException, UnexpectedCDAException, DuplicateEntryException {
		if (logger.isInfoEnabled()) {
			logger.info("addConsent called with cda");
		}
		if (logger.isTraceEnabled()) {
			logger.trace(cda);
		}
		try {
			storeInfosFromCDA(cda, domainName);
			if (logger.isInfoEnabled()) {
				logger.info("addConsent with cda successfully executed");
			}
		} catch (SmooksException e) {
			String message = "exception while parsing cda: " + cda;
			logger.error(message, e);
			throw new MalformedCDAException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	private ConsentTransferObject parseViaSmooksCDA(String cda) throws SmooksException {
		if (logger.isDebugEnabled()) {
			logger.debug("parsing cda");
		}
		ConsentTransferObject result = new ConsentTransferObject(new HashMap<String, Object>(), new HashMap<String, HashMap<String, Object>>(),
				new ArrayList<HashMap<String, Object>>(), new ArrayList<HashMap<String, Object>>());
		try {
			ExecutionContext executionContext = smooks.createExecutionContext();
			JavaResult smooksResult = new JavaResult();
			// try {
			// einkommentieren, wenn das extrahieren unten nicht klappt - legt eine sehr gute report-datei an
			// executionContext.setEventListener(new HtmlReportGenerator("c:/smooks-report.html"));
			// } catch (IOException e) {
			// logger.error(e);
			// }
			// hier werden die daten aus dem xml extrahiert - siehe smooks-config.xml
			smooks.filterSource(executionContext, new StreamSource(new StringReader(cda)), smooksResult);
			logger.debug("cda parsed");
			HashMap<String, Object> values = (HashMap<String, Object>) smooksResult.getBean(SMOOKS_VALUES);
			HashMap<String, HashMap<String, Object>> persons = (HashMap<String, HashMap<String, Object>>) smooksResult.getBean(SMOOKS_PERSONS);
			ArrayList<HashMap<String, Object>> consents = (ArrayList<HashMap<String, Object>>) smooksResult.getBean(SMOOKS_CONSENTS);
			ArrayList<HashMap<String, Object>> freeTexts = (ArrayList<HashMap<String, Object>>) smooksResult.getBean(SMOOKS_FREE_TEXTS);
			result = new ConsentTransferObject(values, persons, consents, freeTexts);
			if (logger.isDebugEnabled()) {
				logger.debug(result);
			}
		} finally {
			smooks.close();
		}
		return result;
	}

	private void storeInfosFromCDA(String cda, String domainName) throws UnknownDomainException, MissingRequiredObjectException,
			InvalidVersionException, UnexpectedCDAException, DuplicateEntryException {
		ConsentTransferObject cdaInfos = parseViaSmooksCDA(cda);
		if (logger.isDebugEnabled()) {
			logger.debug("store consent for case id " + cdaInfos.getValues().get(SMOOKS_CASE_ID));
		}
		ConsentTemplate ct = findOrCreateBasicObjectsCDA(cdaInfos, domainName);

		storeConsentCDA(cdaInfos, ct);
	}

	private void storeConsentCDA(ConsentTransferObject cdaInfos, ConsentTemplate ct) throws UnexpectedCDAException, DuplicateEntryException,
			MissingRequiredObjectException {
		if (logger.isDebugEnabled()) {
			logger.debug("store consent infos");
		}
		try {
			Set<SignerIdDTO> signerIds = new HashSet<SignerIdDTO>();
			SignerIdDTO signerIdDTO = new SignerIdDTO(SIGNER_ID_TYPE_CASE_ID, (String) cdaInfos.getValues().get(SMOOKS_CASE_ID));
			signerIds.add(signerIdDTO);
			signerIdDTO = new SignerIdDTO(SIGNER_ID_TYPE_MPI_ID, (String) cdaInfos.getValues().get(SMOOKS_MPI));
			signerIds.add(signerIdDTO);
			ConsentKeyDTO consentKeyDTO = new ConsentKeyDTO(ct.getKey().toDTO(ct.getDomain().getCTVersionConverterInstance()), signerIds,
					(Date) cdaInfos.getValues().get(SMOOKS_DATE));
			ConsentDTO consentDTO = new ConsentDTO(consentKeyDTO);
			consentDTO.setPatientSigningDate((Date) cdaInfos.getPersons().get(SMOOKS_PATIENT).get(SMOOKS_DATE));
			consentDTO.setPatientSignatureBase64((String) cdaInfos.getPersons().get(SMOOKS_PATIENT).get(SMOOKS_SIGNATURE));
			if ((consentDTO.getPatientSignatureBase64() == null || consentDTO.getPatientSignatureBase64().trim().isEmpty())
					&& cdaInfos.getPersons().get(SMOOKS_GUARDIAN) != null) {
				consentDTO.setPatientSigningDate((Date) cdaInfos.getPersons().get(SMOOKS_GUARDIAN).get(SMOOKS_DATE));
				consentDTO.setPatientSignatureBase64((String) cdaInfos.getPersons().get(SMOOKS_GUARDIAN).get(SMOOKS_SIGNATURE));
				consentDTO.setPatientSignatureIsFromGuardian(true);
			}
			consentDTO.setPhysicanSigningDate((Date) cdaInfos.getPersons().get(SMOOKS_PHYSICAN).get(SMOOKS_DATE));
			consentDTO.setPhysicanSignatureBase64((String) cdaInfos.getPersons().get(SMOOKS_PHYSICAN).get(SMOOKS_SIGNATURE));
			consentDTO.setPhysicanId((String) cdaInfos.getValues().get(SMOOKS_DOC_ID));
			Map<String, String> freeTextVals = new HashMap<String, String>();
			for (HashMap<String, Object> freetext : cdaInfos.getFreeTexts()) {
				freeTextVals.put((String) freetext.get(SMOOKS_NAME), (String) freetext.get(SMOOKS_TEXT));
			}
			consentDTO.setFreeTextVals(freeTextVals);
			HashMap<ModuleKeyDTO, ConsentStatus> moduleStates = new HashMap<ModuleKeyDTO, ConsentStatus>();
			VersionConverter moduleVC = ct.getDomain().getModuleVersionConverterInstance();
			for (HashMap<String, Object> consent : cdaInfos.getConsents()) {
				String moduleName = PRAEFIX_FOR_CDA_MODULE_NAME + (String) consent.get(SMOOKS_NAME);
				String version = null;
				for (ModuleConsentTemplate mct : ct.getModuleConsentTemplates()) {
					if (mct.getModule().getKey().getName().equals(moduleName)) {
						version = moduleVC.intToString(mct.getModule().getKey().getVersion());
						break;
					}
				}
				ModuleKeyDTO moduleKeyDTO = new ModuleKeyDTO(ct.getKey().getDomainName(), moduleName, version);
				ConsentStatus consentStatus = null;
				String consentStatusCDA = (String) consent.get(SMOOKS_CONSENTED);
				if ("accepted".equals(consentStatusCDA)) {
					consentStatus = ConsentStatus.ACCEPTED;
				} else if ("declined".equals(consentStatusCDA)) {
					consentStatus = ConsentStatus.DECLINED;
				} else if ("unknown".equals(consentStatusCDA)) {
					consentStatus = ConsentStatus.UNKNOWN;
				} else if ("not_asked".equals(consentStatusCDA)) {
					consentStatus = ConsentStatus.NOT_ASKED;
				} else {
					String message = "missing consent status for " + moduleKeyDTO;
					logger.error(message);
					throw new MissingRequiredObjectException(message);
				}
				moduleStates.put(moduleKeyDTO, consentStatus);
			}
			consentDTO.setModuleStates(moduleStates);
			addConsent(consentDTO);
			if (logger.isDebugEnabled()) {
				logger.debug("consent infos stored");
			}
		} catch (DuplicateEntryException e) {
			throw e;
		} catch (MissingRequiredObjectException e) {
			throw e;
		} catch (Exception shouldNotHappen) {
			logger.error("unexpected exception", shouldNotHappen);
			throw new UnexpectedCDAException("unexpected exception", shouldNotHappen);
		}
	}

	private ConsentTemplate findOrCreateBasicObjectsCDA(ConsentTransferObject cdaInfos, String domainName) throws MissingRequiredObjectException,
			UnknownDomainException, InvalidVersionException, UnexpectedCDAException {
		if (logger.isDebugEnabled()) {
			logger.debug("find or create basic objects from cda");
		}
		ConsentTemplate result = null;
		Domain domain = getDomainObject(domainName);
		try {
			VersionConverter policyVC = domain.getPolicyVersionConverterInstance();
			VersionConverter moduleVC = domain.getModuleVersionConverterInstance();
			VersionConverter ctVC = domain.getCTVersionConverterInstance();
			List<Module> modules = new ArrayList<Module>();
			if (cdaInfos.getConsents().isEmpty()) {
				throw new MissingRequiredObjectException("at least one '<consent>' part is required within the given cda");
			}
			synchronized (emSynchronizerDummy) {
				for (HashMap<String, Object> consent : cdaInfos.getConsents()) {
					synchronized (emSynchronizerDummy) {
						Policy policy = getOrCreatePolicyCDA(domain, policyVC, consent);
						modules.add(getOrCreateModuleCDA(policy, policyVC, moduleVC, consent));
					}
				}
				result = getOrCreateConsentTemplateCDA(modules, moduleVC, ctVC, cdaInfos.getFreeTexts(),
						(String) cdaInfos.getValues().get(SMOOKS_CONSENT_TYPE));
			}
		} catch (VersionConverterClassException shouldNotHappen) {
			logger.error("unexpected exception", shouldNotHappen);
			throw new UnexpectedCDAException("unexpected exception", shouldNotHappen);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("find or create basic objects from cda done");
		}
		return result;
	}

	private Policy getOrCreatePolicyCDA(Domain domain, VersionConverter policyVC, HashMap<String, Object> consent) throws InvalidVersionException,
			UnknownDomainException, VersionConverterClassException, UnexpectedCDAException {
		Policy policy = null;
		PolicyKeyDTO policyKeyDTO = new PolicyKeyDTO(domain.getName(), (String) consent.get(SMOOKS_NAME), (String) consent.get(SMOOKS_VERSION));
		if (logger.isDebugEnabled()) {
			logger.debug("find or create " + policyKeyDTO);
		}
		PolicyKey policyKey = new PolicyKey(policyVC, policyKeyDTO);
		try {
			policy = getPolicy(policyKey);
			if (logger.isDebugEnabled()) {
				logger.debug("policy found");
			}
		} catch (UnknownPolicyException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("policy not found - create it");
			}
			PolicyDTO policyDTO = new PolicyDTO(policyKeyDTO);
			policyDTO.setComment(GENERATED_FROM_CDA);
			try {
				addPolicy(policyDTO);
				policy = getPolicy(policyKey);
			} catch (DuplicateEntryException shouldNotHappen) {
				String message = "a duplicate policy, which wasn't found a second before";
				logger.warn(message, shouldNotHappen);
				throw new UnexpectedCDAException(message, shouldNotHappen);
			} catch (UnknownPolicyException shouldNotHappen) {
				String message = "a policy which was added just before the search can't be found: " + policyKey;
				logger.error(message, shouldNotHappen);
				throw new UnexpectedCDAException(message, shouldNotHappen);
			}
		}
		return policy;
	}

	private Module getOrCreateModuleCDA(Policy policy, VersionConverter policyVC, VersionConverter moduleVC, HashMap<String, Object> consent) {
		if (logger.isDebugEnabled()) {
			logger.debug("find or create module for " + policy.getKey());
		}
		Module result = null;
		String moduleName = PRAEFIX_FOR_CDA_MODULE_NAME + policy.getKey().getName();
		String text = (String) consent.get(SMOOKS_TEXT);
		String title = (String) consent.get(SMOOKS_TITLE);
		String policyVersion = "";
		// hier koennen mehrere module existieren, da in gani_forms nicht bei jeder textaenderung die version erhoeht wurde ...
		int minVersion;
		String nextPolicyVersion = "";
		int maxVersion;
		try {
			policyVersion = policyVC.intToString(policy.getKey().getVersion());
			minVersion = moduleVC.stringToInt(policyVersion);
			nextPolicyVersion = policyVC.intToString(policy.getKey().getVersion() + 1);
			maxVersion = moduleVC.stringToInt(nextPolicyVersion) - 1;
			List<Module> modules = policy.getModules();
			for (Module module : modules) {
				if (moduleName.equals(module.getKey().getName()) && minVersion <= module.getKey().getVersion()
						&& maxVersion >= module.getKey().getVersion() && module.getText().getText().equals(text)) {
					if (logger.isDebugEnabled()) {
						logger.debug("module found");
					}
					result = module;
					break;
				}
			}
			if (result == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("module not found - create it");
				}
				int nextModuleVersion = getNextModuleVersion(policy.getDomain().getName(), moduleName, minVersion, maxVersion);
				result = new Module(policy.getDomain(), moduleName, nextModuleVersion, text, title, GENERATED_FROM_CDA, "");
				em.persist(result);
				// relationen nach persist
				policy.getDomain().getModules().add(result);
				result.getPolicies().add(policy);
				policy.getModules().add(result);
				if (logger.isDebugEnabled()) {
					logger.debug("module created: " + result);
				}
			}
		} catch (InvalidVersionException shouldNotHappen) {
			logger.fatal("unexpected exception", shouldNotHappen);
		}
		return result;
	}

	private int getNextModuleVersion(String domainName, String moduleName, int minVersion, int maxVersion) {
		if (logger.isDebugEnabled()) {
			logger.debug("get next version for module '" + moduleName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Module> criteriaQuery = criteriaBuilder.createQuery(Module.class);
		Root<Module> root = criteriaQuery.from(Module.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(Module_.key).get(ModuleKey_.name), moduleName),
				criteriaBuilder.equal(root.get(Module_.key).get(ModuleKey_.domainName), domainName));
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get(Module_.key).get(ModuleKey_.version), minVersion, maxVersion));
		criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.desc(root.get(Module_.key).get(ModuleKey_.version)));
		List<Module> resultList = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
		int result;
		if (resultList.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("no module found - take minversion as result: " + minVersion);
			}
			result = minVersion;
		} else {
			result = resultList.get(0).getKey().getVersion() + 1;
			if (logger.isDebugEnabled()) {
				logger.debug("next version found: " + result);
			}
		}
		return result;
	}

	private ConsentTemplate getOrCreateConsentTemplateCDA(List<Module> modules, VersionConverter moduleVC, VersionConverter ctVC,
			ArrayList<HashMap<String, Object>> freeTexts, String ctName) throws UnexpectedCDAException {
		// sucht das ct, welches alle gegebenen module enthaelt; legt ein neues an, wenn keins vorhanden
		if (logger.isDebugEnabled()) {
			logger.debug("find or create consent module for " + modules.size() + " modules");
		}
		ConsentTemplate result = null;
		List<ModuleKey> moduleKeysForTest = new ArrayList<ModuleKey>();
		for (Module module : modules) {
			moduleKeysForTest.add(module.getKey());
		}
		// alle cts, die am ersten modul haengen
		List<ModuleConsentTemplate> moduleCTs = modules.get(0).getModuleConsentTemplates();
		for (ModuleConsentTemplate modCT : moduleCTs) {
			int countCorrectModules = 0;
			// das template muss genau die uebergebenen module enthalten
			for (ModuleConsentTemplate derivedModCT : modCT.getConsentTemplate().getModuleConsentTemplates()) {
				if (!moduleKeysForTest.contains(derivedModCT.getKey().getModuleKey())) {
					countCorrectModules = 0;
					break;
				}
				countCorrectModules++;
			}
			if (countCorrectModules == moduleKeysForTest.size()) {
				result = modCT.getConsentTemplate();
				if (logger.isDebugEnabled()) {
					logger.debug("consent template found");
				}
				break;
			}
		}
		if (result == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("consent template not found - create it");
			}
			try {
				Domain domain = modules.get(0).getDomain();
				// ct-version gibt's in gani_forms nicht, daher einfach 1.0, 1.1, ...
				int minVersion = ctVC.stringToInt("1");
				int maxVersion = ctVC.stringToInt("2") - 1;
				int nextCTVersion = getNextCTVersion(domain.getName(), ctName, minVersion, maxVersion);
				String nextCTVersionString = ctVC.intToString(nextCTVersion);
				List<AssignedModuleDTO> assignedModuleDTOs = new ArrayList<AssignedModuleDTO>();
				Map<ModuleKeyDTO, Module> modulesForCT = new HashMap<ModuleKeyDTO, Module>();
				for (Module module : modules) {
					assignedModuleDTOs.add(new AssignedModuleDTO(module.toDTO(), false, defaultConsentStatusForCDA, displayCheckboxesForCDA, 0, null,
							"", "")); // TODO orderNumber noch mit ins CDA?
					modulesForCT.put(module.getKey().toDTO(moduleVC), module);
				}
				ConsentTemplateKeyDTO ctKeyDTO = new ConsentTemplateKeyDTO(domain.getName(), ctName, nextCTVersionString);
				ConsentTemplateDTO ctDTO = new ConsentTemplateDTO(ctKeyDTO);
				ctDTO.setAssignedModules(assignedModuleDTOs);
				ctDTO.setComment(GENERATED_FROM_CDA);
				List<FreeTextDefDTO> freeTextDefs = new ArrayList<FreeTextDefDTO>();
				for (HashMap<String, Object> freeTextMap : freeTexts) {
					freeTextDefs.add(new FreeTextDefDTO((String) freeTextMap.get(SMOOKS_NAME), false, FreeTextType.String, null, GENERATED_FROM_CDA));
				}
				ctDTO.setFreeTextDefs(freeTextDefs);
				result = new ConsentTemplate(domain, ctDTO, modulesForCT);
				em.persist(result);
				// relationen nach persist
				result.getDomain().getConsentTemplates().add(result);
				if (logger.isDebugEnabled()) {
					logger.debug("consent template created: " + result);
				}
			} catch (Exception shouldNotHappen) {
				logger.error("unexpected exception", shouldNotHappen);
				throw new UnexpectedCDAException("unexpected exception", shouldNotHappen);
			}
		}
		return result;
	}

	private int getNextCTVersion(String domainName, String ctName, int minVersion, int maxVersion) {
		if (logger.isDebugEnabled()) {
			logger.debug("get next version for consent template '" + ctName + "' within domain '" + domainName + "' between " + minVersion + " and "
					+ maxVersion);
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
		Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name), ctName),
				criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName));
		predicate = criteriaBuilder.and(predicate,
				criteriaBuilder.between(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version), minVersion, maxVersion));
		criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.desc(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version)));
		List<ConsentTemplate> resultList = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
		int result;
		if (resultList.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("no consent template found - take minversion as result: " + minVersion);
			}
			result = minVersion;
		} else {
			result = resultList.get(0).getKey().getVersion() + 1;
			if (logger.isDebugEnabled()) {
				logger.debug("next version found: " + result);
			}
		}
		return result;
	}

	/*
	 * struktur ist durch smooks bedingt
	 */
	private final class ConsentTransferObject {
		private final HashMap<String, Object> values;
		private final HashMap<String, HashMap<String, Object>> persons;
		private final ArrayList<HashMap<String, Object>> consents;
		private final ArrayList<HashMap<String, Object>> freeTexts;

		public ConsentTransferObject(HashMap<String, Object> values, HashMap<String, HashMap<String, Object>> persons,
				ArrayList<HashMap<String, Object>> consents, ArrayList<HashMap<String, Object>> freeTexts) {
			super();
			this.values = values;
			this.persons = persons;
			this.consents = consents;
			this.freeTexts = freeTexts;
		}

		public HashMap<String, Object> getValues() {
			return values;
		}

		public HashMap<String, HashMap<String, Object>> getPersons() {
			return persons;
		}

		public ArrayList<HashMap<String, Object>> getConsents() {
			return consents;
		}

		public ArrayList<HashMap<String, Object>> getFreeTexts() {
			return freeTexts;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("consent transfer object with:\n");
			sb.append("patient - signature date: " + persons.get(SMOOKS_PATIENT).get(SMOOKS_DATE) + "\n");
			if (persons.get(SMOOKS_GUARDIAN) != null) {
				sb.append("guardian - signature date: " + persons.get(SMOOKS_GUARDIAN).get(SMOOKS_DATE) + "\n");
			}
			sb.append("doc - signature date: " + persons.get(SMOOKS_PHYSICAN).get(SMOOKS_DATE) + "\n");
			sb.append("found " + consents.size() + " consent(s)\n");
			sb.append("freetexts for:\n");
			for (HashMap<String, Object> freeText : freeTexts) {
				sb.append("'" + freeText.get(SMOOKS_NAME) + "' ");
			}
			for (HashMap<String, Object> consent : consents) {
				sb.append("policy name: " + consent.get(SMOOKS_NAME) + "\n");
				sb.append("consent title: " + consent.get(SMOOKS_TITLE) + "\n");
				sb.append("consented: " + consent.get(SMOOKS_CONSENTED) + "\n");
			}
			sb.append("and the following values:\n");
			for (Entry<String, Object> entry : values.entrySet()) {
				sb.append(entry.getKey() + " - " + entry.getValue() + "\n");
			}
			return sb.toString();
		}
	}

	@Override
	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException {
		if (logger.isInfoEnabled()) {
			logger.info("try to add " + consentDTO);
		}
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
		ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(),
				consentKeyDTO.getConsentTemplateKey()));
		Map<ModuleKeyDTO, Module> modules = validateConsent(consentDTO, consentTemplate, true);

		synchronized (emSynchronizerDummy) {
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
			synchronized (consentCache) {
				consentCache.addConsent(consentDTO.getKey().getSignerIds(), newConsent.getSignedPolicies());
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info(consentDTO + " added");
		}
	}

	private Map<ModuleKeyDTO, Module> validateConsent(ConsentDTO consentDTO, ConsentTemplate consentTemplate, boolean allowRevoke)
			throws InvalidVersionException, VersionConverterClassException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException {
		Domain domain = consentTemplate.getDomain();
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		List<ModuleConsentTemplate> moduleConsentTemplates = consentTemplate.getModuleConsentTemplates();
		List<ModuleKeyDTO> validModulesForTemplate = new ArrayList<ModuleKeyDTO>();
		// test, ob fuer alle modules ein consent status gesetzt wurde
		// gleichzeitig auf mandatory-felder-logik pruefen
		boolean mandatoryFieldsTrue = !allowRevoke; // wenn widerruf nicht zugelassen werden soll, einfach so tun, als waere einem mandatory-feld zugestimmt
		boolean mandatoryFieldsFalse = false;
		boolean optionalFieldsTrue = false;
		for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates) {
			ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getKey().getModuleKey().toDTO(domain.getModuleVersionConverterInstance());
			validModulesForTemplate.add(moduleKeyDTO);
			ConsentStatus consentStatus = consentDTO.getModuleStates().get(moduleKeyDTO);
			if (consentStatus == null) {
				String message = "there's no consent status set for " + moduleKeyDTO;
				logger.error(message);
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
					logger.error(message);
					throw new MandatoryFieldsException(message);
				}
			} else if (csType.equals(ConsentStatusType.ACCEPTED)) {
				optionalFieldsTrue = true;
			}
			// test fuer parent regeln
			if (moduleConsentTemplate.getParent() != null && csType.equals(ConsentStatusType.ACCEPTED)) {
				ConsentStatus parentConsentStatus = consentDTO.getModuleStates().get(
						moduleConsentTemplate.getParent().getKey().toDTO(domain.getModuleVersionConverterInstance()));
				if (parentConsentStatus == null || !ConsentStatusType.ACCEPTED.equals(parentConsentStatus.getConsentStatusType())) {
					String message = "the consent status of the parent module (" + moduleConsentTemplate.getParent().getKey()
							+ ") also need to be one of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
							+ " if the consent status of the child module (" + moduleKeyDTO + ") is one of them";
					logger.error(message);
					throw new MandatoryFieldsException(message);
				}
			}
		}
		if (mandatoryFieldsFalse && mandatoryFieldsTrue) {
			String message = "mandatory modules cannot have a consent status of " + ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED)
					+ " and one of " + ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + " within one consent";
			logger.error(message);
			throw new MandatoryFieldsException(message);
		} else if (mandatoryFieldsFalse && optionalFieldsTrue) {
			String message = "if at least one mandatory module have a consent status of "
					+ ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + ", all optional modules may not have a consent status of "
					+ ConsentStatus.getStringForType(ConsentStatusType.ACCEPTED);
			logger.error(message);
			throw new MandatoryFieldsException(message);
		}

		if (optionalFieldsTrue || mandatoryFieldsTrue) {
			if (!domain.getPropertyObjects().isNoMandatoryScans()
					&& (consentDTO.getScanBase64() == null || consentDTO.getScanBase64().isEmpty())
					&& (consentDTO.getPatientSignatureBase64() == null || consentDTO.getPhysicanSignatureBase64() == null
							|| consentDTO.getPatientSignatureBase64().trim().isEmpty() || consentDTO.getPhysicanSignatureBase64().trim().isEmpty())) {
				String message = "signatures of both the patient and a physican are mandatory if at least one module is consented. "
						+ consentDTO.getKey();
				logger.error(message);
				throw new MissingRequiredObjectException(message);
			} else if (consentDTO.getPatientSigningDate().before(minDateForSignature)
					|| consentDTO.getPhysicanSigningDate().before(minDateForSignature)) {
				String message = "signature date need to be after " + minDateForSignature + " if at least one module is consented. "
						+ consentDTO.getKey();
				logger.error(message);
				throw new MissingRequiredObjectException(message);
			} else if (consentDTO.getKey().getConsentDate().before(minDateForSignature)) {
				String message = "consent date need to be after " + minDateForSignature + " if at least one module is consented. "
						+ consentDTO.getKey();
				logger.error(message);
				throw new MissingRequiredObjectException(message);
			}
		}

		// module-map fuer relationen
		Map<ModuleKeyDTO, Module> modules = new HashMap<ModuleKeyDTO, Module>();
		for (ModuleKeyDTO moduleKeyDTO : consentDTO.getModuleStates().keySet()) {
			if (!validModulesForTemplate.contains(moduleKeyDTO)) {
				String message = "the given " + moduleKeyDTO + " is not part of the given " + consentKeyDTO.getConsentTemplateKey();
				logger.error(message);
				throw new UnknownModuleException(message);
			}
			ModuleKey moduleKey = new ModuleKey(domain.getModuleVersionConverterInstance(), moduleKeyDTO);
			modules.put(moduleKeyDTO, getModule(moduleKey));
		}
		return modules;
	}

	@Override
	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config) throws UnknownDomainException,
			UnknownPolicyException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusType(signerIdDTOs, policyKeyDTO, config));
	}

	@PostConstruct
	private void checkConsentCache() {
		synchronized (emSynchronizerDummy) {
			synchronized (consentCache) {
				if (!consentCache.isInitialised()) {
					logger.info("initialise consent cache");
					consentCache.init(em);
					logger.info("consent cache initialised");
				}
			}
		}
	}

	private ConsentStatusType checkPolicies(CheckConsentConfig config, List<CachedSignedPolicy> signedPolicies, Domain domain) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("properties for checkPolicies: ");
			sb.append(domain.getPropertyObjects().toString());
			logger.debug(sb.toString());
		}
		CachedSignedPolicy currentSignedPolicy = null;
		for (CachedSignedPolicy signedPolicy : signedPolicies) {
			if (domain.getPropertyObjects().isPermanentRevoke()) {
				if (signedPolicy.getConsentStatus().getConsentStatusType().equals(ConsentStatusType.DECLINED)
						|| (config.getUnknownStateIsConsideredAsDecline() && signedPolicy.getConsentStatus().getConsentStatusType()
								.equals(ConsentStatusType.UNKNOWN))) {
					currentSignedPolicy = signedPolicy;
					break;
				}
			}
			if (signedPolicy.getConsentStatus().getConsentStatusType().equals(ConsentStatusType.UNKNOWN)) {
				continue;
			}
			if (domain.getPropertyObjects().isTakeHighestVersion() && (currentSignedPolicy == null || signedPolicy.getVersion() > currentSignedPolicy.getVersion())) {
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

	@Override
	public boolean isConsentedFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusTypeFromIncludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom,
				versionTo, config));
	}

	@Override
	public boolean isConsentedFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusTypeFromExcludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom,
				versionTo, config));
	}

	@Override
	public boolean isConsentedFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusTypeFromIncludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom,
				versionTo, config));
	}

	@Override
	public boolean isConsentedFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		return ConsentStatusType.ACCEPTED.equals(getConsentStatusTypeFromExcludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom,
				versionTo, config));
	}

	@Override
	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("get consent status type for ");
			sb.append(policyKeyDTO);
			sb.append(" signed by:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("using ");
			sb.append(config);
			logger.debug(sb.toString());
		}
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		Domain domain = getDomainObject(policyKeyDTO.getDomainName());
		PolicyKey policyKey = new PolicyKey(domain.getPolicyVersionConverterInstance(), policyKeyDTO);
		getPolicy(policyKey);
		for (SignerIdDTO signerIdDTO : signerIdDTOs) {
			getSignerIdType(domain, signerIdDTO.getIdType());
			List<CachedSignedPolicy> cachedSignedPolicies = null;
			if (config.getIgnoreVersionNumber()) {
				synchronized (consentCache) {
					cachedSignedPolicies = consentCache.getCachedPoliciesFor(domain.getName(), signerIdDTO, policyKey.getName());
				}
			} else {
				synchronized (consentCache) {
					cachedSignedPolicies = consentCache.getCachedPoliciesFor(domain.getName(), signerIdDTO, policyKey.getName(),
							policyKey.getVersion(), policyKey.getVersion());
				}
			}
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("found ");
				sb.append(cachedSignedPolicies.size());
				sb.append(" signed consents for ");
				sb.append(policyKeyDTO);
				sb.append(" signed by: ");
				sb.append(signerIdDTO);
				logger.debug(sb.toString());
			}
			ConsentStatusType consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
			if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED)) {
				result = consentStatusTypeForSigner;
			} else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED)) {
				result = consentStatusTypeForSigner;
				break;
			}
		}
		return result;
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException, UnknownSignerIdTypeException {
		Domain domain = getDomainObject(domainName);
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, domain.getPolicyVersionConverterInstance().stringToInt(versionFrom),
				domain.getPolicyVersionConverterInstance().stringToInt(versionTo), config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException, UnknownSignerIdTypeException {
		Domain domain = getDomainObject(domainName);
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName,
				domain.getPolicyVersionConverterInstance().stringToInt(versionFrom) + 1,
				domain.getPolicyVersionConverterInstance().stringToInt(versionTo), config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException, UnknownSignerIdTypeException {
		Domain domain = getDomainObject(domainName);
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName, domain.getPolicyVersionConverterInstance().stringToInt(versionFrom),
				domain.getPolicyVersionConverterInstance().stringToInt(versionTo) - 1, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException, UnknownSignerIdTypeException {
		Domain domain = getDomainObject(domainName);
		return getConsentStatusTypeFromTo(signerIdDTOs, domainName, policyName,
				domain.getPolicyVersionConverterInstance().stringToInt(versionFrom) + 1,
				domain.getPolicyVersionConverterInstance().stringToInt(versionTo) - 1, config);
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
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 */
	private ConsentStatusType getConsentStatusTypeFromTo(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, int versionFrom,
			int versionTo, CheckConsentConfig config) throws UnknownDomainException, UnknownSignerIdTypeException {
		Domain domain = getDomainObject(domainName);
		ConsentStatusType result = ConsentStatusType.UNKNOWN;
		for (SignerIdDTO signerIdDTO : signerIdDTOs) {
			getSignerIdType(domain, signerIdDTO.getIdType());
			List<CachedSignedPolicy> cachedSignedPolicies = null;
			if (config.getIgnoreVersionNumber()) {
				synchronized (consentCache) {
					cachedSignedPolicies = consentCache.getCachedPoliciesFor(domain.getName(), signerIdDTO, policyName);
				}
			} else {
				synchronized (consentCache) {
					cachedSignedPolicies = consentCache.getCachedPoliciesFor(domain.getName(), signerIdDTO, policyName, versionFrom, versionTo);
				}
			}
			ConsentStatusType consentStatusTypeForSigner = checkPolicies(config, cachedSignedPolicies, domain);
			if (consentStatusTypeForSigner.equals(ConsentStatusType.ACCEPTED)) {
				result = consentStatusTypeForSigner;
			} else if (consentStatusTypeForSigner.equals(ConsentStatusType.DECLINED)) {
				result = consentStatusTypeForSigner;
				break;
			}
		}
		return result;
	}

	@Override
	public List<ConsentTemplateDTO> listConsentTemplates(String domainName) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list consent templates for domain " + domainName);
		}
		Domain domainObject = getDomainObject(domainName);
		List<ConsentTemplate> consentTemplates = domainObject.getConsentTemplates();
		List<ConsentTemplateDTO> result = new ArrayList<ConsentTemplateDTO>();
		for (ConsentTemplate consentTemplate : consentTemplates) {
			result.add(consentTemplate.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " consent templates");
		}
		return result;
	}

	@Override
	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO) throws UnknownDomainException, UnknownModuleException,
			DuplicateEntryException, VersionConverterClassException, InvalidVersionException, FreeTextConverterStringException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + consentTemplateDTO);
		}
		Domain domainObject = getDomainObject(consentTemplateDTO.getKey().getDomainName());
		ConsentTemplateKey key = new ConsentTemplateKey(domainObject.getCTVersionConverterInstance(), consentTemplateDTO.getKey());
		synchronized (emSynchronizerDummy) {
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
				ModuleKey moduleKey = new ModuleKey(domainObject.getModuleVersionConverterInstance(), moduleKeyDTO);
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
			ConsentTemplate consentTemplate = new ConsentTemplate(domainObject, consentTemplateDTO, modules);
			em.persist(consentTemplate);
			// relationen nach persist
			domainObject.getConsentTemplates().add(consentTemplate);
		}
		if (logger.isInfoEnabled()) {
			logger.info(consentTemplateDTO + " added");
		}
	}

	@Override
	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			ObjectInUseException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("delete " + keyDTO);
		}
		Domain domain = getDomainObject(keyDTO.getDomainName());
		synchronized (emSynchronizerDummy) {
			ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(), keyDTO));
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
			domain.getConsentTemplates().remove(consentTemplate);
			for (ModuleConsentTemplate moduleConsentTemplate : consentTemplate.getModuleConsentTemplates()) {
				moduleConsentTemplate.getModule().getModuleConsentTemplates().remove(moduleConsentTemplate);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public List<ModuleDTO> listModules(String domainName) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list modules for domain " + domainName);
		}
		Domain domainObject = getDomainObject(domainName);
		List<Module> modules = domainObject.getModules();
		List<ModuleDTO> result = new ArrayList<ModuleDTO>();
		for (Module module : modules) {
			result.add(module.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " modules");
		}
		return result;
	}

	@Override
	public void addModule(ModuleDTO moduleDTO) throws UnknownDomainException, UnknownPolicyException, DuplicateEntryException,
			VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + moduleDTO);
		}
		Domain domainObject = getDomainObject(moduleDTO.getKey().getDomainName());
		ModuleKey key = new ModuleKey(domainObject.getModuleVersionConverterInstance(), moduleDTO.getKey());
		synchronized (emSynchronizerDummy) {
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
				PolicyKey policyKey = new PolicyKey(domainObject.getPolicyVersionConverterInstance(), policyDTO.getKey());
				Policy policy = getPolicy(policyKey);
				policyNames.add(policyName);
				policies.add(policy);
			}
			Module module = new Module(domainObject, moduleDTO);
			em.persist(module);
			// relationen nach persist
			domainObject.getModules().add(module);
			module.getPolicies().addAll(policies);
			for (Policy policy : policies) {
				policy.getModules().add(module);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info(moduleDTO + " added");
		}
	}

	@Override
	public void deleteModule(ModuleKeyDTO keyDTO) throws UnknownDomainException, UnknownModuleException, ObjectInUseException,
			VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("delete " + keyDTO);
		}
		Domain domain = getDomainObject(keyDTO.getDomainName());
		synchronized (emSynchronizerDummy) {
			Module module = getModule(new ModuleKey(domain.getModuleVersionConverterInstance(), keyDTO));
			if (module.getModuleConsentTemplates().size() > 0) {
				String message = module + " is in use and therefore can't be deleted";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(module);
			module.getDomain().getModules().remove(module);
			for (Policy policy : module.getPolicies()) {
				policy.getModules().remove(module);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public List<PolicyDTO> listPolicies(String domainName) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list policies for domain " + domainName);
		}
		Domain domainObject = getDomainObject(domainName);
		List<Policy> policies = domainObject.getPolicies();
		List<PolicyDTO> result = new ArrayList<PolicyDTO>();
		for (Policy policy : policies) {
			result.add(policy.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " policies");
		}
		return result;
	}

	@Override
	public void addPolicy(PolicyDTO policyDTO) throws UnknownDomainException, DuplicateEntryException, VersionConverterClassException,
			InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + policyDTO);
		}
		Domain domainObject = getDomainObject(policyDTO.getKey().getDomainName());
		PolicyKey key = new PolicyKey(domainObject.getPolicyVersionConverterInstance(), policyDTO.getKey());
		synchronized (emSynchronizerDummy) {
			Policy duplicateTest = em.find(Policy.class, key);
			if (duplicateTest != null) {
				String message = key + " already exists";
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
			Policy policy = new Policy(domainObject, policyDTO);
			em.persist(policy);
			domainObject.getPolicies().add(policy);
		}
		if (logger.isInfoEnabled()) {
			logger.info(policyDTO + " added");
		}
	}

	@Override
	public void deletePolicy(PolicyKeyDTO keyDTO) throws UnknownDomainException, UnknownPolicyException, ObjectInUseException,
			VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("delete " + keyDTO);
		}
		Domain domain = getDomainObject(keyDTO.getDomainName());
		synchronized (emSynchronizerDummy) {
			Policy policy = getPolicy(new PolicyKey(domain.getPolicyVersionConverterInstance(), keyDTO));
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
			policy.getDomain().getPolicies().remove(policy);
		}
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO) throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException,
			InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get policy dto for " + keyDTO);
		}
		Domain domain = getDomainObject(keyDTO.getDomainName());
		Policy result = getPolicy(new PolicyKey(domain.getPolicyVersionConverterInstance(), keyDTO));
		return result.toDTO();
	}

	@Override
	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO) throws UnknownDomainException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get consent template dto for " + keyDTO);
		}
		Domain domain = getDomainObject(keyDTO.getDomainName());
		ConsentTemplate result = getConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(), keyDTO));
		return result.toDTO();
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForSigner(String domainName, Set<SignerIdDTO> signerIdDTOs) throws UnknownDomainException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		Domain domain = getDomainObject(domainName);
		List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
		for (Consent consent : consents) {
			ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(domain.getCTVersionConverterInstance(), consent.getVirtualPerson());
			for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
				PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(domain.getPolicyVersionConverterInstance());
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
		return result;
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSigner(PolicyKeyDTO policyKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("for " + policyKeyDTO);
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		Domain domain = getDomainObject(policyKeyDTO.getDomainName());
		List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
		for (Consent consent : consents) {
			ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(domain.getCTVersionConverterInstance(), consent.getVirtualPerson());
			for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
				PolicyKeyDTO currentPolicyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(domain.getPolicyVersionConverterInstance());
				if (currentPolicyKeyDTO.equals(policyKeyDTO)) {
					SignedPolicyDTO signedPolicyDTO = new SignedPolicyDTO(signedPolicy.getStatus(), currentPolicyKeyDTO, consentKeyDTO);
					result.add(signedPolicyDTO);
				}
			}
		}
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("found " + result.size() + " policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("for " + policyKeyDTO);
			logger.info(sb.toString());
		}
		return result;
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSigner(String domainName, String policyName, Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("for policy name '" + policyName + "' within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = new ArrayList<SignedPolicyDTO>();
		Domain domain = getDomainObject(domainName);
		List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
		for (Consent consent : consents) {
			ConsentKeyDTO consentKeyDTO = consent.getKey().toDTO(domain.getCTVersionConverterInstance(), consent.getVirtualPerson());
			for (SignedPolicy signedPolicy : consent.getSignedPolicies()) {
				if (signedPolicy.getKey().getPolicyKey().getName().equals(policyName)) {
					PolicyKeyDTO policyKeyDTO = signedPolicy.getKey().getPolicyKey().toDTO(domain.getPolicyVersionConverterInstance());
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
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		Set<VirtualPerson> signers = getVirtualPersons(domain, signerIdDTOs);
		List<Consent> result = new ArrayList<Consent>();
		for (VirtualPerson signer : signers) {
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.virtualPersonId), signer.getId()),
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
	}

	private List<Consent> getConsentsForConsentTemplate(ConsentTemplateKey ctKey) throws UnknownDomainException, UnknownConsentTemplateException {
		if (logger.isDebugEnabled()) {
			logger.debug("get consents for consent template " + ctKey);
		}
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
	}

	private Domain getDomainObject(String domainName) throws UnknownDomainException {
		if (logger.isDebugEnabled()) {
			logger.debug("get domain for " + domainName);
		}
		Domain domainObject = em.find(Domain.class, domainName);
		if (domainObject == null) {
			String message = "unknown domain: " + domainName;
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return domainObject;
	}

	private Policy getPolicy(PolicyKey key) throws UnknownPolicyException {
		if (logger.isDebugEnabled()) {
			logger.debug("get policy for " + key);
		}
		Policy result = em.find(Policy.class, key);
		if (result == null) {
			String message = "unknown policy: " + key;
			logger.error(message);
			throw new UnknownPolicyException(message);
		}
		return result;
	}

	private Module getModule(ModuleKey key) throws UnknownModuleException {
		if (logger.isDebugEnabled()) {
			logger.debug("get module for " + key);
		}
		Module result = em.find(Module.class, key);
		if (result == null) {
			String message = "unknown module: " + key;
			logger.error(message);
			throw new UnknownModuleException(message);
		}
		return result;
	}

	private ConsentTemplate getConsentTemplate(ConsentTemplateKey key) throws UnknownConsentTemplateException {
		if (logger.isDebugEnabled()) {
			logger.debug("get consent template for " + key);
		}
		ConsentTemplate result = em.find(ConsentTemplate.class, key);
		if (result == null) {
			String message = "unknown consent template: " + key;
			logger.error(message);
			throw new UnknownConsentTemplateException(message);
		}
		return result;
	}

	@Override
	public ConsentTemplateDTO getNewestConsentTemplate(String consentTemplateName, String domainName) throws UnknownDomainException,
			UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get newest consent template of '" + consentTemplateName + "' for domain '" + domainName + "'");
		}
		getDomainObject(domainName);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
		Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.name), consentTemplateName),
				criteriaBuilder.equal(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.domainName), domainName));
		criteriaQuery.select(root).where(predicate).orderBy(criteriaBuilder.desc(root.get(ConsentTemplate_.key).get(ConsentTemplateKey_.version)));
		List<ConsentTemplate> ctList = em.createQuery(criteriaQuery).setMaxResults(1).getResultList();
		if (ctList.isEmpty()) {
			String message = "unknown consent template with name '" + consentTemplateName + "' for domain '" + domainName + "'";
			logger.error(message);
			throw new UnknownConsentTemplateException(message);
		}
		ConsentTemplateDTO result = ctList.get(0).toDTO();
		return result;
	}

	@Override
	public List<ConsentDTO> getAllConsentsForPerson(String domainName, Set<SignerIdDTO> signerIdDTOs) throws UnknownDomainException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get all consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<ConsentDTO> result = new ArrayList<ConsentDTO>();
		Domain domain = getDomainObject(domainName);
		List<Consent> consents = getConsentsForPerson(domain, signerIdDTOs);
		for (Consent consent : consents) {
			result.add(consent.toDTO());
		}
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("found " + result.size() + " consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within " + domain);
			logger.info(sb.toString());
		}
		return result;
	}

	@Override
	public List<ConsentDTO> getAllConsentsForConsentTemplate(ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get all consents for consent template '" + ctKeyDTO);
		}
		List<ConsentDTO> result = new ArrayList<ConsentDTO>();
		Domain domain = getDomainObject(ctKeyDTO.getDomainName());
		List<Consent> consents = getConsentsForConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(), ctKeyDTO));
		for (Consent consent : consents) {
			result.add(consent.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " consents for consent template " + ctKeyDTO);
		}
		return result;
	}

	@Override
	public List<ConsentDTO> getAllConsentsForDomain(String domainName) throws UnknownDomainException, VersionConverterClassException,
			InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get all consents for domain '" + domainName + "'");
		}
		List<ConsentDTO> result = new ArrayList<ConsentDTO>();
		getDomainObject(domainName);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domainName);
		criteriaQuery.select(root).where(predicate);
		List<Consent> consents = em.createQuery(criteriaQuery).getResultList();

		for (Consent consent : consents) {
			result.add(consent.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " consents for domain '" + domainName + "'");
		}
		return result;
	}

	@Override
	public List<ConsentDTO> getConsentsForDomainPaginated(String domainName, int pageNumber, int pageSize, ConsentField consentField,
			boolean sortIsAscending) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get consents for domain '" + domainName + "', page " + pageNumber + ", page size " + pageSize);
		}
		List<ConsentDTO> result = new ArrayList<ConsentDTO>();
		getDomainObject(domainName);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		Predicate predicate = criteriaBuilder.equal(root.get(Consent_.key).get(ConsentKey_.ctKey).get(ConsentTemplateKey_.domainName), domainName);
		criteriaQuery.select(root).where(predicate);
		Expression<?> order = null;
		switch (consentField) {
			case SCAN:
				order = root.get(Consent_.scanBase64).get(Text_.text);
			break;
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
				logger.warn("unimplemented ConsentField '" + consentField.name() + "' for order-by-clause within getConsentsForDomainPaginated()");
			break;
		}
		if (order != null) {
			if (sortIsAscending) {
				criteriaQuery.orderBy(criteriaBuilder.asc(order));
			} else {
				criteriaQuery.orderBy(criteriaBuilder.desc(order));
			}
		}
		List<Consent> consents = em.createQuery(criteriaQuery).setFirstResult(pageNumber * pageSize).setMaxResults(pageSize).getResultList();

		for (Consent consent : consents) {
			result.add(consent.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " consents for domain " + domainName);
		}
		return result;
	}

	@Override
	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException {
		if (logger.isInfoEnabled()) {
			logger.info("try to create " + domainDTO);
		}
		synchronized (emSynchronizerDummy) {
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
			em.persist(new Domain(domainDTO));
		}
		if (logger.isInfoEnabled()) {
			logger.info(domainDTO + " created");
		}
	}

	@Override
	public void deleteDomain(String domainName) throws ObjectInUseException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("try to delete domain with name " + domainName);
		}
		Domain domain = getDomainObject(domainName);
		if (domain.getPolicies().size() > 0 || domain.getConsentTemplates().size() > 0) {
			String message = "domain with name '" + domainName + "' can't be deleted - it there are " + domain.getPolicies().size()
					+ " policies and " + domain.getConsentTemplates().size() + " consent templates connected to it";
			logger.error(message);
			throw new ObjectInUseException(message);
		}
		synchronized (emSynchronizerDummy) {
			em.remove(domain);
		}
		if (logger.isInfoEnabled()) {
			logger.info("domain with name '" + domainName + "' deleted");
		}
	}

	@Override
	public DomainDTO getDomain(String domainName) throws UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("get domain with name " + domainName);
		}
		DomainDTO result = getDomainObject(domainName).toDTO();
		if (logger.isInfoEnabled()) {
			logger.info("found domain " + result);
		}
		return result;
	}

	@Override
	public List<DomainDTO> listDomains() {
		if (logger.isInfoEnabled()) {
			logger.info("list all domains");
		}
		List<DomainDTO> result = new ArrayList<DomainDTO>();

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Domain> criteriaQuery = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> root = criteriaQuery.from(Domain.class);
		criteriaQuery.select(root);
		List<Domain> domains = em.createQuery(criteriaQuery).getResultList();
		for (Domain domain : domains) {
			result.add(domain.toDTO());
		}
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public void updateDomain(String domainName, String label, String properties, String comment) throws UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("try to update domain '" + domainName + "' with label '" + label + "', properties '" + properties + "' and comment '"
					+ comment + "'");
		}
		Domain domain = getDomainObject(domainName);
		domain.setLabel(label);
		domain.setPropertiesString(properties);
		domain.setComment(comment);
		synchronized (emSynchronizerDummy) {
			em.persist(domain);
		}
		if (logger.isInfoEnabled()) {
			logger.info("domain '" + domainName + "' updated");
		}
	}

	@Override
	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType) throws UnknownDomainException,
			UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException, VersionConverterClassException,
			InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			logger.info("try to add scan to " + consentKeyDTO);
		}
		Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
		ConsentTemplate ct = getConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(), consentKeyDTO.getConsentTemplateKey()));
		Consent consent;
		try {
			VirtualPerson vp = findVirtualPerson(domain, consentKeyDTO.getSignerIds());
			ConsentKey consentKey = new ConsentKey(ct, consentKeyDTO.getConsentDate(), vp.getId());
			if (logger.isDebugEnabled()) {
				logger.debug("search " + consentKey);
			}
			consent = em.find(Consent.class, consentKey);
		} catch (ObjectNotFoundException e) {
			String message = consentKeyDTO + " not found";
			logger.warn(message);
			throw new UnknownConsentException(message);
		}
		if (consent == null) {
			String message = consentKeyDTO + " not found";
			logger.warn(message);
			throw new UnknownConsentException(message);
		}
		if (consent.getScanBase64() != null && consent.getScanBase64().getText() != null) {
			String message = "there's already a scan attached to " + consentKeyDTO;
			logger.warn(message);
			throw new DuplicateEntryException(message);
		}
		consent.setScanBase64(scanBase64, fileType);
		if (logger.isInfoEnabled()) {
			logger.info("scan added to " + consentKeyDTO);
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
		try {
			// suche nach der virtuellen person, die genau die gegebenen id umfasst
			result = findVirtualPerson(domain, signerIdDTOs);
		} catch (ObjectNotFoundException e) {
			result = createVirtualPerson(domain, signerIdDTOs);
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

	private VirtualPerson findVirtualPerson(Domain domain, Set<SignerIdDTO> signerIdDTOs) throws ObjectNotFoundException,
			UnknownSignerIdTypeException {
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
		synchronized (emSynchronizerDummy) {
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
			if (signerIdType.getKey().getName().equals(idType)) {
				return signerIdType;
			}
		}
		String message = "unknown signer id type: " + idType;
		logger.error(message);
		throw new UnknownSignerIdTypeException(message);
	}

	@Override
	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException {
		if (logger.isInfoEnabled()) {
			logger.info("add signer id type '" + signerIdTypeName + "' to domain '" + domainName);
		}
		Domain domain = getDomainObject(domainName);
		SignerIdType signerIdType = new SignerIdType(domain, signerIdTypeName);
		synchronized (emSynchronizerDummy) {
			if (domain.getSignerIdTypes().contains(signerIdType)) {
				String message = "signer id type '" + signerIdTypeName + "' already exists within " + domain;
				logger.warn(message);
				throw new DuplicateEntryException(message);
			}
		}
		synchronized (emSynchronizerDummy) {
			em.persist(signerIdType);
			// relationen nach persistierung
			domain.getSignerIdTypes().add(signerIdType);
		}
		if (logger.isInfoEnabled()) {
			logger.info("signer id type '" + signerIdTypeName + "' successfully added to domain '" + domainName);
		}
	}

	@Override
	public void deleteSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, UnknownSignerIdTypeException,
			ObjectInUseException {
		if (logger.isInfoEnabled()) {
			logger.info("remove signer id type '" + signerIdTypeName + "' from domain '" + domainName);
		}
		Domain domain = getDomainObject(domainName);
		SignerIdType signerIdType = getSignerIdType(domain, signerIdTypeName);
		synchronized (emSynchronizerDummy) {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<SignerId> root = criteriaQuery.from(SignerId.class);
			Predicate predicate = criteriaBuilder.equal(root.get(SignerId_.key).get(SignerIdKey_.signerIdTypeKey), signerIdType.getKey());
			criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
			Long count = em.createQuery(criteriaQuery).getSingleResult();
			if (count > 0) {
				String message = "signer id type '" + signerIdTypeName + "' for " + domain + " is in use";
				logger.error(message);
				throw new ObjectInUseException(message);
			}
			em.remove(signerIdType);
			domain.getSignerIdTypes().remove(signerIdType);
			if (logger.isInfoEnabled()) {
				logger.info("signer id type '" + signerIdTypeName + "' successfully removed from domain '" + domainName);
			}
			return;
		}
	}

	@Override
	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException,
			UnknownSignerIdTypeException {
		List<String> result = new ArrayList<String>();
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
		return result;
	}

	@Override
	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, VersionConverterClassException,
			InvalidVersionException, InconsistentStatusException {
		List<String> result = new ArrayList<String>();
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
		return result;
	}

	@Override
	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke) throws InvalidVersionException, VersionConverterClassException,
			MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("validate " + consentDTO);
		}
		ConsentKeyDTO consentKeyDTO = consentDTO.getKey();
		Domain domain = getDomainObject(consentKeyDTO.getConsentTemplateKey().getDomainName());
		ConsentTemplate consentTemplate = getConsentTemplate(new ConsentTemplateKey(domain.getCTVersionConverterInstance(),
				consentKeyDTO.getConsentTemplateKey()));
		validateConsent(consentDTO, consentTemplate, allowRevoke);
		if (logger.isInfoEnabled()) {
			logger.info(consentDTO + " is valide");
		}
	}
}
