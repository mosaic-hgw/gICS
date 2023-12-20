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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplateKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.QC;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdTypeKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPersonSignerId;

/**
 * cache for consents
 *
 * @author geidell
 *
 */
public class ConsentCache
{
	private static final Logger LOGGER = LogManager.getLogger(ConsentCache.class);
	private static final ConsentCache INSTANCE = new ConsentCache();
	private static final int SIGNED_POLICIES_PAGE_SIZE = 100000;
	private static volatile boolean INITIALISED = false;
	private static final ReentrantReadWriteLock CACHE_RWL = new ReentrantReadWriteLock();
	// domain -> signerId -> signedPolicies
	private static final Map<String, Map<SignerIdKey, List<CachedSignedPolicy>>> SIGNER_CACHE = new HashMap<>();
	private static final Map<String, Map<Long, List<CachedSignedPolicy>>> VP_CACHE = new HashMap<>();
	// in order to safe memory
	private static final Map<SignerIdKey, SignerIdKey> SIGNER_ID_KEY_CACHE = new HashMap<>();
	private static final Map<SignerIdTypeKey, SignerIdTypeKey> SIGNER_ID_TYPE_KEY_CACHE = new HashMap<>();
	private static final Map<SignedPolicyKey, SignedPolicyKey> SIGNED_POLICY_KEY_CACHE = new HashMap<>();
	private static final Map<ConsentKey, ConsentKey> CONSENT_KEY_CACHE = new HashMap<>();
	private static final Map<ConsentTemplateKey, ConsentTemplateKey> CONSENT_TEMPLATE_KEY_CACHE = new HashMap<>();
	private static final Map<PolicyKey, PolicyKey> POLICY_KEY_CACHE = new HashMap<>();

	private ConsentCache()
	{}

