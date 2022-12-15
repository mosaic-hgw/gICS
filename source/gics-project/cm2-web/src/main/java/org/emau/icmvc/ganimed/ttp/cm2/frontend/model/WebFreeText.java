package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextValDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;

public class WebFreeText implements Serializable
{
	private static final long serialVersionUID = -5762424290538065679L;
	private FreeTextDefDTO dto;
	private String value;
	private final String fhirID;

	public WebFreeText(FreeTextDefDTO dto, FreeTextValDTO valDTO)
	{
		this.dto = dto;
		this.value = valDTO.getValue();
		this.fhirID = valDTO.getFhirID();
	}

	public FreeTextDefDTO getDto()
	{
		return dto;
	}

	public void setDto(FreeTextDefDTO dto)
	{
		this.dto = dto;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public Date getValueAsDate()
	{
		if (FreeTextType.DATE.equals(dto.getType()))
		{
			if (StringUtils.isEmpty(dto.getConverterString()) || StringUtils.isEmpty(value))
			{
				return null;
			}
			else
			{
				try
				{
					SimpleDateFormat sdf = new SimpleDateFormat(dto.getConverterString());
					return sdf.parse(value);
				}
				catch (ParseException e)
				{
					throw new UnsupportedOperationException("Cannot parse freeTextDef value " + value + " with date format " + dto.getConverterString());
				}
			}
		}
		else
		{
			throw new UnsupportedOperationException("Cannot convert freeTextDef of type " + dto.getType().name() + " to date");
		}
	}

	public void setValueAsDate(Date value)
	{
		if (FreeTextType.DATE.equals(dto.getType()))
		{
			if (StringUtils.isEmpty(dto.getConverterString()) || value == null)
			{
				this.value = null;
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat(dto.getConverterString());
				this.value = sdf.format(value);
			}
		}
		else
		{
			throw new UnsupportedOperationException("Cannot convert date to freeTextDef of type " + dto.getType().name());
		}
	}

	public FreeTextValDTO toDTO()
	{
		return new FreeTextValDTO(dto.getName(), value, fhirID);
	}
}
