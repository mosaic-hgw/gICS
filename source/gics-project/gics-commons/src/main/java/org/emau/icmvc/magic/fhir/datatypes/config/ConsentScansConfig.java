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
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.ScansConfig;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentScansConfig")
public class ConsentScansConfig extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = -984179299018619299L;
	private static final boolean DEFAULT_MANDATORY = false;

	@Child(name = "mandatory", order = 0)
	@Description(shortDefinition = "if true then scans are mandatory ")
	private BooleanDt mandatory = new BooleanDt(DEFAULT_MANDATORY);
	@Child(name = "sizeLimit", order = 1)
	@Description(shortDefinition = "size limit for scans in bytes")
	private IntegerDt sizeLimit = new IntegerDt(ScansConfig.DEFAULT_SIZE_LIMIT);

	public ConsentScansConfig()
	{
	}

	public ConsentScansConfig(ScansConfig config)
	{
		if (config == null)
		{
			config = new ScansConfig();
		}
		setMandatory(config.isMandatory());
		setSizeLimit(config.getSizeLimit());
	}

	public ScansConfig toScansConfig()
	{
		return new ScansConfig(isMandatory(), getSizeLimit());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(mandatory, sizeLimit);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentScansConfig deepCopy()
	{
		ConsentScansConfig config = new ConsentScansConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentScansConfig config)
	{
		if (config == null)
		{
			config = new ConsentScansConfig();
		}
		setMandatory(config.isMandatory());
		setSizeLimit(config.getSizeLimit());
	}

	public boolean isMandatory()
	{
		if (mandatory == null)
		{
			mandatory = new BooleanDt(DEFAULT_MANDATORY);
		}
		return mandatory.getValue();
	}

	public void setMandatory(boolean mandatory)
	{
		this.mandatory.setValue(mandatory);
	}


	public int getSizeLimit()
	{
		if (sizeLimit == null)
		{
			sizeLimit = new IntegerDt(ScansConfig.DEFAULT_SIZE_LIMIT);
		}
		return sizeLimit.getValue();
	}

	public void setSizeLimit(int sizeLimit)
	{
		this.sizeLimit.setValue(sizeLimit);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentScansConfig that))
			return false;

		return new EqualsBuilder()
				.append(isMandatory(), that.isMandatory())
				.append(getSizeLimit(), that.getSizeLimit())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(isMandatory())
				.append(getSizeLimit())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("mandatory", isMandatory())
				.append("sizeLimit", getSizeLimit())
				.toString();
	}
}
