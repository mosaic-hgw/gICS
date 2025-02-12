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
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatus;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

/**
 * entity fuer die qc problem history tabelle;
 * </p>
 * ist als read-only gedacht, geschrieben wird diese automatisch ueber {@link QCProblemHistCustomizer}, zugriff ist darueber aber nicht moeglich
 *
 * @author moser
 *
 */
@Entity
@Table(name = "qc_problem_hist")
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_QCP_HIST")
public class QCProblemHist implements Serializable, FhirDTOExporter<QCProblemHistoryDTO>
{
	@Serial
	private static final long serialVersionUID = -3939626247120841398L;
	@EmbeddedId
	private QCProblemHistKey key;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME")
	@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME")
	@JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION")
	@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE")
	@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID")
	@MapsId("qcKey")
	private QC qc;
	@Column(name = "STATUS", length = 100)
	private String status;
	@Column(name = "FORM_VALUE", length = 4095)
	private String formValue;
	@Column(name = "SCAN_VALUE", length = 4095)
	private String scanValue;
	@Column(name = "COMMENT_EXTERN", length = 4095)
	private String commentExtern;
	@Column(name = "COMMENT_INTERN", length = 4095)
	private String commentIntern;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Date createTimestamp;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Date updateTimestamp;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_QCP_HIST")
	private String fhirID;
	@Column(name = "END_DATE")
	private Timestamp endDate;

	public QCProblemHist()
	{
		// for deserialization
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		if (fhirID != null)
		{
			fhirID = fhirID.toLowerCase();
		}
	}

	public QCProblemHistKey getKey()
	{
		return key;
	}

	public QC getQc()
	{
		return qc;
	}

	public Date getStartDate()
	{
		return key.getStartDate();
	}

	public Timestamp getEndDate()
	{
		return endDate;
	}

	public String getType()
	{
		return key.getType();
	}

	public QCProblemStatus getStatus()
	{
		return status != null ? QCProblemStatus.valueOf(status) : null;
	}

	public Date getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Date getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public String getFormValue()
	{
		return formValue;
	}

	public String getScanValue()
	{
		return scanValue;
	}

	public String getCommentExtern()
	{
		return commentExtern;
	}

	public String getCommentIntern()
	{
		return commentIntern;
	}

	public String getRef()
	{
		return key.getRef();
	}

	public QCProblemDTO.TypeRef getTypeRef()
	{
		return key.getTypeRef();
	}

	public String getFhirID()
	{
		return fhirID;
	}

	@Override
	public QCProblemHistoryDTO toDTO()
	{
		return new QCProblemHistoryDTO(
				new QCProblemDTO(getType(), getStatus(), getCreateTimestamp(), getUpdateTimestamp(),
						getFormValue(), getScanValue(), getCommentExtern(), getCommentIntern(), getRef(), getFhirID()),
				getStartDate(), getEndDate());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCProblemHist that))
			return false;

		return new EqualsBuilder()
				.append(getKey(), that.getKey()) // qc_key, type, ref, and start_date
				.append(getEndDate(), that.getEndDate())
				.append(getStatus(), that.getStatus())
				.append(getFormValue(), that.getFormValue())
				.append(getScanValue(), that.getScanValue())
				.append(getCommentExtern(), that.getCommentExtern())
				.append(getCommentIntern(), that.getCommentIntern())
				.append(getCreateTimestamp(), that.getCreateTimestamp())
				.append(getUpdateTimestamp(), that.getUpdateTimestamp())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getKey()) // qc_key, type, ref, and start_date
				.append(getEndDate())
				.append(getStatus())
				.append(getFormValue())
				.append(getScanValue())
				.append(getCommentExtern())
				.append(getCommentIntern())
				.append(getCreateTimestamp())
				.append(getUpdateTimestamp())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("key", key) // qc_key, type, ref, and start_date
				.append("endDate", endDate)
				.append("status", status)
				.append("formValue", formValue)
				.append("scanValue", scanValue)
				.append("commentExtern", commentExtern)
				.append("commentIntern", commentIntern)
				.append("createTimestamp", createTimestamp)
				.append("updateTimestamp", updateTimestamp)
				.append("fhirID", fhirID)
				.toString();
	}
}
