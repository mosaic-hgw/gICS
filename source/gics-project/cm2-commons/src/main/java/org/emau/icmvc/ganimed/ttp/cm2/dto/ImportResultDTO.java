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
import java.util.Collections;
import java.util.List;

public class ImportResultDTO implements Serializable
{
	private List<ConsentTemplateKeyDTO> addedTemplates;
	private List<ModuleKeyDTO> addedModules;
	private List<PolicyKeyDTO> addedPolicies;
	private List<DomainDTO> addedDomains;
	private List<ConsentTemplateKeyDTO> updatedTemplates;
	private List<ModuleKeyDTO> updatedModules;
	private List<PolicyKeyDTO> updatedPolicies;
	private List<DomainDTO> updatedDomains;
	private List<ConsentTemplateKeyDTO> ignoredTemplates;
	private List<ModuleKeyDTO> ignoredModules;
	private List<PolicyKeyDTO> ignoredPolicies;
	private List<DomainDTO> ignoredDomains;

	public List<ConsentTemplateKeyDTO> getAddedTemplates()
	{
		return addedTemplates != null ? addedTemplates : Collections.emptyList();
	}

	public void setAddedTemplates(List<ConsentTemplateKeyDTO> addedTemplates)
	{
		this.addedTemplates = addedTemplates;
	}

	public List<ModuleKeyDTO> getAddedModules()
	{
		return addedModules != null ? addedModules : Collections.emptyList();
	}

	public void setAddedModules(List<ModuleKeyDTO> addedModules)
	{
		this.addedModules = addedModules;
	}

	public List<PolicyKeyDTO> getAddedPolicies()
	{
		return addedPolicies != null ? addedPolicies : Collections.emptyList();
	}

	public void setAddedPolicies(List<PolicyKeyDTO> addedPolicies)
	{
		this.addedPolicies = addedPolicies;
	}

	public List<DomainDTO> getAddedDomains()
	{
		return addedDomains != null ? addedDomains : Collections.emptyList();
	}

	public void setAddedDomains(List<DomainDTO> addedDomains)
	{
		this.addedDomains = addedDomains;
	}

	public List<ConsentTemplateKeyDTO> getUpdatedTemplates()
	{
		return updatedTemplates != null ? updatedTemplates : Collections.emptyList();
	}

	public void setUpdatedTemplates(List<ConsentTemplateKeyDTO> updatedTemplates)
	{
		this.updatedTemplates = updatedTemplates;
	}

	public List<ModuleKeyDTO> getUpdatedModules()
	{
		return updatedModules != null ? updatedModules : Collections.emptyList();
	}

	public void setUpdatedModules(List<ModuleKeyDTO> updatedModules)
	{
		this.updatedModules = updatedModules;
	}

	public List<PolicyKeyDTO> getUpdatedPolicies()
	{
		return updatedPolicies != null ? updatedPolicies : Collections.emptyList();
	}

	public void setUpdatedPolicies(List<PolicyKeyDTO> updatedPolicies)
	{
		this.updatedPolicies = updatedPolicies;
	}

	public List<DomainDTO> getUpdatedDomains()
	{
		return updatedDomains != null ? updatedDomains : Collections.emptyList();
	}

	public void setUpdatedDomains(List<DomainDTO> updatedDomains)
	{
		this.updatedDomains = updatedDomains;
	}

	public List<ConsentTemplateKeyDTO> getIgnoredTemplates()
	{
		return ignoredTemplates != null ? ignoredTemplates : Collections.emptyList();
	}

	public void setIgnoredTemplates(List<ConsentTemplateKeyDTO> ignoredTemplates)
	{
		this.ignoredTemplates = ignoredTemplates;
	}

	public List<ModuleKeyDTO> getIgnoredModules()
	{
		return ignoredModules != null ? ignoredModules : Collections.emptyList();
	}

	public void setIgnoredModules(List<ModuleKeyDTO> ignoredModules)
	{
		this.ignoredModules = ignoredModules;
	}

	public List<PolicyKeyDTO> getIgnoredPolicies()
	{
		return ignoredPolicies != null ? ignoredPolicies : Collections.emptyList();
	}

	public void setIgnoredPolicies(List<PolicyKeyDTO> ignoredPolicies)
	{
		this.ignoredPolicies = ignoredPolicies;
	}

	public List<DomainDTO> getIgnoredDomains()
	{
		return ignoredDomains != null ? ignoredDomains : Collections.emptyList();
	}

	public void setIgnoredDomains(List<DomainDTO> ignoredDomains)
	{
		this.ignoredDomains = ignoredDomains;
	}
}
