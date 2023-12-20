package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;

/**
 * ein consent template kann mehrere module (mit jeweils mehreren policies) enthalten, es entspricht
 * dem elektronischen aequivalent eines nicht ausgefuellten konsentdokumentes
 *
 * @author geidell
 *
 */
public class ConsentTemplateDTO extends FhirIdDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = 3799101600837986225L;
	private ConsentTemplateKeyDTO key;
	private String title;
	private ExpirationPropertiesDTO expirationProperties = new ExpirationPropertiesDTO();
	private String comment;
	private String externProperties;
	private ConsentTemplateType type;
	private String header;
	private String footer;
	private String scanBase64;
	private String scanFileType;
	private final Set<AssignedModuleDTO> assignedModules = new HashSet<>();
	private final Set<FreeTextDefDTO> freeTextDefs = new HashSet<>();
	private ConsentTemplateStructureDTO structure = new ConsentTemplateStructureDTO();
	private Date creationDate;
	private Date updateDate;
	private String label;
	private String versionLabel;
	private boolean finalised = false;
	private final Set<ConsentTemplateKeyDTO> mappedConsentTemplates = new HashSet<>();
	private final Set<ConsentTemplateKeyDTO> mappedRevocationTemplates = new HashSet<>();
	private final Set<ConsentTemplateKeyDTO> mappedRefusalTemplates = new HashSet<>();

	public ConsentTemplateDTO()
	{
		super(null);
	}

	public ConsentTemplateDTO(ConsentTemplateDTO dto)
	{
		this(dto.getKey(), dto.getTitle(), dto.getExpirationProperties(), dto.getComment(), dto.getExternProperties(),
				dto.getType(), dto.getHeader(), dto.getFooter(), dto.getScanBase64(), dto.getScanFileType(), dto.getAssignedModules(),
				dto.getFreeTextDefs(), dto.getLabel(), dto.getVersionLabel(), dto.getFinalised(),
				dto.getCreationDate(), dto.getUpdateDate(), dto.getFhirID(), dto.getStructure(),
				dto.getMappedConsentTemplates(), dto.getMappedRevocationTemplates(), dto.getMappedRefusalTemplates());
	}

	public ConsentTemplateDTO(ConsentTemplateKeyDTO key)
	{
		super(null);
		setKey(key);
	}

	public ConsentTemplateDTO(ConsentTemplateKeyDTO key, String title, ExpirationPropertiesDTO expirationProperties, String comment,
			String externProperties, ConsentTemplateType type, String header, String footer, String scanBase64, String scanFileType,
			Set<AssignedModuleDTO> assignedModules, Set<FreeTextDefDTO> freeTextDefs, String label, String versionLabel,
			boolean finalised, Date creationDate, Date updateDate, String fhirID, ConsentTemplateStructureDTO structure,
			Set<ConsentTemplateKeyDTO> mappedConsentTemplates, Set<ConsentTemplateKeyDTO> mappedRevocationTemplates, Set<ConsentTemplateKeyDTO> mappedRefusalTemplates)
	{
		super(fhirID);
		setKey(key);
		this.title = title;
		setExpirationProperties(expirationProperties);
		this.comment = comment;
		this.externProperties = externProperties;
		this.type = type;
		this.header = header;
		this.footer = footer;
		this.scanBase64 = scanBase64;
		this.scanFileType = scanFileType;
		setAssignedModules(assignedModules);
		setFreeTextDefs(freeTextDefs);
		this.label = label;
		this.versionLabel = versionLabel;
		this.finalised = finalised;
		setCreationDate(creationDate);
		setUpdateDate(updateDate);
		setStructure(structure);
		setMappedConsentTemplates(mappedConsentTemplates);
		setMappedRevocationTemplates(mappedRevocationTemplates);
		setMappedRefusalTemplates(mappedRefusalTemplates);
	}

	public ConsentTemplateKeyDTO getKey()
	{
		return key;
	}

	public void setKey(ConsentTemplateKeyDTO key)
	{
		if (key != null)
		{
			this.key = new ConsentTemplateKeyDTO(key);
		}
		else
		{
			this.key = null;
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public ExpirationPropertiesDTO getExpirationProperties()
	{
		return expirationProperties;
	}

	public void setExpirationProperties(ExpirationPropertiesDTO expirationProperties)
	{
		if (expirationProperties != null)
		{
			this.expirationProperties = new ExpirationPropertiesDTO(expirationProperties);
		}
		else
		{
			this.expirationProperties = new ExpirationPropertiesDTO();
		}
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	public ConsentTemplateType getType()
	{
		return type;
	}

	public void setType(ConsentTemplateType type)
	{
		this.type = type;
	}

	public String getHeader()
	{
		return header;
	}

	public void setHeader(String header)
	{
		this.header = header;
	}

	public String getFooter()
	{
		return footer;
	}

	public void setFooter(String footer)
	{
		this.footer = footer;
	}

	public String getScanBase64()
	{
		return scanBase64;
	}

	public void setScanBase64(String scanBase64)
	{
		this.scanBase64 = scanBase64;
	}

	public String getScanFileType()
	{
		return scanFileType;
	}

	public void setScanFileType(String scanFileType)
	{
		this.scanFileType = scanFileType;
	}

	public Set<AssignedModuleDTO> getAssignedModules()
	{
		return assignedModules;
	}

	public void setAssignedModules(Set<AssignedModuleDTO> assignedModules)
	{
		if (this.assignedModules != assignedModules)
		{
			this.assignedModules.clear();
			if (assignedModules != null)
			{
				for (AssignedModuleDTO dto : assignedModules)
				{
					this.assignedModules.add(new AssignedModuleDTO(dto));
				}
			}
		}
	}

	public Set<FreeTextDefDTO> getFreeTextDefs()
	{
		return freeTextDefs;
	}

	public void setFreeTextDefs(Set<FreeTextDefDTO> freeTextDefs)
	{
		if (this.freeTextDefs != freeTextDefs)
		{
			this.freeTextDefs.clear();
			if (freeTextDefs != null)
			{
				for (FreeTextDefDTO dto : freeTextDefs)
				{
					this.freeTextDefs.add(new FreeTextDefDTO(dto));
				}
			}
		}
	}

	public ConsentTemplateStructureDTO getStructure()
	{
		return structure;
	}

	public void setStructure(ConsentTemplateStructureDTO structure)
	{
		if (structure != null)
		{
			this.structure = new ConsentTemplateStructureDTO(structure);
		}
		else
		{
			this.structure = null;
		}
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		if (creationDate != null)
		{
			this.creationDate = new Date(creationDate.getTime());
		}
		else
		{
			this.creationDate = null;
		}
	}

	public Date getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate(Date updateDate)
	{
		if (updateDate != null)
		{
			this.updateDate = new Date(updateDate.getTime());
		}
		else
		{
			this.updateDate = null;
		}
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabelOrName()
	{
		return StringUtils.isNotEmpty(label) ? label : key.getName();
	}

	public String getVersionLabel()
	{
		return versionLabel;
	}

	/**
	 * Returns the version supplemented by the version label (if not null)
	 *
	 * @return "VERSION (VERSION_LABEL)" or "VERSION" if there is no VERSION_LABEL
	 */
	public String getVersionLabelAndVersion()
	{
		return key.getVersion() + (StringUtils.isEmpty(versionLabel) ? "" : " (" + versionLabel + ")");
	}

	/**
	 * Returns the version label if not null or the version otherwise
	 *
	 * @return "VERSION_LABEL" or "VERSION" if there is no VERSION_LABEL
	 */
	public String getVersionLabelOrVersion()
	{
		return versionLabel != null ? versionLabel : key.getVersion();
	}

	public void setVersionLabel(String versionLabel)
	{
		this.versionLabel = StringUtils.isEmpty(versionLabel) ? null : versionLabel;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	public void setFinalised(boolean finalised)
	{
		this.finalised = finalised;
	}

	@Override
	public String getDomainName()
	{
		return getKey().getDomainName();
	}

	public Set<ConsentTemplateKeyDTO> getMappedConsentTemplates()
	{
		return mappedConsentTemplates;
	}

	public void setMappedConsentTemplates(Set<ConsentTemplateKeyDTO> mappedConsentTemplates)
	{
		this.mappedConsentTemplates.clear();
		if (mappedConsentTemplates != null)
		{
			for (ConsentTemplateKeyDTO key : mappedConsentTemplates)
			{
				this.mappedConsentTemplates.add(new ConsentTemplateKeyDTO(key));
			}
		}
	}

	public Set<ConsentTemplateKeyDTO> getMappedRevocationTemplates()
	{
		return mappedRevocationTemplates;
	}

	public void setMappedRevocationTemplates(Set<ConsentTemplateKeyDTO> mappedRevocationTemplates)
	{
		this.mappedRevocationTemplates.clear();
		if (mappedRevocationTemplates != null)
		{
			for (ConsentTemplateKeyDTO key : mappedRevocationTemplates)
			{
				this.mappedRevocationTemplates.add(new ConsentTemplateKeyDTO(key));
			}
		}
	}

	public Set<ConsentTemplateKeyDTO> getMappedRefusalTemplates()
	{
		return mappedRefusalTemplates;
	}

	public void setMappedRefusalTemplates(Set<ConsentTemplateKeyDTO> mappedRefusalTemplates)
	{
		this.mappedRefusalTemplates.clear();
		if (mappedRefusalTemplates != null)
		{
			for (ConsentTemplateKeyDTO key : mappedRefusalTemplates)
			{
				this.mappedRefusalTemplates.add(new ConsentTemplateKeyDTO(key));
			}
		}
	}

	public Set<ConsentTemplateKeyDTO> getMappedTemplates(ConsentTemplateType type)
	{
		return switch (type)
		{
			case CONSENT -> getMappedConsentTemplates();
			case REVOCATION -> getMappedRevocationTemplates();
			case REFUSAL -> getMappedRefusalTemplates();
		};
	}

	public void setMappedTemplates(ConsentTemplateType type, Set<ConsentTemplateKeyDTO> mappedTemplates)
	{
		switch (type)
		{
			case CONSENT -> setMappedConsentTemplates(mappedTemplates);
			case REVOCATION -> setMappedRevocationTemplates(mappedTemplates);
			case REFUSAL -> setMappedRefusalTemplates(mappedTemplates);
		}
	}

	public Set<ConsentTemplateKeyDTO> getAllMappedTemplates()
	{
		Set<ConsentTemplateKeyDTO> mappedTemplates = new HashSet<>();
		Arrays.stream(ConsentTemplateType.values()).forEach(t -> mappedTemplates.addAll(getMappedTemplates(t)));
		return mappedTemplates;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + assignedModules.hashCode();
		result = prime * result + mappedConsentTemplates.hashCode();
		result = prime * result + mappedRevocationTemplates.hashCode();
		result = prime * result + mappedRefusalTemplates.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (expirationProperties == null ? 0 : expirationProperties.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (finalised ? 1231 : 1237);
		result = prime * result + (footer == null ? 0 : footer.hashCode());
		result = prime * result + (freeTextDefs == null ? 0 : freeTextDefs.hashCode());
		result = prime * result + (header == null ? 0 : header.hashCode());
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (scanBase64 == null ? 0 : scanBase64.hashCode());
		result = prime * result + (scanFileType == null ? 0 : scanFileType.hashCode());
		result = prime * result + (structure == null ? 0 : structure.hashCode());
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + (updateDate == null ? 0 : updateDate.hashCode());
		result = prime * result + (versionLabel == null ? 0 : versionLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ConsentTemplateDTO other = (ConsentTemplateDTO) obj;
		if (!assignedModules.equals(other.assignedModules))
		{
			return false;
		}
		if (!mappedConsentTemplates.equals(other.mappedConsentTemplates))
		{
			return false;
		}
		if (!mappedRevocationTemplates.equals(other.mappedRevocationTemplates))
		{
			return false;
		}
		if (!mappedRefusalTemplates.equals(other.mappedRefusalTemplates))
		{
			return false;
		}
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (creationDate == null)
		{
			if (other.creationDate != null)
			{
				return false;
			}
		}
		else if (!creationDate.equals(other.creationDate))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (finalised != other.finalised)
		{
			return false;
		}
		if (footer == null)
		{
			if (other.footer != null)
			{
				return false;
			}
		}
		else if (!footer.equals(other.footer))
		{
			return false;
		}
		if (freeTextDefs == null)
		{
			if (other.freeTextDefs != null)
			{
				return false;
			}
		}
		else if (!freeTextDefs.equals(other.freeTextDefs))
		{
			return false;
		}
		if (header == null)
		{
			if (other.header != null)
			{
				return false;
			}
		}
		else if (!header.equals(other.header))
		{
			return false;
		}
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
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}
		if (scanBase64 == null)
		{
			if (other.scanBase64 != null)
			{
				return false;
			}
		}
		else if (!scanBase64.equals(other.scanBase64))
		{
			return false;
		}
		if (scanFileType == null)
		{
			if (other.scanFileType != null)
			{
				return false;
			}
		}
		else if (!scanFileType.equals(other.scanFileType))
		{
			return false;
		}
		if (structure == null)
		{
			if (other.structure != null)
			{
				return false;
			}
		}
		else if (!structure.equals(other.structure))
		{
			return false;
		}
		if (title == null)
		{
			if (other.title != null)
			{
				return false;
			}
		}
		else if (!title.equals(other.title))
		{
			return false;
		}
		if (type != other.type)
		{
			return false;
		}
		if (updateDate == null)
		{
			if (other.updateDate != null)
			{
				return false;
			}
		}
		else if (!updateDate.equals(other.updateDate))
		{
			return false;
		}
		if (versionLabel == null)
		{
			if (other.versionLabel != null)
			{
				return false;
			}
		}
		else if (!versionLabel.equals(other.versionLabel))
		{
			return false;
		}
		return true;
	}

	public boolean equalsForFhirSerice(Object obj)
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
		ConsentTemplateDTO other = (ConsentTemplateDTO) obj;

		if (assignedModules == null || assignedModules.isEmpty())
		{
			if (other.assignedModules != null && !other.assignedModules.isEmpty())
			{
				return false;
			}
		}
		else
		{
			if (other.assignedModules == null || other.assignedModules.isEmpty())
			{
				return false;
			}
			if (assignedModules.size() != other.assignedModules.size())
			{
				return false;
			}

			Map<ModuleKeyDTO, AssignedModuleDTO> otherAssignedModuleDTOs = new HashMap<>();
			for (AssignedModuleDTO assignedModuleDTO : other.assignedModules)
			{
				otherAssignedModuleDTOs.put(assignedModuleDTO.getModule().getKey(), assignedModuleDTO);
			}

			for (AssignedModuleDTO assignedModuleDTO : assignedModules)
			{
				AssignedModuleDTO otherAssignedModuleDTO = otherAssignedModuleDTOs.get(assignedModuleDTO.getModule().getKey());
				if (!assignedModuleDTO.equalsForFhirSerice(otherAssignedModuleDTO))
				{
					return false;
				}
			}
		}

		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}

		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (finalised != other.finalised)
		{
			return false;
		}
		if (footer == null)
		{
			if (other.footer != null)
			{
				return false;
			}
		}
		else if (!footer.equals(other.footer))
		{
			return false;
		}

		// freetexts

		if (freeTextDefs == null)
		{
			if (other.freeTextDefs != null)
			{
				return false;
			}
		}
		else if (!freeTextDefs.equals(other.freeTextDefs))
		{
			return false;
		}

		if (header == null)
		{
			if (other.header != null)
			{
				return false;
			}
		}
		else if (!header.equals(other.header))
		{
			return false;
		}
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
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}
		if (versionLabel == null)
		{
			if (other.versionLabel != null)
			{
				return false;
			}
		}
		else if (!versionLabel.equals(other.versionLabel))
		{
			return false;
		}
		if (scanBase64 == null)
		{
			if (other.scanBase64 != null)
			{
				return false;
			}
		}
		else if (!scanBase64.equals(other.scanBase64))
		{
			return false;
		}
		if (scanFileType == null)
		{
			if (other.scanFileType != null)
			{
				return false;
			}
		}
		else if (!scanFileType.equals(other.scanFileType))
		{
			return false;
		}
		if (structure == null)
		{
			if (other.structure != null)
			{
				return false;
			}
		}
		else if (!structure.equals(other.structure))
		{
			return false;
		}
		if (title == null)
		{
			if (other.title != null)
			{
				return false;
			}
		}
		else if (!title.equals(other.title))
		{
			return false;
		}
		if (type != other.type)
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(key.toString());
		sb.append(" with title '");
		sb.append(title);
		sb.append("', label '");
		sb.append(label);
		sb.append("', version label '");
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
		sb.append(assignedModules.size());
		sb.append(" assigned modules, ");
		sb.append(freeTextDefs.size());
		sb.append(" free text fields, ");
		sb.append(mappedConsentTemplates.size());
		sb.append(" mapped consent templates, ");
		sb.append(mappedRevocationTemplates.size());
		sb.append(" mapped consent templates, ");
		sb.append(mappedRefusalTemplates.size());
		sb.append(" mapped refusal templates, ");
		sb.append(" created at ");
		sb.append(creationDate);
		sb.append(" last update at ");
		sb.append(updateDate);
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(super.toString());
		return sb.toString();
	}
}
