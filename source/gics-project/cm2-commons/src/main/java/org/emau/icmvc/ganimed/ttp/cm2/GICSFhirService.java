package org.emau.icmvc.ganimed.ttp.cm2;

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

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ImportResultDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FhirExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;


@WebService
public interface GICSFhirService
{
	/**
	 * Import domain-, policy-, module- and template definitions
	 *
	 * @param definition
	 *            content of definition to import
	 * @param fileFormat
	 *            format of definition (e.g. xml or json)
	 * @param allowUpdates
	 *            true if existing definitions are allowed to be updated
	 * @return Map with Lists of add, updated and ignored items.
	 * @throws InvalidExchangeFormatException
	 * @throws InternalException
	 */
	ImportResultDTO importDefinition(@XmlElement(required = true) @WebParam(name = "definition") String definition,
			@XmlElement(required = true) @WebParam(name = "allowUpdates") boolean allowUpdates,
			@XmlElement(required = true) @WebParam(name = "fileFormat") String fileFormat)
			throws InvalidExchangeFormatException, InternalException;

	/**
	 * Import domain-, policy-, module- and template definitions
	 *
	 * @param definition
	 *            content of definition to import
	 * @param fileFormat
	 *            format of definition (e.g. xml or json)
	 * @param allowUpdates
	 *            true if existing definitions are allowed to be updated
	 * @return Map with Lists of add, updated and ignored items.
	 * @throws InvalidExchangeFormatException
	 * @throws InternalException
	 */

	ImportResultDTO previewImportDefinition(@XmlElement(required = true) @WebParam(name = "definition") String definition,
			@XmlElement(required = true) @WebParam(name = "allowUpdates") boolean allowUpdates,
			@XmlElement(required = true) @WebParam(name = "fileFormat") String fileFormat)
			throws InvalidExchangeFormatException, InternalException;

	/**
	 * export domain-, policy-, module- and template definitions for selected domain
	 *
	 * @param domainName
	 *            Name of selected domain
	 * @param exportMode
	 *            mode of export: all, domain, policies, modules, templates, specify
	 * @param itemList
	 *            specify items by key (domain, name, version) to be exported, processed only when
	 *            ExportMode.SPECIFY is used
	 * @param exportLogo
	 *            true, if export should contain base64 encoded domain logo if available
	 * @param fileFormat
	 *            format of definition (e.g. xml or json)
	 * @return definition with export information
	 * @throws UnknownDomainException
	 * @throws InternalException
	 * @throws InvalidExchangeFormatException
	 */
	String exportDefinition(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "exportMode") FhirExportMode exportMode,
			@XmlElement(required = true) @WebParam(name = "itemList") List<String> itemList,
			@XmlElement(required = true) @WebParam(name = "exportLogo") boolean exportLogo,
			@XmlElement(required = true) @WebParam(name = "fileFormat") String fileFormat)
			throws InternalException, InvalidExchangeFormatException;
}
