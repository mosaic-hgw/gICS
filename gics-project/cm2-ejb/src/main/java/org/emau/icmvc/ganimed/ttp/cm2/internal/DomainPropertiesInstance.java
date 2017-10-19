package org.emau.icmvc.ganimed.ttp.cm2.internal;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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

import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;

public class DomainPropertiesInstance implements Serializable {

	private static final long serialVersionUID = -8665466558820461537L;
	private final boolean takeHighestVersion;
	private final boolean permanentRevoke;
	private final boolean noMandatoryScans;
	
	public DomainPropertiesInstance(Properties properties) {
		String temp = (String) properties.get(DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST.toString());
		takeHighestVersion = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.REVOKE_IS_PERMANENT.toString());
		permanentRevoke = Boolean.TRUE.toString().equalsIgnoreCase(temp);
		temp = (String) properties.get(DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS.toString());
		noMandatoryScans = Boolean.TRUE.toString().equalsIgnoreCase(temp);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (noMandatoryScans ? 1231 : 1237);
		result = prime * result + (permanentRevoke ? 1231 : 1237);
		result = prime * result + (takeHighestVersion ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainPropertiesInstance other = (DomainPropertiesInstance) obj;
		if (noMandatoryScans != other.noMandatoryScans)
			return false;
		if (permanentRevoke != other.permanentRevoke)
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
		return sb.toString();
	}
}
