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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for Modules View
 * 
 * @author Arne Blumentritt
 * 
 */
@ViewScoped
@ManagedBean(name = "moduleController")
public class ModuleController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = 3707020199066352023L;
	private List<ModuleDTO> modules;
	private ModuleDTO selectedModule;
	private ModuleDTO editModule;
	private Boolean editMode;

	private DualListModel<PolicyDTO> policies;

	@PostConstruct
	protected void init()
	{
		logger = LoggerFactory.getLogger(getClass());
		setPolicies(new DualListModel<PolicyDTO>(new ArrayList<PolicyDTO>(), new ArrayList<PolicyDTO>()));
		loadModules();
		editMode = false;
	}

	public void onModuleSelect(SelectEvent event)
	{
		selectedModule = (ModuleDTO) event.getObject();
	}

	public void onNewModule()
	{
		editMode = true;
		editModule = new ModuleDTO(new ModuleKeyDTO());
		editModule.getKey().setDomainName(domainSelector.getSelectedDomainName());
		try
		{
			policies.setSource(cmManager.listPolicies(domainSelector.getSelectedDomainName()));
			policies.setTarget(new ArrayList<PolicyDTO>());
			sortPolicies();
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onAddModule(Boolean scroll)
	{
		try
		{
			editModule.setPolicies(policies.getTarget());
			cmManager.addModule(editModule);
			Object[] args = { editModule.getKey().getName(), editModule.getKey().getVersion(), editModule.getKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("module.message.info.added")).format(args), Severity.INFO, scroll);
			loadModules();
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("module.message.error.duplicate"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			logMessage(getBundle().getString("module.message.error.versionFormat"), Severity.ERROR);
		}
		catch (UnknownDomainException | UnknownPolicyException | VersionConverterClassException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onEditModule(ModuleDTO module) throws UnknownDomainException, VersionConverterClassException
	{
		editMode = true;
		editModule = module;
		editModule.getKey().setDomainName(domainSelector.getSelectedDomainName());
		try
		{
			policies.setSource(cmManager.listPolicies(domainSelector.getSelectedDomainName()));
			policies.setTarget(editModule.getPolicies());
			policies.getSource().removeAll(editModule.getPolicies());
			sortPolicies();
		}
		catch (Exception e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}
	
	public void onCancelEdit()
	{
		editMode = false;
	}

	public void onUpdateModule() throws UnknownDomainException, UnknownPolicyException, VersionConverterClassException
	{
		try
		{
			editModule.setPolicies(policies.getTarget());
			cmManager.addModule(editModule);
			Object[] args = { editModule.getKey().getName(), editModule.getKey().getVersion(), editModule.getKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("module.message.info.updated")).format(args), Severity.INFO);
			loadModules();
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("module.message.error.duplicateUpdate"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			logMessage(getBundle().getString("module.message.error.versionFormat"), Severity.ERROR);
		}
		catch (UnknownDomainException | UnknownPolicyException | VersionConverterClassException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDeleteModule(ModuleDTO module) throws UnknownDomainException, UnknownModuleException, VersionConverterClassException
	{
		try
		{
			cmManager.deleteModule(module.getKey());
			Object[] args = { module.getKey().getName(), module.getKey().getVersion(), module.getKey().getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("module.message.info.deleted")).format(args), Severity.INFO);
			loadModules();
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("module.message.error.deleteInUse"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void refreshAvailablePolicies() throws UnknownDomainException, VersionConverterClassException, InvalidVersionException
	{
		List<PolicyDTO> newPolicies = cmManager.listPolicies(domainSelector.getSelectedDomainName());
		this.policies.getSource().add(0, newPolicies.get(newPolicies.size() - 1));
		sortPolicies();
	}

	private void loadModules()
	{
		try
		{
			modules = cmManager.listModules(domainSelector.getSelectedDomainName());
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedModule = null;
	}

	public ModuleDTO getSelectedModule()
	{
		return selectedModule;
	}

	public void setSelectedModule(ModuleDTO selectedModule)
	{
		this.selectedModule = selectedModule;
	}

	public ModuleDTO getEditModule()
	{
		return editModule;
	}

	public Boolean getEditMode()
	{
		return editMode;
	}

	public List<ModuleDTO> getModules()
	{
		return modules;
	}

	public DualListModel<PolicyDTO> getPolicies()
	{
		return policies;
	}

	public void setPolicies(DualListModel<PolicyDTO> policies)
	{
		for (int i = 0; i < policies.getTarget().size(); i++)
		{
			for (int j = i + 1; j < policies.getTarget().size(); j++)
			{
				if (policies.getTarget().get(i).getKey().getName().equals(policies.getTarget().get(j).getKey().getName()))
				{
					Object[] args = { policies.getTarget().get(i).getKey().getName() };
					logMessage(new MessageFormat(getBundle().getString("template.message.error.duplicatePolicy")).format(args), Severity.ERROR, false);
					return;
				}
			}
		}
		this.policies = policies;
	}
	
	private void sortPolicies()
	{
		Collections.sort(policies.getSource(), new Comparator<PolicyDTO>(){
		     public int compare(PolicyDTO p1, PolicyDTO p2){
		         return p1.getKey().getName().toLowerCase().compareTo(p2.getKey().getName().toLowerCase());
		     }
		});
	}
}
