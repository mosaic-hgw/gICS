package org.emau.icmvc.ganimed.ttp.cm2.frontend.validator;

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

import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.primefaces.model.DualListModel;

/**
 * validates a DualListModel of PolicyDTO. Checks, if there are any duplicate Policies in the target List
 * 
 * @author weiherg
 * @author weiherg
 * 
 */
@ManagedBean(name = "policiesValidator")
@RequestScoped
public class PoliciesValidator implements Validator
{
	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2) throws ValidatorException
	{
		@SuppressWarnings("unchecked")
		ArrayList<PolicyDTO> policies = new ArrayList<PolicyDTO>(((DualListModel<PolicyDTO>) arg2).getTarget());
		for (int i = 0; i < policies.size(); i++)
		{
			for (int j = i + 1; j < policies.size(); j++)
			{
				if (policies.get(i).getKey().getName().equals(policies.get(j).getKey().getName()))
				{
					ResourceBundle messages = ResourceBundle.getBundle("messages");
					Object[] args = { policies.get(i).getKey().getName() };
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(
							messages.getString("template.message.error.duplicatePolicy")).format(args), ""));
				}
			}
		}

	}

}
