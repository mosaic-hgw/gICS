package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignedPolicy;

/**
 * @author Christopher Hampf
 */
public class ExternPropertyFilterByOIDs
{
	private static final Logger logger = LogManager.getLogger(ExternPropertyFilterByOIDs.class);

	/**
	 * Static extern properties (all properties for which the policy status need not be checked)
	 */
	private List<StringPair> staticExternProperties;

	/**
	 * Dynamic extern properties (all properties for which the policy status must be checked)
	 */
	private List<StringPair> dynamicExternProperties;

	private final List<Consent> filteredConsents;

	/**
	 * Creates a filter that filters the given consents by the given extern properties. The filter consider the extern properties of:
	 * modules and module consent templates (assigned modules).
	 * Properties are separated into static and dynamic conditions. Static contitions are considered in module extern properties. Dynamic
	 * conditions are considered in module consent templates. Details: {@link ExternPropertyFilterByOIDs#setFilteredExternProperties(List)}
	 *
	 * @param consents             Consents that are filtered
	 * @param moduleSignedPolicies Modules and signed policies that are filtered
	 * @param externProperties     List of key-value pairs of searched extern properties
	 * @param operator             Operator that specifies how the conditions are linked
	 * @throws NotImplementedException If the given operator is not supported
	 */
	public ExternPropertyFilterByOIDs(List<Consent> consents, Map<Module, List<SignedPolicy>> moduleSignedPolicies, List<StringPair> externProperties, Operator operator)
	{
		if (!(operator.equals(Operator.AND) || operator.equals(Operator.OR)))
		{
			String msg = "This operator is not implemented yet";
			logger.fatal(msg);
			throw new NotImplementedException(msg);
		}

		setFilteredExternProperties(externProperties);
		filteredConsents = filterConsentKeysByExternProperties(consents, moduleSignedPolicies, operator);
	}

	/**
	 * Returns the filtered consents that have the searched extern properties.
	 *
	 * @return Filtered consents
	 */
	public List<Consent> getFilteredConsents()
	{
		return filteredConsents;
	}

	/**
	 * Returns a filtered list of the given consents.
	 *
	 * @param consents             Consents that are filtered
	 * @param moduleSignedPolicies Assignment between Modules and assigned signed policies
	 * @param operator             Operator that influences the filter. AND: all conditions have to be fulfilled; OR: at least one conditions have to be fulfilled
	 * @return Filtered list of consents that fulfilled the conditions
	 */
	private List<Consent> filterConsentKeysByExternProperties(List<Consent> consents, Map<Module, List<SignedPolicy>> moduleSignedPolicies, Operator operator)
	{
		List<Consent> result = new ArrayList<>();

		Map<Consent, BitSet> tmp = getFilteredConsentsByExternProperties(consents, moduleSignedPolicies);

		if (operator.equals(Operator.AND))
		{
			int size = staticExternProperties.size() + dynamicExternProperties.size();
			BitSet and = new BitSet(size);
			and.set(0, size);

			for (Map.Entry<Consent, BitSet> e : tmp.entrySet())
			{
				if (e.getValue().equals(and))
					result.add(e.getKey());
			}
		}
		else if (operator.equals(Operator.OR))
		{
			for (Map.Entry<Consent, BitSet> e : tmp.entrySet())
			{
				if (!e.getValue().isEmpty())
					result.add(e.getKey());
			}
		}

		return result;
	}

