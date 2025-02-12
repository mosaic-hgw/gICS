package org.emau.icmvc.ganimed.ttp.cm2.config;

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PoliciesConfig", propOrder = {
		"permanentRevoke",
		"takeHighestVersionInsteadOfNewest",
		"takeMostSpecificValidityInsteadOfShortest"
})
public class PoliciesConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = -7889722045272882071L;

	@XmlAttribute(name = "permanent-revoke")
	private boolean permanentRevoke;
	@XmlAttribute(name = "take-highest-version-instead-of-newest")
	private boolean takeHighestVersionInsteadOfNewest;
	@XmlAttribute(name = "take-most-specific-validity-instead-of-shortest")
	private boolean takeMostSpecificValidityInsteadOfShortest;

	/**
	 * Empty constructor for deserialization.
	 */
	public PoliciesConfig()
	{
	}

	/**
	 * Copy constructor.
	 */
	public PoliciesConfig(PoliciesConfig config)
	{
		capture(config);
	}

	/**
	 * All fields constructor.
	 */
	public PoliciesConfig(boolean permanentRevoke, boolean takeHighestVersionInsteadOfNewest, boolean takeMostSpecificValidityInsteadOfShortest)
	{
		setPermanentRevoke(permanentRevoke);
		setTakeHighestVersionInsteadOfNewest(takeHighestVersionInsteadOfNewest);
		setTakeMostSpecificValidityInsteadOfShortest(takeMostSpecificValidityInsteadOfShortest);
	}

	public void capture(PoliciesConfig config)
	{
		setTakeMostSpecificValidityInsteadOfShortest(config != null && config.isTakeMostSpecificValidityInsteadOfShortest());
		setTakeHighestVersionInsteadOfNewest(config != null && config.isTakeHighestVersionInsteadOfNewest());
		setPermanentRevoke(config != null && config.isPermanentRevoke());
	}

	public boolean isPermanentRevoke()
	{
		return permanentRevoke;
	}

	public void setPermanentRevoke(boolean permanentRevoke)
	{
		this.permanentRevoke = permanentRevoke;
	}

	public boolean isTakeHighestVersionInsteadOfNewest()
	{
		return takeHighestVersionInsteadOfNewest;
	}

	public void setTakeHighestVersionInsteadOfNewest(boolean takeHighestVersionInsteadOfNewest)
	{
		this.takeHighestVersionInsteadOfNewest = takeHighestVersionInsteadOfNewest;
	}

	public boolean isTakeMostSpecificValidityInsteadOfShortest()
	{
		return takeMostSpecificValidityInsteadOfShortest;
	}

	public void setTakeMostSpecificValidityInsteadOfShortest(boolean takeMostSpecificValidityInsteadOfShortest)
	{
		this.takeMostSpecificValidityInsteadOfShortest = takeMostSpecificValidityInsteadOfShortest;
	}


	/**
	 * Currently simply ignores the given given config and updates nothing.
	 * @param config the config to update from
	 * @return false
	 */
	public boolean updateUnlockedParts(PoliciesConfig config)
	{
		return false;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof PoliciesConfig that))
			return false;

		return new EqualsBuilder()
				.append(isPermanentRevoke(), that.isPermanentRevoke())
				.append(isTakeHighestVersionInsteadOfNewest(), that.isTakeHighestVersionInsteadOfNewest())
				.append(isTakeMostSpecificValidityInsteadOfShortest(), that.isTakeMostSpecificValidityInsteadOfShortest())
				.isEquals();
	}

	@Override public int hashCode()
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
				.append("permanentRevoke", permanentRevoke)
				.append("takeHighestVersionInsteadOfNewest", takeHighestVersionInsteadOfNewest)
				.append("takeMostSpecificValidityInsteadOfShortest", takeMostSpecificValidityInsteadOfShortest)
				.toString();
	}
}
