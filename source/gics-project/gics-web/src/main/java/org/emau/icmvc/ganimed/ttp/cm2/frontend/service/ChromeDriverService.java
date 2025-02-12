package org.emau.icmvc.ganimed.ttp.cm2.frontend.service;

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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PreDestroy;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.primefaces.model.DefaultStreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ChromeDriverService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<List<String>, ChromeDriver> drivers = new HashMap<>();

	public ChromeDriver getDriver(List<String> options)
	{
		synchronized (drivers)
		{
			if (!drivers.containsKey(options))
			{
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments(options);
				ChromeDriver driver = new ChromeDriver(chromeOptions);
				logger.info("Started ChromeDriver with options {}", options);
				drivers.put(options, driver);
			}
			return drivers.get(options);
		}
	}

	public DefaultStreamedContent getPDFFromUrl(String url)
	{
		// Start chrome
		ChromeDriver driver = getDriver(
				Arrays.asList("--headless", "--disable-gpu", "--no-sandbox", "--ignore-certificate-errors", "--remote-allow-origins=*", "--silent"));
		System.setProperty(org.openqa.selenium.chrome.ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");

		// Open print url
		driver.get(url);

		// Generate pdf with chrome
		Map<String, Object> output = driver.executeCdpCommand("Page.printToPDF", new HashMap<>());
		byte[] pdf = java.util.Base64.getDecoder().decode((String) output.get("data"));

		if (pdf != null && pdf.length != 0 && PhaseId.RENDER_RESPONSE != FacesContext.getCurrentInstance().getCurrentPhaseId())
		{
			return DefaultStreamedContent.builder().contentType("application/pdf").name("Preview").stream(() -> new ByteArrayInputStream(pdf)).build();
		}
		else
		{
			logger.error("Cannot generate PDF");
			return new DefaultStreamedContent();
		}
	}

	@PreDestroy
	public void destroy()
	{
		logger.info("Closing {} chrome drivers.", drivers.size());
		for (ChromeDriver driver : drivers.values())
		{
			driver.close();
			driver.quit();
		}
	}
}
