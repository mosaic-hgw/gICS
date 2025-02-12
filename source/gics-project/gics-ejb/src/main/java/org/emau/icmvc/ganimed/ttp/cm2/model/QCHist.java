package org.emau.icmvc.ganimed.ttp.cm2.model;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 * 
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12911-022-02081-4
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

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

/**
 * entity fuer die qc history tabelle;
 * </p>
 * ist als read-only gedacht, geschrieben wird diese automatisch ueber {@link QCHistCustomizer}, zugriff ist darueber aber nicht moeglich
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
	@Serial
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
	@JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME")
	@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME")
	@JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION")
	@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE")
	@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID")
	@MapsId("qcKey")
	private QC qc;

	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_QC_HIST")
	private String fhirID;

	public QCHist()
	{
		// for deserialization
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void evaluateQCPassed()
	{
		this.qcPassed = qc.getConfig().hasType(type, QCTypeStatus.VALID);
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
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

	public QC getQc()
	{
		return qc;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	@Override
	public QCHistoryDTO toDTO()
	{
		return new QCHistoryDTO(isQcPassed(), type, timestamp, inspector, comment, externProperties, startDate, endDate, fhirID);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCHist qcHist))
			return false;

		return new EqualsBuilder()
				.append(isQcPassed(), qcHist.isQcPassed())
				.append(getKey(), qcHist.getKey())
				.append(getType(), qcHist.getType())
				.append(getInspector(), qcHist.getInspector())
				.append(getTimestamp(), qcHist.getTimestamp())
				.append(getComment(), qcHist.getComment())
				.append(getExternProperties(), qcHist.getExternProperties())
				.append(startDate, qcHist.startDate)
				.append(endDate, qcHist.endDate)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getKey())
				.append(isQcPassed())
				.append(getType())
				.append(getInspector())
				.append(getTimestamp())
				.append(getComment())
				.append(getExternProperties())
				.append(startDate)
				.append(endDate)
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("key", key)
				.append("qcPassed", qcPassed)
				.append("type", type)
				.append("inspector", inspector)
				.append("timestamp", timestamp)
				.append("comment", comment)
				.append("externProperties", externProperties)
				.append("startDate", startDate)
				.append("endDate", endDate)
				.append("qc", qc)
				.append("fhirID", fhirID)
				.toString();
	}
}
