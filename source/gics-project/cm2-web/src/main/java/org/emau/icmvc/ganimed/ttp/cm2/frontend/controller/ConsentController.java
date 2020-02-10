package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

import java.io.IOException;
import java.io.InputStream;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * Medicine of the University Medicine Greifswald -
 * mosaic-projekt@uni-greifswald.de
 * 
 * concept and implementation
 * l.geidel
 * web client
 * a.blumentritt, m.bialke
 * 
 * Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG
 * HO 1937/5-1).
 * 
 * please cite our publications
 * http://dx.doi.org/10.3414/ME14-01-0133
 * http://dx.doi.org/10.1186/s12967-015-0545-6
 * http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;

import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidConsentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MandatoryFieldsException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsent;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsentLazyModel;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate.TemplateType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.Visibility;
import org.primefaces.util.Base64;

/**
 * Backing Bean for Consent Document View
 * 
 * @author Arne Blumentritt, Martin Bialke
 * 
 */
@ManagedBean(name = "consentController")
@ViewScoped
public class ConsentController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = 9039910194305913160L;

	// Modes
	private Mode mode;

	// List consents
	private List<ConsentLightDTO> consents;
	private LazyDataModel<WebConsent> consentsLazyModel;
	private WebConsent selectedConsent;
	private ConsentTemplateType templateType;

	// Create consent / invalidation
	private WebConsent editConsent;
	private List<ConsentTemplateDTO> templates;
	private ConsentStatus invalidationStatus;
	private SignerIdDTO newSignerId = new SignerIdDTO();

	// Parse consents
	private ConsentTemplateDetector detector = new ConsentTemplateDetector();

	// Search consents
	private SignerIdDTO searchSignerId = null;

	// Print consents
	private List<WebConsent> printConsents = new ArrayList<>();

	/**
	 * Initialize controller, check session map if printing is requested.
	 * Called by pre-render-view event listener.
	 * 
	 * @param templateType
	 */
	@SuppressWarnings("unchecked")
	public void init(String templateType, String sidKey, String sidValue)
	{
		// skip ajax posts
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}
		mode = Mode.LIST;
		printConsents.clear();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();

		// Search Mode
		if (!StringUtils.isEmpty(sidValue))
		{
			this.searchSignerId = new SignerIdDTO(sidKey.equals("null") ? null : sidKey, sidValue);
			mode = Mode.SEARCH;
		}
		// Print Mode (Template)
		else if (sessionMap.containsKey("printTemplate"))
		{
			ConsentTemplateDTO template = getTemplate((ConsentTemplateKeyDTO) sessionMap.get("printTemplate"));
			sessionMap.remove("printTemplate");

			// Look for signerIds to print
			if (sessionMap.containsKey("printSignerIds"))
			{
				for (HashMap<String, String> consentSignerIds : (List<HashMap<String, String>>) sessionMap.get("printSignerIds"))
				{
					WebConsent consent = new WebConsent(new ConsentKeyDTO());
					consent.setTemplate(template);
					consent.setDomain(domainSelector.getSelectedDomain());
					consent.getKey().getSignerIds().clear();
					for (Entry<String, String> sid : consentSignerIds.entrySet())
					{
						SignerIdDTO id = new SignerIdDTO(sid.getKey(), sid.getValue());
						consent.getKey().getSignerIds().add(id);
					}
					printConsents.add(consent);
				}
				sessionMap.remove("printSignerIds");
			}
			else
			{
				WebConsent consent = new WebConsent(new ConsentKeyDTO());
				consent.setTemplate(template);
				consent.setDomain(domainSelector.getSelectedDomain());
				printConsents.add(consent);
			}

			mode = Mode.PRINT;
		}
		// Print Mode (Consents)
		else if (sessionMap.containsKey("printConsent"))
		{
			WebConsent consent = (WebConsent) sessionMap.get("printConsent");
			sessionMap.remove("printConsent");
			consent.setTemplate(getTemplate(consent.getKey().getConsentTemplateKey()));
			consent.setDomain(domainSelector.getSelectedDomain());

			printConsents.add(consent);
			mode = Mode.PRINT;
		}

		// Read template type
		try
		{
			this.templateType = ConsentTemplateType.valueOf(templateType);
		}
		catch (IllegalArgumentException e)
		{
			if (this.mode == Mode.SEARCH)
			{
				this.templateType = null;
			}
			else
			{
				this.templateType = ConsentTemplateType.CONSENT;
			}
		}

		// Load list of consents
		loadConsents();
	}

	/**
	 * Select template for new consent and init modules, freetexts and signerIds accordingly.
	 */
	public void onSelectTemplate()
	{
		editConsent.setTemplate(getTemplate(editConsent.getKey().getConsentTemplateKey()));
		editConsent.setDomain(domainSelector.getSelectedDomain());
	}

	/**
	 * Cancel creating/invalidating a consent and resetting form fields.
	 */
	public void onCancelEdit()
	{
		RequestContext.getCurrentInstance().reset("main");
		mode = Mode.LIST;
	}

	/**
	 * Start creating a new consent.
	 */
	public void onNewConsent()
	{
		editConsent = new WebConsent(new ConsentKeyDTO());
		try
		{
			loadTemplates();
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		mode = Mode.NEW;
	}

	/**
	 * Start parsing a new consent.
	 */
	public void onNewParse()
	{
		onNewConsent();
		mode = Mode.PARSE;
	}

	/**
	 * Cancel parsing a new consent.
	 */
	public void onCancelParse()
	{
		mode = Mode.LIST;
	}

	/**
	 * Perform parsing.
	 * 
	 * @param event
	 */
	public void onParse(FileUploadEvent event)
	{
		if (event.getFile() != null)
		{
			logger.debug("Upload of PDF: " + event.getFile().getFileName()
					+ ", filesize in bytes: " + event.getFile().getSize());

			// store file locally
			Path folder = Paths.get("");
			String filename = FilenameUtils.getBaseName(event.getFile().getFileName());
			String extension = FilenameUtils.getExtension(event.getFile().getFileName());
			try
			{
				Path file = Files.createTempFile(folder, filename + "-", "." + extension);
				try (InputStream input = event.getFile().getInputstream())
				{
					Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
				}

				// process pdf file with n pages
				List<String> result = detector.decodePDF(file.toAbsolutePath().toString(), true);

				if (result.size() > 0)
				{
					// first entry should be first qr code with template info etc
					String qrCodeContent = result.get(0);

					String[] parsed = null;
					String[] signerIDs = null;

					List<SignerIdDTO> toBeUsedSids = new ArrayList<SignerIdDTO>();

					// signer ids contained?
					if (qrCodeContent.contains("#"))
					{
						String[] splitcontent = qrCodeContent.split("#");
						// first template key, template=domain;name;version

						parsed = splitcontent[0].split(";");
						// rest signerids and values, type=value
						signerIDs = Arrays.copyOfRange(splitcontent, 1, splitcontent.length);

						logger.debug("length signer ids: " + signerIDs.length);

						// check signerIDs
						for (String sid : signerIDs)
						{
							String[] splitid = sid.split("=");

							if (splitid != null && splitid.length == 2)
							{
								String type = splitid[0];
								String idvalue = splitid[1];
								logger.debug("SignerId " + type + "=" + idvalue);
								SignerIdDTO id = new SignerIdDTO();
								id.setIdType(type);
								id.setId(idvalue);
								toBeUsedSids.add(id);
							}
						}
					}
					else
					{
						parsed = qrCodeContent.split(";");
					}

					parsed[0] = parsed[0].replaceAll("template=", "");

					if (parsed.length != 3)
					{
						throw new IllegalArgumentException("Unexpected Format of template key: " + qrCodeContent);
					}
					ConsentTemplateKeyDTO detectedKey = new ConsentTemplateKeyDTO(parsed[0], parsed[1], parsed[2]);

					// check if template exists
					ConsentTemplateDTO template = getTemplate(detectedKey);
					if (template == null)
					{
						throw new UnknownConsentTemplateException();
					}
					editConsent.setTemplate(template);
					editConsent.setDomain(domainSelector.getSelectedDomain());

					// append scan to edit consent
					String scan = Base64.encodeToString(event.getFile().getContents(), true);
					this.editConsent.setScanBase64(scan);

					if (toBeUsedSids != null && toBeUsedSids.size() > 0)
					{
						editConsent.getKey().setSignerIds(new HashSet<SignerIdDTO>(toBeUsedSids));
					}

					logger.info("Successful parsed consent template: [DOMAIN=" + parsed[0] + "][NAME=" + parsed[1] + "][Version=" + parsed[2] + "]");
					logger.debug("Uploaded scan " + filename + " already appended to current consent. Please fill in remaining fields.");

					mode = Mode.NEW;
				}
				else
				{
					// TODO message auslagern @bialkem
					logMessage("No QR-Code found.", Severity.INFO);
				}
			}
			catch (IOException | UnknownConsentTemplateException e1)
			{
				// TODO message auslagern @bialkem
				logMessage("Invalid File or QR Code. Unable to open consent template: " + e1.getMessage(), Severity.ERROR);
				e1.printStackTrace();
			}
		}
		else
		{
			logMessage("file is null", Severity.ERROR);
		}
	}

	/**
	 * Upload patient signature.
	 * 
	 * @param event
	 */
	public void handlePatientSignatureUpload(FileUploadEvent event)
	{
		editConsent.setPatientSignatureBase64(Base64.encodeToString(event.getFile().getContents(), true));
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("new_patient_signature", editConsent.getPatientSignatureBase64());
	}

	/**
	 * Upload physician signature.
	 * 
	 * @param event
	 */
	public void handlePhysicianSignatureUpload(FileUploadEvent event)
	{
		editConsent.setPhysicanSignatureBase64(Base64.encodeToString(event.getFile().getContents(), true));
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("new_physician_signature", editConsent.getPhysicanSignatureBase64());
	}

	/**
	 * Get QR Code with template key and signerIds.
	 * 
	 * @param consent
	 * @return
	 */
	public String getQrCode(WebConsent consent)
	{
		ConsentTemplateKeyDTO key = consent.getKey().getConsentTemplateKey();
		String result = "template=" + key.getDomainName() + ";" + key.getName() + ";" + key.getVersion();

		for (SignerIdDTO id : consent.getKey().getSignerIds())
		{
			if (id.getId() != null)
			{
				result += ("#" + id.getIdType() + "=" + id.getId());
			}
		}
		logger.debug("QR Code:" + result);

		return result;
	}

	/**
	 * Save new consent in database.
	 * 
	 * @throws InconsistentStatusException
	 * @throws VersionConverterClassException
	 * @throws UnknownDomainException
	 */
	public void onSaveConsent()
	{
		try
		{
			editConsent.getKey().setConsentDate(new Date());
			editConsent.setTemplateType(templateType);
			cmManager.addConsent(editConsent.toDTO());
			mode = Mode.LIST;
			this.loadConsents();
			Object[] args = { editConsent.getKey().getConsentTemplateKey().getName(),
					editConsent.getKey().getConsentTemplateKey().getVersion(),
					editConsent.getKey().getConsentTemplateKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("consent.message.info.added." + templateType.toString()))
					.format(args), Severity.INFO);
			mode = Mode.LIST;
		}
		catch (MissingRequiredObjectException | InvalidFreeTextException | MandatoryFieldsException | UnknownDomainException | UnknownSignerIdTypeException | DuplicateEntryException
				| UnknownModuleException | UnknownConsentTemplateException
				| VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/**
	 * Upload scan for consent.
	 * 
	 * @param event
	 */
	public void onUploadScan(FileUploadEvent event)
	{
		String scan = Base64.encodeToString(event.getFile().getContents(), true);
		String fileType = "application/pdf";

		if (mode == Mode.NEW || mode == Mode.INVALIDATION)
		{
			editConsent.setScanBase64(scan);
			editConsent.setScanFileType(fileType);
		}
		else
		{
			try
			{
				cmManager.updateConsent(selectedConsent.getKey(), selectedConsent.getExternProperties(),
						selectedConsent.getComment(), scan, fileType);
				if (StringUtils.isEmpty(selectedConsent.getScanBase64()))
				{
					logMessage(getBundle().getString("consent.message.info.scan.added"), Severity.INFO);
				}
				else
				{
					logMessage(getBundle().getString("consent.message.info.scan.replaced"), Severity.INFO);
				}
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | UnknownConsentException
					| InvalidVersionException | VersionConverterClassException | UnknownSignerIdTypeException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
	}

	/**
	 * Download scan.
	 * 
	 * @param consent
	 */
	public void onDownloadScan(WebConsent consent)
	{
		selectedConsent = consent;
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		session.setAttribute("preview_pdf", selectedConsent.getScanBase64());
		session.setAttribute("preview_name", getFileName(selectedConsent));
		if (StringUtils.isEmpty(consent.getScanBase64()))
		{
			logMessage(getBundle().getString("consent.message.warn.scan.none"), Severity.WARN);
		}
	}

	/**
	 * Delete scan.
	 * 
	 * @param consent
	 */
	public void onDeleteScan(WebConsent consent)
	{
		consent.setScanBase64(null);
	}

	/**
	 * Create new invalidation
	 * 
	 * @param consent
	 */
	public void onNewInvalidation(WebConsent consent)
	{
		editConsent = consent;
		editConsent.setComment("");
		editConsent.setScanBase64(null);
		mode = Mode.INVALIDATION;
	}

	/**
	 * Save the invalidation in database.
	 */
	public void onSaveInvalidation()
	{
		try
		{
			cmManager.invalidateConsent(editConsent.getKey(), invalidationStatus, editConsent.getComment(), editConsent.getScanBase64());
			invalidationStatus = null;
			this.loadConsents();

			Object[] args = { editConsent.getKey().getConsentTemplateKey().getName(), editConsent.getKey().getConsentTemplateKey().getVersion() };
			logMessage(new MessageFormat(getBundle().getString("consent.message.info.invalidated." + editConsent.getTemplateType().toString())).format(args), Severity.INFO);
			mode = Mode.LIST;
		}
		catch (InvalidVersionException | InvalidConsentStatusException | VersionConverterClassException | UnknownConsentException | UnknownSignerIdTypeException | UnknownConsentTemplateException
				| UnknownDomainException | UnknownModuleException | InternalException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/**
	 * @return the possible invalidationStates.
	 */
	public List<ConsentStatus> getInvalidationStates()
	{
		List<ConsentStatus> status = new ArrayList<ConsentStatus>();

		// add all status where type is unkown
		for (ConsentStatus cs : ConsentStatus.getAllConsentStatusForType(ConsentStatusType.DECLINED))
		{
			status.add(cs);
		}

		return status;
	}

	public void onAddSignerId()
	{
		try
		{
			cmManager.addSignerIdToConsent(selectedConsent.getKey(), newSignerId);
			this.loadConsents();
			newSignerId = new SignerIdDTO();
			logMessage(getBundle().getString("consent.message.info.added.signerId"), Severity.INFO);
		}
		catch (IllegalArgumentException | InvalidVersionException | VersionConverterClassException | UnknownConsentException | UnknownSignerIdTypeException | UnknownConsentTemplateException
				| UnknownDomainException | InternalException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			facesContext.renderResponse();
			facesContext.validationFailed();
		}
	}

	/**
	 * Update existing consent (comment, extern properties).
	 */
	public void onUpdateConsent()
	{
		try
		{
			ConsentDTO oldConsent = cmManager.getConsent(selectedConsent.getKey());
			cmManager.updateConsent(selectedConsent.getKey(), selectedConsent.getExternProperties(), selectedConsent.getComment(), selectedConsent.getScanBase64(), selectedConsent.getScanFileType());
			if (!Objects.equals(oldConsent.getComment(), selectedConsent.getComment()))
			{
				if (StringUtils.isEmpty(oldConsent.getComment()))
				{
					logMessage(getBundle().getString("consent.message.info.added.comment"), Severity.INFO);
				}
				else
				{
					logMessage(getBundle().getString("consent.message.info.updated.comment"), Severity.INFO);
				}
			}
			else if (!Objects.equals(oldConsent.getExternProperties(), selectedConsent.getExternProperties()))
			{
				if (StringUtils.isEmpty(oldConsent.getExternProperties()))
				{
					logMessage(getBundle().getString("consent.message.info.added.externProperties"), Severity.INFO);
				}
				else
				{
					logMessage(getBundle().getString("consent.message.info.updated.externProperties"), Severity.INFO);
				}
			}
			else
			{
				logMessage(getBundle().getString("consent.message.info.updated"), Severity.INFO);
			}
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | UnknownConsentException | InvalidVersionException | VersionConverterClassException | UnknownSignerIdTypeException
				| InconsistentStatusException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/**
	 * Show details for a given consent.
	 * 
	 * @param consent
	 * @throws UnknownDomainException
	 * @throws UnknownConsentTemplateException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	public void onShowDetails(WebConsent consent) throws UnknownDomainException, UnknownConsentTemplateException,
			VersionConverterClassException, InvalidVersionException
	{
		selectedConsent = consent;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("patient_signature", selectedConsent.getPatientSignatureBase64());
		session.setAttribute("physician_signature", selectedConsent.getPhysicanSignatureBase64());
		session.setAttribute("preview_pdf", selectedConsent.getScanBase64());
		session.setAttribute("preview_name", getFileName(selectedConsent));
	}

	/**
	 * Print a given consent.
	 * 
	 * @param consent
	 * @return
	 */
	public String onPrintConsent(WebConsent consent)
	{
		selectedConsent = consent;
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("printConsent", consent);
		try
		{
			ConsentTemplateDTO template = cmManager.getConsentTemplate(consent.getKey().getConsentTemplateKey());
			return "/html/internal/consents.xhml?faces-redirect=true&templateType=" + template.getType().name()
					+ "&print=true";
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | VersionConverterClassException
				| InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		return null;
	}

	/**
	 * Load light consents and create lazy data model.
	 */
	private void loadConsents()
	{
		try
		{
			consents = new ArrayList<>();

			// Get consents for given signerId
			if (mode == Mode.SEARCH)
			{
				Set<SignerIdDTO> signerIdSet = new HashSet<>();
				// Search vor signerId Value in all signerIdTypes
				if (StringUtils.isEmpty(searchSignerId.getIdType()))
				{
					for (String type : domainSelector.getSelectedDomain().getSignerIdTypes())
					{
						signerIdSet.add(new SignerIdDTO(type, searchSignerId.getId()));
					}
				}
				// Search only in given signerIdType
				else
				{
					signerIdSet.add(searchSignerId);
				}
				for (ConsentLightDTO consent : cmManager.getAllConsentsForPerson(domainSelector.getSelectedDomainName(), signerIdSet))
				{
					if (consent.getTemplateType() == null || templateType == null || consent.getTemplateType().equals(templateType))
					{
						consents.add(consent);
					}
				}
			}
			// Get all consents
			else if (mode == Mode.LIST)
			{
				for (ConsentLightDTO consent : cmManager.getAllConsentsForDomain(domainSelector.getSelectedDomainName()))
				{
					if (consent.getTemplateType() == null || templateType == null || consent.getTemplateType().equals(templateType))
					{
						consents.add(consent);
					}
				}
			}

			consentsLazyModel = new WebConsentLazyModel(cmManager, consents, domainSelector.getSelectedDomain());
		}
		catch (Exception e)
		{
			logMessage("exception while retrieving all consents for: " + domainSelector.getSelectedDomainName(),
					Severity.ERROR, true, e);
		}
	}

	/**
	 * Load available templates.
	 * 
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	private void loadTemplates()
			throws UnknownDomainException, VersionConverterClassException, InvalidVersionException
	{
		this.templates = new ArrayList<>();

		for (ConsentTemplateDTO template : cmManager.listConsentTemplates(domainSelector.getSelectedDomainName()))
		{
			if (template.getType().equals(templateType))
			{
				templates.add(template);
			}
		}
	}

	/**
	 * Get full template for key from database.
	 * 
	 * @param key
	 * @return
	 */
	private ConsentTemplateDTO getTemplate(ConsentTemplateKeyDTO key)
	{
		try
		{
			return cmManager.getConsentTemplate(key);
		}
		catch (UnknownConsentTemplateException | VersionConverterClassException | InvalidVersionException
				| UnknownDomainException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
			return null;
		}
	}

	/**
	 * Create filename for scan download.
	 * 
	 * @param consent
	 * @return
	 */
	private String getFileName(WebConsent consent)
	{
		StringBuilder sb = new StringBuilder(consent.getKey().getConsentTemplateKey().getName());
		sb.append("_");
		sb.append(consent.getKey().getConsentTemplateKey().getVersion());
		sb.append("_");
		for (SignerIdDTO sid : consent.getKey().getSignerIds())
		{
			if (sid.getId() != null)
			{
				sb.append(sid.getIdType());
				sb.append("-");
				sb.append(sid.getId());
			}
		}
		if (consent.getKey().getConsentDate() != null)
		{
			sb.append("_");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			sb.append(df.format(consent.getKey().getConsentDate()));
		}
		return sb.toString();
	}

	public List<ConsentLightDTO> getConsents()
	{
		return consents;
	}

	public LazyDataModel<WebConsent> getConsentsLazyModel()
	{
		return consentsLazyModel;
	}

	public void setConsentsLazyModel(LazyDataModel<WebConsent> consentsLazyModel)
	{
		this.consentsLazyModel = consentsLazyModel;
	}

	public WebConsent getSelectedConsent()
	{
		return selectedConsent;
	}

	public void setSelectedConsent(WebConsent selectedConsent)
	{
		this.selectedConsent = selectedConsent;
	}

	public ConsentTemplateType getTemplateType()
	{
		return templateType;
	}

	public WebConsent getEditConsent()
	{
		return editConsent;
	}

	public String getMode()
	{
		return mode.toString();
	}

	public List<ConsentTemplateDTO> getTemplates()
	{
		return templates;
	}

	public ConsentStatus getInvalidationStatus()
	{
		return invalidationStatus;
	}

	public void setInvalidationStatus(ConsentStatus invalidationStatus)
	{
		this.invalidationStatus = invalidationStatus;
	}

	public List<WebConsent> getPrintConsents()
	{
		return printConsents;
	}

	public ConsentTemplateDetector getDetector()
	{
		return detector;
	}

	public SignerIdDTO getNewSignerId()
	{
		return newSignerId;
	}

	public void setNewSignerId(SignerIdDTO newSignerId)
	{
		this.newSignerId = newSignerId;
	}

	public SignerIdDTO getSearchSignerId()
	{
		return searchSignerId;
	}

	public enum Mode
	{
		LIST, PARSE, NEW, INVALIDATION, PRINT, SEARCH
	};
}
