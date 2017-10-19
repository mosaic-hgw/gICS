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
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.slf4j.LoggerFactory;

@ViewScoped
@ManagedBean(name = "IdSearchController")
public class IdSearchControllerBean extends CMController {

	private List<String> consentedIds;
	/**
	 * model for signer id selection
	 */
	private List<String> signerIdTypes;
	private List<PolicyDTO> policies;

	private DefaultStreamedContent resultCsv;
	private String signerIdType;
	/**
	 * policy for filtering
	 */
	private PolicyKeyDTO policy;

	private CheckConsentConfig config;
	private String selectedDomain;

	@Override
	protected void onInit() {
		logger = LoggerFactory.getLogger(getClass());

	}

	public void handleFileDownload() {
		StringBuilder output = new StringBuilder();
		for (String id : consentedIds) {
			output.append(id);
			output.append("\r\n");
		}
		String fileName = policy.getName() + "_" + policy.getVersion() + "_" + signerIdType;
		InputStream stream = new ByteArrayInputStream(output.toString().getBytes());
		resultCsv = new DefaultStreamedContent(stream, "text/csv", fileName);
	}

	public List<String> getPolicyNames() {
		List<String> names = new ArrayList<String>();
		for (PolicyDTO policy : policies) {
			if (!names.contains(policy.getKey().getName())) {
				names.add(policy.getKey().getName());
			}
		}
		return names;
	}

	public List<String> getPolicyVersions() {
		List<String> versions = new ArrayList<String>();
		for (PolicyDTO policy : policies) {
			if (policy.getKey().getName().equals(this.policy.getName())) {
				versions.add(policy.getKey().getVersion());
			}
		}
		return versions;
	}

	public void onApplyFilter() {
		FacesContext context = FacesContext.getCurrentInstance();
		// cmManager.getAllConsentedIdsFor(...) requires a valid policy key with valid version even if config.ignoreVersionNumber is true.
		// If no version was put in, the version is ignored.
		config.setIgnoreVersionNumber(false);
		if (policy.getVersion() == null || policy.getVersion().equals("")) {
			policy.setVersion(getPolicyVersions().get(0));
			config.setIgnoreVersionNumber(true);
		}
		try {
			if (policy.getVersion() == null) {
				throw new UnknownPolicyException("The given Policy does not exist");
			}
			consentedIds = cmManager.getAllConsentedIdsFor(signerIdType, policy, config);
			String[] args = { Integer.toString(consentedIds.size()), signerIdType, policy.getName(), policy.getVersion() };
			String message;
			if (config.getIgnoreVersionNumber()) {
				message = messages.getString("idSearch.tableHeader");
			} else {
				message = messages.getString("idSearch.tableHeaderWithVersion");
			}
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(message).format(args), ""));

		} catch (UnknownDomainException e) {
			handleDefaultError(context, e, "Domain unkown");
		} catch (UnknownSignerIdTypeException e) {
			handleDefaultError(context, e, "Unkown signer type");
		} catch (UnknownPolicyException e) {
			handleDefaultError(context, e, "the given Policy does not exist");
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while retrieving consented ids for: " + policy, e);
		}
	}

	public void refresh() {
		FacesContext context = FacesContext.getCurrentInstance();
		config = new CheckConsentConfig();
		config.setIgnoreVersionNumber(false);
		config.setUnknownStateIsConsideredAsDecline(false);
		try {
			setSignerIdTypes(cmManager.getDomain(selectedDomain).getSignerIdTypes());
			policies = cmManager.listPolicies(selectedDomain);
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while retrieving policies for: " + selectedDomain, e);
		}
		policy = new PolicyKeyDTO();
		signerIdType = null;
		policy.setDomainName(selectedDomain);
	}

	public void onDomainSelect(SelectEvent event) {
		selectedDomain = ((DomainDTO) event.getObject()).getName();
		refresh();
	}

	public PolicyKeyDTO getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyKeyDTO policy) {
		this.policy = policy;
	}

	public String getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(String selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	public List<String> getConsentedIds() {
		return consentedIds;
	}

	public void setConsentedIds(List<String> consentedIds) {
		this.consentedIds = consentedIds;
	}

	public List<String> getSignerIdTypes() {
		return signerIdTypes;
	}

	public void setSignerIdTypes(List<String> signerIdTypes) {
		this.signerIdTypes = signerIdTypes;
	}

	public String getSignerIdType() {
		return signerIdType;
	}

	public CheckConsentConfig getConfig() {
		return config;
	}

	public void setSignerIdType(String signerIdType) {
		this.signerIdType = signerIdType;
	}

	public void setConfig(CheckConsentConfig config) {
		this.config = config;
	}

	public List<PolicyDTO> getPolicies() {
		return policies;
	}

	public void setPolicies(List<PolicyDTO> policies) {
		this.policies = policies;
	}

	public DefaultStreamedContent getResultCsv() {
		return resultCsv;
	}

	public void setResultCsv(DefaultStreamedContent resultCsv) {
		this.resultCsv = resultCsv;
	}

}
