package org.emau.icmvc.ganimed.ttp.cm2.model;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
 * 							kontakt-ths@uni-greifswald.de
 *
 * 							concept and implementation
 * 							l.geidel, c.hampf
 * 							web client
 * 							a.blumentritt, m.bialke, f.m.moser
 * 							fhir-api
 * 							m.bialke
 * 							docker
 * 							r. schuldt
 *
 * 							The gICS was developed by the University Medicine Greifswald and published
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 *
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12911-022-02081-4
 * 							https://doi.org/10.1186/s12967-020-02457-y
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

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.TextType;

/**
 * ein consent template kann mehrere module (mit jeweils mehreren policies) enthalten, es entspricht dem elektronischen aequivalent eines nicht ausgefuellten konsentdokumentes
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "consent_template")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_CT")
public class ConsentTemplate implements Serializable, FhirDTOExporter<ConsentTemplateDTO>
{
	@Serial
	private static final long serialVersionUID = 5679698013992681307L;
	private static final Logger logger = LogManager.getLogger(ConsentTemplate.class);
	@EmbeddedId
	private ConsentTemplateKey key;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "TITLE", referencedColumnName = "ID")
	private Text title;
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgt die funktion "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private ExpirationPropertiesObject expirationPropertiesObject = null;
	@Column(name = "EXPIRATION_PROPERTIES")
	private String expirationProperties;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@Column(columnDefinition = "char(20)")
	@Enumerated(EnumType.STRING)
	private ConsentTemplateType type;
	@OneToMany(mappedBy = "consentTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModuleConsentTemplate> moduleConsentTemplates = new ArrayList<>();
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
	@JoinColumn(name = "SCAN", referencedColumnName = "ID")
	private ConsentTemplateScan scan;
	@OneToMany(mappedBy = "consentTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderColumn(name = "POS")
	@BatchFetch(BatchFetchType.IN)
	private List<FreeTextDef> freeTextDefs = new ArrayList<>();
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "LABEL", length = 255)
	private String label;
	@Column(name = "VERSION_LABEL", length = 255)
	private String versionLabel;
	@Column(name = "FINALISED", nullable = false)
	private boolean finalised;
	@OneToMany(mappedBy = "consentTemplateFrom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private Set<MappedConsentTemplate> mappedConsentTemplates = new HashSet<>();
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_CT")
	private String fhirID;
	// keine explizite relation zu consents! die wuerden sonst bei fast jedem zugriff auf das template geladen (muessten mit in equals und hashcode rein)

	public ConsentTemplate()
	{}

	public ConsentTemplate(Domain domain, ConsentTemplateDTO dto, Map<ModuleKeyDTO, Module> modules, Map<ConsentTemplateKeyDTO, ConsentTemplate> templates,
			boolean finaliseRelatedEntities)
			throws FreeTextConverterStringException, InvalidParameterException, InvalidPropertiesException, InvalidVersionException,
			RequirementsNotFullfilledException,	UnknownModuleException, UnknownDomainException
	{
		super();
		this.key = new ConsentTemplateKey(VersionConverterCache.getCTVersionConverter(domain.getName()), dto.getKey());
		this.title = new Text(key, TextType.TEMPLATE_TITLE, dto.getTitle());
		this.expirationPropertiesObject = new ExpirationPropertiesObject(dto.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.type = dto.getType();
		this.header = new Text(key, TextType.HEADER, dto.getHeader());
		this.footer = new Text(key, TextType.FOOTER, dto.getFooter());
		this.scan = new ConsentTemplateScan(key, dto.getScanBase64(), dto.getScanFileType());
		for (FreeTextDefDTO freeTextDTO : dto.getFreeTextDefs())
		{
			freeTextDefs.add(new FreeTextDef(this, freeTextDTO));
		}
		this.domain = domain;
		for (AssignedModuleDTO assignedModuleDTO : dto.getAssignedModules())
		{
			Module module = modules.get(assignedModuleDTO.getModule().getKey());
			Module parent = null;
			if (assignedModuleDTO.getParent() != null)
			{
				parent = modules.get(assignedModuleDTO.getParent());
				if (parent == null)
				{
					throw new UnknownModuleException(
							assignedModuleDTO.getParent() + ", which is set as parent of " + module + " is not part of " + key);
				}
			}
			ModuleConsentTemplate moduleConsentTemplate = new ModuleConsentTemplate(this, module, assignedModuleDTO, parent);
			moduleConsentTemplates.add(moduleConsentTemplate);
			module.getModuleConsentTemplates().add(moduleConsentTemplate);
		}

		dto.getAllMappedTemplates().forEach(key -> mappedConsentTemplates.add(
				new MappedConsentTemplate(this, templates.get(key))));

		setLabel(dto.getLabel());
		setVersionLabel(dto.getVersionLabel());
		setFinalised(dto.getFinalised(), finaliseRelatedEntities);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		this.createTimestamp = timestamp;
		this.updateTimestamp = timestamp;
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString()
	{
		try
		{
			expirationPropertiesObject = new ExpirationPropertiesObject(expirationProperties);
		}
		catch (ParseException e)
		{
			logger.fatal("exception while parsing expirationProperties '" + expirationProperties + "'", e);
		}
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public ConsentTemplateKey getKey()
	{
		return key;
	}

	public Text getTitle()
	{
		return title;
	}

	public ExpirationPropertiesObject getExpirationPropertiesObject()
	{
		if (expirationPropertiesObject == null)
		{
			loadPropertiesFromString();
		}
		return expirationPropertiesObject;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public ConsentTemplateType getType()
	{
		return type;
	}

	public List<ModuleConsentTemplate> getModuleConsentTemplates()
	{
		return moduleConsentTemplates;
	}

	public void setModuleConsentTemplates(List<ModuleConsentTemplate> moduleConsentTemplates)
	{
		this.moduleConsentTemplates = moduleConsentTemplates;
	}

	public Text getHeader()
	{
		return header;
	}

	public Text getFooter()
	{
		return footer;
	}

	public void setScanBase64(String scanBase64, String fileType) throws InvalidParameterException
	{
		this.scan.setContent(scanBase64);
		this.scan.setFileType(fileType);
	}

	public String getScanBase64()
	{
		return scan != null ? scan.getContent() : null;
	}

	public List<FreeTextDef> getFreeTextDefs()
	{
		return freeTextDefs;
	}

	public void setFreeTextDefs(List<FreeTextDef> freeTextDefs)
	{
		this.freeTextDefs = freeTextDefs;
	}

	public String getScanFileType()
	{
		return scan != null ? scan.getFileType() : null;
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Timestamp getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public String getLabel()
	{
		return label;
	}

	private void setLabel(String label)
	{
		this.label = label != null ? label : key.getName();
	}

	public String getVersionLabel()
	{
		return versionLabel;
	}

	private void setVersionLabel(String versionLabel)
	{
		// in contrast to the (name) label do not use any default value for the version label but leave it null
		this.versionLabel = versionLabel;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	private void setFinalised(boolean finalised, boolean finaliseRelatedEntities) throws RequirementsNotFullfilledException
	{
		if (finalised)
		{
			if (finaliseRelatedEntities)
			{
				for (ModuleConsentTemplate mct : moduleConsentTemplates)
				{
					mct.getModule().finalise(true);
				}
			}
			else
			{
				for (ModuleConsentTemplate mct : moduleConsentTemplates)
				{
					if (!mct.getModule().getFinalised())
					{
						String message = key + " can't be finalised because at least one related module (" + mct.getModule().getKey() + ") isn't finalised";
						throw new RequirementsNotFullfilledException(message);
					}
				}
			}
			this.finalised = true;
		}
	}

	@PreRemove
	private void beforeRemove()
	{
		new HashSet<>(mappedConsentTemplates).forEach(mct ->
				mct.getConsentTemplateTo().getMappedConsentTemplates().removeIf(rmct ->
						this.equals(rmct.getConsentTemplateTo())));
		mappedConsentTemplates.clear();
	}

	public Set<MappedConsentTemplate> getMappedConsentTemplates()
	{
		return mappedConsentTemplates;
	}

	public void setMappedConsentTemplates(Set<MappedConsentTemplate> mappedConsentTemplates)
	{
		if (mappedConsentTemplates != null)
		{
			for (MappedConsentTemplate mct : mappedConsentTemplates)
			{
				if (!this.equals(mct.getConsentTemplateFrom()))
				{
					throw new IllegalArgumentException("this ct and the first ct in any mapping must match");
				}
			}
		}
		this.mappedConsentTemplates = mappedConsentTemplates;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void finalise(boolean finaliseRelatedEntities) throws RequirementsNotFullfilledException
	{
		setFinalised(true, finaliseRelatedEntities);
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateInUse(ConsentTemplateDTO dto, boolean updateRel)
			throws InvalidFreeTextException, InvalidVersionException, UnknownDomainException, UnknownModuleException, InvalidParameterException
	{
		setLabel(dto.getLabel());
		setVersionLabel(dto.getVersionLabel());
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		setScanBase64(dto.getScanBase64(), dto.getScanFileType());
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
		updateExpiration(dto.getExpirationProperties());

		if (updateRel)
		{
			for (FreeTextDefDTO freeTextDTO : dto.getFreeTextDefs())
			{
				boolean found = false;
				for (FreeTextDef freeTextDef : freeTextDefs)
				{
					if (freeTextDef.getKey().getName().equals(freeTextDTO.getName()))
					{
						freeTextDef.updateInUse(freeTextDTO.getComment(), freeTextDTO.getExternProperties(), freeTextDTO.getLabel(), freeTextDTO.getPos());
						found = true;
						break;
					}
				}
				if (!found && finalised)
				{
					throw new InvalidFreeTextException("unknown free text with name '" + freeTextDTO.getName() + "'");
				}
			}
			for (AssignedModuleDTO assignedModuleDTO : dto.getAssignedModules())
			{
				boolean found = false;
				ModuleKeyDTO keyDTO = assignedModuleDTO.getModule().getKey();
				ModuleKey key = new ModuleKey(VersionConverterCache.getModuleVersionConverter(keyDTO.getDomainName()), keyDTO);
				for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates)
				{
					if (moduleConsentTemplate.getKey().getModuleKey().equals(key))
					{
						moduleConsentTemplate.updateInUse(assignedModuleDTO.getComment(),
								assignedModuleDTO.getExternProperties(), assignedModuleDTO.getExpirationProperties());
						found = true;
						break;
					}
				}
				if (!found && finalised)
				{
					throw new UnknownModuleException("unknown module: " + key);
				}
			}
		}
	}

	public void update(ConsentTemplateDTO dto, boolean finaliseRelatedEntities) throws InvalidFreeTextException, InvalidPropertiesException,
			ObjectInUseException, RequirementsNotFullfilledException, UnknownDomainException, UnknownModuleException, InvalidVersionException, InvalidParameterException
	{
		if (finalised)
		{
			throw new ObjectInUseException(
					"consent template is finalised and can't be updated anymore. to update non-critical fields please use updateConsentTemplateInUse(...)");
		}
		updateInUse(dto, false); // also will handle expiration properties
		this.title.setText(dto.getTitle());
		this.type = dto.getType();
		this.header.setText(dto.getHeader());
		this.footer.setText(dto.getFooter());
		setFinalised(dto.getFinalised(), finaliseRelatedEntities);
	}

	private void updateExpiration(ExpirationPropertiesDTO expiration)
	{
		// for finalized templates only allow to update future expiration properties (existing as well as new date must be in the future)
		this.expirationPropertiesObject = expirationPropertiesObject.createMergedExpirationProperties(expiration, finalised);
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
	}

	@Override
	public ConsentTemplateDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		ConsentTemplateKeyDTO dtoKey = key.toDTO(VersionConverterCache.getCTVersionConverter(domain.getName()));
		ConsentTemplateDTO result = new ConsentTemplateDTO(dtoKey);
		result.setTitle(title.getText());
		result.setComment(comment);
		result.setExternProperties(externProperties);
		result.setType(type);
		result.setFooter(footer.getText());
		result.setHeader(header.getText());
		result.setScanBase64(getScanBase64());
		result.setScanFileType(getScanFileType());
		result.setExpirationProperties(expirationPropertiesObject.toDTO());
		result.setCreationDate(new Date(createTimestamp.getTime()));
		result.setUpdateDate(new Date(updateTimestamp.getTime()));
		result.setLabel(label);
		result.setVersionLabel(versionLabel);
		result.setFinalised(finalised);
		Set<AssignedModuleDTO> assignedModuleDTOs = new HashSet<>();
		// nach ordernumber sortieren
		Collections.sort(moduleConsentTemplates);
		Map<ModuleKeyDTO, List<ModuleKeyDTO>> tempChildren = new HashMap<>();
		for (ModuleConsentTemplate moduleConsentTemplate : moduleConsentTemplates)
		{
			assignedModuleDTOs.add(moduleConsentTemplate.toDTO());
			ModuleKeyDTO moduleKeyDTO = moduleConsentTemplate.getModule().getKey()
					.toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName()));
			if (tempChildren.get(moduleKeyDTO) == null)
			{
				tempChildren.put(moduleKeyDTO, new ArrayList<>());
			}
			if (moduleConsentTemplate.getParent() == null)
			{
				result.getStructure().getFirstLevelModules().add(moduleKeyDTO);
			}
			else
			{
				ModuleKeyDTO parentKeyDTO = moduleConsentTemplate.getParent().getKey()
						.toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName()));
				if (tempChildren.get(parentKeyDTO) == null)
				{
					tempChildren.put(parentKeyDTO, new ArrayList<>());
				}
				tempChildren.get(parentKeyDTO).add(moduleKeyDTO);
			}
		}
		Map<ModuleKeyDTO, ModuleKeyDTO[]> children = result.getStructure().getChildren();
		for (Map.Entry<ModuleKeyDTO, List<ModuleKeyDTO>> tempChild : tempChildren.entrySet())
		{
			children.put(tempChild.getKey(), tempChild.getValue().toArray(new ModuleKeyDTO[tempChild.getValue().size()]));
		}
		result.setAssignedModules(assignedModuleDTOs);
		Set<FreeTextDefDTO> freeTextDTOs = new HashSet<>();
		for (FreeTextDef freeText : freeTextDefs)
		{
			freeTextDTOs.add(freeText.toDTO());
		}
		result.setFreeTextDefs(freeTextDTOs);
		result.setFhirID(fhirID);

		final Set<ConsentTemplateKeyDTO> consentMappings = new HashSet<>();
		final Set<ConsentTemplateKeyDTO> refusalMappings = new HashSet<>();
		final Set<ConsentTemplateKeyDTO> revocationMappings = new HashSet<>();

		for (MappedConsentTemplate m : getMappedConsentTemplates())
		{
			ConsentTemplate consentTemplateTo = m.getConsentTemplateTo();
			ConsentTemplateKeyDTO keyDTO = consentTemplateTo.getKey().toDTO(
					VersionConverterCache.getCTVersionConverter(domain.getName()));

			switch (consentTemplateTo.getType())
			{
				case CONSENT -> consentMappings.add(keyDTO);
				case REFUSAL -> refusalMappings.add(keyDTO);
				case REVOCATION -> revocationMappings.add(keyDTO);
			}
		}

		result.setMappedConsentTemplates(consentMappings);
		result.setMappedRefusalTemplates(refusalMappings);
		result.setMappedRevocationTemplates(revocationMappings);

		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ConsentTemplate other = (ConsentTemplate) obj;
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with title '");
		sb.append(title);
		sb.append("', label '");
		sb.append(label);
		sb.append("', versionLabel '");
		sb.append(versionLabel);
		sb.append(", comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("', type '");
		sb.append(type);
		sb.append("', expiration properties '");
		sb.append(expirationProperties);
		sb.append("', ");
		sb.append(moduleConsentTemplates.size());
		sb.append(" modules and ");
		sb.append(freeTextDefs.size());
		sb.append(" free text fields.");
		// on accessing #toString() to create informative error messages when exceptions have occurred before,
		// then the dates can be null, which leads to NPEs hiding the real cause of the error:
		if (createTimestamp != null)
		{
			sb.append(" created at ");
			sb.append(new Date(createTimestamp.getTime()));
		}
		if (updateTimestamp != null)
		{
			sb.append(" last update at ");
			sb.append(new Date(updateTimestamp.getTime()));
		}
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		return sb.toString();
	}
}
