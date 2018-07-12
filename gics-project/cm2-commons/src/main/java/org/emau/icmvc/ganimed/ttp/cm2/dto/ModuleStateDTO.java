package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;

/**
 * module state - consent state of module and list of affected policies
 * 
 * @author geidell
 * 
 */
public class ModuleStateDTO implements Serializable {

	private static final long serialVersionUID = -104434893231372840L;
	private ModuleKeyDTO key;
	private ConsentStatus consentState;
	private List<PolicyKeyDTO> policyKeys = new ArrayList<PolicyKeyDTO>();

	public ModuleStateDTO() {
	}

	public ModuleStateDTO(ModuleKeyDTO key, ConsentStatus consentState, List<PolicyKeyDTO> policyKeys) {
		super();
		this.key = key;
		this.consentState = consentState;
		this.policyKeys = policyKeys;
	}

	public ModuleKeyDTO getKey() {
		return key;
	}

	public void setKey(ModuleKeyDTO key) {
		this.key = key;
	}

	public ConsentStatus getConsentState() {
		return consentState;
	}

	public void setConsentState(ConsentStatus consentState) {
		this.consentState = consentState;
	}

	public List<PolicyKeyDTO> getPolicyKeys() {
		return policyKeys;
	}

	public void setPolicyKeys(List<PolicyKeyDTO> policyKeys) {
		this.policyKeys = policyKeys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		ModuleStateDTO other = (ModuleStateDTO) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ModuleStateDTO for ");
		sb.append(key);
		sb.append("' with consent state '");
		sb.append(consentState);
		sb.append("'");
		return sb.toString();
	}
}
