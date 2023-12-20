package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for lazy loading of consents
 *
 * @author Arne Blumentritt
 */
public class WebConsentLazyModel extends LazyDataModel<WebConsent>
{
	private static final long serialVersionUID = 1104825405795685955L;
	private transient final Logger logger = LoggerFactory.getLogger(getClass());

	private final transient GICSService service;
	private final PaginationConfig paginationConfig;
	private final List<WebConsent> currentPageData = new ArrayList<>();
	private final Map<ConsentTemplateKeyDTO, ConsentTemplateDTO> templates = new HashMap<>();
	private final Map<ConsentKeyDTO, WebConsent> webConsents = new HashMap<>();
	private final DomainDTO domain;
	private transient VersionConverter versionConverter;
	private final boolean signerIdFilter;

	public WebConsentLazyModel(GICSService service, DomainDTO domain, PaginationConfig paginationConfig, boolean signerIdFilter)
	{
		this.service = service;
		this.domain = domain;
		this.paginationConfig = paginationConfig;
		this.signerIdFilter = signerIdFilter;

		try
		{
			Class<? extends VersionConverter> versionConverterClass = Class.forName(domain.getCtVersionConverter()).asSubclass(VersionConverter.class);
			versionConverter = versionConverterClass.newInstance();
		}
		catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
		{
			logger.error("Error init consent list: " + e.getLocalizedMessage());
		}
	}

	@Override
	public WebConsent getRowData(String rowKey)
	{
		for (WebConsent webConsent : currentPageData)
		{
			if (rowKey.replace("~", ",").equals(webConsent.getKey().toString()))
			{
				return webConsent;
			}
		}
		return null;
	}

	@Override
	public String getRowKey(WebConsent object)
	{
		return object.getKey().toString().replace(",", "~");
	}

	@Override
	public List<WebConsent> load(int first, int pageSize, Map<String, SortMeta> sortFields, Map<String, FilterMeta> filters)
	{
		currentPageData.clear();

		// Set page for request
		paginationConfig.setFirstEntry(first);
		paginationConfig.setPageSize(pageSize);

		// Sorting
		if (!sortFields.isEmpty())
		{
			SortMeta sortMeta = sortFields.values().iterator().next();
			paginationConfig.setSortIsAscending(SortOrder.ASCENDING.equals(sortMeta.getOrder()));

			switch (sortMeta.getField())
			{
				case "key.consentDate":
					paginationConfig.setSortField(ConsentField.DATE);
					break;
				case "template.label":
					paginationConfig.setSortField(ConsentField.CT_LABEL);
					break;
				case "key.consentTemplateKey.version":
					paginationConfig.setSortField(ConsentField.CT_VERSION);
					break;
				default:
					logger.warn("Unknown sort field {}", sortMeta.getField());
			}
		}
		else
		{
			paginationConfig.setSortField(ConsentField.NONE);
		}

		// Filter
		if (!filters.isEmpty() || !paginationConfig.getFilter().isEmpty())
		{
			Map<ConsentField, String> filterMap = paginationConfig.getFilter();

			// Global Filter
			if (filters.containsKey("globalFilter"))
			{
				String globalFilter = (String) filters.get("globalFilter").getFilterValue();
				if (StringUtils.isNotEmpty(globalFilter))
				{
					// Filter CT_LABEL and (or) CT_NAME
					filterMap.put(ConsentField.CT_LABEL, globalFilter);
					filterMap.put(ConsentField.CT_NAME, globalFilter);
					// Filter SIGNER_ID. Don't overwrite if predfined
					if (!signerIdFilter)
					{
						filterMap.put(ConsentField.SIGNER_ID, globalFilter);
					}
					// Filter CT_VERSION. Check if filter is parsable as a version
					try
					{
						versionConverter.stringToInt(globalFilter);
						filterMap.put(ConsentField.CT_VERSION, globalFilter);
					}
					catch (InvalidVersionException e)
					{
						// thats ok
					}
				}
				else
				{
					filterMap.remove(ConsentField.CT_LABEL);
					filterMap.remove(ConsentField.CT_NAME);
					filterMap.remove(ConsentField.CT_VERSION);
					if (!signerIdFilter)
					{
						filterMap.remove(ConsentField.SIGNER_ID);
					}
				}
			}

			// QC Filter
			if (filters.containsKey("qualityControl") && filters.get("qualityControl").getFilterValue() != null)
			{
				filterMap.put(ConsentField.QC_TYPE, String.join(",", (String[]) filters.get("qualityControl").getFilterValue()));
			}
			else
			{
				filterMap.remove(ConsentField.QC_TYPE);
			}

			// Save Filter
			paginationConfig.setFilter(filterMap);
		}

		try
		{
			setRowCount((int) service.countConsentsForDomainWithFilter(domain.getName(), paginationConfig));

			for (ConsentLightDTO consentLightDTO : service.getConsentsForDomainPaginated(domain.getName(), paginationConfig))
			{
				currentPageData.add(getWebConsent(consentLightDTO));
			}
		}
		catch (InconsistentStatusException | InvalidVersionException | UnknownDomainException | VersionConverterClassException | UnknownConsentException
				| UnknownConsentTemplateException | UnknownSignerIdTypeException | InvalidParameterException e)
		{
			logger.error(e.getLocalizedMessage());
		}
		logger.debug("Loaded consents paginated from {} to {}", first, first + pageSize);

		return currentPageData;
	}

