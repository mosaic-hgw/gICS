package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatus;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

public class QCProblemDTO extends FhirIdDTO implements Serializable
{
	@Serial
	private static final long serialVersionUID = 5112377040583190788L;

	public record TypeRef(String type, String ref)
	{
		public TypeRef(String type, String ref)
		{
			this.type = type != null ? type : "";
			this.ref = ref != null ? ref : "";
		}
	}

	private final String type;
	private QCProblemStatus status;
	private Date createdAt;
	private Date updatedAt;
	private String formValue;
	private String scanValue;
	private String commentExtern;
	private String commentIntern;
	private String ref;

	private transient TypeRef typeRef;

	public QCProblemDTO()
	{
		super(null);
		type = null;
	}

	/**
	 * Copy constructor
	 * @param dto the qc problem to copy
	 */
	public QCProblemDTO(QCProblemDTO dto)
	{
		this(dto.getType(), dto.getStatus(), dto.getCreatedAt(), dto.getUpdatedAt(), dto.getFormValue(), dto.getScanValue(),
				dto.getCommentExtern(), dto.getCommentIntern(), dto.getRef(), dto.getFhirID());
	}

	public QCProblemDTO(String type, QCProblemStatus status)
	{
		super(null);
		this.type = type;
		this.status = status;
	}

	public QCProblemDTO(String type, QCProblemStatus status, Date createdAt, Date updatedAt, String formValue, String scanValue,
			String commentExtern, String commentIntern, String ref, String fhirId)
	{
		super(null);
		this.type = type;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.formValue = formValue;
		this.scanValue = scanValue;
		this.commentExtern = commentExtern;
		this.commentIntern = commentIntern;
		this.ref = ref;
		setFhirID(fhirId);
	}

	public String getType()
	{
		return type;
	}

	public QCProblemType getType(QualityControlConfig config)
	{
		return config.getProblemTypeById(getType());
	}

	public QCProblemStatus getStatus()
	{
		return status;
	}

	public void setStatus(QCProblemStatus status)
	{
		this.status = status;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt != null ?  new Date(createdAt.getTime()) : null;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt != null ? new Date(updatedAt.getTime()) : null;
	}

	public String getFormValue()
	{
		return formValue;
	}

	public void setFormValue(String formValue)
	{
		this.formValue = formValue;
	}

	public String getScanValue()
	{
		return scanValue;
	}

	public void setScanValue(String scanValue)
	{
		this.scanValue = scanValue;
	}

	public String getCommentExtern()
	{
		return commentExtern;
	}

	public void setCommentExtern(String commentExtern)
	{
		this.commentExtern = commentExtern;
	}

	public String getCommentIntern()
	{
		return commentIntern;
	}

	public void setCommentIntern(String commentIntern)
	{
		this.commentIntern = commentIntern;
	}

	public String getRef()
	{
		return ref;
	}

	public void setRef(String ref)
	{
		this.ref = ref;
	}

	public TypeRef getTypeRef()
	{
		if (typeRef == null)
		{
			typeRef = new TypeRef(getType(), ref);
		}
		return typeRef;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCProblemDTO that))
			return false;

		return new EqualsBuilder()
				.append(getType(), that.getType())
				.append(getStatus(), that.getStatus())
				.append(getCreatedAt(), that.getCreatedAt())
				.append(getUpdatedAt(), that.getUpdatedAt())
				.append(getFormValue(), that.getFormValue())
				.append(getScanValue(), that.getScanValue())
				.append(getCommentExtern(), that.getCommentExtern())
				.append(getCommentIntern(), that.getCommentIntern())
				.append(getRef(), that.getRef())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getType())
				.append(getStatus())
				.append(getCreatedAt())
				.append(getUpdatedAt())
				.append(getFormValue())
				.append(getScanValue())
				.append(getCommentExtern())
				.append(getCommentIntern())
				.append(getRef())
				.toHashCode();
	}

	@Override public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("type", type)
				.append("status", status)
				.append("createdAt", createdAt)
				.append("updatedAt", updatedAt)
				.append("formValue", formValue)
				.append("scanValue", scanValue)
				.append("commentExtern", commentExtern)
				.append("commentIntern", commentIntern)
				.append("ref", ref)
				.toString();
	}

	public QCProblemStatus[] getAvailableStatus()
	{
		return QCProblemStatus.values();
	}
}
