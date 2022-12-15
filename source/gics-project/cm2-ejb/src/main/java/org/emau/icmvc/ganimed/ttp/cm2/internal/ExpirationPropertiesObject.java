package org.emau.icmvc.ganimed.ttp.cm2.internal;

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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Properties;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ExpirationProperties;

public class ExpirationPropertiesObject extends PropertiesObject implements Serializable
{
	private static final long serialVersionUID = -5724349741953072011L;
	private static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd";
	private final Date expirationDate;
	private final Period validDuration;

	public ExpirationPropertiesObject(String propertiesString) throws ParseException, DateTimeParseException
	{
		super(propertiesString);
		Properties properties = getProperties();
		String temp = (String) properties.get(ExpirationProperties.EXPIRATION_DATE.toString());
		if (temp != null && !temp.isEmpty())
		{
			String expirationDateFormat = (String) properties.get(ExpirationProperties.EXPIRATION_DATE_FORMAT.toString());
			if (expirationDateFormat == null)
			{
				expirationDateFormat = DEFAULT_DATE_FORMAT;
			}
			expirationDate = new SimpleDateFormat(expirationDateFormat).parse(temp);
		}
		else
		{
			expirationDate = null;
		}
		temp = (String) properties.get(ExpirationProperties.VALIDITY_PERIOD.toString());
		if (temp != null && !temp.trim().isEmpty())
		{
			validDuration = Period.parse(temp);
		}
		else
		{
			validDuration = null;
		}
	}

	public ExpirationPropertiesObject(ExpirationPropertiesDTO dto)
	{
		this(dto == null ? null : dto.getFixedExpirationDate(), dto == null ? null : dto.getValidPeriod());
	}

	public ExpirationPropertiesObject(Date expirationDate, Period validDuration)
	{
		super(null);
		this.expirationDate = expirationDate;
		if (expirationDate != null)
		{
			getProperties().put(ExpirationProperties.EXPIRATION_DATE.toString(), new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(expirationDate));
			getProperties().put(ExpirationProperties.EXPIRATION_DATE_FORMAT.toString(), DEFAULT_DATE_FORMAT);
		}
		if (validDuration != null)
		{
			this.validDuration = validDuration;
			getProperties().put(ExpirationProperties.VALIDITY_PERIOD.toString(), validDuration.toString());
		}
		else
		{
			this.validDuration = null;
		}
	}

	public Date getExpirationDate()
	{
		return expirationDate;
	}

	public Date getExpirationDateForConsentDate(Date date)
	{
		// muss ein festes datum sein, sonst schlagen equals-vergleiche auf consenten fehl
		LocalDateTime result = ConsentDateValuesDTO.INFINITE_DATE.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (validDuration != null)
		{
			result = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			result = result.plusDays(validDuration.getDays()).plusMonths(validDuration.getMonths()).plusYears(validDuration.getYears());
		}
		if (expirationDate != null)
		{
			LocalDateTime expDate = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			if (result.isAfter(expDate))
			{
				result = expDate;
			}
		}
		return Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Returns a normalized form of this object without the time part of the fixed expiration date
	 * as defined in {@link #toPropertiesString()}
	 *
	 * @return a normalized form of this object
	 */
	public ExpirationPropertiesObject normalized()
	{
		try
		{
			return new ExpirationPropertiesObject(toPropertiesString());
		}
		catch (ParseException e)
		{
			// this never happens (except the code to format and parse is wrong)
			throw new RuntimeException(e);
		}
	}

	public String toPropertiesString()
	{
		String result = "";
		if (validDuration != null)
		{
			result += ExpirationProperties.VALIDITY_PERIOD.toString() + "=" + validDuration + ";";
		}
		if (expirationDate != null)
		{
			result += ExpirationProperties.EXPIRATION_DATE.toString() + "=" + new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(expirationDate) + ";";
			result += ExpirationProperties.EXPIRATION_DATE_FORMAT.toString() + "=" + DEFAULT_DATE_FORMAT + ";";
		}
		return result;
	}

	public ExpirationPropertiesDTO toDTO()
	{
		return new ExpirationPropertiesDTO(expirationDate, validDuration);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (expirationDate == null ? 0 : expirationDate.hashCode());
		result = prime * result + (validDuration == null ? 0 : validDuration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ExpirationPropertiesObject other = (ExpirationPropertiesObject) obj;
		if (expirationDate == null)
		{
			if (other.expirationDate != null)
			{
				return false;
			}
		}
		else if (!expirationDate.equals(other.expirationDate))
		{
			return false;
		}
		if (validDuration == null)
		{
			if (other.validDuration != null)
			{
				return false;
			}
		}
		else if (!validDuration.equals(other.validDuration))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("consent templates properties: ");
		sb.append(ExpirationProperties.EXPIRATION_DATE.toString());
		sb.append(" = ");
		sb.append(expirationDate);
		sb.append("; ");
		sb.append(ExpirationProperties.VALIDITY_PERIOD.toString());
		sb.append(" = ");
		sb.append(validDuration);
		return sb.toString();
	}
}
