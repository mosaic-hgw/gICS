package org.emau.icmvc.ganimed.ttp.cm2.config;

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

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;

/**
 * configuration for the check whether the given policy is consented by the given person
 * <p>
 * <b>idMatchingType</b><br>
 * match at least one, at least all or exact all of the given signer identifiers<br>
 * default = AT_LEAST_ONE
 * <p>
 * <b>ignoreVersionNumber</b><br>
 * ignore the version number of the policy<br>
 * default = false
 * <p>
 * <b>unknownStateIsConsideredAsDecline</b><br>
 * if the consent state type "unknown" is encountered, it's considered as "declined" for the "isConsented...()"-functions; see {@link ConsentStatusType}<br>
 * default = false
 * <p>
 * 
 * @author geidell
 * 
 */
public class CheckConsentConfig implements Serializable {

	private static final long serialVersionUID = -6636704676431609622L;
	/**
	 * match at least one, at least all or exact all of the given signer identifiers<br>
	 * default = AT_LEAST_ONE
	 */
	private IdMatching idMatchingType = IdMatching.AT_LEAST_ONE;
	/**
	 * ignore the version number of the policy<br>
	 * default = false
	 */
	private boolean ignoreVersionNumber = false;
	/**
	 * if the consent state type "unknown" is encountered, it's considered as "declined" for the "isConsented...()"-functions; see {@link ConsentStatusType}<br>
	 * default = false
	 */
	private boolean unknownStateIsConsideredAsDecline = false;

	public CheckConsentConfig() {
	}

	public IdMatching getIdMatchingType() {
		return idMatchingType;
	}

	public void setIdMatchingType(IdMatching idMatchingType) {
		this.idMatchingType = idMatchingType;
	}

	public boolean getIgnoreVersionNumber() {
		return ignoreVersionNumber;
	}

	public void setIgnoreVersionNumber(boolean ignoreVersionNumber) {
		this.ignoreVersionNumber = ignoreVersionNumber;
	}

	public boolean getUnknownStateIsConsideredAsDecline() {
		return unknownStateIsConsideredAsDecline;
	}

	public void setUnknownStateIsConsideredAsDecline(boolean unknownStateIsConsideredAsDecline) {
		this.unknownStateIsConsideredAsDecline = unknownStateIsConsideredAsDecline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idMatchingType == null) ? 0 : idMatchingType.hashCode());
		result = prime * result + (ignoreVersionNumber ? 1231 : 1237);
		result = prime * result + (unknownStateIsConsideredAsDecline ? 1231 : 1237);
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
		CheckConsentConfig other = (CheckConsentConfig) obj;
		if (idMatchingType != other.idMatchingType)
			return false;
		if (ignoreVersionNumber != other.ignoreVersionNumber)
			return false;
		if (unknownStateIsConsideredAsDecline != other.unknownStateIsConsideredAsDecline)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("check consent config with the following settings:\n");
		sb.append("signer id matching type: ");
		sb.append(idMatchingType);
		sb.append("\nignore version number: ");
		sb.append(ignoreVersionNumber);
		sb.append("\nunknown state is considered as decline: ");
		sb.append(unknownStateIsConsideredAsDecline);
		return sb.toString();
	}

	public enum IdMatching {
		AT_LEAST_ONE, AT_LEAST_ALL, EXACT;
	}
}
