package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
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
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
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

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;

/**
 * definition fuer freitext-felder eines consents
 *
 * @author geidell
 *
 */
public class FreeTextDefDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = 4356138714412527980L;
	private String name;
	private boolean required;
	private FreeTextType type;
	private String converterString;
	private int pos;
	private String comment;
	private Date creationDate;
	private Date updateDate;
	private boolean finalised;
	private String externProperties;
	private String label;

	public FreeTextDefDTO()
	{
		super(null);
	}

	public FreeTextDefDTO(String name, boolean required, FreeTextType type, String converterString, int pos, String comment, boolean finalised,
			Date creationDate, Date updateDate, String fhirID, String externProperties, String label)
	{
		super(fhirID);
		this.name = name;
		this.required = required;
		this.type = type;
		this.converterString = converterString;
		this.pos = pos;
		this.comment = comment;
		this.finalised = finalised;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.externProperties = externProperties;
		this.label = label;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean getRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	public FreeTextType getType()
	{
		return type;
	}

	public void setType(FreeTextType type)
	{
		this.type = type;
	}

	public String getConverterString()
	{
		return converterString;
	}

	public void setConverterString(String converterString)
	{
		this.converterString = converterString;
	}

	public int getPos()
	{
		return pos;
	}

	public void setPos(int pos)
	{
		this.pos = pos;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	public void setFinalised(boolean finalised)
	{
		this.finalised = finalised;
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

	public Date getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate(Date updateDate)
	{
		this.updateDate = updateDate;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	public String getLabel()
	{
		return label;
	}

	/**
	 * Returns the label if not empty or the name otherwise
	 *
	 * @return "Label" or "Name" if Label is empty
	 */
	public String getLabelOrName()
	{
		return StringUtils.isNotEmpty(label) ? label : name;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (converterString == null ? 0 : converterString.hashCode());
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (finalised ? 1231 : 1237);
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + pos;
		result = prime * result + (required ? 1231 : 1237);
		result = prime * result + (type == null ? 0 : type.hashCode());
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
		FreeTextDefDTO other = (FreeTextDefDTO) obj;
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
		if (converterString == null)
		{
			if (other.converterString != null)
			{
				return false;
			}
		}
		else if (!converterString.equals(other.converterString))
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
		if (pos != other.pos)
		{
			return false;
		}
		if (required != other.required)
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
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("FreeTextDTO with name '");
		sb.append(name);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("' type '");
		sb.append(type == null ? "null" : type.toString());
		sb.append("' converter string '");
		sb.append(converterString);
		sb.append("' at pos ");
		sb.append(pos);
		sb.append(" which value is ");
		sb.append(required ? "" : "not ");
		sb.append("required within a consent");
		sb.append(" created at ");
		sb.append(creationDate);
		sb.append(" last update at ");
		sb.append(updateDate);
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(", extern properties: ");
		sb.append(externProperties);
		sb.append(", label: ");
		sb.append(label);
		sb.append(super.toString());
		return sb.toString();
	}
}
