package org.emau.icmvc.ganimed.ttp.cm2.util;

public class SystemKeys
{
	private SystemKeys()
	{
		throw new IllegalStateException("Utility class");
	}

	public static final String SYSPROP_CHECK_POLICY_CHANGED_AT_TIME = "ttp.gics.check_policy_validity_changed_at_time";
	public static final String SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES = "ttp.gics.check_policy_validity_changed_period_minutes";
}
