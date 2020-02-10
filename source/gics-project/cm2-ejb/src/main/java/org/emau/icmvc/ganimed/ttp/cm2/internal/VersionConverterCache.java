package org.emau.icmvc.ganimed.ttp.cm2.internal;

/*-
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.factories.ReflectionClassFactory;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * cache for version converter
 *
 * @author geidell
 *
 */
public class VersionConverterCache {

	private static final Logger logger = Logger.getLogger(VersionConverterCache.class);
	private static boolean initialised = false;
	// domain -> signerId -> signedPolicies
	private static final ReentrantReadWriteLock cacheRWL = new ReentrantReadWriteLock();
	// key ist jeweils der domainName
	private static final Map<String, VersionConverter> policyVersionConvertCache = new HashMap<String, VersionConverter>();
	private static final Map<String, VersionConverter> moduleVersionConvertCache = new HashMap<String, VersionConverter>();
	private static final Map<String, VersionConverter> ctVersionConvertCache = new HashMap<String, VersionConverter>();

	public boolean isInitialised() {
		return initialised;
	}

	public void init(List<Domain> domains) throws VersionConverterClassException {
		cacheRWL.writeLock().lock();
		try {
			if (initialised) {
				return;
			}
			for (Domain domain : domains) {
				addDomain(domain);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("cache filled");
			}
			initialised = true;
		} finally {
			cacheRWL.writeLock().unlock();
		}
	}

	public void addDomain(Domain domain) throws VersionConverterClassException {
		String domainName = domain.getName();
		cacheRWL.writeLock().lock();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("add domain " + domainName);
			}
			VersionConverter policyVersionConverter = initVersionConverter(domain.getPolicyVersionConverter());
			VersionConverter moduleVersionConverter = initVersionConverter(domain.getModuleVersionConverter());
			VersionConverter ctVersionConverter = initVersionConverter(domain.getCTVersionConverter());
			policyVersionConvertCache.put(domainName, policyVersionConverter);
			moduleVersionConvertCache.put(domainName, moduleVersionConverter);
			ctVersionConvertCache.put(domainName, ctVersionConverter);
			if (logger.isDebugEnabled()) {
				logger.debug("domain " + domainName + " added");
			}
		} finally {
			cacheRWL.writeLock().unlock();
		}
	}

	private VersionConverter initVersionConverter(String versionConverterClass) throws VersionConverterClassException {
		try {
			Class<? extends VersionConverter> converterClass = ReflectionClassFactory.getInstance().getSubClass(versionConverterClass,
					VersionConverter.class);
			return converterClass.newInstance();
		} catch (Exception e) {
			String message = "exception while instantiating " + versionConverterClass + ": " + e.getMessage();
			logger.error(message);
			throw new VersionConverterClassException(message, e);
		}
	}

	public void removeDomain(String domainName) {
		cacheRWL.writeLock().lock();
		try {
			policyVersionConvertCache.remove(domainName);
			moduleVersionConvertCache.remove(domainName);
			ctVersionConvertCache.remove(domainName);
		} finally {
			cacheRWL.writeLock().unlock();
		}
	}

	public VersionConverter getPolicyVersionConverter(String domainName) throws UnknownDomainException {
		VersionConverter result;
		cacheRWL.readLock().lock();
		try {
			result = policyVersionConvertCache.get(domainName);
		} finally {
			cacheRWL.readLock().unlock();
		}
		if (result == null) {
			String message = "unknown domain: " + domainName;
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}

	public VersionConverter getModuleVersionConverter(String domainName) throws UnknownDomainException {
		VersionConverter result;
		cacheRWL.readLock().lock();
		try {
			result = moduleVersionConvertCache.get(domainName);
		} finally {
			cacheRWL.readLock().unlock();
		}
		if (result == null) {
			String message = "unknown domain: " + domainName;
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}

	public VersionConverter getCTVersionConverter(String domainName) throws UnknownDomainException {
		VersionConverter result;
		cacheRWL.readLock().lock();
		try {
			result = ctVersionConvertCache.get(domainName);
		} finally {
			cacheRWL.readLock().unlock();
		}
		if (result == null) {
			String message = "unknown domain: " + domainName;
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}
}
