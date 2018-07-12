package org.emau.icmvc.ganimed.ttp.cm2.internal;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainPropertiesObject extends PropertiesObject implements Serializable {

	private static final long serialVersionUID = -5050023943886932483L;
	private static final Logger logger = LoggerFactory.getLogger(DomainPropertiesObject.class);
	private final boolean takeHighestVersion;
	private final boolean permanentRevoke;
	private final boolean noMandatoryScans;
	private final int scansSizeLimit;

	public DomainPropertiesObject(String propertiesString) {
		super(propertiesString);
		Properties properties = getProperties();
		String temp = (String) properties.get(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST.toString());
		takeHighestVersion = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.REVOKE_IS_PERMANENT.toString());
		permanentRevoke = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS.toString());
		noMandatoryScans = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SCANS_SIZE_LIMIT.toString());
		int tempInt = 10485760;
		try {
			tempInt = StringUtils.isEmpty(temp) ? 10485760 : Integer.parseInt(temp);
		} catch (NumberFormatException e) {
			logger.warn("Cannot parse scansSizeLimit " + temp + ". Using default value 10485760 instead.");
			tempInt = 10485760;
		}
		scansSizeLimit = tempInt;
	}

	public boolean isTakeHighestVersion() {
		return takeHighestVersion;
	}

	public boolean isPermanentRevoke() {
		return permanentRevoke;
	}

	public boolean isNoMandatoryScans() {
		return noMandatoryScans;
	}

	public int getScansSizeLimit() {
		return scansSizeLimit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (noMandatoryScans ? 1231 : 1237);
		result = prime * result + (permanentRevoke ? 1231 : 1237);
		result = prime * result + scansSizeLimit;
		result = prime * result + (takeHighestVersion ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainPropertiesObject other = (DomainPropertiesObject) obj;
		if (noMandatoryScans != other.noMandatoryScans)
			return false;
		if (permanentRevoke != other.permanentRevoke)
			return false;
		if (scansSizeLimit != other.scansSizeLimit)
			return false;
		if (takeHighestVersion != other.takeHighestVersion)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("domain properties: ");
		sb.append(DomainProperties.REVOKE_IS_PERMANENT.toString());
		sb.append(" = ");
		sb.append(permanentRevoke);
		sb.append("; ");
		sb.append(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST.toString());
		sb.append(" = ");
		sb.append(takeHighestVersion);
		sb.append("; ");
		sb.append(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS.toString());
		sb.append(" = ");
		sb.append(noMandatoryScans);
		sb.append("; ");
		sb.append(DomainProperties.SCANS_SIZE_LIMIT.toString());
		sb.append(" = ");
		sb.append(scansSizeLimit);
		return sb.toString();
	}
}
