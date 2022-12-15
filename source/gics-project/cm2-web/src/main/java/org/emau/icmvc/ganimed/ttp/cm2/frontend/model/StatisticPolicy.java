package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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

import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;

public class StatisticPolicy extends PolicyDTO
{
	private static final long serialVersionUID = 8792640581937939297L;
	long accepted;
	long declined;
	long unknown;
	int acceptedPercentage;

	public StatisticPolicy(PolicyDTO p, long accepted, long declined, long unknown)
	{
		super(p.getKey(), p.getComment(), p.getExternProperties(), p.getLabel(), p.getFinalised(), p.getCreationDate(), p.getUpdateDate(), p.getFhirID());
		this.accepted = accepted;
		this.declined = declined;
		this.unknown = unknown;
	}

	public Long getAccepted()
	{
		return accepted;
	}

	public long getUnknown()
	{
		return unknown;
	}

	public int getAcceptedPercentageWithoutUnknown()
	{
		return getAcceptedPercentage(false);
	}

	public Integer getAcceptedPercentage(boolean includeUnknown)
	{
		long all = accepted + declined + (includeUnknown ? unknown : 0);
		return (int) ((double) accepted / all * 100);
	}

	public Long getAll(boolean includeUnknown)
	{
		return accepted + declined + (includeUnknown ? unknown : 0L);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof StatisticPolicy))
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		StatisticPolicy that = (StatisticPolicy) o;

		if (accepted != that.accepted)
		{
			return false;
		}
		if (declined != that.declined)
		{
			return false;
		}
		if (unknown != that.unknown)
		{
			return false;
		}
		return acceptedPercentage == that.acceptedPercentage;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (int) (accepted ^ accepted >>> 32);
		result = 31 * result + (int) (declined ^ declined >>> 32);
		result = 31 * result + (int) (unknown ^ unknown >>> 32);
		result = 31 * result + acceptedPercentage;
		return result;
	}
}
