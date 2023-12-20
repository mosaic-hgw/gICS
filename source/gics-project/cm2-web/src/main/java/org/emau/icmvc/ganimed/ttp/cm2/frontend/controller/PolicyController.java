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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
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
public class PolicyController extends AbstractConsentController implements Serializable
{
	private static final long serialVersionUID = -8904144154843390078L;

	private List<PolicyDTO> policies;
	private PolicyDTO selectedPolicy;
	private PolicyDTO editPolicy;

	@PostConstruct
	public void init()
	{
		loadPolicies();
		pageMode = PageMode.READ;
	}

	public void onPolicySelect(SelectEvent event)
	{
		selectedPolicy = (PolicyDTO) event.getObject();
	}

	public void onNewPolicy()
	{
		pageMode = PageMode.NEW;
		editPolicy = new PolicyDTO(new PolicyKeyDTO());
		editPolicy.getKey().setDomainName(domainSelector.getSelectedDomainName());
	}

	public void onEditPolicy(PolicyDTO policy)
	{
		pageMode = PageMode.EDIT;
		editPolicy = policy;
	}

	public void onDuplicatePolicy(PolicyDTO policy)
	{
		pageMode = PageMode.NEW;
		editPolicy = new PolicyDTO(policy.getKey());
		editPolicy.setLabel(policy.getLabel());
		editPolicy.setComment(policy.getComment());
		editPolicy.setExternProperties(policy.getExternProperties());
	}

	public void onSavePolicy(Boolean scroll)
	{
		Object[] args = { editPolicy.getLabel(), editPolicy.getKey().getVersion(), domainSelector.getSelectedDomainName() };

		if (pageMode == PageMode.NEW)
		{
			try
			{
				// Finalise domain to be able to add policy
				if (!domainSelector.getSelectedDomain().getFinalised())
				{
					manager.finaliseDomain(domainSelector.getSelectedDomainName());
					domainSelector.loadDomains();
					domainSelector.setSelectedDomain(domainSelector.getSelectedDomainName());
				}

				// Add policy
				if (StringUtils.isEmpty(editPolicy.getKey().getName()))
				{
					editPolicy.getKey().setName(editPolicy.getLabel());
				}
				editPolicy.setFinalised(true);
				manager.addPolicy(editPolicy);
				logMessage(new MessageFormat(getBundle().getString("policy.message.info.added")).format(args), Severity.INFO, scroll);
				loadPolicies();
				pageMode = PageMode.READ;
			}
			catch (DuplicateEntryException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(getBundle().getString("policy.message.error.duplicate"), Severity.WARN, scroll);
			}
			catch (InvalidVersionException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(new MessageFormat(getBundle().getString("policy.message.error.invalidVersion")).format(args), Severity.WARN, scroll);
			}
			catch (UnknownDomainException | RequirementsNotFullfilledException | InvalidParameterException e)
			{
				FacesContext.getCurrentInstance().validationFailed();
				logMessage(e.getLocalizedMessage(), Severity.ERROR, scroll);
			}
		}
		else if (pageMode == PageMode.EDIT)
		{
			try
			{
				editPolicy.setFinalised(true);
				manager.updatePolicyInUse(editPolicy.getKey(), editPolicy.getLabel(), editPolicy.getExternProperties(), editPolicy.getComment());
				logMessage(new MessageFormat(getBundle().getString("policy.message.info.updated")).format(args), Severity.INFO, scroll);
				loadPolicies();
				pageMode = PageMode.READ;
			}
			catch (UnknownPolicyException e)
			{
				logMessage(new MessageFormat(getBundle().getString("policy.message.error.unkownPolicy")).format(args), Severity.WARN, scroll);
			}
			catch (InvalidVersionException e)
			{
				logMessage(new MessageFormat(getBundle().getString("policy.message.error.invalidVersion")).format(args), Severity.WARN, scroll);
			}
			catch (UnknownDomainException | InvalidParameterException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR, scroll);
			}
		}
	}

	public void onCancel()
	{
		pageMode = PageMode.READ;
	}

	public void onFinalise(PolicyDTO policy)
	{
		try
		{
			manager.finalisePolicy(policy.getKey());
			Object[] args = { policy.getLabel(), policy.getKey().getVersion() };
			logMessage(new MessageFormat(getBundle().getString("policy.message.info.finalised")).format(args), Severity.INFO);
		}
		catch (UnknownPolicyException | InvalidVersionException | UnknownDomainException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDeletePolicy(PolicyDTO policy) throws UnknownDomainException
	{
		Object[] args = { policy.getLabel(), policy.getKey().getVersion(), domainSelector.getSelectedDomainName() };

		try
		{
			manager.deletePolicy(policy.getKey());
			logMessage(new MessageFormat(getBundle().getString("policy.message.info.deleted")).format(args), Severity.INFO);
			loadPolicies();
		}
		catch (ObjectInUseException e)
		{
			logMessage(getBundle().getString("policy.message.error.deleteInUse"), Severity.WARN);
		}
		catch (UnknownPolicyException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	private void loadPolicies()
	{
		try
		{
			policies = service.listPolicies(domainSelector.getSelectedDomainName(), false);
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
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

	public List<PolicyDTO> getPolicies()
	{
		return policies;
	}
}
