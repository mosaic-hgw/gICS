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

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;

/**
 * ein consent template kann mehrere module (mit jeweils mehreren policies) enthalten, es entspricht dem elektronischen aequivalent eines nicht ausgefuellten konsentdokumentes
 * 
 * @author geidell
 * 
 */
public class ConsentTemplateDTO implements Serializable {

	private static final long serialVersionUID = -875195702630175964L;
	private ConsentTemplateKeyDTO key;
	private String title;
	private String propertiesString;
	private String comment;
	private String externProperties;
	private ConsentTemplateType type;
	private String header;
	private String footer;
	private String scanBase64;
	private String scanFileType;
	private List<AssignedModuleDTO> assignedModules = new ArrayList<AssignedModuleDTO>();
	private List<FreeTextDefDTO> freeTextDefs = new ArrayList<FreeTextDefDTO>();
	private ConsentTemplateStructureDTO structure = new ConsentTemplateStructureDTO();

	public ConsentTemplateDTO() {
	}

	public ConsentTemplateDTO(ConsentTemplateKeyDTO key) {
		super();
		this.key = key;
	}

	public ConsentTemplateDTO(ConsentTemplateKeyDTO key, String title, String propertiesString, String comment, String externProperties,
			ConsentTemplateType type, String header, String footer, String scanBase64, String scanFileType, List<AssignedModuleDTO> assignedModules,
			List<FreeTextDefDTO> freeTextDefs) {
		super();
		this.key = key;
		this.title = title;
		this.propertiesString = propertiesString;
		this.comment = comment;
		this.externProperties = externProperties;
		this.type = type;
		this.header = header;
		this.footer = footer;
		this.scanBase64 = scanBase64;
		this.scanFileType = scanFileType;
		this.assignedModules = assignedModules;
		this.freeTextDefs = freeTextDefs;
	}

	public ConsentTemplateKeyDTO getKey() {
		return key;
	}

	public void setKey(ConsentTemplateKeyDTO key) {
		if (key != null) {
			this.key = key;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPropertiesString() {
		return propertiesString;
	}

	public void setPropertiesString(String propertiesString) {
		this.propertiesString = propertiesString;
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

	public ConsentTemplateType getType() {
		return type;
	}

	public void setType(ConsentTemplateType type) {
		this.type = type;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
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

	public List<AssignedModuleDTO> getAssignedModules() {
		return assignedModules;
	}

	public void setAssignedModules(List<AssignedModuleDTO> assignedModules) {
		if (assignedModules != null) {
			this.assignedModules = assignedModules;
		}
	}

	public List<FreeTextDefDTO> getFreeTextDefs() {
		return freeTextDefs;
	}

	public void setFreeTextDefs(List<FreeTextDefDTO> freeTextDefs) {
		if (freeTextDefs != null) {
			this.freeTextDefs = freeTextDefs;
		}
	}

	public ConsentTemplateStructureDTO getStructure() {
		return structure;
	}

	public void setStructure(ConsentTemplateStructureDTO structure) {
		if (structure != null) {
			this.structure = structure;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((assignedModules == null) ? 0 : assignedModules.hashCode());
		result = prime * result + ((propertiesString == null) ? 0 : propertiesString.hashCode());
		result = prime * result + ((scanFileType == null) ? 0 : scanFileType.hashCode());
		result = prime * result + ((scanBase64 == null) ? 0 : scanBase64.hashCode());
		result = prime * result + ((freeTextDefs == null) ? 0 : freeTextDefs.hashCode());
		result = prime * result + ((structure == null) ? 0 : structure.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		ConsentTemplateDTO other = (ConsentTemplateDTO) obj;
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
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (footer == null) {
			if (other.footer != null)
				return false;
		} else if (!footer.equals(other.footer))
			return false;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (assignedModules == null) {
			if (other.assignedModules != null)
				return false;
		} else if (!assignedModules.equals(other.assignedModules))
			return false;
		if (propertiesString == null) {
			if (other.propertiesString != null)
				return false;
		} else if (!propertiesString.equals(other.propertiesString))
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
		if (freeTextDefs == null) {
			if (other.freeTextDefs != null)
				return false;
		} else if (!freeTextDefs.equals(other.freeTextDefs))
			return false;
		if (structure == null) {
			if (other.structure != null)
				return false;
		} else if (!structure.equals(other.structure))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with title '");
		sb.append(title);
		sb.append(", comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("', type '");
		sb.append(type);
		sb.append("', properties '");
		sb.append(propertiesString);
		sb.append("', ");
		sb.append(assignedModules.size());
		sb.append(" modules and ");
		sb.append(freeTextDefs.size());
		sb.append(" free text fields");
		return sb.toString();
	}
}
