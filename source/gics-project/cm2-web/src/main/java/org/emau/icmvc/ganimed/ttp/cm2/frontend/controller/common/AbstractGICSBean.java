package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common;

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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSServiceWithNotification;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.DomainSelector;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.Versions;
import org.icmvc.ttp.web.controller.AbstractBean;

/**
 * Abstract class for web beans
 *
 * @author Arne Blumentritt
 *
 */
public abstract class AbstractGICSBean extends AbstractBean
{
	@EJB(lookup = "java:global/gics/cm2-ejb/GICSServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSService")
	protected GICSService service;

	@EJB(lookup = "java:global/gics/cm2-ejb/GICSServiceWithNotificationImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSServiceWithNotification")
	protected GICSServiceWithNotification serviceWithNotification;

	@ManagedProperty(value = "#{domainSelector}")
	protected DomainSelector domainSelector;

	@ManagedProperty(value = "#{Versions}")
	protected Versions versionPath;

	protected static final String NOT_CHECKED = "not_checked";
	protected static final String CHECKED_NO_FAULTS = "checked_no_faults";
	protected static final String CHECKED_MINOR_FAULTS = "checked_minor_faults";
	protected static final String CHECKED_MAJOR_FAULTS = "checked_major_faults";
	protected static final String INVALIDATED = "invalidated";
	protected static final String TOOL = "gICS";
	protected static final String NOTIFICATION_CLIENT_ID = TOOL + "_Web";


	public void setDomainSelector(DomainSelector domainSelector)
	{
		this.domainSelector = domainSelector;
	}

	public DomainDTO getSelectedDomain()
	{
		return domainSelector.getSelectedDomain();
	}

	public String getQcTypeLabel(String type)
	{
		if (getBundle().containsKey("model.consent.qc.type." + type))
		{
			return getBundle().getString("model.consent.qc.type." + type);
		}
		return type;
	}

	public boolean isUsingNotifications()
	{
		String key = DomainProperties.SEND_NOTIFICATIONS_WEB.name();
		String value = "false";
		String entry = Arrays.stream(getSelectedDomain().getProperties().replace("domain properties: ", "").split(";"))
				.filter(p -> p.contains(key)).findFirst().orElse(null);
		if (entry != null && entry.contains("="))
		{
			value = entry.trim().split("=")[1].trim();
		}
		return Boolean.parseBoolean(value);
	}

	public List<String> getSignerIdTypes()
	{
		return getSelectedDomain().getSignerIdTypes();
	}

	@Override
	protected ResourceBundle getBundle()
	{
		// never use caching here, it will break switching the locale and is already done by the framework
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getResourceBundle(facesContext, "msg");
	}

	public void setVersionPath(Versions versionPath)
	{
		this.versionPath = versionPath;
	}

	public String getTool()
	{
		return TOOL;
	}

	public String getINVALIDATED()
	{
		return INVALIDATED;
	}
}
