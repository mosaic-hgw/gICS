package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.xml.bind.DatatypeConverter;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.TemplateTreeNode;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

/**
 * Backing Bean for Consent Template View
 * 
 * @author Arne Blumentritt
 * 
 */
@ManagedBean(name = "templateController")
@ViewScoped
public class TemplateController extends AbstractConsentController
{

	private List<ConsentTemplateDTO> templates;
	private ConsentTemplateDTO selectedTemplate;
	private ConsentTemplateDTO editTemplate;
	private Boolean editMode;

	private DualListModel<AssignedModuleDTO> modules;

	private TreeNode templateTree;

	@PostConstruct
	protected void init()
	{
		loadTemplates();
	}

	public void onTemplateSelect(SelectEvent event)
	{
		selectedTemplate = (ConsentTemplateDTO) event.getObject();
	}

	public void onNewTemplate()
	{
		editMode = true;
		editTemplate = new ConsentTemplateDTO(new ConsentTemplateKeyDTO());
		editTemplate.setFreeTextDefs(new ArrayList<FreeTextDefDTO>());
		editTemplate.getKey().setDomainName(domainSelector.getSelectedDomainName());
		editTemplate.setAssignedModules(new ArrayList<AssignedModuleDTO>());
		loadModules(editTemplate);
	}

