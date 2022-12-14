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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

/**
 * objekt fuer die n-m tabelle consent virtual person <-> signer_id, wird benoetigt, um beim consent-cache-fuellen direkt auf die tabelle zugreifen zu koennen, ohne andere objekte zu laden
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "virtual_person_signer_id")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class VirtualPersonSignerId implements Serializable
{
	private static final long serialVersionUID = 1402653992545176745L;
	@EmbeddedId
	private VirtualPersonSignerIdKey key;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "VP_ID", referencedColumnName = "ID") })
	@MapsId("vpId")
	private VirtualPerson virtualPerson;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "SIT_NAME", referencedColumnName = "SIT_NAME"),
			@JoinColumn(name = "SIT_DOMAIN_NAME", referencedColumnName = "SIT_DOMAIN_NAME"),
			@JoinColumn(name = "SI_VALUE", referencedColumnName = "VALUE") })
	@MapsId("signerIdKey")
	private SignerId signerId;

	public VirtualPersonSignerId()
	{}

	public VirtualPersonSignerId(VirtualPerson virtualPerson, SignerId signerId)
	{
		super();
		this.key = new VirtualPersonSignerIdKey(virtualPerson.getId(), signerId.getKey());
		this.signerId = signerId;
		this.virtualPerson = virtualPerson;
	}

	public VirtualPersonSignerIdKey getKey()
	{
		return key;
	}

	public VirtualPerson getVirtualPerson()
	{
		return virtualPerson;
	}

	public SignerId getSignerId()
	{
		return signerId;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualPersonSignerId other = (VirtualPersonSignerId) obj;
		if (key == null)
		{
			if (other.key != null)
				return false;
		}
		else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return key.toString();
	}
}
