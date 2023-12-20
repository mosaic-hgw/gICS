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

import java.text.SimpleDateFormat;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

@WebService
public interface GICSManagementService
{
	// ### domains ####################################################################################################

	/**
	 * creates a new domain
	 *
	 * @param domainDTO
	 *            see {@link DomainDTO}
	 * @throws DuplicateEntryException
	 *             if a domain with that name already exists
	 * @throws InvalidParameterException
	 * @throws VersionConverterClassException
	 */
	void addDomain(
			@XmlElement(required = true) @WebParam(name = "domain") DomainDTO domainDTO)
			throws DuplicateEntryException, InvalidParameterException, VersionConverterClassException;

	/**
	 * deletes the given domain
	 *
	 * @param domainName
	 *            the name of the domain
	 * @throws InvalidParameterException
	 * @throws ObjectInUseException
	 *             if there's at least one pseudonym within that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	void deleteDomain(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, ObjectInUseException, UnknownDomainException;

	/**
	 * finalises the given domain
	 *
	 * @param domainName
	 *            the name of the domain
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 */
	void finaliseDomain(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * updates the given domain
	 *
	 * @param domainDTO
	 * @throws InvalidParameterException
	 * @throws ObjectInUseException
	 * @throws UnknownDomainException
	 */
	void updateDomain(
			@XmlElement(required = true) @WebParam(name = "domain") DomainDTO domainDTO)
			throws InvalidParameterException, ObjectInUseException, UnknownDomainException;

	/**
	 * updates the given domain with the given values
	 *
	 * @param domainName
	 *            the name of the domain
	 * @param label
	 * @param logo
	 * @param externProperties
	 * @param comment
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 */
	void updateDomainInUse(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "logo") String logo,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws InvalidParameterException, UnknownDomainException;

	// ### templates ####################################################################################################

	/**
	 * stores the given consent template<br>
	 * the policies within the modulDTOs are ignored and therefore doesn't need to be set
	 *
	 * @param consentTemplateDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws DuplicateEntryException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws FreeTextConverterStringException
	 *             is thrown when the given converter string (in case of {@link FreeTextType}.Date)
	 *             is not a valid string for {@link SimpleDateFormat}
	 * @throws InvalidPropertiesException
	 * @throws RequirementsNotFullfilledException
	 * @throws InvalidParameterException
	 */
	void addConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws UnknownDomainException, UnknownModuleException, DuplicateEntryException, InvalidParameterException, InvalidVersionException,
			FreeTextConverterStringException, InvalidPropertiesException, RequirementsNotFullfilledException, UnknownConsentTemplateException;

	/**
	 * deletes the given consent template (only if it's not in use, i.e. there's no consent
	 * belonging to that consent template)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws ObjectInUseException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	void deleteConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO keyDTO)
			throws UnknownDomainException, UnknownConsentTemplateException, ObjectInUseException, InvalidParameterException, InvalidVersionException;

	/**
	 * finalises the given consent template
	 *
	 * @param consentTemplateKeyDTO
	 * @param finaliseRelatedEntities
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 */
	void finaliseTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplateKey") ConsentTemplateKeyDTO consentTemplateKeyDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException;

	/**
	 * updates the given consent template
	 *
	 * @param consentTemplateDTO
	 * @throws DuplicateEntryException
	 * @throws FreeTextConverterStringException
	 * @throws InvalidFreeTextException
	 * @throws InvalidParameterException
	 * @throws InvalidPropertiesException
	 * @throws InvalidVersionException
	 * @throws ObjectInUseException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 */
	void updateConsentTemplate(
			@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidFreeTextException, InvalidParameterException, InvalidPropertiesException,
			InvalidVersionException, ObjectInUseException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException;

	/**
	 * updates label, title, comment, externProperties, scanBase64, scanFileType,
	 * assignedModule.comment, assignedModule.externProperties, freeTextDef.comment
	 *
	 * @param consentTemplateDTO
	 * @throws InvalidFreeTextException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownConsentTemplateException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 */
	void updateConsentTemplateInUse(
			@XmlElement(required = true) @WebParam(name = "consentTemplate") ConsentTemplateDTO consentTemplateDTO)
			throws InvalidFreeTextException, InvalidParameterException, InvalidVersionException, UnknownConsentTemplateException, UnknownDomainException,
			UnknownModuleException;

	// ### modules ####################################################################################################

	/**
	 * stores the given module
	 *
	 * @param moduleDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws DuplicateEntryException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 */
	void addModule(
			@XmlElement(required = true) @WebParam(name = "module") ModuleDTO moduleDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws UnknownDomainException, UnknownPolicyException, DuplicateEntryException, InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException;

	/**
	 * deletes the given module (only if it's not in use, i.e. there's no consent template linked to
	 * that module)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 * @throws ObjectInUseException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	void deleteModule(
			@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, InvalidParameterException, InvalidVersionException;

	/**
	 * finalises the given module
	 *
	 * @param moduleKeyDTO
	 * @param finaliseRelatedEntities
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 * @throws UnknownDomainException
	 * @throws UnknownModuleException
	 */
	void finaliseModule(
			@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO moduleKeyDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException;

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
	void updateModule(
			@XmlElement(required = true) @WebParam(name = "module") ModuleDTO moduleDTO,
			@XmlElement(required = true) @WebParam(name = "finaliseRelatedEntities") boolean finaliseRelatedEntities)
			throws DuplicateEntryException, InvalidParameterException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException;

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
	void updateModuleInUse(
			@XmlElement(required = true) @WebParam(name = "moduleKey") ModuleKeyDTO moduleKeyDTO,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "shortText") String shortText,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "assignedPolicies") Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException;

	// ### policies ####################################################################################################

	/**
	 * stores the given policy
	 *
	 * @param policyDTO
	 * @throws UnknownDomainException
	 * @throws DuplicateEntryException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws RequirementsNotFullfilledException
	 */
	void addPolicy(
			@XmlElement(required = true) @WebParam(name = "policy") PolicyDTO policyDTO)
			throws UnknownDomainException, DuplicateEntryException, InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException;

	/**
	 * deletes the given policy (only if it's not in use, i.e. there's no module linked to that
	 * policy)
	 *
	 * @param keyDTO
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 * @throws ObjectInUseException
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 */
	void deletePolicy(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, InvalidParameterException, InvalidVersionException;

	/**
	 * finalises the given policy
	 *
	 * @param policyKeyDTO
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void finalisePolicy(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownPolicyException;

	/**
	 * updates the given policy
	 *
	 * @param policyDTO
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws ObjectInUseException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void updatePolicy(
			@XmlElement(required = true) @WebParam(name = "policy") PolicyDTO policyDTO)
			throws InvalidParameterException, InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException;

	/**
	 * updates the given policy with the given values
	 *
	 * @param policyKeyDTO
	 * @param label
	 * @param externProperties
	 * @param comment
	 * @throws InvalidParameterException
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws UnknownPolicyException
	 */
	void updatePolicyInUse(
			@XmlElement(required = true) @WebParam(name = "policyKey") PolicyKeyDTO policyKeyDTO,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "externProperties") String externProperties,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownPolicyException;

	// ### signerIDTypes ####################################################################################################

	/**
	 * add a signer id type with the given name to the given domain
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws DuplicateEntryException
	 *             if the signerIdType already exists
	 */
	void addSignerIdType(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, DuplicateEntryException;

	/**
	 * updates the given signerIdType with the given values
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @param label
	 * @param comment
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownSignerIdTypeException
	 *             if the given signerIdType is not found
	 */
	void updateSignerIdType(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "comment") String comment)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException;

	/**
	 * removes the given signer id type from the given domain
	 *
	 * @param domainName
	 *            domain to which the signerIdType belongs to
	 * @param signerIdTypeName
	 *            identifier
	 * @throws InvalidParameterException
	 * @throws UnknownDomainException
	 * @throws UnknownSignerIdTypeException
	 * @throws ObjectInUseException
	 */
	void deleteSignerIdType(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "signerIdTypeName") String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException;
}
