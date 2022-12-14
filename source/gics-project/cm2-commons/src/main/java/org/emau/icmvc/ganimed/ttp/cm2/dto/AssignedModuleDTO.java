package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
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
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
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
import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;

/**
 * objekt fuer die m-n tabelle consent template <-> modul
 *
 * @author geidell
 *
 */
public class AssignedModuleDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = -4054297467017230728L;
	private ModuleDTO module;
	private boolean mandatory;
	private ConsentStatus defaultConsentStatus;
	private List<ConsentStatus> displayCheckboxes = new ArrayList<>();
	private int orderNumber;
	private ModuleKeyDTO parent;
	private String comment;
	private String externProperties;
	private ExpirationPropertiesDTO expirationProperties;

	public AssignedModuleDTO()
	{
		super(null);
	}

	public AssignedModuleDTO(ModuleDTO module, ModuleKeyDTO parent)
	{
		super(null);
		this.module = module;
		this.parent = parent;
	}

	public AssignedModuleDTO(ModuleDTO module, boolean mandatory, ConsentStatus defaultConsentStatus, List<ConsentStatus> displayCheckboxes,
			int orderNumber, ModuleKeyDTO parent, String comment, String externProperties, ExpirationPropertiesDTO expirationProperties, String fhirID)
	{
		super(fhirID);
		this.module = module;
		this.mandatory = mandatory;
		this.defaultConsentStatus = defaultConsentStatus;
		if (displayCheckboxes != null)
		{
			this.displayCheckboxes = displayCheckboxes;
		}
		this.orderNumber = orderNumber;
		this.parent = parent;
		this.comment = comment;
		this.externProperties = externProperties;
		this.expirationProperties = expirationProperties;
	}

	public ModuleDTO getModule()
	{
		return module;
	}

	public void setModule(ModuleDTO module)
	{
		this.module = module;
	}

	public boolean getMandatory()
	{
		return mandatory;
	}

	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}

	public ConsentStatus getDefaultConsentStatus()
	{
		return defaultConsentStatus;
	}

	public void setDefaultConsentStatus(ConsentStatus defaultConsentStatus)
	{
		this.defaultConsentStatus = defaultConsentStatus;
	}

	public List<ConsentStatus> getDisplayCheckboxes()
	{
		return displayCheckboxes;
	}

	public void setDisplayCheckboxes(List<ConsentStatus> displayCheckboxes)
	{
		if (displayCheckboxes != null)
		{
			this.displayCheckboxes = displayCheckboxes;
		}
	}

	public int getOrderNumber()
	{
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber)
	{
		this.orderNumber = orderNumber;
	}

	public ModuleKeyDTO getParent()
	{
		return parent;
	}

	public void setParent(ModuleKeyDTO parent)
	{
		this.parent = parent;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	public ExpirationPropertiesDTO getExpirationProperties()
	{
		return expirationProperties;
	}

	public void setExpirationProperties(ExpirationPropertiesDTO expirationProperties)
	{
		this.expirationProperties = expirationProperties;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (defaultConsentStatus == null ? 0 : defaultConsentStatus.hashCode());
		result = prime * result + (displayCheckboxes == null ? 0 : displayCheckboxes.hashCode());
		result = prime * result + (expirationProperties == null ? 0 : expirationProperties.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (mandatory ? 1231 : 1237);
		result = prime * result + (module == null ? 0 : module.hashCode());
		result = prime * result + orderNumber;
		result = prime * result + (parent == null ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AssignedModuleDTO other = (AssignedModuleDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (defaultConsentStatus != other.defaultConsentStatus)
		{
			return false;
		}
		if (displayCheckboxes == null)
		{
			if (other.displayCheckboxes != null)
			{
				return false;
			}
		}
		else if (!displayCheckboxes.equals(other.displayCheckboxes))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (mandatory != other.mandatory)
		{
			return false;
		}
		if (module == null)
		{
			if (other.module != null)
			{
				return false;
			}
		}
		else if (!module.equals(other.module))
		{
			return false;
		}
		if (orderNumber != other.orderNumber)
		{
			return false;
		}
		if (parent == null)
		{
			if (other.parent != null)
			{
				return false;
			}
		}
		else if (!parent.equals(other.parent))
		{
			return false;
		}
		return true;
	}

	public boolean equalsForFhirSerice(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AssignedModuleDTO other = (AssignedModuleDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (defaultConsentStatus != other.defaultConsentStatus)
		{
			return false;
		}
		if (displayCheckboxes == null)
		{
			if (other.displayCheckboxes != null)
			{
				return false;
			}
		}
		else if (!displayCheckboxes.equals(other.displayCheckboxes))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (mandatory != other.mandatory)
		{
			return false;
		}
		if (module == null)
		{
			if (other.module != null)
			{
				return false;
			}
		}
		else if (!module.equalsForFhirSerice(other.module))
		{
			return false;
		}
		if (orderNumber != other.orderNumber)
		{
			return false;
		}
		if (parent == null)
		{
			if (other.parent != null)
			{
				return false;
			}
		}
		else if (!parent.equals(other.parent))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(module == null ? "MODULE IS NULL" : module.toString());
		sb.append(", default value: '");
		sb.append(defaultConsentStatus == null ? "'null'" : defaultConsentStatus.toString());
		sb.append("' is mandatory: ");
		sb.append(mandatory);
		sb.append(", has order number ");
		sb.append(orderNumber);
		sb.append(", as parent: '");
		sb.append(parent);
		sb.append("' should show checkboxes for");
		for (ConsentStatus consentStatus : displayCheckboxes)
		{
			sb.append(" ");
			sb.append(consentStatus.toString());
		}
		sb.append("', has as comment '");
		sb.append(comment);
		sb.append("' externProperties '");
		sb.append(externProperties);
		sb.append("' and expirationProperties '");
		sb.append(expirationProperties);
		sb.append("'");
		sb.append(super.toString());
		return sb.toString();
	}
}
