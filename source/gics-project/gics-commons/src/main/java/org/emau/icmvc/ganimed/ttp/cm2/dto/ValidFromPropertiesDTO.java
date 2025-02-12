package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.time.Period;
import java.util.Calendar;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.time.DateUtils;

/**
 * @author penndorfp
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidFromPropertiesDTO implements Serializable
{
	@Serial
	private static final long serialVersionUID = -2096098836350120714L;
	private Date fixedValidFromDate;
	@XmlJavaTypeAdapter(value = PeriodAdapter.class, type = Period.class)
	private Period invalidPeriod;

	public ValidFromPropertiesDTO()
	{
	}

	public ValidFromPropertiesDTO(Date fixedValidFromDate, Period invalidPeriod)
	{
		setFixedValidFromDate(fixedValidFromDate);
		this.invalidPeriod = invalidPeriod;
	}

	public ValidFromPropertiesDTO(ValidFromPropertiesDTO dto)
	{
		this(dto.getFixedValidFromDate(), dto.getInvalidPeriod());
	}

	public Date getFixedValidFromDate()
	{
		return fixedValidFromDate;
	}

	public void setFixedValidFromDate(Date fixedValidFromDate)
	{
		if (fixedValidFromDate != null)
		{
			this.fixedValidFromDate = new Date(fixedValidFromDate.getTime());
		}
		else
		{
			this.fixedValidFromDate = null;
		}
	}

	public Period getInvalidPeriod()
	{
		return invalidPeriod;
	}

	public void setInvalidPeriod(Period invalidPeriod)
	{
		this.invalidPeriod = invalidPeriod;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (fixedValidFromDate == null ? 0 : fixedValidFromDate.hashCode());
		result = prime * result + (invalidPeriod == null ? 0 : invalidPeriod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ValidFromPropertiesDTO other = (ValidFromPropertiesDTO) obj;
		if (fixedValidFromDate == null)
		{
			if (other.fixedValidFromDate != null)
			{
				return false;
			}
		}
		else if (!DateUtils.truncate(fixedValidFromDate, Calendar.DATE).equals(DateUtils.truncate(other.fixedValidFromDate, Calendar.DATE)))
		{
			return false;
		}
		if (invalidPeriod == null)
		{
			return other.invalidPeriod == null;
		}
		else
			return invalidPeriod.equals(other.invalidPeriod);
	}

	@Override
	public String toString()
	{
		return "ValidFromPropertiesDTO [fixedValidFromDate=" + fixedValidFromDate + ", invalidPeriod=" + invalidPeriod + "]";
	}

	public static class PeriodAdapter extends XmlAdapter<String, Period>
	{
		@Override
		public Period unmarshal(String s) throws Exception
		{
			return Period.parse(s);
		}

		@Override
		public String marshal(Period p) throws Exception
		{
			return p.toString();
		}
	}
}
