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

import java.text.MessageFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.primefaces.event.SelectEvent;

/**
 * Backing Bean for Policies View
 * 
 * @author Arne Blumentritt
 * 
 */
@ViewScoped
@ManagedBean(name = "policyController")
public class PolicyController extends AbstractConsentController
{
	private List<PolicyDTO> policies;
	private PolicyDTO selectedPolicy;
	private PolicyDTO editPolicy;
	private Boolean editMode;

	@PostConstruct
	public void init()
	{
		loadPolicies();
		editMode = false;
	}
	
	public void onPolicySelect(SelectEvent event) {
		selectedPolicy = (PolicyDTO) event.getObject();
	}

	public void onNewPolicy()
	{
		editMode = true;
		editPolicy = new PolicyDTO(new PolicyKeyDTO());
		editPolicy.getKey().setDomainName(domainSelector.getSelectedDomainName());
	}

	public void onAddPolicy(Boolean scroll) throws VersionConverterClassException, UnknownDomainException
	{
		try
		{
			cmManager.addPolicy(editPolicy);
			Object[] args = { editPolicy.getKey().getName(), editPolicy.getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage(new MessageFormat(getBundle().getString("policy.message.info.added")).format(args), Severity.INFO, scroll);
			loadPolicies();
			editMode = false;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("policy.message.error.duplicate"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			Object[] args = { editPolicy.getKey().getVersion() };
			logMessage(new MessageFormat(getBundle().getString("policy.message.error.versionFormat")).format(args), Severity.ERROR);
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
	
	public void onDeletePolicy(PolicyDTO policy) throws VersionConverterClassException, UnknownDomainException
	{
		try
		{
			cmManager.deletePolicy(policy.getKey());
			Object[] args = { policy.getKey().getName(), policy.getKey().getVersion(), domainSelector.getSelectedDomainName() };
			logMessage(new MessageFormat(getBundle().getString("policy.message.info.deleted")).format(args), Severity.INFO);
			loadPolicies();
		}
		catch (UnknownPolicyException e)
		{
			logMessage(getBundle().getString("policy.message.error.unknown"), Severity.ERROR);
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("policy.message.error.deleteInUse"), Severity.ERROR);
		}
		catch (InvalidVersionException e)
		{
			Object[] args = { policy.getKey().getVersion() };
			logMessage(new MessageFormat(getBundle().getString("policy.message.error.versionFormat")).format(args), Severity.ERROR);
		}
	}

	private void loadPolicies()
	{
		try
		{
			policies = cmManager.listPolicies(domainSelector.getSelectedDomainName());
		}
		catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		selectedPolicy = null;
	}

	public PolicyDTO getSelectedPolicy()
	{
		return selectedPolicy;
	}

	public void setSelectedPolicy(PolicyDTO selectedPolicy)
	{
		this.selectedPolicy = selectedPolicy;
	}

	public PolicyDTO getEditPolicy()
	{
		return editPolicy;
	}

	public Boolean getEditMode()
	{
		return editMode;
	}

	public List<PolicyDTO> getPolicies()
	{
		return policies;
	}
}
