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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

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
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
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

@WebService
public interface GICSService {

	/**
	 * stores the given consent<br>
	 * the policies within the modulDTOs are ignored and therefore doesn't need to be set
	 *
	 * @param consentDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws UnknownConsentTemplateException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws MissingRequiredObjectException
	 *             when there's no given value for a mandatory free text field
	 * @throws InvalidFreeTextException
	 * @throws MandatoryFieldsException
	 *             see {@link MandatoryFieldsException}
	 * @throws UnknownSignerIdTypeException
	 * @throws DuplicateEntryException
	 */
	public void addConsent(@XmlElement(required = true) @WebParam(name = "consentDTO") ConsentDTO consentDTO) throws UnknownDomainException,
			UnknownModuleException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException,
			MissingRequiredObjectException, InvalidFreeTextException, MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given specific version
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param policyKeyDTO
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public boolean isConsented(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from <= version <= to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public boolean isConsentedFromIncludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from < version <= to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public boolean isConsentedFromExcludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from <= version < to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public boolean isConsentedFromIncludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from < version < to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public boolean isConsentedFromExcludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given specific version
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param policyKeyDTO
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentStatusType getConsentStatusType(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from <= version <= to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from < version <= to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from <= version < to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version range: from < version < to<br>
	 * ignores the config parameter "ignoreVersionNumber"
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param domainName
	 * @param policyName
	 * @param versionFrom
	 * @param versionTo
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * lists all consent templates of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public List<ConsentTemplateDTO> listConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException;

	/**
	 * lists the most recent version of all consent templates of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public List<ConsentTemplateDTO> listCurrentConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException;

	/**
	 * stores the given consent template<br>
	 * the policies within the modulDTOs are ignored and therefore doesn't need to be set
	 *
	 * @param consentTemplateDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws DuplicateEntryException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws FreeTextConverterStringException
	 *             is thrown when the given converter string (in case of {@link FreeTextType}.Date) is not a valid string for {@link SimpleDateFormat}
	 * @throws InvalidPropertiesException
	 */
	public void addConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO)
			throws UnknownDomainException, UnknownModuleException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException,
			FreeTextConverterStringException, InvalidPropertiesException;

	/**
	 * deletes the given consent template (only if it's not in use, i.e. there's no consent belonging to that consent template)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws ObjectInUseException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void deleteConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, ObjectInUseException, VersionConverterClassException,
			InvalidVersionException;

	/**
	 * lists all modules of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public List<ModuleDTO> listModules(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException;

	/**
	 * stores the given module
	 *
	 * @param policyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws DuplicateEntryException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void addModule(@XmlElement(required = true) @WebParam(name = "module") ModuleDTO moduleDTO)
			throws UnknownDomainException, UnknownPolicyException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException;

	/**
	 * deletes the given module (only if it's not in use, i.e. there's no consent template linked to that module)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws ObjectInUseException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void deleteModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, VersionConverterClassException, InvalidVersionException;

	/**
	 * returns all attributes of the requested module
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, VersionConverterClassException, InvalidVersionException;

	/**
	 * lists all policies of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public List<PolicyDTO> listPolicies(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException;

	/**
	 * stores the given policy
	 *
	 * @param policyDTO
	 * @throws UnknownDomainException
	 * @throws DuplicateEntryException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void addPolicy(@XmlElement(required = true) @WebParam(name = "policy") PolicyDTO policyDTO)
			throws UnknownDomainException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException;

	/**
	 * deletes the given policy (only if it's not in use, i.e. there's no module linked to that policy)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws ObjectInUseException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void deletePolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, VersionConverterClassException, InvalidVersionException;

	/**
	 * returns all attributes of the requested policy
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public PolicyDTO getPolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException, InvalidVersionException;

	/**
	 * returns all attributes of the requested consent template
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public ConsentTemplateDTO getConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException;

	/**
	 * returns all attributes of the current (highest version) consent template with the given name
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public ConsentTemplateDTO getCurrentConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateName") String consentTemplateName,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException;

	/**
	 * returns all signed policies which are signed by the person with the given id
	 *
	 * @param domainName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public List<SignedPolicyDTO> getPolicyStatesForSigner(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns all signed policies for the given policy which are signed by the person with the given id
	 *
	 * @param policyKeyDTO
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public List<SignedPolicyDTO> getPolicyStatesForPolicyAndSigner(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns all signed policies for the given policy name (thus ignoring the version number of the policy) which are signed by the person with the given id
	 *
	 * @param domainName
	 * @param policyName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSigner(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * lists all consents for the person with the given id
	 *
	 * @param domainName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	public List<ConsentLightDTO> getAllConsentsForPerson(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs) throws UnknownDomainException,
			VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException;

	/**
	 * returns the current consent for the given signerIds<br>
	 * current means: the consent with the highest versionNumber of the associated ConsentTemplate; if there are more then one, the newest (ConsentDate) of them<br>
	 * if "ignoreVersionNumber" is true, then the newest of all consents belonging to the signerIds, regardless of the VersionNumber of the associated ConsentTemplate
	 *
	 * @param ctKeyDTO
	 * @param signerIdDTOs
	 * @param ignoreVersionNumber
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	public ConsentDTO getCurrentConsentForPersonAndCT(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "ignoreVersionNumber") boolean ignoreVersionNumber)
			throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException, InconsistentStatusException;

	/**
	 * lists all consents for the given consent template
	 *
	 * @param ctKeyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	public List<ConsentLightDTO> getAllConsentsForConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException;

	/**
	 * lists all consents for the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	public List<ConsentLightDTO> getAllConsentsForDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException;

	/**
	 * lists all consents for the given domain which doesn't have a scan attached
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	public List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException;

	/**
	 * lists consents for the given domain with pagination
	 *
	 * @param domainName
	 * @param pageNumber
	 *            starts with 0
	 * @param pageSize
	 * @param sortField
	 *            see {@link ConsentField}
	 * @param sortIsAscending
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	public List<ConsentLightDTO> getConsentsForDomainPaginated(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "firstEntry") int firstEntry,
			@XmlElement(required = true) @WebParam(name = "pageSize") int pageSize,
			@XmlElement(required = true) @WebParam(name = "sortField") ConsentField sortField,
			@XmlElement(required = true) @WebParam(name = "sortIsAscending") boolean sortIsAscending,
			@XmlElement(required = true) @WebParam(name = "filter") HashMapWrapper<ConsentField, Object> filter)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException;

	/**
	 * get the complete dto for the given key - thought to be used in addition to the list functions
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 */
	public ConsentDTO getConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO keyDTO)
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException,
			UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException;

