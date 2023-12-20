package org.emau.icmvc.ganimed.ttp.cm2.servicebased;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentScan;
import org.emau.icmvc.ganimed.ttp.cm2.version.SimpleVersionConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConsentScanTests extends AbstractServiceBasedTest
{
	/*-
	loeschskript:
	delete from qc_hist where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from qc where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from signed_policy where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from signature where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from free_text_val where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from consent_scan where CT_DOMAIN_NAME like 'test12345_scan_cm_test%' or CT_DOMAIN_NAME like 'test12345_cm_version_test%' or CT_DOMAIN_NAME like 'test12345_cm_deletion_test%' or CT_DOMAIN_NAME like 'test12345_scan_cm_test%' or CT_DOMAIN_NAME like 'oidTestDomain%';
	delete from consent where CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'test12345_cm_version_test' or CT_DOMAIN_NAME = 'test12345_scan_cm_test' or CT_DOMAIN_NAME = 'oidTestDomain';
	delete from module_consent_template where CT_DOMAIN = 'test12345_scan_cm_test' or CT_DOMAIN = 'test12345_cm_version_test' or CT_DOMAIN = 'test12345_cm_deletion_test' or CT_DOMAIN = 'test12345_scan_cm_test' or CT_DOMAIN = 'oidTestDomain';
	delete from module_policy where M_DOMAIN_NAME = 'test12345_scan_cm_test' or M_DOMAIN_NAME = 'test12345_cm_version_test' or M_DOMAIN_NAME = 'test12345_cm_deletion_test' or M_DOMAIN_NAME = 'test12345_scan_cm_test' or M_DOMAIN_NAME = 'oidTestDomain';
	delete from free_text_def where DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from consent_template where DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from consent_template_scan where ID like 'test12345_scan_cm_test%' or ID like 'test12345_cm_version_test%' or ID like 'test12345_cm_deletion_test%' or ID like 'test12345_scan_cm_test%' or ID like 'oidTestDomain%';
	delete from module where DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain' or DOMAIN_NAME = 'FhirIDTest';
	delete from policy where DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain';
	delete from text where ID like 'test12345_scan_cm_test%' or ID like 'test12345_cm_version_test%' or ID like 'test12345_cm_deletion_test%' or ID like 'test12345_scan_cm_test%' or ID like 'oidTestDomain%' or ID like 'FhirIDTest%';
	delete from virtual_person_signer_id where SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test' or SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'oidTestDomain';
	delete from alias where ORIG_SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or ALIAS_SIT_DOMAIN_NAME = 'test12345_scan_cm_test';
	delete from signer_id where SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'test12345_cm_version_test' or SIT_DOMAIN_NAME = 'test12345_scan_cm_test' or SIT_DOMAIN_NAME = 'oidTestDomain';
	delete from signer_id_type where DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'test12345_cm_version_test' or DOMAIN_NAME = 'test12345_cm_deletion_test' or DOMAIN_NAME = 'crash-test-dummy' or DOMAIN_NAME = 'test12345_scan_cm_test' or DOMAIN_NAME = 'oidTestDomain' or DOMAIN_NAME ='dummy_domain_for_update_test' or DOMAIN_NAME = 'FhirIDTest';
	delete from domain where NAME = 'test12345_scan_cm_test' or NAME = 'test12345_cm_version_test' or NAME = 'test12345_cm_deletion_test' or NAME = 'crash-test-dummy' or NAME = 'test12345_scan_cm_test' or NAME = 'oidTestDomain' or NAME ='dummy_domain_for_update_test' or NAME = 'FhirIDTest';
	 */

	private static final Logger logger = LogManager.getLogger(ConsentScanTests.class);

	private static final String DOMAIN = "test12345_scan_cm_test";
	private static final ConsentTemplateKeyDTO ctKey1 = new ConsentTemplateKeyDTO(DOMAIN, DOMAIN + "_testScanCT1" + java.time.LocalTime.now().toString(), "1");
	private static final ConsentTemplateKeyDTO ctKey2 = new ConsentTemplateKeyDTO(DOMAIN, DOMAIN + "_testScanCT2" + java.time.LocalTime.now().toString(), "1");
	private static final List<String> idTypes = Collections.singletonList(MPI_ID_TYPE);

	@BeforeAll
	public static void storeBasicEntries() throws VersionConverterClassException, InvalidParameterException
	{
		logger.info("setup");

		setupServices();

		try
		{
			cm2Service.getDomain(DOMAIN);
		}
		catch (UnknownDomainException ude)
		{
			try
			{
				DomainDTO domainDTO = new DomainDTO(DOMAIN, DOMAIN, SimpleVersionConverter.class.getName(), SimpleVersionConverter.class.getName(),
						SimpleVersionConverter.class.getName(), "", "test-domain", "no extern properties", "logo", idTypes, true, null, null, new ExpirationPropertiesDTO(), null);
				logger.info("creating test domain: " + domainDTO);
				cm2Manager.addDomain(domainDTO);
			}
			catch (DuplicateEntryException | InvalidParameterException dee)
			{
				// geht nicht
			}
		}

		logger.info("create consent templates");
		ConsentTemplateDTO ct1 = new ConsentTemplateDTO(ctKey1);
		ConsentTemplateDTO ct2 = new ConsentTemplateDTO(ctKey2);
		ct1.setFinalised(true);
		ct2.setFinalised(true);
		ct1.setType(ConsentTemplateType.CONSENT);
		ct2.setType(ConsentTemplateType.CONSENT);

		try
		{
			cm2Manager.addConsentTemplate(ct1, true);
			cm2Manager.addConsentTemplate(ct2, true);
		}
		catch (Exception e)
		{
			// Template bereits vorhanden
			logger.info(e.getMessage());
		}

		logger.info("setup complete");
	}

	@AfterAll
	public static void deleteBasicEntries()
	{
		logger.info("teardown");
		logger.info("delete the consent templates");
		try
		{
			cm2Manager.deleteConsentTemplate(ctKey1);
			cm2Manager.deleteConsentTemplate(ctKey2);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			// Template bereits in Benutzung
		}

		logger.info("delete the domain");

		try
		{
			cm2Manager.deleteDomain(DOMAIN);
		}
		catch (Exception e)
		{
			// Domain enthaelt bereits Templates/Consents...
		}

		logger.info("finished");
	}

	@Test
	public void testScanOkService() throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException, MissingRequiredObjectException,
			InvalidFreeTextException, MandatoryFieldsException, UnknownSignerIdTypeException,
			DuplicateEntryException, InconsistentStatusException, UnknownConsentException, RequirementsNotFullfilledException, InvalidParameterException
	{
		logger.info("--- testScanOk start ---");

		String base64 = "SGFsbG8gV2VsdCE=";

		logger.info("create consent");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "ID1", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKey1, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);

		List<ConsentScanDTO> cs = new ArrayList<>();
		cs.add(new ConsentScanDTO(consentKey, base64, "", null, null));

		logger.info("set base64 scan");
		consent.setScans(cs);

		logger.info("add consent");
		cm2Service.addConsent(consent);

		ConsentDTO tmpConsent = cm2Service.getConsent(consent.getKey());

		logger.info("check base64 strings are equal");
		assertEquals(base64, tmpConsent.getScans().get(0).getBase64());
	}

	@Test
	public void testScanBase64ErroneousService()
	{
		logger.info("--- testScanBase64Erroneous start ---");

		String base64 = "SGFsbG8gV2VsdCE=,"; // contains "," --> Allowed: A-Z, a-z, 0-9, /, +, =

		logger.info("create consent");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "ID2", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKey2, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);

		List<ConsentScanDTO> cs = new ArrayList<>();
		cs.add(new ConsentScanDTO(consentKey, base64, "", null, null));

		logger.info("set base64 scan");
		consent.setScans(cs);

		assertThrows(Exception.class, () ->
		{
			logger.info("add consent");
			cm2Service.addConsent(consent);
			fail("Expected exception was not throwed");
		});
	}

	@Test
	public void testScanConstructorOK() throws InvalidParameterException
	{
		final String base64 = "SGFsbG8gV2VsdCE=";
		final Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
		final String fileName = "filename";

		ConsentScan s = new ConsentScan(null, base64, "", fileName);
		assertEquals(base64, s.getContent());
		assertEquals(fileName, s.getFileName());
		assertEquals(today, DateUtils.truncate(s.getUploadDate(), Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testScanConstructorErroneous()
	{
		final String base64 = "SGFsbG8gV2VsdCE=aaaa";

		assertThrows(InvalidParameterException.class, () -> new ConsentScan(null, base64, "", null));
	}

	@Test
	public void testScanUpdateErroneous() throws InvalidParameterException
	{
		final String base64 = "SGFsbG8gV2VsdCE=aaaa";
		ConsentScan s = new ConsentScan(null, null, "", null);
		assertThrows(InvalidParameterException.class, () -> s.update(base64, null, null));
	}

	@Test
	public void testScanFileTypeOk() throws UnknownModuleException, UnknownDomainException, InvalidParameterException, UnknownSignerIdTypeException, InvalidFreeTextException, DuplicateEntryException,
			RequirementsNotFullfilledException, MandatoryFieldsException, UnknownConsentTemplateException, MissingRequiredObjectException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentException
	{
		logger.info("--- testScanFileTypeOk start ---");

		String base64 = "SGFsbG8gV2VsdCE=";
		String fileType = "text/plain";

		logger.info("create consent");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "ID3", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKey2, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);

		List<ConsentScanDTO> cs = new ArrayList<>();
		cs.add(new ConsentScanDTO(consentKey, base64, fileType, null, null));

		logger.info("set base64 scan");
		consent.setScans(cs);

		logger.info("add consent");
		cm2Service.addConsent(consent);

		logger.info("request consent");
		ConsentDTO returnedConsent = cm2Service.getConsent(consentKey);

		assertEquals(base64, returnedConsent.getScans().get(0).getBase64());
		assertEquals(fileType, returnedConsent.getScans().get(0).getFileType());
	}

	@Test
	public void testMultipleScansOk() throws UnknownModuleException, UnknownDomainException, InvalidParameterException, UnknownSignerIdTypeException, InvalidFreeTextException, DuplicateEntryException,
			RequirementsNotFullfilledException, MandatoryFieldsException, UnknownConsentTemplateException, MissingRequiredObjectException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException, UnknownConsentException
	{
		logger.info("--- testMultipleScansOk start ---");

		String base64 = "SGFsbG8gV2VsdCE=";
		String fileType = "text/plain";

		logger.info("create consent");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "ID4", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKey2, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);

		int numberOfScans = 4;
		List<ConsentScanDTO> cs = new ArrayList<>();
		for (int i = 0; i < numberOfScans; ++i)
		{
			cs.add(new ConsentScanDTO(consentKey, base64, fileType, null, null));
		}

		logger.info("set scans");
		consent.setScans(cs);

		logger.info("add consent");
		cm2Service.addConsent(consent);

		logger.info("request consent");
		ConsentDTO returnedConsent = cm2Service.getConsent(consentKey);

		assertEquals(numberOfScans, returnedConsent.getScans().size());
	}

	@Test
	public void testRemoveScansOk() throws UnknownModuleException, UnknownDomainException, InvalidParameterException, UnknownSignerIdTypeException, InvalidFreeTextException, DuplicateEntryException,
			RequirementsNotFullfilledException, MandatoryFieldsException, UnknownConsentTemplateException, MissingRequiredObjectException,  InvalidVersionException,
			InconsistentStatusException, UnknownConsentException
	{
		logger.info("--- testRemoveScansOk start ---");

		String base64 = "SGFsbG8gV2VsdCE=";
		String fileType = "text/plain";

		logger.info("create consent");
		Set<SignerIdDTO> ids = new HashSet<>();
		ids.add(new SignerIdDTO(MPI_ID_TYPE, "ID5", null, null));
		ConsentKeyDTO consentKey = new ConsentKeyDTO(ctKey2, ids, new Date());
		ConsentDTO consent = new ConsentDTO(consentKey);

		int numberOfScans = 4;
		List<ConsentScanDTO> cs = new ArrayList<>();
		for (int i = 0; i < numberOfScans; ++i)
		{
			cs.add(new ConsentScanDTO(consentKey, base64, fileType, null, null));
		}

		logger.info("set scans");
		consent.setScans(cs);

		logger.info("add consent");
		cm2Service.addConsent(consent);

		logger.info("request consent");
		ConsentDTO returnedConsent = cm2Service.getConsent(consentKey);
		ConsentScanDTO scan = returnedConsent.getScans().get(0);

		assertEquals(numberOfScans, returnedConsent.getScans().size());

		logger.info("remove scan from consent");
		cm2Service.removeScanFromConsent(consentKey, scan.getFhirID());

		logger.info("request consent");
		returnedConsent = cm2Service.getConsent(consentKey);
		assertEquals(numberOfScans - 1, returnedConsent.getScans().size());
	}
}
