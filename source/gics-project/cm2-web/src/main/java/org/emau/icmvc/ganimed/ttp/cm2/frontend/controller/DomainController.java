package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.io.IOUtils;
import org.emau.icmvc.ganimed.ttp.cm2.GICSFhirService;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.ModuleConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.PolicyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.converter.TemplateKeyConverter;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.Property;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.util.Base64;
import org.slf4j.LoggerFactory;


/**
 * Backing Bean for Domains View
 * 
 * @author Arne Blumentritt, Martin Bialke
 * 
 */
@ManagedBean(name = "domainController")
@ViewScoped
public class DomainController extends AbstractConsentController implements Serializable
{

	private static final long serialVersionUID = 1285364203849974795L;
	private DomainDTO selectedDomain;
	private DomainDTO editDomain;
	private Boolean editMode;
	private Boolean importMode;

	private List<Property> properties;
	private String signerTypes;

	// Import / Export
	private Boolean importAllowUpdates = false;
	private Boolean exportMode;
	private Boolean exportLogo;
	private UploadedFile importFile;
	private UploadedFile logoFile;
	private String domainLogoBase64;
	private String selectedExportMode;
	private DualListModel<PolicyDTO> policiesPicklist;
	private DualListModel<ModuleDTO> modulesPicklist;
	private DualListModel<ConsentTemplateDTO> templatesPicklist;

	@EJB(lookup = "java:global/gics/cm2-ejb/GICSFhirServiceImpl!org.emau.icmvc.ganimed.ttp.cm2.GICSFhirService")
	protected GICSFhirService fhirService;

	@PostConstruct
	public void init()
	{
		logger = LoggerFactory.getLogger(getClass());
		properties = new ArrayList<Property>();
		editMode = false;
		importMode = false;
		exportMode = false;
	}

	public void onDomainSelect(SelectEvent event)
	{
		selectedDomain = (DomainDTO) event.getObject();
	}

	public void onNewDomain()
	{
		editMode = true;
		editDomain = new DomainDTO();
		loadProperties(null);
		loadSignerIdTypes(null);
		loadLogo(null);
	}

