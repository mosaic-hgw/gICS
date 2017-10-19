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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for Modules View
 * 
 * @author weiherg
 * 
 */
@ViewScoped
@ManagedBean(name = "ModuleController")
public class ModuleControllerBean extends CMController implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3707020199066352023L;
	private DomainDTO selectedDomain;
	private List<ModuleDTO> modules;
	private ModuleDTO selectedModule;
	private DualListModel<PolicyDTO> policies;
	private boolean tableDisabled;
	private boolean editMode;

	@Override
	protected void onInit() {
		logger = LoggerFactory.getLogger(getClass());
		List<PolicyDTO> sourcePolicies = new ArrayList<PolicyDTO>();
		List<PolicyDTO> targetPolicies = new ArrayList<PolicyDTO>();
		setPolicies(new DualListModel<PolicyDTO>(sourcePolicies, targetPolicies));

	}

	public void refresh() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			modules = cmManager.listModules(selectedDomain.getName());
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while retrieving modules for: " + selectedDomain, e);
		}
		selectedModule = null;
	}

	public void onDomainSelect(SelectEvent event) throws UnknownDomainException, VersionConverterClassException {
		selectedDomain = ((DomainDTO) event.getObject());
		refresh();
	}

	public void onCancel() {
		selectedModule = null;
	}

	public void onModuleSelect(SelectEvent event) {
		selectedModule = (ModuleDTO) event.getObject();
	}

	public void onAddModule() {
		FacesContext context = FacesContext.getCurrentInstance();
		editMode = false;
		selectedModule = new ModuleDTO();
		ModuleKeyDTO key = new ModuleKeyDTO();
		key.setDomainName(selectedDomain.getName());
		selectedModule.setKey(key);
		policies.setTarget(new ArrayList<PolicyDTO>());
		try {
			policies.setSource(cmManager.listPolicies(selectedDomain.getName()));
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while adding module to: " + selectedDomain, e);
		}
	}

	public void onEditModule() throws UnknownDomainException, VersionConverterClassException {
		FacesContext context = FacesContext.getCurrentInstance();
		editMode = true;
		try {
			policies.setSource(cmManager.listPolicies(selectedDomain.getName()));
			policies.setTarget(selectedModule.getPolicies());
			policies.getSource().removeAll(selectedModule.getPolicies());
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while editing module for: " + selectedModule.getKey(), e);
		}
	}

	public void onDeleteModule() throws UnknownDomainException, UnknownModuleException, VersionConverterClassException {
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { selectedModule.getKey().getName(), selectedModule.getKey().getVersion(), selectedModule.getKey().getDomainName() };
		try {
			cmManager.deleteModule(selectedModule.getKey());
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("modules.message.moduleDeleted")).format(args),
							""));
			if (logger.isInfoEnabled()) {
				logger.info("module with key:" + selectedModule.getKey() + " deleted");
			}
			refresh();
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while deleting module: " + selectedModule.getKey(), e);
		}
	}

	public void onSaveModule() throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException {
		FacesContext context = FacesContext.getCurrentInstance();
		// RequestContext requestContext = RequestContext.getCurrentInstance();
		Object[] args = { selectedModule.getKey().getName(), selectedModule.getKey().getVersion(), selectedModule.getKey().getDomainName() };
		try {
			selectedModule.setPolicies(policies.getTarget());
			cmManager.addModule(selectedModule);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
					new MessageFormat(messages.getString("modules.message.moduleAdded")).format(args), ""));
			if (logger.isInfoEnabled()) {
				logger.info("module with key:" + selectedModule.getKey() + " added to database");
			}
			refresh();

		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while saving module: " + selectedModule, e);
		}
	}

	public ModuleDTO getSelectedModule() {
		return selectedModule;
	}

	public void setSelectedModule(ModuleDTO selectedModule) {
		this.selectedModule = selectedModule;
	}

	public List<ModuleDTO> getModules() {
		return modules;
	}

	public void setModules(List<ModuleDTO> modules) {
		this.modules = modules;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	public DualListModel<PolicyDTO> getPolicies() {
		return policies;
	}

	public void setPolicies(DualListModel<PolicyDTO> policies) {
		this.policies = policies;
	}

	public boolean isTableDisabled() {
		return tableDisabled;
	}

	public void setTableDisabled(boolean tableDisabled) {
		this.tableDisabled = tableDisabled;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean isEditMode) {
		this.editMode = isEditMode;
	}

}
