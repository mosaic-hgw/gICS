package org.emau.icmvc.ganimed.ttp.cm2.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PolicyValidityChangedDTO implements Serializable
{
	private PolicyDTO policy;
	private Set<Date> changedDates;

	public PolicyValidityChangedDTO()
	{
	}

	public PolicyValidityChangedDTO(PolicyDTO policy, Set<Date> changedDates)
	{
		this.policy = policy;
		this.changedDates = changedDates;
	}

	public PolicyDTO getPolicy()
	{
		return policy;
	}

	public void setPolicy(PolicyDTO policy)
	{
		this.policy = policy;
	}

	public Set<Date> getChangedDates()
	{
		return changedDates;
	}

	public void setChangedDates(Set<Date> changedDates)
	{
		this.changedDates = changedDates;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PolicyValidityChangedDTO that = (PolicyValidityChangedDTO) o;
		return Objects.equals(policy, that.policy) && Objects.equals(changedDates, that.changedDates);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(policy, changedDates);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("policy", getPolicy())
				.append("changedDates", getChangedDates())
				.toString();
	}
}
