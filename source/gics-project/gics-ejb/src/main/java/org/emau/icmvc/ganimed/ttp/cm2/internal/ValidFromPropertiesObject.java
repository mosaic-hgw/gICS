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

import java.io.Serial;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ValidFromPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ValidFromProperties;
import org.emau.icmvc.ganimed.ttp.cm2.util.Dates;

/**
 * @author penndorfp
 */
public class ValidFromPropertiesObject extends DateDurationPropertiesObject implements Serializable
{
	@Serial
	private static final long serialVersionUID = 745359611199659074L;

	public ValidFromPropertiesObject(String propertiesString) throws ParseException, DateTimeParseException
	{
		super(propertiesString, ValidFromProperties.VALID_FROM_DATE.toString(), ValidFromProperties.VALID_FROM_PERIOD.toString(), ValidFromProperties.VALID_FROM_DATE_FORMAT.toString());
	}

	public ValidFromPropertiesObject(ValidFromPropertiesDTO dto)
	{
		this(dto == null ? null : dto.getFixedValidFromDate(), dto == null ? null : dto.getInvalidPeriod());
	}

	public ValidFromPropertiesObject(Date date, Period invalidDuration)
	{
		super(date, invalidDuration, ValidFromProperties.VALID_FROM_DATE.toString(), ValidFromProperties.VALID_FROM_PERIOD.toString(), ValidFromProperties.VALID_FROM_DATE_FORMAT.toString());
	}

	/**
	 * Returns a normalized form of this object without the time part of the fixed expiration date
	 * as defined in {@link #toPropertiesString()}
	 *
	 * @return a normalized form of this object
	 */
	public ValidFromPropertiesObject normalized()
	{
		try
		{
			return new ValidFromPropertiesObject(toPropertiesString());
		}
		catch (ParseException e)
		{
			// this never happens (except the code to format and parse is wrong)
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a new validFrom ExpirationProperties (merging this and the specified one) which
	 * <ul>
	 * <li>equals the specified new one if <code>finalised</code> is false, or otherwise</li>
	 * <li>equals this old one if at least one of the fixed validFrom dates dos not refer to a day in the future, or otherwise</li>
	 * <li>contains the the new one's fixed validFrom date and this one's validFrom period (if finalised is true and both validFrom dates refer to days in the future)</li>
	 * </ul>
	 *
	 * @param validFrom
	 * 		the new validFrom properties
	 * @param finalised
	 * 		true to merge in finalised mode
	 * @return the merged validFrom properties
	 */
	public ValidFromPropertiesObject createMergedProperties(ValidFromPropertiesDTO validFrom, boolean finalised)
	{
		if (validFrom == null)
		{
			validFrom = new ValidFromPropertiesDTO(null, null);
		}

		if (!finalised)
		{
			// fully use the new validFrom in non-finalised mode
			return new ValidFromPropertiesObject(validFrom.getFixedValidFromDate(), validFrom.getInvalidPeriod());
		}
		// for finalised templates only allow to update future validFrom properties (both, the old and the new validFrom must refer to a day in the future))
		else if (Dates.isFutureDay(date) && Dates.isFutureDay(validFrom.getFixedValidFromDate()))
		{
			// update a future fixedValidFromDate but keep the old validFromPeriod in this case
			return new ValidFromPropertiesObject(validFrom.getFixedValidFromDate(), duration);
		}

		// on finalised do not update at all if one of the fixed validFrom dates does not refer to a future day
		return this;
	}


	public Date getValidFromDateForConsentDate(Date consentDate)
	{
		// muss ein festes datum sein, sonst schlagen equals-vergleiche auf consenten fehl. wenn keine props existieren ist das consentDate das validFromDate
		LocalDateTime result = null;
		if (duration != null)
		{
			result = consentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			result = result.plusDays(duration.getDays()).plusMonths(duration.getMonths()).plusYears(duration.getYears());
		}
		if (this.date != null)
		{
			LocalDateTime fixedValidFromDate = this.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			if (result == null || fixedValidFromDate.isAfter(result))
			{
				result = fixedValidFromDate;
			}
		}
		return result != null ? Date.from(result.atZone(ZoneId.systemDefault()).toInstant()) : null;
	}

	public ValidFromPropertiesDTO toDTO()
	{
		return new ValidFromPropertiesDTO(date, duration);
	}

	public Date getValidFromDate()
	{
		return date;
	}

	public Period getInvalidPeriod()
	{
		return duration;
	}

	@Override
	public String toString()
	{
		return "consent template valid-from-properties: " + ValidFromProperties.VALID_FROM_DATE
			   + " = "
			   + date
			   + "; "
			   + ValidFromProperties.VALID_FROM_PERIOD
			   + " = "
			   + duration;
	}
}
