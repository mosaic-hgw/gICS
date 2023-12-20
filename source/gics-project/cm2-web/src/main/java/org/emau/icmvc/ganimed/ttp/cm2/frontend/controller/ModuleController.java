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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.WebExpiration;
import org.primefaces.model.DualListModel;

/**
 * Backing Bean for Modules View
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@ManagedBean(name = "moduleController")
public class ModuleController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = 3707020199066352023L;

	private List<ModuleDTO> modules;
	private ModuleDTO selectedModule;
	private ModuleDTO editModule;
	private Map<PolicyKeyDTO, WebExpiration> policyExpirations = new HashMap<>();

	private DualListModel<AssignedPolicyDTO> policies;

	@PostConstruct
	protected void init()
	{
		setPolicies(new DualListModel<>(new ArrayList<>(), new ArrayList<>()));
		loadModules();
		pageMode = PageMode.READ;
	}

	public void onNewModule() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.NEW;
		editModule = new ModuleDTO(new ModuleKeyDTO());
		editModule.getKey().setDomainName(domainSelector.getSelectedDomainName());
		loadPolicies(editModule);
	}

	public void onEditModule(ModuleDTO module) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.EDIT;
		editModule = module;
		loadPolicies(editModule);

		if (module.getFinalised())
		{
			logMessage(getBundle().getString("module.message.info.isFinal"), Severity.INFO);
		}
	}

	public ModuleDTO onDuplicateModule(ModuleDTO module) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		pageMode = PageMode.NEW;
		editModule = new ModuleDTO(module.getKey());
		editModule.setLabel(module.getLabel());
		editModule.setComment(module.getComment());
		editModule.setExternProperties(module.getExternProperties());
		editModule.setAssignedPolicies(module.getAssignedPolicies());
		editModule.setShortText(module.getShortText());
		editModule.setText(module.getText());
		editModule.setTitle(module.getTitle());
		loadPolicies(editModule);
		return editModule;
	}

	public void onSaveModule(Boolean scroll)
	{
		preparePolicies();
		Object[] args = { editModule.getLabel(), editModule.getKey().getVersion(), editModule.getKey().getDomainName() };

		if (pageMode == PageMode.NEW)
		{
			try
			{
				// Finalise domain to be able to add module
				if (!domainSelector.getSelectedDomain().getFinalised())
				{
					manager.finaliseDomain(domainSelector.getSelectedDomainName());
					domainSelector.loadDomains();
					domainSelector.setSelectedDomain(domainSelector.getSelectedDomainName());
				}

				// Add module
				if (StringUtils.isEmpty(editModule.getKey().getName()))
				{
					editModule.getKey().setName(editModule.getLabel());
				}
				manager.addModule(editModule, false);
				logMessage(new MessageFormat(getBundle().getString("module.message.info.added")).format(args), Severity.INFO, scroll);
				loadModules();
				pageMode = PageMode.READ;
			}
			catch (DuplicateEntryException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(getBundle().getString("module.message.error.duplicate"), Severity.WARN, scroll);
			}
			catch (InvalidVersionException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(getBundle().getString("module.message.error.invalidVersion"), Severity.WARN, scroll);
			}
			catch (UnknownDomainException | UnknownPolicyException | RequirementsNotFullfilledException | InvalidParameterException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(e.getLocalizedMessage(), Severity.ERROR, scroll);
			}
		}
		else if (pageMode == PageMode.EDIT)
		{
			try
			{
				if (editModule.getFinalised())
				{
					manager.updateModuleInUse(editModule.getKey(), editModule.getLabel(), editModule.getShortText(), editModule.getExternProperties(), editModule.getComment(),
							editModule.getAssignedPolicies());
				}
				else
				{
					manager.updateModule(editModule, false);
				}
				logMessage(new MessageFormat(getBundle().getString("module.message.info.updated")).format(args), Severity.INFO, scroll);
				loadModules();
				pageMode = PageMode.READ;
			}
			catch (InvalidVersionException e)
			{
				logMessage(getBundle().getString("module.message.error.invalidVersion"), Severity.WARN, scroll);
			}
			catch (UnknownModuleException | UnknownDomainException | DuplicateEntryException | ObjectInUseException | UnknownPolicyException | InvalidParameterException
					| RequirementsNotFullfilledException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR, scroll);
			}
		}
	}

	public void onCancel()
	{
		pageMode = PageMode.READ;
	}

	public void onFinalise(ModuleDTO module)
	{
		try
		{
			manager.finaliseModule(module.getKey(), true);
			Object[] args = { module.getLabel(), module.getKey().getVersion() };
			logMessage(new MessageFormat(getBundle().getString("module.message.info.finalised")).format(args), Severity.INFO);
			loadModules();
		}
		catch (UnknownModuleException | InvalidVersionException | RequirementsNotFullfilledException | UnknownDomainException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDeleteModule(ModuleDTO module) throws UnknownDomainException, UnknownModuleException
	{
		Object[] args = { module.getLabel(), module.getKey().getVersion(), module.getKey().getDomainName() };

		try
		{
			manager.deleteModule(module.getKey());
			logMessage(new MessageFormat(getBundle().getString("module.message.info.deleted")).format(args), Severity.INFO);
			loadModules();
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("module.message.error.deleteInUse"), Severity.WARN);
		}
		catch (InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void addNewestPolicy() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		if (!FacesContext.getCurrentInstance().isValidationFailed())
		{
			List<PolicyDTO> newPolicies = service.listPolicies(domainSelector.getSelectedDomainName(), false);
			AssignedPolicyDTO newAssignedPolicy = new AssignedPolicyDTO(newPolicies.stream().max(Comparator.comparing(PolicyDTO::getCreationDate)).orElse(null));
			policies.getTarget().add(0, newAssignedPolicy);
			sortPolicies();

			Object[] args = { newAssignedPolicy.getPolicy().getLabel(), newAssignedPolicy.getPolicy().getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage("policyPicker", new MessageFormat(getBundle().getString("policy.message.info.added")).format(args), Severity.INFO, false, false);

		}
	}

	public void refreshAvailablePolicies() throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		List<AssignedPolicyDTO> sourcePolicies = new ArrayList<>();
		// Get all available policies
		List<PolicyDTO> unassignedPolicies = service.listPolicies(domainSelector.getSelectedDomainName(), false);

		unassignedPolicies: for (PolicyDTO policy : unassignedPolicies)
		{
			for (AssignedPolicyDTO assignedPolicyDTO : policies.getTarget())
			{
				if (assignedPolicyDTO.getPolicy().getKey().equals(policy.getKey()))
				{
					continue unassignedPolicies;
				}
			}
			sourcePolicies.add(new AssignedPolicyDTO(policy));
		}
		policies.setSource(sourcePolicies);
		sortPolicies();

		logMessage("policyPicker", getBundle().getString("policy.message.info.refreshedAvailable"), Severity.INFO, false, false);
	}

	private void loadModules()
	{
		try
		{
			modules = service.listModules(domainSelector.getSelectedDomainName(), false);
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedModule = null;
	}

	/**
	 * Generate policy picklist with unassigend policies on the left and policies that are already
	 * assigned to the module on the right
	 *
	 * @param module
	 * @throws InvalidVersionException
	 * @throws UnknownDomainException
	 * @throws InvalidParameterException
	 */
	private void loadPolicies(ModuleDTO module) throws InvalidVersionException, UnknownDomainException, InvalidParameterException
	{
		List<AssignedPolicyDTO> sourcePolicies = new ArrayList<>();
		policyExpirations = new HashMap<>();

		// Get all available policies
		List<PolicyDTO> allPolicies = service.listPolicies(domainSelector.getSelectedDomainName(), false);

		// Prepare assigned policies
		for (AssignedPolicyDTO assignedPolicy : module.getAssignedPolicies())
		{
			allPolicies.remove(assignedPolicy.getPolicy());
			WebExpiration expiration = new WebExpiration();
			expiration.getEditExpiration().setExpirationProperties(assignedPolicy.getExpirationProperties());
			expiration.saveExpiration();
			policyExpirations.put(assignedPolicy.getPolicy().getKey(), expiration);
		}

		// Prepare unassigned policies
		for (PolicyDTO policy : allPolicies)
		{
			AssignedPolicyDTO unassignedPolicy = new AssignedPolicyDTO(policy);
			sourcePolicies.add(unassignedPolicy);
			policyExpirations.put(policy.getKey(), new WebExpiration());
		}

		policies = new DualListModel<>(sourcePolicies, new ArrayList<>(module.getAssignedPolicies()));
		sortPolicies();
	}

	/**
	 * Listener for change of the policy selection. Replaces the assigned policies of selected
	 * template with the new policy selection. Configurations from the old selection(extern properties, expiration) are copied
	 */
	public void onAssignPolicies()
	{
		if (!validatePolicies())
		{
			return;
		}

		// The sorting from the target list must be adopted
		// Therefore iterate over all policies in target list and work with this list
		for (AssignedPolicyDTO policy : policies.getTarget())
		{
			boolean found = false;
			for (AssignedPolicyDTO policyModule : editModule.getAssignedPolicies())
			{
				if (policy.getPolicy().getKey().equals(policyModule.getPolicy().getKey()))
				{
					// Copy values from current module policy to policy of the target list
					policy.setComment(policyModule.getComment());
					policy.setExternProperties(policyModule.getExternProperties());
					policy.setPolicy(policyModule.getPolicy());
					found = true;
					break;
				}
			}

			// policy is new and was not yet in module
			if (!found)
			{
				// Store in expiration Map
				policyExpirations.put(policy.getPolicy().getKey(), new WebExpiration());
			}
		}

		// set the target list as the module policy list
		editModule.setAssignedPolicies(Set.copyOf(policies.getTarget()));
	}

	public List<AssignedPolicyDTO> getAssignedPoliciesSorted()
	{
		return editModule.getAssignedPolicies().stream().sorted().collect(Collectors.toList());
	}

	private void preparePolicies()
	{
		for (AssignedPolicyDTO policy : editModule.getAssignedPolicies())
		{
			// Set expirationPropertioes
			WebExpiration expiration = policyExpirations.getOrDefault(policy.getPolicy().getKey(), new WebExpiration());
			policy.setExpirationProperties(expiration.getSavedExpiration().getExpirationProperties());
		}
	}

	private boolean validatePolicies()
	{
		for (int i = 0; i < policies.getTarget().size(); i++)
		{
			for (int j = i + 1; j < policies.getTarget().size(); j++)
			{
				if (policies.getTarget().get(i).getPolicy().getKey().getName().equals(policies.getTarget().get(j).getPolicy().getKey().getName()))
				{
					Object[] args = { policies.getTarget().get(i).getPolicy().getKey().getName() };
					logMessage(new MessageFormat(getBundle().getString("template.message.error.duplicatePolicy")).format(args), Severity.WARN, true, true);
					return false;
				}
			}
		}
		return true;
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

	public List<ModuleDTO> getModules()
	{
		return modules;
	}

	public Map<PolicyKeyDTO, WebExpiration> getPolicyExpirations()
	{
		return policyExpirations;
	}

	public DualListModel<AssignedPolicyDTO> getPolicies()
	{
		return policies;
	}

	public void setPolicies(DualListModel<AssignedPolicyDTO> policies)
	{
		this.policies = policies;
	}

	private void sortPolicies()
	{
		Collections.sort(policies.getSource());
		Collections.sort(policies.getTarget());
	}

	public String getEditModuleSanitizedTitle()
	{
		return text.sanitizeRelaxed(editModule.getTitle());
	}

	public void setEditModuleSanitizedTitle(String title)
	{
		editModule.setTitle(title);
	}

	public String getEditModuleSanitizedText()
	{
		return text.sanitizeRelaxed(editModule.getText());
	}

	public void setEditModuleSanitizedText(String text)
	{
		editModule.setText(text);
	}
}
