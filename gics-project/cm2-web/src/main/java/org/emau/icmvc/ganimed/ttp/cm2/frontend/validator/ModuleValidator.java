package org.emau.icmvc.ganimed.ttp.cm2.frontend.validator;

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.primefaces.model.DualListModel;

/**
 * validates a DualListModel of AssigneModuleDTO. Checks, if there are any duplicate Modules or Policies in the target List
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "modulesValidator")
@RequestScoped
public class ModuleValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2) throws ValidatorException {

		@SuppressWarnings("unchecked")
		ArrayList<AssignedModuleDTO> modules = new ArrayList<AssignedModuleDTO>(((DualListModel<AssignedModuleDTO>) arg2).getTarget());
		ResourceBundle messages = ResourceBundle.getBundle("messages");
		for (int i = 0; i < modules.size(); i++) {
			for (int j = i + 1; j < modules.size(); j++) {
				// check, if different version of same module
				if (modules.get(i).getModule().getKey().getName().equals(modules.get(j).getModule().getKey().getName())) {
					Object[] args = { modules.get(i).getModule().getKey().getName() };
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(
							messages.getString("template.message.error.duplicateModule")).format(args), ""));
				}
				// check for duplicate policies
				for (PolicyDTO policy : modules.get(i).getModule().getPolicies()) {
					if (modules.get(j).getModule().getPolicies().contains(policy)) {
						Object[] args = { modules.get(i).getModule().getKey().getName() };
						throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(
								messages.getString("template.message.error.duplicatePolicy")).format(args), ""));
					}
				}
			}
		}

	}

}
