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
import java.util.Arrays;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;

/**
 * name - identifier<br/>
 * label - label for the web-interface<br>
 * properties - semicolon separated key-value pairs; for valid keys see enum {@link DomainProperties}<br/>
 * comment - comment<br/>
 * 
 * @author geidell
 * 
 */
public class DomainDTO implements Serializable {

	private static final long serialVersionUID = 9152603229919432413L;
	private String name;
	private String label;
	private String ctVersionConverter;
	private String moduleVersionConverter;
	private String policyVersionConverter;
	private String properties;
	private String comment;
	private String externProperties;
	private String logo;
	private List<String> signerIdTypes;

	public DomainDTO() {
	}

	public DomainDTO(String name, String ctVersionConverter, String moduleVersionConverter, String policyVersionConverter,
			List<String> signerIdTypes) {
		super();
		this.name = name;
		this.ctVersionConverter = ctVersionConverter;
		this.moduleVersionConverter = moduleVersionConverter;
		this.policyVersionConverter = policyVersionConverter;
		if (signerIdTypes != null) {
			this.signerIdTypes = signerIdTypes;
		}
	}

	public DomainDTO(String name, String label, String ctVersionConverter, String moduleVersionConverter, String policyVersionConverter,
			String properties, String comment, String externProperties, String logo, List<String> signerIdTypes) {
		super();
		this.name = name;
		this.label = label;
		this.ctVersionConverter = ctVersionConverter;
		this.moduleVersionConverter = moduleVersionConverter;
		this.policyVersionConverter = policyVersionConverter;
		this.properties = properties;
		this.comment = comment;
		this.externProperties = externProperties;
		this.logo = logo;
		if (signerIdTypes != null) {
			this.signerIdTypes = signerIdTypes;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCtVersionConverter() {
		return ctVersionConverter;
	}

	public void setCtVersionConverter(String ctVersionConverter) {
		this.ctVersionConverter = ctVersionConverter;
	}

	public String getModuleVersionConverter() {
		return moduleVersionConverter;
	}

	public void setModuleVersionConverter(String moduleVersionConverter) {
		this.moduleVersionConverter = moduleVersionConverter;
	}

	public String getPolicyVersionConverter() {
		return policyVersionConverter;
	}

	public void setPolicyVersionConverter(String policyVersionConverter) {
		this.policyVersionConverter = policyVersionConverter;
	}

	public String getProperty(String key) {
		// check if key is valid
		boolean found = false;
		for (DomainProperties prop : Arrays.asList(DomainProperties.values())) {
			if (key.equals(prop.toString())) {
				found = true;
				break;
			}
		}

		// check if key-value-pair is found in config
		if (found) {
			for (String pair : properties.split(";")) {
				if (pair.contains(key)) {
					return pair.split("=")[1].trim();
				}
			}
		}
		return null;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
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

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public List<String> getSignerIdTypes() {
		return signerIdTypes;
	}

	public void setSignerIdTypes(List<String> signerIdTypes) {
		if (signerIdTypes != null) {
			this.signerIdTypes = signerIdTypes;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((ctVersionConverter == null) ? 0 : ctVersionConverter.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((logo == null) ? 0 : logo.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((moduleVersionConverter == null) ? 0 : moduleVersionConverter.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((policyVersionConverter == null) ? 0 : policyVersionConverter.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((signerIdTypes == null) ? 0 : signerIdTypes.hashCode());
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
		DomainDTO other = (DomainDTO) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (ctVersionConverter == null) {
			if (other.ctVersionConverter != null)
				return false;
		} else if (!ctVersionConverter.equals(other.ctVersionConverter))
			return false;
		if (externProperties == null) {
			if (other.externProperties != null)
				return false;
		} else if (!externProperties.equals(other.externProperties))
			return false;
		if (logo == null) {
			if (other.logo != null)
				return false;
		} else if (!logo.equals(other.logo))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (moduleVersionConverter == null) {
			if (other.moduleVersionConverter != null)
				return false;
		} else if (!moduleVersionConverter.equals(other.moduleVersionConverter))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (policyVersionConverter == null) {
			if (other.policyVersionConverter != null)
				return false;
		} else if (!policyVersionConverter.equals(other.policyVersionConverter))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (signerIdTypes == null) {
			if (other.signerIdTypes != null)
				return false;
		} else if (!signerIdTypes.equals(other.signerIdTypes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("domain with name '");
		sb.append(name);
		sb.append("', label '");
		sb.append(label);
		sb.append("', ct version converter '");
		sb.append(ctVersionConverter);
		sb.append("', module version converter '");
		sb.append(moduleVersionConverter);
		sb.append("', policy version converter '");
		sb.append(policyVersionConverter);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("', the following properties: '");
		sb.append(properties);
		sb.append((logo != null && !logo.isEmpty()) ? ", a logo" : ", no logo");
		sb.append("' and signerIdTypes:");
		for (String signerIdType : signerIdTypes) {
			sb.append(" '");
			sb.append(signerIdType);
			sb.append("'");
		}
		return sb.toString();
	}
}
