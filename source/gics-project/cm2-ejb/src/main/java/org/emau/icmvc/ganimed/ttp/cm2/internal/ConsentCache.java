package org.emau.icmvc.ganimed.ttp.cm2.internal;

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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPersonSignerId;

/**
 * cache for consents
 *
 * @author geidell
 *
 */
public class ConsentCache {

	private static final Logger logger = Logger.getLogger(ConsentCache.class);
	private static final int SIGNED_POLICIES_PAGE_SIZE = 100000;
	private static boolean initialised = false;
	// domain -> signerId -> signedPolicies
	private static final ReentrantReadWriteLock cacheRWL = new ReentrantReadWriteLock();
	private static final Map<String, Map<SignerIdDTO, List<CachedSignedPolicy>>> signerCache = new HashMap<String, Map<SignerIdDTO, List<CachedSignedPolicy>>>();
	private static final Map<String, Map<Long, List<CachedSignedPolicy>>> vpCache = new HashMap<String, Map<Long, List<CachedSignedPolicy>>>();

	public boolean isInitialised() {
		return initialised;
	}

	public void init(EntityManager em) {
		cacheRWL.writeLock().lock();
		try {
			if (initialised) {
				return;
			}
			List<VirtualPersonSignerId> signerIds = getAllSignerIds(em);
			em.clear(); // wichtig! eclipselink laedt sonst die ganzen objekte (vor allem consents) nach
			if (logger.isDebugEnabled()) {
				logger.debug("found " + signerIds.size() + " signer ids, sorting them now");
			}
			Map<Long, Set<SignerIdDTO>> mappedSignerIds = mapSignerIds(signerIds);
			if (logger.isDebugEnabled()) {
				logger.debug("signer ids sorted, found entries for " + mappedSignerIds.size() + " virtual persons");
			}
			// signed policies chunked to save memory
			long signedPoliciesCount = getSignedPoliciesCount(em);
			if (logger.isDebugEnabled()) {
				logger.debug("found " + signedPoliciesCount + " signed policies, sorting them now");
			}
			for (int i = 0; i * SIGNED_POLICIES_PAGE_SIZE < signedPoliciesCount; i++) {
				int nextPageSize = (int) ((i + 1) * SIGNED_POLICIES_PAGE_SIZE < signedPoliciesCount ? SIGNED_POLICIES_PAGE_SIZE
						: signedPoliciesCount - (i) * SIGNED_POLICIES_PAGE_SIZE);
				if (nextPageSize > 0) {
					List<SignedPolicy> signedPolicies = getSignedPolicies(em, i * SIGNED_POLICIES_PAGE_SIZE, nextPageSize);
					em.clear(); // wichtig! eclipselink laedt sonst die ganzen objekte (vor allem consents) nach
					Map<Long, List<SignedPolicy>> mappedSignedPolicies = mapSignedPolicies(signedPolicies);
					if (logger.isDebugEnabled()) {
						logger.debug("signed policies sorted, found entries for " + mappedSignedPolicies.size() + " virtual persons");
					}
					for (Entry<Long, Set<SignerIdDTO>> entry : mappedSignerIds.entrySet()) {
						addConsent(entry.getKey(), entry.getValue(), mappedSignedPolicies.get(entry.getKey()));
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("cache filled");
			}
			initialised = true;
		} finally {
			cacheRWL.writeLock().unlock();
		}
	}

	private List<VirtualPersonSignerId> getAllSignerIds(EntityManager em) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<VirtualPersonSignerId> criteriaQuery = criteriaBuilder.createQuery(VirtualPersonSignerId.class);
		Root<VirtualPersonSignerId> root = criteriaQuery.from(VirtualPersonSignerId.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private Map<Long, Set<SignerIdDTO>> mapSignerIds(List<VirtualPersonSignerId> virtualPersonSignerIds) {
		Map<Long, Set<SignerIdDTO>> result = new HashMap<Long, Set<SignerIdDTO>>();
		for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds) {
			SignerIdDTO signerIdDTO = new SignerIdDTO(virtualPersonSignerId.getKey().getSignerIdKey().getSignerIdTypeKey().getName(),
					virtualPersonSignerId.getKey().getSignerIdKey().getValue());
			Long vpId = virtualPersonSignerId.getKey().getVpId();
			Set<SignerIdDTO> signerIdsForVP = result.get(vpId);
			if (signerIdsForVP == null) {
				signerIdsForVP = new HashSet<SignerIdDTO>();
				result.put(vpId, signerIdsForVP);
			}
			signerIdsForVP.add(signerIdDTO);
		}
		return result;
	}

	private long getSignedPoliciesCount(EntityManager em) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(SignedPolicy.class)));
		Query query = em.createQuery(criteriaQuery);
		return (long) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	private List<SignedPolicy> getSignedPolicies(EntityManager em, int startPosition, int maxResults) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SignedPolicy> criteriaQuery = criteriaBuilder.createQuery(SignedPolicy.class);
		Root<SignedPolicy> root = criteriaQuery.from(SignedPolicy.class);
		criteriaQuery.select(root);
		Query query = em.createQuery(criteriaQuery);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	private Map<Long, List<SignedPolicy>> mapSignedPolicies(List<SignedPolicy> signedPolicies) {
		Map<Long, List<SignedPolicy>> result = new HashMap<Long, List<SignedPolicy>>();
		for (SignedPolicy signedPolicy : signedPolicies) {
			Long vpId = signedPolicy.getKey().getConsentKey().getVirtualPersonId();
			List<SignedPolicy> signedPoliciesForVP = result.get(vpId);
			if (signedPoliciesForVP == null) {
				signedPoliciesForVP = new ArrayList<SignedPolicy>();
				result.put(vpId, signedPoliciesForVP);
			}
			signedPoliciesForVP.add(signedPolicy);
		}
		return result;
	}

