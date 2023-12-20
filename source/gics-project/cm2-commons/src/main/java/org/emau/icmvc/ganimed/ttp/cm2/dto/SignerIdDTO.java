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

import java.io.Serializable;
import java.util.Date;

/**
 * signer id - combination of id and id type
 *
 * @author geidell
 *
 */
public class SignerIdDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = 9086303850317869011L;
	private String idType;
	private String id;
	private Date creationDate;
	private int orderNumber;

	public SignerIdDTO()
	{
		super(null);
	}

	public SignerIdDTO(String idType, String id)
	{
		super(null);
		this.idType = idType;
		this.id = id;
	}

	public SignerIdDTO(String idType, String id, Date creationDate, String fhirID)
	{
		super(fhirID);
		this.idType = idType;
		this.id = id;
		setCreationDate(creationDate);
	}

	public SignerIdDTO(String idType, String id, int orderNumber, Date creationDate, String fhirID)
	{
		this(idType, id, creationDate, fhirID);
		this.orderNumber = orderNumber;
	}

	public SignerIdDTO(SignerIdDTO dto)
	{
		this(dto.getIdType(), dto.getId(), dto.getOrderNumber(), dto.getCreationDate(), dto.getFhirID());
	}

	public String getIdType()
	{
		return idType;
	}

	public void setIdType(String idType)
	{
		this.idType = idType;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
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

	public int getOrderNumber()
	{
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber)
	{
		this.orderNumber = orderNumber;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (idType == null ? 0 : idType.hashCode());
		result = prime * result + orderNumber;
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
		SignerIdDTO other = (SignerIdDTO) obj;
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
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}
		if (idType == null)
		{
			if (other.idType != null)
			{
				return false;
			}
		}
		else if (!idType.equals(other.idType))
		{
			return false;
		}
		if (orderNumber != other.orderNumber)
		{
			return false;
		}
		return true;
	}

	public boolean equalsId(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		SignerIdDTO other = (SignerIdDTO) obj;
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}
		if (idType == null)
		{
			if (other.idType != null)
			{
				return false;
			}
		}
		else if (!idType.equals(other.idType))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SignerIdDTO with id '");
		sb.append(id);
		sb.append("' and id type '");
		sb.append(idType);
		sb.append("' and order ");
		sb.append(orderNumber);
		sb.append(" created at ");
		sb.append(creationDate);
		sb.append(super.toString());
		return sb.toString();
	}
}
