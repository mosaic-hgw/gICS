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

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.magic.fhir.datatypes.LabeledId;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentQCType")
public class ConsentQCType extends LabeledId
{
	@Serial
	private static final long serialVersionUID = 4171210824817045087L;

	@Child(name = "status", order = 0)
	@Description(shortDefinition = "the status of the qc type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt status = new StringDt();

	public ConsentQCType()
	{
	}

	public ConsentQCType(QCType type)
	{
		setId(type != null ? type.getId() : null);
		setLabels(type != null ? type.getLabels() : null);
		setStatus(type != null ? type.getStatus() : null);
	}

	public QCType toQCType()
	{
		return new QCType(getId(), getLabels(), getStatus());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(id, labels, status);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentQCType deepCopy()
	{
		ConsentQCType action = new ConsentQCType();
		copyValues(action);
		action.capture(this);
		return action;
	}

	public void capture(ConsentQCType action)
	{
		if (action == null)
		{
			action = new ConsentQCType();
		}
		setId(action.getId());
		setLabels(action.getLabels());
		setStatus(action.getStatus());
	}

	public QCTypeStatus getStatus()
	{
		String value = status.getValue();
		return value != null ? QCTypeStatus.valueOf(value) : null;
	}

	public void setStatus(QCTypeStatus status)
	{
		this.status.setValue(status != null ? status.name() : null);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentQCType that))
			return false;

		return new EqualsBuilder()
				.append(getId(), that.getId())
				.append(getLabels(), that.getLabels())
				.append(getStatus(), that.getStatus())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getId())
				.append(getLabels())
				.append(getStatus())
				.toHashCode();
	}

	@Override public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("id", getId())
				.append("status", getStatus())
				.append("labels", getLabels())
				.toString();
	}
}
