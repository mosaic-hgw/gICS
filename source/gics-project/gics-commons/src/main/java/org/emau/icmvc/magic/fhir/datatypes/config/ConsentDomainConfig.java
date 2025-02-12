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

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentDomainConfig")
public class ConsentDomainConfig extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = -7152673513602761995L;

	@Child(name = "qualityControl", order = 0)
	@Description(shortDefinition = "configuration options for quality control")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentQualityControlConfig qualityControlConfig = new ConsentQualityControlConfig();

	@Child(name = "statistic", order = 1)
	@Description(shortDefinition = "configuration options for dashboard statistics")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentStatisticConfig statisticConfig = new ConsentStatisticConfig();

	@Child(name = "policies", order = 2)
	@Description(shortDefinition = "configuration options for handling policies")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentPoliciesConfig policiesConfig = new ConsentPoliciesConfig();

	@Child(name = "scans", order = 3)
	@Description(shortDefinition = "configuration options for handling scans")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentScansConfig scansConfig = new ConsentScansConfig();

	@Child(name = "notifications", order = 4)
	@Description(shortDefinition = "configuration options for handling scans")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentNotificationsConfig notificationsConfig = new ConsentNotificationsConfig();

	@Child(name = "application", order = 5)
	@Description(shortDefinition = "configuration options for the gics application")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private ConsentApplicationConfig applicationConfig = new ConsentApplicationConfig();

	public ConsentDomainConfig()
	{
	}

	public ConsentDomainConfig(DomainConfig config)
	{
		if (config == null)
		{
			config = new DomainConfig();
		}
		setQualityControlConfig(new ConsentQualityControlConfig(config.getQualityControlConfig()));
		setStatisticConfig(new ConsentStatisticConfig(config.getStatisticConfig()));
		setPoliciesConfig(new ConsentPoliciesConfig(config.getPoliciesConfig()));
		setScansConfig(new ConsentScansConfig(config.getScansConfig()));
		setNotificationsConfig(new ConsentNotificationsConfig(config.getNotificationsConfig()));
		setApplicationConfig(new ConsentApplicationConfig(config.getApplicationConfig()));
	}

	public DomainConfig toDomainConfig() throws InvalidExchangeFormatException
	{
		return new DomainConfig(
				getQualityControlConfig().toQualityControlConfig(),
				getStatisticConfig().toStatisticConfig(),
				getPoliciesConfig().toPoliciesConfig(),
				getScansConfig().toScansConfig(),
				getNotificationsConfig().toNotificationsConfig(),
				getApplicationConfig().toApplicationConfig());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(qualityControlConfig, statisticConfig, policiesConfig, scansConfig, notificationsConfig);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentDomainConfig deepCopy()
	{
		ConsentDomainConfig config = new ConsentDomainConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentDomainConfig config)
	{
		if (config == null)
		{
			config = new ConsentDomainConfig();
		}
		setQualityControlConfig(config.getQualityControlConfig());
		setStatisticConfig(config.getStatisticConfig());
		setPoliciesConfig(config.getPoliciesConfig());
		setScansConfig(config.getScansConfig());
		setNotificationsConfig(config.getNotificationsConfig());
		setApplicationConfig(config.getApplicationConfig());
	}

	public ConsentQualityControlConfig getQualityControlConfig()
	{
		return qualityControlConfig;
	}

	public void setQualityControlConfig(ConsentQualityControlConfig qualityControlConfig)
	{
		this.qualityControlConfig.capture(qualityControlConfig);
	}

	public ConsentStatisticConfig getStatisticConfig()
	{
		return statisticConfig;
	}

	public void setStatisticConfig(ConsentStatisticConfig statisticConfig)
	{
		this.statisticConfig.capture(statisticConfig);
	}

	public ConsentPoliciesConfig getPoliciesConfig()
	{
		return policiesConfig;
	}

	public void setPoliciesConfig(ConsentPoliciesConfig policiesConfig)
	{
		this.policiesConfig.capture(policiesConfig);
	}

	public ConsentScansConfig getScansConfig()
	{
		return scansConfig;
	}

	public void setScansConfig(ConsentScansConfig scansConfig)
	{
		this.scansConfig.capture(scansConfig);
	}

	public ConsentNotificationsConfig getNotificationsConfig()
	{
		return notificationsConfig;
	}

	public void setNotificationsConfig(ConsentNotificationsConfig notificationsConfig)
	{
		this.notificationsConfig.capture(notificationsConfig);
	}

	public ConsentApplicationConfig getApplicationConfig()
	{
		return applicationConfig;
	}

	public void setApplicationConfig(ConsentApplicationConfig applicationConfig)
	{
		this.applicationConfig.capture(applicationConfig);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentDomainConfig that))
			return false;

		return new EqualsBuilder().append(getQualityControlConfig(), that.getQualityControlConfig()).append(getStatisticConfig(), that.getStatisticConfig())
				.append(getPoliciesConfig(), that.getPoliciesConfig()).append(getScansConfig(), that.getScansConfig()).append(getNotificationsConfig(), that.getNotificationsConfig())
				.append(getApplicationConfig(), that.getApplicationConfig()).isEquals();
	}

	@Override public int hashCode()
	{
		return new HashCodeBuilder(17, 37).append(getQualityControlConfig()).append(getStatisticConfig()).append(getPoliciesConfig()).append(getScansConfig()).append(getNotificationsConfig())
				.append(getApplicationConfig()).toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("qualityControl", qualityControlConfig)
				.append("statistic", statisticConfig)
				.append("policies", policiesConfig)
				.append("scans", scansConfig)
				.append("notifications", notificationsConfig)
				.append("application", applicationConfig)
				.toString();
	}
}
