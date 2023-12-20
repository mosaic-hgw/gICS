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

import java.io.Serial;
import java.io.Serializable;

/**
 * pk fuer ein modul
 *
 * @author geidell
 *
 */
public class ModuleKeyDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = 7365970101528445478L;
	private String domainName;
	private String name;
	private String version;

	public ModuleKeyDTO()
	{}

	public ModuleKeyDTO(String domainName, String name, String version)
	{
		super();
		setDomainName(domainName);
		setName(name);
		setVersion(version);
	}

	public ModuleKeyDTO(ModuleKeyDTO dto)
	{
		this(dto.getDomainName(), dto.getName(), dto.getVersion());
	}

	@Override
	public String getDomainName()
	{
		return domainName;
	}

	public void setDomainName(String domainName)
	{
		this.domainName = domainName != null ? domainName.intern() : null;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name != null ? name.intern() : null;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version != null ? version.intern() : null;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (domainName == null ? 0 : domainName.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (version == null ? 0 : version.hashCode());
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
		ModuleKeyDTO other = (ModuleKeyDTO) obj;
		if (domainName == null)
		{
			if (other.domainName != null)
			{
				return false;
			}
		}
		else if (!domainName.equals(other.domainName))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		if (version == null)
		{
			if (other.version != null)
			{
				return false;
			}
		}
		else if (!version.equals(other.version))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("module with name '");
		sb.append(name);
		sb.append("' for domain '");
		sb.append(domainName);
		sb.append("' in version ");
		sb.append(version);
		return sb.toString();
	}
}
