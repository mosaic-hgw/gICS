package org.emau.icmvc.ganimed.ttp.gstats.ejb;

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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.emau.icmvc.ganimed.ttp.gstats.CommonStatisticBean;

/**
 * 
 * @author bialkem
 * 
 *         annotated entity bean
 */
@Entity
@Table(name = "stat_entry")
public class Statistic implements Serializable {

	private static final long serialVersionUID = -6608236990956392196L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long stat_entry_id;

	@Column
	private String entrydate;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "stat_value", joinColumns = { @JoinColumn(name = "stat_value_id") })
	@MapKeyColumn(name = "stat_attr", length = 50)
	@Column(name = "stat_value")
	private Map<String, String> mappedStatValue = new HashMap<String, String>();

	public Statistic() {
	}

	public Statistic(long id, String entrydate, Map<String, String> mappedStatValue) {
		this.entrydate = entrydate;
		this.mappedStatValue = mappedStatValue;
	}

	public Statistic(CommonStatisticBean csb) {
		this.entrydate = csb.getEntrydate();
		this.mappedStatValue = csb.getMappedStatValue();
	}

	public long getId() {
		return stat_entry_id;
	}

	public void setId(int id) {
		this.stat_entry_id = id;
	}

	public String getEntrydate() {
		return entrydate;
	}

	public void setEntrydate(String entrydate) {
		this.entrydate = entrydate;
	}

	public Map<String, String> getMappedStatValue() {
		return mappedStatValue;
	}

	public void setMappedStatValue(Map<String, String> mappedStatValue) {
		this.mappedStatValue = mappedStatValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (stat_entry_id ^ (stat_entry_id >>> 32));
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
		Statistic other = (Statistic) obj;
		if (stat_entry_id != other.stat_entry_id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: '");
		sb.append(stat_entry_id);
		sb.append("',  entrydate: ");
		sb.append(entrydate);
		sb.append(", values:");

		for (Map.Entry<String, String> item : mappedStatValue.entrySet()) {
			sb.append(" " + item.getKey() + "=" + item.getValue());
		}

		return sb.toString();
	}

	public CommonStatisticBean convertToCommonStatisticBean() {
		CommonStatisticBean csb = new CommonStatisticBean(stat_entry_id, entrydate, mappedStatValue);
		return csb;
	}
}
