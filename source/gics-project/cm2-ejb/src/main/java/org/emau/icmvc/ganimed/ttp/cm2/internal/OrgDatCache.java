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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * cache for domains, policies, modules, templates
 * <p>
 * all get... and list... functions exists in two versions:
 * <li>...CFEU (Copy For Extern Use) - contructs a new object; in case of a list a new list filled with copies of the DTOs. if you don't know what you're doing, use this.</li>
 * <li>...FIUO (For Intern Use Only) - reference of the cache object; in case of a list a new list with references to the cache DTOs. use for speed up, never return this refs to the caller.</li>
 * <p>
 * don't use write methods (add, update, delete) directly - use them via DataAccessDispatcher
 * <p>
 *
 * @author geidell
 *
 */
public class OrgDatCache
{
	private static final Logger LOGGER = LogManager.getLogger(OrgDatCache.class);
	private static final OrgDatCache INSTANCE = new OrgDatCache();
	private static volatile boolean INITIALISED = false;
	private static final ReentrantReadWriteLock CACHE_RWL = new ReentrantReadWriteLock();
	private static final HashMap<String, DomainDTO> DOMAIN_CACHE = new HashMap<>();
	private static final HashMap<String, HashMap<ConsentTemplateKeyDTO, ConsentTemplateDTO>> CT_CACHE = new HashMap<>();
	private static final HashMap<String, HashMap<ModuleKeyDTO, ModuleDTO>> MODULE_CACHE = new HashMap<>();
	private static final HashMap<String, HashMap<PolicyKeyDTO, PolicyDTO>> POLICY_CACHE = new HashMap<>();
	private static final HashMap<String, HashMap<String, SignerIdTypeDTO>> SIDT_CACHE = new HashMap<>();

	private OrgDatCache()
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

