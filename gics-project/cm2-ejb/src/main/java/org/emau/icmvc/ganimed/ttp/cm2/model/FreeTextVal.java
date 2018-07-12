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
import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;

/**
 * value eines freitext-feldes zu einem consent
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "free_text_val")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class FreeTextVal implements Serializable {

	private static final long serialVersionUID = 3651651839454193611L;
	@EmbeddedId
	private FreeTextValKey key;
	@Column(columnDefinition = "text")
	private String value;
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION"),
			@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "CONSENT_VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID") })
	@MapsId("consentKey")
	private Consent consent;

	public FreeTextVal() {
	}

	public FreeTextVal(Consent consent, String freeTextName, String value, FreeTextDef freeTextDef) throws InvalidFreeTextException {
		super();
		this.key = new FreeTextValKey(consent.getKey(), freeTextName);
		try {
			switch (freeTextDef.getType()) {
				case Date:
					SimpleDateFormat df = new SimpleDateFormat(freeTextDef.getConverterString());
					df.parse(value);
				break;
				case Double:
					Double.valueOf(value);
				break;
				case String:
				break;
				case Integer:
					Integer.valueOf(value);
				case Boolean:
					Boolean.valueOf(value);
				break;
				default:
				break;
			}
		} catch (Exception e) {
			throw new InvalidFreeTextException("invalid value '" + value + "' for free text with name '" + freeTextName + "'", e);
		}
		this.value = value;
		this.consent = consent;
	}

	public FreeTextValKey getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public Consent getConsent() {
		return consent;
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
		FreeTextVal other = (FreeTextVal) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with value '");
		sb.append(value);
		sb.append("'");
		return sb.toString();
	}
}
