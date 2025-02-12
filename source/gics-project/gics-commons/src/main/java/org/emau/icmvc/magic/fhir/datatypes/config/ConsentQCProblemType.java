package org.emau.icmvc.magic.fhir.datatypes.config;

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
import java.util.Objects;
import java.util.Set;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeAction;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeError;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeField;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeOccurrence;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.magic.fhir.datatypes.LabeledId;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentQCProblemType")
public class ConsentQCProblemType extends LabeledId
{
	@Serial
	private static final long serialVersionUID = 2009244762084204683L;

	@Child(name = "error", order = 0)
	@Description(shortDefinition = "the error of the QC problem type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt error = new StringDt();
	@Child(name = "field", order = 1)
	@Description(shortDefinition = "the field of the QC problem type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt field = new StringDt();
	@Child(name = "occurrence", order = 2)
	@Description(shortDefinition = "the occurrence of the QC problem type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt occurrence = new StringDt();
	@Child(name = "action", order = 3)
	@Description(shortDefinition = "the id of the action for the QC problem type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt actionId = new StringDt();

	public ConsentQCProblemType()
	{
	}

	public ConsentQCProblemType(QCProblemType type)
	{
		if (type == null)
		{
			type = new QCProblemType();
		}
		setId(type.getId());
		setLabels(type.getLabels());
		setError(type.getError());
		setField(type.getField());
		setOccurrence(type.getOccurrence());
		setActionId(type.getAction().getId());
	}

	public QCProblemType toQCProblemType(Set<ConsentQCProblemTypeAction> actions) throws InvalidExchangeFormatException
	{
		String actionId = getActionId();
		QCProblemTypeAction action = actions.stream()
				.filter(a -> Objects.equals(a.getId(), actionId))
				.map(ConsentQCProblemTypeAction::toQCProblemTypeAction).findFirst().orElse(null);
		if (action == null && StringUtils.isNotBlank(actionId))
		{
			throw new InvalidExchangeFormatException("No QC problem type action found for ID '" + actionId + "' in " + actions);
		}
		return new QCProblemType(getId(), getError(), getField(), getOccurrence(), action, getLabels());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(id, labels, error, field, occurrence, actionId);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentQCProblemType deepCopy()
	{
		ConsentQCProblemType type = new ConsentQCProblemType();
		copyValues(type);
		type.capture(this);
		return type;
	}

	public void capture(ConsentQCProblemType type)
	{
		if (type == null)
		{
			type = new ConsentQCProblemType();
		}
		setId(type.getId());
		setLabels(type.getLabels());
		setError(type.getError());
		setField(type.getField());
		setOccurrence(type.getOccurrence());
		setActionId(type.getActionId());
	}

	public QCProblemTypeError getError()
	{
		String value = error.getValue();
		return value != null ? QCProblemTypeError.valueOf(value) : null;
	}

	public void setError(QCProblemTypeError error)
	{
		this.error.setValue(error != null ? error.name() : null);
	}

	public QCProblemTypeField getField()
	{
		String value = field.getValue();
		return value != null ? QCProblemTypeField.valueOf(value) : null;
	}

	public void setField(QCProblemTypeField field)
	{
		this.field.setValue(field != null ? field.name() : null);
	}

	public QCProblemTypeOccurrence getOccurrence()
	{
		String value = occurrence.getValue();
		return value != null ? QCProblemTypeOccurrence.valueOf(value) : null;
	}

	public void setOccurrence(QCProblemTypeOccurrence occurrence)
	{
		this.occurrence.setValue(occurrence != null ? occurrence.name() : null);
	}

	public String getActionId()
	{
		return actionId.getValue();
	}

	public void setActionId(String actionId)
	{
		this.actionId.setValue(actionId);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentQCProblemType type))
			return false;

		return new EqualsBuilder()
				.append(getId(), type.getId())
				.append(getLabels(), type.getLabels())
				.append(getError(), type.getError())
				.append(getField(), type.getField())
				.append(getOccurrence(), type.getOccurrence())
				.append(getActionId(), type.getActionId())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getId())
				.append(getLabels())
				.append(getError())
				.append(getField())
				.append(getOccurrence())
				.append(getActionId())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("id", getId())
				.append("labels", getLabels())
				.append("error", getError())
				.append("field", getField())
				.append("occurrence", getOccurrence())
				.append("actionId", getActionId())
				.toString();
	}
}
