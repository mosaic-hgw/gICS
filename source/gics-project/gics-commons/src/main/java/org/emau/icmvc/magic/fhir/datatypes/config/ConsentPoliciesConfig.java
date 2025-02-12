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
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.PoliciesConfig;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentPoliciesConfig")
public class ConsentPoliciesConfig extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = -3025958687514302360L;
	private static final boolean DEFAULT_PERMANENT_REVOKE = false;
	private static final boolean DEFAULT_TAKE_HIGHEST_VERSION = false;
	private static final boolean DEFAULT_TAKE_MOST_SPECIFIC_VALIDITY = false;

	@Child(name = "permanentRevoke", order = 0)
	@Description(shortDefinition = "true to void all signed policies for s single signed policy with the state declines")
	private BooleanDt permanentRevoke = new BooleanDt(false);
	@Child(name = "takeHighestVersionInsteadOfNewest", order = 1)
	@Description(shortDefinition = "true to take the highest version for multiple signed policies (instead of the most recent one)")
	private BooleanDt takeHighestVersionInsteadOfNewest = new BooleanDt(DEFAULT_TAKE_HIGHEST_VERSION);
	@Child(name = "takeMostSpecificValidityInsteadOfShortest", order = 2)
	@Description(shortDefinition = "true to use the shortest possible period of validity or the most specific one otherwise")
	private BooleanDt takeMostSpecificValidityInsteadOfShortest = new BooleanDt(DEFAULT_TAKE_MOST_SPECIFIC_VALIDITY);

	public ConsentPoliciesConfig()
	{
	}

	public ConsentPoliciesConfig(PoliciesConfig config)
	{
		if (config == null)
		{
			config = new PoliciesConfig();
		}
		setPermanentRevoke(config.isPermanentRevoke());
		setTakeHighestVersionInsteadOfNewest(config.isTakeHighestVersionInsteadOfNewest());
		setTakeMostSpecificValidityInsteadOfShortest(config.isTakeMostSpecificValidityInsteadOfShortest());
	}

	public PoliciesConfig toPoliciesConfig()
	{
		return new PoliciesConfig(isPermanentRevoke(), isTakeHighestVersionInsteadOfNewest(), isTakeMostSpecificValidityInsteadOfShortest());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(permanentRevoke, takeHighestVersionInsteadOfNewest, takeMostSpecificValidityInsteadOfShortest);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentPoliciesConfig deepCopy()
	{
		ConsentPoliciesConfig config = new ConsentPoliciesConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentPoliciesConfig config)
	{
		if (config == null)
		{
			config = new ConsentPoliciesConfig();
		}
		setPermanentRevoke(config.isPermanentRevoke());
		setTakeHighestVersionInsteadOfNewest(config.isTakeHighestVersionInsteadOfNewest());
		setTakeMostSpecificValidityInsteadOfShortest(config.isTakeMostSpecificValidityInsteadOfShortest());
	}

	public boolean isPermanentRevoke()
	{
		if (permanentRevoke == null)
		{
			permanentRevoke = new BooleanDt(DEFAULT_PERMANENT_REVOKE);
		}
		return permanentRevoke.getValue();
	}

	public void setPermanentRevoke(boolean permanentRevoke)
	{
		this.permanentRevoke.setValue(permanentRevoke);
	}

	public boolean isTakeHighestVersionInsteadOfNewest()
	{
		if (takeHighestVersionInsteadOfNewest == null)
		{
			takeHighestVersionInsteadOfNewest = new BooleanDt(DEFAULT_TAKE_HIGHEST_VERSION);
		}
		return takeHighestVersionInsteadOfNewest.getValue();
	}

	public void setTakeHighestVersionInsteadOfNewest(boolean takeHighestVersionInsteadOfNewest)
	{
		this.takeHighestVersionInsteadOfNewest.setValue(takeHighestVersionInsteadOfNewest);
	}


	public boolean isTakeMostSpecificValidityInsteadOfShortest()
	{
		if (takeMostSpecificValidityInsteadOfShortest == null)
		{
			takeMostSpecificValidityInsteadOfShortest = new BooleanDt(DEFAULT_TAKE_MOST_SPECIFIC_VALIDITY);
		}
		return takeMostSpecificValidityInsteadOfShortest.getValue();
	}

	public void setTakeMostSpecificValidityInsteadOfShortest(boolean takeMostSpecificValidityInsteadOfShortest)
	{
		this.takeMostSpecificValidityInsteadOfShortest.setValue(takeMostSpecificValidityInsteadOfShortest);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentPoliciesConfig that))
			return false;

		return new EqualsBuilder()
				.append(isPermanentRevoke(), that.isPermanentRevoke())
				.append(isTakeHighestVersionInsteadOfNewest(), that.isTakeHighestVersionInsteadOfNewest())
				.append(isTakeMostSpecificValidityInsteadOfShortest(), that.isTakeMostSpecificValidityInsteadOfShortest())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(isPermanentRevoke())
				.append(isTakeHighestVersionInsteadOfNewest())
				.append(isTakeMostSpecificValidityInsteadOfShortest())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("permanentRevoke", isPermanentRevoke())
				.append("takeHighestVersionInsteadOfNewest", isTakeHighestVersionInsteadOfNewest())
				.append("takeMostSpecificValidityInsteadOfShortest", isTakeMostSpecificValidityInsteadOfShortest())
				.toString();
	}
}
