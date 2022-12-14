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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
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

@WebService(name = "gicsService")
public interface GICSService
{
	/**
	 * Stores the given consent.
	 * The policies within the modulDTOs are ignored and therefore doesn't need to be set.
	 *
	 * @param consentDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidVersionException
	 * @throws MissingRequiredObjectException
	 *             when there's no given value for a mandatory free text field
	 * @throws InvalidFreeTextException
	 * @throws MandatoryFieldsException
	 *             see {@link MandatoryFieldsException}
	 * @throws UnknownSignerIdTypeException
	 * @throws DuplicateEntryException
	 * @throws RequirementsNotFullfilledException
	 * @throws InvalidParameterException
	 */
	void addConsent(
			@XmlElement(required = true) @WebParam(name = "consent") ConsentDTO consentDTO)
			throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException,
			InternalException, RequirementsNotFullfilledException, InvalidParameterException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given
	 * specific version
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param policyKeyDTO
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsented(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from <= version <= to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromIncludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from < version <= to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromExcludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from <= version < to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromIncludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from < version < to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromExcludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given
	 * specific version
	 *
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param policyKeyDTO
	 * @param config
	 *            see {@link CheckConsentConfig}
	 * @return see {@link ConsentStatusType}
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusType(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from <= version <= to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusTypeFromIncludingToIncluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from < version <= to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusTypeFromExcludingToIncluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from <= version < to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusTypeFromIncludingToExcluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * checks whether there's a signed consent for the given patient and policy in the given version
	 * range: from < version < to<br>
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
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusTypeFromExcludingToExcluding(
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * lists all consent templates of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consentTemplates")
	List<ConsentTemplateDTO> listConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException;

	/**
	 * lists the most recent version of all consent templates of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "currentConsentTemplates")
	List<ConsentTemplateDTO> listCurrentConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, InvalidVersionException;

	/**
	 * stores the given consent template<br>
	 * the policies within the modulDTOs are ignored and therefore doesn't need to be set
	 *
	 * @param consentTemplateDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws DuplicateEntryException
	 * @throws InvalidVersionException
	 * @throws FreeTextConverterStringException
	 *             is thrown when the given converter string (in case of {@link FreeTextType}.Date)
	 *             is not a valid string for {@link SimpleDateFormat}
	 * @throws InvalidPropertiesException
	 * @throws RequirementsNotFullfilledException
	 * @throws InvalidParameterException
	 */
	void addConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws UnknownDomainException, UnknownModuleException, DuplicateEntryException, InvalidVersionException,
			FreeTextConverterStringException, InvalidPropertiesException, RequirementsNotFullfilledException, InvalidParameterException;

	/**
	 * deletes the given consent template (only if it's not in use, i.e. there's no consent
	 * belonging to that consent template)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws ObjectInUseException
	 * @throws InvalidVersionException
	 */
	void deleteConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, ObjectInUseException, InvalidVersionException;

	/**
	 * lists all modules of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "modules")
	List<ModuleDTO> listModules(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException;

	/**
	 * stores the given module
	 *
	 * @param moduleDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws DuplicateEntryException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 */
	void addModule(@XmlElement(required = true) @WebParam(name = "module") ModuleDTO moduleDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws UnknownDomainException, UnknownPolicyException, DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException;

	/**
	 * deletes the given module (only if it's not in use, i.e. there's no consent template linked to
	 * that module)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws ObjectInUseException
	 * @throws InvalidVersionException
	 */
	void deleteModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, InvalidVersionException;

	/**
	 * returns all attributes of the requested module
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws InvalidVersionException
	 */
	ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, InvalidVersionException;

	/**
	 * lists all policies of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "policies")
	List<PolicyDTO> listPolicies(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidVersionException;

	/**
	 * stores the given policy
	 *
	 * @param policyDTO
	 * @throws UnknownDomainException
	 * @throws DuplicateEntryException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 */
	void addPolicy(@XmlElement(required = true) @WebParam(name = "policy") PolicyDTO policyDTO) throws UnknownDomainException,
			DuplicateEntryException, InvalidVersionException, RequirementsNotFullfilledException;

	/**
	 * deletes the given policy (only if it's not in use, i.e. there's no module linked to that
	 * policy)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws ObjectInUseException
	 * @throws InvalidVersionException
	 */
	void deletePolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, InvalidVersionException;

	/**
	 * returns all attributes of the requested policy
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws InvalidVersionException
	 */
	PolicyDTO getPolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidVersionException;

	/**
	 * returns all attributes of the requested consent template
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidVersionException
	 */
	ConsentTemplateDTO getConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException;

	/**
	 * returns all attributes of the current (highest version) consent template with the given name
	 *
	 * @param consentTemplateName
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidVersionException
	 */
	ConsentTemplateDTO getCurrentConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateName") String consentTemplateName,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException;

	/**
	 * returns all signed policies which are signed by any person with at least one of the given ids
	 *
	 * @param domainName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param useAliases
	 *            check aliases for related signerIds
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns all signed policies for the given policy which are signed by any person with at least
	 * one of the given ids
	 *
	 * @param policyKeyDTO
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param useAliases
	 *            check aliases for related signerIds
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns all signed policies for the given policy name (thus ignoring the version number of
	 * the policy) which are signed by the person with the given id
	 *
	 * @param domainName
	 * @param policyName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param useAliases
	 *            check aliases for related signerIds
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns the current signed policies which are signed by any person with the given ids (depends on {@link CheckConsentConfig#getIdMatchingType()})
	 *
	 * @param domainName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param config
	 *            see {@link CheckConsentConfig}<br>
	 *            {@link CheckConsentConfig#getIgnoreVersionNumber()} is ignored
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * lists all consents for the person with the given id
	 *
	 * @param domainName
	 * @param signerIdDTOs
	 *            ids of the signer (patient)
	 * @param useAliases
	 *            check aliases for related signerIds
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException;

	/**
	 * returns the current consent for the given signerIds<br>
	 * current means: the consent with the highest versionNumber of the associated ConsentTemplate;
	 * if there are more then one, the newest (ConsentDate) of them<br>
	 * if "ignoreVersionNumber" is true, then the newest of all consents belonging to the signerIds,
	 * regardless of the VersionNumber of the associated ConsentTemplate
	 *
	 * @param ctKeyDTO
	 * @param signerIdDTOs
	 * @param ignoreVersionNumber
	 * @param useAliases
	 *            check aliases for related signerIds
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	ConsentDTO getCurrentConsentForSignerIdsAndCT(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "ignoreVersionNumber") boolean ignoreVersionNumber,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException;

	/**
	 * lists all consents for the given consent template
	 *
	 * @param ctKeyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidVersionException, InconsistentStatusException;

	/**
	 * lists all consents for the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws InconsistentStatusException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException;

	/**
	 * lists all consents for the given domain paginated
	 *
	 * @param domainName
	 * @param config
	 *            see {@link PaginationConfig}
	 * @return
	 * @throws InconsistentStatusException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getConsentsForDomainPaginated(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config)
			throws InconsistentStatusException, InvalidParameterException, InvalidVersionException, UnknownDomainException;

	/**
	 * count all consents for the given domain with the given filter
	 *
	 * @param domainName
	 * @param config
	 *            see {@link PaginationConfig}, only {@link PaginationConfig#getFilter()},
	 *            {@link PaginationConfig#isFilterFieldsAreTreatedAsConjunction()} and
	 *            {@link PaginationConfig#isFilterIsCaseSensitive()} are
	 *            used
	 * @return count with consents within domain with consideration of given filter criterion
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 */
	long countConsentsForDomainWithFilter(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException;

	/**
	 * lists all consents for the given domain which doesn't have a scan attached
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException;

	/**
	 * get the complete dto for the given key - thought to be used in addition to the list functions
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentDTO getConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO keyDTO)
			throws UnknownDomainException, InvalidVersionException, InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException;

	/**
	 * creates a new domain
	 *
	 * @param domainDTO
	 *            see {@link DomainDTO}
	 * @throws DuplicateEntryException
	 *             if a domain with that name already exists
	 * @throws VersionConverterClassException
	 */
	void addDomain(@XmlElement(required = true) @WebParam(name = "domain") DomainDTO domainDTO)
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
	void deleteDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
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
	DomainDTO getDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws UnknownDomainException;

	/**
	 * @return list of all domains within the database; see {@link DomainDTO}
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "domains")
	List<DomainDTO> listDomains();

	/**
	 * updates the given domain
	 *
	 * @param domainDTO
	 * @throws ObjectInUseException
	 * @throws UnknownDomainException
	 */
	void updateDomain(@XmlElement(required = true) @WebParam(name = "domain") DomainDTO domainDTO)
			throws ObjectInUseException, UnknownDomainException;

	/**
	 * updates the given domain with the given values
	 *
	 * @param domainName
	 * @param label
	 * @param logo
	 * @param externProperties
	 * @param comment
	 * @throws UnknownDomainException
	 */
	void updateDomainInUse(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "label") String label, @XmlElement(required = true) @WebParam(name = "logo") String logo,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment) throws UnknownDomainException;

	/**
	 * updates the given policy
	 *
	 * @param policyDTO
	 * @throws InvalidVersionException
	 * @throws ObjectInUseException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void updatePolicy(@XmlElement(required = true) @WebParam(name = "domainName") PolicyDTO policyDTO)
			throws InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException;

	/**
	 * updates the given policy with the given values
	 *
	 * @param policyKeyDTO
	 * @param label
	 * @param externProperties
	 * @param comment
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void updatePolicyInUse(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException;

	/**
	 * updates the given module
	 *
	 * @param moduleDTO
	 * @throws DuplicateEntryException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws ObjectInUseException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws UnknownPolicyException
	 */
	void updateModule(@XmlElement(required = true) @WebParam(name = "module") ModuleDTO moduleDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws DuplicateEntryException, InvalidParameterException, InvalidVersionException, ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException,
			UnknownModuleException, UnknownPolicyException;

	/**
	 * updates the given module with the given values
	 *
	 * @param moduleKeyDTO
	 * @param label
	 * @param comment
	 * @param shortText
	 * @param externProperties
	 * @param assignedPolicyDTOs
	 *            only externProperties and comments of this objects are updated
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 */
	void updateModuleInUse(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO moduleKeyDTO,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "shortText") String shortText,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "assignedPolicies") Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException;

	/**
	 * updates the given consent template
	 *
	 * @param consentTemplateDTO
	 * @throws DuplicateEntryException
	 * @throws FreeTextConverterStringException
	 * @throws InvalidFreeTextException
	 * @throws InvalidPropertiesException
	 * @throws InvalidVersionException
	 * @throws ObjectInUseException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws InvalidParameterException
	 */
	void updateConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities) throws DuplicateEntryException,
			FreeTextConverterStringException, InvalidFreeTextException, InvalidPropertiesException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException;

	/**
	 * updates label, title, comment, externProperties, scanBase64, scanFileType,
	 * assignedModule.comment, assignedModule.externProperties, freeTextDef.comment
	 *
	 * @param consentTemplateDTO
	 * @throws InvalidFreeTextException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws InvalidParameterException
	 */
	void updateConsentTemplateInUse(@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO)
			throws InvalidFreeTextException, InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException, InvalidParameterException;

	/**
	 * Updates the given consent.
	 *
	 * @param consentKeyDTO
	 * @param externProperties
	 * @param comment
	 * @param scan
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentException
	 * @throws InvalidParameterException
	 */
	void updateConsentInUse(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "scan") ConsentScanDTO scan)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException;

	/**
	 * finalises the given domain
	 *
	 * @param domainName
	 * @throws UnknownDomainException
	 */
	void finaliseDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws UnknownDomainException;

	/**
	 * finalises the given policy
	 *
	 * @param policyKeyDTO
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void finalisePolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO)
			throws InvalidVersionException, UnknownDomainException, UnknownPolicyException;

	/**
	 * finalises the given module
	 *
	 * @param moduleKeyDTO
	 * @param finaliseRelatedEntities
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 */
	void finaliseModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO moduleKeyDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException;

	/**
	 * finalises the given consent template
	 *
	 * @param consentTemplateKeyDTO
	 * @param finaliseRelatedEntities
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void finaliseTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO consentTemplateKeyDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * add the given scan to the given consent
	 *
	 * @param consentKeyDTO
	 * @param scanBase64
	 * @param fileType
	 * @param fileName
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws DuplicateEntryException
	 *             if there's already a scan attached to that consent
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidParameterException
	 */
	void addScanToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "scanBase64") String scanBase64,
			@XmlElement(required = true) @WebParam(name = "fileType") String fileType,
			@XmlElement(required = true) @WebParam(name = "fileName") String fileName) throws UnknownDomainException, UnknownConsentTemplateException,
			UnknownConsentException, DuplicateEntryException, InvalidVersionException, UnknownSignerIdTypeException, InvalidParameterException;

