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
import java.util.Date;
import java.util.HashSet;
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
public class ConsentKey implements Serializable {

	private static final long serialVersionUID = 5748616991194260088L;
	@Column(insertable = false, updatable = false)
	private ConsentTemplateKey ctKey;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CONSENT_DATE")
	private Date consentDate;
	@Column(name = "VIRTUAL_PERSON_ID")
	private Long virtualPersonId;

	public ConsentKey() {
	}

	public ConsentKey(ConsentTemplate ct, Date consentDate, Long virtualPersonId) {
		super();
		this.ctKey = ct.getKey();
		this.consentDate = consentDate;
		this.virtualPersonId = virtualPersonId;
	}

	public ConsentTemplateKey getCtKey() {
		return ctKey;
	}

	public Date getConsentDate() {
		return consentDate;
	}

	public Long getVirtualPersonId() {
		return virtualPersonId;
	}

	public ConsentKeyDTO toDTO(VersionConverter ctVersionConverter, VirtualPerson signer) throws InvalidVersionException {
		Set<SignerIdDTO> signerIds = new HashSet<SignerIdDTO>();
		for (VirtualPersonSignerId virtualPersonSignerId : signer.getVirtualPersonSignerIds()) {
			signerIds.add(new SignerIdDTO(virtualPersonSignerId.getKey().getSignerIdKey().getSignerIdTypeKey().getName(), virtualPersonSignerId
					.getKey().getSignerIdKey().getValue()));
		}
		return new ConsentKeyDTO(ctKey.toDTO(ctVersionConverter), signerIds, consentDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctKey == null) ? 0 : ctKey.hashCode());
		result = prime * result + ((consentDate == null) ? 0 : consentDate.hashCode());
		result = prime * result + ((virtualPersonId == null) ? 0 : virtualPersonId.hashCode());
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
		ConsentKey other = (ConsentKey) obj;
		if (ctKey == null) {
			if (other.ctKey != null)
				return false;
		} else if (!ctKey.equals(other.ctKey))
			return false;
		if (consentDate == null) {
			if (other.consentDate != null)
				return false;
		} else if (!consentDate.equals(other.consentDate))
			return false;
		if (virtualPersonId == null) {
			if (other.virtualPersonId != null)
				return false;
		} else if (!virtualPersonId.equals(other.virtualPersonId))
			return false;
		return true;
	}

	@Override
	public String toString() {
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
