package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common;

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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.DomainSelector;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.Versions;
import org.emau.icmvc.ganimed.ttp.cm2.util.Dates;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorCharVersionConverter;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * Abstract class for web beans
 *
 * @author Arne Blumentritt
 *
 */
public abstract class AbstractGICSBean extends AbstractGICSServiceBean
{
	protected static final String NOT_CHECKED = "not_checked";
	protected static final String CHECKED_NO_FAULTS = "checked_no_faults";
	protected static final String CHECKED_MINOR_FAULTS = "checked_minor_faults";
	protected static final String CHECKED_MAJOR_FAULTS = "checked_major_faults";
	protected static final String INVALIDATED = "invalidated";

	@ManagedProperty(value = "#{domainSelector}")
	protected DomainSelector domainSelector;

	@ManagedProperty(value = "#{Versions}")
	protected Versions versionPath;

	public void setDomainSelector(DomainSelector domainSelector)
	{
		this.domainSelector = domainSelector;
	}

	public DomainDTO getSelectedDomain()
	{
		return domainSelector.getSelectedDomain();
	}

	public String getSelectedDomainName()
	{
		DomainDTO selectedDomain = domainSelector.getSelectedDomain();
		return selectedDomain == null ? null : selectedDomain.getName();
	}

	public String getQcTypeLabel(String type)
	{
		if (getBundle().containsKey("model.consent.qc.type." + type))
		{
			return getBundle().getString("model.consent.qc.type." + type);
		}
		return type;
	}
	
	public String getPolicyLabelAndVersion(PolicyDTO policy)
	{
		return getPolicyLabel(policy) + " " + policy.getKey().getVersion();
	}

	public String getPolicyLabel(PolicyDTO policy)
	{
		String label = policy != null ? policy.getLabel() : null;
		return StringUtils.isNotBlank(label) ? label : policy != null ? policy.getKey().getName() : null;
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

	public String getINVALIDATED()
	{
		return INVALIDATED;
	}

	public int convertCtVersion(String version)
	{
		return convertVersion(version, getSelectedDomain().getCtVersionConverterInstance());
	}

	public int convertModuleVersion(String version)
	{
		return convertVersion(version, getSelectedDomain().getModuleVersionConverterInstance());
	}

	public int convertPolicyVersion(String version)
	{
		return convertVersion(version, getSelectedDomain().getPolicyVersionConverterInstance());
	}

	public int convertVersion(String version, VersionConverter vc)
	{
		if (StringUtils.isBlank(version))
		{
			return 0;
		}

		if (vc != null)
		{
			try
			{
				int v = vc.stringToInt(version);

				v *= switch (vc.numberOfParts())  // mixed versions with 1, 2, or 3 parts should be comparable
				{
					case 1 -> 1000000;
					case 2 -> 1000;
					default -> 1;
				};

				if (vc instanceof MajorMinorCharVersionConverter)
				{
					v += 900; // 1.1.a should be sorted after 1.1 and 1.1.b after 1.1.1
				}

				return v;
			}
			catch (InvalidVersionException e)
			{
				logger.warn("Invalid version {}: {}", version, e.getLocalizedMessage());
			}
		}
		else
		{
			logger.warn("No version converter to convert {}", version);
		}

		// fallback: not really correct for all theoretical cases but pragmatically helpful for many
		return version.chars().reduce(0, (a, b) -> 100 * a + b);
	}

	public LocalDate getToday()
	{
		return Dates.today();
	}

	public LocalDate getTomorrow()
	{
		return Dates.tomorrow();
	}
}