	/**
	 * Returns a filtered list of the given consents.
	 *
	 * @param consents             Consents that are filtered
	 * @param moduleSignedPolicies Assignment between Modules and assigned signed policies
	 * @return Filtered list of consents that fulfilled the conditions
	 */
	private Map<Consent, BitSet> getFilteredConsentsByExternProperties(List<Consent> consents, Map<Module, List<SignedPolicy>> moduleSignedPolicies)
	{
		// Bitset: each "bit" represents a condition (false: not fulfilled, true: fulfilled)
		// Size of Bitset is the sum of static (x) and dynamic (y) extern properties
		// First x bits are reserved for static conditions and second y bits are reserved for dynamic conditions
		Map<Consent, BitSet> result = new HashMap<>();

		for (Consent c : consents)
		{
			if (!result.containsKey(c))
				result.put(c, new BitSet(staticExternProperties.size() + dynamicExternProperties.size()));
		}

		Map<ModuleConsentTemplate, List<SignedPolicy>> mctsp = new HashMap<>();
		for (Consent c : consents)
		{
			for (ModuleConsentTemplate mct : c.getConsentTemplate().getModuleConsentTemplates())
			{
				for (Map.Entry<Module, List<SignedPolicy>> msp : moduleSignedPolicies.entrySet())
				{
					if (msp.getKey().equals(mct.getModule()))
					{
						for (SignedPolicy sp : msp.getValue())
						{
							if (sp.getConsent().equals(c))
							{
								if (!mctsp.containsKey(mct))
									mctsp.put(mct, new ArrayList<>());

								mctsp.get(mct).add(sp);
							}
						}

						break;
					}
				}
			}
		}

		for (Map.Entry<ModuleConsentTemplate, List<SignedPolicy>> e : mctsp.entrySet())
		{
			Module m = e.getKey().getModule();
			for (int i = 0; i < staticExternProperties.size(); ++i)
			{
				StringPair sep = staticExternProperties.get(i);
				try
				{
					KeyValueHelper kvh = new KeyValueHelper(m.getExternProperties());
					if (kvh.hasKeyValuePair(sep.getKey(), sep.getValue()))
					{
						for (SignedPolicy sp : e.getValue())
						{
							if (result.containsKey(sp.getConsent()))
								result.get(sp.getConsent()).set(i);
						}
					}
				}
				catch (IllegalArgumentException iae)
				{
					logger.warn("Extern properties of module are not in the expected format: " + iae.getMessage());
				}
			}

			for (int i = 0; i < dynamicExternProperties.size(); ++i)
			{
				StringPair dep = dynamicExternProperties.get(i);
				try
				{
					KeyValueHelper kvh = new KeyValueHelper(e.getKey().getExternProperties());

					if (kvh.hasKeyValuePair(dep.getKey(), dep.getValue()))
					{
						for (SignedPolicy sp : e.getValue())
						{
							ConsentStatusType expectedStatus = getExpectedStatus(dep.getKey());
							if (expectedStatus != null && expectedStatus.equals(sp.getStatus().getConsentStatusType()))
							{
								if (result.containsKey(sp.getConsent()))
									result.get(sp.getConsent()).set(staticExternProperties.size() + i);
							}
						}
					}
				}
				catch (IllegalArgumentException iae)
				{
					logger.warn("Extern properties of module consent template (assigned module) are not in the expected format: " + iae.getMessage());
				}
			}
		}

		return result;
	}

	/**
	 * Returns the interpreted ConsentStatusType {@link ConsentStatusType} of given string.
	 * If the given string contains "_yes" at the end, ACCEPTED is returned {@link ConsentStatusType#ACCEPTED}.
	 * If the given string contains "_no" at the end, DECLINED is returned {@link ConsentStatusType#DECLINED}.
	 * If the given string contains "_unknown" at the end, UNKNOWN is returned {@link ConsentStatusType#UNKNOWN}.
	 * Otherwise, NULL is returned.
	 *
	 * @param property Propertyname which is interpreted
	 * @return Interpreted ConsentStatusType {@link ConsentStatusType}
	 */
	private ConsentStatusType getExpectedStatus(String property)
	{
		if (property == null)
			return null;

		ConsentStatusType searchedStatus = null;
		// Find answer in extern property (*_yes, *_no, *_unknown)
		if (property.endsWith("_yes"))
			searchedStatus = ConsentStatusType.ACCEPTED;
		else if (property.endsWith("_no"))
			searchedStatus = ConsentStatusType.DECLINED;
		else if (property.endsWith("_unknown"))
			searchedStatus = ConsentStatusType.UNKNOWN;

		return searchedStatus;
	}

	/**
	 * Checks whether the given string ends with "_yes", "_no" or "_unknown".
	 * The check is case-sensitive.
	 *
	 * @param str String that is checked
	 * @return True if the string ends with one of the above, otherwise false.
	 */
	private boolean isConsentStateRequired(String str)
	{
		return str != null &&
				(
						str.endsWith("_yes") ||
								str.endsWith("_no") ||
								str.endsWith("_unknown")
				);
	}

	/**
	 * Separate the given list of extern properties to static and dynamic properties.
	 * Static properties can evaluated directly, without a check of the consent status.
	 * Dynamic properties depends on the consent status. Therefore it must first be queried and checked,
	 * before this property can be evaluated.
	 *
	 * @param externProperties Extern properties that are separated
	 */
	private void setFilteredExternProperties(List<StringPair> externProperties)
	{
		staticExternProperties = new ArrayList<>();
		dynamicExternProperties = new ArrayList<>();

		if (externProperties != null)
		{
			for (StringPair keyValues : externProperties)
			{
				boolean consentRequired = isConsentStateRequired(keyValues.getKey());

				if (!consentRequired)
					staticExternProperties.add(new StringPair(keyValues.getKey(), keyValues.getValue()));
				else
					dynamicExternProperties.add(new StringPair(keyValues.getKey(), keyValues.getValue()));
			}
		}
	}
}