	/**
	 * creates a new domain
	 *
	 * @param domainDTO
	 *            see {@link DomainDTO}
	 * @throws DuplicateEntryException
	 *             if a domain with that name already exists
	 * @throws VersionConverterClassException
	 */
	public void addDomain(@XmlElement(required = true) @WebParam(name = "domain") DomainDTO domainDTO)
			throws DuplicateEntryException, VersionConverterClassException;

	/**
	 * deletes the given domain
	 *
	 * @param domainName
	 *            identifier
	 * @throws ObjectInUseException
	 *             if there's at least one pseudonym within that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void deleteDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws ObjectInUseException, UnknownDomainException;

	/**
	 * returns all information for the given domain
	 *
	 * @param domainName
	 *            identifier
	 * @return see {@link DomainDTO}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public DomainDTO getDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws UnknownDomainException;

	/**
	 * @return list of all domains within the database; see {@link DomainDTO}
	 */
	public List<DomainDTO> listDomains();

	/**
	 * updates the given domain
	 *
	 * @param domainName
	 *            identifier
	 * @param label
	 * @param logo
	 * @param externProperties
	 * @param comment
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void updateDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "label") String label, @XmlElement(required = true) @WebParam(name = "logo") String logo,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment) throws UnknownDomainException;

	/**
	 * updates the given consent template
	 *
	 * @param consentTemplateKey
	 *            identifier
	 * @param externProperties
	 * @param comment
	 * @param scanBase64
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownConsentTemplateException
	 *             if the given consent template is not found
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void updateConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO consentTemplateKey,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "scanBase64") String scanBase64,
			@XmlElement(required = true) @WebParam(name = "scanFileType") String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, VersionConverterClassException;

	/**
	 * updates the given policy
	 *
	 * @param policyKey
	 *            identifier
	 * @param label
	 * @param logo
	 * @param externProperties
	 * @param comment
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownPolicyException
	 *             if the given policy is not found
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void updatePolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKey,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException, VersionConverterClassException;

	/**
	 * updates the given module
	 *
	 * @param moduleKey
	 *            identifier
	 * @param label
	 * @param logo
	 * @param externProperties
	 * @param comment
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownModuleException
	 *             if the given module is not found
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void updateModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO moduleKey,
			@XmlElement(required = true) @WebParam(name = "title") String title,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException, VersionConverterClassException;

	/**
	 * updates the given consent
	 *
	 * @param consentKey
	 *            identifier
	 * @param externProperties
	 * @param comment
	 * @param scanBase64
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownConsentTemplateException
	 *             if the given consent template is not found
	 * @throws UnknownConsentException
	 *             if the given consent is not found
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	public void updateConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKey,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "scanBase64") String scanBase64,
			@XmlElement(required = true) @WebParam(name = "scanFileType") String scanFileType)
			throws UnknownDomainException, UnknownConsentTemplateException, UnknownConsentException, InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException;

	/**
	 * add the given scan to the given consent
	 *
	 * @deprecated since 2.5.0 - use {@link GICSService.updateConsent} instead
	 * @param consentKeyDTO
	 * @param scanBase64
	 * @param fileType
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws DuplicateEntryException
	 *             if there's already a scan attached to that consent
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@Deprecated
	public void addScanToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "scanBase64") String scanBase64,
			@XmlElement(required = true) @WebParam(name = "fileType") String fileType) throws UnknownDomainException, UnknownConsentTemplateException,
			UnknownConsentException, DuplicateEntryException, VersionConverterClassException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * add a signer id type with the given name to the given domain
	 *
	 * @param domainName
	 * @param name
	 * @throws UnknownDomainException
	 * @throws DuplicateEntryException
	 */
	public void addSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, DuplicateEntryException;

