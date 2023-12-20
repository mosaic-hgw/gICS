package org.emau.icmvc.ganimed.ttp.cm2.internal;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;

public class DomainPropertiesObject extends PropertiesObject implements Serializable
{
	private static final long serialVersionUID = 5241079938793824683L;
	private static final Logger logger = LogManager.getLogger(DomainPropertiesObject.class);
	private static final int DEFAULT_SCAN_SIZE_LIMIT = 10485760;
	private final boolean takeHighestVersion;
	private final boolean permanentRevoke;
	private final boolean noMandatoryScans;
	private final boolean sendNotificationsWeb;
	private final boolean statisticDocumentDetails;
	private final boolean statisticPolicyDetails;
	private final int scansSizeLimit;
	private final boolean takeSpecificValidity;
	private final Set<String> validQcTypes = new HashSet<>();
	private final Set<String> invalidQcTypes = new HashSet<>();
	private final String defaultQcType;

	public DomainPropertiesObject(String domainName, String propertiesString)
	{
		super(propertiesString);
		Properties properties = getProperties();
		String temp = (String) properties.get(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST.toString());
		takeHighestVersion = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.REVOKE_IS_PERMANENT.toString());
		permanentRevoke = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS.toString());
		noMandatoryScans = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SEND_NOTIFICATIONS_WEB.toString());
		sendNotificationsWeb = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.STATISTIC_DOCUMENT_DETAILS.toString());
		statisticDocumentDetails = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.STATISTIC_POLICY_DETAILS.toString());
		statisticPolicyDetails = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SCANS_SIZE_LIMIT.toString());
		int tempInt = DEFAULT_SCAN_SIZE_LIMIT;
		try
		{
			tempInt = StringUtils.isEmpty(temp) ? DEFAULT_SCAN_SIZE_LIMIT : Integer.parseInt(temp);
		}
		catch (NumberFormatException e)
		{
			logger.warn("Cannot parse scansSizeLimit " + temp + " for domain " + domainName + ". Using default value " + DEFAULT_SCAN_SIZE_LIMIT + " instead.");
		}
		scansSizeLimit = tempInt;
		temp = (String) properties.get(DomainProperties.TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST.toString());
		takeSpecificValidity = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.VALID_QC_TYPES.toString());
		if (isNotEmpty(temp))
		{
			validQcTypes.addAll(Arrays.asList(temp.split(",")));
		}
		temp = (String) properties.get(DomainProperties.INVALID_QC_TYPES.toString());
		if (isNotEmpty(temp))
		{
			invalidQcTypes.addAll(Arrays.asList(temp.split(",")));
		}
		temp = (String) properties.get(DomainProperties.DEFAULT_QC_TYPE.toString());
		if (!validQcTypes.contains(temp) && !invalidQcTypes.contains(temp))
		{
			logger.warn("default qc type " + temp + " doesn't exist in valid or invalid qc types for domain " + domainName + ", setting it to " + QCDTO.AUTO_GENERATED);
			temp = QCDTO.AUTO_GENERATED;
		}
		defaultQcType = temp;
		if (!validQcTypes.contains(QCDTO.AUTO_GENERATED))
		{
			if (logger.isInfoEnabled())
			{
				logger.info(QCDTO.AUTO_GENERATED + " doesn't exist in valid qc types for domain " + domainName + ", adding it");
			}
			validQcTypes.add(QCDTO.AUTO_GENERATED);
		}
	}

	/**
	 * Return true if the domain is configured to send notifications from web interface.
	 *
	 * @return true if the domain is configured to send notifications from web interface.
	 */
	public boolean isSendNotificationsWeb()
	{
		return sendNotificationsWeb;
	}

	public boolean isTakeHighestVersion()
	{
		return takeHighestVersion;
	}

	public boolean isPermanentRevoke()
	{
		return permanentRevoke;
	}

	public boolean isNoMandatoryScans()
	{
		return noMandatoryScans;
	}

	public int getScansSizeLimit()
	{
		return scansSizeLimit;
	}

	public boolean isTakeSpecificValidity()
	{
		return takeSpecificValidity;
	}

	public Set<String> getValidQcTypes()
	{
		return validQcTypes;
	}

	public Set<String> getInvalidQcTypes()
	{
		return invalidQcTypes;
	}

	public String getDefaultQcType()
	{
		return defaultQcType;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (defaultQcType == null ? 0 : defaultQcType.hashCode());
		result = prime * result + (invalidQcTypes == null ? 0 : invalidQcTypes.hashCode());
		result = prime * result + (noMandatoryScans ? 1231 : 1237);
		result = prime * result + (sendNotificationsWeb ? 1231 : 1237);
		result = prime * result + (permanentRevoke ? 1231 : 1237);
		result = prime * result + scansSizeLimit;
		result = prime * result + (takeHighestVersion ? 1231 : 1237);
		result = prime * result + (takeSpecificValidity ? 1231 : 1237);
		result = prime * result + (validQcTypes == null ? 0 : validQcTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DomainPropertiesObject other = (DomainPropertiesObject) obj;
		if (defaultQcType == null)
		{
			if (other.defaultQcType != null)
			{
				return false;
			}
		}
		else if (!defaultQcType.equals(other.defaultQcType))
		{
			return false;
		}
		if (invalidQcTypes == null)
		{
			if (other.invalidQcTypes != null)
			{
				return false;
			}
		}
		else if (!invalidQcTypes.equals(other.invalidQcTypes))
		{
			return false;
		}
		if (noMandatoryScans != other.noMandatoryScans)
		{
			return false;
		}
		if (sendNotificationsWeb != other.sendNotificationsWeb)
		{
			return false;
		}
		if (permanentRevoke != other.permanentRevoke)
		{
			return false;
		}
		if (scansSizeLimit != other.scansSizeLimit)
		{
			return false;
		}
		if (takeHighestVersion != other.takeHighestVersion)
		{
			return false;
		}
		if (takeSpecificValidity != other.takeSpecificValidity)
		{
			return false;
		}
		if (validQcTypes == null)
		{
			if (other.validQcTypes != null)
			{
				return false;
			}
		}
		else if (!validQcTypes.equals(other.validQcTypes))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DomainProperties.REVOKE_IS_PERMANENT).append("=").append(permanentRevoke).append(";");
		sb.append(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST).append("=").append(takeHighestVersion).append(";");
		sb.append(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS).append("=").append(noMandatoryScans).append(";");
		sb.append(DomainProperties.SEND_NOTIFICATIONS_WEB).append("=").append(sendNotificationsWeb).append(";");
		sb.append(DomainProperties.STATISTIC_DOCUMENT_DETAILS).append("=").append(statisticDocumentDetails).append(";");
		sb.append(DomainProperties.STATISTIC_POLICY_DETAILS).append("=").append(statisticPolicyDetails).append(";");
		sb.append(DomainProperties.SCANS_SIZE_LIMIT).append("=").append(scansSizeLimit).append(";");
		sb.append(DomainProperties.TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST).append("=").append(takeSpecificValidity).append(";");
		// prevent [ ] in implicit toString
		if (validQcTypes != null && !validQcTypes.isEmpty())
		{
			sb.append(DomainProperties.VALID_QC_TYPES).append("=").append(String.join(",", validQcTypes)).append(";");
		}
		// prevent [ ] in implicit toString
		if (invalidQcTypes != null && !invalidQcTypes.isEmpty())
		{
			sb.append(DomainProperties.INVALID_QC_TYPES).append("=").append(String.join(",", invalidQcTypes)).append(";");
		}
		sb.append(DomainProperties.DEFAULT_QC_TYPE).append("=").append(defaultQcType);
		return sb.toString();
	}
}
