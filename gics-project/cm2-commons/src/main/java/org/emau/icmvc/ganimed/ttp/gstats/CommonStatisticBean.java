package org.emau.icmvc.ganimed.ttp.gstats;

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

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author bialkem
 * 
 *         common bean for statistic data
 * 
 */
public class CommonStatisticBean implements Serializable {

	private static final long serialVersionUID = -5003707952272256896L;

	private final long id;
	private final String entrydate;
	private final Map<String, String> mappedStatValue = new HashMap<String, String>();

	public CommonStatisticBean(long id, String entrydate, Map<String, String> mappedStatValue) {
		this.id = id;
		this.entrydate = entrydate;
		this.mappedStatValue.putAll(mappedStatValue);
	}

	/**
	 * @return id
	 */
	@XmlElement(required = true)
	public long getId() {
		return id;
	}

	/**
	 * @return entrydate
	 */
	@XmlElement(required = true)
	public String getEntrydate() {
		return entrydate;
	}

	/**
	 * @return mappedStatValue
	 */
	@XmlElement(required = true)
	public Map<String, String> getMappedStatValue() {
		Map<String, String> result = new HashMap<String, String>(mappedStatValue);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		CommonStatisticBean other = (CommonStatisticBean) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: '");
		sb.append(id);
		sb.append("',  entrydate: ");
		sb.append(entrydate);
		sb.append(", values:");

		for (Map.Entry<String, String> item : mappedStatValue.entrySet()) {
			sb.append(" " + item.getKey() + "=" + item.getValue());
		}

		return sb.toString();
	}
}
