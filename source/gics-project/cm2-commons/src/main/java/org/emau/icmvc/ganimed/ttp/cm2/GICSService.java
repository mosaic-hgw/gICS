package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
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

@WebService
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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws MissingRequiredObjectException
	 *             when there's no given value for a mandatory free text field
	 * @throws InvalidFreeTextException
	 * @throws MandatoryFieldsException
	 *             see {@link MandatoryFieldsException}
	 * @throws UnknownSignerIdTypeException
	 * @throws DuplicateEntryException
	 * @throws RequirementsNotFullfilledException
	 */
	void addConsent(
			@XmlElement(required = true) @WebParam(name = "consent") ConsentDTO consentDTO)
			throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException, MissingRequiredObjectException,
			InvalidFreeTextException, MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException, RequirementsNotFullfilledException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsented(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromIncludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromExcludingToIncluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromIncludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	boolean isConsentedFromExcludingToExcluding(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "versionFrom") String versionFrom,
			@XmlElement(required = true) @WebParam(name = "versionTo") String versionTo,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentStatusType getConsentStatusType(@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config) throws UnknownDomainException, UnknownPolicyException,
			InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
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
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
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
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
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
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
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
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * lists all consent templates of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consentTemplates")
	List<ConsentTemplateDTO> listConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException;

	/**
	 * lists the most recent version of all consent templates of the given domain
	 *
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "currentConsentTemplates")
	List<ConsentTemplateDTO> listCurrentConsentTemplates(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException;

	/**
	 * lists all modules of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "modules")
	List<ModuleDTO> listModules(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException;

	/**
	 * returns all attributes of the requested module
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	ModuleDTO getModule(@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, InvalidParameterException, InvalidVersionException;

	/**
	 * lists all policies of the given domain
	 *
	 * @param domainName
	 * @param onlyFinal
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "policies")
	List<PolicyDTO> listPolicies(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "onlyFinal") boolean onlyFinal)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException;

	/**
	 * returns all attributes of the requested policy
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	PolicyDTO getPolicy(@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, InvalidParameterException, InvalidVersionException;

	/**
	 * returns all attributes of the requested consent template
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	ConsentTemplateDTO getConsentTemplate(@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException;

	/**
	 * returns all attributes of the current (highest version) consent template with the given name
	 *
	 * @param consentTemplateName
	 * @param domainName
	 * @return
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	ConsentTemplateDTO getCurrentConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateName") String consentTemplateName,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForPolicyAndSignerIds(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getPolicyStatesForPolicyNameAndSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "policyName") String policyName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signedPolicies")
	List<SignedPolicyDTO> getCurrentPolicyStatesForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws InconsistentStatusException
	 */
	ConsentDTO getCurrentConsentForSignerIdsAndCT(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs,
			@XmlElement(required = true) @WebParam(name = "ignoreVersionNumber") boolean ignoreVersionNumber,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException, InconsistentStatusException;

	/**
	 * lists all consents for the given consent template
	 *
	 * @param ctKeyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO) throws UnknownDomainException,
			UnknownConsentTemplateException, InvalidParameterException, InvalidVersionException, InconsistentStatusException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consents")
	List<ConsentLightDTO> getAllConsentsForDomainWithoutScan(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, InconsistentStatusException;

	/**
	 * get the complete dto for the given key - thought to be used in addition to the list functions
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentDTO getConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO keyDTO)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException,
			UnknownConsentException;

	/**
	 * get the light dto for the given key
	 *
	 * @param keyDTO
	 * @return
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 */
	ConsentLightDTO getConsentLight(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO keyDTO)
			throws UnknownDomainException, InvalidParameterException, InvalidVersionException, InconsistentStatusException, UnknownConsentTemplateException, UnknownSignerIdTypeException,
			UnknownConsentException;

	/**
	 * returns all information for the given domain
	 *
	 * @param domainName
	 *            identifier
	 * @return see {@link DomainDTO}
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	DomainDTO getDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InvalidParameterException, UnknownDomainException;

	/**
	 * @return list of all domains within the database; see {@link DomainDTO}
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "domains")
	List<DomainDTO> listDomains();

	/**
	 * Updates the given consent.
	 *
	 * @param consentKeyDTO
	 * @param externProperties
	 * @param comment
	 * @param scan
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentException
	 */
	void updateConsentInUse(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "scan") ConsentScanDTO scan)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException,
			UnknownSignerIdTypeException, UnknownConsentException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	void addScanToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "scanBase64") String scanBase64,
			@XmlElement(required = true) @WebParam(name = "fileType") String fileType,
			@XmlElement(required = true) @WebParam(name = "fileName") String fileName) throws UnknownDomainException, UnknownConsentTemplateException,
			UnknownConsentException, DuplicateEntryException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * Remove a scan from the given consent
	 *
	 * @param consentKeyDTO
	 * @param fhirId
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownConsentException
	 * @throws DuplicateEntryException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 */
	void removeScanFromConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "fhirId") String fhirId) throws UnknownDomainException, UnknownConsentTemplateException,
			UnknownConsentException, DuplicateEntryException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException;

	/**
	 * returns all information for the given signerIdType
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @return see {@link SignerIdTypeDTO}
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownSignerIdTypeException
	 *             if the given signerIdType is not found
	 */
	SignerIdTypeDTO getSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * list all signerIDTypes for the given domain
	 *
	 * @param domainName
	 *            domain to which the signerIdTypes belongs to
	 * @return list of all domains within the database; see {@link SignerIdTypeDTO}
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signerIdTypes")
	List<SignerIdTypeDTO> listSignerIdTypes(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * returns all id for the given signer id type and domain
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier of the signerIdType
	 * @return
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signerIds")
	List<String> getAllIdsForSignerIdType(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws InconsistentStatusException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "consentIds")
	List<String> getAllConsentedIdsFor(@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName,
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "config") CheckConsentConfig config)
			throws UnknownDomainException, UnknownSignerIdTypeException, UnknownPolicyException, InvalidParameterException, InvalidVersionException, InconsistentStatusException;

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
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws MissingRequiredObjectException
	 * @throws MandatoryFieldsException
	 * @throws UnknownModuleException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void validateConsent(@XmlElement(required = true) @WebParam(name = "consent") ConsentDTO consentDTO,
			@XmlElement(required = true) @WebParam(name = "allowRevoke") boolean allowRevoke)
			throws InvalidParameterException, InvalidVersionException, MissingRequiredObjectException, MandatoryFieldsException, UnknownModuleException, UnknownConsentTemplateException,
			UnknownDomainException;

	/**
	 * Stores a "refused" consent.
	 * The consent is not declined, the participant didn't want to fill a consent.<br>
	 * The date of the refused consent is set to the current date.
	 *
	 * @param ctKeyDTO
	 * @param signerIdDTOs
	 * @throws InternalException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void refuseConsent(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO ctKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerIds") Set<SignerIdDTO> signerIdDTOs)
			throws InternalException, InvalidParameterException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * adds the given signerId to the given consent
	 *
	 * @param consentKeyDTO
	 * @param signerIdDTO
	 * @throws InternalException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void addSignerIdToConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "signerId") SignerIdDTO signerIdDTO)
			throws InternalException,
			InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, IllegalArgumentException,
			UnknownDomainException;

	/**
	 * adds newSignerId (e.g. case number) to one or more virtual persons (which are using the given
	 * existentSignerId (e.g. pseudonym))
	 *
	 * @param existentSignerIdDTO
	 *            existing signerID identifying used by one or more virtual persons
	 * @param newSignerIdDTO
	 *            new signerID to be added to identified virtual persons
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidParameterException
	 * @throws IllegalArgumentException
	 * @throws InternalException
	 * @throws UnknownDomainException
	 */
	void addSignerIdToSignerId(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "existentSignerId") SignerIdDTO existentSignerIdDTO,
			@XmlElement(required = true) @WebParam(name = "newSignerId") SignerIdDTO newSignerIdDTO)
			throws UnknownSignerIdTypeException, InvalidParameterException, IllegalArgumentException, InternalException, UnknownDomainException;

	/**
	 * returns the important dates for the given consent<br>
	 *
	 * @param consentKeyDTO
	 * @return
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	ConsentDateValuesDTO getConsentDates(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

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
	@XmlElement(name = "aliases")
	List<SignerIdDTO> getAliasesForSignerId(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "originalSignerId") SignerIdDTO originalSignerId)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * returns a map with all signer ids which are connected via an alias to the given signer ids
	 *
	 * @param domainName
	 * @param originalSignerIds
	 * @return
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "aliases")
	Map<SignerIdDTO, SignerIdDTO[]> getAliasesForSignerIds(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "originalSignerIds") List<SignerIdDTO> originalSignerIds)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException;

	/**
	 * returns a list with all signer ids that have the given id as an alias
	 *
	 * @param domainName
	 * @param aliasSignerId
	 * @return
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdException
	 * @throws UnknownSignerIdTypeException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "signerIds")
	List<SignerIdDTO> getSignerIdsForAlias(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "aliasSignerId") SignerIdDTO aliasSignerId)
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
	 */
	void setQCForConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO,
			@XmlElement(required = true) @WebParam(name = "qc") QCDTO qc)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * returns a list with all qc entries for the given consent
	 *
	 * @param consentKeyDTO
	 * @return
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentException
	 * @throws UnknownSignerIdTypeException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "qcHistories")
	List<QCHistoryDTO> getQCHistoryForConsent(@XmlElement(required = true) @WebParam(name = "consentKey") ConsentKeyDTO consentKeyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException, UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * Returns all consent templates which relate to at least one of the specified policies.
	 * (Template zurückgeben, wenn min. 1 der übergebenen Policies vorkommt) --> zum Vorfiltern der Auswahllisten)
	 *
	 * @param policyKeyDTOs the keys of the policies to find related consent templates for
	 * @return consent templates relating to the specified policies
	 * @throws UnknownDomainException if the related domain is unknown
	 * @throws InvalidVersionException if any of the consent templates version is invalid
	 * @throws InvalidParameterException if parameters are invalid
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "templates")
	List<ConsentTemplateKeyDTO> getTemplatesWithPolicies(
			@XmlElement(required = true) @WebParam(name = "policyKeyDTOs") List<PolicyKeyDTO> policyKeyDTOs)
			throws InvalidParameterException, UnknownDomainException, InvalidVersionException;


	/**
	 * Returns all mapped consent templates for a signer ID in the specified domain.
	 * (Durch holen aller Einwilligungsvorlagen der signerId)
	 *
	 * @param domainName the name of the domain to return consent templates for
	 * @param ctType the type of consent templates to return
	 * @param signerId the signer ID to return consent templates for
	 * @param useAliases true to use aliases
	 * @return all mapped consent templates for a signer ID in the specified domain
	 * @throws UnknownDomainException if the domain is unknown
	 * @throws UnknownSignerIdException if the signer ID is unknown
	 * @throws InvalidParameterException if parameters are invalid
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "templates")
	List<ConsentTemplateKeyDTO> getMappedTemplatesForSignerId(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "ctType") ConsentTemplateType ctType,
			@XmlElement(required = true) @WebParam(name = "signerId") SignerIdDTO signerId,
			@XmlElement(required = true) @WebParam(name = "useAliases") boolean useAliases)
			throws UnknownDomainException, UnknownSignerIdException, UnknownSignerIdTypeException, InvalidVersionException, InconsistentStatusException, InvalidParameterException;

	/**
	 * Searches the object with the given fhirID.
	 *
	 * @param clazz the class of the object to search for
	 * @param fhirID the FHIR ID to search for
	 * @return the object with the given fhirID
	 * @throws UnknownFhirIdObjectException if that object is not known (DAO.getObjectByFhirID)
	 * @throws UnknownIDException if that object is not found
	 */
	<T extends FhirIdDTO> T getObjectByFhirID(
			@XmlElement(required = true) @WebParam(name = "clazz") Class<T> clazz,
			@XmlElement(required = true) @WebParam(name = "fhirID") String fhirID)
			throws UnknownFhirIdObjectException, UnknownIDException;
}
