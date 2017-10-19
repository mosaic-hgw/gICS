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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.CM2Manager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.slf4j.Logger;

/**
 * abstract class for managed beans manages most of recources and some general Fields
 * 
 * @author weiherg
 * 
 */
@ManagedBean
public abstract class CMController {
	protected static final ConsentStatus[] mandatoryConsentStates = { ConsentStatus.ACCEPTED, ConsentStatus.DECLINED, ConsentStatus.INVALIDATED,
			ConsentStatus.REVOKED };

	@EJB(lookup = "java:global/gics/cm2-ejb/CM2ManagerBean!org.emau.icmvc.ganimed.ttp.cm2.CM2Manager")
	protected CM2Manager cmManager;
	protected ResourceBundle messages;

	protected Logger logger;

	// Lists for Domain selection
	protected List<DomainDTO> domains;
	protected List<DomainDTO> filteredDomains;

	// determines if a new entity is created or an old one edited
	protected boolean editMode;

	@PostConstruct
	public void init() {
		domains = cmManager.listDomains();
		messages = ResourceBundle.getBundle("messages");
		editMode = false;
		onInit();
	}

	/**
	 * Method to pass ConsentStatus to Servlet
	 * 
	 * @return
	 */
	public ConsentStatus[] getConsentStates() {
		return ConsentStatus.values();
	}

	public Object[] getMandatoryConsentStates(boolean mandatory) {
		if (mandatory) {
			ArrayList<ConsentStatus> states = new ArrayList<ConsentStatus>();
			for (ConsentStatus status : getConsentStates()) {
				if (status.getConsentStatusType() != ConsentStatusType.UNKNOWN) {
					states.add(status);
				}
			}
			return states.toArray();
		} else {
			return getConsentStates();
		}
	}

	/**
	 * maps labels to ConsentStatus values
	 * 
	 * @param status
	 * @return label for the status
	 */
	public String getConsentStatusLabel(ConsentStatus status) {
		switch (status) {
			case ACCEPTED:
				return "Accepted";
			case DECLINED:
				return "Declined";
			case UNKNOWN:
				return "Unkown";
			case NOT_ASKED:
				return "Not Asked";
			case NOT_CHOOSEN:
				return "Not Choosen";
			case INVALIDATED:
				return "Invalidated";
			case REVOKED:
				return "Revoked";
			default:
				return "";
		}
	}

	/**
	 * adds an Error message in the context and an error log for an Exception
	 * 
	 * @param context
	 *            current context
	 * @param e
	 *            Exception
	 * @param message
	 *            Error message to display and log
	 */
	protected void handleDefaultError(FacesContext context, Exception e, String message) {
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
		if (logger.isErrorEnabled()) {
			logger.error("", e);
		}
	}

	/**
	 * adds an Error message in the context and an error log for an Exception
	 * 
	 * @param e
	 *            Exception
	 * @param message
	 *            Error message to display and log
	 */
	protected void handleDefaultError(Exception e, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		handleDefaultError(context, e, message);
	}

	/**
	 * hook for custom init code
	 */
	protected abstract void onInit();

	/**
	 * Get-Method for domains.
	 * 
	 * @return domains
	 */
	public List<DomainDTO> getDomains() {
		return domains;
	}

	/**
	 * Set-Method for domains.
	 * 
	 * @param domains
	 */
	public void setDomains(List<DomainDTO> domains) {
		this.domains = domains;
	}

	/**
	 * Get-Method for cmManager.
	 * 
	 * @return cmManager
	 */
	public CM2Manager getCmManager() {
		return cmManager;
	}

	/**
	 * Set-Method for cmManager.
	 * 
	 * @param cmManager
	 */
	public void setCmManager(CM2Manager cmManager) {
		this.cmManager = cmManager;
	}

	/**
	 * Get-Method for editMode.
	 * 
	 * @return editMode
	 */
	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Set-Method for editMode.
	 * 
	 * @param editMode
	 */
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public List<DomainDTO> getFilteredDomains() {
		return filteredDomains;
	}

	public void setFilteredDomains(List<DomainDTO> filteredDomains) {
		this.filteredDomains = filteredDomains;
	}

}
