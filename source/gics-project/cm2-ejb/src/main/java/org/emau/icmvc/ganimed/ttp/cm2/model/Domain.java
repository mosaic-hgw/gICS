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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DomainPropertiesObject;

/**
 * zur gruppierung
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "domain")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class Domain implements Serializable {

	private static final long serialVersionUID = -7017427406683339204L;
	@Id
	@Column(length = 50)
	private String name;
	private String label;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	@BatchFetch(BatchFetchType.IN)
	private List<Policy> policies = new ArrayList<Policy>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	@BatchFetch(BatchFetchType.IN)
	private List<Module> modules = new ArrayList<Module>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	// @BatchFetch(BatchFetchType.IN) auskommentiert wegen bug in eclipselink - die am ct haengenden text-entities werden nicht richtig mitgeladen
	// (javax.resource.ResourceException: IJ000460: Error checking for a transaction)
	private List<ConsentTemplate> consentTemplates = new ArrayList<ConsentTemplate>();
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgt die funktion "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private DomainPropertiesObject propertiesObject = null;
	@Column(name = "PROPERTIES")
	private String propertiesString;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	@Column(name = "CT_VERSION_CONVERTER")
	private String ctVersionConverter;
	@Column(name = "MODULE_VERSION_CONVERTER")
	private String moduleVersionConverter;
	@Column(name = "POLICY_VERSION_CONVERTER")
	private String policyVersionConverter;
	@Lob
	private String logo;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<SignerIdType> signerIdTypes = new ArrayList<SignerIdType>();

	public Domain() {
	}

	public Domain(DomainDTO dto) throws VersionConverterClassException {
		super();
		this.name = dto.getName();
		this.label = dto.getLabel();
		this.ctVersionConverter = dto.getCtVersionConverter();
		this.moduleVersionConverter = dto.getModuleVersionConverter();
		this.policyVersionConverter = dto.getPolicyVersionConverter();
		this.propertiesString = dto.getProperties();
		loadPropertiesFromString();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.logo = dto.getLogo();
		for (String signerIdType : dto.getSignerIdTypes()) {
			signerIdTypes.add(new SignerIdType(this, signerIdType));
		}
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString() {
		propertiesObject = new DomainPropertiesObject(propertiesString);
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public DomainPropertiesObject getPropertiesObject() {
		if (propertiesObject == null) {
			loadPropertiesFromString();
		}
		return propertiesObject;
	}

	public String getPropertiesString() {
		return propertiesString;
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

	public List<Module> getModules() {
		return modules;
	}

	public List<ConsentTemplate> getConsentTemplates() {
		return consentTemplates;
	}

	public DomainDTO toDTO() {
		List<String> sIdTypes = new ArrayList<String>();
		for (SignerIdType signerIdType : signerIdTypes) {
			sIdTypes.add(signerIdType.getKey().getName());
		}
		DomainDTO result = new DomainDTO(name, label, ctVersionConverter, moduleVersionConverter, policyVersionConverter, propertiesObject.toString(),
				comment, externProperties, logo, sIdTypes);
		return result;
	}

	public String getCTVersionConverter() {
		return ctVersionConverter;
	}

	public String getModuleVersionConverter() {
		return moduleVersionConverter;
	}

	public String getPolicyVersionConverter() {
		return policyVersionConverter;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public List<SignerIdType> getSignerIdTypes() {
		return signerIdTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Domain other = (Domain) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "domain '" + name + "'";
	}

	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append("domain '");
		sb.append(name);
		sb.append("', comment = '");
		sb.append(comment);
		sb.append("', extern properties = '");
		sb.append(externProperties);
		sb.append("', properties: '");
		sb.append(propertiesString);
		sb.append("', ct version converter '");
		sb.append(ctVersionConverter);
		sb.append("', module version converter '");
		sb.append(moduleVersionConverter);
		sb.append("', policy version converter '");
		sb.append(policyVersionConverter);
		sb.append("' has ");
		sb.append(policies.size());
		sb.append(" policies, ");
		sb.append(modules.size());
		sb.append(" modules, ");
		sb.append(consentTemplates.size());
		sb.append(" consent templates");
		sb.append((logo != null && !logo.isEmpty()) ? ", a logo and " : ", no logo and ");
		sb.append(signerIdTypes.size());
		sb.append(" signer id types");
		return sb.toString();
	}
}
