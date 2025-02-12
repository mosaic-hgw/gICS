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
import org.emau.icmvc.ganimed.ttp.cm2.config.StatisticConfig;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentStatisticConfig")
public class ConsentStatisticConfig extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = 8405278736284612870L;
	private static final boolean DEFAULT_CALCULATE_DOCUMENT_DETAILS = false;
	private static final boolean DEFAULT_CALCULATE_POLICY_DETAILS = false;

	@Child(name = "calculateDocumentDetails", order = 0)
	@Description(shortDefinition = "true to calculate the document details (isExpired, hasDigitalSignature) for statistics (requires iterating over all ConsentLightDTOs)")
	private BooleanDt calculateDocumentDetails = new BooleanDt(DEFAULT_CALCULATE_DOCUMENT_DETAILS);
	@Child(name = "calculatePolicyDetails", order = 1)
	@Description(shortDefinition = "true to calculate the policy details (how many signed policies for each policy and domain) for statistics (requires fetching the policy status for all signers)")
	private BooleanDt calculatePolicyDetails = new BooleanDt(DEFAULT_CALCULATE_POLICY_DETAILS);

	public ConsentStatisticConfig()
	{
	}

	public ConsentStatisticConfig(StatisticConfig config)
	{
		if (config == null)
		{
			config = new StatisticConfig();
		}
		setCalculateDocumentDetails(config.isCalculateDocumentDetails());
		setCalculatePolicyDetails(config.isCalculatePolicyDetails());
	}

	public StatisticConfig toStatisticConfig()
	{
		return new StatisticConfig(isCalculateDocumentDetails(), isCalculatePolicyDetails());
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(calculateDocumentDetails, calculatePolicyDetails);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentStatisticConfig deepCopy()
	{
		ConsentStatisticConfig config = new ConsentStatisticConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentStatisticConfig config)
	{
		if (config == null)
		{
			config = new ConsentStatisticConfig();
		}
		setCalculateDocumentDetails(config.isCalculateDocumentDetails());
		setCalculatePolicyDetails(config.isCalculatePolicyDetails());
	}

	public boolean isCalculateDocumentDetails()
	{
		if (calculateDocumentDetails == null)
		{
			calculateDocumentDetails = new BooleanDt(DEFAULT_CALCULATE_DOCUMENT_DETAILS);
		}
		return calculateDocumentDetails.getValue();
	}

	public void setCalculateDocumentDetails(boolean calculateDocumentDetails)
	{
		this.calculateDocumentDetails.setValue(calculateDocumentDetails);
	}

	public boolean isCalculatePolicyDetails()
	{
		if (calculatePolicyDetails == null)
		{
			calculatePolicyDetails = new BooleanDt(DEFAULT_CALCULATE_POLICY_DETAILS);
		}
		return calculatePolicyDetails.getValue();
	}

	public void setCalculatePolicyDetails(boolean calculatePolicyDetails)
	{
		this.calculatePolicyDetails.setValue(calculatePolicyDetails);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentStatisticConfig that))
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
				.append("calculateDocumentDetails", isCalculateDocumentDetails())
				.append("calculatePolicyDetails", isCalculatePolicyDetails())
				.toString();
	}
}
