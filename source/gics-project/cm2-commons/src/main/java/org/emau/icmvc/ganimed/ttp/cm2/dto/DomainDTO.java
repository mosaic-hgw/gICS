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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * name - identifier<br/>
 * label - label for the web-interface<br>
 * properties - semicolon separated key-value pairs; for valid keys see enum {@link DomainProperties}<br/>
 * comment - comment<br/>
 *
 * @author geidell
 *
 */
public class DomainDTO extends FhirIdDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = 7811630137389142377L;
	private String name;
	private String label;
	private String ctVersionConverter;
	private transient VersionConverter ctVersionConverterInstance;
	private String moduleVersionConverter;
	private transient VersionConverter moduleVersionConverterInstance;
	private String policyVersionConverter;
	private transient VersionConverter policyVersionConverterInstance;
	private String properties;
	private String comment;
	private String externProperties;
	private String logo;
	private final List<String> signerIdTypes = new ArrayList<>();
	private Date creationDate;
	private Date updateDate;
	private boolean finalised;
	private ExpirationPropertiesDTO expirationProperties = new ExpirationPropertiesDTO();

	public DomainDTO()
	{
		super(null);
	}

	public DomainDTO(String name, String ctVersionConverter, String moduleVersionConverter, String policyVersionConverter,
			List<String> signerIdTypes)
	{
		super(null);
		setName(name);
		this.ctVersionConverter = ctVersionConverter;
		this.moduleVersionConverter = moduleVersionConverter;
		this.policyVersionConverter = policyVersionConverter;
		setSignerIdTypes(signerIdTypes);
	}

	public DomainDTO(String name, String label, String ctVersionConverter, String moduleVersionConverter, String policyVersionConverter,
			String properties, String comment, String externProperties, String logo, List<String> signerIdTypes, boolean finalised, Date creationDate,
			Date updateDate, ExpirationPropertiesDTO expirationProperties, String fhirID)
	{
		super(fhirID);
		setName(name);
		this.label = label;
		this.ctVersionConverter = ctVersionConverter;
		this.moduleVersionConverter = moduleVersionConverter;
		this.policyVersionConverter = policyVersionConverter;
		this.properties = properties;
		this.comment = comment;
		this.externProperties = externProperties;
		this.logo = logo;
		setSignerIdTypes(signerIdTypes);
		this.finalised = finalised;
		setCreationDate(creationDate);
		setUpdateDate(updateDate);
		setExpirationProperties(expirationProperties);
	}

	public DomainDTO(DomainDTO dto)
	{
		this(dto.getName(), dto.getLabel(), dto.getCtVersionConverter(), dto.getModuleVersionConverter(), dto.getPolicyVersionConverter(), dto.getProperties(),
				dto.getComment(), dto.getExternProperties(), dto.getLogo(), dto.getSignerIdTypes(), dto.getFinalised(), dto.getCreationDate(),
				dto.getUpdateDate(), dto.getExpirationProperties(), dto.getFhirID());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name != null ? name.intern() : null;
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
		return StringUtils.isNotEmpty(label) ? label : name;
	}

	public String getCtVersionConverter()
	{
		return ctVersionConverter;
	}

	public void setCtVersionConverter(String ctVersionConverter)
	{
		this.ctVersionConverter = ctVersionConverter;
		ctVersionConverterInstance = null;
	}

	public VersionConverter getCtVersionConverterInstance()
	{
		if (ctVersionConverterInstance == null)
		{
			ctVersionConverterInstance = createVersionConverter(getCtVersionConverter());
		}
		return ctVersionConverterInstance;
	}

	public String getModuleVersionConverter()
	{
		return moduleVersionConverter;
	}

	public void setModuleVersionConverter(String moduleVersionConverter)
	{
		this.moduleVersionConverter = moduleVersionConverter;
		moduleVersionConverterInstance = null;
	}

	public VersionConverter getModuleVersionConverterInstance()
	{
		if (moduleVersionConverterInstance == null)
		{
			moduleVersionConverterInstance = createVersionConverter(getModuleVersionConverter());
		}
		return moduleVersionConverterInstance;
	}

	public String getPolicyVersionConverter()
	{
		return policyVersionConverter;
	}

	public void setPolicyVersionConverter(String policyVersionConverter)
	{
		this.policyVersionConverter = policyVersionConverter;
		policyVersionConverterInstance = null;
	}

	public VersionConverter getPolicyVersionConverterInstance()
	{
		if (policyVersionConverterInstance == null)
		{
			policyVersionConverterInstance = createVersionConverter(getPolicyVersionConverter());
		}
		return policyVersionConverterInstance;
	}

	private VersionConverter createVersionConverter(String className)
	{
		try
		{
			Class<? extends VersionConverter> versionConverterClass = Class.forName(className).asSubclass(VersionConverter.class);
			return versionConverterClass.getDeclaredConstructor().newInstance();
		}
		catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
		{
			return null;
		}
	}

	public String getProperty(String key)
	{
		// check if key is valid
		boolean found = false;
		for (DomainProperties prop : DomainProperties.values())
		{
			if (key.equals(prop.toString()))
			{
				found = true;
				break;
			}
		}

		// check if key-value-pair is found in config
		if (found)
		{
			for (String pair : properties.split(";"))
			{
				if (pair.contains(key))
				{
					return pair.split("=")[1].trim();
				}
			}
		}
		return null;
	}

	public String getProperties()
	{
		return properties;
	}

	public void setProperties(String properties)
	{
		this.properties = properties;
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

	public String getLogo()
	{
		return logo;
	}

	public void setLogo(String logo)
	{
		this.logo = logo;
	}

	public List<String> getSignerIdTypes()
	{
		return signerIdTypes;
	}

	public void setSignerIdTypes(List<String> signerIdTypes)
	{
		if (this.signerIdTypes != signerIdTypes)
		{
			this.signerIdTypes.clear();
			if (signerIdTypes != null)
			{
				this.signerIdTypes.addAll(signerIdTypes);
			}
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

	public boolean getFinalised()
	{
		return finalised;
	}

	public void setFinalised(boolean finalised)
	{
		this.finalised = finalised;
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

	public List<String> getValidQcTypes()
	{
		if (!StringUtils.isEmpty(getProperty("VALID_QC_TYPES")))
		{
			return Stream.of(getProperty("VALID_QC_TYPES").split(","))
					.map(String::trim)
					.map(s -> s.replace("[", "")) // dieser Fall sollte jetzt nicht mehr auftreten @blumentritta
					.map(s -> s.replace("]", ""))
					.filter(StringUtils::isNotEmpty)
					.collect(Collectors.toList());
		}
		else
		{
			return new ArrayList<>();
		}
	}

	public List<String> getInvalidQcTypes()
	{
		if (!StringUtils.isEmpty(getProperty("INVALID_QC_TYPES")))
		{
			return Stream.of(getProperty("INVALID_QC_TYPES").split(","))
					.map(String::trim)
					.map(s -> s.replace("[", "")) // dieser Fall sollte jetzt nicht mehr auftreten @blumentritta
					.map(s -> s.replace("]", ""))
					.filter(StringUtils::isNotEmpty)
					.collect(Collectors.toList());
		}
		else
		{
			return new ArrayList<>();
		}
	}

	@Override
	public String getDomainName()
	{
		return getName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (ctVersionConverter == null ? 0 : ctVersionConverter.hashCode());
		result = prime * result + (expirationProperties == null ? 0 : expirationProperties.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (finalised ? 1231 : 1237);
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (logo == null ? 0 : logo.hashCode());
		result = prime * result + (moduleVersionConverter == null ? 0 : moduleVersionConverter.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (policyVersionConverter == null ? 0 : policyVersionConverter.hashCode());
		result = prime * result + (properties == null ? 0 : properties.hashCode());
		result = prime * result + (signerIdTypes == null ? 0 : signerIdTypes.hashCode());
		result = prime * result + (updateDate == null ? 0 : updateDate.hashCode());
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
		DomainDTO other = (DomainDTO) obj;
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
		if (ctVersionConverter == null)
		{
			if (other.ctVersionConverter != null)
			{
				return false;
			}
		}
		else if (!ctVersionConverter.equals(other.ctVersionConverter))
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
		if (logo == null)
		{
			if (other.logo != null)
			{
				return false;
			}
		}
		else if (!logo.equals(other.logo))
		{
			return false;
		}
		if (moduleVersionConverter == null)
		{
			if (other.moduleVersionConverter != null)
			{
				return false;
			}
		}
		else if (!moduleVersionConverter.equals(other.moduleVersionConverter))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		if (policyVersionConverter == null)
		{
			if (other.policyVersionConverter != null)
			{
				return false;
			}
		}
		else if (!policyVersionConverter.equals(other.policyVersionConverter))
		{
			return false;
		}
		if (properties == null)
		{
			if (other.properties != null)
			{
				return false;
			}
		}
		else if (!properties.equals(other.properties))
		{
			return false;
		}
		if (signerIdTypes == null)
		{
			if (other.signerIdTypes != null)
			{
				return false;
			}
		}
		else if (!signerIdTypes.equals(other.signerIdTypes))
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
		DomainDTO other = (DomainDTO) obj;
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
		if (ctVersionConverter == null)
		{
			if (other.ctVersionConverter != null)
			{
				return false;
			}
		}
		else if (!ctVersionConverter.equals(other.ctVersionConverter))
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
		if (logo == null)
		{
			if (other.logo != null)
			{
				return false;
			}
		}
		else if (!logo.equals(other.logo))
		{
			return false;
		}
		if (moduleVersionConverter == null)
		{
			if (other.moduleVersionConverter != null)
			{
				return false;
			}
		}
		else if (!moduleVersionConverter.equals(other.moduleVersionConverter))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		if (policyVersionConverter == null)
		{
			if (other.policyVersionConverter != null)
			{
				return false;
			}
		}
		else if (!policyVersionConverter.equals(other.policyVersionConverter))
		{
			return false;
		}
		if (properties == null)
		{
			if (other.properties != null)
			{
				return false;
			}
		}
		else if (!properties.equals(other.properties))
		{
			return false;
		}
		if (signerIdTypes == null)
		{
			if (other.signerIdTypes != null)
			{
				return false;
			}
		}
		else if (!signerIdTypes.equals(other.signerIdTypes))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
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
		sb.append("', ");
		sb.append(expirationProperties);
		sb.append(", extern properties '");
		sb.append(externProperties);
		sb.append("', the following properties: '");
		sb.append(properties);
		sb.append(logo != null && !logo.isEmpty() ? ", a logo and " : ", no logo and ");
		sb.append(signerIdTypes.size());
		sb.append(" signer id types. created at ");
		sb.append(creationDate);
		sb.append(" last update at ");
		sb.append(updateDate);
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(super.toString());
		return sb.toString();
	}
}
