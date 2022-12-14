package org.emau.icmvc.ganimed.ttp.cm2.frontend.converter;

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


import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;

// Cannot use @FacesConverter with @EJB until JSF 2.3
@ManagedBean
@RequestScoped
public class DomainDTOConverter implements Converter
{
	@EJB(lookup = "java:global/gics/cm2-ejb/GICSServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSService")
	protected GICSService cmManager;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object modelValue)
	{
		if (modelValue == null)
		{
			return "";
		}

		if (modelValue instanceof DomainDTO)
		{
			return ((DomainDTO) modelValue).getName();
		}
		else
		{
			throw new ConverterException(new FacesMessage(modelValue + " is not a valid DomainDTO"));
		}
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String submittedValue)
	{
		if (submittedValue == null || submittedValue.isEmpty())
		{
			return null;
		}

		try
		{
			List<DomainDTO> domains = cmManager.listDomains();

			for (DomainDTO domain : domains)
			{
				if (domain.getName().equals(submittedValue))
				{
					return domain;
				}
			}
			throw new ConverterException(new FacesMessage("Cannot find DomainDTO with ID: " + submittedValue));
		}
		catch (NumberFormatException e)
		{
			throw new ConverterException(new FacesMessage(submittedValue + " is not a valid DomainDTO ID"), e);
		}
	}
}
