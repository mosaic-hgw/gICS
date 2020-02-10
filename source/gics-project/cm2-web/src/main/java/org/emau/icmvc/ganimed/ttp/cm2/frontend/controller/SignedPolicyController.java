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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.primefaces.event.SelectEvent;

@ViewScoped
@ManagedBean(name = "SignedPolicyController")
public class SignedPolicyController extends AbstractConsentController {
	/**
	 * List of Signed Policies
	 */
	private List<SignedPolicyDTO> policies;
	/**
	 * Signer Ids, identifying a single person for filtering
	 */
	private List<SignerIdDTO> signerIds;
	/**
	 * policy for filtering
	 */
	private PolicyKeyDTO policy;

	private boolean ignoreVersion;
	private boolean filterByPolicy;
	private String selectedDomain;

	public void onApplyFilter() {
		FacesContext context = FacesContext.getCurrentInstance();
		Set<SignerIdDTO> idSet = new HashSet<SignerIdDTO>();
		idSet.addAll(signerIds);
		try {
			if (policy.getName() != null && !policy.getName().equals("")) {
				if (policy.getVersion() != null && !policy.getVersion().equals("")) {
					policies = cmManager.getPolicyStatesForPolicyAndSigner(policy, idSet);
				} else {
					policies = cmManager.getPolicyStatesForPolicyNameAndSigner(selectedDomain, policy.getName(), idSet);
				}
			} else {
				policies = cmManager.getPolicyStatesForSigner(selectedDomain, idSet);
			}

		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while getting policy states: ", e);
		}
	}

	private void refresh() {
		FacesContext context = FacesContext.getCurrentInstance();
		// generate Signer ID types
		signerIds = new ArrayList<SignerIdDTO>();
		try {
			for (String signerIdType : cmManager.getDomain(selectedDomain).getSignerIdTypes()) {
				SignerIdDTO signerIdDTO = new SignerIdDTO();
				signerIdDTO.setIdType(signerIdType);
				signerIds.add(signerIdDTO);
			}
		} catch (UnknownDomainException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception updateting modules for: " + selectedDomain, e);
		}
		// generate policy model
		policy = new PolicyKeyDTO();
		policy.setDomainName(selectedDomain);
	}

	public void onDomainSelect(SelectEvent event) {
		selectedDomain = ((DomainDTO) event.getObject()).getName();
		refresh();
	}

	public List<SignedPolicyDTO> getPolicies() {
		return policies;
	}

	public void setPolicies(List<SignedPolicyDTO> policies) {
		this.policies = policies;
	}

	public List<SignerIdDTO> getSignerIds() {
		return signerIds;
	}

	public void setSignerIds(List<SignerIdDTO> signerIds) {
		this.signerIds = signerIds;
	}

	public PolicyKeyDTO getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyKeyDTO policy) {
		this.policy = policy;
	}

	public boolean isFilterByPolicy() {
		return filterByPolicy;
	}

	public void setFilterByPolicy(boolean filterByPolicy) {
		this.filterByPolicy = filterByPolicy;
	}

	public boolean isIgnoreVersion() {
		return ignoreVersion;
	}

	public void setIgnoreVersion(boolean ignoreVersion) {
		this.ignoreVersion = ignoreVersion;
	}

	public void setSelectedDomain(String selectedDomain) {
		this.selectedDomain = selectedDomain;
	}
}
