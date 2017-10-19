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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * zusammengesetzter primaerschluessel fuer eine freitext definition
 * 
 * @author geidell
 * 
 */
@Embeddable
public class FreeTextDefKey implements Serializable {

	private static final long serialVersionUID = 5901404136967632656L;
	@Column(insertable = false, updatable = false)
	private ConsentTemplateKey consentTemplateKey;
	// eclipselink (oder jpa?) kommt nicht damit klar, dass es das feld "name" schon mal in domain gibt (ist ueber den consent template key verknuepft)
	@Column(name = "FREETEXT_NAME")
	private String name;

	public FreeTextDefKey() {
	}

	public FreeTextDefKey(ConsentTemplateKey consentTemplateKey, String name) {
		this.consentTemplateKey = consentTemplateKey;
		this.name = name;
	}

	public ConsentTemplateKey getConsentTemplateKey() {
		return consentTemplateKey;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consentTemplateKey == null) ? 0 : consentTemplateKey.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FreeTextDefKey other = (FreeTextDefKey) obj;
		if (consentTemplateKey == null) {
			if (other.consentTemplateKey != null)
				return false;
		} else if (!consentTemplateKey.equals(other.consentTemplateKey))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("free text definition '");
		sb.append(name);
		sb.append("' for ");
		sb.append(consentTemplateKey);
		return sb.toString();
	}
}
