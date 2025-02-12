package org.emau.icmvc.ganimed.ttp.cm2.config;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationConfig", propOrder = { "enableChromePdfExport", "chromedriverPath", "templateTypes", "embeddedCss" })
public class ApplicationConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = 3581874137927738031L;
	private static final Set<ConsentTemplateType> DEFAULT_TEMPLATE_TYPES = new LinkedHashSet<>(Arrays.asList(ConsentTemplateType.CONSENT, ConsentTemplateType.REVOCATION, ConsentTemplateType.REFUSAL));

	@XmlAttribute(name = "enable-chrome-pdf-export")
	private boolean enableChromePdfExport = false;

	@XmlAttribute(name = "chromedriver-path")
	private String chromedriverPath = "";

	@XmlElement(name = "template-types")
	private Set<ConsentTemplateType> templateTypes = new LinkedHashSet<>(DEFAULT_TEMPLATE_TYPES);

	@XmlAttribute(name = "embedded-css")
	private String embeddedCss = "";

	/**
	 * Empty constructor for deserialization.
	 */
	public ApplicationConfig()
	{
	}

	/**
	 * Copy constructor.
	 */
	public ApplicationConfig(ApplicationConfig config)
	{
		capture(config);
	}

	/**
	 * All fields constructor.
	 */
	public ApplicationConfig(boolean enableChromePdfExport, String chromedriverPath, Set<ConsentTemplateType> templateTypes, String embeddedCss)
	{
		this.enableChromePdfExport = enableChromePdfExport;
		this.chromedriverPath = chromedriverPath;
		this.templateTypes = templateTypes;
		this.embeddedCss = embeddedCss;
	}

	public void capture(ApplicationConfig config)
	{
		if (config == null)
		{
			config = new ApplicationConfig();
		}
		setEnableChromePdfExport(config.isEnableChromePdfExport());
		setChromedriverPath(config.getChromedriverPath());
		setTemplateTypes(config.getTemplateTypes());
		setEmbeddedCss(config.getEmbeddedCss());
	}

	public boolean containsTemplateType(String type)
	{
		return templateTypes.contains(ConsentTemplateType.valueOf(type));
	}

	public boolean isEnableChromePdfExport()
	{
		return enableChromePdfExport;
	}

	public void setEnableChromePdfExport(boolean enableChromePdfExport)
	{
		this.enableChromePdfExport = enableChromePdfExport;
	}

	public String getChromedriverPath()
	{
		return chromedriverPath;
	}

	public void setChromedriverPath(String chromedriverPath)
	{
		this.chromedriverPath = chromedriverPath;
	}

	public Set<ConsentTemplateType> getTemplateTypes()
	{
		return templateTypes;
	}

	public void setTemplateTypes(Set<ConsentTemplateType> templateTypes)
	{
		this.templateTypes = templateTypes;
	}

	public String getEmbeddedCss()
	{
		return embeddedCss;
	}

	public void setEmbeddedCss(String embeddedCss)
	{
		this.embeddedCss = embeddedCss;
	}

	/**
	 * Updates the send-from-web from the given config.
	 *
	 * @param config
	 * 		the config to update from
	 * @return true if the config changed on update
	 */
	public boolean updateUnlockedParts(ApplicationConfig config)
	{
		ApplicationConfig nc = new ApplicationConfig(this);
		capture(config);
		return !equals(nc);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ApplicationConfig that))
			return false;

		return new EqualsBuilder().append(isEnableChromePdfExport(), that.isEnableChromePdfExport()).append(getChromedriverPath(), that.getChromedriverPath())
				.append(getTemplateTypes(), that.getTemplateTypes()).append(getEmbeddedCss(), that.getEmbeddedCss()).isEquals();
	}

	@Override public int hashCode()
	{
		return new HashCodeBuilder(17, 37).append(isEnableChromePdfExport()).append(getChromedriverPath()).append(getTemplateTypes()).append(getEmbeddedCss()).toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("enableChromePdfExport", enableChromePdfExport)
				.append("chromedriverPath", chromedriverPath)
				.append("templateTypes", templateTypes)
				.append("embeddedCss", embeddedCss)
				.toString();
	}
}
