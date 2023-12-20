package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentParseResultDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DetectedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.PrintPrefillEntry;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsent;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsentLazyModel;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

/**
 * Backing Bean for Consent Document View
 *
 * @author Arne Blumentritt, Martin Bialke
 */
@ManagedBean(name = "consentController")
@ViewScoped
public class ConsentController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = 9039910194305913160L;

	// Modes
	private ConsentPageMode consentPageMode;

	// List consents
	private int consentCount;
	private LazyDataModel<WebConsent> consentsLazyModel;
	private WebConsent selectedConsent;
	private ConsentTemplateType templateType;
	private List<SelectItem> qcFilterTypes;
	private List<SelectItem> qcSelectionTypes;
	private String qcLocale;

	// Create consent
	private WebConsent editConsent;
	private List<ConsentTemplateDTO> templates = new ArrayList<>();
	private SignerIdDTO newSignerId = new SignerIdDTO();

	// Scan
	private boolean replaceScanCheckbox = false;
	private transient UploadedFile scanFile;

	// Set QC
	private QCDTO newQc;

	// Parse pdf consents
	private transient ConsentContentParser consentParser;

	// Search consents
	private SignerIdDTO searchSignerId = null;

	// Print consents
	private final List<WebConsent> printConsents = new ArrayList<>();

	/**
	 * Initialize controller, check session map if printing is requested.
	 * Called by pre-render-view event listener.
	 *
	 * @param templateType
	 */
	@SuppressWarnings("unchecked")
	public void init(String templateType, String sidKey, String sidValue)
	{
		if (qcLocale == null || !qcLocale.equals(languageBean.getLanguage()))
		{
			qcLocale = languageBean.getLanguage();
			loadQcTypes();
		}

		// skip ajax posts
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}
		consentPageMode = ConsentPageMode.LIST;
		printConsents.clear();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();

		// Search Mode
		if (!StringUtils.isEmpty(sidValue))
		{
			searchSignerId = new SignerIdDTO(sidKey.equals("null") ? null : sidKey, sidValue, null, null);
			consentPageMode = ConsentPageMode.SEARCH;
		}
		// Print Mode (Template)
		else if (sessionMap.containsKey(SessionMapKeys.PRINT_TEMPLATE))
		{
			ConsentTemplateDTO template = getTemplate((ConsentTemplateKeyDTO) sessionMap.get(SessionMapKeys.PRINT_TEMPLATE));
			boolean printOptionQrCode = (boolean) sessionMap.get(SessionMapKeys.PRINT_OPTION_QR_CODE);
			sessionMap.remove(SessionMapKeys.PRINT_TEMPLATE);
			sessionMap.remove(SessionMapKeys.PRINT_OPTION_QR_CODE);

			// Look for signerIds, dates and places to print
			if (sessionMap.containsKey(SessionMapKeys.PRINT_SIGNER_IDS))
			{
				for (PrintPrefillEntry entry : (List<PrintPrefillEntry>) sessionMap.get(SessionMapKeys.PRINT_SIGNER_IDS))
				{
					WebConsent consent = new WebConsent(new ConsentKeyDTO());
					consent.setTemplate(template);
					consent.setDomain(domainSelector.getSelectedDomain());
					consent.getKey().getSignerIds().clear();
					consent.setPrintOptionQrCode(printOptionQrCode);
					for (SignerIdDTO sid : entry.getSignerIdDtos())
					{
						SignerIdDTO id = new SignerIdDTO(sid.getIdType(), sid.getId(), null, null);
						consent.getKey().getSignerIds().add(id);
					}

					consent.setPatientSigningDate(entry.getSignerDate());
					consent.setPhysicianSigningDate(entry.getPhysicianDate());

					if (entry.getPhysicianPlace() != null)
					{
						consent.setPhysicianSigningPlace(entry.getPhysicianPlace());
					}
					if (entry.getSignerPlace() != null)
					{
						consent.setPatientSigningPlace(entry.getSignerPlace());
					}

					printConsents.add(consent);
				}
				sessionMap.remove(SessionMapKeys.PRINT_SIGNER_IDS);
			}
			else
			{
				WebConsent consent = new WebConsent(new ConsentKeyDTO());
				consent.setTemplate(template);
				consent.setDomain(domainSelector.getSelectedDomain());
				consent.setPrintOptionQrCode(printOptionQrCode);
				printConsents.add(consent);
			}

			consentPageMode = ConsentPageMode.PRINT;
		}
		// Print Mode (Consents)
		else if (sessionMap.containsKey(SessionMapKeys.PRINT_CONSENT))
		{
			ConsentKeyDTO key = (ConsentKeyDTO) sessionMap.get(SessionMapKeys.PRINT_CONSENT);
			sessionMap.remove(SessionMapKeys.PRINT_CONSENT);
			WebConsent consent;
			try
			{
				consent = new WebConsent(service.getConsent(key));
				consent.setTemplate(getTemplate(consent.getKey().getConsentTemplateKey()));
				consent.setDomain(domainSelector.getSelectedDomain());

				printConsents.add(consent);
				consentPageMode = ConsentPageMode.PRINT;
			}
			catch (UnknownDomainException | InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownSignerIdTypeException
					| UnknownConsentException | InvalidParameterException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}

		// Read template type
		try
		{
			this.templateType = ConsentTemplateType.valueOf(templateType);
		}
		catch (IllegalArgumentException e)
		{
			if (consentPageMode == ConsentPageMode.SEARCH)
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

		// Replace searchSignerId created by only the string value with a real signerIdDTO object (incl. creationDate)
		try
		{
			if (searchSignerId != null)
			{
				List<ConsentLightDTO> consents = service.getAllConsentsForSignerIds(domainSelector.getSelectedDomainName(), new HashSet<>(Collections.singletonList(searchSignerId)), false);

				if (!consents.isEmpty())
				{
					for (SignerIdDTO signerIdDTO : consents.get(0).getKey().getSignerIds())
					{
						if (signerIdDTO.getIdType().equals(searchSignerId.getIdType())
								&& signerIdDTO.getId().equals(searchSignerId.getId()))
						{
							searchSignerId = signerIdDTO;
						}
					}
				}
			}
		}
		catch (UnknownDomainException | InvalidVersionException | UnknownSignerIdTypeException | InconsistentStatusException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	private void loadQcTypes()
	{
		qcFilterTypes = new ArrayList<>();
		qcSelectionTypes = new ArrayList<>();
		qcSelectionTypes.add(new SelectItem(null, getCommonBundle().getString("ui.select.pleaseSelect"), null, true, false, true));

		SelectItemGroup valid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.valid"));
		valid.setSelectItems(domainSelector.getSelectedDomainQcTypes().entrySet()
				.stream()
				.filter(Entry::getValue)
				.map(e -> new SelectItem(e.getKey(), getQcTypeLabel(e.getKey())))
				.toArray(SelectItem[]::new));
		qcFilterTypes.add(valid);

		SelectItemGroup validSelection = new SelectItemGroup(getBundle().getString("model.consent.qc.status.valid"));
		validSelection.setSelectItems(Arrays.stream(valid.getSelectItems())
				.filter(i -> !QCDTO.AUTO_GENERATED.equals(i.getValue()))
				.toArray(SelectItem[]::new));
		qcSelectionTypes.add(validSelection);

		SelectItemGroup invalid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.invalid"));
		invalid.setSelectItems(domainSelector.getSelectedDomainQcTypes().entrySet()
				.stream()
				.filter(e -> !e.getValue())
				.map(e -> new SelectItem(e.getKey(), getQcTypeLabel(e.getKey())))
				.toArray(SelectItem[]::new));
		qcFilterTypes.add(invalid);

		SelectItemGroup invalidSelection = new SelectItemGroup(getBundle().getString("model.consent.qc.status.invalid"));
		invalidSelection.setSelectItems(Arrays.stream(invalid.getSelectItems())
				.filter(i -> !INVALIDATED.equals(i.getValue()))
				.filter(i -> !QCDTO.AUTO_GENERATED.equals(i.getValue()))
				.toArray(SelectItem[]::new));
		qcSelectionTypes.add(invalidSelection);
	}

	/**
	 * Select template for new consent and init modules, freetexts and signerIds accordingly.
	 */
	public void onSelectTemplate()
	{
		editConsent = new WebConsent(editConsent.getKey());
		editConsent.setTemplate(getTemplate(editConsent.getKey().getConsentTemplateKey()));
		editConsent.setDomain(domainSelector.getSelectedDomain());

		if (!editConsent.getTemplate().getFinalised())
		{
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

			Object[] args = { getBundle().getString("template.type.small." + editConsent.getTemplateType().toString()), getRequestPath(
					(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()) + "/html/internal/admin/templates.xhtml" };
			logMessage(new MessageFormat(getBundle().getString("consent.message.warn.notFinal"))
					.format(args), Severity.WARN);
		}
	}

	/**
	 * Cancel creating a consent and resetting form fields.
	 */
	public void onCancelEdit()
	{
		PrimeFaces.current().resetInputs("main");
		consentPageMode = searchSignerId == null ? ConsentPageMode.LIST : ConsentPageMode.SEARCH;
		templateType = null;
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
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException | UnknownSignerIdException | InconsistentStatusException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		consentPageMode = ConsentPageMode.NEW;
	}

	public void onNewConsentForCurrentSigner(String type)
	{
		editConsent = new WebConsent(new ConsentKeyDTO());
		editConsent.getKey().setSignerIds(new HashSet<>(Collections.singletonList(searchSignerId)));
		templateType = ConsentTemplateType.valueOf(type);

		try
		{
			loadTemplates();
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException | UnknownSignerIdException | InconsistentStatusException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		consentPageMode = ConsentPageMode.NEW;
	}

	/**
	 * Start parsing a new consent.
	 */
	public void onNewParse()
	{
		// Init Consent parser
		consentParser = ConsentContentParser.getInstance();

		if (consentParser == null)
		{
			logMessage(getBundle().getString("consent.parse.openCvNotAvailable"), Severity.ERROR);
		}
		else
		{
			onNewConsent();
			consentPageMode = ConsentPageMode.PARSE;
		}
	}

	/**
	 * Cancel parsing a new consent.
	 */
	public void onCancelParse()
	{
		consentPageMode = ConsentPageMode.LIST;
	}

	/**
	 * Perform parsing.
	 *
	 * @param event
	 */
	public void onParse(FileUploadEvent event)
	{
		logger.debug("Upload of PDF: {}, filesize in bytes: {}", event.getFile().getFileName(), event.getFile().getSize());

		Date startParsing;
		Date endParsing;

		// store file locally
		Path folder = Paths.get("");
		String filename = FilenameUtils.getBaseName(event.getFile().getFileName());
		String extension = FilenameUtils.getExtension(event.getFile().getFileName());

		try
		{
			// check filesize
			long currentFileSize = event.getFile().getSize();
			long allowedFileSize = Long.parseLong(DomainController.parseProperty(getSelectedDomain().getProperties(), DomainProperties.SCANS_SIZE_LIMIT));
			logger.debug("filesize allowed {}", allowedFileSize);
			logger.debug("filesize current {}", currentFileSize);

			if (currentFileSize > allowedFileSize)
			{
				logMessage(getBundle().getString("consent.message.warn.scan.size"), Severity.WARN);
				return;
			}

			Path file = Files.createTempFile(folder, filename + "-", "." + extension);
			try (InputStream input = event.getFile().getInputStream())
			{
				Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
			}

			/*
			 * Start PDF Parsing
			 */
			startParsing = new Date();
			ConsentParseResultDTO result = consentParser.decodePDF(file.toAbsolutePath().toString(), true, service);
			endParsing = new Date();
			long diffInSeconds = (endParsing.getTime() - startParsing.getTime()) / 1000;
			logger.debug("Parsing Duration in seconds: " + diffInSeconds);

			if (result != null)
			{
				ConsentTemplateKeyDTO templateKey = result.getDetectedTemplateKey();
				if (templateKey != null)
				{
					/*
					 * Process Parsing Result
					 */
					processParseResult(event.getFile(), result, templateKey);
					consentPageMode = ConsentPageMode.NEW;
					logMessage(getBundle().getString("consent.message.info.qr.success"), Severity.INFO);
				}
				else
				{
					logMessage(getBundle().getString("consent.message.warn.qr.notFound"), Severity.WARN);
				}
				if (result.getScalingError())
				{
					logMessage(getBundle().getString("consent.message.warn.qr.tooSmall"), Severity.WARN);
				}
			}
		}
		catch (IOException | UnknownConsentTemplateException | IllegalArgumentException e1)
		{
			logMessage(getBundle().getString("consent.message.error.qr.invalid"), Severity.ERROR);
			logger.error(e1.getLocalizedMessage());
		}
		consentParser.setProgress(0);
	}

	private void processParseResult(UploadedFile scanFile, ConsentParseResultDTO result, ConsentTemplateKeyDTO templateKey) throws UnknownConsentTemplateException
	{
		// force domain selection based on template
		domainSelector.setSelectedDomain(templateKey.getDomainName());

		// preselect template
		setDetectedTemplate(templateKey);

		// invoke update template selection
		onSelectTemplate();

		// append scan to current consent
		setPdfAsScan(scanFile.getContent(), scanFile.getFileName(), new Date());

		// prefill signerids to consent
		if (result.getDetectedSignerIds() != null)
		{
			setDetectedSignerIds(result.getDetectedSignerIds());
		}

		// process detected modules and states
		if (result.getDetectedModuleStates() != null)
		{
			setDetectedModuleStates(result.getDetectedModuleStates());

		}

		markMissingModules(result.getMissingModules());

		// process patient data
		if (result.getDetectedPatientSigningPlace() != null && !result.getDetectedPatientSigningPlace().isEmpty())
		{
			setDetectedPatientSigningPlace(result.getDetectedPatientSigningPlace());
		}
		if (result.getDetectedPatientSigningDate() != null)
		{
			setDetectedPatientSigningDate(result.getDetectedPatientSigningDate());
		}
		// process physician data
		if (result.getDetectedPhysicianSigningPlace() != null && !result.getDetectedPhysicianSigningPlace().isEmpty())
		{
			setDetectedPhysicianSigningPlace(result.getDetectedPhysicianSigningPlace());
		}
		if (result.getDetectedPhysicianSigningDate() != null)
		{
			setDetectedPhysicianSigningDate(result.getDetectedPhysicianSigningDate());
		}

	}

	private void setDetectedPhysicianSigningDate(Date date)
	{
		editConsent.setPhysicianSigningDate(date);
	}

	private void setDetectedPhysicianSigningPlace(String place)
	{
		editConsent.setPhysicianSigningPlace(place);
	}

	private void setDetectedPatientSigningDate(Date date)
	{
		editConsent.setPatientSigningDate(date);
	}

	private void setDetectedPatientSigningPlace(String place)
	{
		editConsent.setPatientSigningPlace(place);
	}

	/**
	 * notify missing modules
	 *
	 * @param missingModules
	 */
	private void markMissingModules(List<AssignedModuleDTO> missingModules)
	{
		if (missingModules != null && !missingModules.isEmpty())
		{
			logger.debug("the following module could not be detected");

			for (AssignedModuleDTO m : missingModules)
			{
				logger.debug(m.toString());
			}

			// notify frontend
			Object[] args = { missingModules.size() };

			// wird in einigen fällen nicht im frontend angezeigt. dubios
			logMessage(new MessageFormat(getBundle().getString("consent.message.warn.qr.notAllModulesFound"))
					.format(args), Severity.INFO);
		}
	}

	/**
	 * update of the shown module states with regards to detected modules
	 *
	 * @param detectedModuleStates
	 */
	private void setDetectedModuleStates(List<DetectedModuleDTO> detectedModuleStates)
	{
		if (detectedModuleStates != null)
		{
			Map<ModuleKeyDTO, ModuleStateDTO> workingList = new HashMap<>();

			for (DetectedModuleDTO m : detectedModuleStates)
			{
				logger.info(m.toString());

				// logmessage args
				Object[] args = { m.getKey().getName() };

				switch (m.getParseResult())
				{
					case OK:
						if (m.getConsentStatus().size() == 1)
						{
							workingList.put(
									m.getKey(),
									new ModuleStateDTO(
											m.getKey(),
											m.getConsentStatus().get(0),
											getPolicyKeysFromModule(editConsent.getTemplate(), m.getKey())));
						}
						break;
					case REVISE_EMPTY:

						logMessage(new MessageFormat(getBundle().getString("consent.message.warn.qr.revise.empty"))
								.format(args), Severity.WARN);
						break;

					case REVISE_TOO_MANY:
						logMessage(new MessageFormat(getBundle().getString("consent.message.warn.qr.revise.many"))
								.format(args), Severity.WARN);
						break;
					default:
						break;
				}
			}

			editConsent.setModuleStates(workingList);
		}
	}

	/**
	 * retrieve assigend policy-keys based on templateDTO and contained moduleName
	 *
	 * @param template
	 *            template retrieve policy from
	 * @param module
	 *            name of template which should be part assigned to template
	 * @return retrieved list of policykeys
	 */
	private List<PolicyKeyDTO> getPolicyKeysFromModule(ConsentTemplateDTO template, ModuleKeyDTO module)
	{
		List<PolicyKeyDTO> pols = new ArrayList<>();

		if (template != null && module != null)
		{
			for (AssignedModuleDTO currentAsm : template.getAssignedModules())
			{
				if (currentAsm.getModule().getKey().equals(module))
				{
					for (AssignedPolicyDTO ap : currentAsm.getModule().getAssignedPolicies())
					{
						pols.add(ap.getPolicy().getKey());
					}
				}
			}
		}

		return pols;
	}

	private void setDetectedSignerIds(List<SignerIdDTO> toBeUsedSids)
	{
		if (toBeUsedSids != null && !toBeUsedSids.isEmpty())
		{
			editConsent.getKey().setSignerIds(new HashSet<>(toBeUsedSids));
		}
	}

	private void setPdfAsScan(byte[] pdfbytes, String fileName, Date uploadDate)
	{
		// append scan to edit consent
		String scan = Base64.getEncoder().encodeToString(pdfbytes);
		editConsent.getScans().add(new ConsentScanDTO(UUID.randomUUID().toString(), editConsent.getKey(), scan, "application/pdf", fileName, uploadDate));
		logger.debug("Uploaded scan already appended to current consent. Please fill in remaining fields.");
	}

	private void setDetectedTemplate(ConsentTemplateKeyDTO consentTemplateKeyDTO) throws UnknownConsentTemplateException
	{
		// check if template exists
		ConsentTemplateDTO template = getTemplate(consentTemplateKeyDTO);
		if (template == null)
		{
			throw new UnknownConsentTemplateException();
		}
		logger.info("Successful parsed consent template: [DOMAIN="
				+ consentTemplateKeyDTO.getDomainName()
				+ "][NAME=" + consentTemplateKeyDTO.getName()
				+ "][Version=" + template.getVersionLabelAndVersion() + "]");

		editConsent.setTemplate(template);
	}

	/**
	 * Get QR Code with template key and signerIds.
	 *
	 * @param consent
	 * @return
	 */
	public String getQrCode(WebConsent consent)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String qrCodeContent = "";
		String sep = "#";
		ConsentTemplateKeyDTO key = consent.getKey().getConsentTemplateKey();
		String template = "template=" + key.getDomainName() + ";" + key.getName() + ";" + key.getVersion();

		String patientDate = "";
		if (consent.getPatientSigningDate() != null)
		{
			patientDate = sep + SessionMapKeys.PRINT_SIGNER_DATE + "=" + df.format(consent.getPatientSigningDate());
		}

		String patientPlace = "";
		if (consent.getPatientSigningPlace() != null && !consent.getPatientSigningPlace().isEmpty())
		{
			patientPlace = sep + SessionMapKeys.PRINT_SIGNER_PLACE + "=" + consent.getPatientSigningPlace();
		}

		String physicianDate = "";
		if (consent.getPhysicianSigningDate() != null)
		{
			physicianDate = sep + SessionMapKeys.PRINT_PHYSICIAN_DATE + "=" + df.format(consent.getPhysicianSigningDate());
		}

		String physicianPlace = "";
		if (consent.getPhysicianSigningPlace() != null && !consent.getPhysicianSigningPlace().isEmpty())
		{
			physicianPlace = sep + SessionMapKeys.PRINT_PHYSICIAN_PLACE + "=" + consent.getPhysicianSigningPlace();
		}

		qrCodeContent = template
				+ patientDate
				+ patientPlace
				+ physicianDate
				+ physicianPlace;

		for (SignerIdDTO id : consent.getKey().getSignerIds())
		{
			if (id.getId() != null)
			{
				qrCodeContent += sep + id.getIdType() + "=" + id.getId();
			}
		}

		return qrCodeContent;
	}

	public String moduleMissing(ModuleDTO module)
	{
		if (!StringUtils.isEmpty(module.getTitle()))
		{
			return sanitize(module.getTitle());
		}
		else
		{
			return sanitize(module.getLabel());
		}
	}

	/**
	 * Save new consent in database.
	 */
	public void onSaveConsent()
	{
		if (editConsent.getKey().getSignerIds().stream().filter(sid -> StringUtils.isNotEmpty(sid.getId())).findAny().isEmpty())
		{
			logMessage(getBundle().getString("consent.message.warn.noSignerId"), Severity.ERROR, Severity.WARN);
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}

		try
		{
			editConsent.getKey().setConsentDate(new Date());
			editConsent.setTemplateType(templateType);

			// Primefaces adds metadate before base64 string
			editConsent.setPatientSignatureBase64(editConsent.getPatientSignature().getBase64());
			editConsent.setPhysicianSignatureBase64(editConsent.getPhysicianSignature().getBase64());

			if (isUsingNotifications())
			{
				serviceWithNotification.addConsent(NOTIFICATION_CLIENT_ID, editConsent.toDTO());
			}
			else
			{
				service.addConsent(editConsent.toDTO());
			}

			ConsentTemplateDTO template = Objects.requireNonNull(getTemplate(editConsent.getKey().getConsentTemplateKey()));
			Object[] args = { template.getLabel(), template.getVersionLabelAndVersion(),
					editConsent.getKey().getConsentTemplateKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("consent.message.info.added." + templateType.toString()))
					.format(args), Severity.INFO);
			
			consentPageMode = searchSignerId == null ? ConsentPageMode.LIST : ConsentPageMode.SEARCH;
			templateType = null;
			loadConsents();
			selectedConsent = editConsent;
		}
		catch (MissingRequiredObjectException | InvalidFreeTextException | MandatoryFieldsException | UnknownDomainException | UnknownSignerIdTypeException | DuplicateEntryException
				| UnknownModuleException | UnknownConsentTemplateException
				| InvalidVersionException | RequirementsNotFullfilledException | InvalidParameterException e)
		{
			if (e.getLocalizedMessage().contains("if at least one mandatory module have a consent status of"))
			{
				logMessage(getBundle().getString("consent.message.warn.mandatoryDeclinedOptionalAccepted"), Severity.WARN);
			}
			else
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
	}

	/**
	 * Adds the given scan to the editConsent object (consent and scans are not yet persisted to database)
	 *
	 * @param event
	 *            FileUploadEvent with new scan
	 */
	public void onAddScan(FileUploadEvent event)
	{
		ConsentScanDTO scanDTO = new ConsentScanDTO(UUID.randomUUID().toString(), editConsent.getKey(),
				Base64.getEncoder().encodeToString(event.getFile().getContent()),
				event.getFile().getContentType(), event.getFile().getFileName(), new Date());
		editConsent.getScans().add(scanDTO);
	}

	/**
	 * Removes the given scan from the editConsent object (consent and scans are not yet persisted to database)
	 *
	 * @param scan
	 *            scan to be removed
	 */
	public void onRemoveScan(ConsentScanDTO scan)
	{
		editConsent.getScans().remove(scan);
	}

	private void updateConsentInUse(ConsentScanDTO scanDTO) throws InvalidVersionException, UnknownDomainException,
			UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		if (isUsingNotifications())
		{
			serviceWithNotification.updateConsentInUse(NOTIFICATION_CLIENT_ID, selectedConsent.getKey(), selectedConsent.getExternProperties(), selectedConsent.getComment(), scanDTO);
		}
		else
		{
			service.updateConsentInUse(selectedConsent.getKey(), selectedConsent.getExternProperties(), selectedConsent.getComment(), scanDTO);
		}
	}

	/**
	 * Saves the given scan for the selected consent in the database
	 *
	 * @param event
	 *            FileUploadEvent with new scan
	 */
	public void onSaveScanForSelectedConsent(FileUploadEvent event)
	{
		ConsentScanDTO scanDTO = new ConsentScanDTO(selectedConsent.getKey(), Base64.getEncoder().encodeToString(event.getFile().getContent()),
				event.getFile().getContentType(), event.getFile().getFileName(), new Date());

		try
		{
			updateConsentInUse(scanDTO);
			selectedConsent.setScans(service.getConsent(selectedConsent.getKey()).getScans());
		}
		catch (InvalidVersionException | UnknownDomainException | UnknownConsentTemplateException | InvalidParameterException | UnknownConsentException
				| UnknownSignerIdTypeException | InconsistentStatusException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/**
	 * Deletes the selected scan from the given consent in the database.
	 *
	 * @param consent
	 *            consent from whom the selected scan should be removed
	 */
	public void onDeleteSelectedScan(WebConsent consent)
	{
		try
		{
			service.removeScanFromConsent(consent.getKey(), consent.getSelectedScan());
			consent.setScans(service.getConsent(consent.getKey()).getScans());
			consent.setDeleteConfirmation(false);
		}
		catch (UnknownSignerIdTypeException | InvalidParameterException | InvalidVersionException | UnknownConsentTemplateException | UnknownDomainException
				| DuplicateEntryException | UnknownConsentException | InconsistentStatusException e)
		{
			logger.error(e.getLocalizedMessage());
		}
	}

	/**
	 * Download scan.
	 *
	 * @param consent
	 */
	public void onDownloadScan(WebConsent consent, ConsentScanDTO scan)
	{
		selectedConsent = consent;
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		session.setAttribute("preview_pdf", scan.getBase64());
		session.setAttribute("preview_name", getFileName(consent, scan));
	}

	public void onAddSignerId()
	{
		try
		{
			service.addSignerIdToConsent(selectedConsent.getKey(), newSignerId);
			loadConsents();
			newSignerId = new SignerIdDTO();
			logMessage(getBundle().getString("consent.message.info.added.signerId"), Severity.INFO);
		}
		catch (IllegalArgumentException | InvalidVersionException | UnknownConsentException | UnknownSignerIdTypeException | UnknownConsentTemplateException
				| UnknownDomainException | InternalException | InvalidParameterException e)
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
			ConsentDTO oldConsent = service.getConsent(selectedConsent.getKey());
			updateConsentInUse(null);
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
		catch (UnknownDomainException | UnknownConsentTemplateException | UnknownConsentException | InvalidVersionException | UnknownSignerIdTypeException
				| InconsistentStatusException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
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
		sessionMap.put(SessionMapKeys.PRINT_CONSENT, consent.getKey());
		try
		{
			ConsentTemplateDTO template = service.getConsentTemplate(consent.getKey().getConsentTemplateKey());
			return "/html/internal/consents.xhtml?faces-redirect=true&templateType=" + template.getType().name()
					+ "&print=true";
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		return null;
	}

	public List<SelectItem> getQcSelectionTypes()
	{
		return qcSelectionTypes;
	}

	public List<SelectItem> getQcFilterTypes()
	{
		return qcFilterTypes;
	}

	public void onNewQc()
	{
		newQc = new QCDTO();
		newQc.setComment(null);
		newQc.setInspector(null);
		newQc.setType(null);
		String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		if (!StringUtils.isEmpty(user))
		{
			newQc.setInspector(user);
		}
	}

	public void onNewInvalidation()
	{
		onNewQc();
		newQc.setType(INVALIDATED);
		if (!domainSelector.getSelectedDomainQcTypes().containsKey(INVALIDATED))
		{
			logMessage("Please add type 'invalidated' to the list of INVALID_QC_TYPES in your domain properties", Severity.ERROR);
		}
	}

	public void onSaveQc() throws UnknownConsentException, UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException, UnknownConsentTemplateException, InvalidParameterException
	{
		ConsentTemplateDTO template = Objects.requireNonNull(getTemplate(selectedConsent.getKey().getConsentTemplateKey()));
		Object[] args = { getBundle().getString("template.type." + selectedConsent.getTemplateType()),
				selectedConsent.getKey().getConsentTemplateKey().getName(),
				template.getVersionLabelAndVersion() };

		if (isUsingNotifications())
		{
			serviceWithNotification.setQCForConsent(NOTIFICATION_CLIENT_ID, selectedConsent.getKey(), newQc);
		}
		else
		{
			service.setQCForConsent(selectedConsent.getKey(), newQc);
		}

		if (INVALIDATED.equals(newQc.getType()))
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.qc.message.info.invalidated")).format(args), Severity.INFO);
		}
		else
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.qc.message.info.saved")).format(args), Severity.INFO);
		}
	}

	public Boolean isDigitalSignature(String signatureBase64)
	{
		return !(StringUtils.isEmpty(signatureBase64) || ConsentLightDTO.NO_SIGNATURE.equals(signatureBase64) || ConsentLightDTO.NO_REAL_SIGNATURE.equals(signatureBase64));
	}

	/**
	 * Load light consents and create lazy data model.
	 */
	private void loadConsents()
	{
		try
		{
			// Prepare paginationConfig
			PaginationConfig paginationConfig = new PaginationConfig();
			paginationConfig.setFilterIsCaseSensitive(false);
			paginationConfig.setFilterFieldsAreTreatedAsConjunction(false);
			paginationConfig.setTemplateType(templateType);
			Map<ConsentField, String> filter = new HashMap<>();
			if (searchSignerId != null)
			{
				filter.put(ConsentField.SIGNER_ID, searchSignerId.getId());
				paginationConfig.setFilterFieldsAreTreatedAsConjunction(true);
			}
			paginationConfig.setFilter(filter);

			// Count
			consentCount = (int) service.countConsentsForDomainWithFilter(domainSelector.getSelectedDomainName(), paginationConfig);

			// Init lazy model
			consentsLazyModel = new WebConsentLazyModel(service, domainSelector.getSelectedDomain(), paginationConfig, searchSignerId != null);
		}
		catch (Exception e)
		{
			logMessage(e);
		}
	}

	/**
	 * Load available templates.
	 *
	 * @throws UnknownDomainException
	 * @throws InvalidVersionException
	 * @throws InvalidParameterException
	 */
	private void loadTemplates() throws UnknownDomainException, InconsistentStatusException, InvalidVersionException, InvalidParameterException, UnknownSignerIdException, UnknownSignerIdTypeException
	{
		templates.clear();
		if (editConsent != null && !editConsent.getKey().getSignerIds().isEmpty())
		{
			templates = service.getMappedTemplatesForSignerId(domainSelector.getSelectedDomainName(), templateType, editConsent.getKey().getOrderedSignerIds().get(0), true).stream().map(t -> {
				try
				{
					return service.getConsentTemplate(t);
				}
				catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
				{
					logger.error(e.getLocalizedMessage());
					return null;
				}
			}).collect(Collectors.toList());
		}
		if (templates.isEmpty())
		{
			templates = service.listConsentTemplates(domainSelector.getSelectedDomainName(), false).stream()
					.filter(t -> t.getType().equals(templateType))
					.sorted(Comparator.comparing(t -> t.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList());
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
			return service.getConsentTemplate(key);
		}
		catch (UnknownConsentTemplateException | InvalidVersionException | UnknownDomainException | InvalidParameterException e)
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
	private String getFileName(WebConsent consent, ConsentScanDTO scan)
	{
		ConsentTemplateDTO template = Objects.requireNonNull(getTemplate(consent.getKey().getConsentTemplateKey()));
		StringBuilder sb = new StringBuilder(template.getLabel());
		sb.append("_");
		sb.append(template.getVersionLabelAndVersion().replace(' ', '_'));
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
			DateFormat df = new SimpleDateFormat(getCommonBundle().getString("ui.date.pattern.datetime"));
			sb.append(df.format(consent.getKey().getConsentDate()));
		}
		return sb.toString();
	}

	public Integer getConsentCount()
	{
		return consentCount;
	}

	/**
	 * Returns a nifty string with the scan's <code>uploadDate</code>.
	 *
	 * @param scan
	 *            the scan to return a title for
	 * @return a nifty string with the scan's <code>uploadDate</code>.
	 */
	public String getConsentScanTitle(ConsentScanDTO scan)
	{
		Date uploadDate = scan.getUploadDate();
		if (uploadDate != null)
		{
			return dateToString(uploadDate, "datetime");
		}
		return "";
	}

	/**
	 * Returns a nifty string with the scan's <code>fileName</code> and <code>uploadDate</code> e.g. for tooltips.
	 *
	 * @param scan
	 *            the scan to return a title tip for
	 * @return a nifty string with the scan's <code>fileName</code> and <code>uploadDate</code> e.g. for tooltips.
	 */
	public String getConsentScanTitleTip(ConsentScanDTO scan)
	{
		String fileName = scan.getFileName();
		String title = "";

		if (fileName != null)
		{
			title = fileName;
		}

		if (title.isEmpty())
		{
			title = getConsentScanTitle(scan);
		}
		else
		{
			title += " (" + getConsentScanTitle(scan) + ")";
		}

		return title;
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

	public void onRowSelect(SelectEvent<WebConsent> event)
	{
		setSelectedConsent(event.getObject());
	}

	public void setSelectedConsent(WebConsent selectedConsent)
	{
		if (selectedConsent != null)
		{
			this.selectedConsent = selectedConsent;
			replaceScanCheckbox = false;
			scanFile = null;
		}
	}

	public ConsentTemplateType getTemplateType()
	{
		return templateType;
	}

	public WebConsent getEditConsent()
	{
		return editConsent;
	}

	public List<ConsentTemplateDTO> getTemplates()
	{
		return templates;
	}

	public boolean isReplaceScanCheckbox()
	{
		return replaceScanCheckbox;
	}

	public void setReplaceScanCheckbox(boolean replaceScanCheckbox)
	{
		this.replaceScanCheckbox = replaceScanCheckbox;
	}

	public UploadedFile getScanFile()
	{
		return scanFile;
	}

	public void setScanFile(UploadedFile scanFile)
	{
		this.scanFile = scanFile;
	}

	public List<WebConsent> getPrintConsents()
	{
		return printConsents;
	}

	/**
	 * Get filename for pdf print in this format: 2020-01-23 12:00:00 SID 1234 Domäne Template
	 * Version
	 *
	 * @return filename
	 */
	public String getPrintFileName()
	{
		String result = "";
		WebConsent c = printConsents.get(0);

		if (printConsents.size() == 1 && c.getKey().getConsentDate() != null)
		{
			// Consent Timestamp
			SimpleDateFormat sdf = new SimpleDateFormat(getCommonBundle("en").getString("ui.date.pattern.datetime"));
			result += sdf.format(c.getKey().getConsentDate()) + " ";

			// SignerIds
			if (c.getKey().getSignerIds() != null && !c.getKey().getSignerIds().isEmpty())
			{
				result += c.getSignerIdsAsString() + " ";
			}
		}

		// Domain + Name + Version (Version Label)
		result += c.getTemplate().getKey().getDomainName() + " ";
		result += c.getTemplate().getKey().getName() + " ";
		result += c.getTemplate().getVersionLabelAndVersion() + " ";

		return result;
	}

	public ConsentContentParser getDetector()
	{
		return consentParser;
	}

	public SignerIdDTO getNewSignerId()
	{
		return newSignerId;
	}

	public void setNewSignerId(SignerIdDTO newSignerId)
	{
		this.newSignerId = newSignerId;
	}

	public QCDTO getNewQc()
	{
		return newQc;
	}

	public void setNewQc(QCDTO newQc)
	{
		this.newQc = newQc;
	}

	public SignerIdDTO getSearchSignerId()
	{
		return searchSignerId;
	}

	public ConsentPageMode getConsentPageMode()
	{
		return consentPageMode;
	}

	public enum ConsentPageMode
	{
		LIST, PARSE, NEW, PRINT, SEARCH
	}

	public boolean isQcEnabled()
	{
		return !domainSelector.getSelectedDomainQcTypes().entrySet().isEmpty();
	}
}