	/**
	 * removes the given signer id type from the given domain
	 *
	 * @param domainName
	 * @param name
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 * @throws ObjectInUseException
	 */
	public void deleteSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException;

	/**
	 * returns all id for the given signer id type and domain
	 *
	 * @param domainName
	 * @param signerIdType
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 */
	public List<String> getAllIdsForSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * list all consented ids for the given policy and signerIdType
	 *
	 * @param signerIdType
	 * @param policyKey
	 * @param config
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownPolicyException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public List<String> getAllConsentedIdsFor(@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, VersionConverterClassException,
			InvalidVersionException, InconsistentStatusException;

	/**
	 * validates the given consentDTO<br>
	 * <li>checks the given domain, consent template and assures, that the given modules are part of the consent template
	 * <li>all mandatory modules need to be either accepted or declined
	 * <li>when there's at least one declined mandatory module, no module may be accepted
	 * <li>no child module may be accepted if its parent is not
	 * <li>when there's at least one accepted module, the two signatures have to be provided
	 *
	 * @param consentDTO
	 * @param allowRevoke
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 * @throws MissingRequiredObjectException
	 * @throws MandatoryFieldsException
	 * @throws UnknownModuleException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	public void validateConsent(@XmlElement(required = true) @WebParam(name = "consent") ConsentDTO consentDTO,
			@XmlElement(required = true) @WebParam(name = "allowRevoke") boolean allowRevoke)
			throws InvalidVersionException, VersionConverterClassException, MissingRequiredObjectException, MandatoryFieldsException,
			UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * invalidates the given consent (stores a new consent with all policies set to invalidationState)
	 *
	 * @param consentKey
	 * @param invalidationState
	 *            new state for all policies, need to be of type {@link ConsentStatusType.DECLINED}
	 * @param comment
	 * @param scanBase64
	 * @throws InvalidVersionException
	 * @throws InvalidConsentStatusException
	 *             if invalidationState is not of type {@link ConsentStatusType.DECLINED}
	 * @throws VersionConverterClassException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws InternalException
	 */
	public void invalidateConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "invalidationState") ConsentStatus invalidationState,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = false) @WebParam(name = "scanBase64") String scanBase64)
			throws InvalidVersionException, InvalidConsentStatusException, VersionConverterClassException, UnknownConsentException,
			UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InternalException;

	/**
	 * stores a "refused" consent - the consent is not declined, the participant didn't want to fill a consent<br>
	 * the date of the refused consent is set to the current date
	 *
	 * @param ctKeyDTO
	 * @param signerIdDTOs
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InternalException
	 */
	public void refuseConsent(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs) throws InvalidVersionException,
			VersionConverterClassException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException, InternalException;

	/**
	 * adds the given signerId to the given consent
	 *
	 * @param consentKeyDTO
	 * @param signerIdDTO
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InternalException
	 */
	public void addSignerIdToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerId") SignerIdDTO signerIdDTO)
			throws InvalidVersionException, VersionConverterClassException, UnknownConsentException, UnknownSignerIdTypeException,
			UnknownConsentTemplateException, IllegalArgumentException, UnknownDomainException, InternalException;

	/**
	 * adds newSignerId (e.g. case number) to one or more virtual persons (which are using the given existentSignerId (e.g. pseudonym))
	 * 
	 * @param existentSignerIdDTO
	 *            existing signerID identifying used by one or more virtual persons
	 * @param newSignerIdDTO
	 *            new signerID to be added to identified virtual persons
	 * @throws UnknownSignerIdTypeException
	 * @throws IllegalArgumentException
	 * @throws InternalException
	 * @throws UnknownDomainException
	 */
	public void addSignerIdToSignerId(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "existentSignerId") SignerIdDTO existentSignerIdDTO,
			@XmlElement(required = true) @WebParam(name = "newSignerId") SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, IllegalArgumentException, InternalException, UnknownDomainException;

	/**
	 * returns the expiration date for the given consent<br>
	 * returns a date in the future (aprox. + 1000 years), if no expiration date is set
	 *
	 * @param consentKey
	 * @return
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InternalException
	 */
	public Date getExpirationDate(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO)
			throws InvalidVersionException, VersionConverterClassException, UnknownConsentException, UnknownSignerIdTypeException,
			UnknownConsentTemplateException, UnknownDomainException, InternalException;
}
