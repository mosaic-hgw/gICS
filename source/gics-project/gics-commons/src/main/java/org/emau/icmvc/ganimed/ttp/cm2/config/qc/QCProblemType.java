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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QCProblemType", propOrder = {
		"id",
		"error",
		"field",
		"occurrence",
		"action",
		"labels"
})
public class QCProblemType extends LabeledId
{
	@Serial
	private static final long serialVersionUID = 6993210090462872602L;

	@XmlAttribute(name = "error")
    private QCProblemTypeError error;
	@XmlAttribute(name = "field")
    private QCProblemTypeField field;
	@XmlAttribute(name = "occurrence")
    private QCProblemTypeOccurrence occurrence;
	@XmlIDREF
	@XmlAttribute(name = "action")
    private QCProblemTypeAction action;

	/**
	 * Empty constructor for deserialization.
	 */
	public QCProblemType()
	{
	}

	/**
	 * Copy constructor
	 * @param problemType the qc problem type to copy
	 */
	public QCProblemType(QCProblemType problemType)
	{
		super(problemType);
		capture(problemType);
	}

	public QCProblemType(String id)
	{
		super(id);
	}

	public QCProblemType(String id, QCProblemTypeError error, QCProblemTypeField field, QCProblemTypeOccurrence occurrence, QCProblemTypeAction action, Map<String, String> labels)
	{
		super(id, labels);
		setError(error);
		setField(field);
		setOccurrence(occurrence);
		setAction(action);
	}

	public void capture(QCProblemType problemType)
	{
		if (problemType == null)
		{
			problemType = new QCProblemType();
		}

		setError(problemType.getError());
		setField(problemType.getField());
		setOccurrence(problemType.getOccurrence());
		setAction(problemType.getAction());
	}

	public QCProblemTypeError getError()
	{
		return error;
	}

	public void setError(QCProblemTypeError error)
	{
		this.error = error;
	}

	public QCProblemTypeField getField()
	{
		return field;
	}

	public void setField(QCProblemTypeField field)
	{
		this.field = field;
	}

	public QCProblemTypeOccurrence getOccurrence()
	{
		return occurrence;
	}

	public void setOccurrence(QCProblemTypeOccurrence occurrence)
	{
		this.occurrence = occurrence;
	}

	public QCProblemTypeAction getAction()
	{
		return action;
	}

	/**
	 * Setter for the problem type's action.
	 * Actions set here also must be present in the set of actions
	 * as provided by {@link QualityControlConfig#getProblemTypeActions()}.
	 * @param action the problem type's action.
	 */
	public void setAction(QCProblemTypeAction action)
	{
		this.action = action;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCProblemType that))
			return false;

		return new EqualsBuilder()
				.appendSuper(super.equals(that))
				.append(getError(), that.getError())
				.append(getField(), that.getField())
				.append(getOccurrence(), that.getOccurrence())
				.append(getAction(), that.getAction())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(getError())
				.append(getField())
				.append(getOccurrence())
				.append(getAction())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("id", getId())
				.append("labels", toLabelString())
				.append("error", error)
				.append("field", field)
				.append("occurrence", occurrence)
				.append("action", action)
				.toString();
	}
}
