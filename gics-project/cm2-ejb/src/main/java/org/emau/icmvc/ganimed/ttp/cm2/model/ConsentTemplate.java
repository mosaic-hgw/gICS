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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PreRemove;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentTemplatePropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

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

	private static final long serialVersionUID = 2069983297054960850L;
	@EmbeddedId
	private ConsentTemplateKey key;
	private String title;
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgt die funktion "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private ConsentTemplatePropertiesObject propertiesObject = null;
	@Column(name = "PROPERTIES")
	private String propertiesString;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	@Column(columnDefinition = "char(20)")
	@Enumerated(EnumType.STRING)
	private ConsentTemplateType type;
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
	@OrderColumn(name = "POS")
	@BatchFetch(BatchFetchType.IN)
	private List<FreeTextDef> freeTextDefs = new ArrayList<FreeTextDef>();
	// keine explizite relation zu consents! die wuerden sonst bei fast jedem zugriff auf das template geladen (muessten mit in equals und hashcode rein)

	public ConsentTemplate() {
	}

	public ConsentTemplate(Domain domain, ConsentTemplateDTO dto, Map<ModuleKeyDTO, Module> modules, VersionConverterCache vcc)
			throws VersionConverterClassException, InvalidVersionException, FreeTextConverterStringException, UnknownModuleException,
			InvalidPropertiesException {
		super();
		try {
			this.key = new ConsentTemplateKey(vcc.getCTVersionConverter(domain.getName()), dto.getKey());
		} catch (UnknownDomainException impossible) {
			throw new VersionConverterClassException("impossible UnknownDomainException", impossible);
		}
		this.title = dto.getTitle();
		this.propertiesString = dto.getPropertiesString();
		loadPropertiesFromString();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.type = dto.getType();
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
					throw new UnknownModuleException(
							assignedModuleDTO.getParent() + ", which is set as parent of " + module + " is not part of " + key);
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
	@PostLoad
	public void loadPropertiesFromString() throws InvalidPropertiesException {
		try {
			propertiesObject = new ConsentTemplatePropertiesObject(propertiesString);
		} catch (ParseException e) {
			throw new InvalidPropertiesException(e);
		}
	}

	public ConsentTemplateKey getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ConsentTemplatePropertiesObject getPropertiesObject() throws InvalidPropertiesException {
		if (propertiesObject == null) {
			loadPropertiesFromString();
		}
		return propertiesObject;
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

	public ConsentTemplateType getType() {
		return type;
	}

	public void setType(ConsentTemplateType type) {
		this.type = type;
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

	public void setScanBase64(String scanBase64, String fileType) {
		this.scanBase64.setText(scanBase64);
		this.scanFileType = fileType;
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

	public ConsentTemplateDTO toDTO(VersionConverterCache vcc) throws VersionConverterClassException, InvalidVersionException {
		try {
			ConsentTemplateKeyDTO dtoKey = key.toDTO(vcc.getCTVersionConverter(domain.getName()));
			ConsentTemplateDTO result = new ConsentTemplateDTO(dtoKey);
			result.setTitle(title);
			result.setComment(comment);
			result.setExternProperties(externProperties);
			result.setType(type);
			result.setFooter(footer.getText());
			result.setHeader(header.getText());
			result.setScanBase64(scanBase64.getText());
			result.setScanFileType(scanFileType);
			result.setPropertiesString(propertiesString);
			List<AssignedModuleDTO> assignedModuleDTOs = new ArrayList<AssignedModuleDTO>();
			// nach ordernumber sortieren
			Collections.sort(moduleConsentTemplates);
			for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates) {
				assignedModuleDTOs.add(moduleConsentTemplate.toAssignedModuleDTO(vcc));
				ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getModule().getKey().toDTO(vcc.getModuleVersionConverter(domain.getName()));
				Map<ModuleKeyDTO, ArrayList<ModuleKeyDTO>> children = result.getStructure().getChildren();
				if (children.get(moduleKeyDTO) == null) {
					children.put(moduleKeyDTO, new ArrayList<ModuleKeyDTO>());
				}
				if (moduleConsentTemplate.getParent() == null) {
					result.getStructure().getFirstLevelModules().add(moduleKeyDTO);
				} else {
					ModuleKeyDTO parentKeyDTO = moduleConsentTemplate.getParent().getKey().toDTO(vcc.getModuleVersionConverter(domain.getName()));
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
		} catch (UnknownDomainException impossible) {
			throw new VersionConverterClassException("impossible unknownDomainException", impossible);
		}
	}

	@PreRemove
	private void beforeRemove() {
		domain.getConsentTemplates().remove(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ConsentTemplate other = (ConsentTemplate) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with title '");
		sb.append(title);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties: '");
		sb.append(externProperties);
		sb.append("', type: '");
		sb.append(type);
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