	private WebConsent getWebConsent(ConsentLightDTO lightDTO) throws UnknownDomainException, UnknownConsentTemplateException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException, UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		WebConsent webConsent;
		if (webConsents.containsKey(lightDTO.getKey()))
		{
			webConsent = webConsents.get(lightDTO.getKey());
			webConsent.updateFromLightDTO(lightDTO);
		}
		else
		{
			webConsent = new WebConsent(service.getConsent(lightDTO.getKey()));
			webConsent.setTemplate(getTemplate(webConsent.getKey().getConsentTemplateKey()));
			webConsent.setDomain(domain);
			webConsents.put(lightDTO.getKey(), webConsent);
		}
		// This might be changed in frontend
		webConsent.setQcHistory(service.getQCHistoryForConsent(webConsent.getKey()));
		return webConsent;
	}

	/**
	 * Returns the count of items in the database wrt. the filter configuration.
	 * It is legal to implement this method as a dummy e.g. always returning 0 (like this implementation does),
	 * as long as {@link #setRowCount(int)} is used correctly in {@link #load(int, int, Map, Map)}.
	 * In other words, when this method is implemented correctly, there is no need to call
	 * {@link #setRowCount(int)} in {@link #load(int, int, Map, Map)} anymore.
	 *
	 * @see <a href="https://primefaces.github.io/primefaces/11_0_0/#/../migrationguide/11_0_0?id=datatable-dataview-datagrid-datalist">DataTable section in PF Migration guide 10 -> 11</a>
	 * @see <a href="https://primefaces.github.io/primefaces/11_0_0/#/components/datatable?id=lazy-loading">Lazy Loading in DataTable part of PF Documentation</a>
	 *
	 * @param filterBy
	 *            the filter map
	 * @return the number of items in the database wrt. the filter configuration or any arbitrary value, when {@link #setRowCount(int)} is used correctly
	 */
	@Override
	public int count(Map<String, FilterMeta> filterBy)
	{
		return 0;
	}

	private ConsentTemplateDTO getTemplate(ConsentTemplateKeyDTO key)
	{
		if (!templates.containsKey(key))
		{
			try
			{
				templates.put(key, service.getConsentTemplate(key));
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
			{
				logger.error(e.getLocalizedMessage());
			}
		}
		return templates.get(key);
	}
}
