package org.emau.icmvc.ganimed.ttp.cm2.model;

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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

/**
 * eine policy ist die kleinstmoegliche unterteilung eines consents; sie repraesentiert eine atomare, zustimmbare einheit
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "policy")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class Policy implements Serializable {

	private static final long serialVersionUID = -7411027119873993119L;
	@EmbeddedId
	private PolicyKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	// keine explizite relation zu signed policies! die wuerden sonst bei fast jedem zugriff auf das template geladen (muessten mit in equals und hashcode rein)
	@ManyToMany(mappedBy = "policies", fetch = FetchType.LAZY)
	private List<Module> modules = new ArrayList<Module>();

	public Policy() {
	}

	public Policy(Domain domain, PolicyDTO dto) throws VersionConverterClassException, InvalidVersionException {
		super();
		this.key = new PolicyKey(domain.getPolicyVersionConverterInstance(), dto.getKey());
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.domain = domain;
	}

	public PolicyKey getKey() {
		return key;
	}

	public Domain getDomain() {
		return domain;
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

	public List<Module> getModules() {
		return modules;
	}

	public PolicyDTO toDTO() throws VersionConverterClassException, InvalidVersionException {
		PolicyKeyDTO dtoKey = key.toDTO(domain.getPolicyVersionConverterInstance());
		PolicyDTO result = new PolicyDTO(dtoKey);
		result.setComment(comment);
		result.setExternProperties(externProperties);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
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
		Policy other = (Policy) obj;
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
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(key.toString());
		sb.append(" with comment = '");
		sb.append(comment);
		sb.append("' and extern properties '");
		sb.append(externProperties);
		sb.append("'");
		return sb.toString();
	}
}
