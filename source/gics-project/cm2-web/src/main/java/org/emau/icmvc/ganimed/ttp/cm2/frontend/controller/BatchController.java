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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.ModuleKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.PolicyKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.TemplateKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport.Status;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebImport.Type;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;

/**
 * Backing Bean for Batch View
 *
 * @author Arne Blumentritt
 */
@ManagedBean(name = "batchController")
@ViewScoped
public class BatchController extends AbstractGICSBean implements Serializable
{
	@Serial
	private static final long serialVersionUID = 8305310169562399180L;
	public int currentStep = 1;
	private BatchPageMode batchPageMode = null;
	private transient UploadedFile importFile;
	private Boolean importAllowUpdates = false;
	private List<WebImport> importedObjects;

	private DefaultStreamedContent exportFile;
	private Boolean exportLogo;
	private FhirExportMode exportMode;
	private String exportFormat = "JSON";
	private DualListModel<PolicyDTO> policies;
	private DualListModel<ModuleDTO> modules;
	private DualListModel<ConsentTemplateDTO> templates;

	public void init()
	{
		batchPageMode = null;
		currentStep = 1;
		importAllowUpdates = false;
	}

	public void onNewImport()
	{
		importFile = null;
		batchPageMode = BatchPageMode.IMPORT;
		currentStep = 2;
		importedObjects = new ArrayList<>();
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
			parseImportResult(fhirService.importDefinition(new String(importFile.getContent(), StandardCharsets.UTF_8),
					importAllowUpdates, importFile.getContentType().contains("xml") ? "xml" : "json"));
			domainSelector.loadDomains();
			// Set selected domain by key (name) because object might have changed after import
			domainSelector.setSelectedDomain(domainSelector.getSelectedDomain().getName());

			long ignored = importedObjects.stream().filter(w -> w.getStatus().equals(Status.IGNORED)).count();
			long success = importedObjects.size() - ignored;

			logMessage(new MessageFormat(getBundle().getString("page.batch.message.info.imported"))
					.format(("" + success).split(" ")), Severity.INFO);

			if (ignored > 0)
			{
				logMessage(new MessageFormat(getBundle().getString("page.batch.message.info.ignored"))
						.format(("" + ignored).split(" ")), Severity.WARN);
			}
			currentStep = 4;
		}
		catch (InvalidExchangeFormatException | InternalException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDownload()
	{
		String format = exportFormat.toLowerCase();

		List<String> itemList;
		switch (exportMode)
		{
			case POLICIES:
				itemList = PolicyKeyConverter.getKeysAsStrings(policies.getTarget(), ';');
				break;
			case MODULES:
				itemList = ModuleKeyConverter.getKeysAsStrings(modules.getTarget(), ';');
				break;
			case TEMPLATES:
				itemList = TemplateKeyConverter.getKeysAsStrings(templates.getTarget(), ';');
				break;
			default:
				itemList = new ArrayList<>();
		}

		String fileName = getIndividualFileNameString(format, exportMode, policies, modules, templates);

		try
		{
			InputStream stream = new ByteArrayInputStream(
					fhirService.exportDefinition(domainSelector.getSelectedDomainName(), exportMode, itemList, exportLogo, format).getBytes(StandardCharsets.UTF_8));
			exportFile = DefaultStreamedContent.builder().contentType("text/" + format).name(fileName).contentEncoding(StandardCharsets.UTF_8.name())
					.stream(() -> stream).build();
		}
		catch (InternalException | InvalidExchangeFormatException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/**
	 * create case-specific filename with regards to exported content
	 *
	 * @param format
	 * @param exportMode
	 * @param policies
	 * @param modules
	 * @param templates
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
			case POLICIES:
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
				break;
			case MODULES:
				if (mCount != 1)
				{
					contentString += exportMode;
				}
				else
				{
					ModuleKeyDTO m = modules.getTarget().get(0).getKey();
					contentString += "MODULE" + sep + m.getName() + sep + versionPrefix + m.getVersion();
				}
				break;
			case TEMPLATES:
				if (tCount != 1)
				{
					contentString += exportMode;
				}
				else
				{
					ConsentTemplateKeyDTO t = templates.getTarget().get(0).getKey();
					contentString += "TEMPLATE" + sep + t.getName() + sep + versionPrefix + t.getVersion();
				}
				break;
			case ALL:
				contentString += "COMPLETE";
				break;
			case DOMAIN:
				contentString += "DOMAIN_ONLY";
				break;
			default:
				break;
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
			policies.setSource(service.listPolicies(domainSelector.getSelectedDomainName(), false).stream()
					.sorted(Comparator.comparing(p -> p.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList()));
			policies.setTarget(new ArrayList<>());

			modules.setSource(service.listModules(domainSelector.getSelectedDomainName(), false).stream()
					.sorted(Comparator.comparing(m -> m.getLabelOrName().toLowerCase()))
					.collect(Collectors.toList()));
			modules.setTarget(new ArrayList<>());

			templates.setSource(service.listConsentTemplates(domainSelector.getSelectedDomainName(), false).stream()
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
			parseImportResult(fhirService.previewImportDefinition(new String(importFile.getContent(), StandardCharsets.UTF_8), importAllowUpdates,
					importFile.getContentType().contains("xml") ? "xml" : "json"));
		}
		catch (InvalidExchangeFormatException | InternalException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	private void parseImportResult(ImportResultDTO result)
	{
		importedObjects = new ArrayList<>();

		if (result != null)
		{
			Map<String, DomainDTO> domains = new HashMap<>();

			result.getIgnoredDomains().forEach(d -> domains.put(d.getName(), d));
			result.getAddedDomains().forEach(d -> domains.put(d.getName(), d));
			result.getUpdatedDomains().forEach(d -> domains.put(d.getName(), d));

			// added objects
			result.getAddedDomains().forEach(d -> importedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.ADDED)));
			result.getAddedTemplates()
					.forEach(t -> importedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.ADDED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getAddedModules()
					.forEach(m -> importedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.ADDED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getAddedPolicies()
					.forEach(p -> importedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.ADDED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));

			// updated objects
			result.getUpdatedDomains().forEach(d -> importedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.UPDATED)));
			result.getUpdatedTemplates()
					.forEach(t -> importedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.UPDATED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getUpdatedModules()
					.forEach(m -> importedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.UPDATED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getUpdatedPolicies()
					.forEach(p -> importedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.UPDATED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));

			// ignored objects
			result.getIgnoredDomains().forEach(d -> importedObjects.add(new WebImport(Type.DOMAIN, d.getName(), null, Status.IGNORED)));
			result.getIgnoredTemplates()
					.forEach(t -> importedObjects.add(new WebImport(Type.TEMPLATE, t.getName(), t.getVersion(), Status.IGNORED, domains.get(t.getDomainName()).getCtVersionConverterInstance())));
			result.getIgnoredModules()
					.forEach(m -> importedObjects.add(new WebImport(Type.MODULE, m.getName(), m.getVersion(), Status.IGNORED, domains.get(m.getDomainName()).getModuleVersionConverterInstance())));
			result.getIgnoredPolicies()
					.forEach(p -> importedObjects.add(new WebImport(Type.POLICY, p.getName(), p.getVersion(), Status.IGNORED, domains.get(p.getDomainName()).getPolicyVersionConverterInstance())));
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

	public boolean getLastStep()
	{
		return BatchPageMode.IMPORT == batchPageMode && currentStep == 4
				|| BatchPageMode.EXPORT == batchPageMode && currentStep == 3;
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

	public Boolean getImportAllowUpdates()
	{
		return importAllowUpdates;
	}

	public void setImportAllowUpdates(Boolean importAllowUpdates)
	{
		this.importAllowUpdates = importAllowUpdates;
	}

	public List<WebImport> getImportedObjects()
	{
		return importedObjects;
	}

	public BatchPageMode getBatchPageMode()
	{
		return batchPageMode;
	}

	public String getExportFormat()
	{
		return exportFormat;
	}

	public void setExportFormat(String exportFormat)
	{
		this.exportFormat = exportFormat;
	}

	public List<String> getExportFormats()
	{
		return new ArrayList<>(Arrays.asList("JSON", "XML"));
	}

	public enum BatchPageMode
	{
		IMPORT, EXPORT
	}
}