	public static boolean isInitialised()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return INITIALISED;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void init(EntityManager em)
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (INITIALISED)
			{
				return;
			}
			List<VirtualPersonSignerId> signerIds = getAllSignerIds(em);
			em.clear(); // wichtig! eclipselink laedt sonst die ganzen objekte (vor allem consents) nach
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + signerIds.size() + " signer ids, sorting them now");
			}
			Map<Long, Set<SignerIdKey>> mappedSignerIds = mapSignerIds(signerIds);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("signer ids sorted, found entries for " + mappedSignerIds.size() + " virtual persons");
			}
			// signed policies chunked to save memory
			long signedPoliciesCount = getSignedPoliciesCount(em);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + signedPoliciesCount + " signed policies, sorting them now");
			}
			List<ConsentTemplate> cts = getAllCTs(em);
			Map<ConsentTemplateKey, ConsentTemplate> ctMap = new HashMap<>();
			for (ConsentTemplate ct : cts)
			{
				ctMap.put(ct.getKey(), ct);
			}
			List<Consent> consents = getAllConsents(em);
			Map<ConsentKey, ConsentDateValues> consentDates = new HashMap<>();
			Map<ConsentKey, Long> gicsConsentDates = new HashMap<>(2);
			Map<ConsentKey, Long> legalConsentDates = new HashMap<>(2);
			for (Consent consent : consents)
			{
				if (!consent.getQc().isQcPassed())
				{
					continue;
				}
				ConsentDateValues consentDateValues = consent.getConsentDateValues();
				consentDates.put(consent.getKey(), consentDateValues);
				gicsConsentDates.put(consent.getKey(), consentDateValues.getGicsConsentTimestamp());
				legalConsentDates.put(consent.getKey(), consentDateValues.getLegalConsentTimestamp());
			}
			for (int i = 0; i * SIGNED_POLICIES_PAGE_SIZE < signedPoliciesCount; i++)
			{
				int nextPageSize = (int) ((i + 1) * SIGNED_POLICIES_PAGE_SIZE < signedPoliciesCount ? SIGNED_POLICIES_PAGE_SIZE : signedPoliciesCount - i * SIGNED_POLICIES_PAGE_SIZE);
				if (nextPageSize > 0)
				{
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("loading consent information " + i * SIGNED_POLICIES_PAGE_SIZE + " - " + (i * SIGNED_POLICIES_PAGE_SIZE + nextPageSize));
					}
					List<SignedPolicy> signedPolicies = getSignedPolicies(em, i * SIGNED_POLICIES_PAGE_SIZE, nextPageSize);
					em.clear(); // wichtig! eclipselink laedt sonst die ganzen objekte (vor allem consents) nach
					// key = vpId
					Map<Long, List<SignedPolicy>> mappedSignedPolicies = mapSignedPolicies(signedPolicies);
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("sorted entries for " + mappedSignedPolicies.size() + " virtual persons");
					}
					long counter = 0;
					for (Entry<Long, Set<SignerIdKey>> entry : mappedSignerIds.entrySet())
					{
						List<SignedPolicy> signedPoliciesToProcess = mappedSignedPolicies.get(entry.getKey());
						if (signedPoliciesToProcess != null)
						{
							Map<SignedPolicy, Long> signedPoliciesWithExpiration = getSignedPoliciesWithExpirationDates(signedPoliciesToProcess, ctMap, consentDates);
							addPolicies(entry.getKey(), entry.getValue(), signedPoliciesWithExpiration, gicsConsentDates, legalConsentDates);
							counter += signedPoliciesWithExpiration.size();
						}
					}
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("added " + counter + " consent information to cache");
					}
				}
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("cache filled with " + signedPoliciesCount + " consent informations");
			}
			INITIALISED = true;
		}
		finally
		{
			em.clear();
			CACHE_RWL.writeLock().unlock();
		}
	}

	private static List<ConsentTemplate> getAllCTs(EntityManager em)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConsentTemplate> criteriaQuery = criteriaBuilder.createQuery(ConsentTemplate.class);
		Root<ConsentTemplate> root = criteriaQuery.from(ConsentTemplate.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private static List<Consent> getAllConsents(EntityManager em)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Consent> criteriaQuery = criteriaBuilder.createQuery(Consent.class);
		Root<Consent> root = criteriaQuery.from(Consent.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private static List<VirtualPersonSignerId> getAllSignerIds(EntityManager em)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<VirtualPersonSignerId> criteriaQuery = criteriaBuilder.createQuery(VirtualPersonSignerId.class);
		Root<VirtualPersonSignerId> root = criteriaQuery.from(VirtualPersonSignerId.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private static Map<Long, Set<SignerIdKey>> mapSignerIds(List<VirtualPersonSignerId> virtualPersonSignerIds)
	{
		Map<Long, Set<SignerIdKey>> result = new HashMap<>();
		for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds)
		{
			Long vpId = virtualPersonSignerId.getKey().getVpId();
			Set<SignerIdKey> signerIdsForVP = result.get(vpId);
			if (signerIdsForVP == null)
			{
				signerIdsForVP = new HashSet<>();
				result.put(vpId, signerIdsForVP);
			}
			signerIdsForVP.add(virtualPersonSignerId.getKey().getSignerIdKey());
		}
		return result;
	}

	private static long getSignedPoliciesCount(EntityManager em)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(SignedPolicy.class)));
		Query query = em.createQuery(criteriaQuery);
		return (long) query.getSingleResult();
	}

	private static List<SignedPolicy> getSignedPolicies(EntityManager em, int startPosition, int maxResults)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SignedPolicy> criteriaQuery = criteriaBuilder.createQuery(SignedPolicy.class);
		Root<SignedPolicy> root = criteriaQuery.from(SignedPolicy.class);
		criteriaQuery.select(root);
		TypedQuery<SignedPolicy> query = em.createQuery(criteriaQuery);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	private static Map<Long, List<SignedPolicy>> mapSignedPolicies(List<SignedPolicy> signedPolicies)
	{
		Map<Long, List<SignedPolicy>> result = new HashMap<>();
		for (SignedPolicy signedPolicy : signedPolicies)
		{
			Long vpId = signedPolicy.getKey().getConsentKey().getVirtualPersonId();
			List<SignedPolicy> signedPoliciesForVP = result.get(vpId);
			if (signedPoliciesForVP == null)
			{
				signedPoliciesForVP = new ArrayList<>();
				result.put(vpId, signedPoliciesForVP);
			}
			signedPoliciesForVP.add(signedPolicy);
		}
		return result;
	}

	private static Map<SignedPolicy, Long> getSignedPoliciesWithExpirationDates(List<SignedPolicy> signedPolicies, Map<ConsentTemplateKey, ConsentTemplate> ctMap,
			Map<ConsentKey, ConsentDateValues> consentDates)
	{
		Map<SignedPolicy, Long> result = new HashMap<>();
		for (SignedPolicy signedPolicy : signedPolicies)
		{
			ConsentKey consentKey = signedPolicy.getKey().getConsentKey();
			// if no valid consentDate found (because of qcStatus), skip
			if (!consentDates.containsKey(consentKey))
			{
				continue;
			}
			try
			{
				result.put(signedPolicy, consentDates.get(consentKey).getTimestampForPolicy(signedPolicy.getKey().getPolicyKey()));
			}
			catch (UnknownPolicyException impossible)
			{
				LOGGER.fatal("impossible exception while calculating signed policy expiration dates", impossible);
			}
		}
		return result;
	}

	public static void addConsent(Long id, Set<SignerIdKey> signerIdKeys, Map<SignedPolicy, Long> signedPoliciesWithExpirationDates, Consent consent)
	{
		Map<ConsentKey, Long> gicsConsentDates = new HashMap<>(2);
		Map<ConsentKey, Long> legalConsentDates = new HashMap<>(2);
		gicsConsentDates.put(consent.getKey(), consent.getConsentDateValues().getGicsConsentTimestamp());
		legalConsentDates.put(consent.getKey(), consent.getConsentDateValues().getLegalConsentTimestamp());
		addPolicies(id, signerIdKeys, signedPoliciesWithExpirationDates, gicsConsentDates, legalConsentDates);
	}

	public static void addPolicies(Long vpId, Set<SignerIdKey> signerIdKeys, Map<SignedPolicy, Long> signedPoliciesWithExpirationDates, Map<ConsentKey, Long> gicsConsentDates,
			Map<ConsentKey, Long> legalConsentDates)
	{
		if (signedPoliciesWithExpirationDates == null || signedPoliciesWithExpirationDates.isEmpty() || signerIdKeys == null || signerIdKeys.isEmpty())
		{
			return;
		}
		CACHE_RWL.writeLock().lock();
		try
		{
			// QC
			SignedPolicy firstPolicyKey = signedPoliciesWithExpirationDates.keySet().iterator().next();
			QC qc = firstPolicyKey.getConsent().getQc();
			if (!qc.isQcPassed())
			{
				return;
			}
			String domainName = firstPolicyKey.getKey().getPolicyKey().getDomainName();
			Map<SignerIdKey, List<CachedSignedPolicy>> domainSignerCache = SIGNER_CACHE.get(domainName);
			if (domainSignerCache == null)
			{
				domainSignerCache = new HashMap<>();
				SIGNER_CACHE.put(domainName.intern(), domainSignerCache);
			}
			for (SignerIdKey signerIdKey : signerIdKeys)
			{
				if (domainSignerCache.get(signerIdKey) == null)
				{
					signerIdKey = getCachedSignerIdKey(signerIdKey);
					domainSignerCache.put(signerIdKey, new ArrayList<>());
				}
			}
			Map<Long, List<CachedSignedPolicy>> domainVPCache = VP_CACHE.get(domainName);
			if (domainVPCache == null)
			{
				domainVPCache = new HashMap<>();
				VP_CACHE.put(domainName.intern(), domainVPCache);
			}
			List<CachedSignedPolicy> vpSignedPolicies = domainVPCache.get(vpId);
			if (vpSignedPolicies == null)
			{
				vpSignedPolicies = new ArrayList<>();
				domainVPCache.put(vpId, vpSignedPolicies);
			}
			// objekte nur einmal erzeugen, deswegen neue schleife
			for (Entry<SignedPolicy, Long> entry : signedPoliciesWithExpirationDates.entrySet())
			{
				ConsentKey consentKey = entry.getKey().getKey().getConsentKey();
				if (entry.getValue() != null)
				{
					SignedPolicyKey cachedKey = getCachedSignedPolicyKey(entry.getKey().getKey());
					CachedSignedPolicy cachedSignedPolicy = INSTANCE.new CachedSignedPolicy(cachedKey, gicsConsentDates.get(consentKey), legalConsentDates.get(consentKey),
							entry.getKey().getStatus(), entry.getValue());
					for (SignerIdKey signerId : signerIdKeys)
					{
						domainSignerCache.get(signerId).add(cachedSignedPolicy);
					}
					vpSignedPolicies.add(cachedSignedPolicy);
				}
				else
				{
					LOGGER.fatal("inconsistent data - signed policy isn't part of template: " + entry.getKey());
				}
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	private static SignerIdKey getCachedSignerIdKey(SignerIdKey signerIdKey)
	{
		SignerIdKey result = SIGNER_ID_KEY_CACHE.get(signerIdKey);
		if (result == null)
		{
			SignerIdTypeKey typeKey = getCachedSignerIdTypeKey(signerIdKey.getSignerIdTypeKey());
			result = new SignerIdKey(typeKey, signerIdKey.getValue());
			SIGNER_ID_KEY_CACHE.put(result, result);
		}
		return result;
	}

	private static SignerIdTypeKey getCachedSignerIdTypeKey(SignerIdTypeKey signerIdTypeKey)
	{
		SignerIdTypeKey result = SIGNER_ID_TYPE_KEY_CACHE.get(signerIdTypeKey);
		if (result == null)
		{
			result = new SignerIdTypeKey(signerIdTypeKey.getDomainName().intern(), signerIdTypeKey.getName().intern());
			SIGNER_ID_TYPE_KEY_CACHE.put(result, result);
		}
		return result;
	}

	private static SignedPolicyKey getCachedSignedPolicyKey(SignedPolicyKey key)
	{
		SignedPolicyKey result = SIGNED_POLICY_KEY_CACHE.get(key);
		if (result == null)
		{
			String domainName = key.getPolicyKey().getDomainName().intern();
			ConsentTemplateKey ctKey = getCachedConsentTemplateKey(domainName, key.getConsentKey().getCtKey());
			ConsentKey cKey = getCachedConsentKey(ctKey, key.getConsentKey());
			PolicyKey pKey = getCachedPolicyKey(domainName, key.getPolicyKey());
			result = new SignedPolicyKey(cKey, pKey);
			SIGNED_POLICY_KEY_CACHE.put(result, result);
		}
		return result;
	}

	private static ConsentTemplateKey getCachedConsentTemplateKey(String domainName, ConsentTemplateKey ctKey)
	{
		ConsentTemplateKey result = CONSENT_TEMPLATE_KEY_CACHE.get(ctKey);
		if (result == null)
		{
			result = new ConsentTemplateKey(domainName, ctKey.getName().intern(), ctKey.getVersion());
			CONSENT_TEMPLATE_KEY_CACHE.put(result, result);
		}
		return result;
	}

	private static ConsentKey getCachedConsentKey(ConsentTemplateKey ctKey, ConsentKey consentKey)
	{
		ConsentKey result = CONSENT_KEY_CACHE.get(consentKey);
		if (result == null)
		{
			result = new ConsentKey(ctKey, new Date(consentKey.getConsentDate().getTime()), consentKey.getVirtualPersonId());
			CONSENT_KEY_CACHE.put(result, result);
		}
		return result;
	}

	private static PolicyKey getCachedPolicyKey(String domainName, PolicyKey policyKey)
	{
		PolicyKey result = POLICY_KEY_CACHE.get(policyKey);
		if (result == null)
		{
			result = new PolicyKey(domainName, policyKey.getName().intern(), policyKey.getVersion());
			POLICY_KEY_CACHE.put(result, result);
		}
		return result;
	}

	public static void removeConsent(Consent consent, Set<SignerIdKey> signerIdKeys, Map<SignedPolicy, Long> signedPoliciesWithExpirationDates, String domainName)
	{
		if (signedPoliciesWithExpirationDates == null || signedPoliciesWithExpirationDates.isEmpty() || signerIdKeys == null || signerIdKeys.isEmpty())
		{
			return;
		}
		CACHE_RWL.writeLock().lock();
		try
		{
			Map<SignerIdKey, List<CachedSignedPolicy>> domainSignerCache = SIGNER_CACHE.get(domainName);
			Map<Long, List<CachedSignedPolicy>> domainVPCache = VP_CACHE.get(domainName);
			List<CachedSignedPolicy> vpSignedPolicies = domainVPCache.get(consent.getVirtualPerson().getId());
			for (Entry<SignedPolicy, Long> entry : signedPoliciesWithExpirationDates.entrySet())
			{
				if (entry.getValue() != null)
				{
					CachedSignedPolicy cachedSignedPolicy = INSTANCE.new CachedSignedPolicy(entry.getKey().getKey(), consent.getConsentDateValues().getGicsConsentTimestamp(),
							consent.getConsentDateValues().getLegalConsentTimestamp(), entry.getKey().getStatus(), entry.getValue());
					for (SignerIdKey signerIdKey : signerIdKeys)
					{
						domainSignerCache.get(signerIdKey).remove(cachedSignedPolicy);
					}
					vpSignedPolicies.remove(cachedSignedPolicy);
				}
				else
				{
					LOGGER.fatal("inconsistent data - signed policy isn't part of template: " + entry.getKey());
				}
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<CachedSignedPolicy> getCachedPoliciesForSigner(String domain, SignerIdKey signerIdKey, String policyName)
	{
		List<CachedSignedPolicy> result = new ArrayList<>();
		CACHE_RWL.readLock().lock();
		try
		{
			if (SIGNER_CACHE.get(domain) != null && SIGNER_CACHE.get(domain).get(signerIdKey) != null)
			{
				for (CachedSignedPolicy cachedSignedPolicy : SIGNER_CACHE.get(domain).get(signerIdKey))
				{
					if (cachedSignedPolicy.getName().equals(policyName))
					{
						result.add(cachedSignedPolicy);
					}
				}
			}
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
		return result;
	}

	public static List<CachedSignedPolicy> getCachedPoliciesForSigner(String domain, SignerIdKey signerIdKey, String policyName, int versionFrom,
			int versionTo)
	{
		List<CachedSignedPolicy> result = new ArrayList<>();
		CACHE_RWL.readLock().lock();
		try
		{
			if (SIGNER_CACHE.get(domain) != null && SIGNER_CACHE.get(domain).get(signerIdKey) != null)
			{
				for (CachedSignedPolicy cachedSignedPolicy : SIGNER_CACHE.get(domain).get(signerIdKey))
				{
					if (cachedSignedPolicy.getName().equals(policyName) && cachedSignedPolicy.getVersion() >= versionFrom
							&& cachedSignedPolicy.getVersion() <= versionTo)
					{
						result.add(cachedSignedPolicy);
					}
				}
			}
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
		return result;
	}

	public static List<CachedSignedPolicy> getCachedPoliciesForVP(String domain, VirtualPerson vp, String policyName)
	{
		List<CachedSignedPolicy> result = new ArrayList<>();
		CACHE_RWL.readLock().lock();
		try
		{
			if (VP_CACHE.get(domain) != null && VP_CACHE.get(domain).get(vp.getId()) != null)
			{
				for (CachedSignedPolicy cachedSignedPolicy : VP_CACHE.get(domain).get(vp.getId()))
				{
					if (cachedSignedPolicy.getName().equals(policyName))
					{
						result.add(cachedSignedPolicy);
					}
				}
			}
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
		return result;
	}

	public static List<CachedSignedPolicy> getCachedPoliciesForVP(String domain, VirtualPerson vp, String policyName, int versionFrom,
			int versionTo)
	{
		List<CachedSignedPolicy> result = new ArrayList<>();
		CACHE_RWL.readLock().lock();
		try
		{
			if (VP_CACHE.get(domain) != null && VP_CACHE.get(domain).get(vp.getId()) != null)
			{
				for (CachedSignedPolicy cachedSignedPolicy : VP_CACHE.get(domain).get(vp.getId()))
				{
					if (cachedSignedPolicy.getName().equals(policyName) && cachedSignedPolicy.getVersion() >= versionFrom
							&& cachedSignedPolicy.getVersion() <= versionTo)
					{
						result.add(cachedSignedPolicy);
					}
				}
			}
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
		return result;
	}

	public class CachedSignedPolicy
	{
		private final SignedPolicyKey spKey;
		private final long gicsConsentDate;
		private final long legalConsentDate;
		private final ConsentStatus consentStatus;
		private final long consentExpirationDate;

		public CachedSignedPolicy(SignedPolicyKey spKey, long gicsConsentDate, long legalConsentDate, ConsentStatus consentStatus, long consentExpirationDate)
		{
			super();
			this.spKey = spKey;
			this.gicsConsentDate = gicsConsentDate;
			this.legalConsentDate = legalConsentDate;
			this.consentStatus = consentStatus;
			this.consentExpirationDate = consentExpirationDate;
		}

		public SignedPolicyKey getSPKey()
		{
			return spKey;
		}

		public long getGicsConsentDate()
		{
			return gicsConsentDate;
		}

		public long getLegalConsentDate()
		{
			return legalConsentDate;
		}

		public ConsentStatus getConsentStatus()
		{
			return consentStatus;
		}

		public long getConsentExpirationDate()
		{
			return consentExpirationDate;
		}

		public String getName()
		{
			return spKey.getPolicyKey().getName();
		}

		public int getVersion()
		{
			return spKey.getPolicyKey().getVersion();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (consentExpirationDate ^ consentExpirationDate >>> 32);
			result = prime * result + (consentStatus == null ? 0 : consentStatus.hashCode());
			result = prime * result + (int) (gicsConsentDate ^ gicsConsentDate >>> 32);
			result = prime * result + (int) (legalConsentDate ^ legalConsentDate >>> 32);
			result = prime * result + (spKey == null ? 0 : spKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			CachedSignedPolicy other = (CachedSignedPolicy) obj;
			if (consentExpirationDate != other.consentExpirationDate)
			{
				return false;
			}
			if (consentStatus != other.consentStatus)
			{
				return false;
			}
			if (gicsConsentDate != other.gicsConsentDate)
			{
				return false;
			}
			if (legalConsentDate != other.legalConsentDate)
			{
				return false;
			}
			if (spKey == null)
			{
				if (other.spKey != null)
				{
					return false;
				}
			}
			else if (!spKey.equals(other.spKey))
			{
				return false;
			}
			return true;
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder("cache signed policy for signed policy with '");
			sb.append(spKey);
			sb.append(", with expiration date ");
			sb.append(new Date(consentExpirationDate));
			sb.append(" is consented with consent state: ");
			sb.append(consentStatus);
			return sb.toString();
		}
	}
}
