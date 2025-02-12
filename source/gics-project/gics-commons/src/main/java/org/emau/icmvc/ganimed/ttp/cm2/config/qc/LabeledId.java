package org.emau.icmvc.ganimed.ttp.cm2.config.qc;

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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient // to allow to declare the propOrder in the subclasses
public class LabeledId implements Serializable
{
	@Serial
	private static final long serialVersionUID = 7068287782651527153L;
	private static final Pattern NC_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\-.]*");

	@XmlID
	@XmlAttribute(name = "id")
	private String id;

	@XmlElement(name = "label")
	private final Set<Label> labels = new HashSet<>();

	private transient LabelMap labelMap = new LabelMap();

	public LabeledId()
	{
		this((String) null);
	}

	public LabeledId(String id)
	{
		this(id, null);
	}

	public LabeledId(LabeledId lid)
	{
		capture(lid);
	}

	public LabeledId(String id, Map<String, String> labels)
	{
		this.id = id;
		setLabels(labels);
	}

	public void capture(LabeledId lid)
	{
		if (lid == null)
		{
			lid = new LabeledId();
		}
		setId(lid.getId());
		setLabels(lid.getLabels());
	}

	/**
	 * Updates the labels from the given labeled id. Ignores null as argument.
	 * @param lid the labeled id to update from
	 */
	public void updateLabels(LabeledId lid)
	{
		if (lid != null)
		{
			setLabels(lid.getLabels());
		}
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getLabel(String lang)
	{
		return getLabels().get(lang);
	}
	
	public String getLabelOrId(String lang)
	{
		String label = getLabel(lang);
		return StringUtils.isNotBlank(label) ? label : id;
	}

	public void setLabel(String lang, String value)
	{
		getLabels().put(lang, value);
	}

	public String removeLabel(String lang)
	{
		String oldValue = getLabels().get(lang);
		labels.removeIf(l -> l.getLang().equals(lang));
		return oldValue;
	}

	public Map<String, String> getLabels()
	{
		if (labelMap == null)
		{
			labelMap = new LabelMap();
		}
		return labelMap;
	}

	public void setLabels(Map<String, String> labels)
	{
		getLabels().clear();
		getLabels().putAll(labels);
	}

	public int size()
	{
		return getLabels().size();
	}

	public boolean isEmpty()
	{
		return getLabels().isEmpty();
	}

	public void clear()
	{
		getLabels().clear();
	}

	/**
	 * {@return a pattern to check for valid 'xs:NCName' IDs}.
	 * Subclasses can overwrite this method customize th validation of IDs.
	 * @see <a href="https://www.data2type.de/xml-xslt-xslfo/xml-schema/datentypen-referenz/xs-ncname/">www.data2type.de/xml-xslt-xslfo/xml-schema/datentypen-referenz/xs-ncname</a>
	 */
	public Pattern getValidIdPattern()
	{
		return NC_NAME_PATTERN;
	}

	/**
	 * Checks whether our id is a valid.
	 * @see #getValidIdPattern()
	 * @return true if our id is a valid
	 */
	public boolean hasValidId()
	{
		return getValidIdPattern().matcher(getId()).matches();
	}

	/**
	 * {@return characters not matching the ID pattern or an empty string, if the ID is valid}
	 * The characters will be unique and in the order occurring in the ID
	 * @see #getValidIdPattern()
	 */
	public String findInvalidCharsInID()
	{
		return findInvalidCharsInID(getId());
	}

	public String findInvalidCharsInID(String id)
	{
		if (id == null)
		{
			return "";
		}

		Matcher matcher = getValidIdPattern().matcher(id);
		// Finde den ersten Teil des Strings, der mit dem Muster übereinstimmt
		if (matcher.find())
		{
			// Erhalte den Index des nächsten Zeichens nach dem gefundenen Teil
			int start = matcher.start();
			int end = matcher.end();

			if (start == 0 && end >= id.length())
			{
				return "";
			}

			StringBuilder invalid = new StringBuilder();

			if (start > 0)
			{
				invalid.append(id, 0, start);
			}

			while (matcher.find(end))
			{
				start = matcher.start();
				if (start > end)
				{
					invalid.append(id, end, start);
				}
				end = matcher.end();
			}

			if (end < id.length())
			{
				invalid.append(id, end, id.length());
			}

			return squeezed(invalid.toString());
		}
		else
		{
			return squeezed(id);
		}
	}

	private static String squeezed(String s)
	{
		// squeeze duplicate chars preserving order
		Set<Character> charSet = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (charSet.add(c)){
				sb.append(c);
			}
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof LabeledId that))
			return false;

		return new EqualsBuilder()
				.append(id, that.id)
				.append(labels, that.labels)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(id)
				.append(labels)
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("id", id)
				.append("labels", labels)
				.toString();
	}

	public String toLabelString()
	{
		return labels.toString();
	}

	private class LabelMap extends AbstractMap<String, String>
	{
		private LabelEntrySet labelEntrySet;

		@Override
		public Set<Entry<String, String>> entrySet()
		{
			if (labelEntrySet == null)
			{
				labelEntrySet = new LabelEntrySet();
			}
			return labelEntrySet;
		}

		@Override
		public void putAll(Map<? extends String, ? extends String> m)
		{
			if (m == null)
			{
				super.clear();
			}
			else
			{
				super.putAll(m);
			}
		}

		@Override
		public String put(String lang, String value)
		{
			String oldValue = remove(lang);
			labels.add(new Label(lang, value));
			return oldValue;
		}

		@Override
		public String remove(Object lang)
		{
			String oldValue = get(lang);
			labels.removeIf(l -> l.getLang().equals(lang));
			return oldValue;
		}
	}

	private class LabelEntrySet extends AbstractSet<Map.Entry<String, String>>
	{
		@Override
		public Iterator<Map.Entry<String, String>> iterator()
		{
			return new LabelEntrySetIterator();
		}

		@Override
		public int size()
		{
			return labels.size();
		}

		@Override
		public boolean add(Map.Entry<String, String> entry)
		{
			return labels.add(new Label(entry.getKey(), entry.getValue()));
		}
	}

	private class LabelEntrySetIterator implements Iterator<Map.Entry<String, String>>
	{
		Iterator<Label> labelsIterator = labels.iterator();

		@Override
		public boolean hasNext()
		{
			return labelsIterator.hasNext();
		}

		@Override
		public Map.Entry<String, String> next()
		{
			return labelsIterator.next();
		}

		@Override
		public void remove()
		{
			labelsIterator.remove();
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Label", propOrder = {
			"lang",
			"value"
	})
	static class Label implements Map.Entry<String, String>, Serializable
	{
		@Serial
		private static final long serialVersionUID = 2263992772722741056L;

		@XmlAttribute(name = "lang")
		private String lang;
		@XmlAttribute(name = "value")
		private String value;

		public Label()
		{
			// for deserialization
		}

		public Label(String lang, String value)
		{
			this.lang = lang;
			this.value = value;
		}

		public String getLang()
		{
			return getKey();
		}

		@Override
		public String getKey()
		{
			return lang;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public String setValue(String value)
		{
			String oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;

			if (!(o instanceof Label label))
				return false;

			return new EqualsBuilder()
					.append(getLang(), label.getLang())
					.append(getValue(), label.getValue())
					.isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(getLang())
					.append(getValue())
					.toHashCode();
		}

		@Override
		public String toString()
		{
			return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
					.append("lang", lang)
					.append("value", value)
					.toString();
		}
	}
}
