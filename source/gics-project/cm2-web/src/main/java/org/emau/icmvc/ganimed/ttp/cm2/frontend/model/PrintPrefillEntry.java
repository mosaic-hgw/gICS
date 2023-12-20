package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.icmvc.ttp.web.controller.AbstractBean;

/**
 * simple model to hold data for prefilled templates in gics web
 */
public class PrintPrefillEntry extends AbstractBean implements Serializable
{
	private static final long serialVersionUID = -3621381591861495294L;

	private SimpleDateFormat format = new SimpleDateFormat(getCommonBundle().getString("ui.date.pattern.date"));

	private String internalKey = UUID.randomUUID().toString();

	private List<SignerIdDTO> signerIdDtos;

	private String physicianPlace = "";
	private String signerPlace = "";

	private Date physicianDate;
	private Date signerDate;


	public PrintPrefillEntry()
	{
		//empty on purpose
	}

	public String getInternalKey()
	{
		return internalKey;
	}

	public List<SignerIdDTO> getSignerIdDtos()
	{
		return signerIdDtos;
	}

	public String getSignerIdValueFromDtosByType(String type)
	{
		for (SignerIdDTO id : getSignerIdDtos())
		{
			if (id.getIdType().equals(type))
			{
				return id.getId();
			}
		}
		return null;
	}

	public void setSignerIdDtos(List<SignerIdDTO> signerIdDtos)
	{
		this.signerIdDtos = signerIdDtos;
	}

	public String getPhysicianPlace()
	{
		return physicianPlace;
	}

	public void setPhysicianPlace(String physicianPlace)
	{
		this.physicianPlace = physicianPlace;
	}

	public String getSignerPlace()
	{
		return signerPlace;
	}

	public void setSignerPlace(String signerPlace)
	{
		this.signerPlace = signerPlace;
	}

	public Date getPhysicianDate()
	{
		return physicianDate;
	}

	public void setPhysicianDate(Date physicianDate)
	{
		this.physicianDate = physicianDate;
	}
	
	public void setPhysicianDate(String physicianDateString) throws ParseException
	{
		this.physicianDate = format.parse(physicianDateString);
	}

	public Date getSignerDate()
	{
		return signerDate;
	}

	public void setSignerDate(Date signerDate)
	{
		this.signerDate = signerDate;
	}

	public void setSignerDate(String signerDateString) throws ParseException
	{
		this.signerDate = format.parse(signerDateString);
	}
}
