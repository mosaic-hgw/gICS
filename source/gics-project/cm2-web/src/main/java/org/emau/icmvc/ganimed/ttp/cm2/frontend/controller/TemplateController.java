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

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.WebExpiration;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.TemplateTree;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.primefaces.model.DualListModel;

/**
 * Backing Bean for Consent Template View
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@ManagedBean(name = "templateController")
public class TemplateController extends AbstractConsentController implements Serializable
{
	@Serial
	private static final long serialVersionUID = -3735510572725064442L;

	// Show templates
	private List<ConsentTemplateDTO> templates;
	private ConsentTemplateDTO selectedTemplate;
	private TemplateTree templateTree;
	private boolean printOptionQrCode = true;

	// Edit Template
	private ConsentTemplateDTO editTemplate;
	private WebExpiration templateExpiration = new WebExpiration();

	// Edit Modules in template
	private DualListModel<AssignedModuleDTO> modules;
	private Map<ModuleKeyDTO, WebExpiration> moduleExpirations = new HashMap<>();
	private String modulesDuplicateVersion;

	// Edit Freetexts in template
	private List<FreeTextDefDTO> freeTextDefs;

	// Edit Mappings
	private List<ConsentTemplateKeyDTO> templatesWithPolicies = new ArrayList<>();

	@ManagedProperty(value = "#{moduleController}")
	private ModuleController moduleController;

	@PostConstruct
	protected void init()
	{
		loadTemplates();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (sessionMap.containsKey(SessionMapKeys.PRINT_TREE))
		{
			templateTree = (TemplateTree) sessionMap.get(SessionMapKeys.PRINT_TREE);
			sessionMap.remove(SessionMapKeys.PRINT_TREE);
		}
		pageMode = PageMode.READ;
	}

	public void onNewTemplate() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.NEW;
		editTemplate = new ConsentTemplateDTO(new ConsentTemplateKeyDTO());
		editTemplate.setFreeTextDefs(new HashSet<>());
		editTemplate.getKey().setDomainName(domainSelector.getSelectedDomainName());
		editTemplate.setAssignedModules(new HashSet<>());
		templateExpiration = new WebExpiration();
		loadModulesAndFreeTexts(editTemplate);
	}

	public void onEditTemplate(ConsentTemplateDTO template) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.EDIT;
		editTemplate = template;
		templateExpiration.getEditExpiration().setExpirationProperties(editTemplate.getExpirationProperties());
		loadModulesAndFreeTexts(editTemplate);

		if (template.getFinalised())
		{
			logMessage(getBundle().getString("template.message.info.isFinal"), Severity.INFO);
		}
	}

	public void onDuplicateTemplate(ConsentTemplateDTO template, boolean duplicateModules)
			throws UnknownDomainException, InvalidVersionException, DuplicateEntryException, UnknownPolicyException, RequirementsNotFullfilledException, InvalidParameterException
	{
		Set<AssignedModuleDTO> duplicatedModules = new HashSet<>();

		if (duplicateModules)
		{
			// Check for module version conflicts before copying any module
			for (AssignedModuleDTO original : template.getAssignedModules())
			{
				original.getModule().getKey().setVersion(modulesDuplicateVersion);
				for (ModuleDTO module : moduleController.getModules())
				{
					if (original.getModule().getKey().equals(module.getKey()))
					{
						logMessage(getBundle().getString("template.duplicate.modules.duplicate.conflict"), Severity.WARN);
						return;
					}
				}
			}

			for (AssignedModuleDTO original : template.getAssignedModules())
			{
				original.getModule().getKey().setVersion(modulesDuplicateVersion);

				// Create duplicate of assignedModule and save in database
				ModuleDTO moduleDTO = moduleController.onDuplicateModule(original.getModule());
				try
				{
					manager.addModule(moduleDTO, false);
				}
				catch (InvalidVersionException e)
				{
					logMessage(getBundle().getString("module.message.error.invalidVersion"), Severity.WARN, false);
					return;
				}

				AssignedModuleDTO duplicate = new AssignedModuleDTO(moduleDTO,
						original.getMandatory(),
						original.getDefaultConsentStatus(),
						original.getDisplayCheckboxes(),
						original.getOrderNumber(),
						original.getParent(),
						original.getComment(),
						original.getExternProperties(),
						original.getExpirationProperties(),
						original.getFhirID());

				duplicatedModules.add(duplicate);
			}
			modulesDuplicateVersion = null;
		}

		pageMode = PageMode.NEW;
		editTemplate = new ConsentTemplateDTO(template.getKey());
		editTemplate.setLabel(template.getLabel());
		editTemplate.setVersionLabel(template.getVersionLabel());
		editTemplate.setAssignedModules(duplicateModules ? duplicatedModules : template.getAssignedModules());
		editTemplate.setComment(template.getComment());
		editTemplate.setExternProperties(template.getExternProperties());
		editTemplate.setFooter(template.getFooter());
		editTemplate.setFreeTextDefs(template.getFreeTextDefs());
		editTemplate.setHeader(template.getHeader());
		editTemplate.setExpirationProperties(template.getExpirationProperties());
		editTemplate.setScanBase64(template.getScanBase64());
		editTemplate.setScanFileType(template.getScanBase64());
		editTemplate.setStructure(template.getStructure());
		editTemplate.setTitle(template.getTitle());
		editTemplate.setType(template.getType());
		templateExpiration.getEditExpiration().setExpirationProperties(template.getExpirationProperties());
		loadModulesAndFreeTexts(editTemplate);
	}

	public void onSaveTemplate()
	{
		prepareModulesAndFreeTextsForSaving();
		editTemplate.setExpirationProperties(templateExpiration.getEditExpiration().getExpirationProperties());
		Object[] args = { editTemplate.getLabel(), editTemplate.getVersionLabelAndVersion(), domainSelector.getSelectedDomainName() };

		if (pageMode == PageMode.NEW)
		{
			try
			{
				// Finalise domain to be able to add template
				if (!domainSelector.getSelectedDomain().getFinalised())
				{
					manager.finaliseDomain(domainSelector.getSelectedDomainName());
					domainSelector.loadDomains();
					domainSelector.setSelectedDomain(domainSelector.getSelectedDomainName());
				}

				// Add template
				if (StringUtils.isEmpty(editTemplate.getKey().getName()))
				{
					editTemplate.getKey().setName(editTemplate.getLabel());
				}
				manager.addConsentTemplate(editTemplate, false);
				logMessage(new MessageFormat(getBundle().getString("template.message.info.added")).format(args), Severity.INFO);
				loadTemplates();
				pageMode = PageMode.READ;
			}
			catch (DuplicateEntryException e)
			{
				logMessage(getBundle().getString("template.message.error.duplicate"), Severity.WARN);
			}
			catch (InvalidVersionException e)
			{
				logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.WARN);
			}
			catch (UnknownDomainException | UnknownModuleException | FreeTextConverterStringException | InvalidPropertiesException |
				   RequirementsNotFullfilledException | InvalidParameterException | UnknownConsentTemplateException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
		else if (pageMode == PageMode.EDIT)
		{
			try
			{
				if (editTemplate.getFinalised())
				{
					manager.updateConsentTemplateInUse(editTemplate);
				}
				else
				{
					manager.updateConsentTemplate(editTemplate, false);
				}
				logMessage(new MessageFormat(getBundle().getString("template.message.info.updated")).format(args), Severity.INFO);
				loadTemplates();
				pageMode = PageMode.READ;
			}
			catch (InvalidVersionException e)
			{
				logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.WARN);
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | DuplicateEntryException | FreeTextConverterStringException | InvalidPropertiesException | ObjectInUseException
					| UnknownModuleException | InvalidFreeTextException | RequirementsNotFullfilledException | InvalidParameterException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
	}

	public void onCancel()
	{
		pageMode = PageMode.READ;
	}

	public void onFinalise(ConsentTemplateDTO template)
	{
		try
		{
			manager.finaliseTemplate(template.getKey(), true);
			Object[] args = { template.getLabel(), template.getVersionLabelAndVersion() };
			logMessage(new MessageFormat(getBundle().getString("template.message.info.finalised")).format(args), Severity.INFO);
			loadTemplates();
		}
		catch (UnknownConsentTemplateException | InvalidVersionException | RequirementsNotFullfilledException | UnknownDomainException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onShowTree(ConsentTemplateDTO template)
	{
		templateTree = new TemplateTree(template, getSimpleDateFormat("date"));
	}

	public void onDeleteTemplate(ConsentTemplateDTO template)
	{
		Object[] args = { template.getLabel(), template.getVersionLabelAndVersion(), domainSelector.getSelectedDomainName() };

		try
		{
			manager.deleteConsentTemplate(template.getKey());
			logMessage(new MessageFormat(getBundle().getString("template.message.info.deleted")).format(args), Severity.INFO);
			loadTemplates();
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("template.message.error.deleteInUse"), Severity.WARN);
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public String onPrintTemplate()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TEMPLATE, selectedTemplate.getKey());
		sessionMap.put(SessionMapKeys.PRINT_OPTION_QR_CODE, printOptionQrCode);

		return "/html/internal/consents.xhtml?templateType=" + selectedTemplate.getType().name() + "&print=true&faces-redirect=true";
	}

	public String onPrintTemplateWithSignerIds()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TEMPLATE, selectedTemplate.getKey());
		sessionMap.put(SessionMapKeys.PRINT_OPTION_QR_CODE, printOptionQrCode);

		return "/html/internal/print.xhtml";
	}

	public String onPrintTree()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TREE, templateTree);
		return "/html/internal/printTree.xhtml?faces-redirect=true&print=true";
	}

	public void addNewestModule() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		if (!FacesContext.getCurrentInstance().isValidationFailed())
		{
			List<ModuleDTO> newModules = service.listModules(domainSelector.getSelectedDomainName(), false);
			AssignedModuleDTO newAssignedModule = createAssignedModule(newModules.stream().max(Comparator.comparing(ModuleDTO::getCreationDate)).orElse(null));
			modules.getTarget().add(0, newAssignedModule);
			sortUnassignedModules();

			Object[] args = { newAssignedModule.getModule().getKey().getName(), newAssignedModule.getModule().getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage("modulePicker", new MessageFormat(getBundle().getString("module.message.info.added")).format(args), Severity.INFO, false, false);
		}
	}

	public void refreshAvailableModules() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		List<AssignedModuleDTO> sourceModules = new ArrayList<>();
		// Get all available modules
		List<ModuleDTO> unassignedModules = service.listModules(domainSelector.getSelectedDomainName(), false);

		// Create an assigned module for every module that is not in the target list and add it to
		// the source list
		unassignedModules: for (ModuleDTO module : unassignedModules)
		{
			for (AssignedModuleDTO assignedModuleDTO : modules.getTarget())
			{
				if (assignedModuleDTO.getModule().getKey().equals(module.getKey()))
				{
					continue unassignedModules;
				}
			}
			sourceModules.add(createAssignedModule(module));
		}
		modules.setSource(sourceModules);
		sortUnassignedModules();

		logMessage("modulePicker", getBundle().getString("module.message.info.refreshedAvailable"), Severity.INFO, false, false);
	}

	public FreeTextType[] getFreeTextTypes()
	{
		return FreeTextType.values();
	}

	/**
	 * Generate module picklist with unassigend modules on the left and modules that are already
	 * assigend to the template on the right
	 *
	 * @param template
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 */
	public void loadModulesAndFreeTexts(ConsentTemplateDTO template) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		List<AssignedModuleDTO> sourceModules = new ArrayList<>();
		moduleExpirations = new HashMap<>();

		// Get all available modules
		List<ModuleDTO> allModules = service.listModules(domainSelector.getSelectedDomainName(), false);

		// Prepare assigned modules
		for (AssignedModuleDTO assignedModule : template.getAssignedModules())
		{
			allModules.remove(assignedModule.getModule());
			WebExpiration expiration = new WebExpiration();
			expiration.getEditExpiration().setExpirationProperties(assignedModule.getExpirationProperties());
			expiration.saveExpiration();
			moduleExpirations.put(assignedModule.getModule().getKey(), expiration);
		}

		// Prepare unassigend modules
		for (ModuleDTO module : allModules)
		{
			AssignedModuleDTO unassignedModule = createAssignedModule(module);
			sourceModules.add(unassignedModule);
			moduleExpirations.put(module.getKey(), new WebExpiration());
		}

		modules = new DualListModel<>(sourceModules, new ArrayList<>(template.getAssignedModules()));
		sortUnassignedModules();
		sortAssignedModules();

		// Create sorted list from freeTextDefDTOs
		freeTextDefs = new ArrayList<>(template.getFreeTextDefs());
		freeTextDefs.sort(Comparator.comparing(FreeTextDefDTO::getPos));
	}

	/**
	 * Apply default values depending on template type to the target modules
	 */
	public void onAssignModules()
	{
		if (!validateModules())
		{
			return;
		}

		for (AssignedModuleDTO module : modules.getTarget())
		{
			// Set defaults for newly assigned modules
			if (!moduleExpirations.containsKey(module.getModule().getKey()))
			{
				// Set default checkbox values
				List<ConsentStatus> displayCheckboxes = new ArrayList<>();
				if (editTemplate.getType() == null)
				{
					// have a nice and sunny day :)
				}
				else if (editTemplate.getType().equals(ConsentTemplateType.CONSENT))
				{
					displayCheckboxes.add(ConsentStatus.ACCEPTED);
					displayCheckboxes.add(ConsentStatus.DECLINED);
				}
				else if (editTemplate.getType().equals(ConsentTemplateType.REVOCATION))
				{
					displayCheckboxes.add(ConsentStatus.WITHDRAWN);
				}
				else if (editTemplate.getType().equals(ConsentTemplateType.REFUSAL))
				{
					displayCheckboxes.add(ConsentStatus.REFUSED);
				}
				module.setDisplayCheckboxes(displayCheckboxes);

				// Store in expiration Map
				moduleExpirations.put(module.getModule().getKey(), new WebExpiration());
			}
		}
	}

	/**
	 * Add all modules from target list to template
	 */
	private void prepareModulesAndFreeTextsForSaving()
	{
		int i = 0;
		editTemplate.getAssignedModules().clear();
		for (AssignedModuleDTO module : modules.getTarget())
		{
			// Assign order number
			module.setOrderNumber(i);
			i++;

			// Set expirationPropertioes
			WebExpiration expiration = moduleExpirations.getOrDefault(module.getModule().getKey(), new WebExpiration());
			module.setExpirationProperties(expiration.getSavedExpiration().getExpirationProperties());

			// Add module to template
			editTemplate.getAssignedModules().add(module);
		}

		// Save freeTextDefs List as Set in template and assign pos numbers
		i = 0;
		editTemplate.getFreeTextDefs().clear();
		for (FreeTextDefDTO freeTextDef : freeTextDefs)
		{
			// Assign pos number
			freeTextDef.setPos(i);
			i++;

			// Add freeTextDef to template
			editTemplate.getFreeTextDefs().add(freeTextDef);
		}
	}

	public void onNewFreeText()
	{
		FreeTextDefDTO freeText = new FreeTextDefDTO();
		freeText.setConverterString(getCommonBundle().getString("ui.date.pattern.date"));
		freeTextDefs.add(freeText);
	}

	public void removeFreeText(FreeTextDefDTO freeText)
	{
		freeTextDefs.remove(freeText);
	}

	public void onMoveFreeTextUp(Integer index)
	{
		if (index > 0)
		{
			Collections.swap(freeTextDefs, index, index - 1);
		}
	}

	public void onMoveFreeTextDown(Integer index)
	{
		if (freeTextDefs.size() > index)
		{
			Collections.swap(freeTextDefs, index, index + 1);
		}
	}

	public void onEditMapping()
	{
		try
		{
			templatesWithPolicies = service.getTemplatesWithPolicies(selectedTemplate.getAssignedModules()
					.stream()
					.flatMap(am -> am.getModule().getAssignedPolicies().stream().map(ap -> ap.getPolicy().getKey()))
					.collect(Collectors.toList()));
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onSaveMapping()
	{
		try
		{
			manager.updateConsentTemplateInUse(selectedTemplate);
			Object[] args = { selectedTemplate.getLabelOrName(), selectedTemplate.getVersionLabelOrVersion() };
			logMessage(new MessageFormat(getBundle().getString("template.message.info.mapping.updated")).format(args), Severity.INFO);
		}
		catch (UnknownDomainException | InvalidFreeTextException| InvalidParameterException | InvalidVersionException | UnknownConsentTemplateException | UnknownModuleException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public List<ConsentTemplateKeyDTO> getAvailableMappingOptions(String type)
	{
		List<ConsentTemplateKeyDTO> templateKeysForType = templates.stream().filter(t -> t.getType().equals(ConsentTemplateType.valueOf(type))).map(ConsentTemplateDTO::getKey).toList();
		return templatesWithPolicies.stream().filter(templateKeysForType::contains).collect(Collectors.toList());
	}

	public void setActiveAccordionTabs(String tabs)
	{
		// JSF needs this method, can be empty
	}

	public String getActiveAccordionTabs()
	{
		String active = "";
		if (!editTemplate.getFreeTextDefs().isEmpty())
		{
			active += "0,";
		}
		if (StringUtils.isNotEmpty(editTemplate.getComment()) || editTemplate.getExpirationProperties() != null
				|| StringUtils.isNotEmpty(editTemplate.getExternProperties()))
		{
			active += "1";
		}
		active = active.replaceAll(",$", "");
		if (StringUtils.isEmpty(active))
		{
			active = "-1";
		}
		return active;
	}

	public ConsentStatus[] getConsentStates()
	{
		return ConsentStatus.values();
	}

	public Object[] getMandatoryConsentStates(boolean mandatory)
	{
		List<ConsentStatus> states = new ArrayList<>();

		switch (editTemplate.getType())
		{
			case CONSENT:
			case REFUSAL:
				states.add(ConsentStatus.ACCEPTED);
				states.add(ConsentStatus.DECLINED);
				states.add(ConsentStatus.NOT_ASKED);
				states.add(ConsentStatus.NOT_CHOSEN);
				break;
			case REVOCATION:
				states.add(ConsentStatus.ACCEPTED);
				states.add(ConsentStatus.WITHDRAWN);
				states.add(ConsentStatus.NOT_ASKED);
				states.add(ConsentStatus.NOT_CHOSEN);
				break;
			default:
		}

		// Sort by ConsentStatusType
		states = states.stream().sorted(Comparator.comparing(s -> s.getConsentStatusType().name())).collect(Collectors.toList());

		// Remove unkown if module is mandatory
		if (mandatory)
		{
			return states.stream()
					.filter(s -> ConsentStatusType.UNKNOWN != s.getConsentStatusType())
					.toArray();
		}
		else
		{
			return states.toArray();
		}
	}

	private void loadTemplates()
	{
		try
		{
			templates = service.listConsentTemplates(domainSelector.getSelectedDomainName(), false);
		}
		catch (Exception e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedTemplate = null;
	}

	private boolean validateModules()
	{
		for (int i = 0; i < modules.getTarget().size(); i++)
		{
			for (int j = i + 1; j < modules.getTarget().size(); j++)
			{
				// check, if different version of same module
				if (modules.getTarget().get(i).getModule().getKey().getName().equals(modules.getTarget().get(j).getModule().getKey().getName()))
				{
					Object[] args = { modules.getTarget().get(i).getModule().getKey().getName() };
					logMessage("modulePicker", new MessageFormat(getBundle().getString("template.message.error.duplicateModule")).format(args), Severity.WARN, false, true);
					FacesContext.getCurrentInstance().validationFailed();
					return false;
				}
				// check for duplicate policies
				for (AssignedPolicyDTO ap : modules.getTarget().get(i).getModule().getAssignedPolicies())
				{
					if (modules.getTarget().get(j).getModule().getAssignedPolicies().contains(ap))
					{
						Object[] args = { ap.getPolicy().getLabel() };
						logMessage("modulePicker", new MessageFormat(getBundle().getString("template.message.error.duplicatePolicy")).format(args), Severity.WARN, false, true);
						FacesContext.getCurrentInstance().validationFailed();
						return false;
					}
				}
			}
		}
		return true;
	}

	public List<ConsentTemplateDTO> getTemplates()
	{
		return templates;
	}

	public ConsentTemplateDTO getSelectedTemplate()
	{
		return selectedTemplate;
	}

	public void setSelectedTemplate(ConsentTemplateDTO selectedTemplate)
	{
		this.selectedTemplate = selectedTemplate;
	}

	public DualListModel<AssignedModuleDTO> getModules()
	{
		return modules;
	}

	public void setModules(DualListModel<AssignedModuleDTO> modules)
	{
		this.modules = modules;
	}

	public String getModulesDuplicateVersion()
	{
		return modulesDuplicateVersion;
	}

	public void setModulesDuplicateVersion(String modulesDuplicateVersion)
	{
		this.modulesDuplicateVersion = modulesDuplicateVersion;
	}

	public boolean getCanDuplicateModules()
	{
		return selectedTemplate != null && !selectedTemplate.getAssignedModules().isEmpty();
	}

	public ConsentTemplateDTO getEditTemplate()
	{
		return editTemplate;
	}

	public WebExpiration getTemplateExpiration()
	{
		return templateExpiration;
	}

	public Map<ModuleKeyDTO, WebExpiration> getModuleExpirations()
	{
		return moduleExpirations;
	}

	public TemplateTree getTemplateTree()
	{
		return templateTree;
	}

	public boolean isPrintOptionQrCode()
	{
		return printOptionQrCode;
	}

	public void setPrintOptionQrCode(boolean printOptionQrCode)
	{
		this.printOptionQrCode = printOptionQrCode;
	}

	public List<FreeTextDefDTO> getFreeTextDefs()
	{
		return freeTextDefs;
	}

	public void setFreeTextDefs(List<FreeTextDefDTO> freeTextDefs)
	{
		this.freeTextDefs = freeTextDefs;
	}

	private void sortUnassignedModules()
	{
		modules.getSource().sort(Comparator.comparing((AssignedModuleDTO m) -> m.getModule().getKey().getName().toLowerCase()));
	}

	private void sortAssignedModules()
	{
		modules.getTarget().sort(Comparator.comparing(AssignedModuleDTO::getOrderNumber));
	}

	private AssignedModuleDTO createAssignedModule(ModuleDTO dto)
	{
		AssignedModuleDTO assignedModule = new AssignedModuleDTO();
		assignedModule.setModule(dto);
		assignedModule.setMandatory(false);
		assignedModule.setDisplayCheckboxes(new ArrayList<>(Arrays.asList(getConsentStates())));
		return assignedModule;
	}

	public void setModuleController(ModuleController moduleController)
	{
		this.moduleController = moduleController;
	}

	public String getEditTemplateSanitizedTitle()
	{
		return text.sanitizeRelaxed(editTemplate.getTitle());
	}

	public void setEditTemplateSanitizedTitle(String title)
	{
		editTemplate.setTitle(title);
	}

	public String getEditTemplateSanitizedHeader()
	{
		return text.sanitizeRelaxed(editTemplate.getHeader());
	}

	public void setEditTemplateSanitizedHeader(String header)
	{
		editTemplate.setHeader(header);
	}

	public String getEditTemplateSanitizedFooter()
	{
		return text.sanitizeRelaxed(editTemplate.getFooter());
	}

	public void setEditTemplateSanitizedFooter(String footer)
	{
		editTemplate.setFooter(footer);
	}
}
