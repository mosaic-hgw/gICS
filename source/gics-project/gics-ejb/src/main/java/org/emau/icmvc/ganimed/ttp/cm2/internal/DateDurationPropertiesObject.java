package org.emau.icmvc.ganimed.ttp.cm2.internal;

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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import org.emau.icmvc.ganimed.ttp.cm2.util.PropertiesObject;

public class DateDurationPropertiesObject extends PropertiesObject implements Serializable
{
	protected static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd";
	protected final Date date;
	protected final String dateKey;
	protected final Period duration;
	protected final String durationKey;
	protected final String dateFormatKey;

	public DateDurationPropertiesObject(String propertiesString, String dateKey, String durationKey, String dateFormatKey) throws ParseException
	{
		super(propertiesString);
		this.dateKey = dateKey;
		this.durationKey = durationKey;
		this.dateFormatKey = dateFormatKey;
		Properties properties = getProperties();
		String temp = (String) properties.get(dateKey);
		if (temp != null && !temp.isEmpty())
		{
			String expirationDateFormat = (String) properties.get(dateFormatKey);
			if (expirationDateFormat == null)
			{
				expirationDateFormat = DEFAULT_DATE_FORMAT;
			}
			date = new SimpleDateFormat(expirationDateFormat).parse(temp);
		}
		else
		{
			date = null;
		}
		temp = (String) properties.get(durationKey);
		if (temp != null && !temp.trim().isEmpty())
		{
			duration = Period.parse(temp);
		}
		else
		{
			duration = null;
		}
	}

	public DateDurationPropertiesObject(Date expirationDate, Period validDuration, String dateKey, String durationKey, String dateFormatKey)
	{
		super(null);
		this.date = expirationDate;
		this.dateKey = dateKey;
		this.durationKey = durationKey;
		this.dateFormatKey = dateFormatKey;
		if (expirationDate != null)
		{
			getProperties().put(dateKey, new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(expirationDate));
			getProperties().put(dateFormatKey, DEFAULT_DATE_FORMAT);
		}
		if (validDuration != null)
		{
			this.duration = validDuration;
			getProperties().put(durationKey, validDuration.toString());
		}
		else
		{
			this.duration = null;
		}
	}

	public String toPropertiesString()
	{
		String result = "";
		if (duration != null)
		{
			result += durationKey + "=" + duration + ";";
		}
		if (date != null)
		{
			result += dateKey + "=" + new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(date) + ";";
			result += dateFormatKey + "=" + DEFAULT_DATE_FORMAT + ";";
		}
		return result;
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
		DateDurationPropertiesObject that = (DateDurationPropertiesObject) o;
		return Objects.equals(date, that.date) && Objects.equals(duration, that.duration);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), date, duration);
	}
}
