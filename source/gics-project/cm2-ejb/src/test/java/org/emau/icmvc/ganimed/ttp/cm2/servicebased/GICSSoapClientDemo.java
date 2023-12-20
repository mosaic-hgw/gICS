package org.emau.icmvc.ganimed.ttp.cm2.servicebased;

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

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
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

/**
 * Simple gICS SOAP client for demo and testing purposes
 */
public class GICSSoapClientDemo extends AbstractServiceBasedTest
{
	private static final Logger logger = LogManager.getLogger(CM2ManagerTests.class);

	enum QCType
	{
		initial, // QC status of patient zero
		not_checked, checked_no_faults, checked_minor_faults, checked_major_faults, invalidated
	}

	private final String domain;
	private final String patientZeroSignerIDType;
	private final String patientZeroSignerID;
	private final ConsentDTO patientZero;

	public GICSSoapClientDemo(String domain, String patientZeroSignerIDType, String patientZeroSignerID)
	{
		logger.info("setup");

		setupServices();

		if (cm2Service == null)
		{
			throw new IllegalStateException("CM2 manager is null");
		}

		this.domain = domain;
		this.patientZeroSignerIDType = patientZeroSignerIDType;
		this.patientZeroSignerID = patientZeroSignerID;

		try
		{
			Set<SignerIdDTO> patientZeroSignerIDs = Collections.singleton(new SignerIdDTO(patientZeroSignerIDType, patientZeroSignerID));
			ConsentLightDTO patientZeroConsentLight = cm2Service.getAllConsentsForSignerIds(domain, patientZeroSignerIDs, false).stream().findFirst().orElse(null);

			if (patientZeroConsentLight == null)
			{
				throw new IllegalArgumentException("Patient zero with SignerID " + patientZeroSignerID + " inot found");
			}

			patientZero = cm2Service.getConsent(patientZeroConsentLight.getKey());
			patientZero.getKey().setSignerIds(patientZeroSignerIDs);
		}
		catch (UnknownDomainException | UnknownSignerIdTypeException | InvalidParameterException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownConsentException e)
		{
			throw new IllegalStateException(e);
		}

		logger.info("setup complete, patient zero: " + patientZero);
	}

	private void addConsentsForNextPersons(int number, QCType qcType, boolean withSignatures, boolean withScan)
	{
		try
		{
			int start = cm2Service.getAllIdsForSignerIdType(domain, patientZeroSignerIDType).size();

			for (int i = 1; i <= number; i++)
			{
				ConsentDTO consent = cloneConsentForPerson(start + i, qcType, withSignatures, withScan);
				cm2Service.addConsent(consent);
				logger.info("added " + consent);
			}
		}
		catch (UnknownDomainException | UnknownSignerIdTypeException | UnknownModuleException | UnknownConsentTemplateException | InvalidVersionException | MissingRequiredObjectException
				| InvalidFreeTextException | MandatoryFieldsException | DuplicateEntryException | RequirementsNotFullfilledException | InvalidParameterException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ConsentDTO cloneConsentForPerson(int personNumber, QCType qcType, boolean withSignatures, boolean withScan)
	{
		ConsentKeyDTO key = patientZero.getKey();
		SignerIdDTO signerId = key.getSignerIds().stream().filter(s -> s.getIdType().equals(patientZeroSignerIDType)).findFirst().orElse(null);

		if (signerId == null)
		{
			throw new IllegalStateException("Missing signerID of type " + patientZeroSignerIDType + " for patient zero " + patientZeroSignerID);
		}

		signerId = new SignerIdDTO(patientZeroSignerIDType, "p" + String.format("%04d", personNumber), signerId.getCreationDate(), null);
		key = new ConsentKeyDTO(key.getConsentTemplateKey(), Collections.singleton(signerId), key.getConsentDate());

		ConsentDTO consent = new ConsentDTO(patientZero);
		consent.setKey(key);
		consent.setExpirationProperties(patientZero.getExpirationProperties());

		if (!withScan)
		{
			consent.setScans(Collections.emptyList());
		}

		if (!withSignatures)
		{
			consent.setPatientSignatureBase64(null);
			consent.setPhysicianSignatureBase64(null);
		}

		if (qcType != null && !qcType.equals(QCType.initial))
		{
			consent.setQualityControl(new QCDTO(true, qcType.name(), new Date(), "ich", "", "", null));
		}

		return consent;
	}

	private static String argOrElse(String[] args, int index, String def)
	{
		return args != null && args.length > index && args[index] != null && !args[index].isEmpty() ? args[index] : def;
	}

	public static void main(String[] args)
	{
		try
		{
			String domain = argOrElse(args, 0, "UKER");
			String patientZeroSignerIDType = argOrElse(args, 1, "PatID");
			String patientZeroSignerID = argOrElse(args, 2, "p1");

			GICSSoapClientDemo demo = new GICSSoapClientDemo(domain, patientZeroSignerIDType, patientZeroSignerID);

			int number = Integer.parseInt(argOrElse(args, 3, "5"));
			QCType qcType = QCType.valueOf(argOrElse(args, 4, QCType.initial.name()));
			boolean withSignature = Boolean.parseBoolean(argOrElse(args, 5, "true"));
			boolean withScan = Boolean.parseBoolean(argOrElse(args, 6, "true"));

			demo.addConsentsForNextPersons(number, qcType, withSignature, withScan);
		}
		catch (Throwable t)
		{
			logger.error("failed", t);
			System.exit(1);
		}

		logger.info("finished");
	}
}
