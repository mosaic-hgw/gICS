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

import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
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

@WebService(name = "gicsManagementService")
@Remote(GICSManagementService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class GICSManagementServiceImpl extends GICSServiceBase implements GICSManagementService
{
	// ### domains ####################################################################################################

	@Override
	public void addDomain(DomainDTO domainDTO)
			throws DuplicateEntryException, InvalidParameterException, VersionConverterClassException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("create " + domainDTO);
		}
		checkParameter(domainDTO, "domainDTO");
		checkAllowedDomainAsInvalidParameter(domainDTO.getName());
		dad.addDomain(domainDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(domainDTO + " created");
		}
	}

	@Override
	public void deleteDomain(String domainName)
			throws InvalidParameterException, ObjectInUseException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete domain with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		dad.deleteDomain(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("domain with name '" + domainName + "' deleted");
		}
	}

	@Override
	public void finaliseDomain(String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise domain with name '" + domainName);
		}
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		dad.finaliseDomain(domainName);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + domainName + " finalised");
		}
	}

	@Override
	public void updateDomain(DomainDTO dto) throws InvalidParameterException, ObjectInUseException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update domain with name " + dto.getName());
		}
		checkParameter(dto, "domainDTO");
		checkAllowedDomain(dto.getName());
		dad.updateDomain(dto);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + dto.getName() + "' updated");
		}
	}

	@Override
	public void updateDomainInUse(String domainName, String label, String logo, String externProperties, String comment)
			throws InvalidParameterException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update domain with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		checkAllowedDomain(domainName);
		dad.updateDomainInUse(domainName, label, logo, externProperties, comment);
		if (logger.isInfoEnabled())
		{
			logger.info("domain '" + domainName + "' updated");
		}
	}

	// ### templates ####################################################################################################

	@Override
	public void addConsentTemplate(ConsentTemplateDTO consentTemplateDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidParameterException, InvalidVersionException, InvalidPropertiesException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + consentTemplateDTO);
		}
		checkParameter(consentTemplateDTO, "consentTemplateDTO");
		checkAllowedDomain(consentTemplateDTO);
		dad.addConsentTemplate(consentTemplateDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(consentTemplateDTO + " added");
		}
	}

	@Override
	public void deleteConsentTemplate(ConsentTemplateKeyDTO keyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException, ObjectInUseException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.deleteConsentTemplate(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public void finaliseTemplate(ConsentTemplateKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.finaliseTemplate(keyDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " finalised");
		}
	}

	@Override
	public void updateConsentTemplate(ConsentTemplateDTO dto, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, FreeTextConverterStringException, InvalidFreeTextException, InvalidParameterException,
			InvalidPropertiesException, InvalidVersionException, ObjectInUseException, RequirementsNotFullfilledException,
			UnknownConsentTemplateException, UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		checkParameter(dto, "consentTemplateDTO");
		checkAllowedDomain(dto);
		dad.updateConsentTemplate(dto, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updateConsentTemplateInUse(ConsentTemplateDTO dto)
			throws InvalidFreeTextException, InvalidParameterException, InvalidVersionException, UnknownConsentTemplateException,
			UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		checkParameter(dto, "consentTemplateDTO");
		checkAllowedDomain(dto);
		dad.updateConsentTemplateInUse(dto);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	// ### modules ####################################################################################################

	@Override
	public void addModule(ModuleDTO moduleDTO, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException,
			UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + moduleDTO);
		}
		checkParameter(moduleDTO, "moduleDTO");
		checkAllowedDomain(moduleDTO);
		dad.addModule(moduleDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(moduleDTO + " added");
		}
	}

	@Override
	public void deleteModule(ModuleKeyDTO keyDTO)
			throws UnknownDomainException, UnknownModuleException, ObjectInUseException, InvalidParameterException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.deleteModule(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public void finaliseModule(ModuleKeyDTO keyDTO, boolean finaliseRelatedEntities)
			throws InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.finaliseModule(keyDTO, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " finalised");
		}
	}

	@Override
	public void updateModule(ModuleDTO dto, boolean finaliseRelatedEntities)
			throws DuplicateEntryException, InvalidParameterException, InvalidVersionException, ObjectInUseException,
			RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		checkParameter(dto, "moduleDTO");
		checkAllowedDomain(dto);
		dad.updateModule(dto, finaliseRelatedEntities);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updateModuleInUse(ModuleKeyDTO keyDTO, String label, String shortText, String externProperties, String comment, Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownModuleException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.updateModuleInUse(keyDTO, label, shortText, externProperties, comment, assignedPolicyDTOs);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " updated");
		}
	}

	// ### policies ####################################################################################################

	@Override
	public void addPolicy(PolicyDTO policyDTO)
			throws DuplicateEntryException, InvalidParameterException, InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + policyDTO);
		}
		checkParameter(policyDTO, "policyDTO");
		checkAllowedDomain(policyDTO);
		dad.addPolicy(policyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(policyDTO + " added");
		}
	}

	@Override
	public void deletePolicy(PolicyKeyDTO keyDTO)
			throws UnknownDomainException, UnknownPolicyException, ObjectInUseException, InvalidParameterException, InvalidVersionException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("delete " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.deletePolicy(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " deleted");
		}
	}

	@Override
	public void finalisePolicy(PolicyKeyDTO keyDTO)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("finalise " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.finalisePolicy(keyDTO);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " finalised");
		}
	}

	@Override
	public void updatePolicy(PolicyDTO dto)
			throws InvalidParameterException, InvalidVersionException, ObjectInUseException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + dto.getKey());
		}
		checkParameter(dto, "policyDTO");
		checkAllowedDomain(dto);
		dad.updatePolicy(dto);
		if (logger.isInfoEnabled())
		{
			logger.info(dto.getKey() + " updated");
		}
	}

	@Override
	public void updatePolicyInUse(PolicyKeyDTO keyDTO, String label, String externProperties, String comment)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException, UnknownPolicyException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update " + keyDTO);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.updatePolicyInUse(keyDTO, label, externProperties, comment);
		if (logger.isInfoEnabled())
		{
			logger.info(keyDTO + " updated");
		}
	}

	// ### signerIDTypes ####################################################################################################

	@Override
	public void addSignerIdType(String domainName, String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, DuplicateEntryException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add signer id type " + signerIdTypeName + " to domain " + domainName);
		}
		checkParameter(domainName, "domainName");
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkAllowedDomain(domainName);
		dad.addSignerIdType(domainName, signerIdTypeName);
		if (logger.isInfoEnabled())
		{
			logger.info("signer id type " + signerIdTypeName + " successfully added to domain " + domainName);
		}
	}

	@Override
	public void deleteSignerIdType(String domainName, String signerIdTypeName)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException, ObjectInUseException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("remove signer id type " + signerIdTypeName + " from domain " + domainName);
		}
		checkParameter(domainName, "domainName");
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkAllowedDomain(domainName);
		dad.deleteSignerIdType(domainName, signerIdTypeName);
		if (logger.isInfoEnabled())
		{
			logger.info("signer id type " + signerIdTypeName + " removed from domain " + domainName);
		}
	}

	@Override
	public void updateSignerIdType(String domainName, String signerIdTypeName, String label, String comment)
			throws InvalidParameterException, UnknownDomainException, UnknownSignerIdTypeException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update signerIdType with name " + signerIdTypeName + " within domain " + domainName);
		}
		checkParameter(domainName, "domainName");
		checkParameter(signerIdTypeName, "signerIdTypeName");
		checkAllowedDomain(domainName);
		dad.updateSignerIdType(domainName, signerIdTypeName, label, comment);
		if (logger.isInfoEnabled())
		{
			logger.info("signerIdType " + signerIdTypeName + " within domain " + domainName + " updated");
		}
	}
}