	public void addConsent(Long vpId, Set<SignerIdDTO> signerIdDTOs, List<SignedPolicy> signedPolicies) {
		if (signedPolicies == null || signedPolicies.isEmpty() || signerIdDTOs == null || signerIdDTOs.isEmpty()) {
			return;
		}
		String domainName = signedPolicies.get(0).getKey().getPolicyKey().getDomainName();
		cacheRWL.writeLock().lock();
		try {
			Map<SignerIdDTO, List<CachedSignedPolicy>> domainSignerCache = signerCache.get(domainName);
			if (domainSignerCache == null) {
				domainSignerCache = new HashMap<SignerIdDTO, List<CachedSignedPolicy>>();
				signerCache.put(domainName, domainSignerCache);
			}
			for (SignerIdDTO signerId : signerIdDTOs) {
				if (domainSignerCache.get(signerId) == null) {
					domainSignerCache.put(signerId, new ArrayList<CachedSignedPolicy>());
				}
			}
			Map<Long, List<CachedSignedPolicy>> domainVPCache = vpCache.get(domainName);
			if (domainVPCache == null) {
				domainVPCache = new HashMap<Long, List<CachedSignedPolicy>>();
				vpCache.put(domainName, domainVPCache);
			}
			List<CachedSignedPolicy> vpSignedPolicies = domainVPCache.get(vpId);
			if (vpSignedPolicies == null) {
				vpSignedPolicies = new ArrayList<CachedSignedPolicy>();
				domainVPCache.put(vpId, vpSignedPolicies);
			}
			// objekte nur einmal erzeugen, deswegen neue schleife
			for (SignedPolicy signedPolicy : signedPolicies) {
				PolicyKey policyKey = signedPolicy.getKey().getPolicyKey();
				CachedSignedPolicy cachedSignedPolicy = new CachedSignedPolicy(policyKey.getName(), policyKey.getVersion(),
						signedPolicy.getKey().getConsentKey().getConsentDate().getTime(), signedPolicy.getStatus());
				for (SignerIdDTO signerId : signerIdDTOs) {
					domainSignerCache.get(signerId).add(cachedSignedPolicy);
				}
				vpSignedPolicies.add(cachedSignedPolicy);
			}
		} finally {
			cacheRWL.writeLock().unlock();
		}
	}

