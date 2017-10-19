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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

/**
 * ein modul ist eine zustimmbare unterteilung eines consents; sie fasst mehrere policies zusammen, denen gewoehnlicherweise gemeinsam zugestimmt wird
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "module")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class Module implements Serializable {

	private static final long serialVersionUID = 9161530859134353933L;
	@EmbeddedId
	private ModuleKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "TEXT", referencedColumnName = "ID")
	private Text text;
	private String title;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModuleConsentTemplate> moduleConsentTemplates = new ArrayList<ModuleConsentTemplate>();
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "MODULE_POLICY", joinColumns = { @JoinColumn(name = "M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "M_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "M_VERSION", referencedColumnName = "VERSION") }, inverseJoinColumns = {
			@JoinColumn(name = "P_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "P_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "P_VERSION", referencedColumnName = "VERSION") })
	private List<Policy> policies = new ArrayList<Policy>();

	public Module() {
	}

	public Module(Domain domain, String name, int version, String text, String title, String comment, String externProperties) {
		super();
		this.key = new ModuleKey(domain.getName(), name, version);
		this.text = new Text(key, TextType.MODUL, text);
		this.title = title;
		this.comment = comment;
		this.externProperties = externProperties;
		this.domain = domain;
	}

	public Module(Domain domain, ModuleDTO dto) throws VersionConverterClassException, InvalidVersionException {
		super();
		this.key = new ModuleKey(domain.getModuleVersionConverterInstance(), dto.getKey());
		this.text = new Text(key, TextType.MODUL, dto.getText());
		this.title = dto.getTitle();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.domain = domain;
	}

	public ModuleKey getKey() {
		return key;
	}

	public Domain getDomain() {
		return domain;
	}

	public Text getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public List<Policy> getPolicies() {
		return policies;
	}

	public List<ModuleConsentTemplate> getModuleConsentTemplates() {
		return moduleConsentTemplates;
	}

	public ModuleDTO toDTO() throws VersionConverterClassException, InvalidVersionException {
		ModuleKeyDTO dtoKey = key.toDTO(domain.getModuleVersionConverterInstance());
		List<PolicyDTO> policyDTOs = new ArrayList<PolicyDTO>();
		for (Policy policy : policies) {
			policyDTOs.add(policy.toDTO());
		}
		ModuleDTO result = new ModuleDTO(dtoKey, text.getText(), title, comment, externProperties, policyDTOs);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		Module other = (Module) obj;
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
		if (policies == null) {
			if (other.policies != null)
				return false;
		} else if (!policies.equals(other.policies))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
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
		StringBuilder sb = new StringBuilder(key.toString());
		sb.append(" with title '");
		sb.append(title);
		sb.append(", comment '");
		sb.append(comment);
		sb.append(", extern properties '");
		sb.append(externProperties);
		sb.append("' and ");
		sb.append(policies.size());
		sb.append(" policies");
		return sb.toString();
	}
}
