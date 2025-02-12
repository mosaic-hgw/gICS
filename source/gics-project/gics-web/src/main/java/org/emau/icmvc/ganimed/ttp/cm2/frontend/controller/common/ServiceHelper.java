package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common;

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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.emau.icmvc.ganimed.ttp.cm2.GICSFhirService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSManagementService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSServiceWithNotification;
import org.emau.icmvc.ganimed.ttp.cm2.StatisticManager;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.service.ChromeDriverService;
import org.emau.icmvc.ttp.auth.TTPNames;
import org.emau.icmvc.ttp.util.ProxyBuilder;
import org.icmvc.ttp.web.util.WebAuthContext;

/**
 * Common helper for using the (proxied) services.
 *
 * @author moser
 */
@Named
@SessionScoped
public class ServiceHelper implements Serializable
{
	@Serial
	private static final long serialVersionUID = 2240183450244716423L;

	public static final String TOOL = "gICS";
	public static final String NOTIFICATION_CLIENT_ID = TOOL + "_Web";

	@Inject
	private WebAuthContext webAuthContext;

	@EJB(lookup = "java:global/gics/gics-ejb/GICSServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSService")
	private transient GICSService serviceTarget;
	private transient GICSService service;
	protected transient GICSService serviceWithAutomaticNotification;

	@EJB(lookup = "java:global/gics/gics-ejb/GICSManagementServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSManagementService")
	private transient GICSManagementService managerTarget;
	private transient GICSManagementService manager;

	@EJB(lookup = "java:global/gics/gics-ejb/GICSServiceWithNotificationImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSServiceWithNotification")
	private transient GICSServiceWithNotification serviceWithNotificationTarget;
	private transient GICSServiceWithNotification serviceWithNotification;

	@EJB(lookup = "java:global/gics/gics-ejb/GICSFhirServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSFhirService")
	private transient GICSFhirService fhirServiceTarget;
	private transient GICSFhirService fhirService;

	@EJB(lookup = "java:global/gics/gics-ejb/StatisticManagerBean!org.emau.icmvc.ganimed.ttp.cm2.StatisticManager")
	private transient StatisticManager statisticServiceTarget;
	private transient StatisticManager statisticService;

	@EJB
	private transient ChromeDriverService chromeDriverService;

	@PostConstruct
	private void init() {
		if (getWebAuthContext().isUsingDomainBasedRolesDisabled(TTPNames.Tool.gics))
		{
			service = serviceTarget;
			manager = managerTarget;
			serviceWithNotification = serviceWithNotificationTarget;
			fhirService = fhirServiceTarget;
			statisticService = statisticServiceTarget;
		}
		else
		{
			service = getWebAuthContext().createUpdateAuthContextProxy(serviceTarget, GICSService.class);
			manager = getWebAuthContext().createUpdateAuthContextProxy(managerTarget, GICSManagementService.class);
			serviceWithNotification = getWebAuthContext().createUpdateAuthContextProxy(serviceWithNotificationTarget, GICSServiceWithNotification.class);
			fhirService = getWebAuthContext().createUpdateAuthContextProxy(fhirServiceTarget, GICSFhirService.class);
			statisticService = getWebAuthContext().createUpdateAuthContextProxy(statisticServiceTarget, StatisticManager.class);
		}
	}

	public GICSService getService()
	{
		if (service == null)
		{
			init();
		}
		return service;
	}

	public GICSManagementService getManager()
	{
		if (manager == null)
		{
			init();
		}
		return manager;
	}

	public GICSServiceWithNotification getServiceWithNotification()
	{
		if (serviceWithNotification == null)
		{
			init();
		}
		return serviceWithNotification;
	}

	public GICSService getServiceWithAutomaticNotification(boolean notify)
	{
		if (notify && serviceWithAutomaticNotification == null)
		{
			serviceWithAutomaticNotification = ProxyBuilder.wrap(getService(), GICSService.class).withMatchingMethodsDelegatingInvocationHandler(
					getServiceWithNotification(), GICSServiceWithNotification.class, new ProxyBuilder.ArgumentsPrepender(NOTIFICATION_CLIENT_ID)).build();
		}
		return notify ? serviceWithAutomaticNotification : getService();
	}

	public GICSFhirService getFhirService()
	{
		if (fhirService == null)
		{
			init();
		}
		return fhirService;
	}

	public StatisticManager getStatisticService()
	{
		if (statisticService == null)
		{
			init();
		}
		return statisticService;
	}

	public ChromeDriverService getChromeDriverService()
	{
		return chromeDriverService;
	}

	public WebAuthContext getWebAuthContext()
	{
		return webAuthContext;
	}
}
