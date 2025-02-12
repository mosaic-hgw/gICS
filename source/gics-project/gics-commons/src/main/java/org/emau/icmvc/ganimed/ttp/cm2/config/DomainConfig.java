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
import java.util.HashSet;
import java.util.Set;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.util.PropertiesObject;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.emau.icmvc.ttp.xml.XMLBindingUtil;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DomainConfig", propOrder = {
		"qualityControlConfig",
		"statisticConfig",
		"policiesConfig",
		"scansConfig",
		"notificationsConfig",
		"applicationConfig"
})
@XmlRootElement(name = "domain-config")
public class DomainConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = 5241079938793824683L;
	private static final Logger logger = LogManager.getLogger(DomainConfig.class);
	public static final String DOMAIN_CONFIG_XSD = "domain-config-1.0.0.xsd";
	private static final XMLBindingUtil BINDER = new XMLBindingUtil();

	@XmlElement(name = "quality-control")
	private final QualityControlConfig qualityControlConfig = new QualityControlConfig();

	@XmlElement(name = "statistic")
	private final StatisticConfig statisticConfig = new StatisticConfig();

	@XmlElement(name = "policies")
	private final PoliciesConfig policiesConfig = new PoliciesConfig();

	@XmlElement(name = "scans")
	private final ScansConfig scansConfig = new ScansConfig();

	@XmlElement(name = "notifications")
	private final NotificationsConfig notificationsConfig = new NotificationsConfig();

	@XmlElement(name = "application")
	private final ApplicationConfig applicationConfig = new ApplicationConfig();

	/**
	 * Empty constructor for deserialization.
	 */
	public DomainConfig()
	{
	}

	/**
	 * Copy constructor.
	 */
	public DomainConfig(DomainConfig config)
	{
		capture(config);
	}

	/**
	 * All fields constructor.
	 */
	public DomainConfig(QualityControlConfig qualityControlConfig, StatisticConfig statisticConfig, PoliciesConfig policiesConfig,
			ScansConfig scansConfig, NotificationsConfig notificationsConfig, ApplicationConfig applicationConfig)
	{
		setQualityControlConfig(qualityControlConfig);
		setStatisticConfig(statisticConfig);
		setPoliciesConfig(policiesConfig);
		setScansConfig(scansConfig);
		setNotificationsConfig(notificationsConfig);
		setApplicationConfig(applicationConfig);
	}

	public DomainConfig(String domainName, String configString)
	{
		if (isXmlConfig(configString))
		{
			try
			{
				capture(fromXml(configString));
			}
			catch (JAXBException e)
			{
				logger.warn("Reading domain config failed for XML string: \n" + configString, e);
				capture(fromLegacyCsv(domainName, configString));
			}
		}
		else
		{
			logger.debug("Reading domain config from legacy CVS string: \n{}", configString);
			capture(fromLegacyCsv(domainName, configString));
		}
		normalize(domainName);
	}

	public void capture(DomainConfig config)
	{
		if (config == null)
		{
			config = new DomainConfig();
		}
		setQualityControlConfig(config.getQualityControlConfig());
		setStatisticConfig(config.getStatisticConfig());
		setPoliciesConfig(config.getPoliciesConfig());
		setScansConfig(config.getScansConfig());
		setNotificationsConfig(config.getNotificationsConfig());
		setApplicationConfig(config.getApplicationConfig());
	}

	public QualityControlConfig getQualityControlConfig()
	{
		return qualityControlConfig;
	}

	public void setQualityControlConfig(QualityControlConfig config)
	{
		this.qualityControlConfig.capture(config);
	}

	public StatisticConfig getStatisticConfig()
	{
		return statisticConfig;
	}

	public void setStatisticConfig(StatisticConfig config)
	{
		this.statisticConfig.capture(config);
	}

	public PoliciesConfig getPoliciesConfig()
	{
		return policiesConfig;
	}

	public void setPoliciesConfig(PoliciesConfig config)
	{
		this.policiesConfig.capture(config);
	}

	public ScansConfig getScansConfig()
	{
		return scansConfig;
	}

	public void setScansConfig(ScansConfig config)
	{
		this.scansConfig.capture(config);
	}

	public NotificationsConfig getNotificationsConfig()
	{
		return notificationsConfig;
	}

	public void setNotificationsConfig(NotificationsConfig config)
	{
		this.notificationsConfig.capture(config);
	}

	public ApplicationConfig getApplicationConfig()
	{
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfig config)
	{
		this.applicationConfig.capture(config);
	}

	public void normalize()
	{
		normalize("unknown");
	}

	public void normalize(String domainName)
	{
		getQualityControlConfig().normalize(domainName);
	}

	/**
	 * Updates those parts of this domain config from another config,
	 * which are allowed to change even if the domain is in use (finalized).
	 * For more detail see the single subgroups of the config.
	 *
	 * @param config
	 * 		the new config
	 * @return true if this domain config has changed on update
	 * @see QualityControlConfig#updateUnlockedParts(QualityControlConfig)
	 * @see PoliciesConfig#updateUnlockedParts(PoliciesConfig)
	 * @see ScansConfig#updateUnlockedParts(ScansConfig)
	 * @see StatisticConfig#updateUnlockedParts(StatisticConfig)
	 * @see NotificationsConfig#updateUnlockedParts(NotificationsConfig)
	 * @see ApplicationConfig#updateUnlockedParts(ApplicationConfig)
	 */
	public boolean updateUnlockedParts(DomainConfig config)
	{
		if (config == null)
		{
			return false;
		}

		boolean changed = getQualityControlConfig().updateUnlockedParts(config.getQualityControlConfig());
		changed = getPoliciesConfig().updateUnlockedParts(config.getPoliciesConfig()) || changed;
		changed = getStatisticConfig().updateUnlockedParts(config.getStatisticConfig()) || changed;
		changed = getScansConfig().updateUnlockedParts(config.getScansConfig()) || changed;
		changed = getNotificationsConfig().updateUnlockedParts(config.getNotificationsConfig()) || changed;
		return getApplicationConfig().updateUnlockedParts(config.getApplicationConfig()) || changed;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof DomainConfig that))
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
				.append("qualityControlConfig", qualityControlConfig)
				.append("statisticConfig", statisticConfig)
				.append("policiesConfig", policiesConfig)
				.append("scansConfig", scansConfig)
				.append("notificationsConfig", notificationsConfig)
				.append("applicationConfig", applicationConfig)
				.toString();
	}

	/**
	 * Returns the legacy domain configuration, also known as domain properties,
	 * as semicolon-separated key-value pairs as before up to version 2023.1.x.
	 * Note, that this is only a subset of the current configuration,
	 * e.g. ignoring all configuration related to QC problem-types.
	 *
	 * @return the legacy domain configuration  as of version 2023.1.x known as domain properties
	 */
	public String toLegacyCsv()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DomainProperties.REVOKE_IS_PERMANENT).append("=").append(getPoliciesConfig().isPermanentRevoke()).append(";");
		sb.append(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST).append("=").append(getPoliciesConfig().isTakeHighestVersionInsteadOfNewest()).append(";");
		sb.append(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS).append("=").append(!getScansConfig().isMandatory()).append(";");
		sb.append(DomainProperties.SEND_NOTIFICATIONS_WEB).append("=").append(getNotificationsConfig().isSendFromWeb()).append(";");
		sb.append(DomainProperties.STATISTIC_DOCUMENT_DETAILS).append("=").append(getStatisticConfig().isCalculateDocumentDetails()).append(";");
		sb.append(DomainProperties.STATISTIC_POLICY_DETAILS).append("=").append(getStatisticConfig().isCalculatePolicyDetails()).append(";");
		sb.append(DomainProperties.SCANS_SIZE_LIMIT).append("=").append(getScansConfig().getSizeLimit()).append(";");
		sb.append(DomainProperties.TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST).append("=").append(getPoliciesConfig().isTakeMostSpecificValidityInsteadOfShortest()).append(";");
		Set<String> validQcTypes = getQualityControlConfig().getValidQcTypeValues();
		// prevent [ ] in implicit toString
		if (validQcTypes != null && !validQcTypes.isEmpty())
		{
			sb.append(DomainProperties.VALID_QC_TYPES).append("=").append(String.join(",", validQcTypes)).append(";");
		}
		Set<String> invalidQcTypes = getQualityControlConfig().getInvalidQcTypeValues();
		// prevent [ ] in implicit toString
		if (invalidQcTypes != null && !invalidQcTypes.isEmpty())
		{
			sb.append(DomainProperties.INVALID_QC_TYPES).append("=").append(String.join(",", invalidQcTypes)).append(";");
		}
		sb.append(DomainProperties.DEFAULT_QC_TYPE).append("=").append(getQualityControlConfig().getDefaultTypeId());
		return sb.toString();
	}

	/**
	 * Legacy CSV-parsing mainly for backward-compatibility when reading exported domain configs.
	 *
	 * @param domainName
	 * 		the name of the domain used for warning messages only
	 * @param propertiesString
	 * 		the properties string to parse
	 */
	private static DomainConfig fromLegacyCsv(String domainName, String propertiesString)
	{
		DomainConfig c = new DomainConfig();
		PropertiesObject props = new PropertiesObject(propertiesString);

		// ===== policies-config =====
		c.getPoliciesConfig().setPermanentRevoke(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.REVOKE_IS_PERMANENT.toString())));
		c.getPoliciesConfig().setTakeHighestVersionInsteadOfNewest(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST.toString())));
		c.getPoliciesConfig().setTakeMostSpecificValidityInsteadOfShortest(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST.toString())));

		// ===== notification-config =====
		c.getNotificationsConfig().setSendFromWeb(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.SEND_NOTIFICATIONS_WEB.toString())));

		// ===== statistic-config =====
		c.getStatisticConfig().setCalculateDocumentDetails(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.STATISTIC_DOCUMENT_DETAILS.toString())));
		c.getStatisticConfig().setCalculatePolicyDetails(Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.STATISTIC_POLICY_DETAILS.toString())));

		// ===== scans-config =====
		c.getScansConfig().setMandatory(!Boolean.TRUE.toString().equalsIgnoreCase(
				props.getProperty(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS.toString())));
		int scansSizeLimit = ScansConfig.DEFAULT_SIZE_LIMIT;
		String temp = props.getProperty(DomainProperties.SCANS_SIZE_LIMIT.toString());
		try
		{
			scansSizeLimit = StringUtils.isEmpty(temp) ? ScansConfig.DEFAULT_SIZE_LIMIT : Integer.parseInt(temp);
		}
		catch (NumberFormatException e)
		{
			logger.warn("Cannot parse scansSizeLimit {} for domain {}. Using default value {} instead.",
					temp, domainName, ScansConfig.DEFAULT_SIZE_LIMIT);
		}
		c.getScansConfig().setSizeLimit(scansSizeLimit);

		// ===== quality-control-config =====
		c.getQualityControlConfig().setValidQcTypeValues(splitCommaSeparatedStrings(props.getProperty(DomainProperties.VALID_QC_TYPES.toString())));
		c.getQualityControlConfig().setInvalidQcTypeValues(splitCommaSeparatedStrings(props.getProperty(DomainProperties.INVALID_QC_TYPES.toString())));
		c.getQualityControlConfig().setDefaultTypeId(props.getProperty(DomainProperties.DEFAULT_QC_TYPE.toString(), QCDTO.AUTO_GENERATED));

		return c;
	}

	public void validate() throws InvalidParameterException
	{
		validate(null);
	}

	public void validate(String paramName) throws InvalidParameterException
	{
		getQualityControlConfig().validate(paramName);

		// finally check if the config is serializable to XML

		try
		{
			toXml();
		}
		catch (JAXBException e)
		{
			throw new InvalidParameterException(paramName, e.getMessage(), e);
		}
	}

	/**
	 * Marshals this domain configuration into the XML representation.
	 *
	 * @return the XML representation for this domain configuration
	 * @throws JAXBException
	 * 		if this domain configuration can not be marshalled
	 */
	public String toXml() throws JAXBException
	{
		return toXml(true);
	}

	public String toXml(boolean validate) throws JAXBException
	{
		if (validate)
		{
			return BINDER.marshal(DOMAIN_CONFIG_XSD, this);
		}
		else
		{
			return BINDER.marshal(this);
		}
	}

	/**
	 * Creates a domain configuration from an XML description
	 *
	 * @param configXml
	 * 		the configuration as XML
	 * @return a domain configuration for an XML description
	 * @throws JAXBException
	 * 		if the XML cannot be parsed
	 */
	public static DomainConfig fromXml(String configXml) throws JAXBException
	{
		return BINDER.parse(DomainConfig.class, configXml, DOMAIN_CONFIG_XSD);
	}

	private static Set<String> splitCommaSeparatedStrings(String temp)
	{
		Set<String> strings = new HashSet<>();
		if (isNotEmpty(temp))
		{
			strings.addAll(Arrays.stream(temp.split(",")).map(String::trim).toList());
		}
		return strings;
	}

	public static boolean isXmlConfig(String config)
	{
		return !StringUtils.isBlank(config) && config.contains(":domain-config>") && config.contains(ObjectFactory.NAME_SPACE);
	}

	public static DomainConfig createDefaultDomainConfig()
	{
		DomainConfig config = new DomainConfig();
		config.normalize();
		return config;
	}
}
