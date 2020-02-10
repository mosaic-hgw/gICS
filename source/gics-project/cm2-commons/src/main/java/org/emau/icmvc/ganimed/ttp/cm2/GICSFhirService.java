package org.emau.icmvc.ganimed.ttp.cm2;

import java.util.List;

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


import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.gstats.CommonStatisticBean;

@WebService
public interface GICSFhirService {

	/**
	 * import domain-, policy-, module- and template definitions 
	 * 
	 * @param xmlFhirFormat
	 *            importformatdefinition in fhir xmlformat
	 * @param allowUpdates
	 *            true if existing definitions shall be updates if possible
	 * @throws InvalidExchangeFormatException
	 * @throws InternalException
	 */
	public void importDefinition(@XmlElement(required = true) @WebParam(name = "xmlFhirFormat") String xmlFhirFormat,
			@XmlElement(required = true) @WebParam(name = "allowUpdates") boolean allowUpdates)
			throws InvalidExchangeFormatException, InternalException;

	/**
	 * export domain-, policy-, module- and template definitions for selected domain
	 * @param domainName
	 * 	Name of selected domain
	 * @param exportMode
	 * 	mode of export: all, domain, policies, modules, templates, specify
	 * @param itemList
	 * 	specify items by key (domain, name, version) to be exported, processed only when ExportMode.SPECIFY is used
	 * @param exportLogo
	 * 	true, if export should contain base64 encoded domain logo if available
	 * @return xmlFhirFormat with export information
	 * @throws UnknownDomainException
	 * @throws InternalException 
	 */
	public String exportDefinition(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "exportMode") ExportMode exportMode,
			@XmlElement(required = true) @WebParam(name = "itemList") List<String> itemList,
			@XmlElement(required = true) @WebParam(name = "exportLogo") Boolean exportLogo) 
					throws UnknownDomainException, InternalException;
}
