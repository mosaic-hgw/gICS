package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
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
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Christopher Hampf
 */
public class KeyValueHelper
{
	/**
	 * key-value pairs
	 */
	private final Map<String, String> keyValues;

	/**
	 * Creates a key-value helper and interprets the given string as key-value-pairs by the given assignment string and
	 * separator. The assignment contains the string which assigns key a value. The separator contains the string which
	 * separates key-value-pairs.
	 *
	 * @param keyValueStr Contains the key-value-pairs
	 * @param assignment  Contains the string used to assign keys and values
	 * @param separator   Contains the string used to separate key-value-pairs
	 * @throws IllegalArgumentException If a given parameter is null
	 */
	public KeyValueHelper(String keyValueStr, String assignment, String separator)
	{
		if (keyValueStr == null)
			throw new IllegalArgumentException("given string for key-value extraction is null");
		if (assignment == null)
			throw new IllegalArgumentException("given assignment string is null");
		if (separator == null)
			throw new IllegalArgumentException("given separator string is null");

		keyValues = getKeyValues(keyValueStr, assignment, separator);
	}

	/**
	 * Creates a key-value helper
	 *
	 * @param keyValues Contains key-values
	 */
	public KeyValueHelper(Map<String, String> keyValues)
	{
		this.keyValues = keyValues == null ? new HashMap<>() : keyValues;
	}

	/**
	 * Creates a key-value helper and interprets the given string as key-value-pairs. Keys and values are assigned by
	 * equals (=). Key-value-pairs are separated by semicolon (;).
	 *
	 * @param keyValueStr Contains the key-value-pairs
	 * @throws IllegalArgumentException if the given key-value string is null
	 */
	public KeyValueHelper(String keyValueStr)
	{
		this(keyValueStr, "=", ";");
	}

	/**
	 * Checks whether a searched key is contained in the key-value list.
	 *
	 * @param searchedKey searched value
	 * @return true, if the list contains at least one pair with the specified key, otherwise false
	 */
	public boolean containsKey(String searchedKey)
	{
		if (searchedKey == null)
			return false;

		return keyValues.containsKey(searchedKey);
	}

	/**
	 * Checks whether a searched value is contained in the key-value list.
	 *
	 * @param searchedValue searched value
	 * @return true, if the list contains at least one pair with the specified value, otherwise false
	 */
	public boolean containsValue(String searchedValue)
	{
		if (searchedValue == null)
			return false;

		return keyValues.containsValue(searchedValue);
	}

	/**
	 * Checks whether a searched key-value pair is contained in the key-value list.
	 *
	 * @param key   searched key
	 * @param value searched value
	 * @return True, if key-value pair is contained. False, if key or value is null or key-value pair is not contained.
	 */
	public boolean hasKeyValuePair(String key, String value)
	{
		String val = keyValues.get(key);

		if (val != null)
			return val.equals(value);
		else
			return false;
	}

	/**
	 * Returns the key with a given value. If multiple keys contain the specified value, the first key found is returned.
	 *
	 * @param searchedValue searched value
	 * @return Founded key, if the list contains at least one pair with the specified value, otherwise NULL
	 */
	public String getKeyByValue(String searchedValue)
	{
		if (searchedValue == null)
			return null;

		for (Map.Entry<String, String> keyVal : keyValues.entrySet())
		{
			if (keyVal.getValue().equals(searchedValue))
				return keyVal.getKey();
		}

		return null;
	}

	/**
	 * Returns the value with a given key. If the specified key is contained several times, the value of the first key
	 * found is returned.
	 *
	 * @param searchedKey searched key
	 * @return Founded value, if the list contains at least one pair with the specified key, otherwise NULL
	 */
	public String getValueByKey(String searchedKey)
	{
		if (searchedKey == null)
			return null;

		return keyValues.get(searchedKey);
	}

	/**
	 * Returns a list of all keys.
	 *
	 * @return List of all keys.
	 */
	public List<String> getKeys()
	{
		return Arrays.asList(keyValues.keySet().toArray(new String[keyValues.size()]));
	}

	/**
	 * Returns the key-value pair with the given key. If the specified key is contained several times, the pair of the
	 * first key found is returned.
	 *
	 * @param searchedKey searched key
	 * @return Founded key-value pair, if the list contains at least one pair with the specified key, otherwise NULL
	 */
	public Pair<String, String> getPairByKey(String searchedKey)
	{
		if (searchedKey == null)
			return null;

		if (keyValues.containsKey(searchedKey))
			return new MutablePair<>(searchedKey, keyValues.get(searchedKey));

		return null;
	}

	/**
	 * Returns the key-value pair with the given value. If the specified value is contained several times, the pair of
	 * the first value found is returned.
	 *
	 * @param searchedValue searched value
	 * @return Founded key-value pair, if the list contains at least one pair with the specified value, otherwise NULL
	 */
	public Pair<String, String> getPairByValue(String searchedValue)
	{
		if (searchedValue == null)
			return null;

		for (Map.Entry<String, String> keyVal : keyValues.entrySet())
		{
			if (keyVal.getValue().equals(searchedValue))
				return new MutablePair<>(keyVal.getKey(), keyVal.getValue());
		}

		return null;
	}

	/**
	 * Returns number of key-value pairs. Duplicates are not filtered.
	 *
	 * @return Number of key-value pairs
	 */
	public int size()
	{
		return keyValues.size();
	}

	/**
	 * Extracts the keys and values from given string. Keys and values are separated by an assignment string.
	 * Several key-value pairs are separated by a separator. Before and after a string, the whitespaces are removed.
	 *
	 * @param str        string with key-value pairs
	 * @param assignment string which separates a key and value
	 * @param separator  string which separates serveral key-value pairs
	 * @return List of key-value pairs
	 */
	private Map<String, String> getKeyValues(String str, String assignment, String separator)
	{
		if (str == null || separator == null || assignment == null)
			return new HashMap<>();

		Map<String, String> result = new HashMap<>();

		String[] keyValuesTmp = str.split(separator);

		for (String keyValue : keyValuesTmp)
		{
			String[] tmp = keyValue.split(assignment);
			if (tmp.length == 2)
				result.put(tmp[0].trim(), tmp[1].trim());
		}

		return result;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		KeyValueHelper that = (KeyValueHelper) o;
		return Objects.equals(keyValues, that.keyValues);
	}

	@Override public int hashCode()
	{
		return Objects.hash(keyValues);
	}

	@Override public String toString()
	{
		return "KeyValueHelper [" +
				"keyValues=" + keyValues +
				']';
	}
}
