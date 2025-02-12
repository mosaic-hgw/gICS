package org.emau.icmvc.ganimed.ttp.cm2.config.qc;

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
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QCType", propOrder = {
		"id",
		"status",
		"labels"
})
public class QCType extends LabeledId
{
	@Serial
	private static final long serialVersionUID = 6120307622398124535L;

	private static final Pattern QC_TYPE_ID_PATTERN = Pattern.compile("[A-Za-z_#][A-Za-z0-9_#\\-.]*");

	@XmlAttribute(name = "status")
	private QCTypeStatus status;

	/**
	 * Empty constructor. Should be interpreted as the configured default qc type.
	 */
	public QCType()
	{
		this(null, null);
	}

	/**
	 * Copy constructor
	 * @param qcType the qc type to copy
	 */
	public QCType(QCType qcType)
	{
		this(qcType.getId(), qcType.getLabels(), qcType.getStatus());
	}

	public QCType(String id, QCTypeStatus status)
	{
		this(id, null, status);
	}

	public QCType(String id, Map<String, String> labels, QCTypeStatus status)
	{
		super(id);
		this.status = status;
		setLabels(labels);
	}

	public QCType(String id, boolean passed)
	{
		this(id, passed ? QCTypeStatus.VALID : QCTypeStatus.INVALID);
	}

	public QCTypeStatus getStatus()
	{
		return status;
	}

	public void setStatus(QCTypeStatus status)
	{
		this.status = status;
	}

	public boolean isValid()
	{
		return QCTypeStatus.VALID.equals(getStatus());
	}

	public boolean isInvalid()
	{
		return QCTypeStatus.INVALID.equals(getStatus());
	}

	/**
	 * {@return a pattern to check for valid 'xs:NCName' IDs but additionally allowing '#' in the name}.
	 * @see <a href="https://www.data2type.de/xml-xslt-xslfo/xml-schema/datentypen-referenz/xs-ncname/">www.data2type.de/xml-xslt-xslfo/xml-schema/datentypen-referenz/xs-ncname</a>
	 */
	@Override
	public Pattern getValidIdPattern()
	{
		return QC_TYPE_ID_PATTERN;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCType qcType))
			return false;

		return new EqualsBuilder()
				.appendSuper(super.equals(qcType))
				.append(getStatus(), qcType.getStatus())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(getStatus())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("value", getId())
				.append("labels", toLabelString())
				.append("status", status)
				.toString();
	}
}
