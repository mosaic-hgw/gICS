package org.emau.icmvc.ganimed.ttp.cm2.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;

import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;

/**
 * Methods to calculate special stuff like changedPolicyStatus
 *
 * @author Peter Penndorf
 */
public final class CalcUtils
{
	private CalcUtils()
	{
	}

	public record ChangedSignedPolicyValidity(ConsentCache.CachedSignedPolicy policy, Set<Date> changedDates)
	{
	}

	public static List<ChangedSignedPolicyValidity> getChangedPolicyValidities(List<ConsentCache.CachedSignedPolicy> policies, Date fromDate, Date toDate)
	{
		if (toDate == null || fromDate == null || toDate.getTime() < fromDate.getTime())
			throw new IllegalArgumentException("toDate is less than fromDate or any of these dates is null");

		final long toTime = toDate.getTime();
		final long fromTime = fromDate.getTime();
		Map<PolicyKey, ChangedSignedPolicyValidity> policiesResultMap = new HashMap<>();
		//maybe multi-threaded? right now ~3 Mio/s on i7-13700
		for (ConsentCache.CachedSignedPolicy policy : policies)
		{
			final PolicyKey policyKey = policy.getSPKey().getPolicyKey();
			ChangedSignedPolicyValidity existingPolicyValidity = policiesResultMap.get(policyKey);

			//legalConsentDate
			existingPolicyValidity = checkValidity(toTime, fromTime, policiesResultMap, policy, policy.getLegalConsentDate(), policyKey, existingPolicyValidity);

			//expiration
			checkValidity(toTime, fromTime, policiesResultMap, policy, policy.getConsentExpirationDate(), policyKey, existingPolicyValidity);
		}

		return new ArrayList<>(policiesResultMap.values());
	}

	private static ChangedSignedPolicyValidity checkValidity(long toTime, long fromTime, Map<PolicyKey, ChangedSignedPolicyValidity> map, ConsentCache.CachedSignedPolicy policy, long referenceDate,
			PolicyKey policyKey, ChangedSignedPolicyValidity current)
	{
		if (referenceDate > fromTime && referenceDate < toTime)
		{
			if (current == null)
			{
				final HashSet<Date> dates = new HashSet<>();
				dates.add(new Date(referenceDate));
				final ChangedSignedPolicyValidity policyValidity = new ChangedSignedPolicyValidity(policy, dates);
				map.put(policyKey, policyValidity);
				return policyValidity;
			}
			else
				current.changedDates.add(new Date(referenceDate));
		}
		return current;
	}

	public static Map<SignerId, List<ChangedSignedPolicyValidity>> groupPoliciesBySignerId(List<ChangedSignedPolicyValidity> policies, LongFunction<Set<SignerId>> getSignerIdsForVP,
			boolean removeDuplicates)
	{
		if (getSignerIdsForVP == null)
			throw new IllegalArgumentException("getSignerIdsForVP is null");
		Map<SignerId, List<ChangedSignedPolicyValidity>> result = new HashMap<>();

		for (ChangedSignedPolicyValidity policy : policies)
		{
			Set<SignerId> signerIds = getSignerIdsForVP.apply(policy.policy.getSPKey().getConsentKey().getVirtualPersonId());
			for (SignerId signerId : signerIds)
			{
				result.computeIfAbsent(signerId, k -> new ArrayList<>());
				if (!removeDuplicates || !result.get(signerId).contains(policy))
					result.get(signerId).add(policy);
			}
		}

		return result;
	}
}