	public void onSaveDomain(Boolean isNew)
	{
		try
		{
			// Set signerId types
			String[] signerTypesArray = signerTypes.split(",", -1);
			for (String type : signerTypesArray)
			{
				type = type.trim();
			}
			editDomain.setSignerIdTypes(Arrays.asList(signerTypesArray));

			// Set properties
			StringBuilder sb = new StringBuilder();
			for (Property p : properties)
			{
				if (!p.getValue().isEmpty())
				{
					sb.append(p.getLabel());
					sb.append('=');
					sb.append(p.getValue());
					sb.append(';');
				}
			}
			editDomain.setProperties(sb.toString());
			Object[] args = { editDomain.getLabel() };

			// set domain logo
			if (logoFile != null)
			{
				editDomain.setLogo(Base64.encodeToString(logoFile.getContents(), false));
			}
			else
			{
				// was removed
				editDomain.setLogo(null);
			}

			if (isNew)
			{
				editDomain.setName(editDomain.getLabel());

				cmManager.addDomain(editDomain);
				logMessage(new MessageFormat(getBundle().getString("domain.message.info.added")).format(args), Severity.INFO);
			}
			else
			{
				cmManager.updateDomain(editDomain.getName(),
						editDomain.getLabel(), editDomain.getLogo(), editDomain.getExternProperties(),
						editDomain.getComment());

				logMessage(new MessageFormat(getBundle().getString("domain.message.info.updated")).format(args), Severity.INFO);
			}

			domainSelector.loadDomains();
			selectedDomain = null;
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("domain.message.error.duplicate"), Severity.ERROR);
		}
		catch (VersionConverterClassException e)
		{
			logMessage(getBundle().getString("domain.message.error.versionFormat"), Severity.ERROR);
		}
		catch (UnknownDomainException e)
		{
			logMessage(getBundle().getString("domain.message.error.unknownDomain"), Severity.ERROR);
		}
	}

	public void onEditDomain(DomainDTO domain)
	{
		editMode = true;
		editDomain = domain;
		loadProperties(domain);
		loadSignerIdTypes(domain);
		loadLogo(domain);
	}

	public void onCancel()
	{
		editMode = false;
		importMode = false;
		exportMode = false;
	}

	public void onDeleteDomain(DomainDTO domain) throws UnknownDomainException
	{
		try
		{
			cmManager.deleteDomain(domain.getName());
			Object[] args = { domain.getName() };
			logMessage(new MessageFormat(getBundle().getString("domain.message.info.deleted")).format(args), Severity.INFO);
			domainSelector.loadDomains();
			selectedDomain = null;
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("domain.message.error.deleteInUse"), Severity.ERROR);
		}
	}

	public void onImportDomain()
	{
		importMode = true;
	}

	public void onExportDomain()
	{
		initPickItems();
		exportMode = true;
	}

	public void onExportModeChange()
	{
		logger.info("export mode changed: " + selectedExportMode);
	}

	public String getSelectedExportMode()
	{
		return selectedExportMode;
	}

	public void setSelectedExportMode(String mode)
	{
		this.selectedExportMode = mode;
	}

	public List<String> getExportModes()
	{
		List<String> modes = new ArrayList<String>();
		for (ExportMode mode : ExportMode.values())
		{
			modes.add(mode.toString());
		}
		return modes;
	}

	public void onRemoveLogo()
	{
		removeLogo();
	}

	private void removeLogo()
	{
		logoFile = null;
		domainLogoBase64 = null;
	}

	public void onUploadDomain(FileUploadEvent event)
	{
		if (event.getFile() != null)
		{
			setImportFile(event.getFile());

			if (logger.isInfoEnabled())
			{
				logger.info("filename: " + importFile.getFileName()
						+ ", filesize in bytes: " + importFile.getSize()
						+ ", allowUpdates: " + importAllowUpdates);
			}

			try
			{				
				String fileContentString = new String(importFile.getContents(), "UTF-8");
				fhirService.importDefinition(fileContentString, importAllowUpdates);
				logMessage("The file has been successfully uploaded and processed.", Severity.INFO);
				importMode = false;
				domainSelector.loadDomains();
			}
			catch (InvalidExchangeFormatException | InternalException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
			catch (UnsupportedEncodingException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
		else
		{
			logMessage("file is null", Severity.ERROR);
		}
	}

	public String getDomainLogo()
	{
		return this.domainLogoBase64;
	}

	public void onUploadLogo(FileUploadEvent event)
	{
		if (event.getFile() != null)
		{
			try
			{
				setLogoFile(event.getFile());
				setLogoContent(event.getFile().getInputstream());
			}
			catch (IOException e)
			{

				logger.error("error occured during logo import: " + e.getMessage());
				e.printStackTrace();
			}

			if (logger.isInfoEnabled())
			{
				logger.info("filename: " + logoFile.getFileName()
						+ ", filesize in bytes: " + logoFile.getSize());
			}
		}
		else
		{
			logMessage("file is null", Severity.ERROR);
		}
	}

	public Boolean hasDomainLogo()
	{
		if (domainLogoBase64 != null)
		{
			return true;
		}

		return false;
	}
	
	public StreamedContent getExportFile() throws UnsupportedEncodingException
	{
		String result = "";
		String domainName = domainSelector.getSelectedDomainName();

		if (domainName != null && !domainName.isEmpty())
		{
			logger.debug("Export Mode: " + selectedExportMode);

			try
			{
				switch (selectedExportMode)
				{
					case "ALL":
					{
						result = fhirService.exportDefinition(domainName, ExportMode.ALL, new ArrayList<String>(), exportLogo);
						break;
					}

					case "DOMAIN":
					{
						result = fhirService.exportDefinition(domainName, ExportMode.DOMAIN, new ArrayList<String>(), exportLogo);
						break;
					}

					case "POLICIES":
					{
						List<String> policyKeys = PolicyConverter.getAsStrings(policiesPicklist.getTarget(), ';');
						result = fhirService.exportDefinition(domainName, ExportMode.POLICIES, policyKeys, exportLogo);
						break;
					}

					case "MODULES":
					{
						List<String> moduleKeys = ModuleConverter.getAsStrings(modulesPicklist.getTarget(), ';');
						result = fhirService.exportDefinition(domainName, ExportMode.MODULES, moduleKeys, exportLogo);
						break;
					}

					case "TEMPLATES":
					{
						List<String> templateKeys = TemplateKeyConverter.getAsStrings(templatesPicklist.getTarget(), ';');
						result = fhirService.exportDefinition(domainName, ExportMode.TEMPLATES, templateKeys, exportLogo);
						break;
					}

					default:
					{
						break;
					}
				}
			}
			catch (UnknownDomainException e)
			{

				e.printStackTrace();
			}
			catch (InternalException e)
			{
				e.printStackTrace();
			}

			return new DefaultStreamedContent(new ByteArrayInputStream(result.getBytes("UTF-8")), "text/xml", domainName + "_MODE_" + selectedExportMode + "_export.xml", "UTF-8");
		}

		return new DefaultStreamedContent(new ByteArrayInputStream("empty".getBytes("UTF-8")), "text/xml", domainName + "_export.xml", "UTF-8");
	}

	public DomainDTO getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain;
	}

	public DomainDTO getEditDomain()
	{
		return editDomain;
	}

	public Boolean getEditMode()
	{
		return editMode;
	}

	public List<DomainDTO> getDomains()
	{
		return domainSelector.getDomains();
	}

	public List<Property> getProperties()
	{
		return properties;
	}

	// public void setProperties(List<Property> properties)
	// {
	// this.properties = properties;
	// }

	public String getSignerTypes()
	{
		return signerTypes;
	}

	public void setSignerTypes(String signerTypes)
	{
		this.signerTypes = signerTypes;
	}

	public Boolean getImportAllowUpdates()
	{
		return importAllowUpdates;
	}

	public Boolean getImportMode()
	{
		return importMode;
	}

	public Boolean getExportMode()
	{
		return exportMode;
	}

	public Boolean getExportLogo()
	{
		return exportLogo;
	}

	public UploadedFile getImportFile()
	{
		return importFile;
	}

	public void setImportFile(UploadedFile importFile)
	{
		this.importFile = importFile;
	}

	public UploadedFile getLogoFile()
	{
		return logoFile;
	}

	public void setLogoFile(UploadedFile logoFile)
	{
		this.logoFile = logoFile;
	}

	public void setExportLogo(Boolean exportLogo)
	{
		this.exportLogo = exportLogo;
	}

	public Boolean isImportAllowUpdates()
	{
		return importAllowUpdates;
	}

	public void setImportAllowUpdates(Boolean importAllowUpdates)
	{
		this.importAllowUpdates = importAllowUpdates;
	}
	
	public DualListModel<PolicyDTO> getPolicyPickItems()
	{
		return policiesPicklist;
	}

	public void setPolicyPickItems(DualListModel<PolicyDTO> pickItems)
	{
		this.policiesPicklist = pickItems;
	}

	public DualListModel<ModuleDTO> getModulePickItems()
	{
		return modulesPicklist;
	}

	public void setModulePickItems(DualListModel<ModuleDTO> pickItems)
	{
		this.modulesPicklist = pickItems;
	}

	public DualListModel<ConsentTemplateDTO> getTemplatePickItems()
	{
		return templatesPicklist;
	}

	public void setTemplatePickItems(DualListModel<ConsentTemplateDTO> pickItems)
	{
		this.templatesPicklist = pickItems;
	}
	
	private void setLogoContent(InputStream logoStream)
	{
		if (logoStream != null)
		{
			String base64String;
			try
			{
				base64String = Base64.encodeToString(IOUtils.toByteArray(logoStream), false);
				// add base64 info for primefaces
				this.domainLogoBase64 = "data:image/png;base64," + base64String;

			}
			catch (IOException e)
			{
				logger.error("an error occured during base64 conversion: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private void initPickItems()
	{
		try
		{
			List<PolicyDTO> policyPiSource = new ArrayList<PolicyDTO>();
			for (PolicyDTO p : cmManager.listPolicies(domainSelector.getSelectedDomainName()))
			{
				policyPiSource.add(p);
			}

			List<ModuleDTO> modulePiSource = new ArrayList<ModuleDTO>();
			for (ModuleDTO m : cmManager.listModules(domainSelector.getSelectedDomainName()))
			{
				modulePiSource.add(m);
			}

			List<ConsentTemplateDTO> templatePiSource = new ArrayList<ConsentTemplateDTO>();
			for (ConsentTemplateDTO t : cmManager.listConsentTemplates(domainSelector.getSelectedDomainName()))
			{
				templatePiSource.add(t);
			}

			policiesPicklist = new DualListModel<PolicyDTO>(policyPiSource, new ArrayList<PolicyDTO>());
			modulesPicklist = new DualListModel<ModuleDTO>(modulePiSource, new ArrayList<ModuleDTO>());
			templatesPicklist = new DualListModel<ConsentTemplateDTO>(templatePiSource, new ArrayList<ConsentTemplateDTO>());
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}
	
	private void loadProperties(DomainDTO domain)
	{
		properties.clear();
		for (DomainProperties property : DomainProperties.values())
		{
			properties.add(new Property(property.toString()));
		}
		if (domain != null)
		{
			for (String propertyString : domain.getProperties().split(";"))
			{
				String[] tmp = propertyString.split("=");
				for (Property property : properties)
				{
					if (property.getLabel().equals(tmp[0]))
					{
						property.setValue(tmp[1]);
					}
				}
			}
		}
	}

	private void loadLogo(DomainDTO domain)
	{
		// load logo from db and store in logo file
		if (domain != null)
		{
			String logoString = domain.getLogo();

			if (logoString != null && !logoString.isEmpty())
			{
				byte[] logoBytes = Base64.decode(logoString);
				setLogoContent(new ByteArrayInputStream(logoBytes));
				return;
			}
			logger.debug("no logo for domain " + domain.getName() + " available");
			domainLogoBase64 = null;
		}
	}
	
	private void loadSignerIdTypes(DomainDTO domain)
	{
		StringBuilder sb = new StringBuilder();
		if (domain != null)
		{
			for (String type : domain.getSignerIdTypes())
			{
				if (sb.length() != 0)
				{
					sb.append(',');
				}
				sb.append(type);
			}
		}
		signerTypes = sb.toString();
	}
}
