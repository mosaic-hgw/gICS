package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * zusammengesetzter primaerschluessel fuer ein consent template
 * 
 * @author geidell
 * 
 */
@Embeddable
public class ConsentTemplateKey implements Serializable {

	private static final long serialVersionUID = 6021029307015254315L;
	@Column(insertable = false, updatable = false)
	private String domainName;
	@Column(length = 100)
	private String name;
	private int version;

	public ConsentTemplateKey() {
	}

	public ConsentTemplateKey(String domainName, String name, int version) {
		this.domainName = domainName;
		this.name = name;
		this.version = version;
	}

	public ConsentTemplateKey(VersionConverter versionConverter, ConsentTemplateKeyDTO dto) throws InvalidVersionException {
		this.domainName = dto.getDomainName();
		this.name = dto.getName();
		this.version = versionConverter.stringToInt(dto.getVersion());
	}

	public String getDomainName() {
		return domainName;
	}

	public String getName() {
		return name;
	}

	public int getVersion() {
		return version;
	}

	public ConsentTemplateKeyDTO toDTO(VersionConverter versionConverter) throws InvalidVersionException {
		return new ConsentTemplateKeyDTO(domainName, name, versionConverter.intToString(version));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + version;
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
		ConsentTemplateKey other = (ConsentTemplateKey) obj;
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("consent template '");
		sb.append(name);
		sb.append("' in version ");
		sb.append(version);
		sb.append(" for domain '");
		sb.append(domainName);
		sb.append("'");
		return sb.toString();
	}
}