	/**
	 * Remove a scan from the given consent
	 *
	 * @param consentKeyDTO
	 * @param fhirId
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws DuplicateEntryException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidParameterException
	 */
	void removeScanFromConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "fhirId") String fhirId) throws UnknownDomainException, UnknownConsentTemplateException,
			UnknownConsentException, DuplicateEntryException, InvalidVersionException, UnknownSignerIdTypeException, InvalidParameterException;

	/**
	 * add a signer id type with the given name to the given domain
	 *
	 * @param domainName
	 * @param signerIdTypeName
	 *            identifier
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws DuplicateEntryException
	 *             if the signerIdType already exists
	 */
	void addSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, DuplicateEntryException;

	/**
	 * returns all information for the given signerIdType
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @return see {@link SignerIdTypeDTO}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownSignerIdTypeException
	 *             if the given signerIdType is not found
	 */
	SignerIdTypeDTO getSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * updates the given signerIdType with the given values
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @param label
	 * @param comment
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownSignerIdTypeException
	 *             if the given signerIdType is not found
	 */
	void updateSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "comment") String comment) throws UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * list all signerIDTypes for the given domain
	 *
	 * @param domainName
	 *            domain to which the signerIdTypes belongs to
	 * @return list of all domains within the database; see {@link SignerIdTypeDTO}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "sinerIdTypes")
	List<SignerIdTypeDTO> listSignerIdTypes(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException;

	/**
	 * removes the given signer id type from the given domain
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 * @throws ObjectInUseException
	 */
	void deleteSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException;

	/**
	 * returns all id for the given signer id type and domain
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier of the signerIdType
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signerIds")
	List<String> getAllIdsForSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * list all consented ids for the given policy and signerIdType
	 *
	 * @param signerIdTypeName
	 * @param policyKeyDTO
	 * @param config
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownPolicyException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consentIds")
	List<String> getAllConsentedIdsFor(@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, InvalidVersionException, InconsistentStatusException;

	/**
	 * validates the given consentDTO<br>
	 * <li>checks the given domain, consent template and assures, that the given modules are part of
	 * the consent template
	 * <li>all mandatory modules need to be either accepted or declined
	 * <li>when there's at least one declined mandatory module, no module may be accepted
	 * <li>no child module may be accepted if its parent is not
	 * <li>when there's at least one accepted module, the two signatures have to be provided
	 *
	 * @param consentDTO
	 * @param allowRevoke
	 * @throws InvalidVersionException
	 * @throws MissingRequiredObjectException
	 * @throws MandatoryFieldsException
	 * @throws UnknownModuleException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void validateConsent(@XmlElement(required = true) @WebParam(name = "consent") ConsentDTO consentDTO,
			@XmlElement(required = true) @WebParam(name = "allowRevoke") boolean allowRevoke)
			throws InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * Stores a "refused" consent.
	 * The consent is not declined, the participant didn't want to fill a consent.<br>
	 * The date of the refused consent is set to the current date.
	 *
	 * @param ctKeyDTO
	 * @param signerIdDTOs
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InternalException
	 * @throws InvalidParameterException
	 */
	void refuseConsent(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException,
			UnknownDomainException, InternalException, InvalidParameterException;

	/**
	 * adds the given signerId to the given consent
	 *
	 * @param consentKeyDTO
	 * @param signerIdDTO
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InternalException
	 * @throws InvalidParameterException
	 */
	void addSignerIdToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerId") SignerIdDTO signerIdDTO)
			throws InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, IllegalArgumentException, UnknownDomainException, InternalException,
			InvalidParameterException;

	/**
	 * adds newSignerId (e.g. case number) to one or more virtual persons (which are using the given
	 * existentSignerId (e.g. pseudonym))
	 *
	 * @param existentSignerIdDTO
	 *            existing signerID identifying used by one or more virtual persons
	 * @param newSignerIdDTO
	 *            new signerID to be added to identified virtual persons
	 * @throws UnknownSignerIdTypeException
	 * @throws IllegalArgumentException
	 * @throws InternalException
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 */
	void addSignerIdToSignerId(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "existentSignerId") SignerIdDTO existentSignerIdDTO,
			@XmlElement(required = true) @WebParam(name = "newSignerId") SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, IllegalArgumentException, InternalException, UnknownDomainException, InvalidParameterException;

	/**
	 * returns the important dates for the given consent<br>
	 *
	 * @param consentKeyDTO
	 * @return
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	ConsentDateValuesDTO getConsentDates(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO)
			throws InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * creates an alias between two signer ids; used for merges
	 *
	 * @param domainName
	 * @param originalSignerId
	 * @param aliasSignerId
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 *             if the originalSignerId doesn't exists within the given domain
	 * @throws UnknownSignerIdTypeException
	 */
	void addAlias(@XmlElement(required = true) @WebParam(name = "domainName") String domainName, @XmlElement(required = true) @WebParam(name = "originalSignerId") SignerIdDTO originalSignerId,
			@XmlElement(required = true) @WebParam(name = "aliasSignerId") SignerIdDTO aliasSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * deactivates the given alias
	 *
	 * @param domainName
	 * @param originalSignerId
	 * @param aliasSignerId
	 * @throws InvalidParameterException
	 * @throws UnknownAliasException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	void deactivateAlias(@XmlElement(required = true) @WebParam(name = "domainName") String domainName, @XmlElement(required = true) @WebParam(name = "originalSignerId") SignerIdDTO originalSignerId,
			@XmlElement(required = true) @WebParam(name = "aliasSignerId") SignerIdDTO aliasSignerId)
			throws InvalidParameterException, UnknownAliasException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * returns a list with all signer ids which are connected via an alias to the given signer id
	 *
	 * @param domainName
	 * @param originalSignerId
	 * @return
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signerIds")
	List<SignerIdDTO> getAliasesForSignerId(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "originalSignerId") SignerIdDTO originalSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * updates the qc for the given consent
	 *
	 * @param consentKeyDTO
	 * @param qc
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 */
	void setQCForConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "qc") QCDTO qc)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * returns a list with all qc entries for the given consent
	 *
	 * @param consentKeyDTO
	 * @return
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "qcHistories")
	List<QCHistoryDTO> getQCHistoryForConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO)
			throws InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * searches the object with the given fhirID
	 *
	 * @param clazz
	 * @param fhirID
	 * @return
	 * @throws UnknownFhirIdObjectException
	 *             if that object is not known (DAO.getObjectByFhirID)
	 * @throws UnknownIDException
	 *             if that object is not found
	 */
	<T extends FhirIdDTO> T getObjectByFhirID(@XmlElement(required = true) @WebParam(name = "clazz") Class<T> clazz, @XmlElement(required = true) @WebParam(name = "fhirID") String fhirID)
			throws UnknownFhirIdObjectException, UnknownIDException;
}
