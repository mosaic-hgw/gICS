package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
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

/**
 * implementation of the gics-service with notification
 *
 * @author moser
 *
 */
@WebService(name = "gicsServiceWithNotification")
@Remote(GICSServiceWithNotification.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class GICSServiceWithNotificationImpl extends GICSServiceBase implements GICSServiceWithNotification
{
	@Override
	public void addConsent(String notificationClientID, ConsentDTO consentDTO)
			throws UnknownDomainException, UnknownModuleException, UnknownConsentTemplateException, InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException,
			MandatoryFieldsException, UnknownSignerIdTypeException, DuplicateEntryException, RequirementsNotFullfilledException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("add " + consentDTO + " with notificationClientID " + notificationClientID);
		}
		checkParameter(consentDTO, "consentDTO");
		checkAllowedDomain(consentDTO);
		dad.addConsent(consentDTO, notificationClientID);
		if (logger.isInfoEnabled())
		{
			logger.info("added " + consentDTO + " with notificationClientID " + notificationClientID);
		}
	}

	@Override
	public void updateConsentInUse(String notificationClientID, ConsentKeyDTO keyDTO, String externProperties, String comment, ConsentScanDTO scan)
			throws InvalidVersionException, UnknownDomainException, UnknownConsentTemplateException,
			UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("update" + createLogMessageForUpdateConsent(keyDTO, externProperties, comment, scan) + " with notificationClientID " + notificationClientID);
		}
		checkParameter(keyDTO, "keyDTO");
		checkAllowedDomain(keyDTO);
		dad.updateConsentInUse(keyDTO, externProperties, comment, scan, notificationClientID);
		if (logger.isInfoEnabled())
		{
			logger.info("updated" + createLogMessageForUpdateConsent(keyDTO, externProperties, comment, scan) + " with notificationClientID " + notificationClientID);
		}
	}

	@Override
	public void refuseConsent(String notificationClientID, ConsentTemplateKeyDTO ctKeyDTO, Set<SignerIdDTO> signerIdDTOs)
			throws InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException,
			UnknownDomainException, InternalException, InvalidParameterException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("refuse" + createLogMessageForRefuseConsent(ctKeyDTO, signerIdDTOs) + " with notificationClientID " + notificationClientID);
		}
		checkParameter(ctKeyDTO, "ctKeyDTO");
		checkParameter(signerIdDTOs, "signerIdDTOs");
		checkAllowedDomain(ctKeyDTO);
		dad.refuseConsent(ctKeyDTO, signerIdDTOs, notificationClientID);
		if (logger.isInfoEnabled())
		{
			logger.info("refused " + createLogMessageForRefuseConsent(ctKeyDTO, signerIdDTOs) + " with notificationClientID " + notificationClientID);
		}
	}

	@Override
	public void setQCForConsent(String notificationClientID, ConsentKeyDTO consentKeyDTO, QCDTO qc)
			throws InvalidParameterException, InvalidVersionException, UnknownConsentException,
			UnknownSignerIdTypeException, UnknownConsentTemplateException, UnknownDomainException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("setting quality control " + qc + " for " + consentKeyDTO + " with notificationClientID " + notificationClientID);
		}
		checkParameter(consentKeyDTO, "consentKeyDTO");
		checkAllowedDomain(consentKeyDTO);
		dad.setQcForConsent(consentKeyDTO, qc, notificationClientID);
		if (logger.isDebugEnabled())
		{
			logger.debug("done setting quality control " + qc + " for " + consentKeyDTO + " with notificationClientID " + notificationClientID);
		}
	}
}
