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
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DomainPropertiesObject;

/**
 * entity fuer die qc history tabelle;
 * </p>
 * ist als read-only gedacht, geschrieben wird diese automatisch ueber {@Link QCHistory}, zugriff ist darueber aber nicht moeglich
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "qc_hist")
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_QC_HIST")
public class QCHist implements Serializable, FhirDTOExporter<QCHistoryDTO>
{
	private static final long serialVersionUID = 3839907692182837143L;
	@EmbeddedId
	private QCHistKey key;
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
	@MapsId
	@Column(name = "start_Date")
	private Timestamp startDate;
	@Column(name = "end_Date")
	private Timestamp endDate;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION"),
			@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID")
	})
	@MapsId
	private Consent consent;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_QC_HIST")
	private String fhirID;

	public QCHist()
	{}

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

	public QCHistKey getKey()
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
	public QCHistoryDTO toDTO()
	{
		return new QCHistoryDTO(qcPassed, type, timestamp, inspector, comment, externProperties, startDate, endDate, fhirID);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (consent == null ? 0 : consent.hashCode());
		result = prime * result + (endDate == null ? 0 : endDate.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (fhirID == null ? 0 : fhirID.hashCode());
		result = prime * result + (inspector == null ? 0 : inspector.hashCode());
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (qcPassed ? 1231 : 1237);
		result = prime * result + (startDate == null ? 0 : startDate.hashCode());
		result = prime * result + (timestamp == null ? 0 : timestamp.hashCode());
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
		QCHist other = (QCHist) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (consent == null)
		{
			if (other.consent != null)
			{
				return false;
			}
		}
		else if (!consent.equals(other.consent))
		{
			return false;
		}
		if (endDate == null)
		{
			if (other.endDate != null)
			{
				return false;
			}
		}
		else if (!endDate.equals(other.endDate))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (fhirID == null)
		{
			if (other.fhirID != null)
			{
				return false;
			}
		}
		else if (!fhirID.equals(other.fhirID))
		{
			return false;
		}
		if (inspector == null)
		{
			if (other.inspector != null)
			{
				return false;
			}
		}
		else if (!inspector.equals(other.inspector))
		{
			return false;
		}
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		if (qcPassed != other.qcPassed)
		{
			return false;
		}
		if (startDate == null)
		{
			if (other.startDate != null)
			{
				return false;
			}
		}
		else if (!startDate.equals(other.startDate))
		{
			return false;
		}
		if (timestamp == null)
		{
			if (other.timestamp != null)
			{
				return false;
			}
		}
		else if (!timestamp.equals(other.timestamp))
		{
			return false;
		}
		if (type == null)
		{
			if (other.type != null)
			{
				return false;
			}
		}
		else if (!type.equals(other.type))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "QCHist [key=" + key + ", qcPassed=" + qcPassed + ", type=" + type + ", inspector=" + inspector + ", timestamp=" + timestamp + ", comment=" + comment + ", externProperties="
				+ externProperties + ", startDate=" + startDate + ", endDate=" + endDate + ", consent=" + consent + ", fhirID=" + fhirID + "]";
	}
}
