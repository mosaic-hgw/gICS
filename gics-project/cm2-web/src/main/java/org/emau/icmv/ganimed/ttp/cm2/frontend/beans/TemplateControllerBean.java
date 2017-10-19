package org.emau.icmv.ganimed.ttp.cm2.frontend.beans;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.xml.bind.DatatypeConverter;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.slf4j.LoggerFactory;

/**
 * BAcking Bean for Consent Template View
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "TemplateController")
@ViewScoped
public class TemplateControllerBean extends CMController {
	private TreeNode root;
	private DomainDTO selectedDomain;
	private List<ConsentTemplateDTO> templates;
	private ConsentTemplateDTO selectedTemplate;
	/**
	 * model for Module selection dialog (picklist).
	 */
	private DualListModel<AssignedModuleDTO> modules;

	private FreeTextDefDTO freeText;

	private boolean tableDisabled = false;

	public String getFreeTextTypeLabel(FreeTextType type) {
		switch (type) {
			case Date:
				return "Date";
			case Double:
				return "Double";
			case Integer:
				return "Integer";
			case String:
				return "Text";
			default:
				return "";
		}
	}

	public FreeTextType[] getFreeTextTypes() {
		return FreeTextType.values();
	}

	@Override
	protected void onInit() {
		logger = LoggerFactory.getLogger(TemplateControllerBean.class);
		root = new DefaultTreeNode("root", null);
		templates = new ArrayList<ConsentTemplateDTO>();
		for (DomainDTO domain : domains) {
			new DefaultTreeNode(domain.getName(), root);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("template tab initialised");
		}
	}

	/**
	 * updates the Model for the module picklist with data from the db and the assignedModules of the give template.
	 * 
	 * @param template
	 */
	public void updateModuleList(ConsentTemplateDTO template) {
		FacesContext context = FacesContext.getCurrentInstance();
		List<AssignedModuleDTO> sourceModules = new ArrayList<AssignedModuleDTO>();
		try {
			List<ModuleDTO> unassigned = cmManager.listModules(selectedDomain.getName());
			for (AssignedModuleDTO module : template.getAssignedModules()) {
				unassigned.remove(module.getModule());
			}
			/**
			 * umwandeln der moduleDTO in AssignedModuleDTO
			 */
			for (ModuleDTO module : unassigned) {
				AssignedModuleDTO assignedModule = new AssignedModuleDTO();
				assignedModule.setModule(module);
				assignedModule.setMandatory(false);
				assignedModule.setDisplayCheckboxes(new ArrayList<ConsentStatus>(Arrays.asList(getConsentStates())));
				sourceModules.add(assignedModule);
			}
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while updateting modules for: " + selectedTemplate.getKey(), e);
		}
		modules = new DualListModel<AssignedModuleDTO>(sourceModules, new ArrayList<AssignedModuleDTO>(template.getAssignedModules()));

	}

	public void onDomainSelect(SelectEvent event) {
		selectedDomain = ((DomainDTO) event.getObject());
		refresh();
		if (logger.isDebugEnabled()) {
			logger.debug("domain " + selectedDomain + " selected");
		}
	}

	public void onAddConsentTemplate() {
		editMode = false;
		selectedTemplate = new ConsentTemplateDTO(new ConsentTemplateKeyDTO());
		selectedTemplate.setFreeTextDefs(new ArrayList<FreeTextDefDTO>());
		selectedTemplate.getKey().setDomainName(selectedDomain.getName());
		selectedTemplate.setAssignedModules(new ArrayList<AssignedModuleDTO>());
		tableDisabled = true;
		updateModuleList(selectedTemplate);

	}

	public void onConsentTemplateSelect(SelectEvent event) {
		selectedTemplate = (ConsentTemplateDTO) event.getObject();
	}

	public void onEditTemplate() {
		editMode = true;
		tableDisabled = true;
		updateModuleList(selectedTemplate);

	}

	/**
	 * Listener for change of the module selection. Replaces the assigned modules of selected template with the new module selection. Configurations from the old selection(mandatory,
	 * defaultConsentStatus,... are copied
	 */
	public void onModulesSubmit() {
		// copy old configuration into the new assignedModuleDTO from new selection
		for (int i = 0; i < modules.getTarget().size(); i++) {
			AssignedModuleDTO selectedModule = modules.getTarget().get(i);
			selectedModule.setOrderNumber(i);
			for (AssignedModuleDTO templateModule : selectedTemplate.getAssignedModules()) {
				if (selectedModule.getModule().getKey().equals(templateModule.getModule().getKey())) {
					selectedModule.setMandatory(templateModule.getMandatory());
					selectedModule.setDisplayCheckboxes(templateModule.getDisplayCheckboxes());
					selectedModule.setDefaultConsentStatus(templateModule.getDefaultConsentStatus());
					selectedTemplate.getAssignedModules().remove(templateModule);
					break;
				}
			}
		}
		// replace assigne modules
		selectedTemplate.setAssignedModules(modules.getTarget());
	}

	public boolean templateHasConsents() {
		FacesContext context = FacesContext.getCurrentInstance();
		boolean result = false;
		if (selectedTemplate != null) {
			try {
				result = cmManager.getAllConsentsForConsentTemplate(selectedTemplate.getKey()).size() != 0;
			} catch (Exception e) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
				logger.error("exception while getting consents for: " + selectedTemplate.getKey(), e);
			}
		}

		return result;
	}

	public void onDeleteTemplate() {
		FacesContext context = FacesContext.getCurrentInstance();
		String domainName = (selectedDomain.getLabel() != null && !selectedDomain.getLabel().isEmpty())? selectedDomain.getLabel() : selectedDomain.getName();
		Object[] args = { selectedTemplate.getKey().getName(), selectedTemplate.getKey().getVersion(), domainName };
		try {
			cmManager.deleteConsentTemplate(selectedTemplate.getKey());
			if (logger.isInfoEnabled()) {
				logger.info("template with key:" + selectedTemplate.getKey() + " deleted");
			}
			refresh();
			context.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("template.message.templateDeleted"))
							.format(args), ""));
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while deleting: " + selectedTemplate.getKey(), e);
		}
	}

	public StreamedContent getTemplatePDF() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		StreamedContent consentPDFStream;
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			consentPDFStream = new DefaultStreamedContent();
		} else {
			byte[] out = DatatypeConverter.parseBase64Binary(selectedTemplate.getScanBase64());
			consentPDFStream = new DefaultStreamedContent(new ByteArrayInputStream(out));
		}
		return consentPDFStream;
	}

	public void onSaveConsentTemplate() {
		FacesContext context = FacesContext.getCurrentInstance();
		String domainName = (selectedDomain.getLabel() != null && !selectedDomain.getLabel().isEmpty())? selectedDomain.getLabel() : selectedDomain.getName();
		Object[] args = { selectedTemplate.getKey().getName(), selectedTemplate.getKey().getVersion(), domainName };

		try {
			cmManager.addConsentTemplate(selectedTemplate);
			if (logger.isInfoEnabled()) {
				logger.info("template with key: " + selectedTemplate.getKey() + " added");
			}
			refresh();
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							new MessageFormat(messages.getString("template.message.templateAdded")).format(args), ""));
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while storing: " + selectedTemplate.getKey(), e);
		}
	}

	public void onNewFreeText() {
		FreeTextDefDTO freeText = new FreeTextDefDTO();
		freeText.setConverterString("dd.MM.yyyy");
		selectedTemplate.getFreeTextDefs().add(freeText);
	}

	public void removeFreeText(FreeTextDefDTO freeText) {
		selectedTemplate.getFreeTextDefs().remove(freeText);
	}

	public void refresh() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			templates = cmManager.listConsentTemplates(selectedDomain.getName());
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while listing consent templates for domain: " + selectedDomain.getName(), e);
		}
		tableDisabled = false;
		selectedTemplate = null;
	}

	/**
	 * Get-Method for root.
	 * 
	 * @return root
	 */
	public TreeNode getRoot() {
		return root;
	}

	/**
	 * Set-Method for root.
	 * 
	 * @param root
	 */
	public void setRoot(TreeNode root) {
		this.root = root;
	}

	/**
	 * Get-Method for selectedDomain.
	 * 
	 * @return selectedDomain
	 */
	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	/**
	 * Set-Method for selectedDomain.
	 * 
	 * @param selectedDomain
	 */
	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	/**
	 * Get-Method for templates.
	 * 
	 * @return templates
	 */
	public List<ConsentTemplateDTO> getTemplates() {
		return templates;
	}

	/**
	 * Set-Method for templates.
	 * 
	 * @param templates
	 */
	public void setTemplates(List<ConsentTemplateDTO> templates) {
		this.templates = templates;
	}

	/**
	 * Get-Method for selectedTemplate.
	 * 
	 * @return selectedTemplate
	 */
	public ConsentTemplateDTO getSelectedTemplate() {
		return selectedTemplate;
	}

	/**
	 * Set-Method for selectedTemplate.
	 * 
	 * @param selectedTemplate
	 */
	public void setSelectedTemplate(ConsentTemplateDTO selectedTemplate) {
		this.selectedTemplate = selectedTemplate;
	}

	/**
	 * Get-Method for tableDisabled.
	 * 
	 * @return tableDisabled
	 */
	public boolean isTableDisabled() {
		return tableDisabled;
	}

	/**
	 * Set-Method for tableDisabled.
	 * 
	 * @param tableDisabled
	 */
	public void setTableDisabled(boolean tableDisabled) {
		this.tableDisabled = tableDisabled;
	}

	/**
	 * Get-Method for freeText.
	 * 
	 * @return freeText
	 */
	public FreeTextDefDTO getFreeText() {
		return freeText;
	}

	/**
	 * Set-Method for freeText.
	 * 
	 * @param freeText
	 */
	public void setFreeText(FreeTextDefDTO freeText) {
		this.freeText = freeText;
	}

	public DualListModel<AssignedModuleDTO> getModules() {
		return modules;
	}

	public void setModules(DualListModel<AssignedModuleDTO> modules) {
		this.modules = modules;
	}
}
