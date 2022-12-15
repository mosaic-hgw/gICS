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
import java.sql.Timestamp;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.Customizer;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DomainPropertiesObject;

/**
 * qualitaetskontrolleintrag; kann ueberschrieben werden (-> automatische history)<br>
 * aktueller wert von "qc_passed" wird benutzt, um nur valide consente zu betrachten<br>
 * eintrag wird fuer jeden consent angelegt, gueltige werte fuer "type" werden in der domainconfig
 * konfiguriert<br>
 * ist dort nichts konfiguriert, wird automatisch ein eintrag mit "qc_passed" = true angelegt
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "qc")
// automatic history
@Customizer(org.emau.icmvc.ganimed.ttp.cm2.model.QCHistory.class)
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_QC")
public class QC implements Serializable, FhirDTOExporter<QCDTO>
{
	private static final long serialVersionUID = -1121221171114223597L;
	@EmbeddedId
	private ConsentKey key;
	@Transient
	private boolean qcPassed;
	@Column(length = 100)
	private String type;
	@Column(length = 100)
	private String inspector;
	private Timestamp timestamp;
	@Column(length = 4095)
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION"),
			@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID")
	})
	@MapsId
	private Consent consent;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_QC")
	private String fhirID;

	public QC()
	{}

	public QC(Consent consent, QCDTO qcDTO, Timestamp date, DomainPropertiesObject domainProperties) throws InvalidParameterException
	{
		super();
		key = consent.getKey();
		update(qcDTO, date, domainProperties);
		this.consent = consent;
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void evaluateQCPassed()
	{
		this.qcPassed = consent.getConsentTemplate().getDomain().getPropertiesObject().getValidQcTypes().contains(type);
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public void evaluateQCPassed(DomainPropertiesObject domainProperties)
	{
		this.qcPassed = domainProperties.getValidQcTypes().contains(type);
	}

	public ConsentKey getKey()
	{
		return key;
	}

	public boolean isQcPassed()
	{
		return qcPassed;
	}

	public String getType()
	{
		return type;
	}

	public String getInspector()
	{
		return inspector;
	}

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public Consent getConsent()
	{
		return consent;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	@Override
	public QCDTO toDTO()
	{
		return new QCDTO(qcPassed, type, timestamp, inspector, comment, externProperties, fhirID);
	}

	public void update(QCDTO qcDTO, Timestamp timestamp, DomainPropertiesObject domainProperties) throws InvalidParameterException
	{
		this.timestamp = timestamp;
		type = qcDTO.getType() != null ? qcDTO.getType() : domainProperties.getDefaultQcType();
		if (!domainProperties.getValidQcTypes().contains(type) && !domainProperties.getInvalidQcTypes().contains(type))
		{
			throw new InvalidParameterException("The given qc type (" + type + ") is not know within this domain");
		}
		this.qcPassed = domainProperties.getValidQcTypes().contains(type);
		inspector = qcDTO.getInspector();
		comment = qcDTO.getComment();
		externProperties = qcDTO.getExternProperties();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (consent == null ? 0 : consent.hashCode());
		result = prime * result + (timestamp == null ? 0 : timestamp.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (inspector == null ? 0 : inspector.hashCode());
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (qcPassed ? 1231 : 1237);
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		QC other = (QC) obj;
		if (!Objects.equals(comment, other.comment))
		{
			return false;
		}
		if (!Objects.equals(consent, other.consent))
		{
			return false;
		}
		if (!Objects.equals(timestamp, other.timestamp))
		{
			return false;
		}
		if (!Objects.equals(externProperties, other.externProperties))
		{
			return false;
		}
		if (!Objects.equals(inspector, other.inspector))
		{
			return false;
		}
		if (!Objects.equals(key, other.key))
		{
			return false;
		}
		if (qcPassed != other.qcPassed)
		{
			return false;
		}
		if (!Objects.equals(type, other.type))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "QC [key=" + key + ", qcPassed=" + qcPassed + ", type=" + type + ", inspector=" + inspector + ", timestamp=" + timestamp + ", comment=" + comment + ", externProperties="
				+ externProperties + ", consent=" + consent + ", fhirID=" + fhirID + "]";
	}
}
