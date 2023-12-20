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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdTypeDTO;

/**
 * type fuer die signer id
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "signer_id_type")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_SIDT")
public class SignerIdType implements Serializable, FhirDTOExporter<SignerIdTypeDTO>
{
	private static final long serialVersionUID = 6552647182920215146L;
	@EmbeddedId
	private SignerIdTypeKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "LABEL", length = 255)
	private String label;
	private String comment;
	@Column(name = "ORDER_NUMBER", nullable = false)
	private int orderNumber;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_SIDT")
	private String fhirID;

	// keine explizite relation zu SignerId

	public SignerIdType()
	{}

	public SignerIdType(Domain domain, String name)
	{
		this.key = new SignerIdTypeKey(domain.getName(), name);
		this.domain = domain;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		this.createTimestamp = timestamp;
		this.updateTimestamp = timestamp;
		this.orderNumber = 0;
	}

	public SignerIdType(Domain domain, int orderNumber, String name)
	{
		this(domain, name);
		this.orderNumber = orderNumber;
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public SignerIdTypeKey getKey()
	{
		return key;
	}

	public Domain getDomain()
	{
		return domain;
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

	public String getComment()
	{
		return comment;
	}

	public int getOrderNumber()
	{
		return orderNumber;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void update(String label, String comment)
	{
		this.label = label;
		this.comment = comment;
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	@Override
	public SignerIdTypeDTO toDTO()
	{
		return new SignerIdTypeDTO(key.getName(), new Date(createTimestamp.getTime()), new Date(updateTimestamp.getTime()), label, comment, fhirID);
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
		SignerIdType other = (SignerIdType) obj;
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
		return key + ". FHIR-ID: " + fhirID;
	}
}
