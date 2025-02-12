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
@XmlType(name = "StatisticConfig", propOrder = {
		"calculateDocumentDetails",
		"calculatePolicyDetails"
})
public class StatisticConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = -1228642058307038353L;

	@XmlAttribute(name = "calculate-document-details")
	private boolean calculateDocumentDetails;
	@XmlAttribute(name = "calculate-policy-details")
	private boolean calculatePolicyDetails;

	/**
	 * Empty constructor for deserialization.
	 */
	public StatisticConfig()
	{
	}

	/**
	 * Copy constructor.
	 */
	public StatisticConfig(StatisticConfig config)
	{
		capture(config);
	}

	/**
	 * All fields constructor.
	 */
	public StatisticConfig(boolean calculateDocumentDetails, boolean calculatePolicyDetails)
	{
		setCalculateDocumentDetails(calculateDocumentDetails);
		setCalculatePolicyDetails(calculatePolicyDetails);
	}

	public void capture(StatisticConfig config)
	{
		setCalculateDocumentDetails(config != null && config.isCalculateDocumentDetails());
		setCalculatePolicyDetails(config != null && config.isCalculatePolicyDetails());
	}

	public boolean isCalculateDocumentDetails()
	{
		return calculateDocumentDetails;
	}

	public void setCalculateDocumentDetails(boolean calculateDocumentDetails)
	{
		this.calculateDocumentDetails = calculateDocumentDetails;
	}

	public boolean isCalculatePolicyDetails()
	{
		return calculatePolicyDetails;
	}

	public void setCalculatePolicyDetails(boolean calculatePolicyDetails)
	{
		this.calculatePolicyDetails = calculatePolicyDetails;
	}

	/**
	 * Updates all statistics flags from the given config.
	 * @param config the config to update from
	 * @return true if the config changed on update
	 */
	public boolean updateUnlockedParts(StatisticConfig config)
	{
		StatisticConfig sc = new StatisticConfig(this);
		capture(config);
		return !equals(sc);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof StatisticConfig that))
			return false;

		return new EqualsBuilder()
				.append(isCalculateDocumentDetails(), that.isCalculateDocumentDetails())
				.append(isCalculatePolicyDetails(), that.isCalculatePolicyDetails())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(isCalculateDocumentDetails())
				.append(isCalculatePolicyDetails())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("calculateDocumentDetails", calculateDocumentDetails)
				.append("calculatePolicyDetails", calculatePolicyDetails)
				.toString();
	}
}
