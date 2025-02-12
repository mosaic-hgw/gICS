package org.emau.icmvc.ganimed.ttp.cm2.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.version.VersionConverter;
import org.emau.icmvc.ttp.notification.NotificationMessage;

/**
 * A class to encapsulate a map with signerIds and there changed policies
 * which can be serialized to and deserialized from a JSON representation.
 */
public class PolicyValidityChangedNotificationMessage extends NotificationMessage
{
	public static final String NOTI_TYPE = "GICS.policyValidityStatusChanged";
	private final List<PolicyValidityChangedSignerId> affectedPolicies = new ArrayList<>(0);
	private String domain;

	/**
	 * Creates an empty notification message (needed by deserialization).
	 */
	public PolicyValidityChangedNotificationMessage()
	{
	}

	public PolicyValidityChangedNotificationMessage(Map<Pair<SignerIdDTO, List<SignerIdDTO>>, List<CalcUtils.ChangedSignedPolicyValidity>> policies, String domain, String clientId)
			throws UnknownDomainException, InvalidVersionException
	{
		super(NOTI_TYPE, clientId, "");
		this.domain = domain;
		VersionConverter policyVersionConverter;
		policyVersionConverter = VersionConverterCache.getPolicyVersionConverter(domain);
		for (Map.Entry<Pair<SignerIdDTO, List<SignerIdDTO>>, List<CalcUtils.ChangedSignedPolicyValidity>> entry : policies.entrySet())
		{
			SignerIdDTO signerId = entry.getKey().getKey();
			List<CalcUtils.ChangedSignedPolicyValidity> policyList = entry.getValue();
			List<ValidityChangedPolicy> result = new ArrayList<>();
			for (CalcUtils.ChangedSignedPolicyValidity policyStatus : policyList)
			{
				result.add(new ValidityChangedPolicy(policyStatus.policy().getName(), policyVersionConverter.intToString(policyStatus.policy().getVersion()),
						policyStatus.changedDates()));
			}
			affectedPolicies.add(
					new PolicyValidityChangedSignerId(signerId.getId(), signerId.getIdType(), entry.getKey().getValue().stream().map(dto -> new AliasSignerId(dto.getId(), dto.getIdType())).toList(), result));
		}
	}

	public List<PolicyValidityChangedSignerId> getAffectedPolicies()
	{
		return affectedPolicies;
	}

	public String getDomain()
	{
		return domain;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		PolicyValidityChangedNotificationMessage that = (PolicyValidityChangedNotificationMessage) o;
		return Objects.equals(affectedPolicies, that.affectedPolicies) && Objects.equals(domain, that.domain);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), affectedPolicies, domain);
	}

	public static final class ValidityChangedPolicy
	{
		private String name;
		private String version;
		private Set<Date> changedDates = new HashSet<>();

		public ValidityChangedPolicy()
		{
		}

		public ValidityChangedPolicy(String name, String version, Set<Date> changedDates)
		{
			this.name = name;
			this.version = version;
			this.changedDates = changedDates;
		}

		public String name()
		{
			return name;
		}

		public String version()
		{
			return version;
		}

		public Set<Date> getChangedDates()
		{
			return changedDates;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ValidityChangedPolicy that = (ValidityChangedPolicy) o;
			return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(changedDates, that.changedDates);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(name, version, changedDates);
		}

		@Override
		public String toString()
		{
			return new ToStringBuilder(this)
					.append("name", name)
					.append("version", version)
					.append("changedDates", getChangedDates())
					.toString();
		}
	}

	public static final class AliasSignerId
	{
		private String signerId;
		private String signerIdType;

		public AliasSignerId()
		{
		}

		public AliasSignerId(String signerId, String signerIdType)
		{
			this.signerId = signerId;
			this.signerIdType = signerIdType;
		}

		public String getSignerId()
		{
			return signerId;
		}

		public void setSignerId(String signerId)
		{
			this.signerId = signerId;
		}

		public String getSignerIdType()
		{
			return signerIdType;
		}

		public void setSignerIdType(String signerIdType)
		{
			this.signerIdType = signerIdType;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AliasSignerId that = (AliasSignerId) o;
			return Objects.equals(signerId, that.signerId) && Objects.equals(signerIdType, that.signerIdType);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(signerId, signerIdType);
		}
	}

	public static final class PolicyValidityChangedSignerId
	{
		private String signerId;
		private String signerIdType;
		private List<AliasSignerId> aliases;
		private List<ValidityChangedPolicy> policies;

		public PolicyValidityChangedSignerId()
		{
		}

		public PolicyValidityChangedSignerId(String signerId, String signerIdType, List<AliasSignerId> aliases, List<ValidityChangedPolicy> policies)
		{
			this.signerId = signerId;
			this.signerIdType = signerIdType;
			this.aliases = aliases;
			this.policies = policies;
		}

		public String signerId()
		{
			return signerId;
		}

		public String signerIdType()
		{
			return signerIdType;
		}

		public List<ValidityChangedPolicy> policies()
		{
			return policies;
		}

		public List<AliasSignerId> getAliases()
		{
			return aliases;
		}

		public void setAliases(List<AliasSignerId> aliases)
		{
			this.aliases = aliases;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			PolicyValidityChangedSignerId that = (PolicyValidityChangedSignerId) o;
			return Objects.equals(signerId, that.signerId) && Objects.equals(signerIdType, that.signerIdType) && Objects.equals(aliases, that.aliases)
				   && Objects.equals(policies, that.policies);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(signerId, signerIdType, aliases, policies);
		}

		@Override
		public String toString()
		{
			return new ToStringBuilder(this)
					.append("signerId", signerId)
					.append("signerIdType", signerIdType)
					.append("aliases", getAliases())
					.append("policies", policies)
					.toString();
		}
	}
}