	public void onAddTemplate()
	{
		try
		{
			this.generateOrderNumbers();
			cmManager.addConsentTemplate(editTemplate);
			Object[] args = { editTemplate.getKey().getName(), editTemplate.getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage(new MessageFormat(getBundle().getString("template.message.info.added")).format(args), Severity.INFO);
			loadTemplates();
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("template.message.error.duplicate"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.ERROR);
		}
		catch (UnknownDomainException | UnknownModuleException | VersionConverterClassException | FreeTextConverterStringException | InvalidPropertiesException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onEditTemplate(ConsentTemplateDTO template)
	{
		editMode = true;
		editTemplate = template;
		loadModules(editTemplate);
	}

	public void onCancelEdit()
	{
		editMode = false;
	}

	public void onShowTree(ConsentTemplateDTO template)
	{
		logger.debug("Open template tree for template: " + template.getKey().toString());

		templateTree = new DefaultTreeNode(new TemplateTreeNode(template.getKey()), null);
		templateTree.setExpanded(true);

		for (AssignedModuleDTO module : template.getAssignedModules())
		{
			TreeNode moduleNode = new DefaultTreeNode(new TemplateTreeNode(module.getModule().getKey(), module.getMandatory()), templateTree);
			moduleNode.setExpanded(true);

			List<PolicyDTO> policies = module.getModule().getPolicies();
			for (PolicyDTO p : policies)
			{
				TreeNode polNode = new DefaultTreeNode(new TemplateTreeNode(p.getKey()), moduleNode);
				moduleNode.getChildren().add(polNode);
			}

			templateTree.getChildren().add(moduleNode);
		}
	}

	public TreeNode getTemplateTree()
	{
		return templateTree;
	}

	public void onUpdateTemplate()
	{
		try
		{
			this.generateOrderNumbers();
			cmManager.addConsentTemplate(editTemplate);
			Object[] args = { editTemplate.getKey().getName(), editTemplate.getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage(new MessageFormat(getBundle().getString("template.message.info.added")).format(args), Severity.INFO);
			loadTemplates();
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("template.message.error.duplicateUpdate"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			logMessage(getBundle().getString("template.message.error.versionFormat"), Severity.ERROR);
		}
		catch (UnknownDomainException | UnknownModuleException | VersionConverterClassException | FreeTextConverterStringException | InvalidPropertiesException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDeleteTemplate(ConsentTemplateDTO template)
	{

		try
		{
			Object[] args = { template.getKey().getName(), template.getKey().getVersion(), domainSelector.getSelectedDomainName() };
			cmManager.deleteConsentTemplate(template.getKey());
			logMessage(new MessageFormat(getBundle().getString("template.message.info.deleted")).format(args), Severity.INFO);
			loadTemplates();
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("template.message.error.deleteInUse"), Severity.ERROR);
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public String onPrintTemplate()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("printTemplate", selectedTemplate.getKey());

		return "/html/internal/consents.xhml?templateType=" + selectedTemplate.getType().name() + "&print=true&faces-redirect=true";
	}

	public String onPrintTemplateWithSignerIds()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("printTemplate", selectedTemplate.getKey());

		return "/html/internal/print.xhml";
	}

	public void refreshAvailableModules() throws UnknownDomainException, VersionConverterClassException, InvalidVersionException
	{
		List<ModuleDTO> newModules = cmManager.listModules(domainSelector.getSelectedDomainName());
		AssignedModuleDTO newAssignedModule = new AssignedModuleDTO();
		newAssignedModule.setModule(newModules.get(newModules.size() - 1));
		newAssignedModule.setMandatory(false);
		newAssignedModule.setDisplayCheckboxes(new ArrayList<ConsentStatus>(Arrays.asList(getConsentStates())));
		modules.getSource().add(0, newAssignedModule);
		sortModules();
	}

	public FreeTextType[] getFreeTextTypes()
	{
		return FreeTextType.values();
	}

	/**
	 * updates the Model for the module picklist with data from the db and the assignedModules of
	 * the give template.
	 * 
	 * @param template
	 */
	public void loadModules(ConsentTemplateDTO template)
	{
		List<AssignedModuleDTO> sourceModules = new ArrayList<AssignedModuleDTO>();
		try
		{
			List<ModuleDTO> unassigned;

			unassigned = cmManager.listModules(domainSelector.getSelectedDomainName());

			for (AssignedModuleDTO module : template.getAssignedModules())
			{
				unassigned.remove(module.getModule());
			}
			/**
			 * umwandeln der moduleDTO in AssignedModuleDTO
			 */
			for (ModuleDTO module : unassigned)
			{
				AssignedModuleDTO assignedModule = new AssignedModuleDTO();
				assignedModule.setModule(module);
				assignedModule.setMandatory(false);
				assignedModule.setDisplayCheckboxes(new ArrayList<ConsentStatus>(Arrays.asList(getConsentStates())));
				sourceModules.add(assignedModule);
			}
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		modules = new DualListModel<AssignedModuleDTO>(sourceModules, new ArrayList<AssignedModuleDTO>(template.getAssignedModules()));
		sortModules();
	}

	/**
	 * Listener for change of the module selection. Replaces the assigned modules of selected
	 * template with the new module selection. Configurations from the old selection(mandatory,
	 * defaultConsentStatus,... are copied
	 */
	public void onModulesSubmit()
	{
		for (AssignedModuleDTO module : modules.getTarget())
		{
			boolean found = false;
			for (AssignedModuleDTO templateModule : editTemplate.getAssignedModules())
			{
				if (module.getModule().getKey().equals(templateModule.getModule().getKey()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
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
					displayCheckboxes.add(ConsentStatus.REVOKED);
				}
				else if (editTemplate.getType().equals(ConsentTemplateType.REFUSAL))
				{
					displayCheckboxes.add(ConsentStatus.REFUSED);
				}
				module.setDisplayCheckboxes(displayCheckboxes);
			}
		}
		editTemplate.setAssignedModules(modules.getTarget());
	}

	public boolean templateHasConsents()
	{
		FacesContext context = FacesContext.getCurrentInstance();
		boolean result = false;
		if (selectedTemplate != null)
		{
			try
			{
				result = cmManager.getAllConsentsForConsentTemplate(selectedTemplate.getKey()).size() != 0;
			}
			catch (Exception e)
			{
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
				logger.error("exception while getting consents for: " + selectedTemplate.getKey(), e);
			}
		}

		return result;
	}

	public StreamedContent getTemplatePDF() throws IOException
	{
		FacesContext context = FacesContext.getCurrentInstance();
		StreamedContent consentPDFStream;
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE)
		{
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			consentPDFStream = new DefaultStreamedContent();
		}
		else
		{
			byte[] out = DatatypeConverter.parseBase64Binary(selectedTemplate.getScanBase64());
			consentPDFStream = new DefaultStreamedContent(new ByteArrayInputStream(out));
		}
		return consentPDFStream;
	}

	private void generateOrderNumbers()
	{
		int i = 0;
		for (AssignedModuleDTO module : editTemplate.getAssignedModules())
		{
			module.setOrderNumber(i);
			i++;
		}
	}

	public void onNewFreeText()
	{
		FreeTextDefDTO freeText = new FreeTextDefDTO();
		freeText.setConverterString(getBundle().getString("common.date.pattern"));
		editTemplate.getFreeTextDefs().add(freeText);
	}

	public void removeFreeText(FreeTextDefDTO freeText)
	{
		editTemplate.getFreeTextDefs().remove(freeText);
	}

	private void loadTemplates()
	{
		try
		{
			templates = cmManager.listConsentTemplates(domainSelector.getSelectedDomainName());
		}
		catch (Exception e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedTemplate = null;
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
		for (int i = 0; i < modules.getTarget().size(); i++)
		{
			for (int j = i + 1; j < modules.getTarget().size(); j++)
			{
				// check, if different version of same module
				if (modules.getTarget().get(i).getModule().getKey().getName().equals(modules.getTarget().get(j).getModule().getKey().getName()))
				{
					Object[] args = { modules.getTarget().get(i).getModule().getKey().getName() };
					logMessage(new MessageFormat(getBundle().getString("template.message.error.duplicateModule")).format(args), Severity.ERROR, false);
					return;
				}
				// check for duplicate policies
				for (PolicyDTO policy : modules.getTarget().get(i).getModule().getPolicies())
				{
					if (modules.getTarget().get(j).getModule().getPolicies().contains(policy))
					{
						Object[] args = { policy.getKey().getName() };
						logMessage(new MessageFormat(getBundle().getString("template.message.error.duplicatePolicy")).format(args), Severity.ERROR, false);
						return;
					}
				}
			}
		}
		this.modules = modules;
	}

	public ConsentTemplateDTO getEditTemplate()
	{
		return editTemplate;
	}

	public Boolean getEditMode()
	{
		return editMode;
	}

	private void sortModules()
	{
		Collections.sort(modules.getSource(), new Comparator<AssignedModuleDTO>() {
			public int compare(AssignedModuleDTO m1, AssignedModuleDTO m2)
			{
				return m1.getModule().getKey().getName().toLowerCase().compareTo(m2.getModule().getKey().getName().toLowerCase());
			}
		});
	}
}
