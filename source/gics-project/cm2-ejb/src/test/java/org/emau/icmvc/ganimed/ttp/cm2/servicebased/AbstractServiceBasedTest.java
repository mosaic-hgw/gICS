package org.emau.icmvc.ganimed.ttp.cm2.servicebased;

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
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.emau.icmvc.ganimed.ttp.cm2.GICSManagementService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.junit.jupiter.api.Assertions;

public abstract class AbstractServiceBasedTest
{
	public static final String MPI_ID_TYPE = "mpi_id_type";
	public static final String NO_TITLE = "no_title";
	public static final String NO_COMMENT = "no_comment";
	public static final String NO_EXTERN_PROPERTIES = "no_extern_properties";

	public static final String CM2_SERVICE_WSDL = "http://localhost:8080/gics/gicsService?wsdl";
	public static final String CM2_MANAGER_WSDL = "http://localhost:8080/gics/gicsManagementService?wsdl";

	protected static GICSService cm2Service;
	protected static GICSManagementService cm2Manager;

	public static <T> T getPort(Class<T> serviceEndpointInterface, String localPart, String wsdlURL) throws MalformedURLException
	{
		QName serviceName = new QName("http://cm2.ttp.ganimed.icmvc.emau.org/", localPart);
		Service service = Service.create(new URL(wsdlURL), serviceName);
		Assertions.assertNotNull(service, "webservice object for " + localPart + " is null");
		return service.getPort(serviceEndpointInterface);
	}

	public static void setupServices()
	{
		try
		{
			if (cm2Service == null)
			{
				cm2Service = getPort(GICSService.class, "GICSServiceImplService", CM2_SERVICE_WSDL);
				Assertions.assertNotNull(cm2Service, "cm2 service object is null");
			}

			if (cm2Manager == null)
			{
				cm2Manager = getPort(GICSManagementService.class, "GICSManagementServiceImplService", CM2_MANAGER_WSDL);
				Assertions.assertNotNull(cm2Manager, "cm2 manager object is null");
			}
		}
		catch (MalformedURLException e)
		{
			// should never happen
			throw new RuntimeException(e);
		}
	}
}