	public static void init(List<Domain> domains, List<ConsentTemplate> cts, List<Module> modules, List<Policy> policies, List<SignerIdType> sIdTypes)
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (INITIALISED)
			{
				return;
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("initialising orgDatCache");
				LOGGER.debug("add " + domains.size() + " domains");
			}
			for (Domain domain : domains)
			{
				addDomain(domain.toDTO());
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add " + cts.size() + " consentTemplates");
			}
			for (ConsentTemplate ct : cts)
			{
				ConsentTemplateDTO dto = ct.toDTO();
				String domainName = ct.getDomain().getName();
				CT_CACHE.get(domainName).put(dto.getKey(), dto);
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add " + modules.size() + " modules");
			}
			for (Module module : modules)
			{
				ModuleDTO dto = module.toDTO();
				MODULE_CACHE.get(module.getKey().getDomainName()).put(dto.getKey(), dto);
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add " + policies.size() + " policies");
			}
			for (Policy policy : policies)
			{
				PolicyDTO dto = policy.toDTO();
				POLICY_CACHE.get(policy.getKey().getDomainName()).put(dto.getKey(), dto);
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add " + sIdTypes.size() + " signerIdTypes");
			}
			for (SignerIdType sid : sIdTypes)
			{
				SignerIdTypeDTO dto = sid.toDTO();
				SIDT_CACHE.get(sid.getKey().getDomainName()).put(dto.getName(), dto);
			}
			INITIALISED = true;
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("orgDatCache initialised");
			}
		}
		catch (InvalidVersionException | UnknownDomainException e)
		{
			LOGGER.fatal("Exception while initialize OrgDatCache", e);
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	private static ConsentTemplateKeyDTO getVersionCorrectedCTKey(ConsentTemplateKeyDTO ctKey) throws InvalidVersionException, UnknownDomainException
	{
		VersionConverter ctVersionConverter = VersionConverterCache.getCTVersionConverter(ctKey.getDomainName());
		String correctedVersion = ctVersionConverter.intToString(ctVersionConverter.stringToInt(ctKey.getVersion())).intern();
		return new ConsentTemplateKeyDTO(ctKey.getDomainName(), ctKey.getName(), correctedVersion);
	}

	private static ModuleKeyDTO getVersionCorrectedModuleKey(ModuleKeyDTO moduleKey) throws InvalidVersionException, UnknownDomainException
	{
		VersionConverter moduleVersionConverter = VersionConverterCache.getModuleVersionConverter(moduleKey.getDomainName());
		String correctedVersion = moduleVersionConverter.intToString(moduleVersionConverter.stringToInt(moduleKey.getVersion())).intern();
		return new ModuleKeyDTO(moduleKey.getDomainName(), moduleKey.getName(), correctedVersion);
	}

	private static PolicyKeyDTO getVersionCorrectedPolicyKey(PolicyKeyDTO policyKey) throws InvalidVersionException, UnknownDomainException
	{
		VersionConverter policyVersionConverter = VersionConverterCache.getPolicyVersionConverter(policyKey.getDomainName());
		String correctedVersion = policyVersionConverter.intToString(policyVersionConverter.stringToInt(policyKey.getVersion())).intern();
		return new PolicyKeyDTO(policyKey.getDomainName(), policyKey.getName(), correctedVersion);
	}

	// ------------------------------------------ domains ------------------------------------------

	public static void addDomain(DomainDTO domain)
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			String domainName = domain.getName();
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add domain " + domainName);
			}
			DOMAIN_CACHE.put(domainName, new DomainDTO(domain));
			CT_CACHE.put(domainName, new HashMap<>());
			MODULE_CACHE.put(domainName, new HashMap<>());
			POLICY_CACHE.put(domainName, new HashMap<>());
			SIDT_CACHE.put(domainName, new HashMap<>());
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("domain " + domainName + " added");
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<DomainDTO> listDomainsCFEU(boolean onlyFinal)
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<DomainDTO> result = new ArrayList<>();
			for (DomainDTO domain : DOMAIN_CACHE.values())
			{
				if (!onlyFinal || domain.getFinalised())
				{
					result.add(new DomainDTO(domain));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<DomainDTO> listDomainsFIUO(boolean onlyFinal)
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<DomainDTO> result = new ArrayList<>();
			for (DomainDTO domain : DOMAIN_CACHE.values())
			{
				if (!onlyFinal || domain.getFinalised())
				{
					result.add(domain);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static DomainDTO getDomainCFEU(String domainName) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return new DomainDTO(getDomainFIUO(domainName));
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static DomainDTO getDomainFIUO(String domainName) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			DomainDTO result = DOMAIN_CACHE.get(domainName);
			if (result == null)
			{
				String message = "domain " + domainName + " not found within cache";
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info(message);
				}
				throw new UnknownDomainException(message);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void updateDomain(DomainDTO domain) throws UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			getDomainFIUO(domain.getName());
			DOMAIN_CACHE.put(domain.getName(), new DomainDTO(domain));
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removeDomain(String domainName) throws UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove domain " + domainName);
			}
			getDomainFIUO(domainName);
			DOMAIN_CACHE.remove(domainName);
			CT_CACHE.remove(domainName);
			MODULE_CACHE.remove(domainName);
			POLICY_CACHE.remove(domainName);
			SIDT_CACHE.remove(domainName);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("domain " + domainName + " removed");
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ consent templates ------------------------------------------

	public static void addConsentTemplate(ConsentTemplateDTO ct) throws InvalidVersionException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			String domainName = ct.getKey().getDomainName();
			getDomainFIUO(domainName);
			ConsentTemplateKeyDTO ctKey = getVersionCorrectedCTKey(ct.getKey());
			ct.setKey(ctKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add consent template " + ctKey + " to domain " + domainName);
			}
			CT_CACHE.get(domainName).put(ctKey, new ConsentTemplateDTO(ct));
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("consent template " + ctKey + " added to domain " + domainName);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesCFEU()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (String domainName : CT_CACHE.keySet())
			{
				for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
				{
					result.add(new ConsentTemplateDTO(ct));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesFIUO()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (String domainName : CT_CACHE.keySet())
			{
				for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
				{
					result.add(ct);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesCFEU(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
			{
				if (!onlyFinal || ct.getFinalised())
				{
					result.add(new ConsentTemplateDTO(ct));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesFIUO(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
			{
				if (!onlyFinal || ct.getFinalised())
				{
					result.add(ct);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesCFEU(String domainName, boolean onlyFinal, ConsentTemplateType type) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
			{
				if ((!onlyFinal || ct.getFinalised()) && ct.getType().equals(type))
				{
					result.add(new ConsentTemplateDTO(ct));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ConsentTemplateDTO> listConsentTemplatesFIUO(String domainName, boolean onlyFinal, ConsentTemplateType type) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ConsentTemplateDTO> result = new ArrayList<>();
			for (ConsentTemplateDTO ct : CT_CACHE.get(domainName).values())
			{
				if ((!onlyFinal || ct.getFinalised()) && ct.getType().equals(type))
				{
					result.add(ct);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static ConsentTemplateDTO getConsentTemplateCFEU(ConsentTemplateKeyDTO ctKey) throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return new ConsentTemplateDTO(getConsentTemplateFIUO(ctKey));
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static ConsentTemplateDTO getConsentTemplateFIUO(ConsentTemplateKeyDTO ctKey) throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(ctKey.getDomainName());
			ctKey = getVersionCorrectedCTKey(ctKey);
			ConsentTemplateDTO result = CT_CACHE.get(ctKey.getDomainName()).get(ctKey);
			if (result == null)
			{
				String message = "consent template " + ctKey + " not found within cache for domain " + ctKey.getDomainName();
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info(message);
				}
				throw new UnknownConsentTemplateException(message);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void updateConsentTemplate(ConsentTemplateDTO ct) throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			ConsentTemplateKeyDTO ctKey = getVersionCorrectedCTKey(ct.getKey());
			ct.setKey(ctKey);
			getConsentTemplateFIUO(ctKey);
			CT_CACHE.get(ct.getKey().getDomainName()).put(ctKey, new ConsentTemplateDTO(ct));
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removeConsentTemplate(ConsentTemplateKeyDTO ctKey) throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove consent template " + ctKey + " from domain " + ctKey.getDomainName());
			}
			ctKey = getVersionCorrectedCTKey(ctKey);
			getConsentTemplateFIUO(ctKey);
			CT_CACHE.get(ctKey.getDomainName()).remove(ctKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("consent template " + ctKey + " removed from domain " + ctKey.getDomainName());
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void addMappedTemplateToConsentTemplate(ConsentTemplateKeyDTO ctKey, ConsentTemplateKeyDTO ctKeyToAdd, ConsentTemplateType ctType)
			throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add consent template " + ctKeyToAdd + " (type " + ctType + ") to template " + ctKey);
			}
			ctKey = getVersionCorrectedCTKey(ctKey);
			ConsentTemplateDTO ct = getConsentTemplateFIUO(ctKey);
			switch (ctType)
			{
				case CONSENT:
					ct.getMappedConsentTemplates().add(ctKeyToAdd);
					break;
				case REFUSAL:
					ct.getMappedRefusalTemplates().add(ctKeyToAdd);
					break;
				case REVOCATION:
					ct.getMappedRevocationTemplates().add(ctKeyToAdd);
					break;
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("consent template " + ctKeyToAdd + " (type " + ctType + ") added to template " + ctKey);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removeMappedTemplateFromConsentTemplate(ConsentTemplateKeyDTO ctKey, ConsentTemplateKeyDTO ctKeyToRemove)
			throws InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove consent template " + ctKeyToRemove + " from template " + ctKey);
			}
			ctKey = getVersionCorrectedCTKey(ctKey);
			ConsentTemplateDTO ct = getConsentTemplateFIUO(ctKey);
			// einfach in allen drei mappings entfernen
			ct.getMappedConsentTemplates().remove(ctKeyToRemove);
			ct.getMappedRefusalTemplates().remove(ctKeyToRemove);
			ct.getMappedRevocationTemplates().remove(ctKeyToRemove);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("consent template " + ctKeyToRemove + " removed from template " + ctKey);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ modules ------------------------------------------

	public static void addModule(ModuleDTO module) throws InvalidVersionException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			String domainName = module.getKey().getDomainName();
			getDomainFIUO(domainName);
			ModuleKeyDTO moduleKey = getVersionCorrectedModuleKey(module.getKey());
			module.setKey(moduleKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add module " + moduleKey + " to domain " + domainName);
			}
			MODULE_CACHE.get(domainName).put(moduleKey, new ModuleDTO(module));
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("module " + moduleKey + " added to domain " + domainName);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<ModuleDTO> listModulesCFEU()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<ModuleDTO> result = new ArrayList<>();
			for (String domainName : MODULE_CACHE.keySet())
			{
				for (ModuleDTO module : MODULE_CACHE.get(domainName).values())
				{
					result.add(new ModuleDTO(module));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ModuleDTO> listModulesFIUO()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<ModuleDTO> result = new ArrayList<>();
			for (String domainName : MODULE_CACHE.keySet())
			{
				for (ModuleDTO module : MODULE_CACHE.get(domainName).values())
				{
					result.add(module);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ModuleDTO> listModulesCFEU(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ModuleDTO> result = new ArrayList<>();
			for (ModuleDTO module : MODULE_CACHE.get(domainName).values())
			{
				if (!onlyFinal || module.getFinalised())
				{
					result.add(new ModuleDTO(module));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<ModuleDTO> listModulesFIUO(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<ModuleDTO> result = new ArrayList<>();
			for (ModuleDTO module : MODULE_CACHE.get(domainName).values())
			{
				if (!onlyFinal || module.getFinalised())
				{
					result.add(module);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static ModuleDTO getModuleCFEU(ModuleKeyDTO moduleKey) throws InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return new ModuleDTO(getModuleFIUO(moduleKey));
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static ModuleDTO getModuleFIUO(ModuleKeyDTO moduleKey) throws InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(moduleKey.getDomainName());
			ModuleDTO result = MODULE_CACHE.get(moduleKey.getDomainName()).get(getVersionCorrectedModuleKey(moduleKey));
			if (result == null)
			{
				String message = "module " + moduleKey + " not found within cache for domain " + moduleKey.getDomainName();
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info(message);
				}
				throw new UnknownModuleException(message);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void updateModule(ModuleDTO module) throws InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			ModuleKeyDTO moduleKey = getVersionCorrectedModuleKey(module.getKey());
			module.setKey(moduleKey);
			getModuleFIUO(moduleKey);
			MODULE_CACHE.get(moduleKey.getDomainName()).put(moduleKey, new ModuleDTO(module));
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removeModule(ModuleKeyDTO moduleKey) throws InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove module " + moduleKey + " from domain " + moduleKey.getDomainName());
			}
			moduleKey = getVersionCorrectedModuleKey(moduleKey);
			getModuleFIUO(moduleKey);
			MODULE_CACHE.get(moduleKey.getDomainName()).remove(moduleKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("module " + moduleKey + " removed from domain " + moduleKey.getDomainName());
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ policies ------------------------------------------

	public static void addPolicy(PolicyDTO policy) throws InvalidVersionException, UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			String domainName = policy.getKey().getDomainName();
			getDomainFIUO(domainName);
			PolicyKeyDTO policyKey = getVersionCorrectedPolicyKey(policy.getKey());
			policy.setKey(policyKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add policy " + policyKey + " to domain " + domainName);
			}
			POLICY_CACHE.get(domainName).put(policyKey, new PolicyDTO(policy));
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("policy " + policyKey + " added to domain " + domainName);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<PolicyDTO> listPoliciesCFEU()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<PolicyDTO> result = new ArrayList<>();
			for (String domainName : POLICY_CACHE.keySet())
			{
				for (PolicyDTO policy : POLICY_CACHE.get(domainName).values())
				{
					result.add(new PolicyDTO(policy));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<PolicyDTO> listPoliciesFIUO()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<PolicyDTO> result = new ArrayList<>();
			for (String domainName : POLICY_CACHE.keySet())
			{
				for (PolicyDTO policy : POLICY_CACHE.get(domainName).values())
				{
					result.add(policy);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<PolicyDTO> listPoliciesCFEU(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<PolicyDTO> result = new ArrayList<>();
			for (PolicyDTO policy : POLICY_CACHE.get(domainName).values())
			{
				if (!onlyFinal || policy.getFinalised())
				{
					result.add(new PolicyDTO(policy));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<PolicyDTO> listPoliciesFIUO(String domainName, boolean onlyFinal) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<PolicyDTO> result = new ArrayList<>();
			for (PolicyDTO policy : POLICY_CACHE.get(domainName).values())
			{
				if (!onlyFinal || policy.getFinalised())
				{
					result.add(policy);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static PolicyDTO getPolicyCFEU(PolicyKeyDTO policyKey) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return new PolicyDTO(getPolicyFIUO(policyKey));
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static PolicyDTO getPolicyFIUO(PolicyKeyDTO policyKey) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(policyKey.getDomainName());
			policyKey = getVersionCorrectedPolicyKey(policyKey);
			PolicyDTO result = POLICY_CACHE.get(policyKey.getDomainName()).get(policyKey);
			if (result == null)
			{
				String message = "policy " + policyKey + " not found within cache for domain " + policyKey.getDomainName();
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info(message);
				}
				throw new UnknownPolicyException(message);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void updatePolicy(PolicyDTO policy) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			PolicyKeyDTO policyKey = getVersionCorrectedPolicyKey(policy.getKey());
			policy.setKey(policyKey);
			getPolicyFIUO(policy.getKey());
			POLICY_CACHE.get(policyKey.getDomainName()).put(policyKey, new PolicyDTO(policy));
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removePolicy(PolicyKeyDTO policyKey) throws InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove policy " + policyKey + " from domain " + policyKey.getDomainName());
			}
			policyKey = getVersionCorrectedPolicyKey(policyKey);
			getPolicyFIUO(policyKey);
			POLICY_CACHE.get(policyKey.getDomainName()).remove(policyKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("policy " + policyKey + " removed from domain " + policyKey.getDomainName());
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	// ------------------------------------------ signerId types ------------------------------------------

	public static void addSignerIdType(String domainName, SignerIdTypeDTO sIdT) throws UnknownDomainException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("add signerId type " + sIdT.getName() + " to domain " + domainName);
			}
			DomainDTO domain = getDomainFIUO(domainName);
			domain.getSignerIdTypes().add(sIdT.getName());
			SIDT_CACHE.get(domainName).put(sIdT.getName(), new SignerIdTypeDTO(sIdT));
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("signerId type " + sIdT.getName() + " added to domain " + domainName);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static List<SignerIdTypeDTO> listSignerIdTypesCFEU()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<SignerIdTypeDTO> result = new ArrayList<>();
			for (String domainName : SIDT_CACHE.keySet())
			{
				for (SignerIdTypeDTO d : SIDT_CACHE.get(domainName).values())
				{
					result.add(new SignerIdTypeDTO(d));
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<SignerIdTypeDTO> listSignerIdTypesFIUO()
	{
		CACHE_RWL.readLock().lock();
		try
		{
			List<SignerIdTypeDTO> result = new ArrayList<>();
			for (String domainName : SIDT_CACHE.keySet())
			{
				for (SignerIdTypeDTO sIdT : SIDT_CACHE.get(domainName).values())
				{
					result.add(sIdT);
				}
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<SignerIdTypeDTO> listSignerIdTypesCFEU(String domainName) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<SignerIdTypeDTO> result = new ArrayList<>();
			for (SignerIdTypeDTO ct : SIDT_CACHE.get(domainName).values())
			{
				result.add(new SignerIdTypeDTO(ct));
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static List<SignerIdTypeDTO> listSignerIdTypesFIUO(String domainName) throws UnknownDomainException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			List<SignerIdTypeDTO> result = new ArrayList<>();
			for (SignerIdTypeDTO ct : SIDT_CACHE.get(domainName).values())
			{
				result.add(ct);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static SignerIdTypeDTO getSignerIdTypeCFEU(String domainName, String sIdTName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			return new SignerIdTypeDTO(getSignerIdTypeFIUO(domainName, sIdTName));
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static SignerIdTypeDTO getSignerIdTypeFIUO(String domainName, String sIdTName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		CACHE_RWL.readLock().lock();
		try
		{
			getDomainFIUO(domainName);
			SignerIdTypeDTO result = SIDT_CACHE.get(domainName).get(sIdTName);
			if (result == null)
			{
				String message = "signerId type " + sIdTName + " not found within cache for domain " + domainName;
				LOGGER.warn(message);
				throw new UnknownSignerIdTypeException(message);
			}
			return result;
		}
		finally
		{
			CACHE_RWL.readLock().unlock();
		}
	}

	public static void updateSignerIdType(String domainName, SignerIdTypeDTO sIdT) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			getSignerIdTypeFIUO(domainName, sIdT.getName());
			SIDT_CACHE.get(domainName).put(sIdT.getName(), new SignerIdTypeDTO(sIdT));
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}

	public static void removeSignerIdType(String domainName, String sIdTName) throws UnknownDomainException, UnknownSignerIdTypeException
	{
		CACHE_RWL.writeLock().lock();
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("remove signerId type " + sIdTName + " from domain " + domainName);
			}
			getSignerIdTypeFIUO(domainName, sIdTName);
			SIDT_CACHE.get(domainName).remove(sIdTName);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("signerId type " + sIdTName + " removed from domain " + domainName);
			}
		}
		finally
		{
			CACHE_RWL.writeLock().unlock();
		}
	}
}
