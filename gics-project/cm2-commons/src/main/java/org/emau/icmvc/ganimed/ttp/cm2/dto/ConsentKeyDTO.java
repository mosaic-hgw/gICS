package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * pk fuer einen consent
 * 
 * @author geidell
 * 
 */
public class ConsentKeyDTO implements Serializable {

	private static final long serialVersionUID = 3488915902092845220L;
	private ConsentTemplateKeyDTO consentTemplateKey;
	private Set<SignerIdDTO> signerIds = new HashSet<SignerIdDTO>();
	private Date consentDate;

	public ConsentKeyDTO() {
	}

	public ConsentKeyDTO(ConsentTemplateKeyDTO consentTemplateKey, Set<SignerIdDTO> signerIds, Date consentDate) {
		super();
		this.consentTemplateKey = consentTemplateKey;
		if (signerIds != null) {
			this.signerIds = signerIds;
		}
		this.consentDate = consentDate;
	}

	public ConsentTemplateKeyDTO getConsentTemplateKey() {
		return consentTemplateKey;
	}

	public void setConsentTemplateKey(ConsentTemplateKeyDTO consentTemplateKey) {
		this.consentTemplateKey = consentTemplateKey;
	}

	public Set<SignerIdDTO> getSignerIds() {
		return signerIds;
	}

	public void setSignerIds(Set<SignerIdDTO> signerIds) {
		if (signerIds != null) {
			this.signerIds = signerIds;
		}
	}

	public Date getConsentDate() {
		return consentDate;
	}

	public void setConsentDate(Date consentDate) {
		this.consentDate = consentDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consentDate == null) ? 0 : consentDate.hashCode());
		result = prime * result + ((consentTemplateKey == null) ? 0 : consentTemplateKey.hashCode());
		result = prime * result + ((signerIds == null) ? 0 : signerIds.hashCode());
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
		ConsentKeyDTO other = (ConsentKeyDTO) obj;
		if (consentDate == null) {
			if (other.consentDate != null)
				return false;
		} else if (!consentDate.equals(other.consentDate))
			return false;
		if (consentTemplateKey == null) {
			if (other.consentTemplateKey != null)
				return false;
		} else if (!consentTemplateKey.equals(other.consentTemplateKey))
			return false;
		if (signerIds == null) {
			if (other.signerIds != null)
				return false;
		} else if (!signerIds.equals(other.signerIds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("signed consent for ");
		sb.append(consentTemplateKey);
		sb.append(" signed by [");
		for (SignerIdDTO signerId : signerIds) {
			sb.append(signerId.toString() + " ");
		}
		sb.append("] at ");
		sb.append(consentDate);
		return sb.toString();
	}
}
