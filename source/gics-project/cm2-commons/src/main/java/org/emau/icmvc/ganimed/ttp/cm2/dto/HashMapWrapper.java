package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
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

/**
 * wrapper class, because jaxb can't handle hashmaps - https://wiki.kuali.org/display/STUDENTDOC/5.2+Using+JAXB+Objects+in+Web+Services :<br>
 * Unfortunately, as of 2.1, this processing is only defined for bean properties and not when you marshal HashMap as a top-level object (such as a value in JAXBElement.) In such case, HashMap will be
 * treated as a Java bean, and when you look at HashMap _as a bean it defines no getter/setter property pair... This issue has been recorded as_ #223 and the fix needs to happen in later versions of
 * the JAXB spec.[https://jaxb.dev.java.net/guide/Mapping_your_favorite_class.html]<br>
 * a simple solution is this wrapper class (http://stackoverflow.com/questions/13782797/jax-ws-exception-return-is-not-a-valid-property)
 * 
 * @author geidell
 * 
 */
public class HashMapWrapper<K, V> implements Serializable {

	private static final long serialVersionUID = -7187508614583233675L;
	// nein, nicht final - wird vom jboss beim deserialiseren per reflection (setter) gesetzt
	private HashMap<K, V> map;

	public HashMapWrapper() {
		map = new HashMap<K, V>();
	}

	public HashMapWrapper(HashMap<K, V> map) {
		super();
		if (map != null) {
			this.map = map;
		} else {
			this.map = new HashMap<K, V>();
		}
	}

	public HashMap<K, V> getMap() {
		return map;
	}

	public void setMap(HashMap<K, V> map) {
		if (map != null) {
			this.map = map;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		@SuppressWarnings("unchecked")
		HashMapWrapper<K, V> other = (HashMapWrapper<K, V>) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
}
