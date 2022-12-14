package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component;

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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for displaying media(special getters are needed for correct display). Renders Scan-pdf and
 * signature-png
 *
 * @author weiherg, geidell
 */
@ManagedBean(name = "mediaProvider")
@RequestScoped
public class MediaProviderBean
{
	private static final Logger logger = LoggerFactory.getLogger(MediaProviderBean.class);

	private static final String NO_SCAN_FILE = "/invalid_scan.pdf";
	private static final DefaultStreamedContent emptyDummy = new DefaultStreamedContent();
	private static final byte[] noScan;

	static
	{
		byte[] temp = new byte[0];
		try
		{
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SCAN_FILE);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			temp = buffer.toByteArray();
		}
		catch (Exception e)
		{
			logger.error("can't read file '" + NO_SCAN_FILE + "'", e);
		}
		noScan = temp;

	}

	public DefaultStreamedContent getConsentPDFStreamDownload()
	{
		DefaultStreamedContent result = null;

		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String scanBase64 = (String) session.getAttribute("preview_pdf");
		String name = (String) session.getAttribute("preview_name") + ".pdf";
		// kein trim, da der aufruf wahrscheinlich nach getAllConsentsForDomainWithoutScan kommt und
		// dieses (genauer: der criteriabuilder) "trim" nicht unterstuetzt
		if (scanBase64 != null && !scanBase64.isEmpty())
		{
			byte[] out = Base64.getDecoder().decode(scanBase64);
			if (out == null || out.length == 0)
			{
				logger.warn("scan is not base64 encoded: {}", scanBase64);
				result = DefaultStreamedContent.builder().contentType("application/pdf").name(name).stream(() -> new ByteArrayInputStream(noScan)).build();
			}
			else
			{
				result = DefaultStreamedContent.builder().contentType("application/pdf").name(name).stream(() -> new ByteArrayInputStream(out)).build();
			}
		}
		else
		{
			if (logger.isInfoEnabled())
			{
				logger.info("no scan found");
			}
		}
		return result;
	}

	public StreamedContent getConsentPDFStream()
	{
		FacesContext context = FacesContext.getCurrentInstance();
		DefaultStreamedContent consentPDFStream;

		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE)
		{
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			consentPDFStream = emptyDummy;
		}
		else
		{
			consentPDFStream = getConsentPDFStreamDownload();
		}

		return consentPDFStream;
	}
	
	public String getRandomId()
	{
		return UUID.randomUUID().toString();
	}
}
