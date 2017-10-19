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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for Modules View
 * 
 * @author weiherg
 * 
 */
@ViewScoped
@ManagedBean(name = "PolicyController")
public class PolicyControllerBean extends CMController {

	private TreeNode root;
	private DomainDTO selectedDomain;
	private List<PolicyDTO> policies;
	private PolicyDTO selectedPolicy;

	@Override
	public void onInit() {
		logger = LoggerFactory.getLogger(PolicyControllerBean.class);
		policies = new ArrayList<PolicyDTO>();
		// erstellen von testwerten {
		root = new DefaultTreeNode("root", null);
		for (DomainDTO domain : domains) {
			new DefaultTreeNode(domain.getName(), root);
		}
	}

	public void refresh() {

		try {
			policies = cmManager.listPolicies(selectedDomain.getName());
		} catch (UnknownDomainException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}

		} catch (VersionConverterClassException e) {
			handleDefaultError(e, "");
		} catch (InvalidVersionException e) {
			handleDefaultError(e, "");
		}
		selectedPolicy = null;
	}

	public void onDomainSelect(SelectEvent event) {
		selectedDomain = ((DomainDTO) event.getObject());
		refresh();
	}

	public void onCancel() {
		selectedPolicy = null;
	}

	public void onPolicySelect(SelectEvent event) {
		selectedPolicy = (PolicyDTO) event.getObject();
	}

	public void onAddPolicy() {
		editMode = false;
		selectedPolicy = new PolicyDTO(new PolicyKeyDTO());
		selectedPolicy.getKey().setDomainName(selectedDomain.getName());
	}

	public void onEditPolicy() {
		editMode = true;
	}

	public void onDeletePolicy() throws VersionConverterClassException, UnknownDomainException {
		FacesContext context = FacesContext.getCurrentInstance();
		String domainName = (selectedDomain.getLabel() != null && !selectedDomain.getLabel().isEmpty())? selectedDomain.getLabel() : selectedDomain.getName();
		Object[] args = { selectedPolicy.getKey().getName(), selectedPolicy.getKey().getVersion(), domainName };
		try {
			cmManager.deletePolicy(selectedPolicy.getKey());
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("policy.message.policyDeleted")).format(args),
							""));
			if (logger.isInfoEnabled()) {
				logger.info("policy with key:" + selectedPolicy.getKey() + " deleted");
			}
			refresh();
		} catch (UnknownPolicyException e) {
			handleDefaultError(e, messages.getString("unknownPolicyError"));
		} catch (ObjectInUseException e) {
			handleDefaultError(e, "The Policy is in use and therefore can't be deleted");
		} catch (InvalidVersionException e) {
			String convertermessage = messages.getString("versionFormattingError");
			Object[] errorArgs = { selectedPolicy.getKey().getVersion() };
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(convertermessage).format(errorArgs), ""));
		}
	}

	public void onSavePolicy() throws VersionConverterClassException, UnknownDomainException {
		FacesContext context = FacesContext.getCurrentInstance();
		// RequestContext requestContext = RequestContext.getCurrentInstance();
		try {

			cmManager.addPolicy(selectedPolicy);
			Object[] args = { selectedPolicy.getKey().getName(), selectedPolicy.getKey().getVersion(), selectedDomain.getLabel() };
			context.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("policy.message.policyAdded")).format(args), ""));
			if (logger.isInfoEnabled()) {
				logger.info("policy with key:" + selectedPolicy.getKey() + " added to database");
			}
			refresh();

		} catch (DuplicateEntryException e) {
			handleDefaultError(e, messages.getString("duplicatePolicyError"));
		} catch (InvalidVersionException e) {
			String convertermessage = messages.getString("versionFormattingError");
			Object[] args = { selectedPolicy.getKey().getVersion() };
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(convertermessage).format(args), ""));
		}
	}

	/**
	 * Get-Method for policies.
	 * 
	 * @return policies
	 */
	public List<PolicyDTO> getPolicies() {
		return policies;
	}

	/**
	 * Set-Method for policies.
	 * 
	 * @param policies
	 */
	public void setPolicies(List<PolicyDTO> policies) {
		this.policies = policies;
	}

	/**
	 * Get-Method for selectedPolicy.
	 * 
	 * @return selectedPolicy
	 */
	public PolicyDTO getSelectedPolicy() {
		return selectedPolicy;
	}

	/**
	 * Set-Method for selectedPolicy.
	 * 
	 * @param selectedPolicy
	 */
	public void setSelectedPolicy(PolicyDTO selectedPolicy) {
		this.selectedPolicy = selectedPolicy;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}
}
