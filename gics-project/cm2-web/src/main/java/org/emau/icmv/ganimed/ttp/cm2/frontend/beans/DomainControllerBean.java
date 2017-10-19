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
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmv.ganimed.ttp.cm2.frontend.util.Property;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for Domains View
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "DomainController")
@ViewScoped
public class DomainControllerBean extends CMController {
	private DomainDTO selectedDomain;
	private List<Property> properties;
	private boolean editMode;
	private boolean tableDisabled;
	private String signerTypes;

	@Override
	protected void onInit() {
		logger = LoggerFactory.getLogger(DomainControllerBean.class);
		properties = new ArrayList<Property>();
		refresh();
	}

	/**
	 * resets the properties input
	 */
	public void resetProperties() {
		properties.clear();
		for (DomainProperties property : DomainProperties.values()) {
			properties.add(new Property(property.toString()));
		}
	}

	public void refresh() {
		tableDisabled = false;
		selectedDomain = null;
		domains = cmManager.listDomains();

	}

	public void onSaveDomain() throws VersionConverterClassException, UnknownDomainException {
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { selectedDomain.getLabel() };

		// generate Signer Types
		String[] signerTypesArray = signerTypes.split(",", -1);
		for (String type : signerTypesArray) {
			type = type.trim();
		}
		selectedDomain.setSignerIdTypes(Arrays.asList(signerTypesArray));

		// build Properties String
		StringBuilder sb = new StringBuilder();
		for (Property p : properties) {
			if (!p.getValue().isEmpty()) {
				sb.append(p.getLabel());
				sb.append('=');
				sb.append(p.getValue());
				sb.append(';');
			}
		}
		selectedDomain.setProperties(sb.toString());

		// persist Domain object
		try {
			if (editMode) {
				cmManager.updateDomain(selectedDomain.getName(), selectedDomain.getLabel(), selectedDomain.getProperties(),
						selectedDomain.getComment());
				context.addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("domain.message.domainUpdated"))
								.format(args), ""));
			} else {
				selectedDomain.setName(selectedDomain.getLabel());
				cmManager.addDomain(selectedDomain);
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO,
								new MessageFormat(messages.getString("domain.message.domainAdded")).format(args), ""));
			}
			refresh();
		} catch (DuplicateEntryException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			if (logger.isErrorEnabled()) {
				logger.error("domain does already exist: ", e);
			}
		}
	}

	public void onDeleteDomain() throws UnknownDomainException {
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { selectedDomain.getName() };
		try {
			cmManager.deleteDomain(selectedDomain.getName());
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("domain.message.domainDeleted")).format(args),
							""));
			refresh();
		} catch (ObjectInUseException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
			if (logger.isErrorEnabled()) {
				logger.error("domain in use: ", e);
			}
		}
	}

	public void onEditDomain() {
		tableDisabled = true;
		editMode = true;
		resetProperties();
		for (String propertyString : selectedDomain.getProperties().split(";")) {
			String[] tmp = propertyString.split("=");
			for (Property property : properties) {
				if (property.getLabel().equals(tmp[0])) {
					property.setValue(tmp[1]);
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String type : selectedDomain.getSignerIdTypes()) {
			if (sb.length() != 0) {
				sb.append(',');
			}
			sb.append(type);
		}
		signerTypes = sb.toString();
	}

	public void onNewDomain() {

		editMode = false;
		tableDisabled = true;
		resetProperties();
		selectedDomain = new DomainDTO();
		selectedDomain.setSignerIdTypes(new ArrayList<String>());
		signerTypes = null;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	public List<DomainDTO> getDomains() {
		return domains;
	}

	public void setDomains(List<DomainDTO> domains) {
		this.domains = domains;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public boolean isTableDisabled() {
		return tableDisabled;
	}

	public void setTableDisabled(boolean tableDisabled) {
		this.tableDisabled = tableDisabled;
	}

	public String getSignerTypes() {
		return signerTypes;
	}

	public void setSignerTypes(String signerTypes) {
		this.signerTypes = signerTypes;
	}
}
