package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for lazy loading of consents
 * 
 * @author Arne Blumentritt
 *
 */
public class WebConsentLazyModel extends LazyDataModel<WebConsent>
{
	private static final long serialVersionUID = 1104825405795685955L;
	public Logger logger = LoggerFactory.getLogger(getClass());

	private GICSService service;
	private List<ConsentLightDTO> lightConsents;
	private List<WebConsent> data;
	private DomainDTO domain;
	private int i;

	public WebConsentLazyModel(GICSService service, List<ConsentLightDTO> lightConsents, DomainDTO domain)
	{
		this.service = service;
		this.lightConsents = lightConsents;
		data = new ArrayList<>();
		this.domain = domain;
		setRowCount(lightConsents.size());
	}

	@Override
	public WebConsent getRowData(String rowKey)
	{
		for (WebConsent consent : data)
		{
			if (consent.getKey().toString().equals(rowKey))
			{
				return consent;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(WebConsent object)
	{
		return object.getKey();
	}

	@Override
	public List<WebConsent> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters)
	{
		data.clear();
		int end = first + pageSize;

		// Sorting
		if (!StringUtils.isEmpty(sortField))
		{
			boolean asc = sortOrder.equals(SortOrder.ASCENDING);

			if (sortField.equals("key.consentDate"))
			{
				Collections.sort(lightConsents, new Comparator<ConsentLightDTO>() {
					public int compare(ConsentLightDTO c1, ConsentLightDTO c2)
					{
						return asc ? c1.getKey().getConsentDate().compareTo(c2.getKey().getConsentDate())
								: c2.getKey().getConsentDate().compareTo(c1.getKey().getConsentDate());
					}
				});
			}
			else if (sortField.equals("key.consentTemplateKey.name"))
			{
				Collections.sort(lightConsents, new Comparator<ConsentLightDTO>() {
					public int compare(ConsentLightDTO c1, ConsentLightDTO c2)
					{
						return asc ? c1.getKey().getConsentTemplateKey().getName().compareTo(c2.getKey().getConsentTemplateKey().getName())
								: c2.getKey().getConsentTemplateKey().getName().compareTo(c1.getKey().getConsentTemplateKey().getName());
					}
				});
			}
			else if (sortField.equals("key.consentTemplateKey.version"))
			{
				Collections.sort(lightConsents, new Comparator<ConsentLightDTO>() {
					public int compare(ConsentLightDTO c1, ConsentLightDTO c2)
					{
						return asc ? c1.getKey().getConsentTemplateKey().getVersion().compareTo(c2.getKey().getConsentTemplateKey().getVersion())
								: c2.getKey().getConsentTemplateKey().getVersion().compareTo(c1.getKey().getConsentTemplateKey().getVersion());
					}
				});
			}
		}

		// Get filter value and create SimpleDateFormat to parse date in filter value
		String filter = null;
		if (!filters.isEmpty() && filters.containsKey("globalFilter") && !StringUtils.isEmpty((String) filters.get("globalFilter")))
		{
			filter = ((String) filters.get("globalFilter")).toLowerCase();
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "msg");
		SimpleDateFormat sdf = new SimpleDateFormat(bundle.getString("common.date.time.pattern"));
		
		// No filter: get and return only consents from "first" to "end"
		if (filter == null)
		{
			for (i = first; i < end && i < lightConsents.size(); i++)
			{
				try
				{
					data.add(getWebConsent(lightConsents.get(i).getKey()));
				}
				catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownSignerIdTypeException
						| UnknownConsentException e)
				{
					e.printStackTrace();
				}
			}
			logger.debug("Loaded full consents for light consents from " + first + " to " + (end));
		}
		// Filter: check all light consents and get matching full consents from "first" to "end"
		else
		{
			int found = 0;
			for (i = 0; i < lightConsents.size(); i++)
			{
				try
				{
					String date = sdf.format(lightConsents.get(i).getKey().getConsentDate()).toLowerCase();
					String signerIds = Arrays.toString(lightConsents.get(i).getKey().getSignerIds().toArray()).toLowerCase();
					String templateName = lightConsents.get(i).getKey().getConsentTemplateKey().getName().toLowerCase();
					String templateVersion = lightConsents.get(i).getKey().getConsentTemplateKey().getVersion().toLowerCase();

					// if filter matches, add
					if (date.contains(filter) || signerIds.contains(filter) || templateName.contains(filter) || templateVersion.contains(filter))
					{
						// TODO setRowCount aufrufen wenn entsprechende dbquery vorhanden
						if (found >= first)
						{
							data.add(getWebConsent(lightConsents.get(i).getKey()));
						}
						found++;
						
						if (found >= end)
						{
							break;
						}
					}
				}
				catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownSignerIdTypeException
						| UnknownConsentException e)
				{
					e.printStackTrace();
				}
			}
			
			// return sublist of filter result for pagination
			logger.debug("Loaded filtered full consents for light consents from " + first + " to " + end);
		}
		return data;
	}

	public int getProgress()
	{
		return i;
	}
	
	private WebConsent getWebConsent(ConsentKeyDTO key) throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException, InconsistentStatusException, UnknownSignerIdTypeException, UnknownConsentException
	{
		WebConsent consent = new WebConsent(service.getConsent(key));
		consent.setTemplate(service.getConsentTemplate(consent.getKey().getConsentTemplateKey()));
		consent.setDomain(domain);
		
		return consent;
	}
}
