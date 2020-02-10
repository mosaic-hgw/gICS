package org.emau.icmvc.ganimed.ttp.cm2.model;

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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;

/**
 * konkrete signer id
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "signer_id")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class SignerId implements Serializable {

	private static final long serialVersionUID = 4917519470648603456L;
	@EmbeddedId
	private SignerIdKey key;
	@OneToMany(mappedBy = "signerId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<VirtualPersonSignerId> virtualPersonSignerIds = new ArrayList<VirtualPersonSignerId>();
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "SIT_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "SIT_NAME", referencedColumnName = "NAME") })
	@MapsId("signerIdTypeKey")
	private SignerIdType signerIdType;

	public SignerId() {
	}

	public SignerId(SignerIdType signerIdType, String value) {
		super();
		this.key = new SignerIdKey(signerIdType.getKey(), value);
		this.signerIdType = signerIdType;
	}

	public SignerIdKey getKey() {
		return key;
	}

	public List<VirtualPersonSignerId> getVirtualPersonSignerIds() {
		return virtualPersonSignerIds;
	}

	public SignerIdType getSignerIdType() {
		return signerIdType;
	}

	public SignerIdDTO toDTO() {
		SignerIdDTO result = new SignerIdDTO(key.getSignerIdTypeKey().getName(), key.getValue());
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		SignerId other = (SignerId) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
