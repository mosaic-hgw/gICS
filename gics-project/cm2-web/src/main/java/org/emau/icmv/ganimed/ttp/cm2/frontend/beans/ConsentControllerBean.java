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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpSession;

import org.emau.icmv.ganimed.ttp.cm2.frontend.util.FreeTextValue;
import org.emau.icmv.ganimed.ttp.cm2.frontend.util.ModuleState;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.Base64;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for Consent Document View
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "ConsentController")
@ViewScoped
public class ConsentControllerBean extends CMController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8346496271087066642L;

	private List<ConsentDTO> consents;
	private List<ConsentDTO> filteredConsents;
	private List<ConsentTemplateKeyDTO> templateKeys;
	private ConsentTemplateDTO template;
	private List<ModuleState> moduleStates;
	private List<FreeTextValue> freeTexts;
	private DomainDTO selectedDomain;
	private ConsentDTO selectedConsent;
	private ConsentDTO detailsConsent;
	private TreeNode root;
	private boolean tableDisabled;

	/**
	 * Handler for adding scans to already persisted consents
	 * 
	 * @param event
	 * @throws UnknownSignerIdTypeException
	 * @throws InvalidVersionException
	 * @throws VersionConverterClassException
	 * @throws UnknownDomainException
	 * @throws InconsistentStatusException
	 */
	public void handleScanUpload(FileUploadEvent event) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			UnknownSignerIdTypeException, InconsistentStatusException {
		FacesContext context = FacesContext.getCurrentInstance();
		String base64 = Base64.encodeToString(event.getFile().getContents(), true);
		String fileType = "application/pdf";
		try {
			cmManager.addScanToConsent(selectedConsent.getKey(), base64, fileType);
			ConsentDTO tmp = selectedConsent;
			refresh();
			// update selected consent for correct Preview display
			for (ConsentDTO consent : consents) {
				if (consent.getKey().equals(tmp.getKey())) {
					selectedConsent = consent;
					updatePreview(selectedConsent);
					break;
				}
			}

		} catch (UnknownConsentTemplateException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (UnknownConsentException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (DuplicateEntryException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}
	}

	/**
	 * handler for adding scans to new consents
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		selectedConsent.setScanBase64(Base64.encodeToString(event.getFile().getContents(), true));
		updatePreview(selectedConsent);
	}

	/**
	 * handler for patient Signature file upload
	 * 
	 * @param event
	 */
	public void handlePatientSignatureUplaod(FileUploadEvent event) {
		selectedConsent.setPatientSignatureBase64(Base64.encodeToString(event.getFile().getContents(), true));
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("patient_signature", selectedConsent.getPatientSignatureBase64());
	}

	/**
	 * handler for physican signature file upload
	 * 
	 * @param event
	 */
	public void handlePhysicianSignatureUplaod(FileUploadEvent event) {
		selectedConsent.setPhysicanSignatureBase64(Base64.encodeToString(event.getFile().getContents(), true));
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("physician_signature", selectedConsent.getPhysicanSignatureBase64());
	}

	public void onDomainSelect(SelectEvent event) throws UnknownDomainException, VersionConverterClassException, InvalidVersionException,
			InconsistentStatusException {
		selectedDomain = ((DomainDTO) event.getObject());
		refresh();
	}

	/**
	 * sets the detailConsent for Detail display
	 * 
	 * @param consent
	 */
	public void onShowDetails(ConsentDTO consent) {
		detailsConsent = consent;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("physician_signature", detailsConsent.getPhysicanSignatureBase64());
		session.setAttribute("patient_signature", detailsConsent.getPatientSignatureBase64());
	}

	/**
	 * persists the selectedConsent
	 * 
	 * @throws InconsistentStatusException
	 * @throws VersionConverterClassException
	 * @throws UnknownDomainException
	 */
	public void onSaveConsent() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			HashMap<ModuleKeyDTO, ConsentStatus> moduleMapping = new HashMap<ModuleKeyDTO, ConsentStatus>();
			for (ModuleState status : moduleStates) {
				moduleMapping.put(status.getAssignedModule().getModule().getKey(), status.getStatus());
			}
			if (selectedConsent.getPatientSignatureBase64() == null) {
				context.validationFailed();
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("consent.noPatientSignature"), ""));
				// throw new MissingRequiredObjectException("PAtientSignature must be set");
			}
			if (selectedConsent.getPhysicanSignatureBase64() == null) {
				context.validationFailed();
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("consent.noPhysicanSignature"), ""));
				// throw new MissingRequiredObjectException("PAtientSignature must be set");
			}
			// selectedConsent.setPolicyStates(policyMapping);
			selectedConsent.setModuleStates(moduleMapping);
			selectedConsent.setScanFileType("application/pdf");

			HashMap<String, String> freeTextMapping = new HashMap<String, String>();
			for (FreeTextValue freeText : freeTexts) {
				freeTextMapping.put(freeText.getFreeTextDef().getName(), freeText.getValue());
			}
			selectedConsent.setFreeTextVals(freeTextMapping);
			selectedConsent.getKey().setConsentDate(new Date());

			cmManager.addConsent(selectedConsent);
			refresh();
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while saving consent: " + selectedConsent.getKey(), e);
		}
	}

	private void generateModuleStates() {
		moduleStates = new ArrayList<ModuleState>();
		for (AssignedModuleDTO assignedModule : template.getAssignedModules()) {
			moduleStates.add(new ModuleState(assignedModule));
		}
	}

	private void generateFreeTextValues() {
		freeTexts = new ArrayList<FreeTextValue>();
		for (FreeTextDefDTO freeTextDef : template.getFreeTextDefs()) {
			freeTexts.add(new FreeTextValue(freeTextDef));

		}
	}

	private void generateSignerIdInput() {
		List<SignerIdDTO> signerIds = new ArrayList<SignerIdDTO>();
		for (String type : selectedDomain.getSignerIdTypes()) {
			SignerIdDTO id = new SignerIdDTO();
			id.setIdType(type);
			signerIds.add(id);
		}
		selectedConsent.getKey().setSignerIds(new HashSet<SignerIdDTO>(signerIds));
	}

	public void onAddConsent() {
		selectedConsent = new ConsentDTO(new ConsentKeyDTO());
		generateSignerIdInput();
		tableDisabled = true;
	}

	/**
	 * sets the template to null, so it gets repopulated ('see getTemplate()')
	 * 
	 * @param e
	 */
	public void onTemplateChanged(AjaxBehaviorEvent e) {
		template = null;
	}

	public void refresh() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			consents = cmManager.getAllConsentsForDomain(selectedDomain.getName());
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while retrieving all consents for: " + selectedDomain.getName(), e);
		}
		templateKeys = new ArrayList<ConsentTemplateKeyDTO>();
		moduleStates = null;
		freeTexts = null;
		template = null;
		tableDisabled = false;
		try {
			for (ConsentTemplateDTO template : cmManager.listConsentTemplates(selectedDomain.getName())) {
				templateKeys.add(template.getKey());
			}
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			logger.error("exception while list consent templates for: " + selectedDomain.getName(), e);
		}
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		session.removeAttribute("patient_signature");
		session.removeAttribute("physician_signature");
		selectedConsent = null;
		detailsConsent = null;
	}

	public void onConsentSelect(SelectEvent event) {
		ConsentDTO consent = ((ConsentDTO) event.getObject());
		updatePreview(consent);
	}

	private void updatePreview(ConsentDTO consent) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("preview_pdf", consent.getScanBase64());
		session.setAttribute("preview_name", consent.getKey().getConsentTemplateKey().getName() + "_"
				+ consent.getKey().getConsentTemplateKey().getVersion() + "_" + consent.getKey().getSignerIds() + "_"
				+ consent.getKey().getConsentDate());
	}

	@Override
	protected void onInit() {
		tableDisabled = false;
		logger = LoggerFactory.getLogger(TemplateControllerBean.class);
		consents = new ArrayList<ConsentDTO>();
		root = new DefaultTreeNode("root", null);
		for (DomainDTO domain : domains) {
			new DefaultTreeNode(domain.getName(), root);
		}

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
	 * Get-Method for consents.
	 * 
	 * @return consents
	 */
	public List<ConsentDTO> getConsents() {
		return consents;
	}

	/**
	 * Set-Method for consents.
	 * 
	 * @param consents
	 */
	public void setConsents(List<ConsentDTO> consents) {
		this.consents = consents;
	}

	/**
	 * Get-Method for domains.
	 * 
	 * @return domains
	 */
	public ConsentDTO getSelectedConsent() {
		return selectedConsent;
	}

	public void setSelectedConsent(ConsentDTO selectedConsent) {
		this.selectedConsent = selectedConsent;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	/**
	 * Get-Method for templateKeys.
	 * 
	 * @return templateKeys
	 */
	public List<ConsentTemplateKeyDTO> getTemplateKeys() {
		return templateKeys;
	}

	/**
	 * Set-Method for templateKeys.
	 * 
	 * @param templateKeys
	 */
	public void setTemplateKeys(List<ConsentTemplateKeyDTO> templateKeys) {
		this.templateKeys = templateKeys;
	}

	/**
	 * Get-method for template populates template if it is null
	 * 
	 * @return
	 */
	public ConsentTemplateDTO getTemplate() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (template == null && selectedConsent.getKey().getConsentTemplateKey() != null) {
			try {
				template = cmManager.getConsentTemplate(selectedConsent.getKey().getConsentTemplateKey());
				generateModuleStates();
				generateFreeTextValues();
			} catch (Exception e) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
				logger.error("exception while retrieving consent template for: " + selectedConsent.getKey(), e);
			}
		}
		return template;
	}

	public void setTemplate(ConsentTemplateDTO template) {
		this.template = template;
	}

	/**
	 * Get-Method for moduleStates.
	 * 
	 * @return moduleStates
	 */
	public List<ModuleState> getModuleStates() {
		return moduleStates;
	}

	/**
	 * Set-Method for moduleStates.
	 * 
	 * @param moduleStates
	 */
	public void setModuleStates(List<ModuleState> moduleStates) {
		this.moduleStates = moduleStates;
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
	 * Get-Method for freeTexts.
	 * 
	 * @return freeTexts
	 */
	public List<FreeTextValue> getFreeTexts() {
		return freeTexts;
	}

	/**
	 * Set-Method for freeTexts.
	 * 
	 * @param freeTexts
	 */
	public void setFreeTexts(List<FreeTextValue> freeTexts) {
		this.freeTexts = freeTexts;
	}

	public List<ConsentDTO> getFilteredConsents() {
		return filteredConsents;
	}

	public void setFilteredConsents(List<ConsentDTO> filteredConsents) {
		this.filteredConsents = filteredConsents;
	}

	public ConsentDTO getDetailsConsent() {
		return detailsConsent;
	}

	public void setDetailsConsent(ConsentDTO detailsConsent) {
		this.detailsConsent = detailsConsent;
	}
}
