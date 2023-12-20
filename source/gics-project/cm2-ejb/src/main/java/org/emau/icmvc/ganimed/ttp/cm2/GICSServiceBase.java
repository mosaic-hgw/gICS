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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainRelated;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FhirIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownIDException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DataAccessDispatcher;
import org.emau.icmvc.ttp.auth.AbstractServiceBase;
import org.emau.icmvc.ttp.auth.TTPNames;

public abstract class GICSServiceBase extends AbstractServiceBase
{
	private static final String PARAMETER_MISSING_MESSAGE = "invalid parameter: ";
	protected final Logger logger = LogManager.getLogger(getClass());

	@EJB
	protected DataAccessDispatcher dad;

	@Override
	public TTPNames.Tool getTool()
	{
		return TTPNames.Tool.gics;
	}

	protected <T extends DomainRelated> List<T> filterAllowedDomains(List<T> domains)
	{
		return filterAllowedDomains(domains, DomainRelated::getDomainName);
	}

	protected void checkAllowedDomain(String domainName) throws UnknownDomainException
	{
		if (!isAllowedDomain(domainName))
		{
			throw new UnknownDomainException(domainName);
		}
	}

	protected void checkAllowedDomain(DomainRelated domainRelatedObject) throws UnknownDomainException
	{
		checkAllowedDomain(domainRelatedObject.getDomainName());
	}

	protected void checkAllowedDomain(Collection<? extends DomainRelated> domainRelatedObjects) throws UnknownDomainException
	{
		for (DomainRelated d : domainRelatedObjects)
		{
			checkAllowedDomain(d.getDomainName());
		}
	}

	protected <T extends FhirIdDTO> void checkAllowedDomainForFHIRObject(T objectForFhirID) throws UnknownIDException
	{
		try
		{
			if (objectForFhirID instanceof DomainRelated dro)
			{
				checkAllowedDomain(dro);
			}
		}
		catch (UnknownDomainException e)
		{
			throw new UnknownIDException(objectForFhirID.getFhirID(), e);
		}
	}

	protected void checkAllowedDomainAsInvalidParameter(String domainName) throws InvalidParameterException
	{
		try
		{
			checkAllowedDomain(domainName);
		}
		catch (UnknownDomainException e)
		{
			throw new InvalidParameterException("Illegal parameter domain.name " + domainName
					+ " (not matching any domain-based role: " + getAuthContext().getDomainBasedRoles() + ")");
		}
	}

	protected String createLogMessageForUpdateConsent(ConsentKeyDTO keyDTO, String externProperties, String comment, ConsentScanDTO scan)
	{
		return keyDTO.toString() + " "
				+ "with extern properties '" + externProperties + "' "
				+ "and comment '" + comment + "' "
				+ "and " + (scan == null ? "no scan " : "a scan ")
				+ "with type '" + (scan != null ? scan.getFileType() : "no type") + "'";
	}

	protected String createLogMessageForRefuseConsent(ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
	{
		StringBuilder sb = new StringBuilder(ctKeyDTO.toString()).append(" for:\n");
		for (SignerIdDTO signerId : signerIdDTOs)
		{
			sb.append(signerId).append("\n");
		}
		return sb.toString();
	}

	protected void checkParameter(Object parameter, String paramName) throws InvalidParameterException
	{
		if (parameter == null)
		{
			throwParameterMissingIPE(paramName);
		}
		else if (parameter instanceof String temp)
		{
			if (temp.isEmpty())
			{
				throwParameterMissingIPE(paramName);
			}
		}
		else if (parameter instanceof List<?> temp)
		{
			// empty list must be valid as parameters e.g. to clear consent template mappings
			for (Object o : temp)
			{
				checkParameter(o, paramName + ".listElement");
			}
		}
		else if (parameter instanceof Set<?> temp)
		{
			if (temp.isEmpty())
			{
				throwParameterMissingIPE(paramName);
			}
			for (Object o : temp)
			{
				checkParameter(o, paramName + ".listElement");
			}
		}
		else if (parameter instanceof DomainDTO temp)
		{
			checkParameter(temp.getDomainName(), paramName + ".domainName");
		}
		else if (parameter instanceof ConsentTemplateDTO temp)
		{
			checkParameter(temp.getKey(), paramName + ".key");
		}
		else if (parameter instanceof ModuleDTO temp)
		{
			checkParameter(temp.getKey(), paramName + ".key");
		}
		else if (parameter instanceof PolicyDTO temp)
		{
			checkParameter(temp.getKey(), paramName + ".key");
		}
		else if (parameter instanceof ConsentDTO temp)
		{
			checkParameter(temp.getKey(), paramName + ".key");
		}
		else if (parameter instanceof SignerIdDTO temp)
		{
			checkParameter(temp.getId(), paramName + ".id");
			checkParameter(temp.getIdType(), paramName + ".signerIdType");
		}
		else if (parameter instanceof ConsentTemplateKeyDTO temp)
		{
			checkParameter(temp.getDomainName(), paramName + ".domainName");
			checkParameter(temp.getName(), paramName + ".name");
			checkParameter(temp.getVersion(), paramName + ".version");
		}
		else if (parameter instanceof ModuleKeyDTO temp)
		{
			checkParameter(temp.getDomainName(), paramName + ".domainName");
			checkParameter(temp.getName(), paramName + ".name");
			checkParameter(temp.getVersion(), paramName + ".version");
		}
		else if (parameter instanceof PolicyKeyDTO temp)
		{
			checkParameter(temp.getDomainName(), paramName + ".domainName");
			checkParameter(temp.getName(), paramName + ".name");
			checkParameter(temp.getVersion(), paramName + ".version");
		}
		else if (parameter instanceof ConsentKeyDTO temp)
		{
			checkParameter(temp.getConsentTemplateKey(), paramName + ".consentTemplateKey");
			checkParameter(temp.getConsentDate(), paramName + ".consentDate");
			for (SignerIdDTO sidDTO : temp.getSignerIds())
			{
				checkParameter(sidDTO, paramName + ".key.signerId");
			}
		}
	}

	private void throwParameterMissingIPE(String paramName) throws InvalidParameterException
	{
		String message = PARAMETER_MISSING_MESSAGE + paramName;
		logger.warn(message);
		throw new InvalidParameterException(paramName, message);
	}
}
