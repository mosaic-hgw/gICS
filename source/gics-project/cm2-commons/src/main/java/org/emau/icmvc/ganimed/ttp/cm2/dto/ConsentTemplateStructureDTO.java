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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * structure of the modules of a consent template
 *
 * @author geidell
 *
 */
public class ConsentTemplateStructureDTO implements Serializable
{
	private static final long serialVersionUID = -2648718463539013805L;
	private final List<ModuleKeyDTO> firstLevelModules = new ArrayList<>();
	// jaxb kann nicht mit map of list umgehen ...
	private final Map<ModuleKeyDTO, ModuleKeyDTO[]> children = new HashMap<>();

	public ConsentTemplateStructureDTO()
	{}

	public ConsentTemplateStructureDTO(List<ModuleKeyDTO> firstLevelModules, Map<ModuleKeyDTO, ModuleKeyDTO[]> children)
	{
		setFirstLevelModules(firstLevelModules);
		setChildren(children);
	}

	public ConsentTemplateStructureDTO(ConsentTemplateStructureDTO dto)
	{
		this(dto.getFirstLevelModules(), dto.getChildren());
	}

	public List<ModuleKeyDTO> getFirstLevelModules()
	{
		return firstLevelModules;
	}

	public void setFirstLevelModules(List<ModuleKeyDTO> firstLevelModules)
	{
		if (this.firstLevelModules != firstLevelModules)
		{
			this.firstLevelModules.clear();
			if (firstLevelModules != null)
			{
				for (ModuleKeyDTO key : firstLevelModules)
				{
					this.firstLevelModules.add(new ModuleKeyDTO(key));
				}
			}
		}
	}

	public Map<ModuleKeyDTO, ModuleKeyDTO[]> getChildren()
	{
		return children;
	}

	public void setChildren(Map<ModuleKeyDTO, ModuleKeyDTO[]> children)
	{
		if (this.children != children)
		{
			this.children.clear();
			if (children != null)
			{
				for (Entry<ModuleKeyDTO, ModuleKeyDTO[]> entry : children.entrySet())
				{
					ModuleKeyDTO[] list = new ModuleKeyDTO[entry.getValue().length];
					for (int i = 0; i < entry.getValue().length; i++)
					{
						list[i] = new ModuleKeyDTO(entry.getValue()[i]);
					}
					this.children.put(new ModuleKeyDTO(entry.getKey()), list);
				}
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (children == null ? 0 : children.hashCode());
		result = prime * result + (firstLevelModules == null ? 0 : firstLevelModules.hashCode());
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
		ConsentTemplateStructureDTO other = (ConsentTemplateStructureDTO) obj;
		if (children == null)
		{
			if (other.children != null)
			{
				return false;
			}
		}
		else if (!children.equals(other.children))
		{
			return false;
		}
		if (firstLevelModules == null)
		{
			if (other.firstLevelModules != null)
			{
				return false;
			}
		}
		else if (!firstLevelModules.equals(other.firstLevelModules))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ConsentTemplateStructureDTO with ");
		sb.append(firstLevelModules.size());
		sb.append(" first level modules");
		return sb.toString();
	}
}
