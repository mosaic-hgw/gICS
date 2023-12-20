package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
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
import java.time.Period;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExpirationPropertiesDTO implements Serializable
{
	private static final long serialVersionUID = 5346718142260816824L;
	private Date fixedExpirationDate;
	@XmlJavaTypeAdapter(value = PeriodAdapter.class, type = Period.class)
	private Period validPeriod;

	public ExpirationPropertiesDTO()
	{
		super();
	}

	public ExpirationPropertiesDTO(Date fixedExpirationDate, Period validPeriod)
	{
		super();
		setFixedExpirationDate(fixedExpirationDate);
		this.validPeriod = validPeriod;
	}

	public ExpirationPropertiesDTO(ExpirationPropertiesDTO dto)
	{
		this(dto.getFixedExpirationDate(), dto.getValidPeriod());
	}

	public Date getFixedExpirationDate()
	{
		return fixedExpirationDate;
	}

	public void setFixedExpirationDate(Date fixedExpirationDate)
	{
		if (fixedExpirationDate != null)
		{
			this.fixedExpirationDate = new Date(fixedExpirationDate.getTime());
		}
		else
		{
			this.fixedExpirationDate = null;
		}
	}

	public Period getValidPeriod()
	{
		return validPeriod;
	}

	public void setValidPeriod(Period validPeriod)
	{
		this.validPeriod = validPeriod;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (fixedExpirationDate == null ? 0 : fixedExpirationDate.hashCode());
		result = prime * result + (validPeriod == null ? 0 : validPeriod.hashCode());
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
		ExpirationPropertiesDTO other = (ExpirationPropertiesDTO) obj;
		if (fixedExpirationDate == null)
		{
			if (other.fixedExpirationDate != null)
			{
				return false;
			}
		}
		// TODO: should equals ignore the time part of the date which however will vanish on serialization
		else if (!fixedExpirationDate.equals(other.fixedExpirationDate))
		{
			return false;
		}
		if (validPeriod == null)
		{
			if (other.validPeriod != null)
			{
				return false;
			}
		}
		else if (!validPeriod.equals(other.validPeriod))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "ExpirationPropertiesDTO [fixedExpirationDate=" + fixedExpirationDate + ", validPeriod=" + validPeriod + "]";
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
