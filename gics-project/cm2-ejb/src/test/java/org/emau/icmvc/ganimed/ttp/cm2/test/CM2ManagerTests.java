package org.emau.icmvc.ganimed.ttp.cm2.test;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidConsentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorMaintenanceVersionConverter;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter;
import org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CM2ManagerTests {

	/*-
	loeschscript:
	delete from signed_policy where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from signature where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from free_text_val where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from consent where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from module_consent_template where CT_DOMAIN = 'test12345_cm_test' or CT_DOMAIN = 'test12345_cm_version_test' or CT_DOMAIN = 'test12345_cm_deletion_test';
	delete from module_policy where M_DOMAIN_NAME = 'test12345_cm_test' or M_DOMAIN_NAME = 'test12345_cm_version_test' or M_DOMAIN_NAME = 'test12345_cm_deletion_test';
	delete from free_text_def where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test';
	delete from consent_template where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test';
	delete from module where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test';
	delete from policy where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test';
	delete from text where ID like 'test12345_cm_test%' or ID like 'test12345_cm_version_test%' or ID like 'test12345_cm_deletion_test%';
	delete from virtual_person_signer_id where SIT_DOMAIN_NAME = 'test12345_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from signer_id where SIT_DOMAIN_NAME = 'test12345_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test';
	delete from signer_id_type where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'crash-test-dummy';
	delete from domain where NAME = 'test12345_cm_test' or NAME = 'test12345_cm_version_test' or NAME = 'test12345_cm_deletion_test' or NAME = 'crash-test-dummy';
	 */
	private static final String ANOTHER_ID_TYPE = "another_id_type";
	private static final String MPI_ID_TYPE = "mpi_id_type";
	private static final String NO_TITLE = "no_title";
	private static final String NO_COMMENT = "no_comment";
	private static final String NO_EXTERN_PROPERTIES = "no_extern_properties";
	private static final String DOMAIN = "test12345_cm_test";
	private static final String VERSION_DOMAIN = "test12345_cm_version_test";
	private static final String DELETION_DOMAIN = "test12345_cm_deletion_test";
	private static final String CM2_URL = "http://localhost:8080/gics/gicsService?wsdl";
	private static GICSService cm2Manager;
	private static final Logger logger = Logger.getLogger(CM2ManagerTests.class);
	private static final String DELETION_TEST = "deletion_test";

	private final static PolicyKeyDTO policyKey11 = new PolicyKeyDTO(DOMAIN, "test1", "1");
	private final static PolicyDTO policy11 = new PolicyDTO(policyKey11);
	private final static PolicyKeyDTO policyKey21 = new PolicyKeyDTO(DOMAIN, "test2", "1");
	private final static PolicyDTO policy21 = new PolicyDTO(policyKey21);
	private final static PolicyKeyDTO policyKey22 = new PolicyKeyDTO(DOMAIN, "test2", "2");
	private final static PolicyDTO policy22 = new PolicyDTO(policyKey22);
	private final static PolicyKeyDTO policyKey31 = new PolicyKeyDTO(DOMAIN, "test3", "1");
	private final static PolicyDTO policy31 = new PolicyDTO(policyKey31);
	private final static PolicyKeyDTO policyKeyForDeletion = new PolicyKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static PolicyDTO policyForDeletion = new PolicyDTO(policyKeyForDeletion);
	private final static ModuleKeyDTO moduleKey11 = new ModuleKeyDTO(DOMAIN, "test11", "1");
	private final static ModuleDTO module11 = new ModuleDTO(moduleKey11, "kein sinnvoller modultext 11", NO_TITLE, NO_COMMENT, NO_EXTERN_PROPERTIES,
			Arrays.asList(policy11));
	private final static ModuleKeyDTO moduleKey1121 = new ModuleKeyDTO(DOMAIN, "test1121", "1");
	private final static ModuleDTO module1121 = new ModuleDTO(moduleKey1121, "kein sinnvoller modultext 1121", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Arrays.asList(policy11, policy21));
	private final static ModuleKeyDTO moduleKey1122 = new ModuleKeyDTO(DOMAIN, "test1122", "1");
	private final static ModuleDTO module1122 = new ModuleDTO(moduleKey1122, "kein sinnvoller modultext 1122", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Arrays.asList(policy11, policy21));
	private final static ModuleKeyDTO moduleKey1131 = new ModuleKeyDTO(DOMAIN, "test1131", "1");
	private final static ModuleDTO module1131 = new ModuleDTO(moduleKey1131, "kein sinnvoller modultext 1131", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Arrays.asList(policy11, policy31));
	private final static ModuleKeyDTO moduleKey2231 = new ModuleKeyDTO(DOMAIN, "test2231", "2");
	private final static ModuleDTO module2231 = new ModuleDTO(moduleKey2231, "kein sinnvoller modultext 2231", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Arrays.asList(policy22, policy31));
	private final static ModuleKeyDTO moduleKeyForDeletion = new ModuleKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static ModuleDTO moduleForDeletion = new ModuleDTO(moduleKeyForDeletion, "kein sinnvoller modultext", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Arrays.asList(policyForDeletion));
	private final static ConsentTemplateKeyDTO ctKey11 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "1");
	private final static ConsentTemplateKeyDTO ctKey12 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "2");
	private final static ConsentTemplateKeyDTO ctKeyForDeletion = new ConsentTemplateKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static ConsentTemplateDTO ct11 = new ConsentTemplateDTO(ctKey11);
	private final static ConsentTemplateKeyDTO ctKey21 = new ConsentTemplateKeyDTO(DOMAIN, "testCT2", "1");
	private final static ConsentTemplateDTO ctForDeletion = new ConsentTemplateDTO(ctKeyForDeletion);
	private final static List<ConsentStatus> allConsentStatus = Arrays.asList(ConsentStatus.values());
	private final static AssignedModuleDTO am11 = new AssignedModuleDTO(module11, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties");
	private final static AssignedModuleDTO am1121 = new AssignedModuleDTO(module1121, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties");
	private final static AssignedModuleDTO am1122 = new AssignedModuleDTO(module1122, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties");
	private final static AssignedModuleDTO am1131 = new AssignedModuleDTO(module1131, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties");
	private final static AssignedModuleDTO am2231 = new AssignedModuleDTO(module2231, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, moduleKey11,
			"no comment", "no extern properties");
	private final static AssignedModuleDTO amForDeletion = new AssignedModuleDTO(moduleForDeletion, true, ConsentStatus.UNKNOWN, allConsentStatus, 0,
			null, "no comment", "no extern properties");
	private final static List<String> idTypes = Arrays.asList(MPI_ID_TYPE, ANOTHER_ID_TYPE);

	@BeforeClass
	public static void storeBasicEntries() throws Exception {
		logger.info("setup");
		QName serviceName = new QName("http://cm2.ttp.ganimed.icmvc.emau.org/", "GICSServiceImplService");
		URL wsdlURL = new URL(CM2_URL);
		Service service = Service.create(wsdlURL, serviceName);
		Assert.assertNotNull("webservice object for CM2Manager is null", service);
		cm2Manager = (GICSService) service.getPort(GICSService.class);
		Assert.assertNotNull("cm2 manager object is null", cm2Manager);

		try {
			cm2Manager.getDomain(DOMAIN);
		} catch (UnknownDomainException maybe) {
			try {
				DomainDTO domainDTO = new DomainDTO(DOMAIN, "dummy", SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
						SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo", idTypes);
				logger.info("creating test domain: " + domainDTO);
				cm2Manager.addDomain(domainDTO);
			} catch (DuplicateEntryException ignore) {
				// geht nicht
			}
		}

		try {
			cm2Manager.getDomain(VERSION_DOMAIN);
		} catch (UnknownDomainException maybe) {
			try {
				DomainDTO domainDTO = new DomainDTO(VERSION_DOMAIN, "dummy", MajorMinorVersionConverter.class.getName(),
						MajorMinorVersionConverter.class.getName(), MajorMinorMaintenanceVersionConverter.class.getName(), "", "version-test-domain",
						"no extern properties", "logo", idTypes);
				logger.info("creating version test domain: " + domainDTO);
				cm2Manager.addDomain(domainDTO);
			} catch (DuplicateEntryException ignore) {
				// geht nicht
			}
		}

		logger.info("create policies");
		cm2Manager.addPolicy(policy11);
		cm2Manager.addPolicy(policy21);
		cm2Manager.addPolicy(policy22);
		cm2Manager.addPolicy(policy31);

		logger.info("create modules");
		cm2Manager.addModule(module11);
		cm2Manager.addModule(module1121);
		cm2Manager.addModule(module1122);
		cm2Manager.addModule(module1131);
		cm2Manager.addModule(module2231);

		logger.info("create consent templates");
		ConsentTemplateDTO ct12 = new ConsentTemplateDTO(ctKey12);
		ConsentTemplateDTO ct21 = new ConsentTemplateDTO(ctKey21);
		ct11.getAssignedModules().add(am1121);
		ct12.getAssignedModules().add(am1122);
		ct21.getAssignedModules().add(am11);
		ct21.getAssignedModules().add(am2231);
		ct11.setType(ConsentTemplateType.CONSENT);
		ct12.setType(ConsentTemplateType.CONSENT);
		ct21.setType(ConsentTemplateType.CONSENT);
		cm2Manager.addConsentTemplate(ct11);
		cm2Manager.addConsentTemplate(ct12);
		cm2Manager.addConsentTemplate(ct21);
		logger.info("setup complete");
	}

	@AfterClass
	public static void deleteBasicEntries() throws Exception {
		logger.info("teardown");
		logger.info("delete the consent templates");
		cm2Manager.deleteConsentTemplate(ctKey11);
		cm2Manager.deleteConsentTemplate(ctKey12);
		cm2Manager.deleteConsentTemplate(ctKey21);

		logger.info("delete the modules");
		cm2Manager.deleteModule(moduleKey11);
		cm2Manager.deleteModule(moduleKey1121);
		cm2Manager.deleteModule(moduleKey1122);
		cm2Manager.deleteModule(moduleKey1131);
		cm2Manager.deleteModule(moduleKey2231);

		logger.info("delete the policies");
		cm2Manager.deletePolicy(policyKey11);
		cm2Manager.deletePolicy(policyKey21);
		cm2Manager.deletePolicy(policyKey22);
		cm2Manager.deletePolicy(policyKey31);
		logger.info("finished");
	}

	@Test
	public void basicTest() throws Exception {
		logger.info("### basic test start");
		logger.info("try to create a duplicate policy");
		try {
			cm2Manager.addPolicy(policy11);
			Assert.fail("could create a duplicate policy");
		} catch (DuplicateEntryException expected) {
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a duplicate consent template");
		try {
			cm2Manager.addConsentTemplate(ct11);
			Assert.fail("could create a duplicate consent template");
		} catch (DuplicateEntryException expected) {
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to delete a used policy");
		try {
			cm2Manager.deletePolicy(policyKey11);
			Assert.fail("could delete a policy which is in use");
		} catch (ObjectInUseException expected) {
			logger.info("expected ObjectInUseException: " + expected);
		}

		logger.info("try to create a consent template with duplicate modules");
		ConsentTemplateKeyDTO tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		ConsentTemplateDTO tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am11);
		tempCT.getAssignedModules().add(am11);
		try {
			cm2Manager.addConsentTemplate(tempCT);
			Assert.fail("could create a consent template with duplicate modules");
		} catch (DuplicateEntryException expected) {
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a consent template with duplicate policies");
		tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am1121);
		tempCT.getAssignedModules().add(am1131);
		try {
			cm2Manager.addConsentTemplate(tempCT);
			Assert.fail("could create a consent template with duplicate policies");
		} catch (DuplicateEntryException expected) {
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a consent template with duplicate policies in different versions");
		tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am1121);
		tempCT.getAssignedModules().add(am2231);
		try {
			cm2Manager.addConsentTemplate(tempCT);
			Assert.fail("could create a consent template with duplicate policies in different versions");
		} catch (DuplicateEntryException expected) {
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a consent template with a module with a parent which is not part of that consent template");
		tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am2231);
		try {
			cm2Manager.addConsentTemplate(tempCT);
			Assert.fail("could create a consent template with a module with a parent which is not part of that consent template");
		} catch (UnknownModuleException expected) {
			logger.info("expected UnknownModuleException: " + expected);
		}

		logger.info("try to create a module with an unknown policy");
		PolicyKeyDTO policyKeyUnknown = new PolicyKeyDTO(DOMAIN, "testUnknown", "1");
		PolicyDTO policyUnknown = new PolicyDTO(policyKeyUnknown);
		ModuleKeyDTO moduleKeyForUP = new ModuleKeyDTO(DOMAIN, "test UP", "2");
		ModuleDTO moduleForUP = new ModuleDTO(moduleKeyForUP, "kein sinnvoller modultext UP", NO_TITLE, NO_COMMENT, NO_EXTERN_PROPERTIES,
				Arrays.asList(policyUnknown));
		try {
			cm2Manager.addModule(moduleForUP);
			Assert.fail("could create a module with an unknown policy");
		} catch (UnknownPolicyException expected) {
			logger.info("expected UnknownPolicyException: " + expected);
		}

		logger.info("try to create a consent template with an unknown modul");
		ModuleKeyDTO moduleKeyUnknown = new ModuleKeyDTO(DOMAIN, "testUnknown", "1");
		ModuleDTO moduleUnknown = new ModuleDTO(moduleKeyUnknown, "", "", "", "", new ArrayList<PolicyDTO>());
		AssignedModuleDTO amUnknown = new AssignedModuleDTO(moduleUnknown, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null, "", "");
		tempCT.getAssignedModules().clear();
		tempCT.getAssignedModules().add(amUnknown);
		try {
			cm2Manager.addConsentTemplate(tempCT);
			Assert.fail("could create a consent template with an unknown module");
		} catch (UnknownModuleException expected) {
			logger.info("expected UnknownModuleException: " + expected);
		}

		logger.info("try to update some consent template fields");
		Date date = new Date();
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;
		String text = "nur ein testtext";
		String pdfType = "pdf";
		cm2Manager.updateConsentTemplate(ctKey11, newExternProperties, newComment, text, pdfType);
		tempCT = cm2Manager.getConsentTemplate(ctKey11);
		Assert.assertEquals(newExternProperties, tempCT.getExternProperties());
		Assert.assertEquals(newComment, tempCT.getComment());
		Assert.assertEquals(text, tempCT.getScanBase64());
		Assert.assertEquals(pdfType, tempCT.getScanFileType());

		logger.info("try to update some policy fields");
		cm2Manager.updatePolicy(policyKey11, newExternProperties, newComment);
		PolicyDTO tempPolicy = cm2Manager.getPolicy(policyKey11);
		Assert.assertEquals(newExternProperties, tempPolicy.getExternProperties());
		Assert.assertEquals(newComment, tempPolicy.getComment());

		logger.info("try to update some module fields");
		String title = "title_" + date;
		cm2Manager.updateModule(moduleKey11, title, newExternProperties, newComment);
		ModuleDTO tempModule = cm2Manager.getModule(moduleKey11);
		Assert.assertEquals(newExternProperties, tempModule.getExternProperties());
		Assert.assertEquals(newComment, tempModule.getComment());
		Assert.assertEquals(title, tempModule.getTitle());
		logger.info("### basic test end");
	}

	@Test
	public void listTest() throws Exception {
		logger.info("### list test start");
		logger.info("list all consent templates");
		List<ConsentTemplateDTO> all = cm2Manager.listConsentTemplates(DOMAIN);
		Assert.assertTrue("wrong number of consent templates: " + all.size(), all.size() > 2 || all.size() < 6); // je nachdem, welche tests schon ausgefuehrt wurden
		Assert.assertTrue("wrong number of consent templates: " + all.size(), all.size() > 2 || all.size() < 6); // je nachdem, welche tests schon ausgefuehrt wurden
		for (ConsentTemplateDTO dto : all) {
			logger.info(dto);
		}
		logger.info("get the newest version of consent template 'testCT1'");
		ConsentTemplateDTO newest = cm2Manager.getCurrentConsentTemplate("testCT1", DOMAIN);
		Assert.assertEquals("wrong version number for newest consent template", newest.getKey().getVersion(), "2");
		logger.info(newest);
		logger.info("get list with the newest version of each consent template");
		List<ConsentTemplateDTO> currentCTs = cm2Manager.listCurrentConsentTemplates(DOMAIN);
		for (ConsentTemplateDTO ct : currentCTs) {
			logger.info(ct);
		}
		logger.info("list all consents without scans");
		List<ConsentLightDTO> consents = cm2Manager.getAllConsentsForDomainWithoutScan(DOMAIN);
		for (ConsentLightDTO consentLight : consents) {
			ConsentDTO consent = cm2Manager.getConsent(consentLight.getKey());
			Assert.assertTrue("getAllConsentsForDomainWithoutScan got a consent WITH scan: " + consent.getKey(),
					consent.getScanBase64() == null || consent.getScanBase64().isEmpty()); // kein trim, die abfrage ueber criteriabuilder gibt das nicht her
		}
		logger.info("found " + consents.size() + " consents without scan");
		logger.info("list consents paginated (2 consents per page)");
		int i = 0;
		do {
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, i++, 2, ConsentField.DATE, true, new HashMapWrapper<ConsentField, Object>());
			for (ConsentLightDTO dto : consents) {
				logger.info("page " + i + ": " + dto);
			}
		} while (consents.size() > 0);
		logger.info("list consents paginated (3 consents per page)");
		i = 0;
		do {
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, i++, 3, ConsentField.CT_NAME, true,
					new HashMapWrapper<ConsentField, Object>());
			for (ConsentLightDTO dto : consents) {
				logger.info("page " + i + ": " + dto);
			}
		} while (consents.size() > 0);
		logger.info("### list test end");
	}

	@Test
	public void consentTest() throws Exception {
		logger.info("### consent test start");
		final String policyNameMandatory = "policyForConsentTestMandatory";
		final String policyNameMandatory2 = "policyForConsentTestMandatory2";
		final String policyNameMandatory3 = "policyForConsentTestMandatory3";
		final String policyNameOptional = "policyForConsentTestOptional";
		final List<PolicyKeyDTO> emptyPolicyKeyList = new ArrayList<PolicyKeyDTO>();
		final PolicyKeyDTO policyKeyForConsentTestMandatory = new PolicyKeyDTO(DOMAIN, policyNameMandatory, "1");
		final PolicyDTO policyForConsentTestMandatory = new PolicyDTO(policyKeyForConsentTestMandatory, "no comment", "no extern properties");
		final PolicyKeyDTO policyKeyForConsentTestMandatory2 = new PolicyKeyDTO(DOMAIN, policyNameMandatory2, "1");
		final PolicyDTO policyForConsentTestMandatory2 = new PolicyDTO(policyKeyForConsentTestMandatory2);
		final PolicyKeyDTO policyKeyForConsentTestMandatory3 = new PolicyKeyDTO(DOMAIN, policyNameMandatory3, "1");
		final PolicyDTO policyForConsentTestMandatory3 = new PolicyDTO(policyKeyForConsentTestMandatory3);
		final PolicyKeyDTO policyKeyForConsentTestOptional = new PolicyKeyDTO(DOMAIN, policyNameOptional, "1");
		final PolicyDTO policyForConsentTestOptional = new PolicyDTO(policyKeyForConsentTestOptional);
		try {
			cm2Manager.addPolicy(policyForConsentTestMandatory);
			cm2Manager.addPolicy(policyForConsentTestMandatory2);
			cm2Manager.addPolicy(policyForConsentTestMandatory3);
			cm2Manager.addPolicy(policyForConsentTestOptional);
		} catch (DuplicateEntryException ignore) {
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}
		ModuleKeyDTO moduleKeyForConsentTestMandatory = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestMandatory", "1");
		ModuleDTO moduleForConsentTestMandatory = new ModuleDTO(moduleKeyForConsentTestMandatory, "dummy text mandatory", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Arrays.asList(policyForConsentTestMandatory));
		ModuleKeyDTO moduleKeyForConsentTestMandatory2 = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestMandatory2", "1");
		ModuleDTO moduleForConsentTestMandatory2 = new ModuleDTO(moduleKeyForConsentTestMandatory2, "dummy text mandatory 2", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Arrays.asList(policyForConsentTestMandatory2, policyForConsentTestMandatory3));
		ModuleKeyDTO moduleKeyForConsentTestOptional = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestOptional", "1");
		ModuleDTO moduleForConsentTestOptional = new ModuleDTO(moduleKeyForConsentTestOptional, "dummy text optional", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Arrays.asList(policyForConsentTestOptional));
		try {
			cm2Manager.addModule(moduleForConsentTestMandatory);
			cm2Manager.addModule(moduleForConsentTestMandatory2);
			cm2Manager.addModule(moduleForConsentTestOptional);
		} catch (DuplicateEntryException ignore) {
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}
		AssignedModuleDTO amMandatory = new AssignedModuleDTO(moduleForConsentTestMandatory, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
				"no comment", "no extern properties");
		AssignedModuleDTO amMandatory2 = new AssignedModuleDTO(moduleForConsentTestMandatory2, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
				"no comment", "no extern properties");
		AssignedModuleDTO amOptional = new AssignedModuleDTO(moduleForConsentTestOptional, false, ConsentStatus.UNKNOWN, allConsentStatus, 0,
				moduleKeyForConsentTestMandatory, "no comment", "no extern properties");
		ConsentTemplateKeyDTO ctKeyConsentTest = new ConsentTemplateKeyDTO(DOMAIN, "ConsentTest", "1");
		ConsentTemplateDTO ctConsentTest = new ConsentTemplateDTO(ctKeyConsentTest);
		ctConsentTest.setType(ConsentTemplateType.CONSENT);
		ctConsentTest.setComment("no comment");
		ctConsentTest.setExternProperties("no extern properties");
		ctConsentTest.getAssignedModules().add(amMandatory);
		ctConsentTest.getAssignedModules().add(amMandatory2);
		ctConsentTest.getAssignedModules().add(amOptional);
		try {
			cm2Manager.addConsentTemplate(ctConsentTest);
		} catch (DuplicateEntryException ignore) {
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}
		logger.info("try to add invalid consents");
		String id = "12345_test";
		Set<SignerIdDTO> ids = new HashSet<SignerIdDTO>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, id));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKeyConsentTest, ids, new Date());
		long time = consentKey.getConsentDate().getTime();
		consentKey.getConsentDate().setTime(time - time % 1000); // db speichert keine millis
		ConsentDTO consent = new ConsentDTO(consentKey);
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with a missing consent status");
		} catch (MissingRequiredObjectException expected) {
			logger.info("expected MissingRequiredObjectException: " + expected);
		}
		ModuleKeyDTO invalidModuleKeyForConsent = new ModuleKeyDTO(DOMAIN, "gibt's nich", "1");
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory2,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory2, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestOptional,
				new ModuleStateDTO(moduleKeyForConsentTestOptional, ConsentStatus.UNKNOWN, emptyPolicyKeyList));
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent without the mandatory signatures and signature dates");
		} catch (MissingRequiredObjectException expected) {
			logger.info("expected MissingRequiredObjectException: " + expected);
		}
		consent.setPatientSignatureBase64("dummy");
		consent.setPatientSigningDate(new Date());
		consent.setPhysicanSignatureBase64("dummy");
		consent.setPhysicanSigningDate(new Date());
		consent.setPhysicanId("123");
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(invalidModuleKeyForConsent,
				new ModuleStateDTO(invalidModuleKeyForConsent, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with an invalid policy");
		} catch (UnknownModuleException expected) {
			logger.info("expected UnknownModuleException: " + expected);
		}
		consent.getModuleStates().clear();
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory2,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory2, ConsentStatus.DECLINED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestOptional,
				new ModuleStateDTO(moduleKeyForConsentTestOptional, ConsentStatus.UNKNOWN, emptyPolicyKeyList));
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with a mandatory-fields-logic-error");
		} catch (MandatoryFieldsException expected) {
			logger.info("expected MandatoryFieldsException: " + expected);
		}
		logger.info("add valid consent");
		consent.getModuleStates().clear();
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory2,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory2, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestOptional,
				new ModuleStateDTO(moduleKeyForConsentTestOptional, ConsentStatus.UNKNOWN, emptyPolicyKeyList));
		consent.setComment("no comment");
		consent.setExternProperties("no extern properties");
		cm2Manager.addConsent(consent);
		logger.info("check consent states");
		CheckConsentConfig config = new CheckConsentConfig();
		Assert.assertTrue("isConsented() returned 'false' where it should return 'true'",
				cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config));
		Assert.assertTrue("isConsented(from, to) returned 'false' where it should return 'true'",
				cm2Manager.isConsentedFromIncludingToIncluding(ids, DOMAIN, policyNameMandatory, "1", "1", config));
		Assert.assertFalse("isConsented(from, to) returned 'true' where it should return 'false'",
				cm2Manager.isConsentedFromExcludingToIncluding(ids, DOMAIN, policyNameMandatory, "1", "1", config));
		Assert.assertFalse("isConsented(from, to) returned 'true' where it should return 'false'",
				cm2Manager.isConsentedFromIncludingToExcluding(ids, DOMAIN, policyNameMandatory, "1", "1", config));
		Assert.assertFalse("isConsented(from, to) returned 'true' where it should return 'false'",
				cm2Manager.isConsentedFromExcludingToExcluding(ids, DOMAIN, policyNameMandatory, "1", "1", config));

		logger.info("### add scan to consent");
		String text = "nur ein test";
		final String txtType = "txt";
		final String pdfType = "pdf";
		cm2Manager.addScanToConsent(consentKey, text, txtType);
		consent = cm2Manager.getConsent(consentKey);
		Assert.assertEquals(text, consent.getScanBase64());
		Assert.assertEquals(txtType, consent.getScanFileType());

		logger.info("try to update some consent fields");
		Date date = new Date();
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;
		text = "ein neuer testtext";
		cm2Manager.updateConsent(consentKey, newExternProperties, newComment, text, pdfType);
		consent = cm2Manager.getConsent(consentKey);
		Assert.assertEquals(newExternProperties, consent.getExternProperties());
		Assert.assertEquals(newComment, consent.getComment());
		Assert.assertEquals(text, consent.getScanBase64());
		Assert.assertEquals(pdfType, consent.getScanFileType());

		logger.info("test getAllConsentsForPerson");
		List<ConsentLightDTO> consentsForPerson = cm2Manager.getAllConsentsForPerson(DOMAIN, ids);
		logger.info("found " + consentsForPerson.size() + " consents");

		logger.info("try to invalidate consent");
		try {
			cm2Manager.invalidateConsent(consent.getKey(), ConsentStatus.ACCEPTED, "no comment", null);
			Assert.fail("could \"invalidate\" a consent with ConsentStatus.ACCEPTED");
		} catch (InvalidConsentStatusException expected) {
			logger.info("expected InvalidConsentStatusException: " + expected.getMessage());
		}
		try {
			cm2Manager.invalidateConsent(consent.getKey(), ConsentStatus.NOT_ASKED, "no comment", null);
			Assert.fail("could \"invalidate\" a consent with ConsentStatus.NOT_ASKED");
		} catch (InvalidConsentStatusException expected) {
			logger.info("expected InvalidConsentStatusException: " + expected.getMessage());
		}
		Thread.sleep(1000); // 1 sekunde pause wegen pk vom consent, da ist die zeit mit drin; sonst duplicate key moeglich
		cm2Manager.invalidateConsent(consent.getKey(), ConsentStatus.REVOKED, "no comment", null);
		Assert.assertFalse("isConsented() returned 'true' where it should return 'false'",
				cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config));
		logger.info("### consent test end");
	}

	@Test
	public void versionTest() throws Exception {
		logger.info("### version test start");
		try {
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", "abc.def.ghi", SimpleVersionConverter.class.getName(),
					SimpleVersionConverter.class.getName(), "", "crash-test-domain", "no extern properties", "", idTypes);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assert.fail("could create a domain with an unknown ctVersionConverterClass");
		} catch (VersionConverterClassException expected) {
			logger.info("expected VersionConverterClassException: " + expected);
		}
		try {
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", SimpleVersionConverter.class.getName(), "abc.def.ghi",
					SimpleVersionConverter.class.getName(), "", "crash-test-domain", "no extern properties", "", idTypes);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assert.fail("could create a domain with an unknown moduleVersionConverterClass");
		} catch (VersionConverterClassException expected) {
			logger.info("expected VersionConverterClassException: " + expected);
		}
		try {
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", SimpleVersionConverter.class.getName(),
					SimpleVersionConverter.class.getName(), "abc.def.ghi", "", "crash-test-domain", "no extern properties", "", idTypes);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assert.fail("could create a domain with an unknown policyVersionConverterClass");
		} catch (VersionConverterClassException expected) {
			logger.info("expected VersionConverterClassException: " + expected);
		}

		logger.info("try to add some policies with invalid version strings");
		try {
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		try {
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "999.111");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		try {
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1111.1.2");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}

		logger.info("add some policies with valid version strings");
		PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1.999.0");
		PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
		cm2Manager.addPolicy(versionPolicy);
		cm2Manager.deletePolicy(versionPolicyKey);
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1.0.0");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		cm2Manager.addPolicy(versionPolicy);
		cm2Manager.deletePolicy(versionPolicyKey);
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "999.999.999");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		cm2Manager.addPolicy(versionPolicy);
		cm2Manager.deletePolicy(versionPolicyKey);
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "01.002.030");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		cm2Manager.addPolicy(versionPolicy);
		cm2Manager.deletePolicy(versionPolicyKey);

		logger.info("trying to add some policies with invalid version strings");
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", null);
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try {
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string (null)");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try {
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string (empty string)");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1.2.3.4");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try {
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string (4 parts)");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1..2");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try {
			cm2Manager.addPolicy(versionPolicy);
			Assert.fail("could create a policy with an invalid version string (two dots)");
		} catch (InvalidVersionException expected) {
			logger.info("expected InvalidVersionException: " + expected);
		}
		logger.info("### version test end");
	}

	@Test
	public void freeTextTest() throws Exception {
		logger.info("### free text test start");
		String REQUIRED_FREE_TEXT = "free text string";
		String DATE_FREE_TEXT = "free text date";
		String INTEGER_FREE_TEXT = "free text integer";
		String DOUBLE_FREE_TEXT = "free text double";

		logger.info("create a consent template with free texts");
		ConsentTemplateKeyDTO tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "freeTextCT", "1");
		ConsentTemplateDTO tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		FreeTextDefDTO freeText1 = new FreeTextDefDTO(REQUIRED_FREE_TEXT, true, FreeTextType.String, "", 1, "comment");
		tempCT.getFreeTextDefs().add(freeText1);
		FreeTextDefDTO freeText2 = new FreeTextDefDTO(DATE_FREE_TEXT, false, FreeTextType.Date, "dd.MM.yyyy", 2, "comment");
		tempCT.getFreeTextDefs().add(freeText2);
		FreeTextDefDTO freeText3 = new FreeTextDefDTO(INTEGER_FREE_TEXT, false, FreeTextType.Integer, "", 3, "comment");
		tempCT.getFreeTextDefs().add(freeText3);
		FreeTextDefDTO freeText4 = new FreeTextDefDTO(DOUBLE_FREE_TEXT, false, FreeTextType.Double, "", 4, "comment");
		tempCT.getFreeTextDefs().add(freeText4);
		try {
			cm2Manager.addConsentTemplate(tempCT);
		} catch (DuplicateEntryException ignore) {
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}

		logger.info("try to create a consent without the mandatory free text");
		Set<SignerIdDTO> ids = new HashSet<SignerIdDTO>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "xyz"));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(tempCTKey, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent without the mandatory free text");
		} catch (MissingRequiredObjectException expected) {
			logger.info("expected MissingRequiredObjectException: " + expected);
		}

		logger.info("try to create a consent with invalid free texts");
		consent.getFreeTextVals().put(DATE_FREE_TEXT, "11.13");
		consent.getFreeTextVals().put(REQUIRED_FREE_TEXT, "test");
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with invalid free texts");
		} catch (InvalidFreeTextException expected) {
			logger.info("expected InvalidFreeTextException: " + expected);
		}
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().put(INTEGER_FREE_TEXT, "1.1");
		consent.getFreeTextVals().put(REQUIRED_FREE_TEXT, "test");
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with invalid free texts");
		} catch (InvalidFreeTextException expected) {
			logger.info("expected InvalidFreeTextException: " + expected);
		}
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().put(DOUBLE_FREE_TEXT, "a.1");
		consent.getFreeTextVals().put(REQUIRED_FREE_TEXT, "test");
		try {
			cm2Manager.addConsent(consent);
			Assert.fail("could create a consent with invalid free texts");
		} catch (InvalidFreeTextException expected) {
			logger.info("expected InvalidFreeTextException: " + expected);
		}

		logger.info("try to create a consent for that consent template with valid free texts");
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().put(REQUIRED_FREE_TEXT, "test");
		consent.getFreeTextVals().put(DATE_FREE_TEXT, "18.03.2014");
		consent.getFreeTextVals().put(DOUBLE_FREE_TEXT, "465.134");
		consent.getFreeTextVals().put(INTEGER_FREE_TEXT, "115346");
		cm2Manager.addConsent(consent);

		logger.info("try to create consent templates with erronous free texts");
		ConsentTemplateKeyDTO erronousCTKey = new ConsentTemplateKeyDTO(DOMAIN, "erronous free text ct", "1");
		ConsentTemplateDTO erronousCT = new ConsentTemplateDTO(erronousCTKey);
		erronousCT.setType(ConsentTemplateType.CONSENT);
		FreeTextDefDTO erronousFreeText = new FreeTextDefDTO("erronous free text", false, FreeTextType.Date, "d.MGTRH.yyyy", 1, "comment");
		erronousCT.getFreeTextDefs().add(erronousFreeText);
		try {
			cm2Manager.addConsentTemplate(erronousCT);
			Assert.fail("could create a free text with an invalid converter string");
		} catch (FreeTextConverterStringException expected) {
			logger.info("expected FreeTextConverterStringException: " + expected);
		}
		erronousFreeText = new FreeTextDefDTO("erronous free text", false, FreeTextType.Date, null, 1, "comment");
		erronousCT.getFreeTextDefs().clear();
		erronousCT.getFreeTextDefs().add(erronousFreeText);
		try {
			cm2Manager.addConsentTemplate(erronousCT);
			Assert.fail("could create a free text with an null converter string and type FreeTextType.Date");
		} catch (FreeTextConverterStringException expected) {
			logger.info("expected FreeTextConverterStringException: " + expected);
		}
		logger.info("### free text test end");
	}

	@Test
	public void domainTests() throws Exception {
		logger.info("### domain test start");
		logger.info("try to update some domain fields");
		Date date = new Date();
		String newLabel = "label_" + date;
		String newLogo = "logo_" + date;
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;
		cm2Manager.updateDomain(DOMAIN, newLabel, newLogo, newExternProperties, newComment);
		DomainDTO domain = cm2Manager.getDomain(DOMAIN);
		Assert.assertEquals(newLabel, domain.getLabel());
		Assert.assertEquals(newLogo, domain.getLogo());
		Assert.assertEquals(newExternProperties, domain.getExternProperties());
		Assert.assertEquals(newComment, domain.getComment());
		logger.info("### domain test end");
	}

	@Test
	public void signerIdTests() throws Exception {
		logger.info("### signer id test start");
		logger.info("try to add and remove a signer id type");
		cm2Manager.addSignerIdType(DOMAIN, "dummySignerIdType");
		cm2Manager.deleteSignerIdType(DOMAIN, "dummySignerIdType");
		logger.info("### signer id test end");
	}

	@Test
	public void deletionTests() throws Exception {
		logger.info("### deletion tests start");
		DomainDTO domainDTO = new DomainDTO(DELETION_DOMAIN, DELETION_TEST, SimpleVersionConverter.class.getName(),
				SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo",
				idTypes);
		ctForDeletion.getAssignedModules().add(amForDeletion);
		ctForDeletion.setType(ConsentTemplateType.CONSENT);
		logger.info("add domain");
		cm2Manager.addDomain(domainDTO);
		logger.info("add policy");
		cm2Manager.addPolicy(policyForDeletion);
		logger.info("add module");
		cm2Manager.addModule(moduleForDeletion);
		logger.info("add consent template");
		cm2Manager.addConsentTemplate(ctForDeletion);
		logger.info("try to delete a used module");
		try {
			cm2Manager.deleteModule(moduleKeyForDeletion);
			Assert.fail("could delete a used module");
		} catch (ObjectInUseException expected) {
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove consent template");
		cm2Manager.deleteConsentTemplate(ctKeyForDeletion);
		logger.info("try to get the deleted consent template");
		try {
			cm2Manager.getConsentTemplate(ctKeyForDeletion);
			Assert.fail("deleted consent template still found");
		} catch (UnknownConsentTemplateException expected) {
			logger.info("expected UnknownConsentTemplateException: " + expected);
		}
		logger.info("try to delete a used policy");
		try {
			cm2Manager.deletePolicy(policyKeyForDeletion);
			Assert.fail("could delete a used policy");
		} catch (ObjectInUseException expected) {
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove module");
		cm2Manager.deleteModule(moduleKeyForDeletion);
		logger.info("try to get the deleted module");
		try {
			cm2Manager.getModule(moduleKeyForDeletion);
			Assert.fail("deleted module still found");
		} catch (UnknownModuleException expected) {
			logger.info("expected UnknownModuleException: " + expected);
		}
		logger.info("try to delete a used domain");
		try {
			cm2Manager.deleteDomain(DELETION_DOMAIN);
			Assert.fail("could delete a used domain");
		} catch (ObjectInUseException expected) {
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove policy");
		cm2Manager.deletePolicy(policyKeyForDeletion);
		logger.info("try to get the deleted policy");
		try {
			cm2Manager.getPolicy(policyKeyForDeletion);
			Assert.fail("deleted policy still found");
		} catch (UnknownPolicyException expected) {
			logger.info("expected UnknownPolicyException: " + expected);
		}
		logger.info("remove domain");
		cm2Manager.deleteDomain(DELETION_DOMAIN);
		logger.info("try to get the deleted domain");
		try {
			cm2Manager.getDomain(DELETION_DOMAIN);
			Assert.fail("deleted domain still found");
		} catch (UnknownDomainException expected) {
			logger.info("expected UnknownDomainException: " + expected);
		}
		logger.info("### deletion tests end");
	}
}
