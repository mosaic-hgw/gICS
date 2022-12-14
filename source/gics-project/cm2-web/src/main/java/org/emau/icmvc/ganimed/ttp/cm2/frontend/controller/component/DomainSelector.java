package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component;

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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.icmvc.ttp.web.controller.AbstractBean;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Managed bean for selecting and providing the current domain
 *
 * @author Arne Blumentritt
 */
@SessionScoped
@ManagedBean(name = "domainSelector")
public class DomainSelector extends AbstractBean
{
	@EJB(lookup = "java:global/gics/cm2-ejb/GICSServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSService")
	protected GICSService cmManager;

	private List<DomainDTO> domains;
	private DomainDTO selectedDomain;

	private final Map<String, Boolean> selectedDomainQcTypes = new LinkedHashMap<>();

	/**
	 * Init domains.
	 */
	@PostConstruct
	private void init()
	{
		loadDomains();
		if (domains.size() > 0)
		{
			setSelectedDomain(domains.get(0));
		}
	}

	public void loadDomains()
	{
		domains = cmManager.listDomains();
	}

	public List<DomainDTO> getDomains()
	{
		return domains;
	}

	public DomainDTO getSelectedDomain()
	{
		boolean found = false;
		for (DomainDTO domain : domains)
		{
			if (selectedDomain != null && domain.getName().equals(selectedDomain.getName()))
			{
				found = true;
				break;
			}
		}

		if (!found)
		{
			if (domains.size() > 0)
			{
				selectedDomain = domains.get(0);
			}
			else
			{
				return null;
			}
		}

		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain;
		loadDomainData();

		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		try
		{
			ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
		}
		catch (IOException e)
		{
			logMessage(e);
		}
	}

	public void setSelectedDomain(String name)
	{
		for (DomainDTO domain : domains)
		{
			if (domain.getName().equals(name))
			{
				selectedDomain = domain;
				loadDomainData();
			}
		}
	}

	private void loadDomainData()
	{
		selectedDomainQcTypes.clear();

		for (String type : selectedDomain.getValidQcTypes())
		{
			if (isNotEmpty(type))
			{
				selectedDomainQcTypes.put(type, true);
			}
		}

		for (String type : selectedDomain.getInvalidQcTypes())
		{
			if (isNotEmpty(type))
			{
				selectedDomainQcTypes.put(type, false);
			}
		}
	}

	public String getSelectedDomainLogo()
	{
		if (!StringUtils.isEmpty(selectedDomain.getLogo()))
		{
			return "data:image/png;base64," + selectedDomain.getLogo();
		}
		return null;
	}

	public String getSelectedDomainName()
	{
		return selectedDomain.getName();
	}

	public Map<String, Boolean> getSelectedDomainQcTypes()
	{
		return selectedDomainQcTypes;
	}
}
