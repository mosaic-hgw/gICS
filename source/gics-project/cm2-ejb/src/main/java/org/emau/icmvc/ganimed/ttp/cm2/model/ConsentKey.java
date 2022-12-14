package org.emau.icmvc.ganimed.ttp.cm2.model;

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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;

/**
 * zusammengesetzter primaerschluessel fuer einen consent
 *
 * @author geidell
 *
 */
@Embeddable
public class ConsentKey implements Serializable
{
	private static final long serialVersionUID = 1829459362365503501L;
	@Column(insertable = false, updatable = false)
	private ConsentTemplateKey ctKey;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CONSENT_DATE")
	private Date consentDate;
	@Column(name = "VIRTUAL_PERSON_ID")
	private Long virtualPersonId;

	public ConsentKey()
	{}

	public ConsentKey(ConsentTemplateKey ctKey, Date consentDate, Long virtualPersonId)
	{
		super();
		this.ctKey = ctKey;
		this.consentDate = consentDate;
		this.virtualPersonId = virtualPersonId;
	}

	public ConsentTemplateKey getCtKey()
	{
		return ctKey;
	}

	public Date getConsentDate()
	{
		return consentDate;
	}

	public Long getVirtualPersonId()
	{
		return virtualPersonId;
	}

	public ConsentKeyDTO toDTO(VersionConverter ctVersionConverter, VirtualPerson signer) throws InvalidVersionException
	{
		Set<SignerIdDTO> signerIds = new HashSet<>();
		for (VirtualPersonSignerId virtualPersonSignerId : signer.getVirtualPersonSignerIds())
		{
			signerIds.add(new SignerIdDTO(virtualPersonSignerId.getKey().getSignerIdKey().getSignerIdTypeKey().getName(),
					virtualPersonSignerId.getKey().getSignerIdKey().getValue(), virtualPersonSignerId.getSignerId().getSignerIdType().getOrderNumber(),
					virtualPersonSignerId.getSignerId().getCreateTimestamp(), virtualPersonSignerId.getSignerId().getFhirID()));
		}
		return new ConsentKeyDTO(ctKey.toDTO(ctVersionConverter), signerIds, consentDate);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (ctKey == null ? 0 : ctKey.hashCode());
		result = prime * result + (consentDate == null ? 0 : consentDate.hashCode());
		result = prime * result + (virtualPersonId == null ? 0 : virtualPersonId.hashCode());
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
		ConsentKey other = (ConsentKey) obj;
		if (!Objects.equals(ctKey, other.ctKey))
		{
			return false;
		}
		if (!Objects.equals(consentDate, other.consentDate))
		{
			return false;
		}
		if (!Objects.equals(virtualPersonId, other.virtualPersonId))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("consent for ");
		sb.append(ctKey);
		sb.append(" signed by virtual person with id ");
		sb.append(virtualPersonId);
		sb.append(" at ");
		sb.append(consentDate);
		return sb.toString();
	}
}
