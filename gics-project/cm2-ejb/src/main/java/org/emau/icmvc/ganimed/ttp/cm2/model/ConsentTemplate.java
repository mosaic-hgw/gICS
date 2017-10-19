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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

/**
 * ein consent template kann mehrere module (mit jeweils mehreren policies) enthalten, es entspricht dem elektronischen aequivalent eines nicht ausgefuellten konsentdokumentes
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "consent_template")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class ConsentTemplate implements Serializable {

	private static final long serialVersionUID = -2069012339762817311L;
	private static final String PROPERTY_DELIMITER = ";";
	@EmbeddedId
	private ConsentTemplateKey key;
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgen die funktionen "persistPropertiesToString" und "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private Properties properties;
	private String title;
	@Column(name = "PROPERTIES")
	private String propertiesString;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	@OneToMany(mappedBy = "consentTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModuleConsentTemplate> moduleConsentTemplates = new ArrayList<ModuleConsentTemplate>();
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "HEADER", referencedColumnName = "ID")
	private Text header;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "FOOTER", referencedColumnName = "ID")
	private Text footer;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "SCAN_BASE64", referencedColumnName = "ID")
	private Text scanBase64;
	private String scanFileType;
	@OneToMany(mappedBy = "consentTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<FreeTextDef> freeTextDefs = new ArrayList<FreeTextDef>();
	// keine explizite relation zu consents! die wuerden sonst bei fast jedem zugriff auf das template geladen (muessten mit in equals und hashcode rein)

	public ConsentTemplate() {
	}

	public ConsentTemplate(Domain domain, ConsentTemplateDTO dto, Map<ModuleKeyDTO, Module> modules) throws VersionConverterClassException,
			InvalidVersionException, FreeTextConverterStringException, UnknownModuleException {
		super();
		this.key = new ConsentTemplateKey(domain.getCTVersionConverterInstance(), dto.getKey());
		this.title = dto.getTitle();
		this.propertiesString = dto.getPropertiesString();
		loadPropertiesFromString();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.header = new Text(key, TextType.HEADER, dto.getHeader());
		this.footer = new Text(key, TextType.FOOTER, dto.getFooter());
		this.scanBase64 = new Text(key, TextType.CONSENTTEMPLATESCAN, dto.getScanBase64());
		this.scanFileType = dto.getScanFileType();
		for (FreeTextDefDTO freeTextDTO : dto.getFreeTextDefs()) {
			freeTextDefs.add(new FreeTextDef(this, freeTextDTO));
		}
		this.domain = domain;
		for (AssignedModuleDTO assignedModuleDTO : dto.getAssignedModules()) {
			Module module = modules.get(assignedModuleDTO.getModule().getKey());
			Module parent = null;
			if (assignedModuleDTO.getParent() != null) {
				parent = modules.get(assignedModuleDTO.getParent());
				if (parent == null) {
					throw new UnknownModuleException(assignedModuleDTO.getParent() + ", which is set as parent of " + module + " is not part of "
							+ key);
				}
			}
			ModuleConsentTemplate moduleConsentTemplate = new ModuleConsentTemplate(this, module, assignedModuleDTO, parent);
			moduleConsentTemplates.add(moduleConsentTemplate);
			module.getModuleConsentTemplates().add(moduleConsentTemplate);
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
	}

	public ConsentTemplateKey getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public Properties getProperties() {
		if (properties == null) {
			loadPropertiesFromString();
		}
		return properties;
	}

	public String getPropertiesString() {
		return propertiesString;
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

	public List<ModuleConsentTemplate> getModuleConsentTemplates() {
		return moduleConsentTemplates;
	}

	public Text getHeader() {
		return header;
	}

	public Text getFooter() {
		return footer;
	}

	public Text getScanBase64() {
		return scanBase64;
	}

	public List<FreeTextDef> getFreeTextDefs() {
		return freeTextDefs;
	}

	public String getScanFileType() {
		return scanFileType;
	}

	public ConsentTemplateDTO toDTO() throws VersionConverterClassException, InvalidVersionException {
		ConsentTemplateKeyDTO dtoKey = key.toDTO(domain.getCTVersionConverterInstance());
		ConsentTemplateDTO result = new ConsentTemplateDTO(dtoKey);
		result.setTitle(title);
		result.setComment(comment);
		result.setExternProperties(externProperties);
		result.setFooter(footer.getText());
		result.setHeader(header.getText());
		result.setScanBase64(scanBase64.getText());
		result.setScanFileType(scanFileType);
		result.setPropertiesString(propertiesString);
		List<AssignedModuleDTO> assignedModuleDTOs = new ArrayList<AssignedModuleDTO>();
		// nach ordernumber sortieren
		Collections.sort(moduleConsentTemplates);
		for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates) {
			assignedModuleDTOs.add(moduleConsentTemplate.toAssignedModuleDTO());
			ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getModule().getKey().toDTO(domain.getModuleVersionConverterInstance());
			Map<ModuleKeyDTO, ArrayList<ModuleKeyDTO>> children = result.getStructure().getChildren();
			if (children.get(moduleKeyDTO) == null) {
				children.put(moduleKeyDTO, new ArrayList<ModuleKeyDTO>());
			}
			if (moduleConsentTemplate.getParent() == null) {
				result.getStructure().getFirstLevelModules().add(moduleKeyDTO);
			} else {
				ModuleKeyDTO parentKeyDTO = moduleConsentTemplate.getParent().getKey().toDTO(domain.getModuleVersionConverterInstance());
				if (children.get(parentKeyDTO) == null) {
					children.put(parentKeyDTO, new ArrayList<ModuleKeyDTO>());
				}
				children.get(parentKeyDTO).add(moduleKeyDTO);
			}
		}
		result.setAssignedModules(assignedModuleDTOs);
		List<FreeTextDefDTO> freeTextDTOs = new ArrayList<FreeTextDefDTO>();
		for (FreeTextDef freeText : freeTextDefs) {
			freeTextDTOs.add(freeText.toDTO());
		}
		result.setFreeTextDefs(freeTextDTOs);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((freeTextDefs == null) ? 0 : freeTextDefs.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((moduleConsentTemplates == null) ? 0 : moduleConsentTemplates.hashCode());
		result = prime * result + ((propertiesString == null) ? 0 : propertiesString.hashCode());
		result = prime * result + ((scanBase64 == null) ? 0 : scanBase64.hashCode());
		result = prime * result + ((scanFileType == null) ? 0 : scanFileType.hashCode());
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
		ConsentTemplate other = (ConsentTemplate) obj;
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
		if (footer == null) {
			if (other.footer != null)
				return false;
		} else if (!footer.equals(other.footer))
			return false;
		if (freeTextDefs == null) {
			if (other.freeTextDefs != null)
				return false;
		} else if (!freeTextDefs.equals(other.freeTextDefs))
			return false;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (moduleConsentTemplates == null) {
			if (other.moduleConsentTemplates != null)
				return false;
		} else if (!moduleConsentTemplates.equals(other.moduleConsentTemplates))
			return false;
		if (propertiesString == null) {
			if (other.propertiesString != null)
				return false;
		} else if (!propertiesString.equals(other.propertiesString))
			return false;
		if (scanBase64 == null) {
			if (other.scanBase64 != null)
				return false;
		} else if (!scanBase64.equals(other.scanBase64))
			return false;
		if (scanFileType == null) {
			if (other.scanFileType != null)
				return false;
		} else if (!scanFileType.equals(other.scanFileType))
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
		sb.append("', extern properties: '");
		sb.append(externProperties);
		sb.append("', properties: '");
		sb.append(propertiesString);
		sb.append("', ");
		sb.append(moduleConsentTemplates.size());
		sb.append(" modules and ");
		sb.append(freeTextDefs.size());
		sb.append(" free text fields");
		return sb.toString();
	}
}
