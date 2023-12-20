package org.emau.icmvc.ganimed.ttp.cm2.dto;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO implements Serializable
{
	private static final long serialVersionUID = -2950194461126763415L;
	private final List<ConsentTemplateKeyDTO> addedTemplates = new ArrayList<>();
	private final List<ModuleKeyDTO> addedModules = new ArrayList<>();
	private final List<PolicyKeyDTO> addedPolicies = new ArrayList<>();
	private final List<DomainDTO> addedDomains = new ArrayList<>();
	private final List<ConsentTemplateKeyDTO> updatedTemplates = new ArrayList<>();
	private final List<ModuleKeyDTO> updatedModules = new ArrayList<>();
	private final List<PolicyKeyDTO> updatedPolicies = new ArrayList<>();
	private final List<DomainDTO> updatedDomains = new ArrayList<>();
	private final List<ConsentTemplateKeyDTO> ignoredTemplates = new ArrayList<>();
	private final List<ModuleKeyDTO> ignoredModules = new ArrayList<>();
	private final List<PolicyKeyDTO> ignoredPolicies = new ArrayList<>();
	private final List<DomainDTO> ignoredDomains = new ArrayList<>();

	public List<ConsentTemplateKeyDTO> getAddedTemplates()
	{
		return addedTemplates;
	}

	public void setAddedTemplates(List<ConsentTemplateKeyDTO> addedTemplates)
	{
		if (this.addedTemplates != addedTemplates)
		{
			this.addedTemplates.clear();
			if (addedTemplates != null)
			{
				for (ConsentTemplateKeyDTO key : addedTemplates)
				{
					this.addedTemplates.add(new ConsentTemplateKeyDTO(key));
				}
			}
		}
	}

	public List<ModuleKeyDTO> getAddedModules()
	{
		return addedModules;
	}

	public void setAddedModules(List<ModuleKeyDTO> addedModules)
	{
		if (this.addedModules != addedModules)
		{
			this.addedModules.clear();
			if (addedModules != null)
			{
				for (ModuleKeyDTO key : addedModules)
				{
					this.addedModules.add(new ModuleKeyDTO(key));
				}
			}
		}
	}

	public List<PolicyKeyDTO> getAddedPolicies()
	{
		return addedPolicies;
	}

	public void setAddedPolicies(List<PolicyKeyDTO> addedPolicies)
	{
		if (this.addedPolicies != addedPolicies)
		{
			this.addedPolicies.clear();
			if (addedPolicies != null)
			{
				for (PolicyKeyDTO key : addedPolicies)
				{
					this.addedPolicies.add(new PolicyKeyDTO(key));
				}
			}
		}
	}

	public List<DomainDTO> getAddedDomains()
	{
		return addedDomains;
	}

	public void setAddedDomains(List<DomainDTO> addedDomains)
	{
		if (this.addedDomains != addedDomains)
		{
			this.addedDomains.clear();
			if (addedDomains != null)
			{
				for (DomainDTO key : addedDomains)
				{
					this.addedDomains.add(new DomainDTO(key));
				}
			}
		}
	}

	public List<ConsentTemplateKeyDTO> getUpdatedTemplates()
	{
		return updatedTemplates;
	}

	public void setUpdatedTemplates(List<ConsentTemplateKeyDTO> updatedTemplates)
	{
		if (this.updatedTemplates != updatedTemplates)
		{
			this.updatedTemplates.clear();
			if (updatedTemplates != null)
			{
				for (ConsentTemplateKeyDTO key : updatedTemplates)
				{
					this.updatedTemplates.add(new ConsentTemplateKeyDTO(key));
				}
			}
		}
	}

	public List<ModuleKeyDTO> getUpdatedModules()
	{
		return updatedModules;
	}

	public void setUpdatedModules(List<ModuleKeyDTO> updatedModules)
	{
		if (this.updatedModules != updatedModules)
		{
			this.updatedModules.clear();
			if (updatedModules != null)
			{
				for (ModuleKeyDTO key : updatedModules)
				{
					this.updatedModules.add(new ModuleKeyDTO(key));
				}
			}
		}
	}

	public List<PolicyKeyDTO> getUpdatedPolicies()
	{
		return updatedPolicies;
	}

	public void setUpdatedPolicies(List<PolicyKeyDTO> updatedPolicies)
	{
		if (this.updatedPolicies != updatedPolicies)
		{
			this.updatedPolicies.clear();
			if (updatedPolicies != null)
			{
				for (PolicyKeyDTO key : updatedPolicies)
				{
					this.updatedPolicies.add(new PolicyKeyDTO(key));
				}
			}
		}
	}

	public List<DomainDTO> getUpdatedDomains()
	{
		return updatedDomains;
	}

	public void setUpdatedDomains(List<DomainDTO> updatedDomains)
	{
		if (this.updatedDomains != updatedDomains)
		{
			this.updatedDomains.clear();
			if (updatedDomains != null)
			{
				for (DomainDTO key : updatedDomains)
				{
					this.updatedDomains.add(new DomainDTO(key));
				}
			}
		}
	}

	public List<ConsentTemplateKeyDTO> getIgnoredTemplates()
	{
		return ignoredTemplates;
	}

	public void setIgnoredTemplates(List<ConsentTemplateKeyDTO> ignoredTemplates)
	{
		if (this.ignoredTemplates != ignoredTemplates)
		{
			this.ignoredTemplates.clear();
			if (ignoredTemplates != null)
			{
				for (ConsentTemplateKeyDTO key : ignoredTemplates)
				{
					this.ignoredTemplates.add(new ConsentTemplateKeyDTO(key));
				}
			}
		}
	}

	public List<ModuleKeyDTO> getIgnoredModules()
	{
		return ignoredModules;
	}

	public void setIgnoredModules(List<ModuleKeyDTO> ignoredModules)
	{
		if (this.ignoredModules != ignoredModules)
		{
			this.ignoredModules.clear();
			if (ignoredModules != null)
			{
				for (ModuleKeyDTO key : ignoredModules)
				{
					this.ignoredModules.add(new ModuleKeyDTO(key));
				}
			}
		}
	}

	public List<PolicyKeyDTO> getIgnoredPolicies()
	{
		return ignoredPolicies;
	}

	public void setIgnoredPolicies(List<PolicyKeyDTO> ignoredPolicies)
	{
		if (this.ignoredPolicies != ignoredPolicies)
		{
			this.ignoredPolicies.clear();
			if (ignoredPolicies != null)
			{
				for (PolicyKeyDTO key : ignoredPolicies)
				{
					this.ignoredPolicies.add(new PolicyKeyDTO(key));
				}
			}
		}
	}

	public List<DomainDTO> getIgnoredDomains()
	{
		return ignoredDomains;
	}

	public void setIgnoredDomains(List<DomainDTO> ignoredDomains)
	{
		if (this.ignoredDomains != ignoredDomains)
		{
			this.ignoredDomains.clear();
			if (ignoredDomains != null)
			{
				for (DomainDTO key : ignoredDomains)
				{
					this.ignoredDomains.add(new DomainDTO(key));
				}
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (addedDomains == null ? 0 : addedDomains.hashCode());
		result = prime * result + (addedModules == null ? 0 : addedModules.hashCode());
		result = prime * result + (addedPolicies == null ? 0 : addedPolicies.hashCode());
		result = prime * result + (addedTemplates == null ? 0 : addedTemplates.hashCode());
		result = prime * result + (ignoredDomains == null ? 0 : ignoredDomains.hashCode());
		result = prime * result + (ignoredModules == null ? 0 : ignoredModules.hashCode());
		result = prime * result + (ignoredPolicies == null ? 0 : ignoredPolicies.hashCode());
		result = prime * result + (ignoredTemplates == null ? 0 : ignoredTemplates.hashCode());
		result = prime * result + (updatedDomains == null ? 0 : updatedDomains.hashCode());
		result = prime * result + (updatedModules == null ? 0 : updatedModules.hashCode());
		result = prime * result + (updatedPolicies == null ? 0 : updatedPolicies.hashCode());
		result = prime * result + (updatedTemplates == null ? 0 : updatedTemplates.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
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
		ImportResultDTO other = (ImportResultDTO) obj;
		if (addedDomains == null)
		{
			if (other.addedDomains != null)
			{
				return false;
			}
		}
		else if (!addedDomains.equals(other.addedDomains))
		{
			return false;
		}
		if (addedModules == null)
		{
			if (other.addedModules != null)
			{
				return false;
			}
		}
		else if (!addedModules.equals(other.addedModules))
		{
			return false;
		}
		if (addedPolicies == null)
		{
			if (other.addedPolicies != null)
			{
				return false;
			}
		}
		else if (!addedPolicies.equals(other.addedPolicies))
		{
			return false;
		}
		if (addedTemplates == null)
		{
			if (other.addedTemplates != null)
			{
				return false;
			}
		}
		else if (!addedTemplates.equals(other.addedTemplates))
		{
			return false;
		}
		if (ignoredDomains == null)
		{
			if (other.ignoredDomains != null)
			{
				return false;
			}
		}
		else if (!ignoredDomains.equals(other.ignoredDomains))
		{
			return false;
		}
		if (ignoredModules == null)
		{
			if (other.ignoredModules != null)
			{
				return false;
			}
		}
		else if (!ignoredModules.equals(other.ignoredModules))
		{
			return false;
		}
		if (ignoredPolicies == null)
		{
			if (other.ignoredPolicies != null)
			{
				return false;
			}
		}
		else if (!ignoredPolicies.equals(other.ignoredPolicies))
		{
			return false;
		}
		if (ignoredTemplates == null)
		{
			if (other.ignoredTemplates != null)
			{
				return false;
			}
		}
		else if (!ignoredTemplates.equals(other.ignoredTemplates))
		{
			return false;
		}
		if (updatedDomains == null)
		{
			if (other.updatedDomains != null)
			{
				return false;
			}
		}
		else if (!updatedDomains.equals(other.updatedDomains))
		{
			return false;
		}
		if (updatedModules == null)
		{
			if (other.updatedModules != null)
			{
				return false;
			}
		}
		else if (!updatedModules.equals(other.updatedModules))
		{
			return false;
		}
		if (updatedPolicies == null)
		{
			if (other.updatedPolicies != null)
			{
				return false;
			}
		}
		else if (!updatedPolicies.equals(other.updatedPolicies))
		{
			return false;
		}
		if (updatedTemplates == null)
		{
			if (other.updatedTemplates != null)
			{
				return false;
			}
		}
		else if (!updatedTemplates.equals(other.updatedTemplates))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "ImportResultDTO [addedTemplates=" + addedTemplates + ", addedModules=" + addedModules + ", addedPolicies=" + addedPolicies + ", addedDomains=" + addedDomains + ", updatedTemplates="
				+ updatedTemplates + ", updatedModules=" + updatedModules + ", updatedPolicies=" + updatedPolicies + ", updatedDomains=" + updatedDomains + ", ignoredTemplates=" + ignoredTemplates
				+ ", ignoredModules=" + ignoredModules + ", ignoredPolicies=" + ignoredPolicies + ", ignoredDomains=" + ignoredDomains + "]";
	}
}
