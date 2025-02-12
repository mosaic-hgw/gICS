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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ImportResultDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FhirExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.ModuleKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.PolicyKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.TemplateKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport.Status;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport.Type;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;

/**
 * Backing Bean for Batch View
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@Named("batchController")
public class BatchController extends AbstractGICSBean
{
	@Serial
	private static final long serialVersionUID = 8305310169562399180L;
	private int currentStep = 1;
	private BatchPageMode batchPageMode = null;
	private transient UploadedFile importFile;
	private boolean finaliseAfterImport = false;
	private Boolean importAllowUpdates = false;
	private List<WebImport> processedObjects;

	private DefaultStreamedContent exportFile;
	private Boolean exportLogo;
	private FhirExportMode exportMode;
	private ExportFormat exportFormat = ExportFormat.JSON;
	private DualListModel<PolicyDTO> policies;
	private DualListModel<ModuleDTO> modules;
	private DualListModel<ConsentTemplateDTO> templates;
	public static final String TEMPLATE_NAME = "templateName";
	public static final String TEMPLATE_VERSION = "templateVersion";
	public static final String MODE = "mode";
	public static final String MODE_EXPORT = "export";
	public static final String MODE_IMPORT = "import";
	public static final String REFERER = "referer";
	private String refererLink;
	private boolean includeQrCode = false;

	public void initWithParams(String mode, String templateName, String templateVersion, String referer)
	{
		// skip ajax posts
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}

		if (StringUtils.isNotBlank(mode) && StringUtils.isNotBlank(templateName) && StringUtils.isNotBlank(templateVersion))
		{
			setInitialStep(mode, templateName, templateVersion, referer);
		}
	}

	@PostConstruct
	public void init()
	{
		batchPageMode = null;
		currentStep = 1;
		importAllowUpdates = false;
		refererLink = null;
	}

	public void openReferer() throws IOException
	{
		FacesContext.getCurrentInstance().getExternalContext().redirect(refererLink);
	}

	private void setInitialStep(String mode, String templateName, String templateVersion, String referer)
	{
		switch (mode)
		{
			case MODE_EXPORT -> onNewExport();
			case MODE_IMPORT -> onNewImport();
			default -> logMessage("requested mode '" + mode + "' is not available", Severity.ERROR);
		}

		exportMode = FhirExportMode.TEMPLATES;
		currentStep = 3;
		ConsentTemplateKeyDTO templateKeyDTO = new ConsentTemplateKeyDTO(
				domainSelector.getSelectedDomainName(), templateName, templateVersion);
		this.refererLink = referer;

		try
		{
			templates.getTarget().add(getService().getConsentTemplate(templateKeyDTO));
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | InvalidParameterException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onNewImport()
	{
		importFile = null;
		batchPageMode = BatchPageMode.IMPORT;
		currentStep = 2;
		processedObjects = new ArrayList<>();
	}

	public void onNewExport()
	{
		batchPageMode = BatchPageMode.EXPORT;
		currentStep = 2;
		exportMode = null;
		exportLogo = false;

		policies = new DualListModel<>();
		modules = new DualListModel<>();
		templates = new DualListModel<>();
	}

	public void onUpload(FileUploadEvent event)
	{
		importFile = event.getFile();
		currentStep = 3;
		onPreview();
	}

	public void onImport()
	{
		try
		{
			parseImportResult(getFhirService().importDefinition(new String(importFile.getContent(), StandardCharsets.UTF_8),
					importAllowUpdates, importFile.getContentType().contains("xml") ? "xml" : "json"));
			domainSelector.loadDomains();
			// Set selected domain by key (name) because object might have changed after import
			domainSelector.setSelectedDomain(domainSelector.getSelectedDomain().getName());

			List<WebImport> importedObjects = processedObjects.stream().filter(o -> !Status.IGNORED.equals(o.getStatus())).toList();

			// Finalise imported objects
			if (finaliseAfterImport)
			{
				WebImport newDomain = importedObjects.stream().filter(o -> Type.DOMAIN.equals(o.getType())).findFirst().orElse(null);
				if (newDomain != null)
				{
					getManager().finaliseDomain(newDomain.getName());
				}

				String domainName = newDomain != null ? newDomain.getName() : domainSelector.getSelectedDomainName();
				importedObjects.stream().filter(o -> Type.POLICY.equals(o.getType())).forEach(o -> {
					try
					{
						getManager().finalisePolicy(new PolicyKeyDTO(domainName, o.getName(), o.getVersion()));
					}
					catch (InvalidParameterException | InvalidVersionException | UnknownDomainException | UnknownPolicyException e)
					{
						logger.error(e.getLocalizedMessage());
					}
				});

				importedObjects.stream().filter(o -> Type.MODULE.equals(o.getType())).forEach(o -> {
					try
					{
						getManager().finaliseModule(new ModuleKeyDTO(domainName, o.getName(), o.getVersion()), true);
					}
					catch (InvalidParameterException | InvalidVersionException | UnknownDomainException | RequirementsNotFullfilledException | UnknownModuleException e)
					{
						logger.error(e.getLocalizedMessage());
					}
				});

				importedObjects.stream().filter(o -> Type.TEMPLATE.equals(o.getType())).forEach(o -> {
					try
					{
						getManager().finaliseTemplate(new ConsentTemplateKeyDTO(domainName, o.getName(), o.getVersion()), true);
					}
					catch (InvalidParameterException | InvalidVersionException | UnknownDomainException | RequirementsNotFullfilledException |
						   UnknownConsentTemplateException e)
					{
						logger.error(e.getLocalizedMessage());
					}
				});
			}

			logMessage(new MessageFormat(getBundle().getString("page.batch.message.info.imported"))
					.format(new Object[] { importedObjects.size() }), Severity.INFO);

			int ignored = importedObjects.size() - processedObjects.size();
			if (ignored > 0)
			{
				logMessage(new MessageFormat(getBundle().getString("page.batch.message.info.ignored"))
						.format(new Object[] { ignored }), Severity.WARN);
			}
			currentStep = 4;
		}
		catch (InvalidExchangeFormatException | InternalException | InvalidParameterException | UnknownDomainException | VersionConverterClassException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDownload()
	{
		String format = exportFormat.name().toLowerCase();

		List<String> itemList = switch (exportMode)
		{
			case POLICIES -> PolicyKeyConverter.getKeysAsStrings(policies.getTarget(), ';');
			case MODULES -> ModuleKeyConverter.getKeysAsStrings(modules.getTarget(), ';');
			case TEMPLATES -> TemplateKeyConverter.getKeysAsStrings(templates.getTarget(), ';');
			default -> new ArrayList<>();
		};

		String fileName = getIndividualFileNameString(format, exportMode, policies, modules, templates);

		if (FhirExportMode.TEMPLATES.equals(exportMode) && ExportFormat.PDF.equals(exportFormat))
		{
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			Map<String, Object> sessionMap = externalContext.getSessionMap();
			sessionMap.put(SessionMapKeys.PRINT_TEMPLATES, templates.getTarget().stream().map(ConsentTemplateDTO::getKey).toList());
			sessionMap.put(SessionMapKeys.PRINT_OPTION_QR_CODE, includeQrCode);

			String pdfPrintUrl = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL().toString().replace("admin/batch.xhtml", "consents.xhtml")
					+ ";jsessionid="
					+ FacesContext.getCurrentInstance().getExternalContext().getSessionId(true)
					+ "?templateType=" + templates.getTarget().get(0).getType().name();
			exportFile = getChromeDriverService().getPDFFromUrl(pdfPrintUrl);
		}
		else
		{
			try
			{
				InputStream stream = new ByteArrayInputStream(
						getFhirService().exportDefinition(domainSelector.getSelectedDomainName(), exportMode, itemList, exportLogo, format).getBytes(StandardCharsets.UTF_8));
				exportFile = DefaultStreamedContent.builder().contentType("text/" + format).name(fileName).contentEncoding(StandardCharsets.UTF_8.name())
						.stream(() -> stream).build();
			}
			catch (InternalException | InvalidExchangeFormatException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
	}

	/**
	 * create case-specific filename with regard to exported content
	 *
	 * @return individual filename
	 */
	private String getIndividualFileNameString(String format, FhirExportMode exportMode, DualListModel<PolicyDTO> policies,
			DualListModel<ModuleDTO> modules, DualListModel<ConsentTemplateDTO> templates)
	{
		// File name
		SimpleDateFormat sdf = new SimpleDateFormat(getCommonBundle("en").getString("ui.date.pattern.date"));

		String contentString = "";
		String sep = " ";
		String versionPrefix = "v";

		int pCount = policies.getTarget().size();
		int mCount = modules.getTarget().size();
		int tCount = templates.getTarget().size();

		switch (exportMode)
		{
			case POLICIES ->
			{
				// templateCount=0, moduleCount=0;
				if (pCount != 1)
				{
					contentString += exportMode;
				}
				else
				{
					PolicyKeyDTO p = policies.getTarget().get(0).getKey();
					contentString += "POLICY" + sep + p.getName() + sep + versionPrefix + p.getVersion();
				}
			}
			case MODULES ->
			{
				if (mCount != 1)
				{
					contentString += exportMode;
				}
				else
				{
					ModuleKeyDTO m = modules.getTarget().get(0).getKey();
					contentString += "MODULE" + sep + m.getName() + sep + versionPrefix + m.getVersion();
				}
			}
			case TEMPLATES ->
			{
				if (tCount != 1)
				{
					contentString += exportMode;
				}
				else
				{
					ConsentTemplateKeyDTO t = templates.getTarget().get(0).getKey();
					contentString += "TEMPLATE" + sep + t.getName() + sep + versionPrefix + t.getVersion();
				}
			}
			case ALL -> contentString += "COMPLETE";
			case DOMAIN -> contentString += "DOMAIN_ONLY";
		}

		String fileName = sdf.format(new Date());
		// fileName += sep + getBundle().getString("page.batch.export.filename");
		fileName += sep + domainSelector.getSelectedDomain().getLabel().replaceAll("[^a-zA-Z0-9-_.]", "_");
		// fileName += sep+"gICS";
		if (!contentString.isEmpty())
		{
			fileName += sep + contentString;
		}
		fileName += "." + format;
		return fileName;
	}

	public void loadItems()
	{
		try
		{
			policies.setSource(getService().listPolicies(domainSelector.getSelectedDomainName(), false).stream()
					.sorted(Comparator.comparing(p -> p.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList()));
			policies.setTarget(new ArrayList<>());

			modules.setSource(getService().listModules(domainSelector.getSelectedDomainName(), false).stream()
					.sorted(Comparator.comparing(m -> m.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList()));
			modules.setTarget(new ArrayList<>());

			templates.setSource(getService().listConsentTemplates(domainSelector.getSelectedDomainName(), false).stream()
					.sorted(Comparator.comparing(t -> t.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList()));
			templates.setTarget(new ArrayList<>());
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onPreview()
	{
		try
		{
			parseImportResult(getFhirService().previewImportDefinition(new String(importFile.getContent(), StandardCharsets.UTF_8), importAllowUpdates,
					importFile.getContentType().contains("xml") ? "xml" : "json"));
		}
		catch (InvalidExchangeFormatException | InternalException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	private void parseImportResult(ImportResultDTO result)
	{
		processedObjects = new ArrayList<>();

		if (result != null)
		{
			Map<String, DomainDTO> domains = new HashMap<>();

			result.getIgnoredDomains().forEach(d -> domains.put(d.getName(), d));
			result.getAddedDomains().forEach(d -> domains.put(d.getName(), d));
			result.getUpdatedDomains().forEach(d -> domains.put(d.getName(), d));

			// added objects
			result.getAddedDomains().forEach(d -> processedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.ADDED)));
			result.getAddedTemplates()
					.forEach(t -> processedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.ADDED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getAddedModules()
					.forEach(m -> processedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.ADDED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getAddedPolicies()
					.forEach(p -> processedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.ADDED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));

			// updated objects
			result.getUpdatedDomains().forEach(d -> processedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.UPDATED)));
			result.getUpdatedTemplates()
					.forEach(t -> processedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.UPDATED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getUpdatedModules()
					.forEach(m -> processedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.UPDATED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getUpdatedPolicies()
					.forEach(p -> processedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.UPDATED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));

			// ignored objects
			result.getIgnoredDomains().forEach(d -> processedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.IGNORED)));
			result.getIgnoredTemplates()
					.forEach(t -> processedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.IGNORED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getIgnoredModules()
					.forEach(m -> processedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.IGNORED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getIgnoredPolicies()
					.forEach(p -> processedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.IGNORED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));
		}
	}

	public UploadedFile getImportFile()
	{
		return importFile;
	}

	public FhirExportMode getExportMode()
	{
		return exportMode;
	}

	public void setExportMode(FhirExportMode exportMode)
	{
		this.exportMode = exportMode;
	}

	public FhirExportMode[] getExportModes()
	{
		return FhirExportMode.values();
	}

	public int getCurrentStep()
	{
		return currentStep;
	}

	public void stepNext()
	{
		currentStep++;
	}

	public void stepPrev()
	{
		if (currentStep == 2)
		{
			init();
		}
		else
		{
			currentStep--;
		}
	}

	public DualListModel<PolicyDTO> getPolicies()
	{
		return policies;
	}

	public void setPolicies(DualListModel<PolicyDTO> policies)
	{
		this.policies = policies;
	}

	public DualListModel<ModuleDTO> getModules()
	{
		return modules;
	}

	public void setModules(DualListModel<ModuleDTO> modules)
	{
		this.modules = modules;
	}

	public DualListModel<ConsentTemplateDTO> getTemplates()
	{
		return templates;
	}

	public void setTemplates(DualListModel<ConsentTemplateDTO> templates)
	{
		this.templates = templates;
	}

	public DefaultStreamedContent getExportFile()
	{
		return exportFile;
	}

	public Boolean getExportLogo()
	{
		return exportLogo;
	}

	public void setExportLogo(Boolean exportLogo)
	{
		this.exportLogo = exportLogo;
	}

	public boolean isFinaliseAfterImport()
	{
		return finaliseAfterImport;
	}

	public void setFinaliseAfterImport(boolean finaliseAfterImport)
	{
		this.finaliseAfterImport = finaliseAfterImport;
	}

	public Boolean getImportAllowUpdates()
	{
		return importAllowUpdates;
	}

	public void setImportAllowUpdates(Boolean importAllowUpdates)
	{
		this.importAllowUpdates = importAllowUpdates;
	}

	public List<WebImport> getProcessedObjects()
	{
		return processedObjects;
	}

	public BatchPageMode getBatchPageMode()
	{
		return batchPageMode;
	}

	public ExportFormat getExportFormat()
	{
		return exportFormat;
	}

	public void setExportFormat(ExportFormat exportFormat)
	{
		this.exportFormat = exportFormat;
	}

	public String getRefererLink()
	{
		return refererLink;
	}

	public boolean isIncludeQrCode()
	{
		return includeQrCode;
	}

	public void setIncludeQrCode(boolean includeQrCode)
	{
		this.includeQrCode = includeQrCode;
	}

	public enum BatchPageMode
	{
		IMPORT, EXPORT
	}

	public enum ExportFormat
	{
		JSON, XML, PDF
	}

	public List<ExportFormat> getAvailableExportFormats()
	{
		return Arrays.stream(ExportFormat.values()).filter(f -> (FhirExportMode.TEMPLATES.equals(exportMode) && getSelectedDomain().getConfig().getApplicationConfig().isEnableChromePdfExport()) || !ExportFormat.PDF.equals(f)).toList();
	}
}
