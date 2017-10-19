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
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.factories.ReflectionClassFactory;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DomainPropertiesInstance;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

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

	private static final long serialVersionUID = 8029022101386823258L;
	private static final String PROPERTY_DELIMITER = ";";
	@Id
	@Column(length = 50)
	private String name;
	private String label;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<Policy> policies = new ArrayList<Policy>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<Module> modules = new ArrayList<Module>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	// @BatchFetch(BatchFetchType.IN) auskommentiert wegen bug in eclipselink - die am ct haengenden text-entities werden nicht richtig mitgeladen
	// (javax.resource.ResourceException: IJ000460: Error checking for a transaction)
	private List<ConsentTemplate> consentTemplates = new ArrayList<ConsentTemplate>();
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgen die funktionen "persistPropertiesToString" und "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private Properties properties = null;
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
	@Transient
	private VersionConverter ctVersionConverterInstance = null;
	@Transient
	private VersionConverter moduleVersionConverterInstance = null;
	@Transient
	private VersionConverter policyVersionConverterInstance = null;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<SignerIdType> signerIdTypes = new ArrayList<SignerIdType>();
	@Transient
	private DomainPropertiesInstance propertyObjects = null;

	public Domain() {
	}

	public Domain(DomainDTO dto) throws VersionConverterClassException {
		super();
		this.name = dto.getName();
		this.label = dto.getLabel();
		this.ctVersionConverter = dto.getCtVersionConverter();
		this.moduleVersionConverter = dto.getModuleVersionConverter();
		this.policyVersionConverter = dto.getPolicyVersionConverter();
		ctVersionConverterInstance = initVersionConverter(ctVersionConverter);
		moduleVersionConverterInstance = initVersionConverter(moduleVersionConverter);
		policyVersionConverterInstance = initVersionConverter(policyVersionConverter);
		this.propertiesString = dto.getProperties();
		loadPropertiesFromString();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		for (String signerIdType : dto.getSignerIdTypes()) {
			signerIdTypes.add(new SignerIdType(this, signerIdType));
		}
	}

	/**
	 * this method is called by jpa
	 */
	@PreUpdate
	@PrePersist
	public void persistPropertiesToString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> property : properties.entrySet()) {
			sb.append(property.getKey());
			sb.append("=");
			sb.append(property.getValue());
			sb.append(PROPERTY_DELIMITER);
		}
		propertiesString = sb.toString();
		propertyObjects = new DomainPropertiesInstance(properties);
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString() {
		properties = new Properties();
		if (propertiesString != null) {
			String[] propertyList = propertiesString.split(PROPERTY_DELIMITER);
			for (String property : propertyList) {
				String[] propertyParts = property.split("=");
				if (propertyParts.length == 2) {
					properties.put(propertyParts[0].trim(), propertyParts[1].trim());
				}
			}
		}
		propertyObjects = new DomainPropertiesInstance(properties);
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

	public Properties getProperties() {
		if (properties == null) {
			loadPropertiesFromString();
		}
		return properties;
	}

	public DomainPropertiesInstance getPropertyObjects() {
		if (propertyObjects == null) {
			loadPropertiesFromString();
		}
		return propertyObjects;
	}

	public String getPropertiesString() {
		return propertiesString;
	}

	public void setPropertiesString(String propertiesString) {
		this.propertiesString = propertiesString;
		loadPropertiesFromString();
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
		DomainDTO result = new DomainDTO(name, label, ctVersionConverter, moduleVersionConverter, policyVersionConverter, propertiesString, comment,
				externProperties, sIdTypes);
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

	public VersionConverter getCTVersionConverterInstance() throws VersionConverterClassException {
		if (ctVersionConverterInstance == null) {
			ctVersionConverterInstance = initVersionConverter(ctVersionConverter);
		}
		return ctVersionConverterInstance;
	}

	public VersionConverter getModuleVersionConverterInstance() throws VersionConverterClassException {
		if (moduleVersionConverterInstance == null) {
			moduleVersionConverterInstance = initVersionConverter(moduleVersionConverter);
		}
		return moduleVersionConverterInstance;
	}

	public VersionConverter getPolicyVersionConverterInstance() throws VersionConverterClassException {
		if (policyVersionConverterInstance == null) {
			policyVersionConverterInstance = initVersionConverter(policyVersionConverter);
		}
		return policyVersionConverterInstance;
	}

	public List<SignerIdType> getSignerIdTypes() {
		return signerIdTypes;
	}

	private VersionConverter initVersionConverter(String versionConverterClass) throws VersionConverterClassException {
		try {
			Class<? extends VersionConverter> converterClass = ReflectionClassFactory.getInstance().getSubClass(versionConverterClass,
					VersionConverter.class);
			return converterClass.newInstance();
		} catch (Exception e) {
			throw new VersionConverterClassException("exception while instantiating " + versionConverterClass, e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((consentTemplates == null) ? 0 : consentTemplates.hashCode());
		result = prime * result + ((ctVersionConverter == null) ? 0 : ctVersionConverter.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
		result = prime * result + ((moduleVersionConverter == null) ? 0 : moduleVersionConverter.hashCode());
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((policyVersionConverter == null) ? 0 : policyVersionConverter.hashCode());
		result = prime * result + ((propertiesString == null) ? 0 : propertiesString.hashCode());
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
		Domain other = (Domain) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (consentTemplates == null) {
			if (other.consentTemplates != null)
				return false;
		} else if (!consentTemplates.equals(other.consentTemplates))
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
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (modules == null) {
			if (other.modules != null)
				return false;
		} else if (!modules.equals(other.modules))
			return false;
		if (moduleVersionConverter == null) {
			if (other.moduleVersionConverter != null)
				return false;
		} else if (!moduleVersionConverter.equals(other.moduleVersionConverter))
			return false;
		if (policies == null) {
			if (other.policies != null)
				return false;
		} else if (!policies.equals(other.policies))
			return false;
		if (policyVersionConverter == null) {
			if (other.policyVersionConverter != null)
				return false;
		} else if (!policyVersionConverter.equals(other.policyVersionConverter))
			return false;
		if (propertiesString == null) {
			if (other.propertiesString != null)
				return false;
		} else if (!propertiesString.equals(other.propertiesString))
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
		sb.append(" consent templates and ");
		sb.append(signerIdTypes.size());
		sb.append(" signer id types");
		return sb.toString();
	}
}
