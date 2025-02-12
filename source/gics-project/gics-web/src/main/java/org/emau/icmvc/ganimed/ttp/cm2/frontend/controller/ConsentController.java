package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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
import java.io.InputStream;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.ConsentField;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatusType;
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
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.EmbeddedController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.PrintPrefillEntry;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsent;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsentLazyModel;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.emau.icmvc.ganimed.ttp.cm2.util.ModuleQRCodec;
import org.icmvc.ttp.web.controller.UserSettingsBean;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

/**
 * Backing Bean for Consent Document View
 *
 * @author Arne Blumentritt, Martin Bialke
 */
@ViewScoped
@Named("consentController")
public class ConsentController extends AbstractConsentController
{
	@Serial
	private static final long serialVersionUID = 9039910194305913160L;

	// Modes
	private ConsentPageMode consentPageMode;

	// List consents
	private int consentCount;
	private LazyDataModel<WebConsent> consentsLazyModel;
	private WebConsent selectedConsent;
	private ConsentTemplateType templateType;
	private List<SelectItem> qcFilterTypes;
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
	private boolean showQcHistory = false;
	@Inject
	@ManagedProperty(value = "#{qcController}")
	QCController qcController;
	@Inject
	@ManagedProperty(value = "#{embeddedController}")
	EmbeddedController embeddedController;
	@Inject
	@ManagedProperty(value = "#{userSettingsBean}")
	UserSettingsBean userSettingsBean;

	// Parse pdf consents
	private transient ConsentContentParser consentParser;

	// Search consents
	private SignerIdDTO searchSignerId = null;

	// Print consents
	private final List<WebConsent> printConsents = new ArrayList<>();
	private DefaultStreamedContent downloadFile;
	
	@PostConstruct
	private void initColumns()
	{
		userSettingsBean.setDefaultColumns("consents", new HashSet<>(Set.of(1, 4, 5, 6, 7)));
	}
	
	public void init()
	{
		init(false);
	}

	private ConsentTemplateType getTemplateType(String t)
	{
		return t == null || t.equals("null") ? null : ConsentTemplateType.valueOf(t);
	}

