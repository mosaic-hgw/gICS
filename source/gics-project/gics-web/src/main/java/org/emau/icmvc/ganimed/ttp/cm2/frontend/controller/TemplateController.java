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

import java.io.Serial;
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

import jakarta.annotation.PostConstruct;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.IllegalCompositionException;
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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.TimeProperty;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.TemplateTree;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.icmvc.ttp.web.controller.UserSettingsBean;
import org.primefaces.model.DualListModel;

/**
 * Backing Bean for Consent Template View
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@Named("templateController")
public class TemplateController extends AbstractConsentController
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
	private TimeProperty templateExpiration = new TimeProperty();
	private TimeProperty templateValidFrom = new TimeProperty();

	// Edit Modules in template
	private DualListModel<AssignedModuleDTO> modulesPicker;
	private Map<ModuleKeyDTO, TimeProperty> moduleExpirations = new HashMap<>();
	private String modulesDuplicateVersion;
	private List<AssignedModuleDTO> modules;

	// Edit Freetexts in template
	private List<FreeTextDefDTO> freeTextDefs;

	// Edit Mappings
	private List<ConsentTemplateKeyDTO> templatesWithPolicies = new ArrayList<>();

	@Inject
	@ManagedProperty(value = "#{moduleController}")
	private ModuleController moduleController;
	@Inject
	@ManagedProperty(value = "#{userSettingsBean}")
	UserSettingsBean userSettingsBean;

	@PostConstruct
	protected void init()
	{
		loadTemplates();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (sessionMap.containsKey(SessionMapKeys.PRINT_TREE))
		{
			templateTree = (TemplateTree) sessionMap.get(SessionMapKeys.PRINT_TREE);
		}

		userSettingsBean.setDefaultColumns("templates", new HashSet<>(Set.of(1, 3, 7, 8)));

		pageMode = PageMode.READ;
	}

	public void onNewTemplate() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.NEW;
		editTemplate = new ConsentTemplateDTO(new ConsentTemplateKeyDTO());
		editTemplate.setFreeTextDefs(new HashSet<>());
		editTemplate.getKey().setDomainName(domainSelector.getSelectedDomainName());
		editTemplate.setAssignedModules(new HashSet<>());
		templateExpiration = new TimeProperty();
		templateValidFrom = new TimeProperty();
		loadModulesAndFreeTexts(editTemplate);
	}

	public void onEditTemplate(ConsentTemplateDTO template) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.EDIT;
		editTemplate = template;
		templateExpiration.getEditTimeProperty().setExpirationProperties(editTemplate.getExpirationProperties());
		templateValidFrom.getEditTimeProperty().setValidFromProperties(editTemplate.getValidFromProperties());
		loadModulesAndFreeTexts(editTemplate);

		if (template.getFinalised())
		{
			logMessage(getBundle().getString("template.message.info.isFinal"), Severity.INFO, false);
		}
	}

	public void onDuplicateTemplate(ConsentTemplateDTO template, boolean duplicateModules)
			throws UnknownDomainException, InvalidVersionException, DuplicateEntryException, UnknownPolicyException, RequirementsNotFullfilledException, InvalidParameterException,
			IllegalCompositionException
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
				ModuleDTO moduleDTO = moduleController.onDuplicateModule(original.getModule(), true);
				try
				{
					getManager().addModule(moduleDTO, false);
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
		editTemplate.getKey().setName(null);
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
		templateExpiration.getEditTimeProperty().setExpirationProperties(template.getExpirationProperties());
		templateValidFrom.getEditTimeProperty().setValidFromProperties(template.getValidFromProperties());
		loadModulesAndFreeTexts(editTemplate);
	}

	public void onSaveTemplate()
	{
		prepareModulesAndFreeTextsForSaving();
		editTemplate.setExpirationProperties(templateExpiration.getEditTimeProperty().getExpirationProperties());
		editTemplate.setValidFromProperties(templateValidFrom.getEditTimeProperty().getValidFromProperties());
		Object[] args = { editTemplate.getLabel(), editTemplate.getVersionLabelAndVersion(), domainSelector.getSelectedDomainName() };

		if (pageMode == PageMode.NEW)
		{
			try
			{
				// Finalise domain to be able to add template
				if (!domainSelector.getSelectedDomain().getFinalised())
				{
					getManager().finaliseDomain(domainSelector.getSelectedDomainName());
					domainSelector.loadDomains();
					domainSelector.setSelectedDomain(domainSelector.getSelectedDomainName());
				}

				// Add template
				if (StringUtils.isEmpty(editTemplate.getKey().getName()))
				{
					editTemplate.getKey().setName(editTemplate.getLabel());
				}
				editTemplate.getKey().setName(editTemplate.getKey().getName().trim());
				getManager().addConsentTemplate(editTemplate, false);
				logMessage(new MessageFormat(getBundle().getString("template.message.info.added")).format(args), Severity.INFO);
				loadTemplates();
				pageMode = PageMode.READ;
			}
			catch (DuplicateEntryException e)
			{
				logMessage(getBundle().getString("template.message.error.duplicate"), Severity.WARN);
			}
			catch (IllegalCompositionException e)
			{
				Object[] exceptionArgs = { e.getIllegalItem() };
				logMessage(new MessageFormat(getBundle().getString("template.message.error.composition." + e.getIllegalItemType().name())).format(exceptionArgs), Severity.WARN);
			}
			catch (InvalidVersionException e)
			{
				logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.WARN);
			}
			catch (UnknownDomainException | UnknownModuleException | FreeTextConverterStringException | InvalidPropertiesException |
				   RequirementsNotFullfilledException | InvalidParameterException | UnknownConsentTemplateException | VersionConverterClassException e)
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
					getManager().updateConsentTemplateInUse(editTemplate);
				}
				else
				{
					getManager().updateConsentTemplate(editTemplate, false);
				}
				logMessage(new MessageFormat(getBundle().getString("template.message.info.updated")).format(args), Severity.INFO);
				loadTemplates();
				pageMode = PageMode.READ;
			}
			catch (InvalidVersionException e)
			{
				logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.WARN);
			}
			catch (IllegalCompositionException e)
			{
				Object[] exceptionArgs = { e.getIllegalItem() };
				logMessage(new MessageFormat(getBundle().getString("template.message.error.composition." + e.getIllegalItemType().name())).format(exceptionArgs), Severity.WARN);
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
			getManager().finaliseTemplate(template.getKey(), true);
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
			getManager().deleteConsentTemplate(template.getKey());
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

	public String onFillInTemplate(ConsentTemplateDTO consentTemplateDTO)
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.FILL_TEMPLATE, consentTemplateDTO.getKey());

		return "/html/internal/consents.xhtml?templateType=" + consentTemplateDTO.getType().name() + "&faces-redirect=true";
	}

	public String onPrintTemplate()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TEMPLATES, Collections.singletonList(selectedTemplate.getKey()));
		sessionMap.put(SessionMapKeys.PRINT_OPTION_QR_CODE, printOptionQrCode);

		return "/html/internal/consents.xhtml?templateType=" + selectedTemplate.getType().name() + "&print=true&faces-redirect=true";
	}

	public String onPrintTemplateWithSignerIds()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TEMPLATES, Collections.singletonList(selectedTemplate.getKey()));
		sessionMap.put(SessionMapKeys.PRINT_OPTION_QR_CODE, printOptionQrCode);

		return "/html/internal/print.xhtml";
	}

	public String onPrintTree()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (templateTree != null)
		{
			sessionMap.put(SessionMapKeys.PRINT_TREE, templateTree);
		}
		return "/html/internal/printTree.xhtml?faces-redirect=true&print=true";
	}

	public void addNewestModule() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		if (!FacesContext.getCurrentInstance().isValidationFailed())
		{
			List<ModuleDTO> newModules = getService().listModules(domainSelector.getSelectedDomainName(), false);
			AssignedModuleDTO newAssignedModule = createAssignedModule(newModules.stream().max(Comparator.comparing(ModuleDTO::getCreationDate)).orElse(null));
			modulesPicker.getTarget().add(0, newAssignedModule);
			sortUnassignedModules();

			Object[] args = { newAssignedModule.getModule().getKey().getName(), newAssignedModule.getModule().getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage("modulePicker", new MessageFormat(getBundle().getString("module.message.info.added")).format(args), Severity.INFO, false, false);
		}
	}

	public void refreshAvailableModules() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		List<AssignedModuleDTO> sourceModules = new ArrayList<>();
		// Get all available modules
		List<ModuleDTO> unassignedModules = getService().listModules(domainSelector.getSelectedDomainName(), false);

		// Create an assigned module for every module that is not in the target list and add it to
		// the source list
		Set<ModuleKeyDTO> targetKeys = modulesPicker.getTarget().stream().map(assignedModuleDTO -> assignedModuleDTO.getModule().getKey()).collect(Collectors.toSet());
		for (ModuleDTO module : unassignedModules)
		{
			if (!targetKeys.contains(module.getKey()))
				sourceModules.add(createAssignedModule(module));
		}
		modulesPicker.setSource(sourceModules);
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
		List<ModuleDTO> allModules = getService().listModules(domainSelector.getSelectedDomainName(), false);

		// Prepare assigned modules
		for (AssignedModuleDTO assignedModule : template.getAssignedModules())
		{
			allModules.remove(assignedModule.getModule());
			TimeProperty expiration = new TimeProperty();
			expiration.getEditTimeProperty().setExpirationProperties(assignedModule.getExpirationProperties());
			expiration.saveExpirationProperty();
			moduleExpirations.put(assignedModule.getModule().getKey(), expiration);
		}

		// Prepare unassigend modules
		for (ModuleDTO module : allModules)
		{
			AssignedModuleDTO unassignedModule = createAssignedModule(module);
			sourceModules.add(unassignedModule);
		}

		modules = template.getAssignedModules().stream().sorted(Comparator.comparing(AssignedModuleDTO::getOrderNumber)).collect(Collectors.toList());
		modulesPicker = new DualListModel<>(sourceModules, new ArrayList<>(modules));
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

		modules.clear();
		modules.addAll(modulesPicker.getTarget());

		for (AssignedModuleDTO module : modules)
		{
			// Set defaults for newly assigned modules
			if (!moduleExpirations.containsKey(module.getModule().getKey()))
			{
				// Set default checkbox values
				List<ConsentStatus> displayCheckboxes = new ArrayList<>();
				if (ConsentTemplateType.CONSENT.equals(editTemplate.getType()))
				{
					displayCheckboxes.add(ConsentStatus.ACCEPTED);
					displayCheckboxes.add(ConsentStatus.DECLINED);
				}
				else if (ConsentTemplateType.CONSENT_OPT_OUT.equals(editTemplate.getType()))
				{
					module.setDefaultConsentStatus(ConsentStatus.ACCEPTED_OPT_OUT);
				}
				else if (ConsentTemplateType.REVOCATION.equals(editTemplate.getType()))
				{
					displayCheckboxes.add(ConsentStatus.WITHDRAWN);
				}
				else if (ConsentTemplateType.REFUSAL.equals(editTemplate.getType()))
				{
					displayCheckboxes.add(ConsentStatus.REFUSED);
				}
				else if (ConsentTemplateType.OBJECTION.equals(editTemplate.getType()))
				{
					displayCheckboxes.add(ConsentStatus.OBJECTED);
				}
				module.setDisplayCheckboxes(displayCheckboxes);

				// Store in expiration Map
				moduleExpirations.put(module.getModule().getKey(), new TimeProperty());
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
		for (AssignedModuleDTO module : modules)
		{
			// Assign order number
			module.setOrderNumber(i);
			i++;

			// Set expirationPropertioes
			TimeProperty expiration = moduleExpirations.getOrDefault(module.getModule().getKey(), new TimeProperty());
			module.setExpirationProperties(expiration.getSavedTimeProperty().getExpirationProperties());

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
			templatesWithPolicies = getService().getTemplatesWithPolicies(selectedTemplate.getAssignedModules()
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
			getManager().updateConsentTemplateInUse(selectedTemplate);
			Object[] args = { selectedTemplate.getLabelOrName(), selectedTemplate.getVersionLabelOrVersion() };
			logMessage(new MessageFormat(getBundle().getString("template.message.info.mapping.updated")).format(args), Severity.INFO);
			loadTemplates();
		}
		catch (UnknownDomainException | InvalidFreeTextException | InvalidParameterException | InvalidVersionException | UnknownConsentTemplateException | UnknownModuleException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public List<ConsentTemplateKeyDTO> getAvailableMappingOptions(String type)
	{
		List<ConsentTemplateKeyDTO> templateKeysForType = templates.stream().filter(t -> t.getType().equals(ConsentTemplateType.valueOf(type))).map(ConsentTemplateDTO::getKey).toList();
		return templatesWithPolicies.stream().filter(templateKeysForType::contains).collect(Collectors.toList());
	}

	public List<ConsentTemplateType> getAvailableMappingTypes(ConsentTemplateType type)
	{
		return Arrays.stream(ConsentTemplateType.values())
				.filter(t -> getSelectedDomain().getConfig().getApplicationConfig().containsTemplateType(t.name()))
				.filter(t -> !type.equals(t))
				.collect(Collectors.toList());
	}

	/**
	 * redirect to export page and auto select template as item to export
	 *
	 * @param templateKey
	 * 		Template Key which shall be selected in export view
	 */
	public String onExportTemplate(ConsentTemplateKeyDTO templateKey)
	{
		return "batch.xhtml?faces-redirect=true&"
				+ BatchController.MODE + "=" + BatchController.MODE_EXPORT + "&"
				+ BatchController.TEMPLATE_NAME + "=" + templateKey.getName() + "&"
				+ BatchController.TEMPLATE_VERSION + "=" + templateKey.getVersion() + "&"
				+ BatchController.REFERER + "=" + "templates.xhtml";
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
			case CONSENT, REFUSAL:
				states.add(ConsentStatus.ACCEPTED);
				states.add(ConsentStatus.DECLINED);
				states.add(ConsentStatus.NOT_ASKED);
				states.add(ConsentStatus.NOT_CHOSEN);
				break;
			case CONSENT_OPT_OUT:
				states.add(ConsentStatus.ACCEPTED_OPT_OUT);
				break;
			case REVOCATION:
				states.add(ConsentStatus.WITHDRAWN);
				states.add(ConsentStatus.ACCEPTED);
				states.add(ConsentStatus.NOT_ASKED);
				states.add(ConsentStatus.NOT_CHOSEN);
				states.add(ConsentStatus.OBJECTED);
				break;
			case OBJECTION:
				states.add(ConsentStatus.NOT_ASKED);
				states.add(ConsentStatus.NOT_CHOSEN);
				states.add(ConsentStatus.OBJECTED);
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
			templates = getService().listConsentTemplates(domainSelector.getSelectedDomainName(), false).stream().filter(t -> getDomainTemplateTypes().contains(t.getType()))
					.collect(Collectors.toList());
		}
		catch (Exception e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedTemplate = null;
	}

	private boolean validateModules()
	{
		for (int i = 0; i < modulesPicker.getTarget().size(); i++)
		{
			for (int j = i + 1; j < modulesPicker.getTarget().size(); j++)
			{
				// check, if different version of same module
				if (modulesPicker.getTarget().get(i).getModule().getKey().getName().equals(modulesPicker.getTarget().get(j).getModule().getKey().getName()))
				{
					Object[] args = { modulesPicker.getTarget().get(i).getModule().getKey().getName() };
					logMessage("modulePicker", new MessageFormat(getBundle().getString("template.message.error.duplicateModule")).format(args), Severity.WARN, false, true);
					FacesContext.getCurrentInstance().validationFailed();
					return false;
				}
				// check for duplicate policies
				for (AssignedPolicyDTO ap : modulesPicker.getTarget().get(i).getModule().getAssignedPolicies())
				{
					if (modulesPicker.getTarget().get(j).getModule().getAssignedPolicies().contains(ap))
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

	public DualListModel<AssignedModuleDTO> getModulesPicker()
	{
		return modulesPicker;
	}

	public void setModulesPicker(DualListModel<AssignedModuleDTO> modulesPicker)
	{
		this.modulesPicker = modulesPicker;
	}

	public List<AssignedModuleDTO> getModules()
	{
		return modules;
	}

	public void setModules(List<AssignedModuleDTO> modules)
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

	public TimeProperty getTemplateExpiration()
	{
		return templateExpiration;
	}

	public TimeProperty getTemplateValidFrom()
	{
		return templateValidFrom;
	}

	public Map<ModuleKeyDTO, TimeProperty> getModuleExpirations()
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
		modulesPicker.getSource().sort(Comparator.comparing((AssignedModuleDTO m) -> m.getModule().getKey().getName().toLowerCase()));
	}

	private void sortAssignedModules()
	{
		modulesPicker.getTarget().sort(Comparator.comparing(AssignedModuleDTO::getOrderNumber));
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
