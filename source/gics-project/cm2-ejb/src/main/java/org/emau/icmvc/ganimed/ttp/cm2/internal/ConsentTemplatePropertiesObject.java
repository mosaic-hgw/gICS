package org.emau.icmvc.ganimed.ttp.cm2.internal;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentTemplateProperties;

public class ConsentTemplatePropertiesObject extends PropertiesObject implements Serializable {

	private static final long serialVersionUID = -168295417654435722L;
	private final static String PERIOD_PATTERN = "^p((?<years>[0-9]+)y)?((?<months>[0-9]+)m)?((?<weeks>[0-9]+)w)?((?<days>[0-9]+)d)?$";
	private final static Pattern periodPattern = Pattern.compile(PERIOD_PATTERN);
	private final static PeriodReplacement noPeriod = new PeriodReplacement();
	private final Date expirationDate;
	private final PeriodReplacement validDuration;

	public ConsentTemplatePropertiesObject(String propertiesString) throws ParseException {
		super(propertiesString);
		Properties properties = getProperties();
		String temp = (String) properties.get(ConsentTemplateProperties.EXPIRATION_DATE.toString());
		if (temp != null && !temp.isEmpty()) {
			String expirationDateFormat = (String) properties.get(ConsentTemplateProperties.EXPIRATION_DATE_FORMAT.toString());
			if (expirationDateFormat == null) {
				expirationDateFormat = "yyyy.MM.dd";
			}
			expirationDate = new SimpleDateFormat(expirationDateFormat).parse(temp);
		} else {
			expirationDate = null;
		}
		temp = (String) properties.get(ConsentTemplateProperties.VALIDITY_PERIOD.toString());
		validDuration = getObjectFromPeriodString(temp);
	}

	// TODO durch java 8 klasse "period" ersetzen, sobald der dispatcher auf java 8 umgestellt ist
	private PeriodReplacement getObjectFromPeriodString(String periodString) throws ParseException {
		PeriodReplacement result = noPeriod;
		if (periodString != null && !periodString.isEmpty()) {
			periodString = periodString.toLowerCase();
			Matcher matcher = periodPattern.matcher(periodString);
			if (matcher.matches()) {
				result = new PeriodReplacement(matcher.group("years"), matcher.group("weeks"), matcher.group("months"), matcher.group("days"));
			} else {
				throw new ParseException("invalid period string: " + periodString, 0);
			}
		}
		return result;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public Date getExpirationDateForConsentWithDate(Date date) {
		return validDuration.getExpirationDateForConsentWithDate(date, expirationDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expirationDate == null) ? 0 : expirationDate.hashCode());
		result = prime * result + ((validDuration == null) ? 0 : validDuration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsentTemplatePropertiesObject other = (ConsentTemplatePropertiesObject) obj;
		if (expirationDate == null) {
			if (other.expirationDate != null)
				return false;
		} else if (!expirationDate.equals(other.expirationDate))
			return false;
		if (validDuration == null) {
			if (other.validDuration != null)
				return false;
		} else if (!validDuration.equals(other.validDuration))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("consent templates properties: ");
		sb.append(ConsentTemplateProperties.EXPIRATION_DATE.toString());
		sb.append(" = ");
		sb.append(expirationDate);
		sb.append("; ");
		sb.append(ConsentTemplateProperties.VALIDITY_PERIOD.toString());
		sb.append(" = ");
		sb.append(validDuration);
		return sb.toString();
	}

	private static final class PeriodReplacement implements Serializable {

		private static final long serialVersionUID = 1954956805335809337L;
		private final int years;
		private final int months;
		private final int days;

		public PeriodReplacement(String years, String months, String weeks, String days) {
			super();
			if (years != null) {
				this.years = Integer.parseInt(years);
			} else {
				this.years = 0;
			}
			if (months != null) {
				this.months = Integer.parseInt(months);
			} else {
				this.months = 0;
			}
			int weeksNumber = 0;
			if (weeks != null) {
				weeksNumber = Integer.parseInt(weeks);
			}
			if (days != null) {
				this.days = Integer.parseInt(days) + weeksNumber * 7;
			} else {
				this.days = weeksNumber * 7;
			}
		}

		protected PeriodReplacement() {
			years = 1000;
			months = 0;
			days = 0;
		}

		public Date getExpirationDateForConsentWithDate(Date date, Date invalidationDate) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.add(Calendar.YEAR, years);
			cal.add(Calendar.MONTH, months);
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date result = cal.getTime();
			if (invalidationDate != null && result.after(invalidationDate)) {
				result = invalidationDate;
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + days;
			result = prime * result + months;
			result = prime * result + years;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PeriodReplacement other = (PeriodReplacement) obj;
			if (days != other.days)
				return false;
			if (months != other.months)
				return false;
			if (years != other.years)
				return false;
			return true;
		}
	}
}