	protected WebConsent prefillConsent(Map<String, String[]> parameters)
	{
		WebConsent consent = new WebConsent(new ConsentKeyDTO());
		ConsentTemplateKeyDTO templateKey = new ConsentTemplateKeyDTO(getParameter(parameters, "domain"), getParameter(parameters, "name"), getParameter(parameters, "version"));

		// Check if all required parameters are present
		if (StringUtils.isEmpty(templateKey.getDomainName()))
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.missingDomain"))
					.format(new Object[] { templateKey.getDomainName() }), Severity.ERROR);
			return null;
		}
		else if (StringUtils.isNotEmpty(templateKey.getVersion()) && StringUtils.isEmpty(templateKey.getName()))
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.missingTemplateName"))
					.format(new Object[] { templateKey.getDomainName() }), Severity.ERROR);
			return null;
		}
		else if (StringUtils.isNotEmpty(templateKey.getName()) && StringUtils.isEmpty(templateKey.getVersion()))
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.missingTemplateVersion"))
					.format(new Object[] { templateKey.getDomainName() }), Severity.ERROR);
			return null;
		}

		// Check domain
		try
		{
			DomainDTO domain = getService().getDomain(templateKey.getDomainName());
			domainSelector.setSelectedDomain(domain.getName());
			consent.setDomainAndInitSignerIds(domain);
		}
		catch (InvalidParameterException e)
		{
			logger.error(e.getLocalizedMessage());
			return null;
		}
		catch (UnknownDomainException e)
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.unknownDomain"))
					.format(new Object[] { templateKey.getDomainName() }), Severity.ERROR);
			return null;
		}

		// Check template
		ConsentTemplateDTO template;
		try
		{
			if (StringUtils.isNotEmpty(templateKey.getName()) && StringUtils.isNotEmpty(templateKey.getVersion()))
			{
				template = getService().getConsentTemplate(templateKey);
				consent.setTemplate(template);
				templateType = template.getType();
				logger.debug("Loaded template {}.", templateKey);
				embeddedController.setPrefilledTemplate(true);
			}
			else
			{
				this.templateType = getParameter(parameters, "type") == null ? ConsentTemplateType.CONSENT : getTemplateType(getParameter(parameters, "type"));
				loadTemplates();
				logger.debug("No template loaded. Enabling user template selection of type {}.", this.templateType);
			}
		}
		catch (UnknownConsentTemplateException e)
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.unknownConsentTemplate"))
					.format(new Object[] { templateKey }), Severity.ERROR);
			return null;
		}
		catch (InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
			return null;
		}
		catch (InvalidVersionException e)
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.prefill.message.invalidVersion"))
					.format(new Object[] { templateKey.getVersion() }), Severity.ERROR);
			return null;
		}
		catch (UnknownDomainException | UnknownSignerIdTypeException | UnknownSignerIdException | InconsistentStatusException e)
		{
			logger.error(e.getLocalizedMessage());
			return null;
		}

		// Fill signerIds if present
		consent.getKey().getSignerIds().forEach(s -> {
			String id = getParameter(parameters, s.getIdType());
			s.setId(id);
			if (StringUtils.isNotEmpty(id))
			{
				s.setId(id);
				embeddedController.setPrefilledSignerId(true);
			}
		});

		// Fill signature information if present
		Date now = new Date();
		String patientSignaturePlace = getParameter(parameters, "patientSignaturePlace");
		String physicianSignaturePlace = getParameter(parameters, "physicianSignaturePlace");
		String sharedSignaturePlace = getParameter(parameters, "signaturePlace");
		consent.setPatientSigningPlace(StringUtils.isNotEmpty(patientSignaturePlace) ? patientSignaturePlace : sharedSignaturePlace);
		consent.setPatientSigningDate(now);
		consent.setPhysicianSigningPlace(StringUtils.isNotEmpty(physicianSignaturePlace) ? physicianSignaturePlace : sharedSignaturePlace);
		consent.setPhysicianSigningDate(now);
		consent.setPhysicianId(getParameter(parameters, "physicianId"));

		return consent;
	}

	@SuppressWarnings("unchecked")
	public void init(boolean app)
	{
		// skip ajax posts
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}

		consentPageMode = ConsentPageMode.LIST;
		printConsents.clear();

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		Map<String, String[]> parameters = ((HttpServletRequest) externalContext.getRequest()).getParameterMap();
		templateType = getTemplateType(getParameter(parameters, "templateType"));
		String sidKey = getParameter(parameters, "sidKey");
		String sidValue = getParameter(parameters, "sidValue");

		if (qcLocale == null || !qcLocale.equals(languageBean.getLanguage()))
		{
			qcLocale = languageBean.getLanguage();
			loadQcTypes();
		}

		if (app)
		{
			// Prepare consent
			editConsent = prefillConsent(parameters);
			consentPageMode = ConsentPageMode.NEW;
		}
		else
		{
			// Search Mode
			if (!StringUtils.isEmpty(sidValue))
			{
				searchSignerId = new SignerIdDTO(sidKey.equals("null") ? null : sidKey, sidValue, null, null);
				consentPageMode = ConsentPageMode.SEARCH;
			}
			// Print Mode (Template)
			else if (sessionMap.containsKey(SessionMapKeys.PRINT_TEMPLATES))
			{
				List<ConsentTemplateDTO> printTemplates = ((List<ConsentTemplateKeyDTO>) sessionMap.get(SessionMapKeys.PRINT_TEMPLATES)).stream().map(this::getTemplate).toList();
				boolean printOptionQrCode = (boolean) sessionMap.get(SessionMapKeys.PRINT_OPTION_QR_CODE);
				sessionMap.remove(SessionMapKeys.PRINT_TEMPLATES);
				sessionMap.remove(SessionMapKeys.PRINT_OPTION_QR_CODE);

				for (ConsentTemplateDTO template : printTemplates)
				{
					// Look for signerIds, dates and places to print
					if (sessionMap.containsKey(SessionMapKeys.PRINT_SIGNER_IDS))
					{
						for (PrintPrefillEntry entry : (List<PrintPrefillEntry>) sessionMap.get(SessionMapKeys.PRINT_SIGNER_IDS))
						{
							WebConsent consent = new WebConsent(new ConsentKeyDTO());
							consent.setTemplate(template);
							consent.setDomainAndInitSignerIds(domainSelector.getSelectedDomain());
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
						consent.setDomainAndInitSignerIds(domainSelector.getSelectedDomain());
						consent.setPrintOptionQrCode(printOptionQrCode);
						printConsents.add(consent);
					}
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
					consent = new WebConsent(getService().getConsent(key));
					consent.setTemplate(getTemplate(consent.getKey().getConsentTemplateKey()));
					consent.setDomainAndInitSignerIds(domainSelector.getSelectedDomain());

					printConsents.add(consent);
					consentPageMode = ConsentPageMode.PRINT;
				}
				catch (UnknownDomainException | InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownSignerIdTypeException
					   | UnknownConsentException | InvalidParameterException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
			}
			// Fill Mode (Template)
			else if (sessionMap.containsKey(SessionMapKeys.FILL_TEMPLATE))
			{
				ConsentTemplateDTO template = getTemplate((ConsentTemplateKeyDTO) sessionMap.get(SessionMapKeys.FILL_TEMPLATE));
				sessionMap.remove(SessionMapKeys.FILL_TEMPLATE);
				this.templateType = template.getType();

				onNewConsent();
				editConsent.getKey().setConsentTemplateKey(template.getKey());
				onSelectTemplate();
			}

			// Load list of consents
			loadConsents();

			// Replace searchSignerId created by only the string value with a real signerIdDTO object (incl. creationDate)
			try
			{
				if (searchSignerId != null)
				{
					List<ConsentLightDTO> consents = getService().getAllConsentsForSignerIds(domainSelector.getSelectedDomainName(), new HashSet<>(Collections.singletonList(searchSignerId)), false);

					if (!consents.isEmpty())
					{
						for (SignerIdDTO signerIdDTO : consents.getFirst().getKey().getSignerIds())
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
	}

	private void loadQcTypes()
	{
		qcFilterTypes = new ArrayList<>();

		SelectItemGroup valid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.valid"));
		valid.setSelectItems(domainSelector.getSelectedDomainQcTypes().entrySet()
				.stream()
				.filter(Entry::getValue)
				.map(e -> new SelectItem(e.getKey(), getQcTypeLabel(e.getKey())))
				.toArray(SelectItem[]::new));
		qcFilterTypes.add(valid);

		SelectItemGroup invalid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.invalid"));
		invalid.setSelectItems(domainSelector.getSelectedDomainQcTypes().entrySet()
				.stream()
				.filter(e -> !e.getValue())
				.map(e -> new SelectItem(e.getKey(), getQcTypeLabel(e.getKey())))
				.toArray(SelectItem[]::new));
		qcFilterTypes.add(invalid);
	}

	/**
	 * Select template for new consent and init modules, freetexts and signerIds accordingly.
	 */
	public void onSelectTemplate()
	{
		editConsent.setTemplate(getTemplate(editConsent.getKey().getConsentTemplateKey()));
		if (editConsent.getDomain() == null)
		{
			editConsent.setDomainAndInitSignerIds(domainSelector.getSelectedDomain());
		}

		if (!editConsent.getTemplate().getFinalised())
		{
			Object[] args = { getBundle().getString("template.type." + editConsent.getTemplateType().toString()), getRequestPath(
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
			long allowedFileSize = getSelectedDomain().getConfig().getScansConfig().getSizeLimit();
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
			ConsentParseResultDTO result = consentParser.decodePDF(file.toAbsolutePath().toString(), true, getService());
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
											m.getConsentStatus().getFirst(),
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
	 * 		template retrieve policy from
	 * @param module
	 * 		name of template which should be part assigned to template
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

			if (ConsentTemplateType.CONSENT_OPT_OUT.equals(templateType))
			{
				getServiceWithAutomaticNotification().addConsentOptOut(editConsent.getTemplate().getKey(), editConsent.getKey().getSignerIds());
			}
			else
			{
				getServiceWithAutomaticNotification().addConsent(editConsent.toDTO());
			}

			ConsentTemplateDTO template = Objects.requireNonNull(getTemplate(editConsent.getKey().getConsentTemplateKey()));
			Object[] args = { template.getLabel(), template.getVersionLabelAndVersion(),
					editConsent.getKey().getConsentTemplateKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("consent.message.info.added." + templateType.toString()))
					.format(args), Severity.INFO);

			consentPageMode = searchSignerId == null ? ConsentPageMode.LIST : ConsentPageMode.SEARCH;
			loadConsents();
			selectedConsent = editConsent;
			embeddedController.setDone(true);
		}
		catch (MissingRequiredObjectException | InvalidFreeTextException | MandatoryFieldsException | UnknownDomainException | UnknownSignerIdTypeException | DuplicateEntryException
			   | UnknownModuleException | UnknownConsentTemplateException
			   | InvalidVersionException | RequirementsNotFullfilledException | InvalidParameterException e)
		{
			if (e.getLocalizedMessage().contains("if at least one mandatory module have a consent status of"))
			{
				logMessage(getBundle().getString("consent.message.warn.mandatoryDeclinedOptionalAccepted"), Severity.WARN);
			}
			else if (e instanceof RequirementsNotFullfilledException && !e.getMessage().contains("finalised"))
			{
				logMessage(getBundle().getString("consent.message.warn.expirationExpressionParseError"), Severity.WARN);
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
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
	 * 		FileUploadEvent with new scan
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
	 * 		scan to be removed
	 */
	public void onRemoveScan(ConsentScanDTO scan)
	{
		editConsent.getScans().remove(scan);
	}

	private void updateConsentInUse(ConsentScanDTO scanDTO) throws InvalidVersionException, UnknownDomainException,
			UnknownConsentTemplateException, UnknownSignerIdTypeException, UnknownConsentException, InvalidParameterException
	{
		getServiceWithAutomaticNotification().updateConsentInUse(selectedConsent.getKey(), selectedConsent.getExternProperties(), selectedConsent.getComment(), scanDTO);
	}

	/**
	 * Saves the given scan for the selected consent in the database
	 *
	 * @param event
	 * 		FileUploadEvent with new scan
	 */
	public void onSaveScanForSelectedConsent(FileUploadEvent event)
	{
		ConsentScanDTO scanDTO = new ConsentScanDTO(selectedConsent.getKey(), Base64.getEncoder().encodeToString(event.getFile().getContent()),
				event.getFile().getContentType(), event.getFile().getFileName(), new Date());

		try
		{
			updateConsentInUse(scanDTO);
			selectedConsent.setScans(getService().getConsent(selectedConsent.getKey()).getScans());
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
	 * 		consent from whom the selected scan should be removed
	 */
	public void onDeleteSelectedScan(WebConsent consent)
	{
		try
		{
			getServiceWithAutomaticNotification().removeScanFromConsent(consent.getKey(), consent.getSelectedScan());
			consent.setScans(getService().getConsent(consent.getKey()).getScans());
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
			getServiceWithAutomaticNotification().addSignerIdToConsent(selectedConsent.getKey(), newSignerId);
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
			ConsentDTO oldConsent = getService().getConsent(selectedConsent.getKey());
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
			ConsentTemplateDTO template = getService().getConsentTemplate(consent.getKey().getConsentTemplateKey());
			return FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath() + "?faces-redirect=true&templateType=" + consent.getTemplateType().name()
					+ "&print=true";
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		return null;
	}

	public void onDownloadConsentPDF(WebConsent consent)
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_CONSENT, consent.getKey());

		String pdfPrintUrl = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL().toString()
				+ ";jsessionid="
				+ FacesContext.getCurrentInstance().getExternalContext().getSessionId(true)
				+ "?templateType=" + consent.getTemplateType().name()
				+ "&pdf=true";
		downloadFile = getChromeDriverService().getPDFFromUrl(pdfPrintUrl);
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
		newQc.setProblems(selectedConsent.getQualityControl().getProblems());
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
		getServiceWithAutomaticNotification().setQCForConsent(selectedConsent.getKey(), newQc);

		if (INVALIDATED.equals(newQc.getType()))
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.qc.message.info.invalidated")).format(args), Severity.INFO);
		}
		else
		{
			logMessage(new MessageFormat(getBundle().getString("page.consents.qc.message.info.saved")).format(args), Severity.INFO);
		}
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
			consentCount = (int) getService().countConsentsForDomainWithFilter(domainSelector.getSelectedDomainName(), paginationConfig);

			// Init lazy model
			consentsLazyModel = new WebConsentLazyModel(getService(), domainSelector.getSelectedDomain(), paginationConfig, searchSignerId != null);
		}
		catch (Exception e)
		{
			logMessage(e);
		}
	}

	public void loadFullConsent(WebConsent webConsent)
	{
		try
		{
			webConsent.updateFromFullConsentDTO(getService().getConsent(webConsent.getKey()));
		}
		catch (UnknownDomainException | InvalidParameterException | InvalidVersionException | InconsistentStatusException | UnknownConsentTemplateException | UnknownSignerIdTypeException |
			   UnknownConsentException e)
		{
			logger.error(e.getLocalizedMessage(), e);
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
		if (editConsent != null && !editConsent.getKey().getSignerIds().isEmpty() && ConsentTemplateType.REVOCATION.equals(templateType))
		{
			templates = getService().getMappedTemplatesForSignerId(domainSelector.getSelectedDomainName(), templateType, editConsent.getKey().getOrderedSignerIds().getFirst(), true).stream()
					.map(t -> {
						try
						{
							return getService().getConsentTemplate(t);
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
			templates = getService().listConsentTemplates(domainSelector.getSelectedDomainName(), false).stream()
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
			return getService().getConsentTemplate(key);
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
	 * 		the scan to return a title for
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
	 * 		the scan to return a title tip for
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

	public void onShowDetails(WebConsent consent)
	{
		setSelectedConsent(consent);
		qcController.load(this.selectedConsent);
	}

	public void onShowScans(WebConsent consent)
	{
		setSelectedConsent(consent);
		replaceScanCheckbox = false;
		scanFile = null;
	}

	public void setSelectedConsent(WebConsent selectedConsent)
	{
		if (selectedConsent != null)
		{
			this.selectedConsent = selectedConsent;
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
		WebConsent c = printConsents.getFirst();

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

	public String getPrettyProblemList(List<QCProblemDTO> problems)
	{
		StringBuilder sb = new StringBuilder();
		for (QCProblemDTO problem : problems.stream().filter(p -> QCProblemStatusType.OPEN.equals(p.getStatus().getType())).toList())
		{
			sb.append("- ").append(problem.getType(getQcConfig()).getLabelOrId(languageBean.getLanguage()));
			sb.append(" (").append(getBundle().getString("model.qc.problem.status." + problem.getStatus().name())).append(")");
			sb.append("<br/>");
		}
		return sb.toString();
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

	public boolean isShowQcHistory()
	{
		return showQcHistory;
	}

	public void setShowQcHistory(boolean showQcHistory)
	{
		this.showQcHistory = showQcHistory;
	}

	public SignerIdDTO getSearchSignerId()
	{
		return searchSignerId;
	}

	public ConsentPageMode getConsentPageMode()
	{
		return consentPageMode;
	}

	public DefaultStreamedContent getDownloadFile()
	{
		return downloadFile;
	}

	public enum ConsentPageMode
	{
		LIST, PARSE, NEW, PRINT, SEARCH
	}

	public boolean isQcEnabled()
	{
		return !domainSelector.getSelectedDomainQcTypes().entrySet().isEmpty();
	}

	public ModuleQRCodec getModuleQrCodec()
	{
		return ModuleQRCodec.DEFAULT;
	}
}
