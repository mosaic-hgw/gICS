package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.jsoup.Jsoup;

/**
 * Abstract class for consent related beans
 * 
 * @author Arne Blumentritt
 * 
 */
@ManagedBean
public abstract class AbstractConsentController extends AbstractGICSBean
{
	protected static final ConsentStatus[] mandatoryConsentStates = { ConsentStatus.ACCEPTED, ConsentStatus.DECLINED, ConsentStatus.INVALIDATED,
			ConsentStatus.REVOKED };

	/**
	 * Method to pass ConsentStatus to Servlet
	 * 
	 * @return
	 */
	public ConsentStatus[] getConsentStates()
	{
		return ConsentStatus.values();
	}

	public Object[] getMandatoryConsentStates(boolean mandatory)
	{
		if (mandatory)
		{
			ArrayList<ConsentStatus> states = new ArrayList<ConsentStatus>();
			for (ConsentStatus status : getConsentStates())
			{
				if (status.getConsentStatusType() != ConsentStatusType.UNKNOWN)
				{
					states.add(status);
				}
			}
			return states.toArray();
		}
		// this is neccessary to order the unkown types behind the mandatory types
		else
		{
			ArrayList<ConsentStatus> states = new ArrayList<ConsentStatus>();
			for (ConsentStatus status : getConsentStates())
			{
				if (status.getConsentStatusType() != ConsentStatusType.UNKNOWN)
				{
					states.add(status);
				}
			}
			for (ConsentStatus status : getConsentStates())
			{
				if (status.getConsentStatusType() == ConsentStatusType.UNKNOWN)
				{
					states.add(status);
				}
			}
			return states.toArray();
		}
	}

	public String cleanStringForTable(String string)
	{
		String result = sanitize(string);
		if (result.length() > 100)
			result = result.substring(0, 100);
		return result;
	}

	public String sanitize(String string)
	{
		return Jsoup.parse(string).text();
	}

	public GICSService getCmManager()
	{
		return cmManager;
	}

	public void setCmManager(GICSService cmManager)
	{
		this.cmManager = cmManager;
	}

	public List<String> getSignerIdTypes()
	{
		return domainSelector.getSelectedDomain().getSignerIdTypes();
	}

	public List<ConsentTemplateType> getTemplateTypes()
	{
		return new ArrayList<ConsentTemplateType>(Arrays.asList(ConsentTemplateType.values()));
	}
}
