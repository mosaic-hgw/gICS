package org.emau.icmvc.ganimed.ttp.cm2.test;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig.IdMatchingType;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextValDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorMaintenanceVersionConverter;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter;
import org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CM2ManagerTests
{
	/*-
	loeschskript:
	delete from qc_hist where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from qc where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from signed_policy where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from signature where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from free_text_val where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from consent_scan where CT_DOMAIN_NAME like 'test12345_cm_test' or CT_DOMAIN_NAME like 'test12345_cm_version_test' or CT_DOMAIN_NAME like 'test12345_cm_deletion_test' or CT_DOMAIN_NAME like 'test12345_scan_cm_test' or CT_DOMAIN_NAME like 'oidTestDomain';
	delete from consent where CT_DOMAIN_NAME = 'test12345_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from module_consent_template where CT_DOMAIN = 'test12345_cm_test' or CT_DOMAIN = 'test12345_cm_version_test' or CT_DOMAIN = 'test12345_cm_deletion_test' or CT_DOMAIN = 'test12345_scan_cm_test' or CT_DOMAIN = 'oidTestDomain';
	delete from module_policy where M_DOMAIN_NAME = 'test12345_cm_test' or M_DOMAIN_NAME = 'test12345_cm_version_test' or M_DOMAIN_NAME = 'test12345_cm_deletion_test' or M_DOMAIN_NAME = 'test12345_scan_cm_test' or M_DOMAIN_NAME = 'oidTestDomain';
	delete from free_text_def where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from consent_template where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from consent_template_scan where ID like 'test12345_cm_test' or ID like 'test12345_cm_version_test' or ID like 'test12345_cm_deletion_test' or ID like 'test12345_scan_cm_test' or ID like 'oidTestDomain';
	delete from module where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain' or DOMAIN_NAME = 'FhirIDTest';
	delete from policy where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from text where ID like 'test12345_cm_test' or ID like 'test12345_cm_version_test' or ID like 'test12345_cm_deletion_test' or ID like 'test12345_scan_cm_test' or ID like 'oidTestDomain' or ID like 'FhirIDTest';
	delete from virtual_person_signer_id where SIT_DOMAIN_NAME = 'test12345_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test' or SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'oidTestDomain';
	delete from alias where ORIG_SIT_DOMAIN_NAME = 'test12345_cm_test' or ALIAS_SIT_DOMAIN_NAME = 'test12345_cm_test';
	delete from signer_id where SIT_DOMAIN_NAME = 'test12345_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test' or SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'oidTestDomain';
	delete from signer_id_type where DOMAIN_NAME = 'test12345_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'crash-test-dummy' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain' or DOMAIN_NAME ='dummy_domain_for_update_test' or DOMAIN_NAME = 'FhirIDTest';
	delete from domain where NAME = 'test12345_cm_test' or NAME = 'test12345_cm_version_test' or NAME = 'test12345_cm_deletion_test' or NAME = 'crash-test-dummy' or NAME = 'test12345_scan_cm_test' or NAME = 'oidTestDomain' or NAME ='dummy_domain_for_update_test' or NAME = 'FhirIDTest';
	 */
	private static final String ANOTHER_ID_TYPE = "another_id_type";
	private static final String MPI_ID_TYPE = "mpi_id_type";
	private static final String NO_TITLE = "no_title";
	private static final String NO_COMMENT = "no_comment";
	private static final String NO_EXTERN_PROPERTIES = "no_extern_properties";
	private static final String DOMAIN = "test12345_cm_test";
	// private static final String DOMAIN_OID_TEST = "oidTestDomain";
	private static final String VERSION_DOMAIN = "test12345_cm_version_test";
	private static final String DELETION_DOMAIN = "test12345_cm_deletion_test";
	private static final String CM2_URL = "http://localhost:8080/gics/gicsService?wsdl";
	private static GICSService cm2Manager;
	private static final Logger logger = LogManager.getLogger(CM2ManagerTests.class);
	private static final String DELETION_TEST = "deletion_test";
	private static final String domainNameForUpdateTest = "dummy_domain_for_update_test";
	private final static ExpirationPropertiesDTO EXPIRATION_PROPERTIES = new ExpirationPropertiesObject( /* tomorrow normalized without time part */
			new Date(new Date().getTime() + 1000 * 24 * 60 * 60), Period.of(1, 2, 3)).normalized().toDTO();
	private static final String signer_id = "12345_test";
	private final static PolicyKeyDTO policyKey11 = new PolicyKeyDTO(DOMAIN, "test1", "1");
	private final static PolicyDTO policy11 = new PolicyDTO(policyKey11);
	private final static PolicyKeyDTO policyKey21 = new PolicyKeyDTO(DOMAIN, "test2", "1");
	private final static PolicyDTO policy21 = new PolicyDTO(policyKey21);
	private final static PolicyKeyDTO policyKey22 = new PolicyKeyDTO(DOMAIN, "test2", "2");
	private final static PolicyDTO policy22 = new PolicyDTO(policyKey22);
	private final static PolicyKeyDTO policyKey31 = new PolicyKeyDTO(DOMAIN, "test3", "1");
	private final static PolicyDTO policy31 = new PolicyDTO(policyKey31);
	private final static PolicyKeyDTO nonExistingPolicyKey = new PolicyKeyDTO(DOMAIN, "notExistingPolicy", "1");
	private final static PolicyKeyDTO policyKeyForDeletion = new PolicyKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static PolicyDTO policyForDeletion = new PolicyDTO(policyKeyForDeletion);
	private final static ModuleKeyDTO moduleKey11 = new ModuleKeyDTO(DOMAIN, "test11", "1");
	private final static ModuleDTO module11 = new ModuleDTO(moduleKey11, "kein sinnvoller modultext 11", NO_TITLE, NO_COMMENT, NO_EXTERN_PROPERTIES,
			Set.of(new AssignedPolicyDTO(policy11)), "no label", "no short text", false, null, null, null);
	private final static ModuleKeyDTO moduleKey1121 = new ModuleKeyDTO(DOMAIN, "test1121", "1");
	private final static ModuleDTO module1121 = new ModuleDTO(moduleKey1121, "kein sinnvoller modultext 1121", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy21)), "no label", "no short text", false, null, null, null);
	private final static ModuleKeyDTO moduleKey1122 = new ModuleKeyDTO(DOMAIN, "test1122", "1");
	private final static ModuleDTO module1122 = new ModuleDTO(moduleKey1122, "kein sinnvoller modultext 1122", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy21)), "no label", "no short text", false, null, null, null);
	private final static ModuleKeyDTO moduleKey1131 = new ModuleKeyDTO(DOMAIN, "test1131", "1");
	private final static ModuleDTO module1131 = new ModuleDTO(moduleKey1131, "kein sinnvoller modultext 1131", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy31)), "no label", "no short text", false, null, null, null);
	private final static ModuleKeyDTO moduleKey2231 = new ModuleKeyDTO(DOMAIN, "test2231", "2");
	private final static ModuleDTO module2231 = new ModuleDTO(moduleKey2231, "kein sinnvoller modultext 2231", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy22), new AssignedPolicyDTO(policy31)), "no label", "no short text", false, null, null, null);
	private final static ModuleKeyDTO moduleKeyForDeletion = new ModuleKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static ModuleDTO moduleForDeletion = new ModuleDTO(moduleKeyForDeletion, "kein sinnvoller modultext", NO_TITLE, NO_COMMENT,
			NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policyForDeletion)), "no label", "no short text", false, null, null, null);
	private final static ConsentTemplateKeyDTO ctKey11 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "1");
	private final static ConsentTemplateKeyDTO ctKey12 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "2");
	private final static ConsentTemplateKeyDTO ctKey13 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "3");
	private final static ConsentTemplateKeyDTO ctKeyForDeletion = new ConsentTemplateKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	private final static ConsentTemplateDTO ct11 = new ConsentTemplateDTO(ctKey11);
	private final static ConsentTemplateKeyDTO ctKey21 = new ConsentTemplateKeyDTO(DOMAIN, "testCT2", "1");
	private final static ConsentTemplateDTO ctForDeletion = new ConsentTemplateDTO(ctKeyForDeletion);
	private final static List<ConsentStatus> allConsentStatus = Arrays.asList(ConsentStatus.values());
	private final static AssignedModuleDTO am11 = new AssignedModuleDTO(module11, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	private final static AssignedModuleDTO am1121 = new AssignedModuleDTO(module1121, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	private final static AssignedModuleDTO am1122 = new AssignedModuleDTO(module1122, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	private final static AssignedModuleDTO am2231 = new AssignedModuleDTO(module2231, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, moduleKey11,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	private final static AssignedModuleDTO amForDeletion = new AssignedModuleDTO(moduleForDeletion, true, ConsentStatus.UNKNOWN, allConsentStatus, 0,
			null, "no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	private final static List<String> idTypes = Arrays.asList(MPI_ID_TYPE, ANOTHER_ID_TYPE);

	@BeforeAll
	public static void storeBasicEntries() throws Exception
	{
		logger.info("setup");
		try
		{
			QName serviceName = new QName("http://cm2.ttp.ganimed.icmvc.emau.org/", "GICSServiceImplService");
			Service service = Service.create(new URL(CM2_URL), serviceName);
			Assertions.assertNotNull(service, "webservice object for CM2Manager is null");
			cm2Manager = service.getPort(GICSService.class);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		Assertions.assertNotNull(cm2Manager, "cm2 manager object is null");

		try
		{
			DomainDTO d = cm2Manager.getDomain(DOMAIN);
			logger.warn("reusing cached domain: " + DOMAIN + " !!!");
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainDTO domainDTO = new DomainDTO(DOMAIN, "dummy", SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
						SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo", idTypes, false, null, null, EXPIRATION_PROPERTIES, null);
				logger.info("creating test domain: " + domainDTO);
				cm2Manager.addDomain(domainDTO);
				try
				{
					cm2Manager.addPolicy(policy11);
					Assertions.fail("policy could be added to a not finalised domain");
				}
				catch (RequirementsNotFullfilledException expected)
				{
					logger.info("expected RequirementsNotFullfilledException: " + expected);
					cm2Manager.finaliseDomain(DOMAIN);
				}
			}
			catch (DuplicateEntryException ignore)
			{
				// geht nicht
			}
		}
		Assertions.assertEquals(EXPIRATION_PROPERTIES, cm2Manager.getDomain(DOMAIN).getExpirationProperties());

		try
		{
			cm2Manager.getDomain(VERSION_DOMAIN);
			logger.warn("reusing cached domain: " + VERSION_DOMAIN + " !!!");
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainDTO domainDTO = new DomainDTO(VERSION_DOMAIN, "dummy", MajorMinorVersionConverter.class.getName(),
						MajorMinorVersionConverter.class.getName(), MajorMinorMaintenanceVersionConverter.class.getName(), "", "version-test-domain",
						"no extern properties", "logo", idTypes, true, null, null, EXPIRATION_PROPERTIES, null);
				logger.info("creating version test domain: " + domainDTO);
				cm2Manager.addDomain(domainDTO);
			}
			catch (DuplicateEntryException ignore)
			{
				// geht nicht
			}
		}
		Assertions.assertEquals(EXPIRATION_PROPERTIES, cm2Manager.getDomain(VERSION_DOMAIN).getExpirationProperties());

		logger.info("create policies");
		cm2Manager.addPolicy(policy11);
		cm2Manager.addPolicy(policy21);
		cm2Manager.addPolicy(policy22);
		cm2Manager.finalisePolicy(policyKey22);
		policy31.setFinalised(true);
		cm2Manager.addPolicy(policy31);

		logger.info("create modules");
		cm2Manager.addModule(module11, true);
		try
		{
			cm2Manager.finaliseModule(moduleKey11, false);
			Assertions.fail("module could be finalised with a not finalised policy");
		}
		catch (RequirementsNotFullfilledException expected)
		{
			logger.info("expected RequirementsNotFullfilledException: " + expected);
			cm2Manager.finalisePolicy(policyKey11);
		}
		cm2Manager.finaliseModule(moduleKey11, true);
		cm2Manager.addModule(module1121, false);
		try
		{
			cm2Manager.finaliseModule(moduleKey1121, false);
			Assertions.fail("module could be finalised with a not finalised policy"); // policy21
		}
		catch (RequirementsNotFullfilledException expected)
		{
			logger.info("expected RequirementsNotFullfilledException: " + expected);
			cm2Manager.finaliseModule(moduleKey1121, true);
		}
		cm2Manager.addModule(module1122, true);
		cm2Manager.finaliseModule(moduleKey1122, false);
		module1131.setFinalised(true);
		cm2Manager.addModule(module1131, false);
		module2231.setFinalised(true);
		cm2Manager.addModule(module2231, true);

		logger.info("create consent templates");
		ConsentTemplateDTO ct12 = new ConsentTemplateDTO(ctKey12);
		ConsentTemplateDTO ct13 = new ConsentTemplateDTO(ctKey13);
		ConsentTemplateDTO ct21 = new ConsentTemplateDTO(ctKey21);
		ct11.getAssignedModules().add(am1121);
		ct12.getAssignedModules().add(am1122);
		ct13.getAssignedModules().add(am1121);
		ct21.getAssignedModules().add(am11);
		ct21.getAssignedModules().add(am2231);
		ct11.setType(ConsentTemplateType.CONSENT);
		ct12.setType(ConsentTemplateType.CONSENT);
		ct13.setType(ConsentTemplateType.CONSENT);
		ct21.setType(ConsentTemplateType.CONSENT);
		cm2Manager.addConsentTemplate(ct11, false);
		cm2Manager.finaliseTemplate(ctKey11, true);
		cm2Manager.addConsentTemplate(ct12, false);
		cm2Manager.finaliseTemplate(ctKey12, true);
		cm2Manager.addConsentTemplate(ct13, false);
		ct21.setFinalised(true);
		cm2Manager.addConsentTemplate(ct21, true);
		logger.info("setup complete");

		// createOIDsTestEnvironment();
	}

	@AfterAll
	public static void deleteBasicEntries() throws Exception
	{
		logger.info("teardown");
		if (cm2Manager != null)
		{
			logger.info("delete the consent templates");
			cm2Manager.deleteConsentTemplate(ctKey11);
			cm2Manager.deleteConsentTemplate(ctKey12);
			cm2Manager.deleteConsentTemplate(ctKey13);
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
		}
		logger.info("finished");
	}

	@Test
	public void basicTest() throws Exception
	{
		logger.info("### basic test start");
		logger.info("try to create a duplicate policy");
		try
		{
			cm2Manager.addPolicy(policy11);
			Assertions.fail("could create a duplicate policy");
		}
		catch (DuplicateEntryException expected)
		{
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a duplicate consent template");
		try
		{
			cm2Manager.addConsentTemplate(ct11, false);
			Assertions.fail("could create a duplicate consent template");
		}
		catch (DuplicateEntryException expected)
		{
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to delete a used policy");
		try
		{
			cm2Manager.deletePolicy(policyKey11);
			Assertions.fail("could delete a policy which is in use");
		}
		catch (ObjectInUseException expected)
		{
			logger.info("expected ObjectInUseException: " + expected);
		}

		ConsentTemplateKeyDTO tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		ConsentTemplateDTO tempCT = new ConsentTemplateDTO(tempCTKey);
		logger.info("try to create a consent template with duplicate policies in different versions");
		tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am1121);
		tempCT.getAssignedModules().add(am2231);
		try
		{
			cm2Manager.addConsentTemplate(tempCT, false);
			Assertions.fail("could create a consent template with duplicate policies in different versions");
		}
		catch (DuplicateEntryException expected)
		{
			logger.info("expected DuplicateEntryException: " + expected);
		}

		logger.info("try to create a consent template with a module with a parent which is not part of that consent template");
		tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "temtCT", "1");
		tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		tempCT.getAssignedModules().add(am2231);
		try
		{
			cm2Manager.addConsentTemplate(tempCT, false);
			Assertions.fail("could create a consent template with a module with a parent which is not part of that consent template");
		}
		catch (UnknownModuleException expected)
		{
			logger.info("expected UnknownModuleException: " + expected);
		}

		logger.info("try to create a module with an unknown policy");
		PolicyKeyDTO policyKeyUnknown = new PolicyKeyDTO(DOMAIN, "testUnknown", "1");
		PolicyDTO policyUnknown = new PolicyDTO(policyKeyUnknown);
		ModuleKeyDTO moduleKeyForUP = new ModuleKeyDTO(DOMAIN, "test UP", "2");
		ModuleDTO moduleForUP = new ModuleDTO(moduleKeyForUP, "kein sinnvoller modultext UP", NO_TITLE, NO_COMMENT, NO_EXTERN_PROPERTIES,
				Set.of(new AssignedPolicyDTO(policyUnknown)), "no label", "no short text", false, null, null, null);
		try
		{
			cm2Manager.addModule(moduleForUP, false);
			Assertions.fail("could create a module with an unknown policy");
		}
		catch (UnknownPolicyException expected)
		{
			logger.info("expected UnknownPolicyException: " + expected);
		}

		logger.info("try to create a consent template with an unknown modul");
		ModuleKeyDTO moduleKeyUnknown = new ModuleKeyDTO(DOMAIN, "testUnknown", "1");
		ModuleDTO moduleUnknown = new ModuleDTO(moduleKeyUnknown, "", "", "", "", new HashSet<>(), "no label", "no short text", false, null, null, null);
		AssignedModuleDTO amUnknown = new AssignedModuleDTO(moduleUnknown, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null, "", "", EXPIRATION_PROPERTIES, null);
		tempCT.getAssignedModules().clear();
		tempCT.getAssignedModules().add(amUnknown);
		try
		{
			cm2Manager.addConsentTemplate(tempCT, false);
			Assertions.fail("could create a consent template with an unknown module");
		}
		catch (UnknownModuleException expected)
		{
			logger.info("expected UnknownModuleException: " + expected);
		}

		logger.info("try to update some consent template fields");
		Date date = new Date();
		String newLabel = "label_" + date;
		String newVersionLabel = "versionLabel_" + date;
		String newShortText = "shortText_" + date;
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;
		String text = "nur+ein+testtext"; // only base64 allowed
		String pdfType = "pdf";
		ConsentTemplateDTO updateCTDTO = new ConsentTemplateDTO(ctKey11);
		updateCTDTO.setLabel(newLabel);
		updateCTDTO.setVersionLabel(newVersionLabel);
		updateCTDTO.setComment(newComment);
		updateCTDTO.setExternProperties(newExternProperties);
		updateCTDTO.setScanBase64(text);
		updateCTDTO.setScanFileType(pdfType);
		cm2Manager.updateConsentTemplateInUse(updateCTDTO);
		tempCT = cm2Manager.getConsentTemplate(ctKey11);
		Assertions.assertEquals(newLabel, tempCT.getLabel());
		Assertions.assertEquals(newVersionLabel, tempCT.getVersionLabel());
		Assertions.assertEquals(newExternProperties, tempCT.getExternProperties());
		Assertions.assertEquals(newComment, tempCT.getComment());
		Assertions.assertEquals(text, tempCT.getScanBase64());
		Assertions.assertEquals(pdfType, tempCT.getScanFileType());

		logger.info("try to update some policy fields");
		cm2Manager.updatePolicyInUse(policyKey11, newLabel, newExternProperties, newComment);
		PolicyDTO tempPolicy = cm2Manager.getPolicy(policyKey11);
		Assertions.assertEquals(newExternProperties, tempPolicy.getExternProperties());
		Assertions.assertEquals(newComment, tempPolicy.getComment());
		Assertions.assertEquals(newLabel, tempPolicy.getLabel());

		logger.info("try to update some module fields");
		cm2Manager.updateModuleInUse(moduleKey11, newLabel, newShortText, newExternProperties, newComment, null);
		ModuleDTO tempModule = cm2Manager.getModule(moduleKey11);
		Assertions.assertEquals(newExternProperties, tempModule.getExternProperties());
		Assertions.assertEquals(newComment, tempModule.getComment());
		Assertions.assertEquals(newLabel, tempModule.getLabel());
		Assertions.assertEquals(newShortText, tempModule.getShortText());
		logger.info("### basic test end");
	}

	@Test
	public void listTest() throws UnknownDomainException, InvalidVersionException, VersionConverterClassException, UnknownConsentTemplateException, InconsistentStatusException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		logger.info("### list test start");
		logger.info("list all consent templates");
		List<ConsentTemplateDTO> all = cm2Manager.listConsentTemplates(DOMAIN, true);
		// je nachdem, welche tests schon ausgefuehrt wurden
		Assertions.assertTrue(all.size() > 2 || all.size() < 7, "wrong number of consent templates: " + all.size());
		for (ConsentTemplateDTO dto : all)
		{
			logger.info(dto);
		}
		logger.info("get the newest version of consent template 'testCT1'");
		ConsentTemplateDTO newest = cm2Manager.getCurrentConsentTemplate("testCT1", DOMAIN);
		Assertions.assertEquals("2", newest.getKey().getVersion(), "wrong version number for newest consent template");
		logger.info(newest);
		logger.info("get list with the newest version of each consent template");
		List<ConsentTemplateDTO> currentCTs = cm2Manager.listCurrentConsentTemplates(DOMAIN);
		for (ConsentTemplateDTO ct : currentCTs)
		{
			logger.info(ct);
		}
		logger.info("list all consents without scans");
		List<ConsentLightDTO> consents = cm2Manager.getAllConsentsForDomainWithoutScan(DOMAIN);
		for (ConsentLightDTO consentLight : consents)
		{
			ConsentDTO consent = cm2Manager.getConsent(consentLight.getKey());
			// kein trim, die abfrage ueber criteriabuilder gibt das nicht her
			Assertions.assertTrue(consent.getScans().isEmpty(), "getAllConsentsForDomainWithoutScan got a consent WITH scan: " + consent.getKey());
		}
		logger.info("found " + consents.size() + " consents without scan");
		Map<ConsentField, String> filterMap = new HashMap<>();
		filterMap.put(ConsentField.CT_NAME, "onsentTe");
		PaginationConfig pConfig = new PaginationConfig();
		long countWithoutFilter = cm2Manager.countConsentsForDomainWithFilter(DOMAIN, pConfig);
		logger.info("count without filter: " + countWithoutFilter);
		pConfig.setFilter(filterMap);
		long countWithFilter = cm2Manager.countConsentsForDomainWithFilter(DOMAIN, pConfig);
		logger.info("count with filter: " + countWithFilter);
		Assertions.assertTrue(countWithFilter > 0, "count with filter is 0");
		Assertions.assertTrue(countWithoutFilter > countWithFilter, "count without filter not > count without filter");
		pConfig.setFilter(filterMap);
		pConfig.setFilterFieldsAreTreatedAsConjunction(false);
		long countWithOrFilter = cm2Manager.countConsentsForDomainWithFilter(DOMAIN, pConfig);
		logger.info("count with or-filter: " + countWithOrFilter);
		// spaeter mal, wenn es unterscheidbare consente gibt
		// Assert.assertTrue("count with or-filter is not > count with filter", countWithOrFilter >
		// countWithFilter);
		Assertions.assertTrue(countWithoutFilter > countWithOrFilter, "count with or-filter not < count without filter");
		logger.info("list consents paginated (2 consents per page)");
		int i = 0;
		pConfig = new PaginationConfig(0, 2, ConsentField.DATE, true);
		do
		{
			pConfig.setFirstEntry(2 * i++);
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
			for (ConsentLightDTO dto : consents)
			{
				logger.info("page " + i + ": " + dto);
			}
		}
		while (!consents.isEmpty());
		logger.info("list consents paginated (3 consents per page, filtered by consent template name like 'onsentTe')");
		i = 0;
		pConfig = new PaginationConfig(0, 3, ConsentField.CT_NAME, true);
		pConfig.setFilter(filterMap);
		pConfig.setFilterFieldsAreTreatedAsConjunction(false);
		do
		{
			pConfig.setFirstEntry(3 * i++);
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
			for (ConsentLightDTO dto : consents)
			{
				logger.info("page " + i + ": " + dto);
			}
		}
		while (!consents.isEmpty());
		logger.info("list consents paginated (3 consents per page, filtered by consent template name like 'onsentTe' and a valid signer_id)");
		filterMap.put(ConsentField.SIGNER_ID, signer_id);
		pConfig.setFilter(filterMap);
		i = 0;
		int validConsentCount = 0;
		do
		{
			pConfig.setFirstEntry(3 * i++);
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
			for (ConsentLightDTO dto : consents)
			{
				logger.info("page " + i + ": " + dto);
			}
			validConsentCount += consents.size();
		}
		while (!consents.isEmpty());
		logger.info("test consents paginated (filtered by consent template name like 'onsentTe' and an invalid signer_id - or-linked)");
		filterMap.put(ConsentField.SIGNER_ID, "invalid_signer_id");
		pConfig.setFilter(filterMap);
		i = 0;
		int invalidOrConsentCount = 0;
		ConsentKeyDTO aConsentKeyDTO = null;
		do
		{
			pConfig.setFirstEntry(3 * i++);
			consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
			if (!consents.isEmpty() && aConsentKeyDTO == null)
			{
				aConsentKeyDTO = consents.get(0).getKey();
			}
			invalidOrConsentCount += consents.size();
		}
		while (!consents.isEmpty());
		Assertions.assertTrue(validConsentCount == invalidOrConsentCount, "validConsentCount != invalidOrConsentCount");
		logger.info("test consents paginated (filtered by consent template name like 'onsentTe' and an invalid signer_id - and-linked)");
		pConfig.setFilterFieldsAreTreatedAsConjunction(true);
		pConfig.setFirstEntry(0);
		consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
		Assertions.assertTrue(consents.isEmpty(), "invalidConsentCount > 0");
		Assertions.assertTrue(validConsentCount == invalidOrConsentCount, "validConsentCount != invalidOrConsentCount");
		logger.info("test consents paginated (filtered by an invalid signer_id - and-linked)");
		filterMap.remove(ConsentField.CT_NAME);
		pConfig.setFilter(filterMap);
		consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
		Assertions.assertTrue(consents.isEmpty(), "invalidConsentCount > 0");
		Assertions.assertTrue(validConsentCount == invalidOrConsentCount, "validConsentCount != invalidOrConsentCount");
		logger.info("test consents paginated (filtered by an invalid signer_id - or-linked)");
		pConfig.setFilterFieldsAreTreatedAsConjunction(true);
		consents = cm2Manager.getConsentsForDomainPaginated(DOMAIN, pConfig);
		Assertions.assertTrue(consents.isEmpty(), "invalidConsentCount > 0");
		logger.info("### test list qc history");
		List<QCHistoryDTO> qcHist = cm2Manager.getQCHistoryForConsent(aConsentKeyDTO);
		Assertions.assertTrue(!qcHist.isEmpty(), "no qc history found");
		logger.info("### list test end");
	}

	@Test
	public void consentTest() throws Exception
	{
		logger.info("### consent test start");
		final String policyNameMandatory = "policyForConsentTestMandatory";
		final String policyNameMandatory2 = "policyForConsentTestMandatory2";
		final String policyNameMandatory3 = "policyForConsentTestMandatory3";
		final String policyNameOptional = "policyForConsentTestOptional";
		final List<PolicyKeyDTO> emptyPolicyKeyList = new ArrayList<>();
		final PolicyKeyDTO policyKeyForConsentTestMandatory = new PolicyKeyDTO(DOMAIN, policyNameMandatory, "1");
		final PolicyDTO policyForConsentTestMandatory = new PolicyDTO(policyKeyForConsentTestMandatory, "no comment", "no extern properties",
				"no label", false, null, null, null);
		final PolicyKeyDTO policyKeyForConsentTestMandatory2 = new PolicyKeyDTO(DOMAIN, policyNameMandatory2, "1");
		final PolicyDTO policyForConsentTestMandatory2 = new PolicyDTO(policyKeyForConsentTestMandatory2);
		final PolicyKeyDTO policyKeyForConsentTestMandatory3 = new PolicyKeyDTO(DOMAIN, policyNameMandatory3, "1");
		final PolicyDTO policyForConsentTestMandatory3 = new PolicyDTO(policyKeyForConsentTestMandatory3);
		final PolicyKeyDTO policyKeyForConsentTestOptional = new PolicyKeyDTO(DOMAIN, policyNameOptional, "1");
		final PolicyDTO policyForConsentTestOptional = new PolicyDTO(policyKeyForConsentTestOptional);
		try
		{
			cm2Manager.addPolicy(policyForConsentTestMandatory);
			cm2Manager.addPolicy(policyForConsentTestMandatory2);
			cm2Manager.addPolicy(policyForConsentTestMandatory3);
			cm2Manager.addPolicy(policyForConsentTestOptional);
		}
		catch (DuplicateEntryException ignore)
		{
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}
		ModuleKeyDTO moduleKeyForConsentTestMandatory = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestMandatory", "1");
		ModuleDTO moduleForConsentTestMandatory = new ModuleDTO(moduleKeyForConsentTestMandatory, "dummy text mandatory", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policyForConsentTestMandatory)), "no label", "no short text", false, null, null, null);
		ModuleKeyDTO moduleKeyForConsentTestMandatory2 = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestMandatory2", "1");
		ModuleDTO moduleForConsentTestMandatory2 = new ModuleDTO(moduleKeyForConsentTestMandatory2, "dummy text mandatory 2", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policyForConsentTestMandatory2), new AssignedPolicyDTO(policyForConsentTestMandatory3)), "no label", "no short text", false, null,
				null, null);
		ModuleKeyDTO moduleKeyForConsentTestOptional = new ModuleKeyDTO(DOMAIN, "moduleForConsentTestOptional", "1");
		ModuleDTO moduleForConsentTestOptional = new ModuleDTO(moduleKeyForConsentTestOptional, "dummy text optional", NO_TITLE, NO_COMMENT,
				NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policyForConsentTestOptional)), "no label", "no short text", false, null, null, null);
		try
		{
			cm2Manager.addModule(moduleForConsentTestMandatory, false);
			cm2Manager.addModule(moduleForConsentTestMandatory2, false);
			cm2Manager.addModule(moduleForConsentTestOptional, false);
		}
		catch (DuplicateEntryException ignore)
		{
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}
		AssignedModuleDTO amMandatory = new AssignedModuleDTO(moduleForConsentTestMandatory, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
				"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
		AssignedModuleDTO amMandatory2 = new AssignedModuleDTO(moduleForConsentTestMandatory2, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
				"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
		AssignedModuleDTO amOptional = new AssignedModuleDTO(moduleForConsentTestOptional, false, ConsentStatus.UNKNOWN, allConsentStatus, 0,
				moduleKeyForConsentTestMandatory, "no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
		ConsentTemplateKeyDTO ctKeyConsentTest = new ConsentTemplateKeyDTO(DOMAIN, "ConsentTest", "1");
		ConsentTemplateDTO ctConsentTest = new ConsentTemplateDTO(ctKeyConsentTest);
		ctConsentTest.setType(ConsentTemplateType.CONSENT);
		ctConsentTest.setComment("no comment");
		ctConsentTest.setExternProperties("no extern properties");
		ctConsentTest.getAssignedModules().add(amMandatory);
		ctConsentTest.getAssignedModules().add(amMandatory2);
		ctConsentTest.getAssignedModules().add(amOptional);
		ctConsentTest.setFinalised(true);
		try
		{
			cm2Manager.addConsentTemplate(ctConsentTest, true); // finalisiert alles
		}
		catch (DuplicateEntryException ignore)
		{
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}

		cm2Manager.getConsentTemplate(ctKeyConsentTest).getAssignedModules().stream().forEach(am -> Assertions.assertEquals(EXPIRATION_PROPERTIES, am.getExpirationProperties()));

		logger.info("try to add invalid consents");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, signer_id, null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKeyConsentTest, ids, new Date());
		long time = consentKey.getConsentDate().getTime();
		consentKey.getConsentDate().setTime(time - time % 1000); // db speichert keine millis
		ConsentDTO consent = new ConsentDTO(consentKey);
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with a missing consent status");
		}
		catch (MissingRequiredObjectException expected)
		{
			logger.info("expected MissingRequiredObjectException: " + expected);
		}
		ModuleKeyDTO invalidModuleKeyForConsent = new ModuleKeyDTO(DOMAIN, "gibt's nich", "1");
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(moduleKeyForConsentTestMandatory2,
				new ModuleStateDTO(moduleKeyForConsentTestMandatory2, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		consent.getModuleStates().put(moduleKeyForConsentTestOptional,
				new ModuleStateDTO(moduleKeyForConsentTestOptional, ConsentStatus.UNKNOWN, emptyPolicyKeyList));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent without the mandatory signatures and signature dates");
		}
		catch (MissingRequiredObjectException expected)
		{
			logger.info("expected MissingRequiredObjectException: " + expected);
		}
		consent.setPatientSignatureBase64("dummy");
		consent.setPatientSigningDate(new Date());
		consent.setPatientSigningPlace("patplace");
		consent.setPhysicianSignatureBase64("dummy");
		consent.setPhysicianSigningDate(new Date());
		consent.setPhysicianSigningPlace("phyplace");
		consent.setPhysicianId("123");
		// policyKeys muessen nicht gesetzt werden, da sie ausgewertet werden
		consent.getModuleStates().put(invalidModuleKeyForConsent,
				new ModuleStateDTO(invalidModuleKeyForConsent, ConsentStatus.ACCEPTED, emptyPolicyKeyList));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with an invalid policy");
		}
		catch (UnknownModuleException expected)
		{
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
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with a mandatory-fields-logic-error");
		}
		catch (MandatoryFieldsException expected)
		{
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
		Assertions.assertTrue(cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config),
				"isConsented() returned 'false' where it should return 'true'");
		Assertions.assertTrue(cm2Manager.isConsentedFromIncludingToIncluding(ids, DOMAIN, policyNameMandatory, "1", "1", config),
				"isConsented(from, to) returned 'false' where it should return 'true'");
		Assertions.assertFalse(cm2Manager.isConsentedFromExcludingToIncluding(ids, DOMAIN, policyNameMandatory, "1", "1", config),
				"isConsented(from, to) returned 'true' where it should return 'false'");
		Assertions.assertFalse(cm2Manager.isConsentedFromIncludingToExcluding(ids, DOMAIN, policyNameMandatory, "1", "1", config),
				"isConsented(from, to) returned 'true' where it should return 'false'");
		Assertions.assertFalse(cm2Manager.isConsentedFromExcludingToExcluding(ids, DOMAIN, policyNameMandatory, "1", "1", config),
				"isConsented(from, to) returned 'true' where it should return 'false'");
		config.setIdMatchingType(IdMatchingType.AT_LEAST_ALL);
		Assertions.assertTrue(cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config),
				"isConsented() returned 'false' where it should return 'true'");
		config.setIdMatchingType(IdMatchingType.EXACT);
		Assertions.assertTrue(cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config),
				"isConsented() returned 'false' where it should return 'true'");
		try
		{
			cm2Manager.isConsented(ids, nonExistingPolicyKey, config);
			Assertions.fail("could query with a non existing policy - version path");
		}
		catch (UnknownPolicyException expected)
		{
			logger.info("expected UnknownPolicyException: " + expected);
		}
		config.setIgnoreVersionNumber(true);
		policyKeyForConsentTestMandatory.setVersion("dummy");
		Assertions.assertTrue(cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config),
				"isConsented() returned 'false' where it should return 'true'");
		Assertions.assertTrue(cm2Manager.isConsentedFromIncludingToIncluding(ids, DOMAIN, policyNameMandatory, "x", "x", config),
				"isConsented(from, to) returned 'false' where it should return 'true'");
		try
		{
			cm2Manager.isConsented(ids, nonExistingPolicyKey, config);
			Assertions.fail("could query with a non existing policy - ignore version path");
		}
		catch (UnknownPolicyException expected)
		{
			logger.info("expected UnknownPolicyException: " + expected);
		}

		logger.info("### add scan to consent");
		String text = "nur+ein+test"; // only base64 allowed
		final String txtType = "txt";
		final String pdfType = "pdf";
		String fileName = "ein file";
		cm2Manager.addScanToConsent(consentKey, text, txtType, fileName);
		consent = cm2Manager.getConsent(consentKey);

		ConsentScanDTO scan = consent.getScans().get(0);

		Assertions.assertEquals(text, scan.getBase64());
		Assertions.assertEquals(txtType, scan.getFileType());
		Assertions.assertEquals(fileName, scan.getFileName());

		logger.info("try to update some consent fields");
		Date date = new Date();
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;
		text = "ein+neuer+testtext=="; // only base64 allowed
		fileName = "eine datei";
		scan.setBase64(text);
		scan.setFileType(pdfType);
		scan.setFileName(fileName);
		cm2Manager.updateConsentInUse(consentKey, newExternProperties, newComment, scan);
		consent = cm2Manager.getConsent(consentKey);
		Assertions.assertEquals(newExternProperties, consent.getExternProperties());
		Assertions.assertEquals(newComment, consent.getComment());
		Assertions.assertEquals(text, consent.getScans().get(0).getBase64());
		Assertions.assertEquals(pdfType, consent.getScans().get(0).getFileType());
		Assertions.assertEquals(fileName, consent.getScans().get(0).getFileName());
		Assertions.assertEquals("patplace", consent.getPatientSigningPlace());
		Assertions.assertEquals("phyplace", consent.getPhysicianSigningPlace());

		logger.info("test getAllConsentsForPerson");
		List<ConsentLightDTO> consentsForPerson = cm2Manager.getAllConsentsForSignerIds(DOMAIN, ids, false);
		logger.info("found " + consentsForPerson.size() + " consents");

		logger.info("try to invalidate consent");
		// TODO invalidate jetzt ueber qc
		// cm2Manager.invalidateConsent(consent.getKey(), ConsentStatus.REVOKED, "no comment",
		// null);
		// Assertions.assertFalse("isConsented() returned 'true' where it should return 'false'",
		// cm2Manager.isConsented(ids, policyKeyForConsentTestMandatory, config));
		logger.info("### consent test end");
	}

	@Test
	public void versionTest() throws Exception
	{
		logger.info("### version test start");
		try
		{
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", "abc.def.ghi", SimpleVersionConverter.class.getName(),
					SimpleVersionConverter.class.getName(), "", "crash-test-domain", "no extern properties", "", idTypes, false, null, null, EXPIRATION_PROPERTIES, null);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assertions.fail("could create a domain with an unknown ctVersionConverterClass");
		}
		catch (VersionConverterClassException expected)
		{
			logger.info("expected VersionConverterClassException: " + expected);
		}
		try
		{
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", SimpleVersionConverter.class.getName(), "abc.def.ghi",
					SimpleVersionConverter.class.getName(), "", "crash-test-domain", "no extern properties", "", idTypes, false, null, null, EXPIRATION_PROPERTIES, null);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assertions.fail("could create a domain with an unknown moduleVersionConverterClass");
		}
		catch (VersionConverterClassException expected)
		{
			logger.info("expected VersionConverterClassException: " + expected);
		}

		try
		{
			DomainDTO domainDTO = new DomainDTO("crash-test-dummy", "dummy", SimpleVersionConverter.class.getName(),
					SimpleVersionConverter.class.getName(), "abc.def.ghi", "", "crash-test-domain", "no extern properties", "", idTypes, false, null,
					null, EXPIRATION_PROPERTIES, null);
			logger.info("creating crash test domain: " + domainDTO);
			cm2Manager.addDomain(domainDTO);
			Assertions.fail("could create a domain with an unknown policyVersionConverterClass");
		}
		catch (VersionConverterClassException expected)
		{
			logger.info("expected VersionConverterClassException: " + expected);
		}

		logger.info("try to add some policies with invalid version strings");
		try
		{
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		try
		{
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "999.111");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		try
		{
			PolicyKeyDTO versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1111.1.2");
			PolicyDTO versionPolicy = new PolicyDTO(versionPolicyKey);
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string");
		}
		catch (InvalidVersionException expected)
		{
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
		try
		{
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string (null)");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try
		{
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string (empty string)");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1.2.3.4");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try
		{
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string (4 parts)");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		versionPolicyKey = new PolicyKeyDTO(VERSION_DOMAIN, "test", "1..2");
		versionPolicy = new PolicyDTO(versionPolicyKey);
		try
		{
			cm2Manager.addPolicy(versionPolicy);
			Assertions.fail("could create a policy with an invalid version string (two dots)");
		}
		catch (InvalidVersionException expected)
		{
			logger.info("expected InvalidVersionException: " + expected);
		}
		logger.info("### version test end");
	}

	@Test
	public void freeTextTest() throws Exception
	{
		logger.info("### free text test start");
		String REQUIRED_FREE_TEXT = "free text string";
		String DATE_FREE_TEXT = "free text date";
		String INTEGER_FREE_TEXT = "free text integer";
		String DOUBLE_FREE_TEXT = "free text double";

		logger.info("create a consent template with free texts");
		ConsentTemplateKeyDTO tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "freeTextCT" + System.currentTimeMillis(), "1");
		ConsentTemplateDTO tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		FreeTextDefDTO freeText1 = new FreeTextDefDTO(REQUIRED_FREE_TEXT, true, FreeTextType.STRING, "", 1, "comment", false, null, null, null, null, null);
		tempCT.getFreeTextDefs().add(freeText1);
		FreeTextDefDTO freeText2 = new FreeTextDefDTO(DATE_FREE_TEXT, false, FreeTextType.DATE, "dd.MM.yyyy", 2, "comment", false, null, null, null, null, null);
		tempCT.getFreeTextDefs().add(freeText2);
		FreeTextDefDTO freeText3 = new FreeTextDefDTO(INTEGER_FREE_TEXT, false, FreeTextType.INTEGER, "", 3, "comment", false, null, null, null, null, null);
		tempCT.getFreeTextDefs().add(freeText3);
		FreeTextDefDTO freeText4 = new FreeTextDefDTO(DOUBLE_FREE_TEXT, false, FreeTextType.DOUBLE, "", 4, "comment", false, null, null, null, null, null);
		tempCT.getFreeTextDefs().add(freeText4);
		try
		{
			cm2Manager.addConsentTemplate(tempCT, false);
		}
		catch (DuplicateEntryException ignore)
		{
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}

		logger.info("try to create a consent without the mandatory free text");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "xyz", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(tempCTKey, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent for a not finalised template");
		}
		catch (RequirementsNotFullfilledException expected)
		{
			logger.info("expected RequirementsNotFullfilledException: " + expected);
			cm2Manager.finaliseTemplate(tempCTKey, true);
			try
			{
				cm2Manager.addConsent(consent);
				Assertions.fail("could create a consent without the mandatory free text");
			}
			catch (MissingRequiredObjectException expected2)
			{
				logger.info("expected MissingRequiredObjectException: " + expected2);
			}
		}

		logger.info("try to create a consent with invalid free texts");
		consent.getFreeTextVals().add(new FreeTextValDTO(DATE_FREE_TEXT, "11.13", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(REQUIRED_FREE_TEXT, "test", "no fhir-id"));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with invalid free texts");
		}
		catch (InvalidFreeTextException expected)
		{
			logger.info("expected InvalidFreeTextException: " + expected);
		}
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().add(new FreeTextValDTO(INTEGER_FREE_TEXT, "1.1", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(REQUIRED_FREE_TEXT, "test", "no fhir-id"));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with invalid free texts");
		}
		catch (InvalidFreeTextException expected)
		{
			logger.info("expected InvalidFreeTextException: " + expected);
		}
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().add(new FreeTextValDTO(DOUBLE_FREE_TEXT, "a.1", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(REQUIRED_FREE_TEXT, "test", "no fhir-id"));
		try
		{
			cm2Manager.addConsent(consent);
			Assertions.fail("could create a consent with invalid free texts");
		}
		catch (InvalidFreeTextException expected)
		{
			logger.info("expected InvalidFreeTextException: " + expected);
		}

		logger.info("try to create a consent for that consent template with valid free texts");
		consent.getFreeTextVals().clear();
		consent.getFreeTextVals().add(new FreeTextValDTO(REQUIRED_FREE_TEXT, "test", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(DATE_FREE_TEXT, "18.03.2014", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(DOUBLE_FREE_TEXT, "465.134", "no fhir-id"));
		consent.getFreeTextVals().add(new FreeTextValDTO(INTEGER_FREE_TEXT, "115346", "no fhir-id"));
		cm2Manager.addConsent(consent);

		logger.info("try to create consent templates with erronous free texts");
		ConsentTemplateKeyDTO erronousCTKey = new ConsentTemplateKeyDTO(DOMAIN, "erronous free text ct", "1");
		ConsentTemplateDTO erronousCT = new ConsentTemplateDTO(erronousCTKey);
		erronousCT.setType(ConsentTemplateType.CONSENT);
		FreeTextDefDTO erronousFreeText = new FreeTextDefDTO("erronous free text", false, FreeTextType.DATE, "d.MGTRH.yyyy", 1, "comment", false, null, null, null, null, null);
		erronousCT.getFreeTextDefs().add(erronousFreeText);
		try
		{
			cm2Manager.addConsentTemplate(erronousCT, false);
			Assertions.fail("could create a free text with an invalid converter string");
		}
		catch (FreeTextConverterStringException expected)
		{
			logger.info("expected FreeTextConverterStringException: " + expected);
		}
		erronousFreeText = new FreeTextDefDTO("erronous free text", false, FreeTextType.DATE, null, 1, "comment", false, null, null, null, null, null);
		erronousCT.getFreeTextDefs().clear();
		erronousCT.getFreeTextDefs().add(erronousFreeText);
		try
		{
			cm2Manager.addConsentTemplate(erronousCT, false);
			Assertions.fail("could create a free text with an null converter string and type FreeTextType.Date");
		}
		catch (FreeTextConverterStringException expected)
		{
			logger.info("expected FreeTextConverterStringException: " + expected);
		}
		logger.info("### free text test end");
	}

	@Test
	public void domainTests() throws Exception
	{
		logger.info("### domain test start");
		logger.info("try to update some domain fields");
		Date date = new Date();
		String newLabel = "label_" + date;
		String newLogo = "logo_" + date;
		String newExternProperties = "externProperties_" + date;
		String newComment = "comment_" + date;

		cm2Manager.updateDomainInUse(DOMAIN, newLabel, newLogo, newExternProperties, newComment);
		DomainDTO domain = cm2Manager.getDomain(DOMAIN);
		Assertions.assertEquals(newLabel, domain.getLabel());
		Assertions.assertEquals(newLogo, domain.getLogo());
		Assertions.assertEquals(newExternProperties, domain.getExternProperties());
		Assertions.assertEquals(newComment, domain.getComment());
		Assertions.assertEquals(EXPIRATION_PROPERTIES, domain.getExpirationProperties());

		try
		{
			logger.info("test update domain methods");
			DomainDTO createDomainDTO = new DomainDTO(domainNameForUpdateTest, "dummy", SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
					SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo", idTypes, false, null, null, EXPIRATION_PROPERTIES, null);
			logger.info("creating test domain: " + createDomainDTO);
			cm2Manager.addDomain(createDomainDTO);
			createDomainDTO.setLabel(newLabel);
			createDomainDTO.setLogo(newLogo);
			createDomainDTO.setExternProperties(newExternProperties);
			createDomainDTO.setExpirationProperties(EXPIRATION_PROPERTIES);
			createDomainDTO.setComment(newComment);
			cm2Manager.updateDomain(createDomainDTO);
			domain = cm2Manager.getDomain(domainNameForUpdateTest);
			Assertions.assertEquals(newLabel, domain.getLabel());
			Assertions.assertEquals(newLogo, domain.getLogo());
			Assertions.assertEquals(newExternProperties, domain.getExternProperties());
			Assertions.assertEquals(newComment, domain.getComment());
			Assertions.assertEquals(EXPIRATION_PROPERTIES, domain.getExpirationProperties());
			cm2Manager.finaliseDomain(domainNameForUpdateTest);
			try
			{
				cm2Manager.updateDomain(createDomainDTO);
				Assertions.fail("could update a finalized domain");
			}
			catch (ObjectInUseException expected)
			{
				logger.info("expected ObjectInUseException: " + expected);
			}
		}
		finally
		{
			cm2Manager.deleteDomain(domainNameForUpdateTest);
		}
		logger.info("### domain test end");
	}

	@Test
	public void signerIdTests() throws Exception
	{
		logger.info("### signer id test start");
		logger.info("try to add and remove a signer id type");
		cm2Manager.addSignerIdType(DOMAIN, "dummySignerIdType");
		cm2Manager.deleteSignerIdType(DOMAIN, "dummySignerIdType");
		logger.info("test aliases");
		Set<SignerIdDTO> origIdSet = new HashSet<>();
		SignerIdDTO signerId = new SignerIdDTO(MPI_ID_TYPE, "signer id for alias", null, null);
		origIdSet.add(signerId);
		logger.info("create a consent template with free texts");
		ConsentTemplateKeyDTO tempCTKey = new ConsentTemplateKeyDTO(DOMAIN, "aliasCT" + System.currentTimeMillis(), "1");
		ConsentTemplateDTO tempCT = new ConsentTemplateDTO(tempCTKey);
		tempCT.setType(ConsentTemplateType.CONSENT);
		try
		{
			cm2Manager.addConsentTemplate(tempCT, false);
			cm2Manager.finaliseTemplate(tempCTKey, true);
		}
		catch (DuplicateEntryException ignore)
		{
			// kann sein, dass das schon angelegt wurde
			// loeschen ueber service nicht moeglich, sobald ein consent angelegt wurde
		}

		ConsentKeyDTO consentKey = new ConsentKeyDTO(tempCTKey, origIdSet, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);
		consent.setPatientSignatureBase64("dummy");
		consent.setPatientSigningDate(new Date());
		consent.setPatientSigningPlace("patplace");
		consent.setPhysicianSignatureBase64("dummy");
		consent.setPhysicianSigningDate(new Date());
		consent.setPhysicianSigningPlace("phyplace");
		consent.setPhysicianId("123");
		// ein consent anlegen, damit die signerId vorhanden ist
		cm2Manager.addConsent(consent);
		String alias = "alias" + new Date();
		SignerIdDTO aliasId = new SignerIdDTO(MPI_ID_TYPE, alias, null, null);
		try
		{
			cm2Manager.addAlias(DOMAIN, aliasId, signerId);
			Assertions.fail("could add an alias to an unknown id");
		}
		catch (UnknownSignerIdException expected)
		{
			cm2Manager.addAlias(DOMAIN, signerId, aliasId);
			List<SignerIdDTO> aliases = cm2Manager.getAliasesForSignerId(DOMAIN, signerId);
			Assertions.assertTrue(containsSignerId(aliases, aliasId), "aliases doesn't contain the added alias");
			cm2Manager.deactivateAlias(DOMAIN, signerId, aliasId);
			aliases = cm2Manager.getAliasesForSignerId(DOMAIN, signerId);
			Assertions.assertFalse(containsSignerId(aliases, aliasId), "aliases contains a deactivated alias");
			cm2Manager.addAlias(DOMAIN, signerId, aliasId);
			aliases = cm2Manager.getAliasesForSignerId(DOMAIN, signerId);
			Assertions.assertTrue(containsSignerId(aliases, aliasId), "aliases doesn't contain the readded alias");
		}
		logger.info("test alias consents");
		Set<SignerIdDTO> aliasIdSet = new HashSet<>();
		aliasIdSet.add(aliasId);
		Set<SignerIdDTO> bothIdSet = new HashSet<>();
		bothIdSet.add(signerId);
		bothIdSet.add(aliasId);
		consentKey = new ConsentKeyDTO(tempCTKey, aliasIdSet, new Date());
		consent = new ConsentDTO(consentKey);
		consent.setPatientSignatureBase64("dummy");
		consent.setPatientSigningDate(new Date());
		consent.setPatientSigningPlace("patplace");
		consent.setPhysicianSignatureBase64("dummy");
		consent.setPhysicianSigningDate(new Date());
		consent.setPhysicianSigningPlace("phyplace");
		consent.setPhysicianId("123");
		// ein consent anlegen, damit die signerId vorhanden ist
		cm2Manager.addConsent(consent);
		List<ConsentLightDTO> origConsents = cm2Manager.getAllConsentsForSignerIds(DOMAIN, origIdSet, false);
		List<ConsentLightDTO> aliasConsents = cm2Manager.getAllConsentsForSignerIds(DOMAIN, aliasIdSet, false);
		Assertions.assertTrue(aliasConsents.size() == 1, "wrong consents fetched for signerId " + aliasId);
		List<ConsentLightDTO> allConsents = cm2Manager.getAllConsentsForSignerIds(DOMAIN, bothIdSet, false);
		Assertions.assertTrue(allConsents.size() == origConsents.size() + 1, "wrong consents fetched for signerIds " + signerId + " + " + aliasId);
		Assertions.assertTrue(listEquals(allConsents, cm2Manager.getAllConsentsForSignerIds(DOMAIN, origIdSet, true)), "wrong consents fetched for signerId " + signerId + " with aliases");
		Assertions.assertTrue(listEquals(aliasConsents, cm2Manager.getAllConsentsForSignerIds(DOMAIN, aliasIdSet, true)),
				"wrong consents fetched for signerId " + aliasId + " with non existing aliases");
		cm2Manager.addAlias(DOMAIN, aliasId, signerId);
		Assertions.assertTrue(listEquals(allConsents, cm2Manager.getAllConsentsForSignerIds(DOMAIN, aliasIdSet, true)), "wrong consents fetched for signerId " + aliasId + " with existing aliases");
		cm2Manager.deactivateAlias(DOMAIN, signerId, aliasId);
		Assertions.assertTrue(listEquals(allConsents, cm2Manager.getAllConsentsForSignerIds(DOMAIN, aliasIdSet, true)), "wrong consents fetched for signerId " + aliasId + " with existing aliases");
		cm2Manager.deactivateAlias(DOMAIN, aliasId, signerId);
		Assertions.assertTrue(listEquals(aliasConsents, cm2Manager.getAllConsentsForSignerIds(DOMAIN, aliasIdSet, true)),
				"wrong consents fetched for signerId " + aliasId + " with deactivated aliases");
		signerId = new SignerIdDTO(MPI_ID_TYPE, "additional signer id", null, null);
		cm2Manager.addSignerIdToConsent(consentKey, signerId);
		// 2. aufruf zum testen, ob das sauber durchlaeuft - fuehrte vor version 2.13.3 zu einem datenbankfehler (duplicate key)
		cm2Manager.addSignerIdToConsent(consentKey, signerId);
		logger.info("### signer id test end");
	}

	private boolean containsSignerId(List<SignerIdDTO> signerIds, SignerIdDTO signerIdToFind)
	{
		for (SignerIdDTO signerId : signerIds)
		{
			if (signerId.equalsId(signerIdToFind))
			{
				return true;
			}
		}
		return false;
	}

	private boolean listEquals(List<?> first, List<?> second)
	{
		return first.size() == second.size() && first.containsAll(second) && second.containsAll(first);
	}

	@Test
	public void deletionTests() throws Exception
	{
		logger.info("### deletion tests start");
		DomainDTO domainDTO = new DomainDTO(DELETION_DOMAIN, DELETION_TEST, SimpleVersionConverter.class.getName(),
				SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo",
				idTypes, true, null, null, EXPIRATION_PROPERTIES, null);
		ctForDeletion.getAssignedModules().add(amForDeletion);
		ctForDeletion.setType(ConsentTemplateType.CONSENT);
		logger.info("add domain");
		cm2Manager.addDomain(domainDTO);
		logger.info("add policy");
		cm2Manager.addPolicy(policyForDeletion);
		logger.info("add module");
		cm2Manager.addModule(moduleForDeletion, false);
		logger.info("add consent template");
		cm2Manager.addConsentTemplate(ctForDeletion, false);
		logger.info("try to delete a used module");
		try
		{
			cm2Manager.deleteModule(moduleKeyForDeletion);
			Assertions.fail("could delete a used module");
		}
		catch (ObjectInUseException expected)
		{
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove consent template");
		cm2Manager.deleteConsentTemplate(ctKeyForDeletion);
		logger.info("try to get the deleted consent template");
		try
		{
			cm2Manager.getConsentTemplate(ctKeyForDeletion);
			Assertions.fail("deleted consent template still found");
		}
		catch (UnknownConsentTemplateException expected)
		{
			logger.info("expected UnknownConsentTemplateException: " + expected);
		}
		logger.info("try to delete a used policy");
		try
		{
			cm2Manager.deletePolicy(policyKeyForDeletion);
			Assertions.fail("could delete a used policy");
		}
		catch (ObjectInUseException expected)
		{
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove module");
		cm2Manager.deleteModule(moduleKeyForDeletion);
		logger.info("try to get the deleted module");
		try
		{
			cm2Manager.getModule(moduleKeyForDeletion);
			Assertions.fail("deleted module still found");
		}
		catch (UnknownModuleException expected)
		{
			logger.info("expected UnknownModuleException: " + expected);
		}
		logger.info("try to delete a used domain");
		try
		{
			cm2Manager.deleteDomain(DELETION_DOMAIN);
			Assertions.fail("could delete a used domain");
		}
		catch (ObjectInUseException expected)
		{
			logger.info("expected ObjectInUseException: " + expected);
		}
		logger.info("remove policy");
		cm2Manager.deletePolicy(policyKeyForDeletion);
		logger.info("try to get the deleted policy");
		try
		{
			cm2Manager.getPolicy(policyKeyForDeletion);
			Assertions.fail("deleted policy still found");
		}
		catch (UnknownPolicyException expected)
		{
			logger.info("expected UnknownPolicyException: " + expected);
		}
		logger.info("remove domain");
		cm2Manager.deleteDomain(DELETION_DOMAIN);
		logger.info("try to get the deleted domain");
		try
		{
			cm2Manager.getDomain(DELETION_DOMAIN);
			Assertions.fail("deleted domain still found");
		}
		catch (UnknownDomainException expected)
		{
			logger.info("expected UnknownDomainException: " + expected);
		}
		logger.info("### deletion tests end");
	}

	@Test
	public void testFhirID() throws Exception
	{
		final String domainName = "FhirIDTest";
		DomainDTO domainDTO = new DomainDTO(domainName, "dummy", SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
				SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo", idTypes, false, null, null, EXPIRATION_PROPERTIES, null);
		ModuleKeyDTO moduleKey = new ModuleKeyDTO(domainName, "test", "1");
		ModuleDTO module = new ModuleDTO(moduleKey, "kein sinnvoller modultext", NO_TITLE, NO_COMMENT, NO_EXTERN_PROPERTIES, new HashSet<>(), "no label", "no short text", false, null,
				null, null);
		try
		{
			logger.info("creating fhir id test entries");
			cm2Manager.addDomain(domainDTO);
			cm2Manager.finaliseDomain(domainName);
			cm2Manager.addModule(module, true);
			logger.info("test getObjectByFhirID with domain");
			DomainDTO domainByName = cm2Manager.getDomain(domainName);
			Assertions.assertNotNull(domainByName.getFhirID(), "fhirID is null");
			Assertions.assertTrue(!domainByName.getFhirID().isEmpty(), "fhirID is empty");
			DomainDTO domainByFhirID = cm2Manager.getObjectByFhirID(DomainDTO.class, domainByName.getFhirID());
			Assertions.assertEquals(domainByName.getFhirID(), domainByFhirID.getFhirID(), "wrong object delivered by getObjectByFhirID - different fhirID");
			Assertions.assertEquals(domainByName.getName(), domainByFhirID.getName(), "wrong object delivered by getObjectByFhirID - different name");
			module = cm2Manager.getModule(moduleKey);
			Assertions.assertTrue('-' == module.getFhirID().charAt(7), "fhirID of module isn't modified (see comment in Module.modifyFhirID)");
		}
		finally
		{
			cm2Manager.deleteModule(moduleKey);
			cm2Manager.deleteDomain(domainName);
		}
	}
}
