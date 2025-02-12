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
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.emau.icmvc.ganimed.ttp.cm2.GICSFhirService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSManagementService;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.StatisticManager;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.service.ChromeDriverService;
import org.icmvc.ttp.web.controller.AbstractBean;
import org.icmvc.ttp.web.util.WebAuthContext;

/**
 * An abstract bean  which encapsulates the gICS service interfaces
 * to ensure intercepting all calls to service methods
 * with updating the current auth context in the thread local context.
 *
 * @author moser
 */
public abstract class AbstractGICSServiceBean extends AbstractBean implements Serializable
{
    @Serial
    private static final long serialVersionUID = 7394720457314722740L;

    protected static final String TOOL = ServiceHelper.TOOL;

    @Inject
    private ServiceHelper serviceHelper;

    @PostConstruct
    private void init() {
    }

    public ServiceHelper getServiceHelper()
    {
        if (serviceHelper == null)
        {
            // https://github.com/eclipse-ee4j/mojarra/issues/4308
            // If the above does not work here, too: "@Inject private ServiceHelper serviceHelper;"
            // but "CDI.current().select(ServiceHelper.class).get();" helps
            serviceHelper = CDI.current().select(ServiceHelper.class).get();
        }

        return serviceHelper;
    }

    public GICSService getService()
    {
        return getServiceHelper().getService();
    }

    public GICSService getServiceWithAutomaticNotification(boolean notify)
    {
        return getServiceHelper().getServiceWithAutomaticNotification(notify);
    }

    public GICSManagementService getManager()
    {
        return getServiceHelper().getManager();
    }

    public GICSFhirService getFhirService()
    {
        return getServiceHelper().getFhirService();
    }

    public StatisticManager getStatisticService()
    {
        return getServiceHelper().getStatisticService();
    }
    
    public ChromeDriverService getChromeDriverService() {
        return getServiceHelper().getChromeDriverService();
    }

    @Override
    public WebAuthContext getWebAuthContext()
    {
        return getServiceHelper().getWebAuthContext();
    }

    public String getTool() {
        return TOOL;
    }
}
