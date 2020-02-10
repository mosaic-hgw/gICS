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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * zusammengesetzter primaerschluessel fuer einen freitext value
 * 
 * @author geidell
 * 
 */
@Embeddable
public class FreeTextValKey implements Serializable {

	private static final long serialVersionUID = -8081224195561155495L;
	@Column(insertable = false, updatable = false)
	private ConsentKey consentKey;
	@Column(name = "FREETEXTDEV_NAME")
	private String freeTextDevName;

	// muesste korrekterweise auch eine verbindung zu FreeTextDev haben - ist dann aber doch zu aufwendig, da dann nicht nur der domain-name (wie z.b. bei SignedPolicy), sondern auch alle
	// ConsentTemplateKey-felder doppelt gehalten werden muessten (trotz identischen inhalts)

	public FreeTextValKey() {
	}

	public FreeTextValKey(ConsentKey consentKey, String freeTextDevName) {
		this.consentKey = consentKey;
		this.freeTextDevName = freeTextDevName;
	}

	public ConsentKey getConsentKey() {
		return consentKey;
	}

	public String getFreeTextDevName() {
		return freeTextDevName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consentKey == null) ? 0 : consentKey.hashCode());
		result = prime * result + ((freeTextDevName == null) ? 0 : freeTextDevName.hashCode());
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
		FreeTextValKey other = (FreeTextValKey) obj;
		if (consentKey == null) {
			if (other.consentKey != null)
				return false;
		} else if (!consentKey.equals(other.consentKey))
			return false;
		if (freeTextDevName == null) {
			if (other.freeTextDevName != null)
				return false;
		} else if (!freeTextDevName.equals(other.freeTextDevName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("value for free text field '");
		sb.append(freeTextDevName);
		sb.append("' for ");
		sb.append(consentKey);
		return sb.toString();
	}
}