	public List<CachedSignedPolicy> getCachedPoliciesForSigner(String domain, SignerIdDTO signerIdDTO, String policyName) {
		List<CachedSignedPolicy> result = new ArrayList<CachedSignedPolicy>();
		cacheRWL.readLock().lock();
		try {
			if (signerCache.get(domain) != null && signerCache.get(domain).get(signerIdDTO) != null) {
				for (CachedSignedPolicy cachedSignedPolicy : signerCache.get(domain).get(signerIdDTO)) {
					if (cachedSignedPolicy.getName().equals(policyName)) {
						result.add(cachedSignedPolicy);
					}
				}
			}
		} finally {
			cacheRWL.readLock().unlock();
		}
		return result;
	}

	public List<CachedSignedPolicy> getCachedPoliciesForSigner(String domain, SignerIdDTO signerIdDTO, String policyName, int versionFrom,
			int versionTo) {
		List<CachedSignedPolicy> result = new ArrayList<CachedSignedPolicy>();
		cacheRWL.readLock().lock();
		try {
			if (signerCache.get(domain) != null && signerCache.get(domain).get(signerIdDTO) != null) {
				for (CachedSignedPolicy cachedSignedPolicy : signerCache.get(domain).get(signerIdDTO)) {
					if (cachedSignedPolicy.getName().equals(policyName) && cachedSignedPolicy.getVersion() >= versionFrom
							&& cachedSignedPolicy.getVersion() <= versionTo) {
						result.add(cachedSignedPolicy);
					}
				}
			}
		} finally {
			cacheRWL.readLock().unlock();
		}
		return result;
	}

	public List<CachedSignedPolicy> getCachedPoliciesForVP(String domain, VirtualPerson vp, String policyName) {
		List<CachedSignedPolicy> result = new ArrayList<CachedSignedPolicy>();
		cacheRWL.readLock().lock();
		try {
			if (vpCache.get(domain) != null && vpCache.get(domain).get(vp.getId()) != null) {
				for (CachedSignedPolicy cachedSignedPolicy : vpCache.get(domain).get(vp.getId())) {
					if (cachedSignedPolicy.getName().equals(policyName)) {
						result.add(cachedSignedPolicy);
					}
				}
			}
		} finally {
			cacheRWL.readLock().unlock();
		}
		return result;
	}

	public List<CachedSignedPolicy> getCachedPoliciesForVP(String domain, VirtualPerson vp, String policyName, int versionFrom, int versionTo) {
		List<CachedSignedPolicy> result = new ArrayList<CachedSignedPolicy>();
		cacheRWL.readLock().lock();
		try {
			if (vpCache.get(domain) != null && vpCache.get(domain).get(vp.getId()) != null) {
				for (CachedSignedPolicy cachedSignedPolicy : vpCache.get(domain).get(vp.getId())) {
					if (cachedSignedPolicy.getName().equals(policyName) && cachedSignedPolicy.getVersion() >= versionFrom
							&& cachedSignedPolicy.getVersion() <= versionTo) {
						result.add(cachedSignedPolicy);
					}
				}
			}
		} finally {
			cacheRWL.readLock().unlock();
		}
		return result;
	}

	public class CachedSignedPolicy {

		private final String name;
		private final int version;
		private final long consentDate;
		private final ConsentStatus consentStatus;

		public CachedSignedPolicy(String name, int version, long consentDate, ConsentStatus consentStatus) {
			super();
			this.name = name;
			this.version = version;
			this.consentDate = consentDate;
			this.consentStatus = consentStatus;
		}

		public String getName() {
			return name;
		}

		public int getVersion() {
			return version;
		}

		public long getConsentDate() {
			return consentDate;
		}

		public ConsentStatus getConsentStatus() {
			return consentStatus;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (consentDate ^ (consentDate >>> 32));
			result = prime * result + ((consentStatus == null) ? 0 : consentStatus.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + version;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CachedSignedPolicy other = (CachedSignedPolicy) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (consentDate != other.consentDate) {
				return false;
			}
			if (consentStatus != other.consentStatus) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (version != other.version) {
				return false;
			}
			return true;
		}

		private ConsentCache getOuterType() {
			return ConsentCache.this;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("cache signed policy with name '");
			sb.append(name);
			sb.append("', in version '");
			sb.append(version);
			sb.append("' and date ");
			sb.append(new Date(consentDate));
			sb.append(" is consented with consent state: ");
			sb.append(consentStatus);
			return sb.toString();
		}
	}

}
