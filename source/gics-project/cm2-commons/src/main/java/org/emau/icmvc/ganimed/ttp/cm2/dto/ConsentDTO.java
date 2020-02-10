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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)
 * 
 * @author geidell
 * 
 */
public class ConsentDTO extends ConsentLightDTO implements Serializable {

	private static final long serialVersionUID = -4094005853140726190L;
	private String patientSignatureBase64;
	private String physicanSignatureBase64;
	private String scanBase64;
	private Map<String, String> freeTextVals = new HashMap<String, String>();

	public ConsentDTO() {
		super();
	}

	public ConsentDTO(ConsentKeyDTO key) {
		super(key);
	}

	public ConsentDTO(ConsentLightDTO lightDTO) {
		super(lightDTO);
	}

	public ConsentDTO(ConsentDTO dto, Date date) {
		super(new ConsentKeyDTO(dto.getKey().getConsentTemplateKey(), dto.getKey().getSignerIds(), date));
		super.setComment(dto.getComment());
		super.setExternProperties(dto.getExternProperties());
		Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<ModuleKeyDTO, ModuleStateDTO>(dto.getModuleStates());
		super.setModuleStates(moduleStates);
		super.setPatientSignatureIsFromGuardian(dto.getPatientSignatureIsFromGuardian());
		super.setPatientSigningDate(dto.getPatientSigningDate());
		super.setPhysicanId(dto.getPhysicanId());
		super.setPhysicanSigningDate(dto.getPhysicanSigningDate());
		super.setScanFileType(dto.getScanFileType());
		setPatientSignatureBase64(dto.getPatientSignatureBase64());
		setPhysicanSignatureBase64(dto.getPhysicanSignatureBase64());
		setScanBase64(dto.getScanBase64());
		Map<String, String> freeTextVals = new HashMap<String, String>(dto.getFreeTextVals());
		setFreeTextVals(freeTextVals);
	}

	public String getPatientSignatureBase64() {
		return patientSignatureBase64;
	}

	public void setPatientSignatureBase64(String patientSignatureBase64) {
		this.patientSignatureBase64 = patientSignatureBase64;
	}

	public String getPhysicanSignatureBase64() {
		return physicanSignatureBase64;
	}

	public void setPhysicanSignatureBase64(String physicanSignatureBase64) {
		this.physicanSignatureBase64 = physicanSignatureBase64;
	}

	public String getScanBase64() {
		return scanBase64;
	}

	public void setScanBase64(String scanBase64) {
		this.scanBase64 = scanBase64;
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
		int result = super.hashCode();
		result = prime * result + ((freeTextVals == null) ? 0 : freeTextVals.hashCode());
		result = prime * result + ((patientSignatureBase64 == null) ? 0 : patientSignatureBase64.hashCode());
		result = prime * result + ((physicanSignatureBase64 == null) ? 0 : physicanSignatureBase64.hashCode());
		result = prime * result + ((scanBase64 == null) ? 0 : scanBase64.hashCode());
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
		ConsentDTO other = (ConsentDTO) obj;
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
		if (physicanSignatureBase64 == null) {
			if (other.physicanSignatureBase64 != null)
				return false;
		} else if (!physicanSignatureBase64.equals(other.physicanSignatureBase64))
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
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" and ");
		sb.append(freeTextVals.size());
		sb.append(" free text values");
		return sb.toString();
	}
}
