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

import java.time.Period;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeAction;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeError;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeField;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeOccurrence;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractServiceBasedTest;
import org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter;

public class TestHelper
{
	public static final String CHECKED_NO_FAULTS = "checked_no_faults";
	public static final String NOT_CHECKED = "not_checked";
	public static final String CHECKED_MINOR_FAULTS = "checked_minor_faults";
	public static final String CHECKED_MAJOR_FAULTS = "checked_major_faults";
	public static final String INVALIDATED = "invalidated";
	public static final String DOMAIN_PROPERTIES =
			";VALID_QC_TYPES=" + NOT_CHECKED + "," + CHECKED_NO_FAULTS + "," + CHECKED_MINOR_FAULTS + ";"
					+ "INVALID_QC_TYPES=" + CHECKED_MAJOR_FAULTS + "," + INVALIDATED + ";"
					+ "DEFAULT_QC_TYPE=" + NOT_CHECKED + ";"
					+ "SCANS_SIZE_LIMIT=10000000;"
					+ "REVOKE_IS_PERMANENT=true;"
					+ "TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST=true;"
					+ "TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST=true;"
					+ "SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS=true;"
					+ "SEND_NOTIFICATIONS_WEB=true;"
					+ "STATISTIC_DOCUMENT_DETAILS=true;"
					+ "STATISTIC_POLICY_DETAILS=true";
	public static final String ANOTHER_ID_TYPE = "another_id_type";
	public static final List<String> idTypes = Arrays.asList(AbstractServiceBasedTest.MPI_ID_TYPE, ANOTHER_ID_TYPE);
	public static final String DOMAIN = "test12345_cm_test";
	public static final String VERSION_DOMAIN = "test12345_cm_version_test";
	public static final String DELETION_DOMAIN = "test12345_cm_deletion_test";
	public static final String DELETION_TEST = "deletion_test";
	public static final ConsentTemplateKeyDTO ctKey21 = new ConsentTemplateKeyDTO(DOMAIN, "testCT2", "1");
	public static final ConsentTemplateKeyDTO ctKey13 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "3");
	public static final ConsentTemplateKeyDTO ctKey12 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "2");
	public static final ConsentTemplateKeyDTO ctKey11 = new ConsentTemplateKeyDTO(DOMAIN, "testCT1", "1");
	public static final ConsentTemplateDTO ct11 = new ConsentTemplateDTO(ctKey11);
	public static final PolicyKeyDTO nonExistingPolicyKey = new PolicyKeyDTO(DOMAIN, "notExistingPolicy", "1");
	public static final PolicyKeyDTO policyKey31 = new PolicyKeyDTO(DOMAIN, "test3", "1");
	public static final PolicyDTO policy31 = new PolicyDTO(policyKey31);
	public static final PolicyKeyDTO policyKey22 = new PolicyKeyDTO(DOMAIN, "test2", "2");
	public static final PolicyDTO policy22 = new PolicyDTO(policyKey22);
	public static final PolicyKeyDTO policyKey21 = new PolicyKeyDTO(DOMAIN, "test2", "1");
	public static final PolicyDTO policy21 = new PolicyDTO(policyKey21);
	public static final PolicyKeyDTO policyKey11 = new PolicyKeyDTO(DOMAIN, "test1", "1");
	public static final PolicyDTO policy11 = new PolicyDTO(policyKey11);
	public static final PolicyKeyDTO policyKeyForDeletion = new PolicyKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	public static final PolicyDTO policyForDeletion = new PolicyDTO(policyKeyForDeletion);
	public static final ModuleKeyDTO moduleKey2231 = new ModuleKeyDTO(DOMAIN, "test2231", "2");
	public static final ModuleDTO module2231 = new ModuleDTO(moduleKey2231, "kein sinnvoller modultext 2231", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy22), new AssignedPolicyDTO(policy31)), "no label", "no short text", false, null, null, null);
	public static final ModuleKeyDTO moduleKey1131 = new ModuleKeyDTO(DOMAIN, "test1131", "1");
	public static final ModuleDTO module1131 = new ModuleDTO(moduleKey1131, "kein sinnvoller modultext 1131", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy31)), "no label", "no short text", false, null, null, null);
	public static final ModuleKeyDTO moduleKey1122 = new ModuleKeyDTO(DOMAIN, "test1122", "1");
	public static final ModuleDTO module1122 = new ModuleDTO(moduleKey1122, "kein sinnvoller modultext 1122", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy21)), "no label", "no short text", false, null, null, null);
	public static final ModuleKeyDTO moduleKey1121 = new ModuleKeyDTO(DOMAIN, "test1121", "1");
	public static final ModuleDTO module1121 = new ModuleDTO(moduleKey1121, "kein sinnvoller modultext 1121", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policy11), new AssignedPolicyDTO(policy21)), "no label", "no short text", false, null, null, null);
	public static final ModuleKeyDTO moduleKey11 = new ModuleKeyDTO(DOMAIN, "test11", "1");
	public static final ModuleDTO module11 = new ModuleDTO(moduleKey11, "kein sinnvoller modultext 11", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES,
			Set.of(new AssignedPolicyDTO(policy11)), "no label", "no short text", false, null, null, null);
	// private static final String DOMAIN_OID_TEST = "oidTestDomain";
	public static final ConsentTemplateKeyDTO ctKeyForDeletion = new ConsentTemplateKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	public static final ConsentTemplateDTO ctForDeletion = new ConsentTemplateDTO(ctKeyForDeletion);
	public static final ModuleKeyDTO moduleKeyForDeletion = new ModuleKeyDTO(DELETION_DOMAIN, DELETION_TEST, "1");
	public static final ModuleDTO moduleForDeletion = new ModuleDTO(moduleKeyForDeletion, "kein sinnvoller modultext", AbstractServiceBasedTest.NO_TITLE, AbstractServiceBasedTest.NO_COMMENT,
			AbstractServiceBasedTest.NO_EXTERN_PROPERTIES, Set.of(new AssignedPolicyDTO(policyForDeletion)), "no label", "no short text", false, null, null, null);
	public static final String domainNameForUpdateTest = "dummy_domain_for_update_test";
	public static final ExpirationPropertiesDTO EXPIRATION_PROPERTIES = new ExpirationPropertiesObject( /* tomorrow normalized without time part */
			new Date(new Date().getTime() + 1000 * 24 * 60 * 60), Period.of(1, 2, 3)).normalized().toDTO();
	public static final String signer_id = "12345_test";
	public static final List<ConsentStatus> allConsentStatus = Arrays.asList(ConsentStatus.values());
	public static final AssignedModuleDTO amForDeletion = new AssignedModuleDTO(moduleForDeletion, true, ConsentStatus.UNKNOWN, allConsentStatus, 0,
			null, "no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	public static final AssignedModuleDTO am2231 = new AssignedModuleDTO(module2231, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, moduleKey11,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	public static final AssignedModuleDTO am1122 = new AssignedModuleDTO(module1122, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	public static final AssignedModuleDTO am1121 = new AssignedModuleDTO(module1121, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	public static final AssignedModuleDTO am11 = new AssignedModuleDTO(module11, true, ConsentStatus.UNKNOWN, allConsentStatus, 0, null,
			"no comment", "no extern properties", EXPIRATION_PROPERTIES, null);
	public static final DomainConfig DOMAIN_CONFIG = DomainConfig.createDefaultDomainConfig();
	public static DomainDTO domain1 = new DomainDTO(DOMAIN, "dummy", SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
			SimpleVersionConverter.class.getName(), DOMAIN_CONFIG, "test-domain",
			"no extern properties", "logo", idTypes, false, null, null, EXPIRATION_PROPERTIES, null, null);
	public static final QCProblemTypeAction qcProblemTypeActionFixNow = new QCProblemTypeAction("fix-now",
			Map.of("de", "Schnell fixen", "en", "Fix quickly"));
	public static final QCProblemTypeAction qcProblemTypeActionFixLater = new QCProblemTypeAction("fix-later",
			Map.of("de", "Später fixen", "en", "Fix later"));
	public static final QCProblemType qcProblemTypeWithName = new QCProblemType("problem-with-name",
			QCProblemTypeError.INCONSISTENT, QCProblemTypeField.IDAT_LASTNAME, QCProblemTypeOccurrence.BOTH, qcProblemTypeActionFixNow,
			Map.of("de", "Problem mit dem Namen", "en", "Problem with the name"));
	public static final QCProblemType qcProblemTypeWithScan = new QCProblemType("problem-with-scan",
			QCProblemTypeError.MISSING_PART, QCProblemTypeField.SIGNATURE_PARTICIPANT_DATE, QCProblemTypeOccurrence.DIGITAL, qcProblemTypeActionFixLater,
			Map.of("de", "Problem mit dem Scan", "en", "Problem with the scan"));

}
