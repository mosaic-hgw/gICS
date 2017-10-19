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
 * zusammengesetzter primaerschluessel fuer eine signer id
 * 
 * @author geidell
 * 
 */
@Embeddable
public class SignerIdKey implements Serializable {

	private static final long serialVersionUID = -5389436135084068888L;
	@Column(insertable = false, updatable = false)
	private SignerIdTypeKey signerIdTypeKey;
	@Column
	private String value;

	public SignerIdKey() {
	}

	public SignerIdKey(SignerIdTypeKey signerIdTypeKey, String value) {
		super();
		this.signerIdTypeKey = signerIdTypeKey;
		this.value = value;
	}

	public SignerIdTypeKey getSignerIdTypeKey() {
		return signerIdTypeKey;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signerIdTypeKey == null) ? 0 : signerIdTypeKey.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SignerIdKey other = (SignerIdKey) obj;
		if (signerIdTypeKey == null) {
			if (other.signerIdTypeKey != null)
				return false;
		} else if (!signerIdTypeKey.equals(other.signerIdTypeKey))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("signer id '");
		sb.append(value);
		sb.append("' for ");
		sb.append(signerIdTypeKey);
		return sb.toString();
	}
}
