package org.emau.icmvc.magic.fhir.datatypes.config;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.ApplicationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ApplicationConfig")
public class ConsentApplicationConfig extends Type implements ICompositeType
{
	@Serial private static final long serialVersionUID = 7330869731062783710L;
	private static final boolean DEFAULT_ENABLE_CHROME_PDF_EXPORT = false;

	@Child(name = "enableChromePdfExport", order = 0)
	@Description(shortDefinition = "true to enable template pdf export, requires installed chrome browser and chromedriver")
	private BooleanDt enableChromePdfExport = new BooleanDt(DEFAULT_ENABLE_CHROME_PDF_EXPORT);

	@Child(name = "chromedriverPath", order = 1)
	@Description(shortDefinition = "installation path of the chromedriver for pdf generation")
	private StringDt chromedriverPath = new StringDt("");

	@Child(name = "templateTypes", order = 2, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of shown template types in domain")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private List<StringDt> templateTypes = new ArrayList<>();

	@Child(name = "embeddedCss", order = 3)
	@Description(shortDefinition = "custom css for gics embedded app views")
	private StringDt embeddedCss = new StringDt("");

	public ConsentApplicationConfig()
	{
	}

	public ConsentApplicationConfig(ApplicationConfig config)
	{
		if (config == null)
		{
			config = new ApplicationConfig();
		}
		setChromedriverPath(config.getChromedriverPath());
		setTemplateTypes(config.getTemplateTypes().stream().map(t -> new StringDt(t.name())).collect(Collectors.toSet()));
		setEnableChromePdfExport(config.isEnableChromePdfExport());
		setEmbeddedCss(config.getEmbeddedCss());
	}

	public ApplicationConfig toApplicationConfig()
	{
		return new ApplicationConfig(isEnableChromePdfExport(), getChromedriverPath(), getTemplateTypes().stream().map(t -> ConsentTemplateType.valueOf(t.getValue())).collect(Collectors.toSet()), getEmbeddedCss());
	}

	@Override
	public boolean isEmpty()
	{
		return isEnableChromePdfExport() == DEFAULT_ENABLE_CHROME_PDF_EXPORT
				&& ElementUtil.isEmpty(chromedriverPath, templateTypes, embeddedCss);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentApplicationConfig deepCopy()
	{
		ConsentApplicationConfig config = new ConsentApplicationConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentApplicationConfig config)
	{
		if (config == null)
		{
			config = new ConsentApplicationConfig();
		}
		setEnableChromePdfExport(config.isEnableChromePdfExport());
		setChromedriverPath(config.getChromedriverPath());
		setTemplateTypes(config.getTemplateTypes());
		setEmbeddedCss(config.getEmbeddedCss());
	}

	public boolean isEnableChromePdfExport()
	{
		if (enableChromePdfExport == null)
		{
			enableChromePdfExport = new BooleanDt(DEFAULT_ENABLE_CHROME_PDF_EXPORT);
		}
		return enableChromePdfExport.getValue();
	}

	public void setEnableChromePdfExport(boolean enableChromePdfExport)
	{
		this.enableChromePdfExport.setValue(enableChromePdfExport);
	}

	public String getChromedriverPath()
	{
		if (chromedriverPath == null)
		{
			chromedriverPath = new StringDt("");
		}
		return chromedriverPath.getValue();
	}

	public void setChromedriverPath(String chromedriverPath)
	{
		this.chromedriverPath.setValue(chromedriverPath);
	}

	public String getEmbeddedCss()
	{
		if (embeddedCss == null)
		{
			embeddedCss = new StringDt("");
		}
		return embeddedCss.getValue();
	}

	public void setEmbeddedCss(String embeddedCss)
	{
		this.embeddedCss.setValue(embeddedCss);
	}

	public Set<StringDt> getTemplateTypes()
	{
		return new HashSet<>(templateTypes);
	}

	public void setTemplateTypes(Set<StringDt> templateTypes)
	{
		this.templateTypes.clear();
		if (templateTypes != null)
		{
			this.templateTypes.addAll(templateTypes);
		}
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentApplicationConfig that))
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
				.append("enableChromePdfExport", isEnableChromePdfExport())
				.append("chromedriverPath", getChromedriverPath())
				.append("templateTypes", getTemplateTypes())
				.append("embeddedCss", getEmbeddedCss())
				.toString();
	}
}
