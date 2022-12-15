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

/**
 * signerIdType (z.b. mpi-id, fallnummer)
 *
 * @author geidell
 *
 */
public class SignerIdTypeDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = 1465299769748771122L;
	private String name;
	private Date createTimestamp;
	private Date updateTimestamp;
	private String label;
	private String comment;

	public SignerIdTypeDTO()
	{
		super(null);
	}

	public SignerIdTypeDTO(String name)
	{
		super(null);
		this.name = name;
	}

	public SignerIdTypeDTO(String name, Date createTimestamp, Date updateTimestamp, String label, String comment, String fhirID)
	{
		super(fhirID);
		this.name = name;
		this.createTimestamp = createTimestamp;
		this.updateTimestamp = updateTimestamp;
		this.label = label;
		this.comment = comment;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Date getCreateTimestamp()
	{
		return createTimestamp;
	}

	public void setCreateTimestamp(Date createTimestamp)
	{
		this.createTimestamp = createTimestamp;
	}

	public Date getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp)
	{
		this.updateTimestamp = updateTimestamp;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (createTimestamp == null ? 0 : createTimestamp.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (updateTimestamp == null ? 0 : updateTimestamp.hashCode());
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
		SignerIdTypeDTO other = (SignerIdTypeDTO) obj;
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
		if (createTimestamp == null)
		{
			if (other.createTimestamp != null)
			{
				return false;
			}
		}
		else if (!createTimestamp.equals(other.createTimestamp))
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
		if (updateTimestamp == null)
		{
			if (other.updateTimestamp != null)
			{
				return false;
			}
		}
		else if (!updateTimestamp.equals(other.updateTimestamp))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "SignerIdTypeDTO [name=" + name + ", createTimestamp=" + createTimestamp + ", updateTimestamp=" + updateTimestamp + ", label=" + label + ", comment=" + comment + ". "
				+ super.toString() + "]";
	}
}
