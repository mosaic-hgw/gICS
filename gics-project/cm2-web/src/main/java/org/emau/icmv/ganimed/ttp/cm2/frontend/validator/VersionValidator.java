package org.emau.icmv.ganimed.ttp.cm2.frontend.validator;

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
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.emau.icmv.ganimed.ttp.cm2.frontend.beans.TemplateControllerBean;
import org.emau.icmvc.ganimed.ttp.cm2.CM2Manager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;

/**
 * Validates Template Version
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "VersionValidator")
@RequestScoped
public class VersionValidator implements Validator {

	@ManagedProperty(value = "#{TemplateController}")
	private TemplateControllerBean templateController;

	@EJB(lookup = "java:global/gics/cm2-ejb/CM2ManagerBean!org.emau.icmvc.ganimed.ttp.cm2.CM2Manager")
	protected CM2Manager cmManager;

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2) throws ValidatorException {
		if (templateController.isEditMode()) {
			String templateName = templateController.getSelectedTemplate().getKey().getName();
			for (ConsentTemplateDTO template : templateController.getTemplates()) {
				if (template.getKey().getName().equals(templateName) && template.getKey().getVersion().equals(arg2)) {
					ResourceBundle messages = ResourceBundle.getBundle("messages");
					Object[] args = { template.getKey().getName(), template.getKey().getVersion() };
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(
							messages.getString("template.message.duplicateVersion")).format(args), ""));
				}
			}
		}

	}

	/**
	 * Get-Method for templateController.
	 * 
	 * @return templateController
	 */
	public TemplateControllerBean getTemplateController() {
		return templateController;
	}

	/**
	 * Set-Method for templateController.
	 * 
	 * @param templateController
	 */
	public void setTemplateController(TemplateControllerBean templateController) {
		this.templateController = templateController;
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

}
