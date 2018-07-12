package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;



/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * Medicine of the University Medicine Greifswald -
 * mosaic-projekt@uni-greifswald.de
 * 
 * concept and implementation
 * l.geidel
 * web client
 * a.blumentritt, m.bialke
 * 
 * Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG
 * HO 1937/5-1).
 * 
 * please cite our publications
 * http://dx.doi.org/10.3414/ME14-01-0133
 * http://dx.doi.org/10.1186/s12967-015-0545-6
 * http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;

/**
 * Backing Bean for searching consents and policies
 * 
 * @author Arne Blumentritt
 * 
 */
@ManagedBean(name = "searchController")
@ViewScoped
public class SearchController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = 159032699222674160L;

	private String sidKey;
	private String sidValue;
	private String templateType;

	public String onSearchConsents()
	{
		try
		{
			if (preSearch())
			{
				return "/html/internal/consents.xhml?templateType=" + templateType + "&sidKey=" + sidKey + "&sidValue=" + sidValue + "&faces-redirect=true";
			}
			else
			{
				Object[] args = {0, sidValue};
				logMessage(new MessageFormat(getBundle().getString("consent.found.amount.type_" + (templateType == null ? "" : templateType)))
						.format(args), Severity.INFO);
				return null;
			}
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException | InconsistentStatusException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
			return null;
		}
	}
	
	/**
	 * Search if results exist before redirecting to result page
	 * @throws InconsistentStatusException 
	 * @throws InvalidVersionException 
	 * @throws VersionConverterClassException 
	 * @throws UnknownDomainException 
	 * @throws UnknownSignerIdTypeException 
	 */
	private boolean preSearch() throws UnknownDomainException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException, UnknownSignerIdTypeException
	{
		Set<SignerIdDTO> signerIdSet = new HashSet<>();
		for (String type : domainSelector.getSelectedDomain().getSignerIdTypes())
		{
			signerIdSet.add(new SignerIdDTO(type, sidValue));
		}
		
		List<ConsentLightDTO> result = new ArrayList<>();
		for (ConsentLightDTO consent : cmManager.getAllConsentsForPerson(domainSelector.getSelectedDomainName(), signerIdSet))
		{
			if (consent.getTemplateType() == null || templateType == null || consent.getTemplateType().toString().equals(templateType))
			{
				result.add(consent);
			}
		}
		return result.size() > 0;
	}

	public String getSidKey()
	{
		return sidKey;
	}

	public void setSidKey(String sidKey)
	{
		this.sidKey = sidKey;
	}

	public String getSidValue()
	{
		return sidValue;
	}

	public void setSidValue(String sidValue)
	{
		this.sidValue = sidValue;
	}

	public String getTemplateType()
	{
		return templateType;
	}

	public void setTemplateType(String templateType)
	{
		this.templateType = templateType;
	}
}