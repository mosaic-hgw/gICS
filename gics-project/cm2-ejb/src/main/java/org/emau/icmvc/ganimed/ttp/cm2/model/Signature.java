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
import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;


/**
 * signaturen sind scans der unterschrift (bei ganiforms: png); extra tabelle wegen lob-feld
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "signature")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class Signature implements Serializable {

	private static final long serialVersionUID = -5096660490173122482L;
	@EmbeddedId
	private SignatureKey key;
	@Lob
	private String signatureScanBase64;
	@Temporal(TemporalType.TIMESTAMP)
	private Date signatureDate;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"),
			@JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION"),
			@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "CONSENT_VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID") })
	@MapsId("consentKey")
	private Consent consent;

	public Signature() {
	}

	public Signature(Consent consent, SignatureType type, String signatureScanBase64, Date signatureDate) {
		super();
		this.key = new SignatureKey(consent.getKey(), type);
		this.signatureScanBase64 = signatureScanBase64;
		this.signatureDate = signatureDate;
		this.consent = consent;
	}

	public SignatureKey getKey() {
		return key;
	}

	public String getSignatureScanBase64() {
		return signatureScanBase64;
	}

	public Date getSignatureDate() {
		return signatureDate;
	}

	public Consent getConsent() {
		return consent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((signatureScanBase64 == null) ? 0 : signatureScanBase64.hashCode());
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
		Signature other = (Signature) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (signatureDate == null) {
			if (other.signatureDate != null)
				return false;
		} else if (!signatureDate.equals(other.signatureDate))
			return false;
		if (signatureScanBase64 == null) {
			if (other.signatureScanBase64 != null)
				return false;
		} else if (!signatureScanBase64.equals(other.signatureScanBase64))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return key + " from " + signatureDate;
	}
}
