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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

/**
 * zusammenfassung mehrerer fremd-ids, eine reale person kann mehreren virtuellen zugeordnet sein<br>
 * wird nur benoetig, da die id einer person teil des keys eines consents ist
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "virtual_person")
@TableGenerator(name = "virtual_person_index", initialValue = 0, allocationSize = 50)
@Cache(isolation = CacheIsolationType.ISOLATED)
public class VirtualPerson implements Serializable {

	private static final long serialVersionUID = 6543506658553490391L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "virtual_person_index")
	private Long id;
	@OneToMany(mappedBy = "virtualPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<Consent> consents = new ArrayList<Consent>();
	@OneToMany(mappedBy = "virtualPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<VirtualPersonSignerId> virtualPersonSignerIds = new ArrayList<VirtualPersonSignerId>();

	public VirtualPerson() {
		super();
		// consents: henne-ei-problem, relation muss bei create consent gesetzt werden!
		// virtualPersonSignerIds: henne-ei-problem, relation muss bei create virtual person gesetzt werden!
	}

	public List<Consent> getConsents() {
		return consents;
	}

	public Long getId() {
		return id;
	}

	public List<VirtualPersonSignerId> getVirtualPersonSignerIds() {
		return virtualPersonSignerIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consents == null) ? 0 : consents.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((virtualPersonSignerIds == null) ? 0 : virtualPersonSignerIds.hashCode());
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
		VirtualPerson other = (VirtualPerson) obj;
		if (consents == null) {
			if (other.consents != null)
				return false;
		} else if (!consents.equals(other.consents))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (virtualPersonSignerIds == null) {
			if (other.virtualPersonSignerIds != null)
				return false;
		} else if (!virtualPersonSignerIds.equals(other.virtualPersonSignerIds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("virtual person with the following ids:\n");
		for (VirtualPersonSignerId virtualPersonSignerId : virtualPersonSignerIds) {
			sb.append(virtualPersonSignerId + "\n");
		}
		return sb.toString();
	}
}
