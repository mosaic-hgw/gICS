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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class Dates
{

	public static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

	/**
	 * Returns the specified date at the start of its day w.r.t. the system's default zone.
	 *
	 * @param date the instant to shift to the start of its day
	 * @return local date at the start of the day
	 */
	public static LocalDate atStartOfDay(Date date)
	{
		return atStartOfDay(date.toInstant().atZone(SYSTEM_ZONE_ID).toLocalDate());

	}

	/**
	 * Returns the specified local date at the start of its day w.r.t. the system's default zone.
	 *
	 * @param localDate the local date to shift to the start of its day
	 * @return local date at the start of the day
	 */
	public static LocalDate atStartOfDay(LocalDate localDate)
	{
		return localDate.atStartOfDay(SYSTEM_ZONE_ID).toLocalDate();
	}

	/**
	 * Returns a local date w.r.t. the system's default zone which refers to today.
	 *
	 * @return a local date w.r.t. the system's default zone which refers to today
	 */
	public static LocalDate today()
	{
		return toLocalDate(Calendar.getInstance().getTime());
	}

	/**
	 * Returns a local date w.r.t. the system's default zone which refers to tomorrow.
	 *
	 * @return a local date w.r.t. the system's default zone which refers to tomorrow
	 */
	public static LocalDate tomorrow()
	{
		return today().plusDays(1);
	}

	/**
	 * Converts the specified local date into a date w.r.t. the system's default zone.
	 *
	 * @param localDate the local date to convert
	 * @return a date w.r.t. the system's default zone
	 */
	public static Date toDate(LocalDate localDate)
	{
		return Date.from(localDate.atStartOfDay(SYSTEM_ZONE_ID).toInstant());
	}

	/**
	 * Converts the specified date into a local date w.r.t. the system's default zone.
	 *
	 * @param date the date to convert
	 * @return a local date w.r.t. the system's default zone
	 */
	public static LocalDate toLocalDate(Date date)
	{
		return date.toInstant().atZone(SYSTEM_ZONE_ID).toLocalDate();
	}

	/**
	 * Returns true if the specified date refers to a day in the future w.r.t. the system's default zone.
	 *
	 * @param date the date to check
	 * @return true if the specified date refers to a day in the future
	 */
	public static boolean isFutureDay(Date date)
	{
		return isFutureDay(toLocalDate(date));
	}

	/**
	 * Returns true if the specified local date refers to a day in the future w.r.t. the system's default zone.
	 *
	 * @param localDate the local date to check
	 * @return true if the specified local date refers to a day in the future
	 */
	public static boolean isFutureDay(LocalDate localDate)
	{
		return today().isBefore(atStartOfDay(localDate));
	}
}
