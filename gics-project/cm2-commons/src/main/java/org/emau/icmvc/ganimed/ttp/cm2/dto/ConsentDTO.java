package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)
 * 
 * @author geidell
 * 
 */
public class ConsentDTO implements Serializable {

	private static final long serialVersionUID = 7940346384330664520L;
	private ConsentKeyDTO key;
	private String patientSignatureBase64;
	private boolean patientSignatureIsFromGuardian;
	private String physicanSignatureBase64;
	private String physicanId;
	private Date patientSigningDate;
	private Date physicanSigningDate;
	private String scanBase64;
	private String scanFileType;
	private Map<ModuleKeyDTO, ConsentStatus> moduleStates = new HashMap<ModuleKeyDTO, ConsentStatus>();
	private Map<String, String> freeTextVals = new HashMap<String, String>();

	public ConsentDTO() {
	}

	public ConsentDTO(ConsentKeyDTO key) {
		super();
		this.key = key;
	}

	public ConsentKeyDTO getKey() {
		return key;
	}

	public void setKey(ConsentKeyDTO key) {
		if (key != null) {
			this.key = key;
		}
	}

	public String getPatientSignatureBase64() {
		return patientSignatureBase64;
	}

	public void setPatientSignatureBase64(String patientSignatureBase64) {
		this.patientSignatureBase64 = patientSignatureBase64;
	}

	public boolean getPatientSignatureIsFromGuardian() {
		return patientSignatureIsFromGuardian;
	}

	public void setPatientSignatureIsFromGuardian(boolean patientSignatureIsFromGuardian) {
		this.patientSignatureIsFromGuardian = patientSignatureIsFromGuardian;
	}

	public String getPhysicanSignatureBase64() {
		return physicanSignatureBase64;
	}

	public void setPhysicanSignatureBase64(String physicanSignatureBase64) {
		this.physicanSignatureBase64 = physicanSignatureBase64;
	}

	public String getPhysicanId() {
		return physicanId;
	}

	public void setPhysicanId(String physicanId) {
		this.physicanId = physicanId;
	}

	public Date getPatientSigningDate() {
		return patientSigningDate;
	}

	public void setPatientSigningDate(Date patientSigningDate) {
		this.patientSigningDate = patientSigningDate;
	}

	public Date getPhysicanSigningDate() {
		return physicanSigningDate;
	}

	public void setPhysicanSigningDate(Date physicanSigningDate) {
		this.physicanSigningDate = physicanSigningDate;
	}

	public String getScanBase64() {
		return scanBase64;
	}

	public void setScanBase64(String scanBase64) {
		this.scanBase64 = scanBase64;
	}

	public String getScanFileType() {
		return scanFileType;
	}

	public void setScanFileType(String scanFileType) {
		this.scanFileType = scanFileType;
	}

	public Map<ModuleKeyDTO, ConsentStatus> getModuleStates() {
		return moduleStates;
	}

	public void setModuleStates(Map<ModuleKeyDTO, ConsentStatus> moduleStates) {
		if (moduleStates != null) {
			this.moduleStates = moduleStates;
		}
	}

	public Map<String, String> getFreeTextVals() {
		return freeTextVals;
	}

	public void setFreeTextVals(Map<String, String> freeTextVals) {
		if (freeTextVals != null) {
			this.freeTextVals = freeTextVals;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((freeTextVals == null) ? 0 : freeTextVals.hashCode());
		result = prime * result + ((patientSignatureBase64 == null) ? 0 : patientSignatureBase64.hashCode());
		result = prime * result + ((patientSignatureIsFromGuardian) ? 1231 : 1237);
		result = prime * result + ((patientSigningDate == null) ? 0 : patientSigningDate.hashCode());
		result = prime * result + ((physicanId == null) ? 0 : physicanId.hashCode());
		result = prime * result + ((physicanSignatureBase64 == null) ? 0 : physicanSignatureBase64.hashCode());
		result = prime * result + ((physicanSigningDate == null) ? 0 : physicanSigningDate.hashCode());
		result = prime * result + ((moduleStates == null) ? 0 : moduleStates.hashCode());
		result = prime * result + ((scanFileType == null) ? 0 : scanFileType.hashCode());
		result = prime * result + ((scanBase64 == null) ? 0 : scanBase64.hashCode());
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
		ConsentDTO other = (ConsentDTO) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (freeTextVals == null) {
			if (other.freeTextVals != null)
				return false;
		} else if (!freeTextVals.equals(other.freeTextVals))
			return false;
		if (patientSignatureBase64 == null) {
			if (other.patientSignatureBase64 != null)
				return false;
		} else if (!patientSignatureBase64.equals(other.patientSignatureBase64))
			return false;
		if(patientSignatureIsFromGuardian != other.patientSignatureIsFromGuardian)
			return false;
		if (patientSigningDate == null) {
			if (other.patientSigningDate != null)
				return false;
		} else if (!patientSigningDate.equals(other.patientSigningDate))
			return false;
		if (physicanId == null) {
			if (other.physicanId != null)
				return false;
		} else if (!physicanId.equals(other.physicanId))
			return false;
		if (physicanSignatureBase64 == null) {
			if (other.physicanSignatureBase64 != null)
				return false;
		} else if (!physicanSignatureBase64.equals(other.physicanSignatureBase64))
			return false;
		if (physicanSigningDate == null) {
			if (other.physicanSigningDate != null)
				return false;
		} else if (!physicanSigningDate.equals(other.physicanSigningDate))
			return false;
		if (moduleStates == null) {
			if (other.moduleStates != null)
				return false;
		} else if (!moduleStates.equals(other.moduleStates))
			return false;
		if (scanFileType == null) {
			if (other.scanFileType != null)
				return false;
		} else if (!scanFileType.equals(other.scanFileType))
			return false;
		if (scanBase64 == null) {
			if (other.scanBase64 != null)
				return false;
		} else if (!scanBase64.equals(other.scanBase64))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with ");
		sb.append(moduleStates.size());
		sb.append(" module states and ");
		sb.append(freeTextVals.size());
		sb.append(" free text values");
		return sb.toString();
	}
}
