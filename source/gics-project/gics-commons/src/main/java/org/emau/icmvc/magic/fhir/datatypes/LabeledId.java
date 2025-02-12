package org.emau.icmvc.magic.fhir.datatypes;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

public abstract class LabeledId extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = 4429786251086221790L;

	@Child(name = "labels", order = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "the multilingual labels")
	protected List<Label> labels = new ArrayList<>();

	public Map<String, String> getLabels()
	{
		HashMap<String, String> map = new HashMap<>();
		labels.forEach(l -> map.put(l.getLang(), l.getValue()));
		return map;
	}

	public void setLabels(Map<String, String> labels)
	{
		this.labels.clear();
		if (labels != null)
		{
			labels.forEach((k, v) -> this.labels.add(new Label(k, v)));
		}
	}

	@DatatypeDef(name = "Label")
	public static class Label extends Type implements ICompositeType
	{
		@Serial
		private static final long serialVersionUID = 8905620930374973437L;

		@Child(name = "lang", order = 0)
		@Description(shortDefinition = "the language of the label")
		private StringDt lang = new StringDt();
		@Child(name = "value", order = 1)
		@Description(shortDefinition = "the value of the label")
		private StringDt value = new StringDt();

		public Label()
		{
		}
		public Label(String lang, String value)
		{
			setLang(lang);
			setValue(value);
		}

		@Override
		public boolean isEmpty()
		{
			return ElementUtil.isEmpty(lang, value);
		}

		@Override
		protected Type typedCopy()
		{
			return deepCopy();
		}

		public Label deepCopy()
		{
			Label label = new Label();
			copyValues(label);
			label.capture(this);
			return label;
		}

		public void capture(Label label)
		{
			if (label == null)
			{
				label = new Label();
			}
			setLang(label.getLang());
			setValue(label.getValue());
		}

		public String getLang()
		{
			return lang.getValue();
		}

		public void setLang(String lang)
		{
			this.lang.setValue(lang);
		}

		public String getValue()
		{
			return value.getValue();
		}

		public void setValue(String value)
		{
			this.value.setValue(value);
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
			return new ToStringBuilder(this)
					.append("lang", getLang())
					.append("value", getValue())
					.toString();
		}
	}
}
