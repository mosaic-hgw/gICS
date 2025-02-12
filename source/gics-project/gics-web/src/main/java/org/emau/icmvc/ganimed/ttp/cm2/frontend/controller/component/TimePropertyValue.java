package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component;

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
import java.util.Date;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpressionExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ValidFromPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.util.Dates;

public class TimePropertyValue implements Serializable
{
	@Serial
	private static final long serialVersionUID = -3739910572711064442L;

	private boolean isSet = false;

	private Date timestamp;

	private int periodYears = 0;
	private int periodMonths = 0;
	private int periodDays = 0;
	
	private String expression;

	public ExpirationPropertiesDTO getExpirationProperties()
	{
		ExpirationPropertiesDTO dto = new ExpirationPropertiesDTO();
		if (isSet)
		{
			if (expression != null)
			{
				dto = new ExpressionExpirationPropertiesDTO();
				((ExpressionExpirationPropertiesDTO) dto).setExpirationExpression(expression);
			}
			
			if (timestamp != null)
			{
				dto.setFixedExpirationDate(timestamp);
			}
			if (periodYears != 0 || periodMonths != 0 || periodDays != 0)
			{
				dto.setValidPeriod(Period.of(periodYears, periodMonths, periodDays));
			}
		}

		return dto;
	}

	public void setExpirationProperties(ExpirationPropertiesDTO dto)
	{
		isSet = false;
		timestamp = null;
		periodDays = 0;
		periodMonths = 0;
		periodYears = 0;

		if (dto != null)
		{
			if (dto.getFixedExpirationDate() != null)
			{
				timestamp = dto.getFixedExpirationDate();
				isSet = true;
			}
			if (dto.getValidPeriod() != null)
			{
				periodYears = dto.getValidPeriod().getYears();
				periodMonths = dto.getValidPeriod().getMonths();
				periodDays = dto.getValidPeriod().getDays();
				isSet = true;
			}
			if (dto instanceof ExpressionExpirationPropertiesDTO expressionExpirationPropertiesDTO
					&& expressionExpirationPropertiesDTO.getExpirationExpression() != null)
			{
				expression = expressionExpirationPropertiesDTO.getExpirationExpression();
				isSet = true;
			}
		}
	}

	public ValidFromPropertiesDTO getValidFromProperties()
	{
		ValidFromPropertiesDTO dto = new ValidFromPropertiesDTO();
		if (isSet)
		{
			if (timestamp != null)
			{
				dto.setFixedValidFromDate(timestamp);
			}
			if (periodYears != 0 || periodMonths != 0 || periodDays != 0)
			{
				dto.setInvalidPeriod(Period.of(periodYears, periodMonths, periodDays));
			}
		}

		return dto;
	}

	public void setValidFromProperties(ValidFromPropertiesDTO dto)
	{
		isSet = false;
		timestamp = null;
		periodDays = 0;
		periodMonths = 0;
		periodYears = 0;

		if (dto != null)
		{
			if (dto.getFixedValidFromDate() != null)
			{
				timestamp = dto.getFixedValidFromDate();
				isSet = true;
			}
			if (dto.getInvalidPeriod() != null)
			{
				periodYears = dto.getInvalidPeriod().getYears();
				periodMonths = dto.getInvalidPeriod().getMonths();
				periodDays = dto.getInvalidPeriod().getDays();
				isSet = true;
			}
		}
	}

	public boolean isSet()
	{
		return isSet;
	}

	public void setSet(boolean set)
	{
		this.isSet = set;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public int getPeriodYears()
	{
		return periodYears;
	}

	public void setPeriodYears(int periodYears)
	{
		this.periodYears = periodYears;
	}

	public int getPeriodMonths()
	{
		return periodMonths;
	}

	public void setPeriodMonths(int periodMonths)
	{
		this.periodMonths = periodMonths;
	}

	public int getPeriodDays()
	{
		return periodDays;
	}

	public void setPeriodDays(int periodDays)
	{
		this.periodDays = periodDays;
	}

	public boolean getHasPassed()
	{
		return !Dates.isFutureDay(timestamp);
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}
}
