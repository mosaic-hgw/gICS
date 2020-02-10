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

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)<br>
 * das light dto enthaelt keine scans und keine freetextvals um sowohl datenmenge als auch db-zugriffe zu minimieren<br>
 * 
 * @author geidell
 * 
 */
public class ConsentLightDTO implements Serializable {

	private static final long serialVersionUID = -4082209239245270664L;
	private ConsentKeyDTO key;
	private boolean patientSignatureIsFromGuardian;
	private String physicanId;
	private Date patientSigningDate;
	private Date physicanSigningDate;
	private String comment;
	private String externProperties;
	private String scanFileType;
	private ConsentTemplateType templateType;
	private Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<ModuleKeyDTO, ModuleStateDTO>();

	public ConsentLightDTO() {
	}

	public ConsentLightDTO(ConsentKeyDTO key) {
		super();
		this.key = key;
	}

	public ConsentLightDTO(ConsentLightDTO lightDTO) {
		this.key = lightDTO.getKey();
		setComment(lightDTO.getComment());
		setExternProperties(lightDTO.getExternProperties());
		Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<ModuleKeyDTO, ModuleStateDTO>(lightDTO.getModuleStates());
		setModuleStates(moduleStates);
		setPatientSignatureIsFromGuardian(lightDTO.getPatientSignatureIsFromGuardian());
		setPatientSigningDate(lightDTO.getPatientSigningDate());
		setPhysicanId(lightDTO.getPhysicanId());
		setPhysicanSigningDate(lightDTO.getPhysicanSigningDate());
		setScanFileType(lightDTO.getScanFileType());
	}

	public ConsentKeyDTO getKey() {
		return key;
	}

	public void setKey(ConsentKeyDTO key) {
		if (key != null) {
			this.key = key;
		}
	}

	public boolean getPatientSignatureIsFromGuardian() {
		return patientSignatureIsFromGuardian;
	}

	public void setPatientSignatureIsFromGuardian(boolean patientSignatureIsFromGuardian) {
		this.patientSignatureIsFromGuardian = patientSignatureIsFromGuardian;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExternProperties() {
		return externProperties;
	}

	public void setExternProperties(String externProperties) {
		this.externProperties = externProperties;
	}

	public String getScanFileType() {
		return scanFileType;
	}

	public void setScanFileType(String scanFileType) {
		this.scanFileType = scanFileType;
	}

	public ConsentTemplateType getTemplateType() {
		return templateType;
	}

	public void setTemplateType(ConsentTemplateType templateType) {
		this.templateType = templateType;
	}

	public Map<ModuleKeyDTO, ModuleStateDTO> getModuleStates() {
		return moduleStates;
	}

	public void setModuleStates(Map<ModuleKeyDTO, ModuleStateDTO> moduleStates) {
		if (moduleStates != null) {
			this.moduleStates = moduleStates;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((patientSignatureIsFromGuardian) ? 1231 : 1237);
		result = prime * result + ((patientSigningDate == null) ? 0 : patientSigningDate.hashCode());
		result = prime * result + ((physicanId == null) ? 0 : physicanId.hashCode());
		result = prime * result + ((physicanSigningDate == null) ? 0 : physicanSigningDate.hashCode());
		result = prime * result + ((moduleStates == null) ? 0 : moduleStates.hashCode());
		result = prime * result + ((scanFileType == null) ? 0 : scanFileType.hashCode());
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
		ConsentLightDTO other = (ConsentLightDTO) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (externProperties == null) {
			if (other.externProperties != null)
				return false;
		} else if (!externProperties.equals(other.externProperties))
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
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("', ");
		sb.append(moduleStates.size());
		sb.append(" module states");
		return sb.toString();
	}
}
