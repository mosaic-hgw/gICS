package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ca.uhn.fhir.parser.path.EncodeContextPath;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ValidFromPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ValidFromPropertiesObject;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate;
import org.emau.icmvc.magic.fhir.datatypes.config.ConsentDomainConfig;
import org.emau.icmvc.magic.fhir.resources.ExchangeFormatDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.CHECKED_MAJOR_FAULTS;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.NOT_CHECKED;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.ct11;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.ctForDeletion;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.domain1;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.module11;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.module1121;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.module1122;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.module1131;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.module2231;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.policy11;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.policy21;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.policy22;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.policy31;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.qcProblemTypeActionFixLater;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.qcProblemTypeActionFixNow;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.qcProblemTypeWithName;
import static org.emau.icmvc.ganimed.ttp.cm2.TestHelper.qcProblemTypeWithScan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GICSFhirServiceImplTest
{
	private static final Logger logger = LogManager.getLogger(GICSFhirServiceImplTest.class);
	static final String CURRENT_VERSION = "2023.1.0";

	static class TestService extends GICSFhirServiceImpl
	{
		private String currentVersion = CURRENT_VERSION;

		@Override
		String getCurrentVersion()
		{
			return currentVersion;
		}

		void setCurrentVersion(String version)
		{
			this.currentVersion = version;
		}
	}

	TestService service;


	@BeforeEach
	void setup()
	{
		service = new TestService();
	}

	@Test
	void testCheckImportVersion()
	{
		assertTrue(service.checkImportVersion("2023.1.0"));
		assertTrue(service.checkImportVersion("2023.1.5")); // ignoring bugfix part
		assertTrue(service.checkImportVersion("2.0.0"));
		assertTrue(service.checkImportVersion("2.1.0"));
		assertTrue(service.checkImportVersion("2.2.0"));
		assertTrue(service.checkImportVersion("2.13.0"));
		assertTrue(service.checkImportVersion("2.13.4"));
		assertTrue(service.checkImportVersion("2.13.x"));

		assertFalse(service.checkImportVersion("2023.2.0"));
		assertFalse(service.checkImportVersion("2024.1.0"));
	}

	@Test
	void testImportExportJson() throws InvalidExchangeFormatException
	{
		assertExportImport("json");
	}

	@Test
	void testImportExportXml() throws InvalidExchangeFormatException
	{
		assertExportImport("xml");
	}

	void assertExportImport(String format) throws InvalidExchangeFormatException
	{
		EncodeContextPath.class.getClassLoader().setClassAssertionStatus(EncodeContextPath.class.getName(), false);

		ct11.setExternProperties("my-external-properties");
		ct11.setExpirationProperties(new ExpirationPropertiesDTO(new Date(System.currentTimeMillis()), Period.ofDays(100)));
		ct11.setValidFromProperties(new ValidFromPropertiesDTO(new Date(System.currentTimeMillis()), Period.ofDays(100)));

		List<PolicyDTO> policies = List.of(policy21, policy22, policy11, policy31);
		List<ModuleDTO> modules = List.of(module1121, module1122, module1131, module2231, module11);
		List<ConsentTemplateDTO> templates = List.of(ct11, ctForDeletion);
		ExchangeFormatDefinition def = service.prepareExport();
		Date date = new Date((new Date().getTime() / 1000) * 1000); // fhir export ignores time nanos

		DomainDTO domain = new DomainDTO(domain1);
		DomainConfig dc = new DomainConfig(domain.getName(), TestHelper.DOMAIN_PROPERTIES);
		QualityControlConfig qc = dc.getQualityControlConfig();

		qc.getTypeById(NOT_CHECKED).setLabels(Map.of("de", "Nicht kontrolliert", "en", "Not checked"));
		qc.getTypeById(CHECKED_MAJOR_FAULTS).setLabels(Map.of("de", "Schwerwiegende Fehler", "en", "Major faults"));

		qc.getProblemTypes().addAll(Set.of(qcProblemTypeWithName, qcProblemTypeWithScan));
		assertEquals(qc.getProblemTypes(), Set.of(qcProblemTypeWithName, qcProblemTypeWithScan));

		qc.getProblemTypeActions().addAll(Set.of(qcProblemTypeActionFixLater, qcProblemTypeActionFixNow));
		assertEquals(qc.getProblemTypeActions(), Set.of(qcProblemTypeActionFixLater, qcProblemTypeActionFixNow));

		dc.getApplicationConfig().setChromedriverPath("xxx");
		dc.getApplicationConfig().setEnableChromePdfExport(true);

		domain.setConfig(dc);

		domain.setCreationDate(date);
		domain.setUpdateDate(date);

		assertEquals(dc, new ConsentDomainConfig(dc).toDomainConfig()); // roundtrip without marshalling/unmarshalling
		service.exportDefinition(domain, def, true, policies, modules, templates);
		assertEquals(domain.getConfig(), def.getDomain().getConfig().toDomainConfig(), "export definition for domain config");
		String encoded = service.convertToExchangeFormat(def, format, true);
		logger.info("\n{}", encoded);

		ExchangeFormatDefinition defImported = service.validateFormat(encoded, format);

		// contact (person) is compared by object identity
		def.getTemplates().forEach(t -> ((ConsentTemplate) t).setContact(null));
		defImported.getTemplates().forEach(t -> ((ConsentTemplate) t).setContact(null));

		ConsentTemplate ct = (org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate) defImported.getTemplates().getFirst();
		assertEquals(new ExpirationPropertiesObject(ct11.getExpirationProperties()).toPropertiesString(), ct.getExpirationProperties());
		assertEquals(new ValidFromPropertiesObject(ct11.getValidFromProperties()).toPropertiesString(), ct.getValidFromProperties());

		assertEquals(def.getSupportedVersion(), defImported.getSupportedVersion());
		assertEquals(def.getDomain().getConfig().getNotificationsConfig(), defImported.getDomain().getConfig().getNotificationsConfig());
		assertEquals(def.getDomain().getConfig().getPoliciesConfig(), defImported.getDomain().getConfig().getPoliciesConfig());
		assertEquals(def.getDomain().getConfig().getScansConfig(), defImported.getDomain().getConfig().getScansConfig());
		assertEquals(def.getDomain().getConfig().getStatisticConfig(), defImported.getDomain().getConfig().getStatisticConfig());
		assertEquals(def.getDomain().getConfig().getQualityControlConfig(), defImported.getDomain().getConfig().getQualityControlConfig());
		assertEquals(def.getDomain().getConfig().getApplicationConfig(), defImported.getDomain().getConfig().getApplicationConfig());
		assertEquals(def.getDomain().getConfig(), defImported.getDomain().getConfig());
		assertEquals(def.getDomain(), defImported.getDomain());
		assertEquals(def.getPolicies(), defImported.getPolicies());
		assertEquals(def.getModules(), defImported.getModules());
		assertEquals(def.getTemplates(), defImported.getTemplates());
		assertEquals(def, defImported);

		DomainDTO domainImported = service.convertFromFhirConsentDomain(defImported.getDomain());
		// imported dates are always new dates, to compare we set the export dates
		domainImported.setCreationDate(date);
		domainImported.setUpdateDate(date);
		assertEquals(domain.getConfig().getNotificationsConfig(), domainImported.getConfig().getNotificationsConfig());
		assertEquals(domain.getConfig().getPoliciesConfig(), domainImported.getConfig().getPoliciesConfig());
		assertEquals(domain.getConfig().getScansConfig(), domainImported.getConfig().getScansConfig());
		assertEquals(domain.getConfig().getStatisticConfig(), domainImported.getConfig().getStatisticConfig());
		assertEquals(domain.getConfig().getQualityControlConfig(), domainImported.getConfig().getQualityControlConfig());
		assertEquals(domain.getConfig().getApplicationConfig(), domainImported.getConfig().getApplicationConfig());
		assertEquals(domain.getConfig(), domainImported.getConfig());
		assertEquals(domain, domainImported);
	}

	@Test
	void testImportLegacyDomainProperties() throws InvalidExchangeFormatException
	{
		String json = """
				{
				  "resourceType": "ExchangeFormatDefinition",
				  "meta": {
				    "profile": [ "http://example.com/StructureDefinition/dontuse#ExchangeFormatDefinition" ]
				  },
				  "supportedVersion": "2023.1.0",
				  "domain": {
				    "name": "test12345_cm_test",
				    "finalized": false,
				    "label": "dummy",
				    "comment": "test-domain",
				    "signerIdType": [ "mpi_id_type", "another_id_type" ],
				    "policyVersionConverter": "org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter",
				    "moduleVersionConverter": "org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter",
				    "consentTemplateVersionConverter": "org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter",
				    "logo": "logo",
				    "properties": "XXX",
				    "externProperties": "no extern properties",
				    "expirationProperties": "VALIDITY_PERIOD=P1Y2M3D;EXPIRATION_DATE=2023.09.29;EXPIRATION_DATE_FORMAT=yyyy.MM.dd;",
				    "creationDate": "2023-09-28T02:37:20+02:00"
				  }
				}
				""";
		json = json.replace("XXX", TestHelper.DOMAIN_PROPERTIES);
		ExchangeFormatDefinition defImported = service.validateFormat(json, "json");
		logger.info("imported config: {}", defImported.getDomain().getConfig());
		logger.info("imported properties: {}", defImported.getDomain().getProperties());
		assertEquals(new DomainConfig(TestHelper.DOMAIN, TestHelper.DOMAIN_PROPERTIES),
				service.convertFromFhirConsentDomainConfig(TestHelper.DOMAIN,
					defImported.getDomain().getConfig(),
					defImported.getDomain().getProperties()));
	}

	@Test
	public void testMIIDomain() throws IOException, InvalidExchangeFormatException
	{
		String mii = IOUtils.toString(
				Objects.requireNonNull(getClass().getResourceAsStream("2021-08-09_Teilwiderruf_zur_Einwilligungserklärung_16d_kompatibel.json")),
				StandardCharsets.UTF_8
		);

		ExchangeFormatDefinition def = service.validateFormat(mii, "json");
		assertEquals(1, def.getTemplates().size());
		ConsentTemplate template = (ConsentTemplate) def.getTemplates().getFirst();
		assertEquals("Teilwiderruf (kompatibel zu Patienteneinwilligung MII 1.6d)", template.getName());
		assertEquals("Teilwiderruf (kompatibel zu Patienteneinwilligung MII 1.6d)", template.getLabel());
	}
}
