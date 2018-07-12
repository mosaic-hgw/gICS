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

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.PersistenceContext;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidConsentStatusException;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

@WebService(name = "gicsService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(GICSService.class)
@PersistenceContext(name = "gics")
/**
 * implementation of the gics-service
 *
 * @author geidell
 *
 */
public class GICSServiceImpl implements GICSService {

	private static final Logger logger = Logger.getLogger(GICSServiceImpl.class);
	@EJB
	private DAO dao;

	public GICSServiceImpl() {
	}

	@Override
	public void addConsent(ConsentDTO consentDTO) throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + consentDTO);
		}
		dao.addConsent(consentDTO);
		if (logger.isInfoEnabled()) {
			logger.info(consentDTO + " added");
		}
	}

	@Override
	public boolean isConsented(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config) throws UnknownDomainException,
			UnknownPolicyException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.isConsented(signerIdDTOs, policyKeyDTO, config);
	}

	@Override
	public boolean isConsentedFromIncludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.isConsentedFromIncludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public boolean isConsentedFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.isConsentedFromExcludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public boolean isConsentedFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.isConsentedFromIncludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public boolean isConsentedFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName, String versionFrom,
			String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.isConsentedFromExcludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public ConsentStatusType getConsentStatusType(Set<SignerIdDTO> signerIdDTOs, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get consent status type for ");
			sb.append(policyKeyDTO);
			sb.append(" signed by:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
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
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.getConsentStatusTypeFromIncludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.getConsentStatusTypeFromExcludingToIncluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.getConsentStatusTypeFromIncludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(Set<SignerIdDTO> signerIdDTOs, String domainName, String policyName,
			String versionFrom, String versionTo, CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		return dao.getConsentStatusTypeFromExcludingToExcluding(signerIdDTOs, domainName, policyName, versionFrom, versionTo, config);
	}

	@Override
	public List<ConsentTemplateDTO> listConsentTemplates(String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list consent templates for domain " + domainName);
		}
		return dao.listConsentTemplates(domainName);
	}

	@Override
	public List<ConsentTemplateDTO> listCurrentConsentTemplates(String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list current consent templates for domain " + domainName);
		}
		return dao.listCurrentConsentTemplates(domainName);
	}

	@Override
	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO)
			throws UnknownDomainException, UnknownModuleException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException,
			FreeTextConverterStringException, InvalidPropertiesException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + consentTemplateDTO);
		}
		dao.addConsentTemplate(consentTemplateDTO);
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
		dao.deleteConsentTemplate(keyDTO);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public List<ModuleDTO> listModules(String domainName) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list modules for domain " + domainName);
		}
		return dao.listModules(domainName);
	}

	@Override
	public void addModule(ModuleDTO moduleDTO)
			throws UnknownDomainException, UnknownPolicyException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + moduleDTO);
		}
		dao.addModule(moduleDTO);
		if (logger.isInfoEnabled()) {
			logger.info(moduleDTO + " added");
		}
	}

	@Override
	public void deleteModule(ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("delete " + keyDTO);
		}
		dao.deleteModule(keyDTO);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get module dto for " + keyDTO);
		}
		return dao.getModule(keyDTO);
	}

	@Override
	public List<PolicyDTO> listPolicies(String domainName) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("list policies for domain " + domainName);
		}
		return dao.listPolicies(domainName);
	}

	@Override
	public void addPolicy(PolicyDTO policyDTO)
			throws UnknownDomainException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + policyDTO);
		}
		dao.addPolicy(policyDTO);
		if (logger.isInfoEnabled()) {
			logger.info(policyDTO + " added");
		}
	}

	@Override
	public void deletePolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("delete " + keyDTO);
		}
		dao.deletePolicy(keyDTO);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public PolicyDTO getPolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get policy dto for " + keyDTO);
		}
		return dao.getPolicy(keyDTO);
	}

	@Override
	public ConsentTemplateDTO getConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get consent template dto for " + keyDTO);
		}
		return dao.getConsentTemplate(keyDTO);
	}

	@Override
	public List<SignedPolicyDTO> getPolicyStatesForSigner(String domainName, Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get policy states for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<SignedPolicyDTO> result = dao.getPolicyStatesForSigner(domainName, signerIdDTOs);
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
		List<SignedPolicyDTO> result = dao.getPolicyStatesForPolicyAndSigner(policyKeyDTO, signerIdDTOs);
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
		List<SignedPolicyDTO> result = dao.getPolicyStatesForPolicyNameAndSigner(domainName, policyName, signerIdDTOs);
		return result;
	}

	@Override
	public ConsentTemplateDTO getCurrentConsentTemplate(String consentTemplateName, String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException {
		if (logger.isInfoEnabled()) {
			logger.info("get newest consent template of '" + consentTemplateName + "' for domain '" + domainName + "'");
		}
		return dao.getCurrentConsentTemplate(consentTemplateName, domainName);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForPerson(String domainName, Set<SignerIdDTO> signerIdDTOs) throws UnknownDomainException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get all consents for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("within domain '" + domainName + "'");
			logger.info(sb.toString());
		}
		List<ConsentLightDTO> result = dao.getAllConsentsForPerson(domainName, signerIdDTOs);
		return result;
	}

	@Override
	public ConsentDTO getCurrentConsentForPersonAndCT(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs, boolean ignoreVersionNumber)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException, InconsistentStatusException {
		ConsentDTO result = null;
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("get current consent for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("and " + ctKeyDTO);
			logger.info(sb.toString());
		}
		result = dao.getCurrentConsentForPersonAndCT(ctKeyDTO, signerIdDTOs, ignoreVersionNumber);
		if (logger.isInfoEnabled()) {
			if (result != null) {
				StringBuilder sb = new StringBuilder("found " + result.getKey() + " for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
					sb.append(signerId + "\n");
				}
				sb.append("and " + ctKeyDTO);
				logger.info(sb.toString());
			} else {
				StringBuilder sb = new StringBuilder("no consent found for:\n");
				for (SignerIdDTO signerId : signerIdDTOs) {
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
			UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get all consents for consent template '" + ctKeyDTO);
		}
		return dao.getAllConsentsForConsentTemplate(ctKeyDTO);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomain(String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get all consents for domain '" + domainName + "'");
		}
		return dao.getAllConsentsForDomain(domainName);
	}

	@Override
	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get all consents for domain '" + domainName + "' which doesn't have a scan attached");
		}
		return dao.getAllConsentsForDomainWithoutScan(domainName);
	}

	@Override
	public List<ConsentLightDTO> getConsentsForDomainPaginated(String domainName, int firstEntry, int pageSize, ConsentField sortField,
			boolean sortIsAscending, HashMapWrapper<ConsentField, Object> filter)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		if (logger.isInfoEnabled()) {
			logger.info("get consents for domain '" + domainName + "', first entry " + firstEntry + ", page size " + pageSize);
		}
		// TODO filter
		List<ConsentLightDTO> result = dao.getConsentsForDomainPaginated(domainName, firstEntry, pageSize, sortField, sortIsAscending);
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " consents for domain " + domainName);
		}
		return result;
	}

	@Override
	public ConsentDTO getConsent(ConsentKeyDTO keyDTO) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException {
		if (logger.isInfoEnabled()) {
			logger.info("get consent template dto for " + keyDTO);
		}
		return dao.getConsent(keyDTO);
	}

	@Override
	public void addDomain(DomainDTO domainDTO) throws DuplicateEntryException, VersionConverterClassException {
		if (logger.isInfoEnabled()) {
			logger.info("create " + domainDTO);
		}
		dao.addDomain(domainDTO);
		if (logger.isInfoEnabled()) {
			logger.info(domainDTO + " created");
		}
	}

	@Override
	public void deleteDomain(String domainName) throws ObjectInUseException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("delete domain with name " + domainName);
		}
		dao.deleteDomain(domainName);
		if (logger.isInfoEnabled()) {
			logger.info("domain with name '" + domainName + "' deleted");
		}
	}

	@Override
	public DomainDTO getDomain(String domainName) throws UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("get domain with name " + domainName);
		}
		return dao.getDomain(domainName);
	}

	@Override
	public List<DomainDTO> listDomains() {
		if (logger.isInfoEnabled()) {
			logger.info("list all domains");
		}
		List<DomainDTO> result = dao.listDomains();
		if (logger.isInfoEnabled()) {
			logger.info("found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public void updateDomain(String domainName, String label, String logo, String externProperties, String comment) throws UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("update domain '" + domainName + "' with label '" + label + "' with extern properties '" + externProperties + "', comment '"
					+ comment + "' and " + ((logo != null && !logo.isEmpty()) ? "a logo" : "no logo"));
		}
		dao.updateDomain(domainName, label, logo, externProperties, comment);
		if (logger.isInfoEnabled()) {
			logger.info("domain '" + domainName + "' updated");
		}
	}

	@Override
	public void updateConsentTemplate(ConsentTemplateKeyDTO keyDTO, String externProperties, String comment, String scanBase64, String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException {
		if (logger.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer("update ");
			sb.append(keyDTO);
			sb.append(" with extern properties '");
			sb.append(externProperties);
			sb.append("', comment '");
			sb.append(comment);
			sb.append("' and ");
			sb.append(scanBase64 == null ? "no scan" : "a scan");
			sb.append(" with type '");
			sb.append(scanFileType);
			sb.append("'");
			logger.info(sb.toString());
		}
		dao.updateConsentTemplate(keyDTO, externProperties, comment, scanBase64, scanFileType);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void updatePolicy(PolicyKeyDTO keyDTO, String externProperties, String comment)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, VersionConverterClassException {
		if (logger.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer("update ");
			sb.append(keyDTO);
			sb.append(" with extern properties '");
			sb.append(externProperties);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("'");
			logger.info(sb.toString());
		}
		dao.updatePolicy(keyDTO, externProperties, comment);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void updateModule(ModuleKeyDTO keyDTO, String title, String externProperties, String comment)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException, VersionConverterClassException {
		if (logger.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer("update ");
			sb.append(keyDTO);
			sb.append(" with title '");
			sb.append(title);
			sb.append("', extern properties '");
			sb.append(externProperties);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("'");
			logger.info(sb.toString());
		}
		dao.updateModule(keyDTO, title, externProperties, comment);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void updateConsent(ConsentKeyDTO keyDTO, String externProperties, String comment, String scanBase64, String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer("update ");
			sb.append(keyDTO);
			sb.append(" with extern properties '");
			sb.append(externProperties);
			sb.append("' and comment '");
			sb.append(comment);
			sb.append("' and ");
			sb.append(scanBase64 == null ? "no scan" : "a scan");
			sb.append(" with type '");
			sb.append(scanFileType);
			sb.append("'");
			logger.info(sb.toString());
		}
		dao.updateConsent(keyDTO, externProperties, comment, scanBase64, scanFileType);
		if (logger.isInfoEnabled()) {
			logger.info(keyDTO + " updated");
		}
	}

	@Override
	public void addScanToConsent(ConsentKeyDTO consentKeyDTO, String scanBase64, String fileType)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, DuplicateEntryException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException {
		if (logger.isInfoEnabled()) {
			logger.info("add scan to " + consentKeyDTO);
		}
		dao.addScanToConsent(consentKeyDTO, scanBase64, fileType);
	}

	@Override
	public void addSignerIdType(String domainName, String signerIdTypeName) throws UnknownDomainException, DuplicateEntryException {
		if (logger.isInfoEnabled()) {
			logger.info("add signer id type '" + signerIdTypeName + "' to domain '" + domainName);
		}
		dao.addSignerIdType(domainName, signerIdTypeName);
		if (logger.isInfoEnabled()) {
			logger.info("signer id type '" + signerIdTypeName + "' successfully added to domain '" + domainName);
		}
	}

	@Override
	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException {
		if (logger.isInfoEnabled()) {
			logger.info("remove signer id type '" + signerIdTypeName + "' from domain '" + domainName);
		}
		dao.deleteSignerIdType(domainName, signerIdTypeName);
	}

	@Override
	public List<String> getAllIdsForSignerIdType(String domainName, String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException {
		List<String> result = dao.getAllIdsForSignerIdType(domainName, signerIdTypeName);
		return result;
	}

	@Override
	public List<String> getAllConsentedIdsFor(String signerIdTypeName, PolicyKeyDTO policyKeyDTO, CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, VersionConverterClassException,
			InvalidVersionException, InconsistentStatusException {
		List<String> result = dao.getAllConsentedIdsFor(signerIdTypeName, policyKeyDTO, config);
		return result;
	}

	@Override
	public void validateConsent(ConsentDTO consentDTO, boolean allowRevoke)
			throws InvalidVersionException, VersionConverterClassException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("validate " + consentDTO);
		}
		dao.validateConsent(consentDTO, allowRevoke);
		if (logger.isInfoEnabled()) {
			logger.info(consentDTO + " is valide");
		}
	}

	@Override
	public void invalidateConsent(ConsentKeyDTO consentKeyDTO, ConsentStatus invalidationState, String comment, String scanBase64)
			throws InvalidVersionException, InvalidConsentStatusException, VersionConverterClassException, UnknownConsentException,
			UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException {
		if (logger.isInfoEnabled()) {
			logger.info("invalidate " + consentKeyDTO + ", new consent status: " + invalidationState);
		}
		if (!ConsentStatusType.DECLINED.equals(invalidationState.getConsentStatusType())) {
			String message = "invalidationState need to be one of: " + ConsentStatus.getStringForType(ConsentStatusType.DECLINED) + " but is "
					+ invalidationState;
			logger.warn(message);
			throw new InvalidConsentStatusException(message);
		}

		dao.invalidateConsent(consentKeyDTO, invalidationState, comment, scanBase64);
	}

	@Override
	public void refuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs) throws InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("refuse ");
			sb.append(ctKeyDTO);
			sb.append(" for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			logger.info(sb.toString());
		}
		dao.refuseConsent(ctKeyDTO, signerIdDTOs);
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder(ctKeyDTO.toString());
			sb.append(" for:\n");
			for (SignerIdDTO signerId : signerIdDTOs) {
				sb.append(signerId + "\n");
			}
			sb.append("refused");
			logger.info(sb.toString());
		}
	}

	/**
	 * add given signerID to given consent
	 */
	@Override
	public void addSignerIdToConsent(ConsentKeyDTO consentKeyDTO, SignerIdDTO signerIdDTO)
			throws InvalidVersionException, VersionConverterClassException, UnknownConsentException, UnknownSignerIdTypeException,
			UnknownConsentTemplateException, UnknownDomainException, InternalException {
		if (logger.isInfoEnabled()) {
			logger.info("add " + signerIdDTO + " to " + consentKeyDTO);
		}

		if (consentKeyDTO == null || signerIdDTO == null) {
			String msg = "unable to addSignerId. The given parameter consentKey or signerId is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.addSignerIdToConsent(consentKeyDTO, signerIdDTO);
	}

	@Override
	public Date getExpirationDate(ConsentKeyDTO consentKeyDTO) throws InvalidVersionException, VersionConverterClassException,
			UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException {
		Date result = null;
		if (logger.isDebugEnabled()) {
			logger.debug("getting expiration date for " + consentKeyDTO);
		}
		result = dao.getExpirationDate(consentKeyDTO, result);
		if (logger.isDebugEnabled()) {
			logger.debug("expiration date for " + consentKeyDTO + " is " + result);
		}
		return result;
	}

	@Override
	public void addSignerIdToSignerId(String domain, SignerIdDTO existentSignerIdDTO, SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, IllegalArgumentException, InternalException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("add signedId " + newSignerIdDTO + " to virtual person using signerid " + existentSignerIdDTO);
		}

		if (existentSignerIdDTO == null || newSignerIdDTO == null) {
			String msg = "unable to addSignerId. The given parameter newSignerIdDTO or existentSignerIdDTO is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		dao.addSignerIdToSignerId(domain, existentSignerIdDTO, newSignerIdDTO);

		if (logger.isInfoEnabled()) {
			logger.info("added signedId " + newSignerIdDTO + " to virtual person using signerid " + existentSignerIdDTO);
		}
	}
}
