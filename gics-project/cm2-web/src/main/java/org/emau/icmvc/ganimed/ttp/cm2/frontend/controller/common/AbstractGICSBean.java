package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common;

import java.util.ResourceBundle;

import javax.ejb.EJB;

/*-
 * ###license-information-start###
 * E-PIX - Enterprise Patient Identifier
 * 							Cross-referencing
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

import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.DomainSelector;
import org.icmvc.ttp.web.controller.AbstractBean;

/**
 * Abstract class for web beans
 * 
 * @author Arne Blumentritt
 *
 */
public abstract class AbstractGICSBean extends AbstractBean
{
	@EJB(lookup = "java:global/gics/cm2-ejb/GICSServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSService")
	protected GICSService cmManager;

	@ManagedProperty(value = "#{domainSelector}")
	protected DomainSelector domainSelector;

	public void setDomainSelector(DomainSelector domainSelector)
	{
		this.domainSelector = domainSelector;
	}

	public DomainDTO getSelectedDomain()
	{
		return domainSelector.getSelectedDomain();
	}

	@Override
	protected ResourceBundle getBundle()
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getResourceBundle(facesContext, "msg");
	}
}
